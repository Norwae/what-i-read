package persistence

import (
	ae "appengine"
	ds "appengine/datastore"
	mc "appengine/memcache"
	"persistence/tx"
	"appengine/user"
	"data"
	"time"
)

const Latest = 1

func UpdateBookshelf(ctx ae.Context, f func(*tx.Transaction, *data.Bookshelf) error) error {
	return ds.RunInTransaction(ctx, updateBookshelf(f), nil)
}

func updateBookshelf(f func(*tx.Transaction, *data.Bookshelf) error) func(ae.Context) error {
	return func(ctx ae.Context) error {
		ctx.Debugf("Beginning shelf update transaction")
		tx := tx.Transaction{
			Context: ctx,
		}

		if shelf, err := LookupBookshelf(ctx); err == nil {
			if err = f(&tx, shelf); err == nil {
				shelf.LastUpdate = time.Now()
				shelf.Version = Latest
				tx.Commit()

				storeShelfMemcache(ctx, user.Current(ctx).ID, shelf)
			}

			return err
		} else {
			ctx.Errorf("Error reading bookshelf: %v", err)

			return err
		}
	}
}

func storeShelfMemcache(ctx ae.Context, uid string, shelf *data.Bookshelf) {
	item := mc.Item{
		Key:    uid,
		Object: shelf,
	}

	err := mc.Gob.Set(ctx, &item)
	ctx.Infof("Set memcache shelf instance with result %v", err)
}

func lookupShelfDatastore(ctx ae.Context, user string) (shelf *data.Bookshelf, err error) {
	ancestor := ds.NewKey(ctx, data.KindBookshelf, user, 0, nil)
	query := ds.NewQuery(data.KindBookInfo).Ancestor(ancestor)

	targetMeta := new(data.BookMetaData)
	targetShelf := new(data.Bookshelf)

	switch err = ds.Get(ctx, ancestor, shelf); err {
	case nil:
		// all good
	case ds.ErrNoSuchEntity:
		targetShelf.Version = Latest
	default:
		return
	}

	it := query.Run(ctx)

	for _, err = it.Next(targetMeta); err == nil; _, err = it.Next(targetMeta) {
		targetShelf.Books = append(targetShelf.Books, *targetMeta)
		targetMeta = new(data.BookMetaData)
	}

	if err == ds.Done {
		err = nil
		shelf = targetShelf
	}

	ctx.Infof("Found %d items in datastore for key %v (error: %v)", len(targetShelf.Books), ancestor, err)

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

	shelf, err := lookupShelfMemcache(ctx, user)

	if err == nil {
		ctx.Debugf("Found shelf for %v in Memcache (%v)", user, shelf)
	} else {
		if shelf, err = lookupShelfDatastore(ctx, user); err == nil {
			storeShelfMemcache(ctx, user, shelf)
		}
	}

	if shelf != nil {
		for i := range shelf.Books {
			shelf.Books[i].Known = true
			shelf.Books[i].Parent = shelf
		}
	}

	return shelf, err
}
