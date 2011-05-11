InputBioSource = function(clientId) {
    this.clientId = clientId;
};

InputBioSource.prototype.displayButtons = function() {
    document.getElementById(this.clientId + ':selectBtn').style.display = 'inline'
    document.getElementById(this.clientId + ':selectBtn').style.visibility = 'visible'
    document.getElementById(this.clientId + ':refreshBtn').style.display = 'inline'
    document.getElementById(this.clientId + ':refreshBtn').style.visibility = 'visible'
    document.getElementById(this.clientId + ':info').style.display = 'none'
    document.getElementById(this.clientId + ':info').style.visibility = 'hidden'
};

InputBioSource.prototype.displayInfo = function () {
    document.getElementById(this.clientId + ':selectBtn').style.display = 'none'
    document.getElementById(this.clientId + ':selectBtn').style.visibility = 'hidden'
    document.getElementById(this.clientId + ':refreshBtn').style.display = 'none'
    document.getElementById(this.clientId + ':refreshBtn').style.visibility = 'hidden'
    document.getElementById(this.clientId + ':info').style.display = 'inline'
    document.getElementById(this.clientId + ':info').style.visibility = 'visible'
};

InputBioSource.prototype.browse = function (dialogWidget) {
    var iframe = document.getElementById(this.clientId + '_bioSourceFrame');
    var src = iframe.src;
    var not_loaded = (src == null || src.length == 0);

    if (not_loaded) {
        iframe.src = this.url;
    }

    dialogWidget.show();
};

InputBioSource.prototype.setUrl = function(url) {
    this.url = url;
};

InputBioSource.prototype.update = function(bioSourceAc, bioSourceLabel, dialogWidget) {
   var selectList = document.getElementById(this.clientId+':selectObject');
    selectList.value = bioSourceAc;
    jsf.ajax.request(this.clientId+':selectObject', null, {execute:'@this',render:this.clientId+':info','javax.faces.behavior.event':'valueChange'});
    dialogWidget.hide();
};

