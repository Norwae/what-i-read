package webservice

import (
	ae "appengine"
	"data"
	"encoding/json"
	"errors"
	"googlebooks"
	"isbn13"
	"net/http"
	"persistence"
	"persistence/tx"
)

type Call struct {
	Context    ae.Context
	Request    *http.Request
	Response   http.ResponseWriter
	StatusCode int
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

	if err != nil {
		if call.StatusCode == 0 {
			call.StatusCode = http.StatusInternalServerError
		}
		errors := call.writeError(err, nil)
		result = map[string][]string{"errors": errors}
	} else {
		if call.StatusCode == 0 {
			call.StatusCode = http.StatusOK
		}
	}

	encoder := json.NewEncoder(w)
	call.Response.WriteHeader(call.StatusCode)
	call.Context.Debugf("Returning %v to client", result)
	encoder.Encode(result)
}

func init() {
	http.Handle("/volumes/", CallHandler(serveVolumeSingle))
	http.Handle("/volumes", CallHandler(serveVolumeBulk))
	http.Handle("/ping", CallHandler(servePing))
}

func servePing(call *Call) (interface{}, error) {
	call.Context.Infof("Ping called")
	if call.Request.Method == "GET" {
		shelf, err := persistence.LookupBookshelfMeta(call.Context)

		return shelf.LastUpdate, err
	} else {
		call.StatusCode = http.StatusMethodNotAllowed

		call.Response.Header().Add("Allow", "GET")
		err := errors.New("Unsupported operation. Only GET method is allowed")
		return nil, err
	}
}

func serveVolumeBulk(call *Call) (interface{}, error) {
	var shelf *data.LookupReply
	var err error

	switch call.Request.Method {
	case "GET":
		shelf, err = getVolumeBulk(call)
	default:
		call.StatusCode = http.StatusMethodNotAllowed

		call.Response.Header().Add("Allow", "GET")
		err = errors.New("Unsupported operation. Only GET method is allowed")
	}

	return shelf, err
}

func getVolumeBulk(call *Call) (reply *data.LookupReply, err error) {
	var shelf *data.Bookshelf

	if shelf, err = persistence.LookupBookshelf(call.Context); err == nil {
		call.Context.Debugf("Bookshelf contains %d volumes", len(shelf.Books))

		infos := shelf.Books

		for _, str := range call.Request.URL.Query()["search"] {
			matches := shelf.Search(str)

			infos = make([]data.BookMetaData, len(matches))
			for i, ptr := range matches {
				infos[i] = *ptr
			}

			call.Context.Debugf("Filtered by \"%s\", down to %d entries: %v", str, len(infos), infos)
		}

		reply = &data.LookupReply{
			Count:     len(infos),
			BookInfos: shelf.Books,
		}
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
		case "DELETE":
			book, err = deleteVolumeSingle(call, isbn)
		default:
			call.StatusCode = http.StatusMethodNotAllowed
			call.Response.Header().Add("Allow", "GET, PUT, DELETE")
			err = errors.New("Unsupported operation. Only GET, PUT and DELETE methods are allowed")
		}
	} else {
		call.StatusCode = http.StatusNotFound
	}

	return book, err
}

func deleteVolumeSingle(call *Call, isbn isbn13.ISBN13) (info *data.BookMetaData, err error) {
	call.Context.Infof("Deleting ISBN %d", isbn)
	err = persistence.UpdateBookshelf(call.Context, func(t *tx.Transaction, shelf *data.Bookshelf) error {
		for i := range shelf.Books {
			if ptr := &shelf.Books[i]; ptr.ISBN == isbn.String() {
				shelf.Books = append(shelf.Books[:i], shelf.Books[i+1:]...)
				t.Delete = []tx.KeyDeriver{ptr}
				return nil
			}

		}
		call.StatusCode = http.StatusNotFound
		return errors.New("ISBN not found")
	})

	return
}

func putVolumeSingle(call *Call, isbn isbn13.ISBN13) (info *data.BookMetaData, err error) {
	call.Context.Debugf("Updating %d", isbn)
	decode := json.NewDecoder(call.Request.Body)
	info = new(data.BookMetaData)
	if err = decode.Decode(info); err == nil {
		call.Context.Debugf("Raw decoded entity: %v", info)
		info.ISBN = isbn.String()
		normalize(call, info)
		call.Context.Debugf("Normalized decoded entity: %v", info)

		persistence.UpdateBookshelf(call.Context, func(t *tx.Transaction, shelf *data.Bookshelf) error {

			if ptr := shelf.LookupInfo(isbn); ptr != nil {
				*ptr = *info
				t.Put = []tx.KeyDeriver{ptr}
			} else {
				info.Parent = shelf
				shelf.Books = append(shelf.Books, *info)
				t.Put = []tx.KeyDeriver{info}
			}

			return nil
		})
	}
	return info, err
}

func normalize(call *Call, info *data.BookMetaData) {
	var isbn isbn13.ISBN13
	var err error
	if isbn, err = isbn13.New(info.ISBN); err == nil {
		var base *data.BookMetaData
		if base, err = persistence.LookupISBN(call.Context, call.DetermineCountry(), isbn); err == nil {
			info.Volume.Images = base.Volume.Images
			return
		} else {
			info.Volume.Images = data.ImageLinks{}
		}
	} else {
		call.Context.Errorf("Could not normalize data for %s: %s", info.ISBN, err.Error())
	}
}

func compositeISBNLookup(ctx ae.Context, country string, isbn isbn13.ISBN13) (resp *data.BookMetaData, err error) {
	// look in my own library first - if this fails badly, abort anything further
	if shelf, err := persistence.LookupBookshelf(ctx); err == nil {
		if book := shelf.LookupInfo(isbn); book != nil {
			return book, nil
		}
	} else {
		return nil, err
	}

	var errors ae.MultiError
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
