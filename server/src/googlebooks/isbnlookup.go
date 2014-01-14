package googlebooks

import (
	"appengine"
	"appengine/urlfetch"
	"encoding/json"
	"fmt"
	"isbn13"
	"net/http"
)

type VolumeInfo struct {
	Title    string   `json:"title"`
	Subtitle string   `json:"subtitle,omitempty"`
	Authors  []string `json:"authors"`
}

type BookMetaData struct {
	Volume      VolumeInfo        `json:"volumeInfo"`
	Publisher   string            `json:"publisher,omitempty"`
	Description string            `json:"publisher,omitempty"`
	Images      map[string]string `json:"imageLink"`
	PageCount   int               `json:"pageCount,omitempty"`
}

type LookupReply struct {
	Count     int            `json:"totalItems"`
	BookInfos []BookMetaData `json:"items"`
}

const lookupURLTemplate = "https://www.googleapis.com/books/v1/volumes?q=isbn:%d&country=%s&key=%s"

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
