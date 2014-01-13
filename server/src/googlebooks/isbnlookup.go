package googlebooks

import (
	"appengine"
	"net/http"
	"encoding/json"
	"fmt"
	"isbn13"
	"appengine/urlfetch"
)

type VolumeInfo struct {
	Title    string   `json:"title"`
	Subtitle string   `json:"subtitle"`
	Authors  []string `json:"authors"`
}

type BookMetaData struct {
	Volume      VolumeInfo        `json:"volumeInfo"`
	Publisher   string            `json:"publisher"`
	Description string            `json:"publisher"`
	Images      map[string]string `json:"imageLink"`
	PageCount   int               `json:"pageCount"`
}

type LookupReply struct {
	BookInfos []BookMetaData `json:"items"`
}

const lookupURLTemplate = "https://www.googleapis.com/books/v1/volumes?q=isbn:%d&country=%s&projection=lite&key=%s"

func LookupISBN(ctx appengine.Context, country string, isbn isbn13.ISBN13) (resp *LookupReply, err error) {
	var r *http.Response
	url := fmt.Sprintf(lookupURLTemplate, uint64(isbn), country, apiKey)

	client := urlfetch.Client(ctx)
	if r, err = client.Get(url); err == nil {
		reply := new(LookupReply)
		decode := json.NewDecoder(r.Body)
		defer r.Body.Close()

		if err = decode.Decode(reply); err == nil {
			resp = reply
		}
	}

	return
}
