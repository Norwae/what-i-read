package webservice

import (
	"appengine"
	"html/template"
	"net/http"
)

var searchPage, errorPage, bookPage *template.Template

func init() {
	http.Handle("/", http.RedirectHandler("/index.html", http.StatusMovedPermanently))
	http.Handle("/index.html", http.HandlerFunc(serveStartPage))
	http.Handle("/book/", http.HandlerFunc(serveBookPage))

	searchPage = template.Must(template.ParseFiles("tmpl/main.html", "tmpl/headers.html"))
	errorPage = template.Must(template.ParseFiles("tmpl/error.html", "tmpl/headers.html"))
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
		rsp.WriteHeader(http.StatusInternalServerError)

		call.Context.Errorf("Emitting error template (%s)", errorPage.Execute(rsp, call.writeError(err, nil)))
	}
}

func serveBookPage(rsp http.ResponseWriter, rq *http.Request) {
	call := Call{
		Context:  appengine.NewContext(rq),
		Request:  rq,
		Response: rsp,
	}

	if _, err := serveVolumeSingle(&call); err == nil {
	} else {
		rsp.WriteHeader(http.StatusInternalServerError)

		call.Context.Errorf("Emitting error template (%s)", errorPage.Execute(rsp, call.writeError(err, nil)))
	}
}
