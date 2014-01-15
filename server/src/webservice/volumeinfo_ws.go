package webservice

import (
	ae "appengine"
	"cache"
	"data"
	"encoding/json"
	"fmt"
	"googlebooks"
	"isbn13"
	"net/http"
	"log"
)

func init() {
	http.HandleFunc("/lookup/", hw)
}

func LookupISBN(ctx ae.Context, country string, isbn isbn13.ISBN13) (resp *data.BookMetaData, err error) {
	funcs := []func(ae.Context, string, isbn13.ISBN13)(*data.BookMetaData, error) {
		cache.LookupISBN,
		googlebooks.LookupISBN,
	}
	
	var multi ae.MultiError
	
	for i, f := range funcs {
		if result, err := f(ctx, country, isbn); err == nil {
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
