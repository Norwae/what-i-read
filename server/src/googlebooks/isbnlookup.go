package googlebooks

import (
	"appengine"
	"appengine/urlfetch"
	"data"
	"encoding/json"
	"fmt"
	"isbn13"
	"net/http"
)

const lookupURLTemplate = "https://www.googleapis.com/books/v1/volumes?q=isbn:%d&country=%s&key=%s"

func LookupISBN(ctx appengine.Context, country string, isbn isbn13.ISBN13) (resp *data.BookMetaData, err error) {
	var r *http.Response
	url := fmt.Sprintf(lookupURLTemplate, uint64(isbn), country, apiKey)

	client := urlfetch.Client(ctx)
	if r, err = client.Get(url); err == nil {
		reply := new(data.LookupReply)
		decode := json.NewDecoder(r.Body)
		defer r.Body.Close()

		if err = decode.Decode(reply); err == nil && reply.Count == 1 {
			resp = &reply.BookInfos[0]
		}
	}

	return
}
