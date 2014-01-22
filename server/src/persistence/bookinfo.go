package persistence

import (
	ae "appengine"
	ds "appengine/datastore"
	"data"
	"isbn13"
	"strings"
)

const kindBookInfo = "book"

func LookupISBN(ctx ae.Context, country string, isbn isbn13.ISBN13) (resp *data.BookMetaData, err error) {
	key := ds.NewKey(ctx, kindBookInfo, key(country, isbn), 0, nil)
	found := new(data.BookMetaData)
	if err = ds.Get(ctx, key, found); err == nil {
		resp = found
	}

	return
}

func StoreISBNResult(ctx ae.Context, country string, isbn isbn13.ISBN13, data *data.BookMetaData) {
	key := ds.NewKey(ctx, kindBookInfo, key(country, isbn), 0, nil)
	_, err := ds.Put(ctx, key, data)

	ctx.Infof("Put ", isbn, " with result ", err)
}

func key(country string, isbn isbn13.ISBN13) string {
	return strings.Join([]string{country, isbn.String()}, ":")
}
