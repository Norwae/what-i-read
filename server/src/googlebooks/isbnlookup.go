package googlebooks

import (
	"appengine"
	"appengine/urlfetch"
	"appengine/user"
	"data"
	"encoding/json"
	"errors"
	"fmt"
	"isbn13"
	"net/http"
	"sync"
	"time"
)

const lookupURLTemplate = "https://www.googleapis.com/books/v1/volumes?q=isbn:%d&country=%s&key=%s&fields=items(volumeInfo(authors%%2Cdescription%%2CimageLinks(large%%2Cmedium%%2Csmall%%2CsmallThumbnail%%2Cthumbnail)%%2CpageCount%%2Cpublisher%%2Csubtitle%%2Ctitle))%%2CtotalItems"

var ErrorAPIRequestTimeout = errors.New("googlebooks-api: timeout waiting for request slot")
var dispatcherStart sync.Once

const placeHolderText = "Unknown ISBN: "

type googleBooksReply struct {
	response *data.BookMetaData
	err      error
}

type googleBooksRequest struct {
	ctx      appengine.Context
	country  string
	isbn     isbn13.ISBN13
	result   chan<- googleBooksReply
	deadline time.Time
}

var requests chan *googleBooksRequest = make(chan *googleBooksRequest)

func dispatch() {
	var queued []*googleBooksRequest
	lookupTimes := make(map[string]time.Time)
	ticker := time.Tick(1 * time.Second)

	for {
		select {
		case rq := <-requests:
			rq.ctx.Debugf("Received request")
			uid := user.Current(rq.ctx).ID
			now := time.Now()
			next, ok := lookupTimes[uid]
			if !ok || now.After(next) {
				rq.ctx.Debugf("Now processing")
				lookupTimes[uid] = now.Add(15 * time.Second)
				go func(request *googleBooksRequest) {
					resp, err := lookupISBN(request.ctx, request.country, request.isbn)

					request.result <- googleBooksReply{resp, err}
				}(rq)
			} else {
				rq.ctx.Debugf("Request queued for quota")
				queued = append(queued, rq)
			}
		case <-ticker:
			now := time.Now()
			var dead, retry, remaining []*googleBooksRequest
			for _, rq := range queued {
				uid := user.Current(rq.ctx).ID
				delay := lookupTimes[uid]
				var target *[]*googleBooksRequest
				switch {
				case now.After(rq.deadline):
					rq.ctx.Debugf("Deadline exceeded, killing API call")
					target = &dead
				case now.After(delay):
					rq.ctx.Debugf("Retrying call")
					target = &retry
				default:
					target = &remaining
				}

				*target = append(*target, rq)
			}

			queued = remaining

			for _, rq := range dead {
				go func(request *googleBooksRequest) {
					request.result <- googleBooksReply{nil, ErrorAPIRequestTimeout}
				}(rq)
			}

			for _, rq := range retry {
				go func(request *googleBooksRequest) {
					requests <- request
				}(rq)
			}
		}
	}
}

func LookupISBN(ctx appengine.Context, country string, isbn isbn13.ISBN13) (resp *data.BookMetaData, err error) {
	dispatcherStart.Do(func() {
		ctx.Debugf("starting request sync goroutine")
		go dispatch()
	})

	ctx.Debugf("Sending request to dispatcher: %s:%d", country, isbn)
	reply := make(chan googleBooksReply)
	requests <- &googleBooksRequest{ctx, country, isbn, reply, time.Now().Add(45 * time.Second)}

	ctx.Debugf("Waiting for API reply: %s:%d", country, isbn)
	replyObj := <-reply

	return replyObj.response, replyObj.err
}

func lookupISBN(ctx appengine.Context, country string, isbn isbn13.ISBN13) (resp *data.BookMetaData, err error) {
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
					ctx.Infof("Google books reported %d matching items: %v", reply.Count, reply)
					resp = new(data.BookMetaData)
					resp.Volume.Title = placeHolderText + isbn.String()
				}

				resp.ISBN = isbn.String()
			}
		}
	}

	return
}
