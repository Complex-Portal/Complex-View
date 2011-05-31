EditorCuration = function() {
};

EditorCuration.prototype.showUnsavedBar = function() {
    var div = document.getElementById("changesPanel");
    div.style.display = 'block';
};

EditorCuration.prototype.hideUnsavedBar = function() {
    var div = document.getElementById("changesPanel");
    div.style.display = 'none';
};

EditorCuration.prototype.setUnsavedChanges = function(unsaved) {
    this.unsavedChanges = unsaved;

    if (unsaved) {
        this.showUnsavedBar();
    } else {
        this.hideUnsavedBar();
    }
};

EditorCuration.prototype.isUnsaved = function() {
    return this.unsavedChanges;
};