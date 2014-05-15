package webservice

import (
	"net/http"
	"html/template"
	"appengine"
)

var searchPage = template.Must(template.ParseFiles("tmpl/main.htmlt", "tmpl/basics.htmlt"))
// var bookPage = template.Must(template.ParseFiles("tmpl/book.htmlt"))
var errorPage = template.Must(template.ParseFiles("tmpl/error.htmlt", "tmpl/basics.htmlt"))

func init() {
	http.Handle("/", http.RedirectHandler("/index.html", http.StatusMovedPermanently))
	http.Handle("/index.html", http.HandlerFunc(serveStartPage))
	// http.Handle("/book/", http.HandlerFunc(serveBookPage))
}

func serveStartPage(rsp http.ResponseWriter, rq *http.Request) {
	call := Call {
		Context:appengine.NewContext(rq),
		Request: rq,
		Response: rsp, 
	}
	
	if bookInfos, err := getVolumeBulk(&call); err == nil {
		call.Context.Infof("Emitting searchPage template")
		searchPage.Execute(rsp, bookInfos)
	} else {
		call.Context.Errorf("Emitting error template (%s)", err)
		errorPage.Execute(rsp, err)
	}
}