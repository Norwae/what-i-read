package persistence

import (
	ae "appengine"
	ds "appengine/datastore"
	mc "appengine/memcache"
	"appengine/user"
	"data"
)

const kindBookshelf = "bookshelf"

func UpdateBookshelf(ctx ae.Context, f func(*Transaction, *data.Bookshelf) error) error {
	return ds.RunInTransaction(ctx, updateBookshelf(f), nil)
}

func updateBookshelf(f func(*Transaction, *data.Bookshelf) error) func(ae.Context) error {
	return func(ctx ae.Context) error {
		ctx.Debugf("Beginning shelf update transaction")
		tx := Transaction{
			Context: ctx,
		}

		if shelf, err := LookupBookshelf(ctx); err == nil {
			if err = f(&tx, shelf); err == nil {
				err = commitTransaction(&tx)
				storeBookshelfMemcache(ctx, user.Current(ctx).ID, shelf)
			}

			return err
		} else {
			ctx.Errorf("Error reading bookshelf: %v", err)

			return err
		}
	}
}

func keys(ctx ae.Context, uid, kind string, elems []data.KeyStringer, ancestor *ds.Key) []*ds.Key {
	result := make([]*ds.Key, len(elems))

	for i := range elems {
		result[i] = ds.NewKey(ctx, kind, elems[i].KeyString(uid), 0, ancestor)
	}

	return result
}

func commitTransaction(tx *Transaction) error {
	var multi []error
	ctx := tx.Context
	uid := user.Current(ctx).ID
	ancestor := ds.NewKey(ctx, kindBookshelf, uid, 0, nil)

	if len(tx.Delete) > 0 {
		if e2 := ds.DeleteMulti(ctx, keys(ctx, uid, kindBookInfo, tx.Delete, ancestor)); e2 != nil {
			multi = append(multi, e2)
		}
	}

	if len(tx.Put) > 0 {
		if _, e2 := ds.PutMulti(ctx, keys(ctx, uid, kindBookInfo, tx.Put, ancestor), tx.Put); e2 != nil {
			multi = append(multi, e2)
		}
	}

	if multi != nil {
		return ae.MultiError(multi)
	}

	return nil
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
		Key:    uid,
		Object: shelf,
	}

	err := mc.Gob.Set(ctx, &item)
	ctx.Infof("Set memcache shelf instance with result %v", err)
}

func storeBookshelfDatastore(ctx ae.Context, uid string, shelf *data.Bookshelf) (err error) {
	ancestor := ds.NewKey(ctx, kindBookshelf, uid, 0, nil)

	var del, put []*ds.Key = nil, make([]*ds.Key, len(shelf.Books))

	for i := range shelf.Books {
		key := ds.NewKey(ctx, kindBookInfo, shelf.Books[i].KeyString(uid), 0, ancestor)
		put[i] = key
	}

	var key *ds.Key
	query := ds.NewQuery(kindBookInfo).Ancestor(ancestor).KeysOnly()
	it := query.Run(ctx)

	for key, err = it.Next(nil); err == nil; key, err = it.Next(nil) {
		usePut := false
		for _, ptr := range put {
			if key.Equal(ptr) {
				usePut = true
				break
			}
		}

		if !usePut {
			del = append(del, key)
		}
	}

	if err == ds.Done {
		me := make([]error, 0, 2)

		ctx.Infof("Updating bookshelf for %v, Requests: %d put, %d delete", uid, len(put), len(del))

		if _, e2 := ds.PutMulti(ctx, put, shelf.Books); e2 != nil {
			ctx.Errorf("Put failed: %v", e2)
			me = append(me, e2)
		}

		if e2 := ds.DeleteMulti(ctx, del); e2 != nil {
			ctx.Errorf("Delete failed: %v", e2)
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
		target = data.BookMetaData{
			Known: true,
		}
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

	shelf, err := lookupShelfMemcache(ctx, user)

	if err == nil {
		ctx.Debugf("Found shelf for %v in Memcache (%v)", user, shelf)
		return shelf, err
	} else {
		if shelf, err = lookupShelfDatastore(ctx, user); err == nil {
			storeBookshelfMemcache(ctx, user, shelf)
		}
	}

	return shelf, err
}
