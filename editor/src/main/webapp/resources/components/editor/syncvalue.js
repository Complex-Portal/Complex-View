SyncValue = function() {
};

SyncValue.prototype.setValue = function(id, val) {
    document.getElementById(id).value = val;
};
