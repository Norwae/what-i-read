package data

import (
	"isbn13"
	"strings"
)

func (shelf *Bookshelf) Search(term string) (r []*BookMetaData) {
	r = make([]*BookMetaData, 0, len(shelf.Books))
	c := func(whole string) bool {
		return strings.Contains(whole, term)
	}
	for i := range shelf.Books {
		book := &shelf.Books[i]
		volume := &book.Volume
		if c(volume.Title) || c(volume.Subtitle) || c(volume.Series) || c(volume.Authors[0]) || c(volume.Description) || c(book.Comment) {
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
