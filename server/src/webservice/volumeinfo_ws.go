package webservice

import (
	ae "appengine"
	ds "appengine/datastore"
	"cache"
	"data"
	"encoding/json"
	"errors"
	"googlebooks"
	"isbn13"
	"net/http"
	"persistence"
)

type Call struct {
	Context  ae.Context
	Request  *http.Request
	Response http.ResponseWriter
}

func (call *Call) ReportError(err error) {
	if me, ok := err.(ae.MultiError); ok {
		for _, next := range me {
			call.ReportError(next)
		}
	} else {
		call.Context.Errorf("Error reported: %s", err.Error())
	}
}

func (call *Call) DetermineCountry() string {
	header := call.Request.Header["X-AppEngine-Country"]
	if len(header) > 0 {
		return header[0]
	}

	query := call.Request.URL.Query()["country"]

	if len(query) > 0 {
		return query[0]
	}

	return "unknown"
}

type CallHandler func(*Call) error

func (function CallHandler) ServeHTTP(w http.ResponseWriter, rq *http.Request) {
	call := Call{
		Context:  ae.NewContext(rq),
		Request:  rq,
		Response: w,
	}

	err := function(&call)

	if err != nil {
		call.ReportError(err)
	}
}

func init() {
	http.Handle("/volumes/", CallHandler(serveVolumeSingle))
	http.Handle("/volumes", CallHandler(serveVolumeBulk))
}

func serveVolumeBulk(call *Call) (err error) {
	var shelf *data.Bookshelf
	status := http.StatusInternalServerError
	switch call.Request.Method {
	case "GET":
		shelf, err = persistence.LookupBookshelf(call.Context)
	case "PUT":
		shelf, err = putVolumeBulk(call)
	default:
		status = http.StatusMethodNotAllowed
		err = errors.New("Unsupported operation. Only GET, PUT and DELETE methods are allowed")
	}

	if err == nil {
		encode := json.NewEncoder(call.Response)
		call.Response.Header().Add("Content-Type", "application/json")
		call.Response.WriteHeader(http.StatusOK)
		err = encode.Encode(shelf)
	} else {
		call.Response.WriteHeader(status)
	}

	return err
}

func putVolumeBulk(call *Call) (shelf *data.Bookshelf, err error) {
	decode := json.NewDecoder(call.Request.Body)
	shelf = new(data.Bookshelf)
	if err = decode.Decode(shelf); err == nil {
		err = persistence.StoreBookshelf(call.Context, shelf)
	}

	return
}

func serveVolumeSingle(call *Call) error {
	status := http.StatusBadRequest
	isbn, err := isbn13.New(call.Request.URL.Path[9:])
	var book *data.BookMetaData

	if err == nil {
		status = http.StatusInternalServerError
		switch call.Request.Method {
		case "GET":
			book, err = compositeISBNLookup(call.Context, call.DetermineCountry(), isbn)
		case "PUT":
			book, err = putVolumeSingle(call, isbn)
		default:
			status = http.StatusMethodNotAllowed
			err = errors.New("Unsupported operation. Only GET, PUT and DELETE methods are allowed")
		}
	}

	if err == nil {
		encode := json.NewEncoder(call.Response)

		call.Response.Header().Add("Content-Type", "application/json")
		call.Response.WriteHeader(http.StatusOK)
		err = encode.Encode(book)
	} else {
		call.Response.WriteHeader(status)
	}

	return err
}

func putVolumeSingle(call *Call, isbn isbn13.ISBN13) (*data.BookMetaData, error) {
	shelf, err := persistence.LookupBookshelf(call.Context)
	var info *data.BookMetaData

	if err == ds.ErrNoSuchEntity {
		err = nil
		shelf = new(data.Bookshelf)
	}

	if err == nil {
		decode := json.NewDecoder(call.Request.Body)
		info = new(data.BookMetaData)
		if err = decode.Decode(info); err == nil {
			if ptr, _ := shelf.LookupInfo(isbn); ptr != nil {
				*ptr = *info
			} else {
				shelf.Books = append(shelf.Books, *info)
			}

			err = persistence.StoreBookshelf(call.Context, shelf)
		}
	}
	return info, err
}

func compositeISBNLookup(ctx ae.Context, country string, isbn isbn13.ISBN13) (resp *data.BookMetaData, err error) {
	funcs := []func(ae.Context, string, isbn13.ISBN13) (*data.BookMetaData, error){
		func(ctx ae.Context, country string, isbn isbn13.ISBN13) (*data.BookMetaData, error) {
			shelf, err := persistence.LookupBookshelf(ctx)
			if err == nil {
				return shelf.LookupInfo(isbn)
			}

			return nil, err
		},
		cache.LookupISBN,
		persistence.LookupISBN,
		func(ctx ae.Context, country string, isbn isbn13.ISBN13) (*data.BookMetaData, error) {
			r, err := googlebooks.LookupISBN(ctx, country, isbn)

			if err == nil && r != nil {
				go cache.CacheISBNResult(ctx, country, isbn, r)
				go persistence.StoreISBNResult(ctx, country, isbn, r)
			}

			return r, err
		},
	}

	var multi ae.MultiError

	for i, f := range funcs {
		if result, err := f(ctx, country, isbn); err == nil && result != nil {
			ctx.Debugf("Found info %v after %d iterations\n", result, i)
			return result, nil
		} else if err != nil {
			multi = append(multi, err)
		}
	}

	return nil, multi
}
