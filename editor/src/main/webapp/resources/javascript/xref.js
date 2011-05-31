// Javascript behaviour for the xrefs displayed in the table,
// with functions to mark the secondaryId as readOnly when the
// primaryId is a GO term

EditorXref = function(xrefIndex) {
    this.xrefIndex = xrefIndex;
};

EditorXref.prototype.primaryElemId = null;
EditorXref.prototype.secondaryElemId = null;

EditorXref.prototype.getPrimaryElem = function() {
    return document.getElementById(this.primaryElemId);
};

EditorXref.prototype.getSecondaryElem = function() {
    return document.getElementById(this.secondaryElemId);
};

EditorXref.prototype.markSecondaryAsReadOnlyIfNecessary = function() {

    if (this.getPrimaryElem().value.indexOf("GO:") > -1) {
        this.getSecondaryElem().readOnly = true;
    }
};

