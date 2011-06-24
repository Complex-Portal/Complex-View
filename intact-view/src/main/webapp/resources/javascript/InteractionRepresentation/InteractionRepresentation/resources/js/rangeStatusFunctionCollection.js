// collection of functions for all possible range type - combinations
// all combinations recognised should be listed here, even if function does nothing
RangeStatusFunctionCollection = function(featureDrawer){
	this._featureDrawer = featureDrawer;
	
	// default drawing function for certain begin and end
    this.draw03350335 = function(range, y, height, featureStart, interactorLength, colour, rangeColour, opacity, tooltipText, symbol){
		return this._featureDrawer.drawCertainCertain(range, y, height, featureStart, interactorLength, colour, rangeColour, opacity, tooltipText, symbol);
	}
    
	// default drawing function for certain begin and end > given position
    this.draw03350336 = function(range, y, height, featureStart, interactorLength, colour, rangeColour, opacity, tooltipText, symbol){
		return this._featureDrawer.drawCertainGreaterThan(range, y, height, featureStart, interactorLength, colour, rangeColour, opacity, tooltipText, symbol);
    }
	
	// default drawing function for certain begin and end < given position
	this.draw03350337 = function(range, y, height, featureStart, interactorLength, colour, rangeColour, opacity, tooltipText, symbol){
		return this._featureDrawer.drawCertainLessThan(range, y, height, featureStart, interactorLength, colour, rangeColour, opacity, tooltipText, symbol);
	}
	
	// default drawing function for certain begin and end's position in a certain range
    this.draw03350338 = function(range, y, height, featureStart, interactorLength, colour, rangeColour, opacity, tooltipText, symbol){
		return this._featureDrawer.drawCertainRange(range, y, height, featureStart, interactorLength, colour, rangeColour, opacity, tooltipText, symbol);
	}

	// default drawing function for certain begin and end == end of protein
	this.draw03350334 = function(range, y, height, featureStart, interactorLength, colour, rangeColour, opacity, tooltipText, symbol){
		return this._featureDrawer.drawCertainCertain(this.getCTerminalRange(range, interactorLength), y, height, featureStart, interactorLength, colour, rangeColour, opacity, tooltipText, symbol); 
	}
	
	// default drawing function for certain begin and end == < end of protein
	this.draw03351039 = function(range, y, height, featureStart, interactorLength, colour, rangeColour, opacity, tooltipText, symbol){
		return this._featureDrawer.drawCertainLessThan(this.getCTerminalRange(range, interactorLength), y, height, featureStart, interactorLength, colour, rangeColour, opacity, tooltipText, symbol);
	}
    
	// default drawing function for certain begin und end's position unknown
	this.draw03350339 = function(range, y, height, featureStart, interactorLength, colour, rangeColour, opacity, tooltipText, symbol){
		var range2 = new Object();
		$.extend(true, range2, range);
		range2["end"]["position"] = range["begin"]["position"];
		return this._featureDrawer.drawCertainGreaterThan(range2, y, height, featureStart, interactorLength, colour, rangeColour, opacity, tooltipText, symbol);
	}
	
	this.draw03360335 = function(range, y, height, featureStart, interactorLength, colour, rangeColour, opacity, tooltipText, symbol){
		return this._featureDrawer.drawGreaterThanCertain(range, y, height, featureStart, interactorLength, colour, rangeColour, opacity, tooltipText, symbol);
	}
	
	this.draw03360336 = function(range, y, height, featureStart, interactorLength, colour, rangeColour, opacity, tooltipText, symbol){
		return this._featureDrawer.drawGreaterThanGreaterThan(range, y, height, featureStart, interactorLength, colour, rangeColour, opacity, tooltipText, symbol);
	}
	
	this.draw03360337 = function(range, y, height, featureStart, interactorLength, colour, rangeColour, opacity, tooltipText, symbol){
		return this._featureDrawer.drawGreaterThanLessThan(range, y, height, featureStart, interactorLength, colour, rangeColour, opacity, tooltipText, symbol);
	}
	
	this.draw03360338 = function(range, y, height, featureStart, interactorLength, colour, rangeColour, opacity, tooltipText, symbol){
		return this._featureDrawer.drawGreaterThanRange(range, y, height, featureStart, interactorLength, colour, rangeColour, opacity, tooltipText, symbol);
	}
	
	this.draw03360334 = function(range, y, height, featureStart, interactorLength, colour, rangeColour, opacity, tooltipText, symbol){
		return this._featureDrawer.drawGreaterThanCertain(this.getCTerminalRange(range, interactorLength), y, height, featureStart, interactorLength, colour, rangeColour, opacity, tooltipText, symbol);
	}
	
	this.draw03361039 = function(range, y, height, featureStart, interactorLength, colour, rangeColour, opacity, tooltipText, symbol){
		return this._featureDrawer.drawGreaterThanLessThan(this.getCTerminalRange(range, interactorLength), y, height, featureStart, interactorLength, colour, rangeColour, opacity, tooltipText, symbol);
	}
	
	this.draw03360339 = function(range, y, height, featureStart, interactorLength, colour, rangeColour, opacity, tooltipText, symbol){
		var range2 = new Object();
		$.extend(true, range2, range);
		range2["end"]["position"] = range.begin.position;
		return this._featureDrawer.drawCertainGreaterThan(range2, y, height, featureStart, interactorLength, colour, rangeColour, opacity, tooltipText, symbol);
	}
	
	// default drawing function for begin < given position and certain end
    this.draw03370335 = function(range, y, height, featureStart, interactorLength, colour, rangeColour, opacity, tooltipText, symbol){
		return this._featureDrawer.drawLessThanCertain(range, y, height, featureStart, interactorLength, colour, rangeColour, opacity, tooltipText, symbol);
    }
    
	// default drawing function for begin < given position and end > given position
	this.draw03370336 = function(range, y, height, featureStart, interactorLength, colour, rangeColour, opacity, tooltipText, symbol){
		return this._featureDrawer.drawLessThanGreaterThan(range, y, height, featureStart, interactorLength, colour, rangeColour, opacity, tooltipText, symbol);
    }
    
	// default drawing function for begin < given position and end < given position
	this.draw03370337 = function(range, y, height, featureStart, interactorLength, colour, rangeColour, opacity, tooltipText, symbol){
		return this._featureDrawer.drawLessThanLessThan(range, y, height, featureStart, interactorLength, colour, rangeColour, opacity, tooltipText, symbol);
	}
	
	// default drawing function for begin < given position and end's position in a certain range
	this.draw03370338 = function(range, y, height, featureStart, interactorLength, colour, rangeColour, opacity, tooltipText, symbol){
		return this._featureDrawer.drawLessThanRange(range, y, height, featureStart, interactorLength, colour, rangeColour, opacity, tooltipText, symbol);
	}
	
	// default drawing function for begin < given position and end == end of protein
	this.draw03370334 = function(range, y, height, featureStart, interactorLength, colour, rangeColour, opacity, tooltipText, symbol){
		return this._featureDrawer.drawLessThanCertain(this.getCTerminalRange(range, interactorLength), y, height, featureStart, interactorLength, colour, rangeColour, opacity, tooltipText, symbol);
	}
	
	// default drawing function for begin < given position and end < end of protein
	this.draw03371039 = function(range, y, height, featureStart, interactorLength, colour, rangeColour, opacity, tooltipText, symbol){
		return this._featureDrawer.drawLessThanLessThan(this.getCTerminalRange(range, interactorLength), y, height, featureStart, interactorLength, colour, rangeColour, opacity, tooltipText, symbol);
	}

	// default drawing function for begin < given position and end's position unknown
	this.draw03370339 = function(range, y, height, featureStart, interactorLength, colour, rangeColour, opacity, tooltipText, symbol){
		return this._featureDrawer.drawLessThanUndetermined(range, y, height, featureStart, interactorLength, colour, rangeColour, opacity, tooltipText, symbol);
	}
	
	// default drawing function for begin's position in a certain range and certain end
	this.draw03380335 = function(range, y, height, featureStart, interactorLength, colour, rangeColour, opacity, tooltipText, symbol){
		return this._featureDrawer.drawRangeCertain(range, y, height, featureStart, interactorLength, colour, rangeColour, opacity, tooltipText, symbol);
	};
	
	// default drawing function for begin's position in a certain range and end > given position
	this.draw03380336 = function(range, y, height, featureStart, interactorLength, colour, rangeColour, opacity, tooltipText, symbol){
		return this._featureDrawer.drawRangeGreaterThan(range, y, height, featureStart, interactorLength, colour, rangeColour, opacity, tooltipText, symbol);
	};
	
	// default drawing function for begin's position in a certain range and end < given position
	this.draw03380337 = function(range, y, height, featureStart, interactorLength, colour, rangeColour, opacity, tooltipText, symbol){
		return this._featureDrawer.drawRangeLessThan(range, y, height, featureStart, interactorLength, colour, rangeColour, opacity, tooltipText, symbol);
	};
	 
	// default drawing function for begin's and end's position in a certain range
    this.draw03380338 = function(range, y, height, featureStart, interactorLength, colour, rangeColour, opacity, tooltipText, symbol){
		return this._featureDrawer.drawRangeRange(range, y, height, featureStart, interactorLength, colour, rangeColour, opacity, tooltipText, symbol);
	}; 
	
	// default drawing function for begin's position in a certain range and end == end of protein
	this.draw03380334 = function(range, y, height, featureStart, interactorLength, colour, rangeColour, opacity, tooltipText, symbol){
		return this._featureDrawer.drawRangeCertain(this.getCTerminalRange(range, interactorLength), y, height, featureStart, interactorLength, colour, rangeColour, opacity, tooltipText, symbol);
	}

	// default drawing function for begin's position in a certain range and end < end of protein
	this.draw03381039 = function(range, y, height, featureStart, interactorLength, colour, rangeColour, opacity, tooltipText, symbol){
		return this._featureDrawer.drawRangeLessThan(this.getCTerminalRange(range, interactorLength), y, height, featureStart, interactorLength, colour, rangeColour, opacity, tooltipText, symbol);
	}
	
	// default drawing function for begins's position in a certain range and the end is undefined
	this.draw03380339 = function(range, y, height, featureStart, interactorLength, colour, rangeColour, opacity, tooltipText, symbol){
		var range2 = new Object();
		$.extend(true, range2, range);
		if (range2.begin === undefined) {
			range2["end"]["position"] = range2.beginInterval.end;
		}
		else {
			range2["end"]["position"] = range2.begin.position + 5;
		}
		return this._featureDrawer.drawRangeGreaterThan(range2, y, height, featureStart, interactorLength, colour, rangeColour, opacity, tooltipText, symbol);
	}
	
	this.draw03400335 = function(range, y, height, featureStart, interactorLength, colour, rangeColour, opacity, tooltipText, symbol){
		return this._featureDrawer.drawCertainCertain(this.getNTerminalRange(range), y, height, featureStart, interactorLength, colour, rangeColour, opacity, tooltipText, symbol);
	}
	
	this.draw03400336 = function(range, y, height, featureStart, interactorLength, colour, rangeColour, opacity, tooltipText, symbol){
		return this._featureDrawer.drawCertainGreaterThan(this.getNTerminalRange(range), y, height, featureStart, interactorLength, colour, rangeColour, opacity, tooltipText, symbol);
	}
	
	this.draw03400337 = function(range, y, height, featureStart, interactorLength, colour, rangeColour, opacity, tooltipText, symbol){
		return this._featureDrawer.drawCertainLessThan(this.getNTerminalRange(range), y, height, featureStart, interactorLength, colour, rangeColour, opacity, tooltipText, symbol);
	}
	
	this.draw03400338 = function(range, y, height, featureStart, interactorLength, colour, rangeColour, opacity, tooltipText, symbol){
		return this._featureDrawer.drawCertainRange(this.getNTerminalRange(range), y, height, featureStart, interactorLength, colour, rangeColour, opacity, tooltipText, symbol);
	}
	
	this.draw03400334 = function(range, y, height, featureStart, interactorLength, colour, rangeColour, opacity, tooltipText, symbol){
		var range2 = this.getNTerminalRange(range);
		range2["end"]["position"] = interactorLength;
		return this._featureDrawer.drawCertainCertain(range2, y, height, featureStart, interactorLength, colour, rangeColour, opacity, tooltipText, symbol);
	}
	
	this.draw03401039 = function(range, y, height, featureStart, interactorLength, colour, rangeColour, opacity, tooltipText, symbol){
		var range2 = this.getNTerminalRange(range);
		range2["end"]["position"] = interactorLength;
		return this._featureDrawer.drawCertainLessThan(range2, y, height, featureStart, interactorLength, colour, rangeColour, opacity, tooltipText, symbol);
	}
	
	this.draw03401040 = function(range, y, height, featureStart, interactorLength, colour, rangeColour, opacity, tooltipText, symbol){
		return this._featureDrawer.drawNTerminalRange(range, y, height, featureStart, interactorLength, colour, rangeColour, opacity, tooltipText, symbol);
	}
	
	this.draw03400339 = function(range, y, height, featureStart, interactorLength, colour, rangeColour, opacity, tooltipText, symbol){
		return this._featureDrawer.drawNTerminalRange(range, y, height, featureStart, interactorLength, colour, rangeColour, opacity, tooltipText, symbol);
	}
	
	// default drawing function for n-terminal position
	this.draw03400340 = function(range, y, height, featureStart, interactorLength, colour, rangeColour, opacity, tooltipText, symbol){
		return this._featureDrawer.drawNTerminalPosition(range, y, height, featureStart, interactorLength, colour, rangeColour, opacity, tooltipText, symbol);
	}
	
	this.draw10400335 = function(range, y, height, featureStart, interactorLength, colour, rangeColour, opacity, tooltipText, symbol){
		return this._featureDrawer.drawGreaterThanCertain(this.getNTerminalRange(range), y, height, featureStart, interactorLength, colour, rangeColour, opacity, tooltipText, symbol);
	}
	
	this.draw10400336 = function(range, y, height, featureStart, interactorLength, colour, rangeColour, opacity, tooltipText, symbol){
		return this._featureDrawer.drawGreaterThanGreaterThan(this.getNTerminalRange(range), y, height, featureStart, interactorLength, colour, rangeColour, opacity, tooltipText, symbol);
	}
	
	this.draw10400337 = function(range, y, height, featureStart, interactorLength, colour, rangeColour, opacity, tooltipText, symbol){
		return this._featureDrawer.drawGreaterThanLessThan(this.getNTerminalRange(range), y, height, featureStart, interactorLength, colour, rangeColour, opacity, tooltipText, symbol);
	}
	
	this.draw10400338 = function(range, y, height, featureStart, interactorLength, colour, rangeColour, opacity, tooltipText, symbol){
		return this._featureDrawer.drawGreaterThanRange(this.getNTerminalRange(range), y, height, featureStart, interactorLength, colour, rangeColour, opacity, tooltipText, symbol);
	}
	
	this.draw10400334 = function(range, y, height, featureStart, interactorLength, colour, rangeColour, opacity, tooltipText, symbol){
		var range2 = this.getNTerminalRange(range);
		range2["end"]["position"] = interactorLength;
		return this._featureDrawer.drawGreaterThanCertain(range2, y, height, featureStart, interactorLength, colour, rangeColour, opacity, tooltipText, symbol);
	}
	
	this.draw10401039 = function(range, y, height, featureStart, interactorLength, colour, rangeColour, opacity, tooltipText, symbol){
		var range2 = this.getNTerminalRange(range);
		range2["end"]["position"] = interactorLength;
		return this._featureDrawer.drawGreaterThanLessThan(range2, y, height, featureStart, interactorLength, colour, rangeColour, opacity, tooltipText, symbol);
	}
	
	// default drawing function for n-terminal range
	this.draw10401040 = function(range, y, height, featureStart, interactorLength, colour, rangeColour, opacity, tooltipText, symbol){
		return this._featureDrawer.drawNTerminalRange(range, y, height, featureStart, interactorLength, colour, rangeColour, opacity, tooltipText, symbol);
	}
	
	// default drawing functin for c-terminal position
	this.draw03340334 = function(range, y, height, featureStart, interactorLength, colour, rangeColour, opacity, tooltipText, symbol){
		return this._featureDrawer.drawCTerminalPosition(range, y, height, featureStart, interactorLength, colour, rangeColour, opacity, tooltipText, symbol);
	}
	
	// default drawing function for c-terminal range
	this.draw10391039 = function(range, y, height, featureStart, interactorLength, colour, rangeColour, opacity, tooltipText, symbol){
		return this._featureDrawer.drawCTerminalRange(range, y, height, featureStart, interactorLength, colour, rangeColour, opacity, tooltipText, symbol);
	}
    
	// default drawing function for begin in c-terminal range and end at c-terminal position
	this.draw10390334 = function(range, y, height, featureStart, interactorLength, colour, rangeColour, opacity, tooltipText, symbol){
		return this._featureDrawer.drawCTerminalRange(range, y, height, featureStart, interactorLength, colour, rangeColour, opacity, tooltipText, symbol);
	}
	
	this.draw03390335 = function(range, y, height, featureStart, interactorLength, colour, rangeColour, opacity, tooltipText, symbol){
		var range2 = new Object();
		$.extend(true, range2, range);
		range2["begin"]["position"] = range2.end.position;
		return this._featureDrawer.drawLessThanCertain(range2, y, height, featureStart, interactorLength, colour, rangeColour, opacity, tooltipText, symbol);
	}
	
	this.draw03390336 = function(range, y, height, featureStart, interactorLength, colour, rangeColour, opacity, tooltipText, symbol){
		return this._featureDrawer.drawUndeterminedGreaterThan(range, y, height, featureStart, interactorLength, colour, rangeColour, opacity, tooltipText, symbol);
	}
	
	this.draw03390337 = function(range, y, height, featureStart, interactorLength, colour, rangeColour, opacity, tooltipText, symbol){
		var range2 = new Object();
		$.extend(true, range2, range);
		range2["begin"]["position"] = range2.end.position;
		return this._featureDrawer.drawLessThanCertain(range2, y, height, featureStart, interactorLength, colour, rangeColour, opacity, tooltipText, symbol);
	}
	
	this.draw03390338 = function(range, y, height, featureStart, interactorLength, colour, rangeColour, opacity, tooltipText, symbol){
		var range2 = new Object();
		$.extend(true, range2, range);
		if (range2.end === undefined) {
			range2["begin"]["position"] = range2.endInterval.begin;
		}
		else {
			range2["begin"]["position"] = range2.end.position - 2;
		}
		return this._featureDrawer.drawLessThanRange(range2, y, height, featureStart, interactorLength, colour, rangeColour, opacity, tooltipText, symbol);
	}
	
	this.draw03390334 = function(range, y, height, featureStart, interactorLength, colour, rangeColour, opacity, tooltipText, symbol){
		return this._featureDrawer.drawCTerminalRange(range, y, height, featureStart, interactorLength, colour, rangeColour, opacity, tooltipText, symbol);
	}
	
	// default drawing function for undeterminded position
	this.draw03390339 = function(range, y, height, featureStart, interactorLength, colour, rangeColour, opacity, tooltipText, symbol){
		/* do nothing */
		// functions doing nothing have to return an empty object
		return {};
	};
	
	// tags' drawing function for undetermined position
	this.tag03390339 = function(range, y, height, featureStart, interactorLength, colour, rangeColour, opacity, tooltipText, symbol){
		return this._featureDrawer.drawNonPositionalTag(range, y, height, featureStart, interactorLength, colour, rangeColour, opacity, tooltipText, symbol);
	};
	
	// isotopes' drawing function for undetermined position
	this.isotope03390339 = function(range, y, height, featureStart, interactorLength, colour, rangeColour, opacity, tooltipText, symbol){
		return this._featureDrawer.drawNonPositionalIsotope(range, y, height, featureStart, interactorLength, colour, rangeColour, opacity, tooltipText, symbol);
	};
	
	this.getNTerminalRange = function(range){
		var range2 = new Object();
		$.extend(true, range2, range);
		range2["begin"]["position"] = 0;
		return range2;
	}
	
	this.getCTerminalRange = function(range, interactorLength){
		var range2 = new Object();
		$.extend(true, range2, range);
		range2["end"]["position"] = interactorLength;
		return range2;
	}
}
