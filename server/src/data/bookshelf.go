package data

import (
	"errors"
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
		if c(book.Volume.Title) || c(book.Volume.Subtitle) || c(book.Volume.Series) || c(book.Volume.Authors[0]) || c(book.Description) || c(book.Comment) {
			r = append(r, book)
		}
	}

	return
}

func (shelf *Bookshelf) LookupInfo(isbn isbn13.ISBN13) (*BookMetaData, error) {
	for i := range shelf.Books {
		book := &shelf.Books[i]

		if book.ISBN == isbn.String() {
			return book, nil
		}
	}

	return nil, errors.New("No book found")
}
