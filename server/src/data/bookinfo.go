package data

import (
	ae "appengine"
	ds "appengine/datastore"
	"isbn13"
)

const KindBookInfo = "book"

func BookInfoPrototypeKey(ctx ae.Context, isbn isbn13.ISBN13, country string) string {
	return country + ":" + isbn.String()
}

func (d *BookMetaData) DeriveKey(ctx ae.Context) *ds.Key {
	var parent *ds.Key
	var id string

	switch d.Parent.(type) {
	case string:
		id = d.Parent.(string)
	case Bookshelf:
		id = d.Parent.(Bookshelf).Owner
		parent = ds.NewKey(ctx, KindBookshelf, id, 0, nil)
	default:
		panic("unmatched case")
	}

	return ds.NewKey(ctx, KindBookInfo, id+":"+d.ISBN, 0, parent)
}
