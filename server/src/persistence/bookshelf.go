package persistence

import (
	ae "appengine"
	ds "appengine/datastore"
	mc "appengine/memcache"
	"appengine/user"
	"data"
	"time"
)

const kindBookshelf = "bookshelf"

func UpdateBookshelf(ctx ae.Context, f func(ae.Context, *data.Bookshelf) error) error {
	return ds.RunInTransaction(ctx, updateBookshelf(f), nil)
}

func updateBookshelf(f func(ae.Context, *data.Bookshelf) error) func(ae.Context) error {
	return func(ctx ae.Context) error {
		uid := user.Current(ctx).ID
		shelf, err := lookupShelfDatastore(ctx, uid)

		if err != nil {
			if err = f(ctx, shelf); err == nil {
				err = StoreBookshelf(ctx, shelf)
			}
		}

		return err
	}
}

func StoreBookshelf(ctx ae.Context, shelf *data.Bookshelf) error {
	user := user.Current(ctx).ID
	err := storeBookshelfDatastore(ctx, user, shelf)

	if err == nil {
		storeBookshelfMemcache(ctx, user, shelf)
	}

	return err
}

func storeBookshelfMemcache(ctx ae.Context, uid string, shelf *data.Bookshelf) {
	item := mc.Item{
		Key:        uid,
		Object:     shelf,
		Expiration: 15 * time.Minute,
	}

	mc.Gob.Add(ctx, &item)
}

func storeBookshelfDatastore(ctx ae.Context, uid string, shelf *data.Bookshelf) (err error) {
	ancestor := ds.NewKey(ctx, kindBookshelf, uid, 0, nil)
	query := ds.NewQuery(kindBookInfo).Ancestor(ancestor)

	currentElements := make(map[string]*data.BookMetaData)

	for i := range shelf.Books {
		next := &shelf.Books[i]
		currentElements[next.ISBN] = next
	}

	ctx.Debugf("Build isbn -> value map: %v", currentElements)

	var target data.BookMetaData
	var key *ds.Key

	var delKeys, putKeys []*ds.Key
	var putVals []*data.BookMetaData

	it := query.Run(ctx)
	_, err = it.Next(&target)
	for ; err == nil; _, err = it.Next(&target) {
		if updated, ok := currentElements[target.ISBN]; ok {
			putKeys = append(putKeys, key)
			putVals = append(putVals, updated)
			delete(currentElements, target.ISBN)

			ctx.Debugf("Updating element %v", target.ISBN)
		} else {
			delKeys = append(delKeys, key)

			ctx.Debugf("Deleting element %v", target.ISBN)
		}
	}

	for _, val := range currentElements {
		putKeys = append(putKeys, ds.NewIncompleteKey(ctx, kindBookshelf, ancestor))
		putVals = append(putVals, val)

		ctx.Debugf("Putting new value %v", val)
	}

	if err == ds.Done {
		me := make([]error, 0, 2)

		ctx.Infof("Updating bookshelf for %v, Requests: %d put, %d delete", uid, len(putKeys), len(delKeys))

		if _, e2 := ds.PutMulti(ctx, putKeys, putVals); e2 != nil {
			me = append(me, e2)
		}

		if e2 := ds.DeleteMulti(ctx, delKeys); e2 != nil {
			me = append(me, e2)
		}

		if len(me) > 0 {
			err = ae.MultiError(me)
		} else {
			err = nil
		}
	}

	return
}

func lookupShelfDatastore(ctx ae.Context, user string) (shelf *data.Bookshelf, err error) {
	ancestor := ds.NewKey(ctx, kindBookshelf, user, 0, nil)
	query := ds.NewQuery(kindBookInfo).Ancestor(ancestor)

	target := data.BookMetaData{
		Known: true,
	}

	shelf = new(data.Bookshelf)

	it := query.Run(ctx)
	_, err = it.Next(&target)
	for ; err == nil; _, err = it.Next(&target) {
		ctx.Infof("Found item %v", &target)
		shelf.Books = append(shelf.Books, target)
	}

	ctx.Infof("Found %d items in datastore for key %v (%v)", len(shelf.Books), ancestor, err)

	if err == ds.Done {
		err = nil
	} else {
		shelf = nil
	}

	return
}

func lookupShelfMemcache(ctx ae.Context, key string) (resp *data.Bookshelf, err error) {
	found := new(data.Bookshelf)

	if _, err = mc.Gob.Get(ctx, key, found); err == nil {
		resp = found
	}

	return
}

func LookupBookshelf(ctx ae.Context) (*data.Bookshelf, error) {
	user := user.Current(ctx).ID

	if shelf, err := lookupShelfMemcache(ctx, user); err == nil {
		ctx.Debugf("Found shelf for %v in Memcache", user)
		return shelf, err
	}

	return lookupShelfDatastore(ctx, user)
}
