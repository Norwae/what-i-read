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

func init() {
	http.HandleFunc("/volumes/", serveLookup)
	http.HandleFunc("/volumes", serveImportExport)
}

func LookupISBN(ctx ae.Context, country string, isbn isbn13.ISBN13) (resp *data.BookMetaData, err error) {
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

			if err == nil {
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

func serveImportExport(w http.ResponseWriter, rq *http.Request) {
	ctx := ae.NewContext(rq)
	var err error
	var shelf *data.Bookshelf
	status := http.StatusBadRequest

	switch rq.Method {
	case "GET":
		shelf, err = persistence.LookupBookshelf(ctx)
	case "PUT":
		decode := json.NewDecoder(rq.Body)
		shelf = new(data.Bookshelf)
		if err = decode.Decode(shelf); err == nil {
			status = http.StatusInternalServerError
			err = persistence.StoreBookshelf(ctx, shelf)
		}
	}

	if err != nil {
		reportError(err)
		w.WriteHeader(status)
	} else {
		encode := json.NewEncoder(w)
		w.WriteHeader(http.StatusOK)
		w.Header().Add("Content-Type", "application/json")
		if err = encode.Encode(shelf); err != nil {
			ctx.Errorf("Could not report error on encode: %s\n", err.Error())
		}

	}
}

func serveLookup(w http.ResponseWriter, rq *http.Request) {
	isbn, err := isbn13.New(rq.URL.Path[9:])
	status := http.StatusBadRequest

	if err == nil {
		status = http.StatusInternalServerError
		switch rq.Method {
		case "GET":
			err = handleGet(w, rq, isbn)
		case "PUT":
			err = handlePut(w, rq, isbn)
		default:
			status = http.StatusMethodNotAllowed
			err = errors.New("Unsupported operation. Only GET, PUT and DELETE methods are allowed")
		}

	}

	if err != nil {
		reportError(err)
		w.WriteHeader(status)
	}
}

func reportError(e error) {
	if me, ok := e.(ae.MultiError); ok {
		for _, next := range me {
			reportError(next)
		}
	} else {
		ctx.Errorf("Error reported: %s", e.Error())
	}
}

func handlePut(w http.ResponseWriter, rq *http.Request, isbn isbn13.ISBN13) error {
	ctx := ae.NewContext(rq)
	shelf, err := persistence.LookupBookshelf(ctx)

	if err == ds.ErrNoSuchEntity {
		err = nil
		shelf = new(data.Bookshelf)
	}

	if err == nil {
		decode := json.NewDecoder(rq.Body)
		info := new(data.BookMetaData)
		if err = decode.Decode(info); err == nil {
			if ptr, _ := shelf.LookupInfo(isbn); ptr != nil {
				*ptr = *info
			} else {
				shelf.Books = append(shelf.Books, *info)
			}

			err = persistence.StoreBookshelf(ctx, shelf)
		}
	}
	return err

}

func handleGet(w http.ResponseWriter, rq *http.Request, isbn isbn13.ISBN13) error {
	var reply *data.BookMetaData
	var err error
	ctx := ae.NewContext(rq)
	country := determineCountry(rq)
	if reply, err = LookupISBN(ctx, country, isbn); err == nil {
		encode := json.NewEncoder(w)
		w.WriteHeader(http.StatusOK)
		w.Header().Add("Content-Type", "application/json")
		if err = encode.Encode(reply); err == nil {
			return nil
		}
	}

	return err
}

func determineCountry(rq *http.Request) string {
	header := rq.Header["X-AppEngine-Country"]
	if len(header) > 0 {
		return header[0]
	}
	query := rq.URL.Query()["country"]

	if len(query) > 0 {
		return query[0]
	}

	return "unknown"

}
