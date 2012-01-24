InputCvObject = function(clientId) {
    this.clientId = clientId;
};

InputCvObject.prototype.browse = function(dialogWidget) {
    var iframe = document.getElementById(this.clientId+'_cvFrame');
    var src = iframe.src;
    var not_loaded = (src == null || src.length == 0);

    if ( not_loaded) {
        // #{dialogWidgetId}_load.show();

        iframe.src = this.url;
        //iframe.onload = #{dialogWidgetId}_showDialog;
    }

    dialogWidget.show();
};

InputCvObject.prototype.setUrl = function(url) {
    this.url = url;
};

InputCvObject.prototype.update = function(cvAc, cvLabel, dialogWidget) {
   var selectList = document.getElementById(this.clientId+':selectObject');
    selectList.value = cvAc;
    jsf.ajax.request(this.clientId+':selectObject', null, {execute:'@this',render:':editorForm:changesPanel :editorForm:unsavedChangesInfo','javax.faces.behavior.event':'valueChange'});
    dialogWidget.hide();
};