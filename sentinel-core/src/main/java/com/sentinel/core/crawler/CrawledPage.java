package com.sentinel.core.crawler;

import com.sentinel.core.http.HttpResponseData;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

public class CrawledPage {
    private final URI url;
    private final HttpResponseData response;
    private final int depth;
    private final List<FormData> forms;
    
    public CrawledPage(URI url, HttpResponseData response, int depth) {
        this.url = url;
        this.response = response;
        this.depth = depth;
        this.forms = new ArrayList<>();
    }
    
    public URI getUrl() { return url; }
    public HttpResponseData getResponse() { return response; }
    public int getDepth() { return depth; }
    public List<FormData> getForms() { return forms; }
    public void addForm(FormData form) { forms.add(form); }
}
