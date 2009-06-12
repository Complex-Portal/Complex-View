dojo.require("dojox.fx._base");

dojo.declare("ebi.intact.CategoryPanelComp", null, {

    xrefGroups: new Array(),

    relevant: false,

    constructor: function(initiallyExpanded) {
        this.expandedAll = true;
        this.expandedAllRelevant = initiallyExpanded;
    },

    expandAll: function(onlyRelevant) {
        //console.log("Expanding groups: "+this.xrefGroups.length);

        for (i=0; i<this.xrefGroups.length; i++) {
            if (onlyRelevant) {
                this.xrefGroups[i].expandIfRelevant();
            } else {
                this.xrefGroups[i].expand();
            }
        }
    },

    collapseAll: function() {
        for (i = 0; i < this.xrefGroups.length; i++) {
            this.xrefGroups[i].collapse();
        }
    },

    expandAllRelevant: function() {
        this.expandAll(true);
    },

    registerXrefGroup: function(xrefGroup) {
        this.xrefGroups[this.xrefGroups.length] = xrefGroup;
    }
});

dojo.declare("ebi.intact.CategoryGroupComp", null, {

    itemCount: 0,

    constructor: function(initiallyExpanded, headerId, contentId, countId) {
        this.expanded = initiallyExpanded;

        this.headerDiv = document.getElementById(headerId);
        this.contentDiv = document.getElementById(contentId);
        this.countDiv = document.getElementById(countId);

        dojo.connect(this.headerDiv, "onclick", this, "toggleExpanded");

        this.updateState();
    },

    updateState: function() {
        this.headerDiv.innerHTML = escapeHTML((this.expanded)? '-' : '+');
        this.contentDiv.style.display = (this.expanded)? 'block' : 'none';
    },

    toggleExpanded: function() {
        //console.log("Updating expanded state of '"+this.contentDiv.id+"' to "+!this.expanded);

        this.expanded = !this.expanded;
        this.updateState();
    },

    expand: function() {
        this.expanded = true;
        this.updateState();
    },

    expandIfRelevant: function() {
        this.expanded = (this.itemCount > 0);
        this.updateState();
    },

    collapse: function() {
        this.expanded = false;
        this.updateState();
    },

    addItemCount: function(count) {
        this.itemCount += count;
        this.countDiv.innerHTML = escapeHTML(String(this.itemCount));
    }
});

function escapeHTML(someText) {
  var div = document.createElement('div');
  var text = document.createTextNode(someText);
  div.appendChild(text);
  return div.innerHTML;
}