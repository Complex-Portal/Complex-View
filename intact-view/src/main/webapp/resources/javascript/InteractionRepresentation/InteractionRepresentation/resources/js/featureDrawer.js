FeatureDrawer = function(interactionInformation, shapeDrawer, featureSet){

    this._interactionInformation = interactionInformation;
    this._shapeDrawer = shapeDrawer;
    
    // default drawing function for certain begin and end
    this.drawCertainCertain = function(range, y, height, featureStart, interactorLength, colour, rangeColour, opacity, tooltipText, symbol){
        var x = 1;
        var x2 = 1;
        if (!(range.begin === undefined)) {
            x = range.begin.position;
        }
        else 
            if (!(range.beginInterval === undefined)) {
                console.log("begin-status-type 'certain', but interval found");
            }
            else {
                console.log("begin-status-type 'certain', but begin not provided");
            }
        
        if (!(range.end === undefined)) {
            x2 = range.end.position;
        }
        else 
            if (!(range.endInterval === undefined)) {
                console.log("end-status-type 'certain', but interval found");
            }
            else {
                console.log("end-status-type 'certain', but end not provided");
            }
        
		x = Number(x);
		x2 = Number(x2);
		
        var xDraw = x * this._interactionInformation._pxPerAA + featureStart;
        var x2Draw = x2 * this._interactionInformation._pxPerAA + featureStart;
        
        var element = this.drawCertain(xDraw, y, x2Draw - xDraw, height, colour, opacity, symbol, true, true);
        var eventHandlingElement = this.createEventHandlingElement(element);
        
        this.createTooltip(eventHandlingElement, tooltipText, x, x2);
        return {
            "x": x,
            "x2": x2,
            "element": element,
            "eventHandlingElement": eventHandlingElement
        };
    };
    
    this.drawCertainLessThan = function(range, y, height, featureStart, interactorLength, colour, rangeColour, opacity, tooltipText, symbol){
        var x = 1;
        var x2 = 1;
        if (!(range.begin === undefined)) {
            x = range.begin.position;
        }
        else 
            if (!(range.beginInterval === undefined)) {
                console.log("begin-status-type 'certain', but interval found");
            }
            else {
                console.log("begin-status-type 'certain', but begin not provided");
            }
        
        if (!(range.end === undefined)) {
            x2 = range.end.position;
        }
        else 
            if (!(range.endInterval === undefined)) {
                console.log("end-status-type 'less than', but interval found");
            }
            else {
                console.log("end-status-type 'less than', but end not provided");
            }
        
		x = Number(x);
		x2 = Number(x2);
		
        var xDraw = x * this._interactionInformation._pxPerAA + featureStart;
        var x2Draw = x2 * this._interactionInformation._pxPerAA + featureStart;
        
        var element = this._shapeDrawer.getSet();
        var width = x2Draw - xDraw;
        element.push(this.drawCertain(xDraw, y, width, height, colour, opacity, symbol, true, false));
        element.push(this.drawLessThan(x2Draw + 1, y, y + height, "white", opacity, symbol, width));
        var eventHandlingElement = this.createEventHandlingElement(element);
        
        this.createTooltip(eventHandlingElement, tooltipText, x, " <" + x2);
        return {
            "x": x,
            "x2": x2,
            "element": element,
            "eventHandlingElement": eventHandlingElement
        };
    };
    
    // default drawing function for certain begin and end > given position
    this.drawCertainGreaterThan = function(range, y, height, featureStart, interactorLength, colour, rangeColour, opacity, tooltipText, symbol){
        var x = 1;
        var x2 = 1;
        if (!(range.begin === undefined)) {
            x = range.begin.position;
        }
        else 
            if (!(range.beginInterval === undefined)) {
                console.log("begin-status-type 'certain', but interval found");
            }
            else {
                console.log("begin-status-type 'certain', but begin not provided");
            }
        
        if (!(range.end === undefined)) {
            x2 = range.end.position;
        }
        else 
            if (!(range.endInterval === undefined)) {
                console.log("end-status-type 'greater than', but interval found");
            }
            else {
                console.log("end-status-type 'greater than', but end not provided");
            }
        
		x = Number(x);
		x2 = Number(x2);
		
        var xDraw = x * this._interactionInformation._pxPerAA + featureStart;
        var x2Draw = x2 * this._interactionInformation._pxPerAA + featureStart;
        
        var element = this._shapeDrawer.getSet();
        var width = x2Draw - xDraw;
        element.push(this.drawCertain(xDraw, y, width, height, colour, opacity, symbol, true, false));
        element.push(this.drawGreaterThan(x2Draw, y, y + height, colour, opacity, symbol, width));
        var eventHandlingElement = this.createEventHandlingElement(element);
        
        this.createTooltip(eventHandlingElement, tooltipText, x, x2 + " <");
        return {
            "x": x,
            "x2": x2,
            "element": element,
            "eventHandlingElement": eventHandlingElement
        };
    };
    
    
    // default drawing function for certain begin and end's position in a certain range
    this.drawCertainRange = function(range, y, height, featureStart, interactorLength, colour, rangeColour, opacity, tooltipText, symbol){
        var specified = true;
        var xBegin = 1;
        var xEnd = 1;
        var xEnd2 = 1;
        if (!(range.begin === undefined)) {
            xBegin = range.begin.position;
        }
        else 
            if (!(range.beginInterval === undefined)) {
                console.log("begin-status-type 'certain', but interval found");
            }
            else {
                console.log("begin-status-type 'certain', but begin not provided");
            }
        
        if (!(range.end === undefined)) {
            specified = false;
            xEnd = range.end.position;
        }
        else 
            if (!(range.endInterval === undefined)) {
                xEnd = range.endInterval.begin;
                xEnd2 = range.endInterval.end;
            }
            else {
                console.log("end-status-type 'range', but end not provided");
            }
        
		xBegin = Number(xBegin);
		xEnd2 = Number(xEnd2);
		xEnd = Number(xEnd);
		
        var element = this._shapeDrawer.getSet();
        
        var xBeginDraw = xBegin * this._interactionInformation._pxPerAA + featureStart;
        var xEndDraw = xEnd * this._interactionInformation._pxPerAA + featureStart;
        var xEnd2Draw = xEnd2 * this._interactionInformation._pxPerAA + featureStart;
        
		var width = xEndDraw - xBeginDraw;
        element.push(this.drawCertain(xBeginDraw, y, width, height, colour, opacity, symbol, true, false));
        if (specified) {
            element.push(this.drawSpecifiedRight(xEndDraw, y, xEnd2Draw - xEndDraw, height, colour, rangeColour, opacity, symbol, width));
            var eventHandlingElement = this.createEventHandlingElement(element);
            
            this.createTooltip(eventHandlingElement, tooltipText, xBegin, "[" + xEnd + " - " + xEnd2 + "]");
            return {
                "x": xBegin,
                "x2": xEnd2,
                "element": element,
                "eventHandlingElement": eventHandlingElement
            };
        }
        else {
            element.push(this.drawUnspecifiedRight(xEndDraw, y, height, colour, opacity, symbol, width));
            var eventHandlingElement = this.createEventHandlingElement(element);
            
            this.createTooltip(eventHandlingElement, tooltipText, xBegin, "~" + xEnd);
            return {
                "x": xBegin,
                "x2": xEnd,
                "element": element,
                "eventHandlingElement": eventHandlingElement
            };
        }
    };
    
    // default drawing function for begin > given position and certain end
    this.drawGreaterThanCertain = function(range, y, height, featureStart, interactorLength, colour, rangeColour, opacity, tooltipText, symbol){
        var x = 1;
        var x2 = 1;
        if (!(range.begin === undefined)) {
            x = range.begin.position;
        }
        else 
            if (!(range.beginInterval === undefined)) {
                console.log("begin-status-type 'certain', but interval found");
            }
            else {
                console.log("begin-status-type 'certain', but begin not provided");
            }
        
        if (!(range.end === undefined)) {
            x2 = range.end.position;
        }
        else 
            if (!(range.endInterval === undefined)) {
                console.log("end-status-type 'less than', but interval found");
            }
            else {
                console.log("end-status-type 'less than', but end not provided");
            }
        
		x = Number(x);
		x2 = Number(x2);
		
        var xDraw = x * this._interactionInformation._pxPerAA + featureStart;
        var x2Draw = x2 * this._interactionInformation._pxPerAA + featureStart;
        
        var width = x2Draw - xDraw;
        var element = this._shapeDrawer.getSet();
        element.push(this.drawCertain(xDraw, y, width, height, colour, opacity, symbol, false, true));
        element.push(this.drawGreaterThan(xDraw - 1, y, y + height, "white", opacity, symbol, width));
        var eventHandlingElement = this.createEventHandlingElement(element);
        
        this.createTooltip(eventHandlingElement, tooltipText, x, " <" + x2);
        return {
            "x": x,
            "x2": x2,
            "element": element,
            "eventHandlingElement": eventHandlingElement
        };
    }
    
    
    // default drawing function for begin > given position and end > given position
    this.drawGreaterThanGreaterThan = function(range, y, height, featureStart, interactorLength, colour, rangeColour, opacity, tooltipText, symbol){
        var x = 1;
        var x2 = 1;
        if (!(range.begin === undefined)) {
            x = range.begin.position;
        }
        else 
            if (!(range.beginInterval === undefined)) {
                console.log("begin-status-type 'certain', but interval found");
            }
            else {
                console.log("begin-status-type 'certain', but begin not provided");
            }
        
        if (!(range.end === undefined)) {
            x2 = range.end.position;
        }
        else 
            if (!(range.endInterval === undefined)) {
                console.log("end-status-type 'greater than', but interval found");
            }
            else {
                console.log("end-status-type 'greater than', but end not provided");
            }
        
		x = Number(x);
		x2 = Number(x2);
		
        var xDraw = x * this._interactionInformation._pxPerAA + featureStart;
        var x2Draw = x2 * this._interactionInformation._pxPerAA + featureStart;
        
        var element = this._shapeDrawer.getSet();
        var width = x2Draw - xDraw;
        element.push(this.drawCertain(xDraw, y, width, height, colour, opacity, symbol, false, false));
        element.push(this.drawGreaterThan(x2Draw, y, y + height, colour, opacity, symbol, width));
        element.push(this.drawGreaterThan(xDraw - 1, y, y + height, "white", opacity, symbol, width));
        var eventHandlingElement = this.createEventHandlingElement(element);
        
        this.createTooltip(eventHandlingElement, tooltipText, x, x2 + " <");
        return {
            "x": x,
            "x2": x2,
            "element": element,
            "eventHandlingElement": eventHandlingElement
        };
    }
    
    
    // default drawing function for begin > given position and end < given position
    this.drawGreaterThanLessThan = function(range, y, height, featureStart, interactorLength, colour, rangeColour, opacity, tooltipText, symbol){
        var x = 1;
        var x2 = 1;
        if (!(range.begin === undefined)) {
            x = range.begin.position;
        }
        else 
            if (!(range.beginInterval === undefined)) {
                console.log("begin-status-type 'certain', but interval found");
            }
            else {
                console.log("begin-status-type 'certain', but begin not provided");
            }
        
        if (!(range.end === undefined)) {
            x2 = range.end.position;
        }
        else 
            if (!(range.endInterval === undefined)) {
                console.log("end-status-type 'less than', but interval found");
            }
            else {
                console.log("end-status-type 'less than', but end not provided");
            }
        
		x = Number(x);
		x2 = Number(x2);
		
        var xDraw = x * this._interactionInformation._pxPerAA + featureStart;
        var x2Draw = x2 * this._interactionInformation._pxPerAA + featureStart;
        
        var element = this._shapeDrawer.getSet();
        var width = x2Draw - xDraw;
        element.push(this.drawCertain(xDraw, y, width, height, colour, opacity, symbol, false, false));
        element.push(this.drawLessThan(x2Draw + 1, y, y + height, "white", opacity, symbol, width));
        element.push(this.drawGreaterThan(xDraw - 1, y, y + height, "white", opacity, symbol, width));
        var eventHandlingElement = this.createEventHandlingElement(element);
        
        this.createTooltip(eventHandlingElement, tooltipText, x, " <" + x2);
        return {
            "x": x,
            "x2": x2,
            "element": element,
            "eventHandlingElement": eventHandlingElement
        };
    }
    
    
    // default drawing function for begin > given position and end's position in a certain range
    this.drawGreaterThanRange = function(range, y, height, featureStart, interactorLength, colour, rangeColour, opacity, tooltipText, symbol){
        var specified = true;
        var xBegin = 1;
        var xEnd = 1;
        var xEnd2 = 1;
        if (!(range.begin === undefined)) {
            xBegin = range.begin.position;
        }
        else 
            if (!(range.beginInterval === undefined)) {
                console.log("begin-status-type 'certain', but interval found");
            }
            else {
                console.log("begin-status-type 'certain', but begin not provided");
            }
        
        if (!(range.end === undefined)) {
            specified = false;
            xEnd = range.end.position;
        }
        else 
            if (!(range.endInterval === undefined)) {
                xEnd = range.endInterval.begin;
                xEnd2 = range.endInterval.end;
            }
            else {
                console.log("end-status-type 'range', but end not provided");
            }
       
	    xBegin = Number(xBegin);
		xEnd = Number(xEnd);
		xEnd2 = Number(xEnd2);
	    
        var element = this._shapeDrawer.getSet();
        
        var xBeginDraw = xBegin * this._interactionInformation._pxPerAA + featureStart;
        var xEndDraw = xEnd * this._interactionInformation._pxPerAA + featureStart;
        var xEnd2Draw = xEnd2 * this._interactionInformation._pxPerAA + featureStart;
        
        var width = xEndDraw - xBeginDraw;
        element.push(this.drawCertain(xBeginDraw, y, width, height, colour, opacity, symbol, false, false));
        element.push(this.drawGreaterThan(xBeginDraw - 1, y, y + height, "white", opacity, symbol, width));
        if (specified) {
            element.push(this.drawSpecifiedRight(xEndDraw, y, xEnd2Draw - xEndDraw, height, colour, rangeColour, opacity, symbol, width));
            var eventHandlingElement = this.createEventHandlingElement(element);
            
            this.createTooltip(eventHandlingElement, tooltipText, xBegin, "[" + xEnd + " - " + xEnd2 + "]");
            return {
                "x": xBegin,
                "x2": xEnd2,
                "element": element,
                "eventHandlingElement": eventHandlingElement
            };
        }
        else {
            element.push(this.drawUnspecifiedRight(xEndDraw, y, height, colour, opacity, symbol, width));
            var eventHandlingElement = this.createEventHandlingElement(element);
            
            this.createTooltip(eventHandlingElement, tooltipText, xBegin, "~" + xEnd);
            return {
                "x": xBegin,
                "x2": xEnd,
                "element": element,
                "eventHandlingElement": eventHandlingElement
            };
        }
    }
    
    
    // default drawing function for begin < given position and certain end
    this.drawLessThanCertain = function(range, y, height, featureStart, interactorLength, colour, rangeColour, opacity, tooltipText, symbol){
        var x = 1;
        var x2 = 1;
        if (!(range.begin === undefined)) {
            x = range.begin.position;
        }
        else 
            if (!(range.beginInterval === undefined)) {
                console.log("begin-status-type 'less than', but interval found");
            }
            else {
                console.log("begin-status-type 'less than', but begin not provided");
            }
        
        if (!(range.end === undefined)) {
            x2 = range.end.position;
        }
        else 
            if (!(range.endInterval === undefined)) {
                console.log("end-status-type 'certain', but interval found");
            }
            else {
                console.log("end-status-type 'certain', but end not provided");
            }
        
		x = Number(x);
		x2 = Number(x2);
		
        var xDraw = x * this._interactionInformation._pxPerAA + featureStart;
        var x2Draw = x2 * this._interactionInformation._pxPerAA + featureStart;
        
        var element = this._shapeDrawer.getSet();
        var width = x2Draw - xDraw;
        element.push(this.drawCertain(xDraw, y, width, height, colour, opacity, symbol, false, true));
        element.push(this.drawLessThan(xDraw, y, y + height, colour, opacity, symbol, width));
        var eventHandlingElement = this.createEventHandlingElement(element);
        
        this.createTooltip(eventHandlingElement, tooltipText, "< " + x, x2);
        return {
            "x": x,
            "x2": x2,
            "element": element,
            "eventHandlingElement": eventHandlingElement
        };
    };
    
    // default drawing function for begin < given position and end < given position
    this.drawLessThanLessThan = function(range, y, height, featureStart, interactorLength, colour, rangeColour, opacity, tooltipText, symbol){
        var x = 1;
        var x2 = 1;
        if (!(range.begin === undefined)) {
            x = range.begin.position;
        }
        else 
            if (!(range.beginInterval === undefined)) {
                console.log("begin-status-type 'less than', but interval found");
            }
            else {
                console.log("begin-status-type 'less than', but begin not provided");
            }
        
        if (!(range.end === undefined)) {
            x2 = range.end.position;
        }
        else 
            if (!(range.endInterval === undefined)) {
                console.log("end-status-type 'less than', but interval found");
            }
            else {
                console.log("end-status-type 'less than', but end not provided");
            }
        
		x = Number(x);
		x2 = Number(x2);
		
        var xDraw = x * this._interactionInformation._pxPerAA + featureStart;
        var x2Draw = x2 * this._interactionInformation._pxPerAA + featureStart;
        
        var element = this._shapeDrawer.getSet();
        var width = x2Draw - xDraw;
        element.push(this.drawCertain(xDraw, y, width, height, colour, opacity, symbol, false, false));
        element.push(this.drawLessThan(x2Draw + 1, y, y + height, "white", opacity, symbol, width));
        element.push(this.drawLessThan(xDraw, y, y + height, colour, opacity, symbol, width));
        var eventHandlingElement = this.createEventHandlingElement(element);
        
        this.createTooltip(eventHandlingElement, tooltipText, "< " + x, " <" + x2);
        return {
            "x": x,
            "x2": x2,
            "element": element,
            "eventHandlingElement": eventHandlingElement
        };
    }
    
    // default drawing function for begin < given position and end > given position
    this.drawLessThanGreaterThan = function(range, y, height, featureStart, interactorLength, colour, rangeColour, opacity, tooltipText, symbol){
        var x = 1;
        var x2 = 1;
        if (!(range.begin === undefined)) {
            x = range.begin.position;
        }
        else 
            if (!(range.beginInterval === undefined)) {
                console.log("begin-status-type 'less than', but interval found");
            }
            else {
                console.log("begin-status-type 'less than', but begin not provided");
            }
        
        if (!(range.end === undefined)) {
            x2 = range.end.position;
        }
        else 
            if (!(range.endInterval === undefined)) {
                console.log("end-status-type 'greater than', but interval found");
            }
            else {
                console.log("end-status-type 'greater than', but end not provided");
            }
       
	    x = Number(x);
		x2 = Number(x2);
	    
        var xDraw = x * this._interactionInformation._pxPerAA + featureStart;
        var x2Draw = x2 * this._interactionInformation._pxPerAA + featureStart;
        
        var element = this._shapeDrawer.getSet();
        var width = x2Draw - xDraw;
        element.push(this.drawCertain(xDraw, y, width, height, colour, opacity, symbol, false, false));
        element.push(this.drawGreaterThan(x2Draw, y, y + height, colour, opacity, symbol, width));
        element.push(this.drawLessThan(xDraw, y, y + height, colour, opacity, symbol, width));
        var eventHandlingElement = this.createEventHandlingElement(element);
        
        this.createTooltip(eventHandlingElement, tooltipText, "< " + x, x2 + " <");
        return {
            "x": x,
            "x2": x2,
            "element": element,
            "eventHandlingElement": eventHandlingElement
        };
    };
    
    
    // default drawing function for begin < given position and end's position in a certain range
    this.drawLessThanRange = function(range, y, height, featureStart, interactorLength, colour, rangeColour, opacity, tooltipText, symbol){
        var xBegin = 1;
        var xEnd = 1;
        var xEnd2 = 1;
        var specified = true;
        if (!(range.begin === undefined)) {
            xBegin = range.begin.position;
        }
        else 
            if (!(range.beginInterval === undefined)) {
                console.log("begin-status-type 'less than', but interval found");
            }
            else {
                console.log("begin-status-type 'less than', but begin not provided");
            }
        
        if (!(range.end === undefined)) {
            specified = false;
            xEnd = range.end.position;
        }
        else 
            if (!(range.endInterval === undefined)) {
                xEnd = range.endInterval.begin;
                xEnd2 = range.endInterval.end;
            }
            else {
                console.log("end-status-type 'range', but end not provided");
            }
        
		xBegin = Number(xBegin);
		xEnd2 = Number(xEnd2);
		xEnd = Number(xEnd);
		
        var xBeginDraw = xBegin * this._interactionInformation._pxPerAA + featureStart;
        var xEndDraw = xEnd * this._interactionInformation._pxPerAA + featureStart;
        var xEnd2Draw = xEnd2 * this._interactionInformation._pxPerAA + featureStart;
        
        var element = this._shapeDrawer.getSet();
        var width = xEndDraw - xBeginDraw;
        element.push(this.drawLessThan(xBeginDraw, y, y + height, colour, opacity, symbol, width));
        element.push(this.drawCertain(xBeginDraw, y, width, height, colour, opacity, symbol, false, false));
		
        if (specified) {
            element.push(this.drawSpecifiedRight(xEndDraw, y, xEnd2Draw - xEndDraw, height, colour, rangeColour, opacity, symbol, width));
            var eventHandlingElement = this.createEventHandlingElement(element);
            
            this.createTooltip(eventHandlingElement, tooltipText, "< " + xBegin, "[" + xEnd + " - " + xEnd2 + "]");
            return {
                "x": xBegin,
                "x2": xEnd2,
                "element": element,
                "eventHandlingElement": eventHandlingElement
            };
        }
        else {
            element.push(this.drawUnspecifiedRight(xEndDraw, y, height, colour, opacity, symbol, width));
            var eventHandlingElement = this.createEventHandlingElement(element);
            
            this.createTooltip(eventHandlingElement, tooltipText, "< " + xBegin, "~" + xEnd);
            return {
                "x": xBegin,
                "x2": xEnd,
                "element": element,
                "eventHandlingElement": eventHandlingElement
            };
        }
    };
    
    
    this.drawLessThanUndetermined = function(range, y, height, featureStart, interactorLength, colour, rangeColour, opacity, tooltipText, symbol){
        var x = 1;
        if (!(range.begin === undefined)) {
            x = range.begin.position;
        }
        else 
            if (!(range.beginInterval === undefined)) {
                console.log("begin-status-type 'less than', but interval found");
            }
            else {
                console.log("begin-status-type 'less than', but begin not provided");
            }
        
		x = Number(x);
		
        var xDraw = x * this._interactionInformation._pxPerAA + featureStart;
        var xUndet = xDraw;
        var element = this._shapeDrawer.getSet();
        
        if (symbol != "") {
        	element.push(this.drawSymbol(symbol, xDraw, y, height, colour, false, opacity));
			xUndet += 4;
        }
        
        element.push(this.drawLessThan(xDraw, y, y + height, colour, opacity, symbol, 0));
        element.push(this.drawUndetermined(xUndet + 1, y, height, opacity, symbol));
        
        var eventHandlingElement = this.createEventHandlingElement(element);
        this.createTooltip(eventHandlingElement, tooltipText, "< " + x, "?");
        
        return {
            "x": x,
            "x2": x,
            "element": element,
            "eventHandlingElement": eventHandlingElement
        };
    }
    
    
    // default drawing function for begin's position in a certain range and certain end
    this.drawRangeCertain = function(range, y, height, featureStart, interactorLength, colour, rangeColour, opacity, tooltipText, symbol){
        var specified = true;
        var xBegin = 1;
        var xBegin2 = 1;
        var xEnd = 1;
        
        if (!(range.begin === undefined)) {
            specified = false;
            xBegin = range.begin.position;
        }
        else 
            if (!(range.beginInterval === undefined)) {
                xBegin = range.beginInterval.begin;
                xBegin2 = range.beginInterval.end;
            }
            else {
                console.log("begin-status-type 'range', but end not provided");
            }
        
        if (!(range.end === undefined)) {
            xEnd = range.end.position;
        }
        else 
            if (!(range.endInterval === undefined)) {
                console.log("end-status-type 'certain', but interval found");
            }
            else {
                console.log("end-status-type 'certain', but begin not provided");
            }
        
		xBegin = Number(xBegin);
		xBegin2 = Number(xBegin2);
		xEnd = Number(xEnd);
		
        var xBeginDraw = xBegin * this._interactionInformation._pxPerAA + featureStart;
        var xBegin2Draw = xBegin2 * this._interactionInformation._pxPerAA + featureStart;
        var xEndDraw = xEnd * this._interactionInformation._pxPerAA + featureStart;
        
        var positionTextBegin = "";
        var element = this._shapeDrawer.getSet();
		var width = xEndDraw - xBegin2Draw;
        if (specified) {
            element.push(this.drawCertain(xBegin2Draw, y, width, height, colour, opacity, symbol, false, true));
            element.push(this.drawSpecifiedLeft(xBeginDraw, y, xBegin2Draw - xBeginDraw, height, colour, rangeColour, opacity, symbol, width));
            
            positionTextBegin = "[" + xBegin + " - " + xBegin2 + "]";
        }
        else {
            element.push(this.drawCertain(xBeginDraw, y, xEndDraw - xBeginDraw, height, colour, opacity, symbol, false, true));
            element.push(this.drawUnspecifiedLeft(xBeginDraw, y, height, colour, opacity, symbol, width));
            positionTextBegin = "~" + xBegin;
        }
        
        var eventHandlingElement = this.createEventHandlingElement(element);
        
        this.createTooltip(eventHandlingElement, tooltipText, positionTextBegin, xEnd);
        return {
            "x": xBegin,
            "x2": xEnd,
            "element": element,
            "eventHandlingElement": eventHandlingElement
        };
    };
    
    this.drawRangeLessThan = function(range, y, height, featureStart, interactorLength, colour, rangeColour, opacity, tooltipText, symbol){
        var specified = true;
        var xBegin = 1;
        var xBegin2 = 1;
        var xEnd = 1;
        
        if (!(range.begin === undefined)) {
            specified = false;
            xBegin = range.begin.position;
        }
        else 
            if (!(range.beginInterval === undefined)) {
                xBegin = range.beginInterval.begin;
                xBegin2 = range.beginInterval.end;
            }
            else {
                console.log("begin-status-type 'range', but end not provided");
            }
        
        if (!(range.end === undefined)) {
            xEnd = range.end.position;
        }
        else 
            if (!(range.endInterval === undefined)) {
                console.log("end-status-type 'less than', but interval found");
            }
            else {
                console.log("end-status-type 'less than', but begin not provided");
            }
        
        xBegin = Number(xBegin);
		xBegin2 = Number(xBegin2);
		xEnd = Number(xEnd);
		
        var xBeginDraw = xBegin * this._interactionInformation._pxPerAA + featureStart;
        var xBegin2Draw = xBegin2 * this._interactionInformation._pxPerAA + featureStart;
        var xEndDraw = xEnd * this._interactionInformation._pxPerAA + featureStart;
        
        var element = this._shapeDrawer.getSet();
        
        var width = 0;
        
        if (specified) {
            width = xEndDraw - xBegin2Draw;
            element.push(this.drawCertain(xBegin2Draw, y, width, height, colour, opacity, symbol, false, false));
            element.push(this.drawSpecifiedLeft(xBeginDraw, y, xBegin2Draw - xBeginDraw, height, colour, rangeColour, opacity, symbol, width));
            positionTextBegin = "[" + xBegin + " - " + xBegin2 + "]";
        }
        else {
            width = xEndDraw - xBeginDraw;
            element.push(this.drawCertain(xBeginDraw, y, width, height, colour, opacity, symbol, false, false));
            element.push(this.drawUnspecifiedLeft(xBeginDraw, y, height, colour, opacity, symbol, width));
            positionTextBegin = "~" + xBegin;
        }
        
        element.push(this.drawLessThan(xEndDraw + 1, y, y + height, "white", opacity, symbol, width));
        
        var eventHandlingElement = this.createEventHandlingElement(element);
        
        this.createTooltip(eventHandlingElement, tooltipText, positionTextBegin, " <" + xEnd);
        return {
            "x": xBegin,
            "x2": xEnd,
            "element": element,
            "eventHandlingElement": eventHandlingElement
        };
    }
    
    // default drawing function for begin's position in a certain range and end > given position
    this.drawRangeGreaterThan = function(range, y, height, featureStart, interactorLength, colour, rangeColour, opacity, tooltipText, symbol){
        var specified = true;
        var xBegin = 1;
        var xBegin2 = 1;
        var xEnd = 1;
        
        if (!(range.begin === undefined)) {
            specified = false;
            xBegin = range.begin.position;
        }
        else 
            if (!(range.beginInterval === undefined)) {
                xBegin = range.beginInterval.begin;
                xBegin2 = range.beginInterval.end;
            }
            else {
                console.log("begin-status-type 'range', but end not provided");
            }
        
        if (!(range.end === undefined)) {
            xEnd = range.end.position;
        }
        else 
            if (!(range.endInterval === undefined)) {
                console.log("end-status-type 'greater than', but interval found");
            }
            else {
                console.log("end-status-type 'greater than', but begin not provided");
            }
        
		xBegin = Number(xBegin);
		xBegin2 = Number(xBegin2);
		xEnd = Number(xEnd);
        
        var xBeginDraw = xBegin * this._interactionInformation._pxPerAA + featureStart;
        var xBegin2Draw = xBegin2 * this._interactionInformation._pxPerAA + featureStart;
        var xEndDraw = xEnd * this._interactionInformation._pxPerAA + featureStart;
        
        var element = this._shapeDrawer.getSet();
        element.push(this.drawGreaterThan(xEndDraw, y, y + height, colour, opacity, symbol, width));
        var width = xEndDraw - xBegin2Draw;
        if (specified) {
            element.push(this.drawSpecifiedLeft(xBeginDraw, y, xBegin2Draw - xBeginDraw, height, colour, rangeColour, opacity, symbol, width));
			element.push(this.drawCertain(xBegin2Draw, y, xEndDraw - xBegin2Draw, height, colour, opacity, symbol, false, false));
            positionTextBegin = "[" + xBegin + " - " + xBegin2 + "]";
        }
        else {
			width = xEndDraw - xBeginDraw;
			element.push(this.drawCertain(xBeginDraw, y, xEndDraw - xBeginDraw, height, colour, opacity, symbol, false, false));
            element.push(this.drawUnspecifiedLeft(xBeginDraw, y, height, colour, opacity, symbol, width));
            positionTextBegin = "~" + xBegin;
        }
        
        
        var eventHandlingElement = this.createEventHandlingElement(element);
        
        this.createTooltip(eventHandlingElement, tooltipText, positionTextBegin, xEnd + " <");
        return {
            "x": xBegin,
            "x2": xEnd,
            "element": element,
            "eventHandlingElement": eventHandlingElement
        };
    };
    
    
    // default drawing function for begin's and end's position in a certain range
    this.drawRangeRange = function(range, y, height, featureStart, interactorLength, colour, rangeColour, opacity, tooltipText, symbol){
        var specifiedBegin = true;
        var specifiedEnd = true;
        var xBegin = 1;
        var xBegin2 = 1;
        var xEnd = 1;
        var xEnd2 = 1;
        
        if (!(range.begin === undefined)) {
            specifiedBegin = false;
            xBegin = range.begin.position;
        }
        else 
            if (!(range.beginInterval === undefined)) {
                xBegin = range.beginInterval.begin;
                xBegin2 = range.beginInterval.end;
            }
            else {
                console.log("begin-status-type 'range', but end not provided");
            }
        
        if (!(range.end === undefined)) {
            specifiedEnd = false;
            xEnd = range.end.position;
        }
        else 
            if (!(range.endInterval === undefined)) {
                xEnd = range.endInterval.begin;
                xEnd2 = range.endInterval.end;
            }
            else {
                console.log("end-status-type 'range', but end not provided");
            }
        
		xBegin = Number(xBegin);
		xBegin2 = Number(xBegin2);
		xEnd = Number(xEnd);
		xEnd2 = Number(xEnd2);
		
        var xBeginDraw = xBegin * this._interactionInformation._pxPerAA + featureStart;
        var xBegin2Draw = xBegin2 * this._interactionInformation._pxPerAA + featureStart;
        var xEndDraw = xEnd * this._interactionInformation._pxPerAA + featureStart;
        var xEnd2Draw = xEnd2 * this._interactionInformation._pxPerAA + featureStart;
        
        var element = this._shapeDrawer.getSet();
		var width = 0;
        if (specifiedBegin) {
			width = xEndDraw - xBegin2Draw;
            element.push(this.drawCertain(xBegin2Draw, y, width, height, colour, opacity, symbol, false, false));
            element.push(this.drawSpecifiedLeft(xBeginDraw, y, xBegin2Draw - xBeginDraw, height, colour, rangeColour, opacity, symbol, width));
            positionTextBegin = "[" + xBegin + " - " + xBegin2 + "]";
        }
        else {
			width = xEndDraw - xBeginDraw;
            element.push(this.drawCertain(xBeginDraw, y, xEndDraw - xBeginDraw, height, colour, opacity, symbol, false, false));
            element.push(this.drawUnspecifiedLeft(xBeginDraw, y, height, colour, opacity, symbol, width));
            positionTextBegin = "~" + xBegin;
        }
        
        if (specifiedEnd) {
            element.push(this.drawSpecifiedRight(xEndDraw, y, xEnd2Draw - xEndDraw, height, colour, rangeColour, opacity, symbol, width));
            
            var eventHandlingElement = this.createEventHandlingElement(element);
            
            this.createTooltip(eventHandlingElement, tooltipText, positionTextBegin, "[" + xEnd + " - " + xEnd2 + "]");
            return {
                "x": xBegin,
                "x2": xEnd2,
                "element": element,
                "eventHandlingElement": eventHandlingElement
            };
        }
        else {
            element.push(this.drawUnspecifiedRight(xEndDraw, y, height, colour, opacity, symbol, width));
            
            var eventHandlingElement = this.createEventHandlingElement(element);
            
            this.createTooltip(eventHandlingElement, tooltipText, positionTextBegin, "~" + xEnd);
            return {
                "x": xBegin,
                "x2": xEnd,
                "element": element,
                "eventHandlingElement": eventHandlingElement
            };
        }
    };
    
    
    // default drawing function for n-terminal position
    this.drawNTerminalPosition = function(range, y, height, featureStart, interactorLength, colour, rangeColour, opacity, tooltipText, symbol){
        var x = 1;
        if (!(range.begin === undefined) && range.begin.position != 0) {
            x = x + range.begin.position;
        }
		x = Number(x);
		
        var xDraw = x + featureStart;
        var element = this.drawCertain(xDraw, y, this._interactionInformation._pxPerAA, height, colour, opacity, symbol, false, false);
        var eventHandlingElement = this.createEventHandlingElement(element);
        
        this.createTooltip(eventHandlingElement, tooltipText, "0", "0");
        return {
            "x": x,
            "x2": x,
            "element": element,
            "eventHandlingElement": eventHandlingElement
        };
    };
    
    
    // default drawing function for n-terminal range
    this.drawNTerminalRange = function(range, y, height, featureStart, interactorLength, colour, rangeColour, opacity, tooltipText, symbol){
        var x = 1;
        if (!(range.begin === undefined) && range.begin.position != 0) {
            x = x + range.begin.position;
        }
		x = Number(x);
		
		var element = this._shapeDrawer.getSet();
		
        var xDraw = x + featureStart;
		if(symbol != ""){
			element.push(this.drawSymbol(symbol, xDraw, y, height, colour, false, opacity));
		}
        element.push(this.drawGreaterThan(xDraw, y, y + height, colour, opacity, symbol, 0));
        var eventHandlingElement = this.createEventHandlingElement(element);
        
        this.createTooltip(eventHandlingElement, tooltipText, "0", "0");
        return {
            "x": x,
            "x2": x,
            "element": element,
            "eventHandlingElement": eventHandlingElement
        };
    };
    
    
    // default drawing function for c-terminal position
    this.drawCTerminalPosition = function(range, y, height, featureStart, interactorLength, colour, rangeColour, opacity, tooltipText, symbol){
        var width = this._interactionInformation._pxPerAA;
        var x = 1;
        interactorLength *= this._interactionInformation._pxPerAA;
        if (!(range.begin === undefined) && range.begin.position != 0) {
            x = x + range.begin.position - interactorLength;
        }
		x = Number(x);
		
        var xDraw = x + interactorLength - width + featureStart;
        var element = this.drawCertain(xDraw, y, width, height, colour, opacity, symbol, false, false);
        var eventHandlingElement = this.createEventHandlingElement(element);
        
        this.createTooltip(eventHandlingElement, tooltipText, "0", "0");
        return {
            "x": x,
            "x2": x,
            "element": element,
            "eventHandlingElement": eventHandlingElement
        };
    };
    
    // default drawing function for c-terminal range
    this.drawCTerminalRange = function(range, y, height, featureStart, interactorLength, colour, rangeColour, opacity, tooltipText, symbol){
        var x = 1;
        interactorLength *= this._interactionInformation._pxPerAA;
        if (!(range.begin === undefined) && range.begin.position != 0) {
            x = x + range.begin.position - interactorLength;
        }
		x = Number(x);
		
        var xDraw = x + interactorLength + featureStart;
		
        var element = this._shapeDrawer.getSet();
		
		if(symbol != ""){
			element.push(this.drawSymbol(symbol, xDraw, y, height, colour, false, opacity));
		}
		 
		element.push(this.drawLessThan(xDraw, y, y + height, colour, opacity, symbol, 0));
        var eventHandlingElement = this.createEventHandlingElement(element);
        
        this.createTooltip(eventHandlingElement, tooltipText, "0", "0");
        return {
            "x": x,
            "x2": x,
            "element": element,
            "eventHandlingElement": eventHandlingElement
        };
    };
    
    this.drawUndeterminedGreaterThan = function(range, y, height, featureStart, interactorLength, colour, rangeColour, opacity, tooltipText, symbol){
        var x = 1;
        if (!(range.end === undefined)) {
            x = range.end.position;
        }
        else 
            if (!(range.endInterval === undefined)) {
                console.log("end-status-type 'greater than', but interval found");
            }
            else {
                console.log("end-status-type 'greater than', but end not provided");
            }
        x = Number(x);
		
        var xDraw = x * this._interactionInformation._pxPerAA + featureStart;
        var xUndet = xDraw;
		
        var element = this._shapeDrawer.getSet();
		
		if (symbol != "") {
        	element.push(this.drawSymbol(symbol, xDraw, y, height, colour, false, opacity));
			xUndet -= 3;
        }
		
        element.push(this.drawGreaterThan(xDraw, y, y + height, colour, opacity, symbol, 1));
        element.push(this.drawUndetermined(xUndet - 7, y, height, opacity, symbol));
        
        var eventHandlingElement = this.createEventHandlingElement(element);
        this.createTooltip(eventHandlingElement, tooltipText, "< " + x, "?");
        
        return {
            "x": x,
            "x2": x,
            "element": element,
            "eventHandlingElement": eventHandlingElement
        };
    };
    
    // tags' drawing function for undeterminded position
    this.drawNonPositionalTag = function(range, y, height, featureStart, interactorLength, colour, rangeColour, opacity, tooltipText, symbol){
        var x = 1;
        if (!(range.begin === undefined) && range.begin.position != 0) {
            x += range.begin.position;
        }else{
            x = x - this._interactionInformation._proteinX + 2;
        }
		x = Number(x);
		
        var xDraw = x + featureStart;
        var circle = this._shapeDrawer.getCircle(xDraw, y, 3 * height / 4);
        circle.attr("stroke", colour);
        circle.attr("fill", colour);
        circle.attr("fill-opacity", opacity);
        
        var eventHandlingElement = this.createEventHandlingElement(circle);
        featureSet.push(circle);
        
        this.createTooltip(eventHandlingElement, tooltipText, "0", "0");
        return {
            "x": x,
            "x2": x,
            "element": circle,
            "eventHandlingElement": eventHandlingElement
        };
    };
    
    
    // isotopes' drawing function for undeterminded position
    this.drawNonPositionalIsotope = function(range, y, height, featureStart, interactorLength, colour, rangeColour, opacity, tooltipText, symbol){
        var x = 1;
        if (!(range.begin === undefined) && range.begin.position != 0) {
            x += range.begin.position;
        }else{
            x = x - this._interactionInformation._proteinX + 4.5;
        }
		x = Number(x);
		
        var xDraw = x + featureStart;
        var element = this.drawIsotope(xDraw, y, height - 1.5, colour, true, opacity);
        var eventHandlingElement = this.createEventHandlingElement(element);
        
        this.createTooltip(eventHandlingElement, tooltipText, "0", "0");
        return {
            "x": x,
            "x2": x,
            "element": element,
            "eventHandlingElement": eventHandlingElement
        };
    };
    
	this.drawUnrecognisedRangeType = function(range, y, height, featureStart, interactorLength, colour, rangeColour, opacity, tooltipText, symbol){
		var x = 1;
		var x2 = 1;
		if (!(range.begin === undefined)) {
            x = range.begin.position;
        }
        else 
            if (!(range.BeginInterval === undefined)) {
                x = range.beginInterval.end;
            }
            else {
                x = 1;
            }
			
		if (!(range.end === undefined)) {
            x2 = range.end.position;
        }
        else 
            if (!(range.endInterval === undefined)) {
                x2 = range.endInterval.begin;
            }
            else {
                x2 = 10;
            }
		
		x = Number(x);
		x2 = Number(x2);
		
		var xDraw = x * this._interactionInformation._pxPerAA + featureStart;
        var x2Draw = x2 * this._interactionInformation._pxPerAA + featureStart;
		
		var element = this.drawCertain(xDraw, y, x2Draw - xDraw, height, colour, opacity, symbol, false, false);
		var undet1 = this.drawUndetermined(xDraw, y, height, opacity, symbol);
		var undet2 = this.drawUndetermined(x2Draw - 6, y, height, opacity, symbol);
		if (symbol == "") {
			undet1.attr("stroke", "white");
			undet2.attr("stroke", "white");
		}else{
			undet1.attr({"x": undet1.attr("x") - 3, "font-size": height + 2});
			undet2.attr({"x": undet2.attr("x") + 3, "font-size": height + 2});
		}	
		var eventHandlingElement = this.createEventHandlingElement(element);
		
        this.createTooltip(eventHandlingElement, tooltipText, "?", "?");
		return {
            "x": x,
            "x2": x2,
            "element": element,
            "eventHandlingElement": eventHandlingElement
        };
	};
    
    this.createTooltip = function(element, tooltipText, positionTextBegin, positionTextEnd){
        if (tooltipText != "" && positionTextBegin != "") {
            tooltipText += "\nposition: " + positionTextBegin + " - " + positionTextEnd;
        }
        
        this.getTooltip(element, tooltipText, this._interactionInformation._tooltipColour, this._interactionInformation._tooltipOpacity);
    };
    
    
    // draw a rectangle with the given width and height in the given language
    // if width == 0 make it as wide as one AA
    this.drawCertain = function(x, y, width, height, colour, opacity, symbol, leftCertain, rightCertain){
        if (symbol != "") {
            var element = this._shapeDrawer.getSet();
			
            if (width > 0) {
                var line = this._shapeDrawer.getLine(x, y + height / 2, x + width, y + height / 2);
                line.attr("opacity", opacity);
                line.attr("stroke-width", this._interactionInformation._strokeWidth);
                element.push(line);
				featureSet.push(line);
                if (leftCertain) {
                    var addLine = this._shapeDrawer.getLine(x, y, x, y + height)
                    addLine.attr("opacity", opacity);
					addLine.attr("stroke-width", this._interactionInformation._strokeWidth);
                    element.push(addLine);
					featureSet.push(addLine);
                }
                if (rightCertain) {
                    var addLine = this._shapeDrawer.getLine(x + width, y, x + width, y + height);
                    addLine.attr("opacity", opacity);
					addLine.attr("stroke-width", this._interactionInformation._strokeWidth);
                    element.push(addLine);
					featureSet.push(addLine);
                }
            }
            var s = this.drawSymbol(symbol, x + width / 2, y, height, colour, false, opacity);
			element.push(s);
            return element;
        }
        else {
            if (width == 0) {
                var line = this._shapeDrawer.getLine(x, y, x, y + height);
                line.attr("stroke", colour);
                line.attr("stroke-width", this._interactionInformation._strokeWidth);
                line.attr("opacity", opacity);
                featureSet.push(line);
                return line;
            }
            else {
                var rect = this._shapeDrawer.getRectangle(x, y, width, height);
                rect.attr("stroke", colour);
                rect.attr("fill", colour);
                rect.attr("stroke-width", this._interactionInformation._strokeWidth);
                rect.attr("fill-opacity", opacity);
                featureSet.push(rect);
                return rect;
            }
        }
    };
    
    this.drawSymbol = function(symbol, x, y, height, colour, undetermined, opacity){
        var functionName = "draw" + symbol;
        var element = null;
        if (this[functionName]) {
            element = this[functionName](x, y, height, colour, undetermined, opacity);
        }
        else {
            if (this._interactionInformation.options.developingMode) {
                console.log("function for drawing symbol \"" + symbol + "\" not provided");
            }
            else {
                //TODO
            }
        }
        return element;
    }
    
    // draw a triangle pointing to the right 
    this.drawGreaterThan = function(x, y, y2, colour, opacity, symbol, width){
        var triangle = null;
        if (symbol == "") {
            triangle = this._shapeDrawer.getTriangleRight(x, y, y2);
            triangle.attr("stroke", colour);
            triangle.attr("fill", colour);
        }
        else {
            if (width < 2 || width === undefined) {
                triangle = this._shapeDrawer.getTriangleRight(x + 3, y, y2);
            }
            else {
                triangle = this._shapeDrawer.getTriangleRight(x - 4, y, y2);
            }
        }
        triangle.attr("fill-opacity", opacity);
        triangle.attr("stroke-width", this._interactionInformation._strokeWidth);
        featureSet.push(triangle);
        return triangle;
    };
    
    
    // draw a triangle pointing to the left
    this.drawLessThan = function(x, y, y2, colour, opacity, symbol, width){
        var triangle = null;
        if (symbol == "") {
            triangle = this._shapeDrawer.getTriangleLeft(x, y, y2);
            triangle.attr("stroke", colour);
            triangle.attr("fill", colour);
        }
        else {
			if (width < 2 || width === undefined) {
				triangle = this._shapeDrawer.getTriangleLeft(x - 3, y, y2);
			}
			else {
				triangle = this._shapeDrawer.getTriangleLeft(x + 4, y, y2);
			}
        }
        triangle.attr("fill-opacity", opacity);
        triangle.attr("stroke-width", this._interactionInformation._strokeWidth);
        featureSet.push(triangle);
        return triangle;
    };
    
    // draw two curves to symbolize uncertainty on the right side
    this.drawUnspecifiedRight = function(x, y, height, colour, opacity, symbol, elementWidth){
        var halfHeight = height / 2;
		
		if(elementWidth < 2 && symbol != ""){
			x += 7;
		}
		
        var curveTop = this._shapeDrawer.getCurveToLeft(x, y + 0.1, halfHeight + 0.1);
        var curveBottom = this._shapeDrawer.getCurveToRight(x, (y + halfHeight) + 0.1, halfHeight - 0.1);
        var curve = this._shapeDrawer.getSet();
        curve.push(curveTop);
        curve.push(curveBottom);
        
        if (symbol == "") {
            curveTop.attr("fill", "white");
            curveBottom.attr("fill", colour);
            curve.attr("stroke", colour);
        }
        else {
            curve.push(this._shapeDrawer.getLine(x, y, x + 2, y));
            curve.push(this._shapeDrawer.getLine(x - 2, y + height, x, y + height));
        }
        
        curve.attr("stroke-width", 0.5);
        curve.attr("fill-opacity", opacity);
        
        featureSet.push(curve);
        return curve;
    };
    
    
    // draw two curves to symbolize uncertainty on the left side
    this.drawUnspecifiedLeft = function(x, y, height, colour, opacity, symbol, elementWidth){
        var halfHeight = height / 2;
		
		if(elementWidth < 2 && symbol != ""){
			x -= 6;
		}
		
        var curveTop = this._shapeDrawer.getCurveToLeft(x, y + 0.1, halfHeight);
        var curveBottom = this._shapeDrawer.getCurveToRight(x, (y + halfHeight), halfHeight);
        var curve = this._shapeDrawer.getSet();
        curve.push(curveTop);
        curve.push(curveBottom);
        
        if (symbol == "") {
            curveTop.attr("fill", colour);
            curveBottom.attr("fill", "white");
			curve.attr("stroke", colour);
        }else {
            curve.push(this._shapeDrawer.getLine(x, y, x + 2, y));
            curve.push(this._shapeDrawer.getLine(x - 2, y + height, x, y + height));
        }
        
        curve.attr("stroke-width", 0.5);
        curve.attr("fill-opacity", opacity);
        
        featureSet.push(curve);
        return curve;
    }
    
    // draw a rectangle with the given width and height with a gradient from left to right
    this.drawSpecifiedRight = function(x, y, width, height, colour, rangeColour, opacity, symbol, elementWidth){
        var grad = null;
        
        if (symbol == "") {
			grad = this._shapeDrawer.getRectangle(x, y, width, height);
            grad.attr("fill", "0-" + rangeColour + "-white");
            grad.attr("stroke", colour);
        }
        else {
			var x2 = x + width;
			var y2 = y + height/2;
			grad = this._shapeDrawer.getSet(); 
			
			if(elementWidth < 2){
				x += 4;
			}
			
			var line = this._shapeDrawer.getLine(x, y2, x2, y2);
			line.attr("stroke-dasharray", ". ");
			grad.push(line);
			grad.push(this._shapeDrawer.getLine(x2, y, x2, y + height));
        }
        
        grad.attr("stroke-width", this._interactionInformation._strokeWidth);
        grad.attr("fill-opacity", opacity);
        featureSet.push(grad);
        return grad;
    };
    
    // draw a rectangle with the given width and height with a gradient from right to left
    this.drawSpecifiedLeft = function(x, y, width, height, colour, rangeColour, opacity, symbol, elementWidth){
        var grad = null;
        
        if (symbol == "") {
			grad = this._shapeDrawer.getRectangle(x, y, width, height);
            grad.attr("fill", "0-white-" + rangeColour);
            grad.attr("stroke", colour);
        }
        else {
			var x2 = x + width;
			
			if(elementWidth < 2){
				x2 -= 4;
			}
			
			var y2 = y + height/2;
			grad = this._shapeDrawer.getSet(); 
			var line = this._shapeDrawer.getLine(x, y2, x2, y2);
			line.attr("stroke-dasharray", ". ");
			grad.push(line);
			grad.push(this._shapeDrawer.getLine(x, y, x, y + height));
        }
        
        grad.attr("stroke-width", this._interactionInformation._strokeWidth);
        grad.attr("fill-opacity", opacity);
        
        featureSet.push(grad);
        return grad;
    };
    
    this.drawUndetermined = function(x, y, height, opacity){
        var text = this._shapeDrawer.getText(x, y + height / 2, "?");
        text.attr({
            "text-anchor": "start",
            "fill-opacity": opacity,
            "font-size": height + 2
        });
        return text;
    }
    
    // draw a star with black lines and filled with the given colour at the given position
    this.drawIsotope = function(x, y, height, colour, undetermined, opacity){
        var isotope = null;
        var set = this._shapeDrawer.getSet();
        if (undetermined) {
            isotope = this._shapeDrawer.getStar(x, y - 1, height + 2);
            circle = this._shapeDrawer.getEnclosingCircle(x, y - 0.5, height + 1);
            circle.attr("stroke-width", this._interactionInformation._strokeWidth - 0.2);
            set.push(circle);
        }
        else {
            isotope = this._shapeDrawer.getStar(x, y - 1, height + 2);
        }
        isotope.attr({"fill": colour, "fill-opacity": opacity, 
					"stroke-width": this._interactionInformation._strokeWidth - 0.2});
        set.push(isotope);
        featureSet.push(set);
        return set;
    };
	
	this.drawPTM = function(x, y, height, colour, undetermined, opacity){
		var ptm = this._shapeDrawer.getTriangleTop(x, y, y + height);
		ptm.attr({"opacity": opacity, "fill": colour, "fill-opacity": opacity, "stroke": colour});
		featureSet.push(ptm);
		return ptm;
	};
	
	this.drawLegendSymbol = function(x, y, height, colour, undetermined, opacity){
		var itemHeight = height - 2;
		var rect = this._shapeDrawer.getRectangle(x - itemHeight/2 , y + 1, itemHeight, itemHeight);
		rect.attr({"fill": colour, "stroke": colour, "fill-opacity": opacity, "opacity": opacity});
		return rect;
	}
	
	
	this.createEventHandlingElement = function(element){
		var set = this._shapeDrawer.getSet();
		set.push(element);
		
		var bb = element.getBBox();
		var rect = null;
		
		if(bb.height == 0){
			rect = this._shapeDrawer.getRectangle(bb.x, bb.y - 3, bb.width, 6);
		}else{
			rect = this._shapeDrawer.getRectangle(bb.x, bb.y, bb.width, bb.height);
		}
		
		if(bb.width == 0){
			rect.attr("width", 2);
			rect.attr("x", bb.x -1);
		}
		
		rect.attr("fill-opacity", 0);
		rect.attr("fill", "white");
		rect.attr("opacity", 0);
		set.push(rect);
		return set;
	}
	
	
	this.getTooltip = function(element, tooltipText, colour, opacity){
		if (tooltipText != "") {
			
			var bb = element.getBBox();
			var text = this._shapeDrawer.getText(0, 0, tooltipText);
			var height = text.getBBox().height + 10;
			var width = text.getBBox().width + 10;
			
			var tooltipY = bb.y;
			
			text.attr("y", tooltipY + height/2);
			text.attr("text-anchor", "start");
			text.attr("fill", "white");
			
			var tooltip = this._shapeDrawer.getRectangle(0, tooltipY, width, height);
			tooltip.attr({"stroke": "white",
						  "r": 5,"rx": 5,"ry": 5,
						  "fill": colour,
						  "fill-opacity": opacity});
			
			var tooltipSet = this._shapeDrawer.getSet();
			tooltipSet.push(tooltip);
			tooltipSet.push(text);
			tooltipSet.toBack();
			tooltipSet.hide();

            var elementPosition = $(this._interactionInformation._myElement).offset();

			element.mouseover(function(event){
				tooltip.attr("x", event.clientX - elementPosition.left);
				text.attr("x", event.clientX + 5 - elementPosition.left);
				tooltipSet.toFront();
				tooltipSet.show();
			});
			
			element.mouseout(function(event){
				tooltipSet.toBack();
				tooltipSet.hide();
			});
			
			element.mousemove(function(event){
				tooltip.attr("x", event.clientX - elementPosition.left);
				text.attr("x", event.clientX + 5 - elementPosition.left);
			});
		}
	};
}
