package persistence

import (
	ae "appengine"
	ds "appengine/datastore"
	mc "appengine/memcache"
	"data"
	"isbn13"
	"strings"
	"sync"
)

const kindBookInfo = "book"

func LookupISBN(ctx ae.Context, country string, isbn isbn13.ISBN13) (*data.BookMetaData, error) {
	keyString := key(country, isbn)
	if result, err := lookupISBNMemcache(ctx, keyString); err == nil {
		return result, err
	} else {
		return lookupISBNDatastore(ctx, keyString)
	}
}

func lookupISBNDatastore(ctx ae.Context, keyStr string) (resp *data.BookMetaData, err error) {
	key := ds.NewKey(ctx, kindBookInfo, keyStr, 0, nil)
	found := new(data.BookMetaData)

	if err = ds.Get(ctx, key, found); err == nil {
		resp = found
		cacheISBNResult(ctx, keyStr, resp)
	}

	return
}

func lookupISBNMemcache(ctx ae.Context, key string) (resp *data.BookMetaData, err error) {
	found := new(data.BookMetaData)

	if _, err = mc.Gob.Get(ctx, key, found); err == nil {
		resp = found
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

func putISBNResult(ctx ae.Context, keyStr string, data *data.BookMetaData) {
	key := ds.NewKey(ctx, kindBookInfo, keyStr, 0, nil)
	_, err := ds.Put(ctx, key, data)

	ctx.Infof("Put %s with result %v", keyStr, err)
}

func StoreISBNResult(ctx ae.Context, country string, isbn isbn13.ISBN13, book *data.BookMetaData) {
	keyStr := key(country, isbn)

	var group sync.WaitGroup
	group.Add(2)

	wrap := func(f func(ae.Context, string, *data.BookMetaData)) {
		defer group.Done()
		f(ctx, keyStr, book)
	}

	go wrap(cacheISBNResult)
	go wrap(putISBNResult)

	group.Wait()
}

func key(country string, isbn isbn13.ISBN13) string {
	return strings.Join([]string{country, isbn.String()}, ":")
}
