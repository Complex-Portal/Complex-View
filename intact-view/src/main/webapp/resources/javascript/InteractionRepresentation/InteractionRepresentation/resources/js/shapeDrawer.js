ShapeDrawer = function(paper){
	
	this._paper = paper;
	
	// draw a triangle pointing to the right
	this.getTriangleRight = function(x, yTop, yBottom){
        var xMiddle = x + (yBottom - yTop) * 0.75;
        var yMiddle = yTop + (yBottom - yTop) / 2;
        var triangle = this._paper.path("M" + x + "," + yTop + "L" + xMiddle + "," + yMiddle + " " + x + "," + yBottom);
        return triangle;
    };
    
	// draw a triangle pointing to the left
    this.getTriangleLeft = function(x, yTop, yBottom){
        var xMiddle = x - (yBottom - yTop) * 0.75;
        var yMiddle = yTop + (yBottom - yTop) / 2;
        var triangle = this._paper.path("M" + x + "," + yTop + "L" + xMiddle + "," + yMiddle + " " + x + "," + yBottom);
        return triangle;
    };
    
	// draw a triangle pointing to the bottom
    this.getTriangleBottom = function(x, yTop, yBottom){
		var halfHeight = (yBottom - yTop)/2;
        var xLeft = x - halfHeight;
        var xRight = x + halfHeight;
        var triangle = this._paper.path("M" + xLeft + "," + yTop + "L" + x + "," + yBottom + " " + xRight + "," + yTop + "Z");
        return triangle;
    };
	
	// draw a triangle pointing to the bottom
    this.getTriangleTop = function(x, yTop, yBottom){
		var halfHeight = (yBottom - yTop)/2;
        var xLeft = x - halfHeight;
        var xRight = x + halfHeight;
        var triangle = this._paper.path("M" + xLeft + "," + yBottom + "L" + x + "," + yTop + " " + xRight + "," + yBottom + "Z");
        return triangle;
    };
	
	// draw a curve with the curved part to the right
	this.getCurveToRight = function(x, y, distance){
		// if x = y = 0, distance = 5
		// M 0,0 C5,0 5,5 0,5
		var xRight = x + 2*distance/3;
		var yMiddle = y + distance;
		var path = this._paper.path("M" + x + "," + y + 
								    "C" + xRight + "," + y + " " + xRight + "," + yMiddle + " "+ x + "," + yMiddle);
		return path;
	}
	
	// draw a curve with the curved part to the left
	this.getCurveToLeft = function(x, y, distance){
		// if x = y = 0, distance = 5
		// M 0,0 C-5,0 -5,5 0,5
		var xLeft = x - 2*distance/3;
		var yMiddle = y + distance;
		var path = this._paper.path("M" + x + "," + y + 
								    "C" + xLeft + "," + y + " " + xLeft + "," + yMiddle + " "+ x + "," + yMiddle);
		return path;
	}
	
	// draw a quadrangle with horizontal bottom and top line (coordinates should contain x, x2, y, height)
	this.getQuadrangle = function(xTop, yTop, widthTop, xBottom, yBottom, widthBottom){
        
		var xTop2 = xTop + widthTop;
		var xBottom2 = xBottom + widthBottom;
		
		if(widthTop > 5){
			yTop += 1;
		}
		
		if(widthBottom > 5){
			yBottom -= 1;
		}
		
        var quad = this._paper.path("M" + xTop + "," + yTop +
        										 "L" + xBottom + "," + yBottom +
        										 "," + xBottom2 + "," + yBottom + 
												 "," + xTop2 + "," + yTop + "Z");
        
        /*var quad = this._paper.path("M" + xTop + "," + yTop +
         "L" + xBottom + "," + yBottom +
         "M" + xTop2 + "," + yTop +
         "L" + xBottom2 + "," + yBottom);*/
        
    	return quad;
    };
	
	// draw a line between to points
	this.getLine = function(x, y, x2, y2){
		var line = this._paper.path("M" + x + "," + y + "L" + x2 + "," + y2);
		return line;
	};
	
	// write text
	this.getText = function(x, y, text){
		var text = this._paper.text(x, y, text);
		text.attr("text-anchor", "start");
		return text;
	};
	
	// draw a circle 
	this.getCircle = function(x, y, diameter){
		var radius = diameter/2;
		var xMiddle = x + radius;
		var yMiddle = y + radius;
		var circle = this._paper.circle(xMiddle, yMiddle, radius);
		return circle;
	};
	
	
	// draw rectangle with the top left corner in the given point with the given width and height
	this.getRectangle = function (x, y, width, height){
		var rectangle = this._paper.rect(x, y, width, height);
		return rectangle;
	};
	
	
	// draw a horizontal line with a vertical line at each side of the line
	this.getRangeLine = function(x, x2, y, height){
		var yMiddle = y + height/2;
		var yBottom = y + height;
		var line = this._paper.path("M" + x + "," + y + "L" + x + "," + yBottom + 
									"M" + x + "," + yMiddle + "L" + x2 + "," + yMiddle +
									"M" + x2 + "," + y + "L" + x2 + "," + yBottom);
		return line;
	};
	
		
	this.getStar = function(x, y, height){
		//        E
		//       / \
		//   _  /   \  _
		// _/  F     D  \_
		//G_             _C
		//  \_ H     B _/
		//      \   /
		//       \ /
		//        A
		
		var unit = height/6;
		var halfHeight = height/2;
		
		var xA = x;
		var yA = y + height;
		
		var xB = x + unit;
		var yB = y + 4*unit;
		
		var xC = x + halfHeight;
		var yC = y + halfHeight;
		
		var xD = xB;
		var yD = y + 2*unit;
		
		var xE = x;
		var yE = y;
		
		var xF = x - unit;
		var yF = yD;
		
		var xG = x - halfHeight;
		var yG = yC;
		
		var xH = xF;
		var yH = yB;
		
		var star = this._paper.path("M" + xA + "," + yA + "L" + xB + "," + yB+ "L" + xC + "," + yC + "L" + xD + "," + yD 
				   + "L" + xE + "," + yE + "L" + xF + "," + yF + "L" + xG + "," + yG + "L" + xH + "," + yH + "Z");
		return star;
	};
	
	
	// get circle that encloses elements (manual fine tuning of parameters is required)  
	this.getEnclosingCircle = function(x, y, height){
		var radius = height/2 + 1;
		var xCircle = x;
		var yCircle = y + radius - 1;
		
		var circle = this._paper.circle(xCircle, yCircle, radius);
		
		return circle;
	};
	
	this.getSet = function(){
		return this._paper.set();
	};
	
	this.getStripes = function(element, beginX, endX){
		var bb = element.getBBox();
		var y2 = bb.y + bb.height;
		var path = "";
		
		beginX += bb.x;
		endX += bb.x;
		
		for(var x = beginX; x < endX; x += bb.height){
			var y = bb.y;
			var x2 = x + bb.height;
			
			if(x < endX && x2 > endX){
				y = y2 - (endX - x);
				x2 = endX;   
			}
			
			if ((x >= beginX) || (x <= endX)) {
				path += "M" + x + "," + y2 + "L" + x2 + "," + y;
			}
		}
		return this._paper.path(path);
	};
}
