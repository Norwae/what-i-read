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

const lookupURLTemplate = "https://www.googleapis.com/books/v1/volumes?q=isbn:%d&country=%s&key=%s&fields=items(volumeInfo(authors%%2Cdescription%%2CimageLinks(large%%2Cmedium%%2Csmall%%2CsmallThumbnail%%2Cthumbnail)%%2CpageCount%%2Cpublisher%%2Csubtitle%%2Ctitle))%%2CtotalItems"

const placeHolderText = "Unknown ISBN: "

func LookupISBN(ctx appengine.Context, country string, isbn isbn13.ISBN13) (resp *data.BookMetaData, err error) {
	var r *http.Response
	url := fmt.Sprintf(lookupURLTemplate, uint64(isbn), country, apiKey)
	ctx.Debugf("Calling %s", fmt.Sprintf(lookupURLTemplate, uint64(isbn), country, "<hidden>"))

	client := urlfetch.Client(ctx)
	if r, err = client.Get(url); err == nil {
		if r.StatusCode != http.StatusOK {
			err = fmt.Errorf("Google API returned %s", r.Status)
		} else {
			reply := new(data.LookupReply)
			decode := json.NewDecoder(r.Body)
			defer r.Body.Close()

			ctx.Infof("Completed API call, result %s\n", r.Status)

			if err = decode.Decode(reply); err == nil {

				if reply.Count == 1 {
					resp = &reply.BookInfos[0]
				} else {
					resp = new(data.BookMetaData)
					resp.Volume.Title = placeHolderText + isbn.String()
				}

				resp.ISBN = isbn.String()
			}
		}
	}

	return
}
