package data

import "strings"

func (d *BookMetaData) KeyString(context string) string {
	return strings.Join([]string{context, d.ISBN}, ":")
}
