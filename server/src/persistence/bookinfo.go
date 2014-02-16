package persistence

import (
	ae "appengine"
	ds "appengine/datastore"
	mc "appengine/memcache"
	"data"
	"isbn13"
)

func LookupISBN(ctx ae.Context, country string, isbn isbn13.ISBN13) (resp *data.BookMetaData, err error) {
	keyString := data.BookInfoPrototypeKey(ctx, isbn, country)
	target := new(data.BookMetaData)

	if _, err = mc.Gob.Get(ctx, keyString, target); err == nil {
		target.Parent = country
		resp = target
	} else {
		key := ds.NewKey(ctx, data.KindBookInfo, keyString, 0, nil)

		if err = ds.Get(ctx, key, target); err == nil {
			resp = target
			resp.Parent = country
			cacheISBNResult(ctx, keyString, resp)
		}
	}

	return
}

func cacheISBNResult(ctx ae.Context, key string, data *data.BookMetaData) {
	item := mc.Item{
		Key:    key,
		Object: data,
	}

	mc.Gob.Add(ctx, &item)
}

func StoreISBNResult(ctx ae.Context, country string, isbn isbn13.ISBN13, book *data.BookMetaData) {
	cacheISBNResult(ctx, data.BookInfoPrototypeKey(ctx, isbn, country), book)
	ds.Put(ctx, book.DeriveKey(ctx), book)
}
