
		function annotationOverlaping(){
			this.annots = new Object();
			this.tracks = new Array();
			this.tracks.push(new Object());
		}

		annotationOverlaping.prototype.setAnnot = function(annotObjectArray){
			this.annots = annotObjectArray;
		}

		annotationOverlaping.prototype.run = function(){
			var trackNumber = 0;
			var annot = new Array();
			for(annotId in this.annots){
				annot = this.getRoundAnnotation(this.annots[annotId]);
				var message = annotationOverlaping.prototype.checkAnnotation(annot);
				if (message.length == 0) {
					trackNumber = this.sortAnnotInOneTrack(annot, annotId);
					//Here we could draw anotations per track. Otherwise we could gather the track Array at the end of the run function.
				} else {
					console.log("Coordinates error: " + message);
				}
			}
		}
		
		annotationOverlaping.prototype.getRoundAnnotation = function(annotArray){
			var newAnnotArray = new Array();
			newAnnotArray[0] = Math.round(annotArray[0]);
			newAnnotArray[1] = Math.round(annotArray[1]);
			return newAnnotArray;
		}
		
		annotationOverlaping.prototype.sortAnnotInOneTrack = function(annot, annotId){
			var overlaping = false;
			var trackNumber = 0;
			tracks:
			for (var i = 0; i < this.tracks.length; i++) {
				overlaping = this.isOverlaping(annot, this.tracks[i]);
				if (overlaping == false) {
					this.tracks[i][annotId] = annot;
					trackNumber = i;
					break tracks;
				} else {
					if(this.tracks[i + 1]==undefined){
					//if (YAHOO.lang.isUndefined(this.tracks[i + 1])) {
						this.tracks.push(new Object());
					}
				}
			}
			return trackNumber;
		}
		
		annotationOverlaping.prototype.checkAnnotation = function(annot){
			var message = "";
			//if(YAHOO.lang.isNumber(annot[0])==false || YAHOO.lang.isNumber(annot[1])==false){
			//	message = "Annotation start or end is not a number";
			//} else if(annot[0] >= annot[1]){
			if(annot[0] >= annot[1]){
				message = "Annotation start can not be higer or equal to annotation end";
			} else {
				message = "";
			}
			return message;
		}
		
		annotationOverlaping.prototype.isOverlaping = function(annot, track){
			var overlaping = false;
			//ANNOT INSIDE TRACKS (k)
			trackAnnot:
			for(k in track){
				// TRACK ANNOT START (0), TRACK ANNOT STOP (1)
				if (annot[0] < track[k][0] && annot[1] < track[k][0]) {
					overlaping = false;
				} else if (annot[0] >= track[k][1] && annot[1] > track[k][1]) {
					overlaping = false;
				} else {
					overlaping = true;
					break trackAnnot;
				}
			}
			return overlaping;
		}
		
		annotationOverlaping.prototype.getResultInFirebug = function(){
			for (var i = 0; i < this.tracks.length; i++) {
				console.log("track: " + i);
				for(annotId in this.tracks[i]){
					console.log(annotId + ":" + this.tracks[i][annotId][0] + "," + this.tracks[i][annotId][1]);
				}
			}
		}