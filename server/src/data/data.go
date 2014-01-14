package data

type VolumeInfo struct {
	Title    string   `json:"title"`
	Subtitle string   `json:"subtitle,omitempty"`
	Authors  []string `json:"authors"`
}

type BookMetaData struct {
	Volume      VolumeInfo        `json:"volumeInfo"`
	Publisher   string            `json:"publisher,omitempty"`
	Description string            `json:"publisher,omitempty"`
	Images      map[string]string `json:"imageLink"`
	PageCount   int               `json:"pageCount,omitempty"`
}

type LookupReply struct {
	Count     int            `json:"totalItems"`
	BookInfos []BookMetaData `json:"items"`
}
