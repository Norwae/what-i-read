package cache

import (
	ae "appengine"
	mc "appengine/memcache"
	"data"
	"isbn13"
	"strings"
)

func LookupISBN(ctx ae.Context, country string, isbn isbn13.ISBN13) (resp *data.BookMetaData, err error) {
	key := key(country, isbn)
	found := new(data.BookMetaData)

	if _, err = mc.Gob.Get(ctx, key, found); err == nil {
		resp = found
	}

	return
}

func CacheISBNResult(ctx ae.Context, country string, isbn isbn13.ISBN13, data *data.BookMetaData) {
	key := key(country, isbn)
	item := mc.Item{
		Key:    key,
		Object: data,
	}

	mc.Gob.Add(ctx, &item)
}

func key(country string, isbn isbn13.ISBN13) string {
	return strings.Join([]string{country, isbn.String()}, ":")
}
