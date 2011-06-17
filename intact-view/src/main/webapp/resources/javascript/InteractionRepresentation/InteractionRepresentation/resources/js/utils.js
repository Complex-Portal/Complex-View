// module with methods used in all parts of the widget
Utils = function(){
	
	// check whether an object is empty
    this.isEmptyObject = function(obj){
        for (var i in obj) {
            if (obj.hasOwnProperty(i)) {
                return false;
            }
        }
        return true;
    };
    
    // count the keys of an object
    this.getObjectKeyCount = function(obj){
        var count = 0;
        for (var i in obj) {
            if (obj.hasOwnProperty(i)) {
                count++;
            }
        }
        return count;
    };
};
