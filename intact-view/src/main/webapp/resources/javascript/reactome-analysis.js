var reactomeAnalysis = function(btn, query, negative, spoke, projection) {

    var reactomeCorsURI;

    if (projection) {
        reactomeCorsURI = 'http://www.reactome.org/AnalysisService/identifiers/url/projection?pageSize=0&page=1';
    } else {
        reactomeCorsURI = 'http://www.reactome.org/AnalysisService/identifiers/url?pageSize=0&page=1';
    }

    // ToDo: make this URI configurable to support local testing. perhaps pass in as argument
    var dataURI = "http://www.ebi.ac.uk/intact/list?query="+ query+"&negative="+negative+"&spoke="+spoke;

    var parent = $(btn).parent();
    parent.text("Loading...");
    parent.removeClass().addClass("reactome-loading");

    $.ajax({
        type: "POST",
        contentType: "text/plain",
        dataType: "json",
        url: reactomeCorsURI,
        data: dataURI,
        success: null //needs to be defined, but null is "something" xD
    })
    .done(function(data) {
            if (data.pathwaysFound > 0) {// results in Reactome
                btn.innerHTML = "View";
                btn.onclick = function(){
                    window.open("http://www.reactome.org/PathwayBrowser/#DTAB=AN&TOOL=AT&ANALYSIS=" + data.summary.token, "_blank");
                };
//                window.open("http://www.reactome.org/PathwayBrowser/#DTAB=AN&TOOL=AT&ANALYSIS=" + data.summary.token, "_blank");
                $(btn).removeClass().addClass("reactome-results");
                parent.removeClass().empty().append(btn);
            } else {// no results in Reactome
                parent.removeClass().addClass("reactome-no-results");
                parent.text("No results");
            }
    })
    .fail(function(response, status) {
        parent.empty();
        switch (response.status){
            case 413: //The file size is larger than the maximum configured size (10MB)
            case 415: //Unsupported Media Type (only 'text/plain')
            case 422: //The provided URL is not processable
                parent.text(response.statusText);
                break;
            default: //Reactome service accessible?
                parent.text("Service not available in this moment");
        }
        parent.removeClass().addClass("reactome-error");
    });
};

var reactomeAnalysisList = function(selectedIds) {

    var reactomeCorsURI = 'http://www.reactome.org/AnalysisService/identifiers/?pageSize=0&page=1&sortBy=ENTITIES_PVALUE&order=ASC&resource=TOTAL';

    var parent = $('.reactome-analyse').parent();
    var btn = $('.reactome-analyse');
    parent.text("Loading...");
    parent.removeClass().addClass("reactome-loading");

    $.ajax({
        type: "POST",
        contentType: "text/plain",
        dataType: "json",
        url: reactomeCorsURI,
        data: selectedIds,
        success: null //needs to be defined, but null is "something" xD
    })
        .done(function(data) {
            if (data.pathwaysFound > 0) {// results in Reactome
                btn.innerHTML = "View";
                btn.onclick = function(){
                    window.open("http://www.reactome.org/PathwayBrowser/#DTAB=AN&TOOL=AT&ANALYSIS=" + data.summary.token, "_blank");
                };
//                window.open("http://www.reactome.org/PathwayBrowser/#DTAB=AN&TOOL=AT&ANALYSIS=" + data.summary.token, "_blank");
                $(btn).removeClass().addClass("reactome-results");
                parent.removeClass().empty().append(btn);
            } else {// no results in Reactome
                parent.removeClass().addClass("reactome-no-results");
                parent.text("No results");
            }
        })
        .fail(function(response, status) {
            parent.empty();
            switch (response.status){
                case 413: //The file size is larger than the maximum configured size (10MB)
                case 415: //Unsupported Media Type (only 'text/plain')
                case 422: //The provided URL is not processable
                    parent.text(response.statusText);
                    break;
                default: //Reactome service accessible?
                    parent.text("Service not available in this moment");
            }
            parent.removeClass().addClass("reactome-error");
        });
};
