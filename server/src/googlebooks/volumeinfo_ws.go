package googlebooks

import (
	"fmt"
	"net/http"
)

func init() {
	http.HandleFunc("/lookup/", hw)
}

func hw(w http.ResponseWriter, rq *http.Request) {
	fmt.Fprintf(w, "Hello, %s!\n", rq.URL.Path)
}
