package webservice

import (
	"appengine"
	"html/template"
	"net/http"
)

var searchPage, errorPage *template.Template

func init() {
	http.Handle("/", http.RedirectHandler("/index.html", http.StatusMovedPermanently))
	http.Handle("/index.html", http.HandlerFunc(serveStartPage))
	// http.Handle("/book/", http.HandlerFunc(serveBookPage))

	searchPage = template.Must(template.ParseFiles("tmpl/main.html", "tmpl/headers.html"))
}

func serveStartPage(rsp http.ResponseWriter, rq *http.Request) {
	call := Call{
		Context:  appengine.NewContext(rq),
		Request:  rq,
		Response: rsp,
	}

	if info, err := getVolumeBulk(&call); err == nil {
		err := searchPage.Execute(rsp, info)
		call.Context.Infof("Emitting searchPage template: %v", err)
	} else {
		call.Context.Errorf("Emitting error template (%s)", err)
		rsp.WriteHeader(http.StatusInternalServerError)
		errorPage.Execute(rsp, err)
	}
}
