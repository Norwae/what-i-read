package tx

import (
	ae "appengine"
	ds "appengine/datastore"
)

type KeyDeriver interface {
	DeriveKey(ae.Context) *ds.Key
}

type Transaction struct {
	Context ae.Context
	Put     []KeyDeriver
	Delete  []KeyDeriver
}

func (tx *Transaction) Commit() error {
	var multi []error

	if len(tx.Delete) > 0 {
		if err := ds.DeleteMulti(tx.Context, keys(tx.Context, tx.Delete)); err != nil {
			multi = append(multi, err)
		}
	}

	if len(tx.Put) > 0 {
		if _, err := ds.PutMulti(tx.Context, keys(tx.Context, tx.Put), tx.Put); err != nil {
			multi = append(multi, err)
		}
	}

	if multi != nil {
		return ae.MultiError(multi)
	}
	
	return nil
}

func keys(ctx ae.Context, elems []KeyDeriver) []*ds.Key {
	result := make([]*ds.Key, len(elems))

	for i := range elems {
		result[i] = elems[i].DeriveKey(ctx)
	}

	return result
}
