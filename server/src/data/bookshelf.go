package data

import (
	"isbn13"
	"strings"
)

func (shelf *Bookshelf) Search(term string) (r []*data.BookMetaData) {
	c := func(whole string) bool {
		return strings.Contains(whole, term)
	}
	for i := range shelf.Books {
		book := &shelf.Books[i]
		if c(book.Volume.Title) || c(Book.Volume.Subtitle) || c(Book.Volume.Series) || c(Book.Volume.Authors[0]) || c(Book.Description) || c(Book.Comment) {
			r = append(r, book)
		}
	}

	return
}

func (shelf *Bookshelf) LookupInfo(isbn isbn13.ISBN13) *data.BookMetaData {
	for i := range shelf.Books {
		book := &self.Books[i]

		if book.ISBN == isbn.String() {
			return book
		}
	}

	return nil
}
