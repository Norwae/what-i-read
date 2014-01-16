package data

type VolumeInfo struct {
	Title    string   `json:"title"`
	Subtitle string   `json:"subtitle,omitempty"`
	Series   string   `json:"series,omitempty"`
	Authors  []string `json:"authors"`
}

type BookMetaData struct {
	Volume      VolumeInfo `json:"volumeInfo" datastore:",noindex"`
	Publisher   string     `json:"publisher,omitempty" datastore:",noindex"`
	Description string     `json:"publisher,omitempty" datastore:",noindex"`
	Images      ImageLinks `json:"imageLink,omitempty" datastore:",noindex"`
	PageCount   int        `json:"pageCount,omitempty" datastore:",noindex"`
	ISBN        string
}

type ImageLinks struct {
	ThumbnailSmall string `json:"smallThumbnail,omitempty" datastore:",noindex"`
	Thumbnail      string `json:"thumbnail,omitempty" datastore:",noindex"`
}

type LookupReply struct {
	Count     int            `json:"totalItems"`
	BookInfos []BookMetaData `json:"items"`
}

type Bookshelf struct {
	Books []BookMetaData
}
