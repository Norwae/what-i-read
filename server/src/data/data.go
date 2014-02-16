package data

import "time"

type VolumeInfo struct {
	Title       string     `json:"title"`
	Subtitle    string     `json:"subtitle,omitempty"`
	Series      string     `json:"series,omitempty"`
	Description string     `json:"description,omitempty" datastore:",noindex"`
	Authors     []string   `json:"authors"`
	Publisher   string     `json:"publisher,omitempty" datastore:",noindex"`
	Images      ImageLinks `json:"imageLinks,omitempty" datastore:",noindex"`
	PageCount   int        `json:"pageCount,omitempty" datastore:",noindex"`
}

type BookMetaData struct {
	Volume  VolumeInfo  `json:"volumeInfo" datastore:",noindex"`
	Comment string      `json:"comment,omitempty" datastore:",noindex"`
	ISBN    string      `json:"isbn" datastore:",noindex"`
	Known   bool        `json:"known" datastore:"-"`
	Parent  interface{} `json:"-" datastore:"-"`
}

type ImageLinks struct {
	ThumbnailSmall string `json:"smallThumbnail,omitempty" datastore:",noindex"`
	Thumbnail      string `json:"thumbnail,omitempty" datastore:",noindex"`
	Small          string `json:"small,omitempty" datastore:",noindex"`
	Medium         string `json:"medium,omitempty" datastore:",noindex"`
	Large          string `json:"large,omitempty" datastore:",noindex"`
}

type LookupReply struct {
	Count      int            `json:"totalItems"`
	LastUpdate time.Time      `json:"timestamp"`
	BookInfos  []BookMetaData `json:"items"`
}

type Bookshelf struct {
	Owner      string         `datastore:"-"`
	LastUpdate time.Time      `datastore:",noindex"`
	Version    int            `datastore:",noindex"`
	Books      []BookMetaData `datastore:-`
}
