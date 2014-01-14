package googlebooks

import (
//	ae "appengine"
	"fmt"
	"isbn13"
	"net/http"
)

func init() {
	http.HandleFunc("/lookup/", hw)
}

func hw(w http.ResponseWriter, rq *http.Request) {
	// ctx := ae.NewContext(rq)

	if isbn, err := isbn13.New(rq.URL.Path[8:]); err == nil {
		fmt.Fprintf(w, "Yup, %s!\n", isbn)
		return
	}

	w.WriteHeader(500)
	fmt.Fprintf(w, "Ooops, %s!\n", rq.URL.Path)

}
