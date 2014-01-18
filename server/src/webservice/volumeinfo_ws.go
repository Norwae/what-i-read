package webservice

import (
	ae "appengine"
	"cache"
	"data"
	"encoding/json"
	"fmt"
	"googlebooks"
	"isbn13"
	"log"
	"net/http"
	"persistence"
)

func init() {
	http.HandleFunc("/lookup/", hw)
}

func LookupISBN(ctx ae.Context, country string, isbn isbn13.ISBN13) (resp *data.BookMetaData, err error) {
	funcs := []func(ae.Context, string, isbn13.ISBN13) (*data.BookMetaData, error){
		func(ctx ae.Context, country string, isbn isbn13.ISBN13) (*data.BookMetaData, error) {
			shelf, err := persistence.LookupBookshelf(ctx)
			if err == nil {
				return shelf.LookupInfo(isbn), nil
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
			log.Printf("Found info %v after %d iterations\n", result, i)
			return result, nil
		} else {
			multi = append(multi, err)
		}
	}

	return nil, multi
}

func hw(w http.ResponseWriter, rq *http.Request) {
	isbn, err := isbn13.New(rq.URL.Path[8:])

	if err == nil {
		var reply *data.BookMetaData
		ctx := ae.NewContext(rq)
		if reply, err = LookupISBN(ctx, "de", isbn); err == nil {
			encode := json.NewEncoder(w)

			if err = encode.Encode(reply); err == nil {
				return
			}
		}
	}

	w.WriteHeader(500)
	fmt.Fprintf(w, "Ooops, %s (%v)!\n", rq.URL.Path, err)
}
