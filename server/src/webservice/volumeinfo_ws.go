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
	var shelf *data.LookupReply
	var err error

	switch call.Request.Method {
	case "GET":
		shelf, err = getVolumeBulk(call)
	case "PUT":
		shelf, err = putVolumeBulk(call)
	default:
		err = errors.New("Unsupported operation. Only GET and PUT methods are allowed")
	}

	return shelf, err
}

func getVolumeBulk(call *Call) (reply *data.LookupReply, err error) {
	var shelf *data.Bookshelf

	if shelf, err = persistence.LookupBookshelf(call.Context); err == nil {
		for _, str := range call.Request.URL.Query()["search"] {
			matches := shelf.Search(str)

			shelf := &data.Bookshelf{make([]data.BookMetaData, len(matches))}
			for i, ptr := range matches {
				shelf.Books[i] = *ptr
			}
		}

		reply = &data.LookupReply{
			Count:     len(shelf.Books),
			BookInfos: shelf.Books,
		}
	}

	return
}

func putVolumeBulk(call *Call) (reply *data.LookupReply, err error) {
	decode := json.NewDecoder(call.Request.Body)
	shelf := new(data.Bookshelf)
	if err = decode.Decode(shelf); err == nil {
		for i := range shelf.Books {
			book := &shelf.Books[i]
			normalize(call, book)
		}

		if err = persistence.StoreBookshelf(call.Context, shelf); err == nil {
			reply = &data.LookupReply{
				Count:     len(shelf.Books),
				BookInfos: shelf.Books,
			}
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
			err = errors.New("Unsupported operation. Only GET, PUT and DELETE methods are allowed")
		}
	}

	return book, err
}

func deleteVolumeSingle(call *Call, isbn isbn13.ISBN13) (info *data.BookMetaData, err error) {
	call.Context.Infof("Deleting ISBN %d", isbn)
	err = persistence.UpdateBookshelf(call.Context, func(tx *persistence.Transaction, shelf *data.Bookshelf) error {
		for i := range shelf.Books {
			if ptr := &shelf.Books[i]; ptr.ISBN == isbn.String() {
				tx.Delete = []data.KeyStringer{ptr}
				return nil
			}

		}
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

		persistence.UpdateBookshelf(call.Context, func(tx *persistence.Transaction, shelf *data.Bookshelf) error {

			if ptr := shelf.LookupInfo(isbn); ptr != nil {
				*ptr = *info
				tx.Put = []data.KeyStringer{ptr}
			} else {
				shelf.Books = append(shelf.Books, *info)
				tx.Put = []data.KeyStringer{info}
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
