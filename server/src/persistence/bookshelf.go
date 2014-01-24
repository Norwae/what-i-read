package persistence

import (
	ae "appengine"
	ds "appengine/datastore"
	"appengine/user"
	"data"
)

const kindBookshelf = "bookshelf"

func StoreBookshelf(ctx ae.Context, shelf *data.Bookshelf) error {
	user := user.Current(ctx)
	key := ds.NewKey(ctx, kindBookshelf, user.ID, 0, nil)
	_, err := ds.Put(ctx, key, shelf)

	ctx.Infof("Put ", shelf, " with result ", err)
	return err
}

func LookupBookshelf(ctx ae.Context) (*data.Bookshelf, error) {
	user := user.Current(ctx)
	key := ds.NewKey(ctx, kindBookshelf, user.ID, 0, nil)

	found := new(data.Bookshelf)

	err := ds.Get(ctx, key, found)
	if err == nil || err == ds.ErrNoSuchEntity {
		return found, nil
	}
	
	return nil, err
}
