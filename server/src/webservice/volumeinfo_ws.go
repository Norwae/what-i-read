package webservice

import (
	ae "appengine"
	ds "appengine/datastore"
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

func (call *Call) writeError(err error, strings []string) []string {
	if me, ok := err.(ae.MultiError); ok {
		for _, next := range me {
			strings = call.writeError(next, strings)
		}
	} else {
		str := err.Error()
		call.Context.Errorf("Error reported: %s", str)
		strings = append(strings, str)
	}

	return strings
}

func (call *Call) DetermineCountry() string {
	header := call.Request.Header["X-Appengine-Country"]
	if len(header) > 0 {
		country := header[0]

		if country != "ZZ" {
			return country
		}
		call.Context.Debugf("Falling back to user-supplied country in dev server")
	} else {
		call.Context.Warningf("Could not auto-detect country (available headers: %v), falling back to query parameter", call.Request.Header)
	}

	query := call.Request.URL.Query()["country"]

	if len(query) > 0 {
		return query[0]
	}

	return "unknown"
}

type CallHandler func(*Call) (interface{}, error)

func (function CallHandler) ServeHTTP(w http.ResponseWriter, rq *http.Request) {
	call := Call{
		Context:  ae.NewContext(rq),
		Request:  rq,
		Response: w,
	}

	call.Response.Header().Add("Content-Type", "application/json;charset=UTF-8")
	result, err := function(&call)
	encoder := json.NewEncoder(w)

	if err != nil {
		errors := call.writeError(err, nil)
		call.Response.WriteHeader(http.StatusInternalServerError)
		encoder.Encode(map[string][]string{
			"errors": errors,
		})
	} else {
		encoder.Encode(result)
	}
}

func init() {
	http.Handle("/volumes/", CallHandler(serveVolumeSingle))
	http.Handle("/volumes", CallHandler(serveVolumeBulk))
}

func serveVolumeBulk(call *Call) (interface{}, error) {
	var shelf *data.Bookshelf
	var err error

	switch call.Request.Method {
	case "GET":
		shelf, err = persistence.LookupBookshelf(call.Context)
	case "PUT":
		shelf, err = putVolumeBulk(call)
	default:
		err = errors.New("Unsupported operation. Only GET, PUT and DELETE methods are allowed")
	}

	return shelf, err
}

func putVolumeBulk(call *Call) (shelf *data.Bookshelf, err error) {
	decode := json.NewDecoder(call.Request.Body)
	shelf = new(data.Bookshelf)
	if err = decode.Decode(shelf); err == nil {
		err = persistence.StoreBookshelf(call.Context, shelf)
	}

	return
}

func serveVolumeSingle(call *Call) (interface{}, error) {
	isbn, err := isbn13.New(call.Request.URL.Path[9:])
	var book *data.BookMetaData

	if err == nil {
		switch call.Request.Method {
		case "GET":
			book, err = compositeISBNLookup(call.Context, call.DetermineCountry(), isbn)
		case "PUT":
			book, err = putVolumeSingle(call, isbn)
		default:
			err = errors.New("Unsupported operation. Only GET, PUT and DELETE methods are allowed")
		}
	}

	return book, err
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
			if ptr := shelf.LookupInfo(isbn); ptr != nil {
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
	var errors ae.MultiError
	if shelf, err := persistence.LookupBookshelf(ctx); err == nil {
		if book := shelf.LookupInfo(isbn); book != nil {
			return book, nil
		}
	} else {
		errors = append(errors, err)
	}

	if book, err := persistence.LookupISBN(ctx, country, isbn); err == nil {
		return book, nil
	} else {
		errors = append(errors, err)
	}

	if r, err := googlebooks.LookupISBN(ctx, country, isbn); err == nil {
		persistence.StoreISBNResult(ctx, country, isbn, r)
		return r, nil
	} else {
		errors = append(errors, err)
	}

	return nil, errors
}
