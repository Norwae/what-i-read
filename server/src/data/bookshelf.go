package data

import (
	ae "appengine"
	ds "appengine/datastore"
	"appengine/user"
	"isbn13"
	"strings"
)

const KindBookshelf = "bookshelf"

func (shelf *Bookshelf) Search(term string) (r []*BookMetaData) {
	r = make([]*BookMetaData, 0, len(shelf.Books))
	term = strings.ToLower(term)

	c := func(whole string) bool {
		return strings.Contains(strings.ToLower(whole), term)
	}

	any := func(whole []string) bool {
		for _, val := range whole {
			if c(val) {
				return true
			}
		}

		return false
	}

	for i := range shelf.Books {
		book := &shelf.Books[i]
		volume := &book.Volume
		if c(volume.Title) || c(volume.Subtitle) || c(volume.Series) || any(volume.Authors) || c(volume.Description) || c(book.Comment) {
			r = append(r, book)
		}
	}

	return
}

func (shelf *Bookshelf) LookupInfo(isbn isbn13.ISBN13) *BookMetaData {
	for i := range shelf.Books {
		book := &shelf.Books[i]

		if book.ISBN == isbn.String() {
			return book
		}
	}

	return nil
}

func (shelf *Bookshelf) DeriveKey(ctx ae.Context) *ds.Key {
	return ds.NewKey(ctx, KindBookshelf, user.Current(ctx).ID, 0, nil)
}
