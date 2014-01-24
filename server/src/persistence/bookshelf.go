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

func StoreBookshelf(ctx ae.Context, shelf *data.Bookshelf) error {
	user := user.Current(ctx)
	err := storeBookshelfDatastore(ctx, user, shelf)

	if err == nil {
		storeBookshelfMemcache(ctx, user, shelf)
	}

	return err
}

func storeBookshelfMemcache(ctx ae.Context, uid string, shelf *data.Bookshelf) {
	item := mc.Item{
		Key:        uid,
		Object:     data,
		Expiration: 15 * time.Minute,
	}

	mc.Gob.Add(ctx, &item)
}

func storeBookshelfDatastore(ctx ae.Context, uid string, shelf *data.Bookshelf) error {
	key := ds.NewKey(ctx, kindBookshelf, uid, 0, nil)
	_, err := ds.Put(ctx, key, shelf)

	ctx.Infof("Put ", shelf, " with result ", err)

	return err
}

func lookupShelfDatastore(ctx ae.Context, user string, ptr *data.Bookshelf) error {
	key := ds.NewKey(ctx, kindBookshelf, user, 0, nil)

	found := new(data.Bookshelf)

	err := ds.Get(ctx, key, found)
	// if not found, the newly-allocated instance will serve fine
	if err == nil || err == ds.ErrNoSuchEntity {
		storeBookshelfMemcache(user, found)
		return found, nil
	}

	return nil, err
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
		return shelf, err
	}

	return lookupShelfDatastore(ctx, user)
}
