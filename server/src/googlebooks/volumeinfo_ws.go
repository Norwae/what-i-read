package googlebooks

import (
	ae "appengine"
	"encoding/json"
	"fmt"
	"isbn13"
	"net/http"
)

func init() {
	http.HandleFunc("/lookup/", hw)
}

func hw(w http.ResponseWriter, rq *http.Request) {
	isbn, err := isbn13.New(rq.URL.Path[8:])

	if err == nil {
		var reply *LookupReply
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
