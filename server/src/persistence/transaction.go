package persistence

import (
	ae "appengine"
	"data"
)

type Transaction struct {
	Context ae.Context
	Put     []data.KeyStringer
	Delete  []data.KeyStringer
}
