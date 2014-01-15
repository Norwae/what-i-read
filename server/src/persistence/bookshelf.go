package persistence

import (
	ae "appengine"
	ds "appengine/datastore"
	"appengine/user"
	"data"
)

const kindBookshelf = "bookshelf"

func LookupBookshelf(ctx ae.Context) (*Bookshelf, error) {
	user := user.Current(ctx)
	key := ds.NewKey(ctx, kindBookshelf, user.ID, 0, nil)

	found := new(data.Bookshelf)
	var err error
	if _, err = ds.Get(ctx, key, found); err == nil {
		return found, nil
	}

	return nil, err
}
