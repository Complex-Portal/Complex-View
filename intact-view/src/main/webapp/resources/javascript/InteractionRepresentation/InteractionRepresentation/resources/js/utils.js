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
    
    // -------- color conversions --------
    
    this.HSLtoRGB = function(HSL){
        var c2;
		var RGB = [];
        HSL[2] /= 100;
        HSL[1] /= 100;
        if (HSL[2] <= 0.5) 
            c2 = HSL[2] * (1 + HSL[1]);
        else 
            c2 = HSL[2] + HSL[1] - (HSL[2] * HSL[1]);
        var c1 = 2 * HSL[2] - c2;
        if (HSL[1] == 0) {
            RGB[0] = HSL[2];
            RGB[1] = HSL[2];
            RGB[2] = HSL[2];
        }
        else {
            RGB[0] = this.findRGB(c1, c2, HSL[0] + 120);
            RGB[1] = this.findRGB(c1, c2, HSL[0]);
            RGB[2] = this.findRGB(c1, c2, HSL[0] - 120);
        }
		
		for(var i = 0; i < RGB.length; i++){
			RGB[i] = Math.round(RGB[i] * 255);
		}
		
		return RGB;
    }
    
    this.findRGB = function(c1, c2, hue){
        if (hue > 360) 
            hue = hue - 360;
        if (hue < 0) 
            hue = hue + 360;
        if (hue < 60) 
            return (c1 + (c2 - c1) * hue / 60);
        else 
            if (hue < 180) 
                return (c2);
            else 
                if (hue < 240) 
                    return (c1 + (c2 - c1) * (240 - hue) / 60);
                else 
                    return (c1);
    }
    
    this.RGBtoHSL = function(RGB){
		for(var i = 0; i < RGB.length; i++){
			RGB[i] = (RGB[i] / 51) * 0.2;
		}

        var min = Math.min(RGB[0], RGB[1], RGB[2]);
        var max = Math.max(RGB[0], RGB[1], RGB[2]);
		var HSL = [];
		        
		HSL[2] = (max + min) / 2;
        if (max == min) {
            HSL[0] = 0;
            HSL[1] = 0;
        }
        else {
            if (HSL[2] < 0.5) {
				HSL[1] = (max - min) / (max + min);
			}
            if (HSL[2] >= 0.5) {
				HSL[1] = (max - min) / (2 - max - min);
			}
            if (RGB[0] == max) {
				HSL[0] = (RGB[1] - RGB[2]) / (max - min);
			}
            if (RGB[1] == max){ 
                HSL[0] = 2 + (RGB[2] - RGB[0]) / (max - min);
			}
            if (RGB[2] == max) {
				HSL[0] = 4 + ((RGB[0] - RGB[1]) / (max - min));
			}
        }
        HSL[0] = Math.round(HSL[0] * 60);
        if (HSL[0] < 0) 
            HSL[0] += 360;
        if (HSL[0] >= 360) 
            HSL[0] -= 360;
        HSL[1] = Math.round(HSL[1] * 100);
        HSL[2] = Math.round(HSL[2] * 100);
		return HSL;
    };
    
	this.RGBtoHEX = function(RGB){
		var hexString = "#";
		$(RGB).each(function(){
			hexString += ("0" + parseInt(this).toString(16)).slice(-2);
		});
		return hexString;
	};
	
	this.HEXtoRGB = function(hexString){
		if(hexString.charAt(0) == "#"){
			hexString = hexString.substring(1, 7);
		}
		
		var RGB = [];
		var j = 0;
		for(var i = 0; i < hexString.length / 2; i++){
			RGB[i] = parseInt(hexString.substring(j, j+2), 16);
			j += 2;
		}
		return RGB;
	};
	
	this.HEXtoHSL = function(hexString){
		return this.RGBtoHSL(this.HEXtoRGB(hexString));
	};
	
	this.HSLtoHEX = function(HSL){
		return this.RGBtoHEX(this.HSLtoRGB(HSL));
	};
};
