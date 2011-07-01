// draws all participants, managing their features, and the legend
// draws everything but the features themselves
ParticipantDrawer = function(interactionInformation){

    this._interactionInformation = interactionInformation;
    
    this._shapeDrawer = new ShapeDrawer(this._interactionInformation._paper);
    
    this._interactorSet = this._shapeDrawer.getSet();
    this._featureSet = this._shapeDrawer.getSet();
    
    this._featureDrawer = new FeatureDrawer(interactionInformation, this._shapeDrawer, this._featureSet);
    this._rangeStatusFunctionCollection = new RangeStatusFunctionCollection(this._featureDrawer);
    
    this._calledFunctions = new Object();
    this._multipleRangeKeyword = "multiple";
    
    this._interactionSet = this._shapeDrawer.getSet();
    this._interactions = new Array();
    this._hidden = false;
    this._saveColours = new Array();
    this._selectedInteraction = {"connections": null, "elements": null};
	this._connectedElements = new Object();
    
    // draw all participants of all interactions in the given data
    // and their features and connect linked features
    this.drawInteractors = function(){
        var self = this;
        var y = this._interactionInformation._initY;
        var explanationText = self._shapeDrawer.getText(self._interactionInformation._proteinX, y - 10, 
		"To display a single interaction region please click on it.\n" +
        "Click again to display all interactions.");
        explanationText.hide();
        
        $(self._interactionInformation._interactionInformation.interactions).each(function(){
        
            // if there is just one participant (no array), just draw it
            if (this.participantList.participant.length === undefined) {
                var curFeatureTrackObject = self._interactionInformation._featureTracksPerParticipant[0];
                var participant = this.participantList.participant;
                y = self.drawInteractor(participant, curFeatureTrackObject, y);
            }
            else { // if there are more than one participants (an array of participants), draw one after the other
                for (var i = 0; i < this.participantList.participant.length; i++) {
                    var curFeatureTrackObject = self._interactionInformation._featureTracksPerParticipant[i];
                    var participant = this.participantList.participant[i];
                    y = self.drawInteractor(participant, curFeatureTrackObject, y);
                }
            }

            self._featureSet.insertBefore(self._interactorSet);
            
            // connect linked features
            if (!(this.inferredInteractionList === undefined)) {
				var error = false;
                $(this.inferredInteractionList.inferredInteraction).each(function(){
                    error = self.drawInteraction(this.participant) || error;
                });
				
				if(error){
					var errorText = self._shapeDrawer.getText(self._interactionInformation._proteinX, 
					y + 10, 
					"Not all information on linked features could be represented.")
				}
				
				for(var key in self._connectedElements){
					self.handleClickForConnectionSet(self._connectedElements[key]);
				}
				
                self.arrangeInteractions();
                explanationText.show();
            }
        });

        this._interactionSet.insertAfter(this._featureSet);

        this.drawLegend(10, y + this._interactionInformation._legendDistanceToImage);
    };
    
    
    // draw a single interactor and it's features
    this.drawInteractor = function(participant, curFeatureTrackObject, y){
        var self = this;
        var curTracks = null;
        var height = 0;
        var interactorElement = null;
		
        y = y + self._interactionInformation._proteinGap;
        
        var interactorRef = curFeatureTrackObject.interactorRef;
        
        var interactor = null;
        
        // find right interactor
        if (interactorRef === undefined) {
        
            // interactor is described directly in the participant-object or interactionRef is specified 
            interactor = participant.interactor;
            if (interactor === undefined) {
                // interactionRef is specified
                // do something
            }
        }
        else {// information about the interactor can be found in the extra "interactors"-array
            $(self._interactionInformation._interactionInformation.interactors).each(function(){
                if (this.id == interactorRef) {
                    interactor = this;
                }
            });
        }
        
        // extract length of interactor
        var length = 0;
        var drawLength = 0;
        var lengthText = "";
        var participantName = "";
        
        if (interactor === undefined) {
            // if there is no additional information to the features, length = maximum protein length
            // and no name for the participant is provided 
            drawLength = self._interactionInformation._proteinWidth;
            length = drawLength / self._interactionInformation._pxPerAA;
            lengthText = "?";
            participantName = "";
        }
        else {
            // if the sequence is not provided the length = maximum protein length
            if (interactor.sequence === undefined) {
                drawLength = self._interactionInformation._proteinWidth;
                length = drawLength / self._interactionInformation._pxPerAA;
                lengthText = "?";
            }
            else {
                length = interactor.sequence.length;
                drawLength = length * self._interactionInformation._pxPerAA;
                lengthText = length;
				length -= 1;
            }
            
			var uniProtId = "";
			var intactId = "";
			var parentIds = new Array();
			if((interactor.xref.primaryRef.refType == "identity") && !(interactor.xref.primaryRef.db == "intact")){
				uniProtId = interactor.xref.primaryRef.id;
			} else if(interactor.xref.primaryRef.refType == "multiple parent"){
				parentIds.push(interactor.xref.primaryRef.id);
			}
			
			if (!(interactor.xref.secondaryRef === undefined)) {
				for (var i = 0; i < interactor.xref.secondaryRef.length; i++) {
					if (uniProtId == "" && interactor.xref.secondaryRef[i].refType == "identity" &&
					!(interactor.xref.secondaryRef[i].db == "intact")) {
						uniProtId = interactor.xref.secondaryRef[i].id;
					}else if(interactor.xref.secondaryRef[i].refType == "identity" &&
					(interactor.xref.secondaryRef[i].db == "intact")){
						intactId = interactor.xref.secondaryRef[i].id;
					}
					else 
						if (interactor.xref.secondaryRef[i].refType == "multiple parent") {
							parentIds.push(interactor.xref.secondaryRef[i].id);
						}
				}
			}
			if (parentIds.length == 0) {
				participantName = interactor.names.shortLabel;
			}else{
				participantName = "fusion of ";
				for(var i = 0; i < parentIds.length; i++){
					participantName += parentIds[i];
					if(i < parentIds.length - 1){
						participantName += ", ";
					}
				}
			}
			
            if (participantName === undefined) {
                participantName = interactor.names.fullName;
                if (participantName === undefined) {
                    participantName = uniProtId;
					if(participantName == ""){
						participantName = intactId;
						uniProtId = intactId;
					}
                }
            }
        }
        
        
        // start drawing
        height = self._interactionInformation._positionsOnProtein["top"];
        // draw top features
        if (!(curFeatureTrackObject.annotations === undefined)) {
            curTracks = curFeatureTrackObject.annotations["top"].tracks;
            if (!(curTracks === undefined)) {
                y = self.drawFeatures(participant, curTracks, height, y, length, uniProtId);
                y = y - height; // to draw length text at the same heigth as the last top-feature
            }
            else {
                y = y + this._interactionInformation._featureGap;
            }
        }
        
        // draw length text
        self.drawLengthText(drawLength + self._interactionInformation._proteinX, y, lengthText);
        y = y + height;
        
        height = self._interactionInformation._positionsOnProtein["middle"];
        
        // draw interactors and middle & bottom features if defined
        if (curFeatureTrackObject.annotations === undefined || curFeatureTrackObject.annotations["middle"].tracks === undefined) {
            interactorElement = self.drawProtein(self._interactionInformation._proteinX, y, drawLength, height + self._interactionInformation._featureGap, true);
            self.drawParticipantName(drawLength + self._interactionInformation._proteinX, y + height / 2, participantName);
            y = y + height + this._interactionInformation._featureGap;
        }
        else {
            curTracks = curFeatureTrackObject.annotations["middle"].tracks;
            var tracksLength = 1;
            if (!(curTracks === undefined)) {
                tracksLength = curTracks.length;
            }
            
            var proteinHeight = (height + self._interactionInformation._featureGap) * tracksLength;
            self.drawParticipantName(drawLength + self._interactionInformation._proteinX, y + proteinHeight / 2, participantName);
            
            var yBottom = 0;
            
            // draw features before protein so that "100AA"-lines are visible
            y++;
            yBottom = self.drawFeatures(participant, curTracks, height, y, length, uniProtId);
            y--;
            
            interactorElement = self.drawProtein(self._interactionInformation._proteinX, y, drawLength, proteinHeight, true);
            y = yBottom + 1;
        }
		
		if (!(curFeatureTrackObject.annotations === undefined)) {
            curTracks = curFeatureTrackObject.annotations["bottom"].tracks;
            
            height = self._interactionInformation._positionsOnProtein["bottom"];
            if (!(curTracks === undefined)) {
                y = y + self._interactionInformation._featureGap;
                y = self.drawFeatures(participant, curTracks, height, y, length, uniProtId);
                y = y - self._interactionInformation._featureGap;
            }
        }

        return y;
    };
    
    // draw a list of features at the given height
    this.drawFeatures = function(participant, features, height, curY, interactorLength, interactorId){
        var self = this;
        $(features).each(function(){
            for (var featureKey in this) {
            
                var feature = null;
                // if there is more than one feature they are stored in an array 
                if (participant.featureList.feature.length > 1) {
                    feature = participant.featureList.feature[self._interactionInformation._featurePositions[featureKey]];
                }
                else {// else there is no array
                    feature = participant.featureList.feature;
                }
                
				var featureId = "";
				var intactFeatureId = "";
				
				if(!(feature.xref === undefined)){
					if(feature.xref.primaryRef.refType == "identity"){
						if(!(feature.xref.primaryRef.db == "intact")){
							featureId = feature.xref.primaryRef.id;		
						}else{
							intactFeatureId = feature.xref.primaryRef.id;
						}
					}
					
					if (featureId == "") {
						$(feature.xref.secondaryRef).each(function(){
							if (featureId == "" && this.refType == "identity" && !(this.db == "intact")) {
							
								featureId = this.id;
								
							}
							else 
								if (this.refType == "identity" && this.db == "intact") {
									intactFeatureId = this.id;
								}
						});
					}
				}
				
				if(featureId == ""){
					featureId = intactFeatureId;
				}
				
				var featureTypeId = feature.featureType.xref.primaryRef.id;
				
                // find the type of the current feature by looking for the featuretypeId in all arrays of id's
                for (var categoryKey in self._interactionInformation._typeCategories) {
                    var curFeatureType = self._interactionInformation._typeCategories[categoryKey];
                    var typeIdentifierIndex = null;
					
                    var insert = false;
                    if ($.isArray(curFeatureType.identifiers)) {
                        insert = ((typeIdentifierIndex = $.inArray(featureTypeId, curFeatureType.identifiers)) > -1);
                    }
                    else {
                        insert = (feature.featureType.xref.primaryRef.id.match(".*" + curFeatureType.identifiers + ".*") != null);
                    }
                    if (insert) {
                        var colour = curFeatureType.colour;
                        var rangeColour = self.getGradientColour(curFeatureType.colour);
                        var opacity = curFeatureType.opacity;
                        var symbol = curFeatureType.symbol;
                        var rangeList = feature.featureRangeList.featureRange;
                        var xrefs = new Object();
                        
                        if (!(feature.xref === undefined)) {
                            self.extractXrefs(feature.xref.primaryRef, xrefs);
                            var secondaryRef = feature.xref.secondaryRef;
                            if (!(secondaryRef === undefined)) {
                                $(secondaryRef).each(function(){
                                    self.extractXrefs(this, xrefs);
                                });
                            }
                        }
                        
                        var xrefText = "";
                        
                        for (var db in xrefs) {
                            xrefText += db + ": ";
                            $(xrefs[db]).each(function(){
                                xrefText += this + ", ";
                            });
                            xrefText = xrefText.substring(0, xrefText.lastIndexOf(",")) + "\n　　 ";
                        }
                        
                        if (xrefText != "") {
                            xrefText = "\nxref: " + xrefText.substring(0, xrefText.lastIndexOf("\n"));
                        }
                        
                        var featureName = feature.names.fullName;
                        if (featureName === undefined) {
                            featureName = feature.names.shortLabel;
                        }
                        
                        var tooltipText = feature.featureType.names.shortLabel;
                        if (tooltipText === undefined) {
                            tooltipText = categoryKey;
                        }
						
                        tooltipText = "name: " + featureName + "\ntype: " + tooltipText + xrefText;
						
						if(categoryKey == "not recognised"){
							tooltipText += "\ntypeId: " + curFeatureType.identifiers[typeIdentifierIndex];
						}
						
                        var coordinates = null;
                        
                        // if more than one range is provided, all are drawn and connected by a line
                        // the whole feature starts at the smallest x-value and ends at the greatest one 
                        if (rangeList.length > 1) {
                            coordinates = self.drawMultipleRangeList(categoryKey, colour, rangeColour, opacity, rangeList, curY, height, self._interactionInformation._proteinX, interactorLength, tooltipText, symbol);
                            self.addCalledFunction(self._multipleRangeKeyword, rangeList[0], symbol);
                        }
                        else {
                            coordinates = self.drawSingleRangeList(categoryKey, colour, rangeColour, opacity, rangeList, curY, height, self._interactionInformation._proteinX, interactorLength, tooltipText, symbol);
                        }
                        
						self.addOnClickHandling(interactorId, featureId, coordinates);
						
                        // provide the coordinates of linked features
                        var id = feature.id;
                        if (!(self._interactionInformation._linkedFeatures[id] === undefined)) {
                            self._interactionInformation._linkedFeatures[id] = coordinates;
                        }
                    }
                }
            }
            curY = curY + height + self._interactionInformation._featureGap;
        });

        return curY;
    }
    
	
	// calculate gradient colour
	this.getGradientColour = function(hexString){
		var HSL = this._interactionInformation._utils.HEXtoHSL(hexString);
		HSL[2] += 10;
		return this._interactionInformation._utils.HSLtoHEX(HSL);
	}
	
	// add given eventHandling function to each feature
	this.addOnClickHandling = function(interactorId, featureId, coordinates){
		var self = this;
		if (!(coordinates.eventHandlingElement === undefined) &&  (typeof self._interactionInformation.options.onFeatureClick == 'function')) {
			coordinates.eventHandlingElement.click(function(event){
				self._interactionInformation.options.onFeatureClick(
							interactorId, featureId, event);
			});
		}
	}
	
    // extract xref-Information
    this.extractXrefs = function(xref, xrefs){
        var db = xref.db;
        if (xrefs[db] === undefined) {
            xrefs[db] = new Array(xref.id);
        }
        else {
            xrefs[db].push(xref.id);
        }
    }
    
    // draw all ranges of a feature and connect them with a line
    this.drawMultipleRangeList = function(categoryKey, colour, rangeColour, opacity, rangeList, y, height, featureStart, interactorLength, tooltipText, symbol){
        var self = this;
        var x = Number.MAX_VALUE;
        var x2 = 0;
        var curCoordinates = null;
        
        var addText = "\npositions: ";
        var set = this._shapeDrawer.getSet();
        var singlePositions = new Array();
        
        $(rangeList).each(function(){
            curCoordinates = self.drawSingleRangeList(categoryKey, colour, rangeColour, opacity, this, y, height, featureStart, interactorLength, "", "", symbol);
            if (curCoordinates.x < x) {
                x = curCoordinates.x;
            }
            if (curCoordinates.x2 > x2) {
                x2 = curCoordinates.x2;
            }
            set.push(curCoordinates.element);
            
            singlePositions.push(curCoordinates.x);
        });
        
        singlePositions.sort(function(a, b){
            return a - b;
        });
        $(singlePositions).each(function(){
            addText += this + ", ";
        });
        
        if (tooltipText != "") {
            tooltipText += addText.substring(0, addText.lastIndexOf(","));
        }
        
        var bb = set.getBBox();
        
        coordinates = self.drawConnectingLine(x, x2, set, colour, tooltipText);
        return coordinates;
    }
    
    // draw ONE range of a feature
    this.drawSingleRangeList = function(category, colour, rangeColour, opacity, rangeList, y, height, featureStart, interactorLength, tooltipText, symbol){
    
        var coordinates = null;
        
		var startId = rangeList.startStatus.xref.primaryRef.id; 
		var endId = rangeList.endStatus.xref.primaryRef.id;
		
		if(!(this._interactionInformation._rangeStatusEquivalents[startId] === undefined)){
			startId = this._interactionInformation._rangeStatusEquivalents[startId];
		}
		
		if(!(this._interactionInformation._rangeStatusEquivalents[endId] === undefined)){
			endId = this._interactionInformation._rangeStatusEquivalents[endId];
		}
		
        // extract the id's of start- and endStatus to build the drawing function's name
        startId = (startId).substring(3);
        endId = (endId).substring(3);
        
        // "draw + identifiers" represents the default-behaviour
        var functionName = "draw" + startId + endId;
        
        // erase all whitespaces in category to build the specific drawing function's name
        category = category.replace(/\s*/g, "");
        var categoryFunctionName = category + startId + endId;
        
        // check whether the function exists
        if (this._rangeStatusFunctionCollection[categoryFunctionName]) {
            this.addCalledFunction(categoryFunctionName, rangeList, symbol);
            coordinates = this._rangeStatusFunctionCollection[categoryFunctionName](rangeList, y, height, featureStart, interactorLength, colour, rangeColour, opacity, tooltipText, symbol);
        }
        else 
            if (this._rangeStatusFunctionCollection[functionName]) {
                this.addCalledFunction(functionName, rangeList, symbol);
                coordinates = this._rangeStatusFunctionCollection[functionName](rangeList, y, height, featureStart, interactorLength, colour, rangeColour, opacity, tooltipText, symbol);
            }
            else {
                this.addCalledFunction("draw", rangeList, symbol);
                if (this._interactionInformation.options.developingMode) {
                    console.log("Range status combination " + startId + " - " + endId + " not recognised.");
                }
				tooltipText += "\nrange type: " + rangeList.startStatus.xref.primaryRef.id 
									+ " - "	+ rangeList.endStatus.xref.primaryRef.id;
                coordinates = this.draw(rangeList, y, height, featureStart, interactorLength, colour, rangeColour, opacity, tooltipText, symbol);
            }
        return coordinates;
    };
    
    // add function to called function (for legend) and add all kinds of ranges if necessary
    this.addCalledFunction = function(functionName, rangeList, symbol){
        if (symbol != "") {
            symbol = "LegendSymbol";
        }

        if (this._calledFunctions[functionName] === undefined) {
            this._calledFunctions[functionName] = new Array({
                "range": rangeList,
                "symbol": symbol
            });
        }
        else {
            var add = false;

            $(this._calledFunctions[functionName]).each(function(){
                if ((this.symbol != symbol) ||
                (this.range.beginInterval === undefined && !(rangeList.beginInterval === undefined)) ||
                (this.range.begin === undefined && !(rangeList.begin === undefined)) ||
                (this.range.endInterval === undefined && !(rangeList.endInterval === undefined)) ||
                (this.range.end === undefined && !(rangeList.end === undefined))) {
                    add = true;
                }

            });

            if (add) {
                this._calledFunctions[functionName].push({
                    "range": rangeList,
                    "symbol": symbol
                });
            }
        }
    };
    
    // draw line for given coordinates and colour it
    this.drawConnectingLine = function(x, x2, featureSet, colour, tooltipText){
        var bb = featureSet.getBBox();
        
        var lineY = bb.y + bb.height / 2;
        
        var line = this._shapeDrawer.getLine(bb.x + 1, lineY, bb.x + bb.width - 2, lineY);
        line.attr({
            "stroke": colour,
            "stroke-dasharray": "- "
        });
        featureSet.push(line);
        var eventHandlingElement = this._featureDrawer.createEventHandlingElement(line);
        this._featureDrawer.createTooltip(eventHandlingElement, tooltipText, "", "");
        var coordinates = {
            "x": x,
            "x2": x2,
            "element": featureSet,
            "eventHandlingElement": eventHandlingElement
        };
        return coordinates;
    };
    
    // draw line (actually a quadrangle) between linked features
    this.drawInteraction = function(participants){
		var self = this;
		
		if (participants.length != 2) {
			return true;
		}
		else {
			idFirst = participants[0].participantFeatureRef;
			idSecond = participants[1].participantFeatureRef;
			
			var coordinatesFirst = this._interactionInformation._linkedFeatures[idFirst];
			var coordinatesSecond = this._interactionInformation._linkedFeatures[idSecond];
			if (!(coordinatesFirst === undefined || coordinatesSecond === undefined ||
			coordinatesFirst.element === undefined ||
			coordinatesSecond.element === undefined)) {
				var bbFirst = coordinatesFirst.element.getBBox();
				var bbSecond = coordinatesSecond.element.getBBox();
				
				if (bbFirst.y > bbSecond.y) {
					coordinatesFirst = this._interactionInformation._linkedFeatures[idSecond];
					coordinatesSecond = this._interactionInformation._linkedFeatures[idFirst];
					var id = idFirst;
					idFirst = idSecond;
					idSecond = id;
					bbFirst = coordinatesFirst.element.getBBox();
					bbSecond = coordinatesSecond.element.getBBox();
				}
				
				var quad = this._shapeDrawer.getQuadrangle(bbFirst.x, bbFirst.y + bbFirst.height, bbFirst.width, bbSecond.x, bbSecond.y, bbSecond.width);
				quad.attr({
					"stroke": "#D17600",
					"stroke-dasharray": "- ",
					"stroke-width": 0.5,
					"fill": this._interactionInformation._typeCategories["binding site"].colour,
					"fill-opacity": 0.3
				});

				quad.insertBefore(coordinatesFirst.eventHandlingElement);
				quad.insertBefore(coordinatesSecond.eventHandlingElement);
				
				this._interactions.push({
					"top": bbFirst,
					"bottom": bbSecond,
					"element": quad
				});
				this._interactionSet.push(quad);
				
				var connectionSet = this._shapeDrawer.getSet();
				connectionSet.push(quad);
				
				if (bbFirst.width + bbSecond.width < 5) {
					var line = this._shapeDrawer.getLine(bbFirst.x, bbFirst.y + bbFirst.height, bbSecond.x, bbSecond.y);
					line.attr({
						"stroke-width": 2,
						"opacity": 0
					});
					line.insertBefore(coordinatesFirst.eventHandlingElement);
					line.insertBefore(coordinatesSecond.eventHandlingElement);
					connectionSet.push(line);
				}
				
				this.prepareEventHandling(coordinatesFirst.eventHandlingElement, [coordinatesFirst.element, coordinatesSecond.element], quad);
				this.prepareEventHandling(coordinatesSecond.eventHandlingElement, [coordinatesFirst.element, coordinatesSecond.element], quad);
				this.prepareEventHandling(connectionSet, [coordinatesFirst.element, coordinatesSecond.element], quad);
			}
		}
		return false;
    };
	
    // store information so that onClick-events can be handled properly
	this.prepareEventHandling = function(element, highlightElements, connection){
		var id = element[0].id;
		if (this._connectedElements[id] === undefined) {
			this._connectedElements[id] = {
				"highlightSet": element,
				"highlightElements": new Array(),
				"connections": new Array()
			};
		}
		this._connectedElements[id].connections.push(connection);
		var self = this;
		$(highlightElements).each(function(){
			if ($.inArray(this, self._connectedElements[id].highlightElements) < 0) {
				self._connectedElements[id].highlightElements.push(this);
			}
		});
	}
	
	// add eventListener to each interaction's elements
	this.handleClickForConnectionSet = function(coordinates){
		if (!(coordinates.highlightSet === undefined)) {
			var self = this;
			coordinates.highlightSet.click(function(event){
				var equalConnections = self.equalConnections(coordinates.connections, self._selectedInteraction.connections);
				
				if (!self._hidden || !equalConnections) {
					if (self._hidden && !equalConnections) {
						self.unhighlight(self._selectedInteraction);
					}
					
					self._selectedInteraction = {
						"connections": coordinates.connections,
						"elements": coordinates.highlightElements
					};
					
					self.highlight(self._selectedInteraction);
					self._hidden = true;
				}
				else {
					self.unhighlight(self._selectedInteraction);
					self._hidden = false;
				}
			});
		}
	};
	
	// compare connectionArrays
	this.equalConnections = function(arrayFirst, arraySecond){
		if(arrayFirst == null || arraySecond == null){
			return false;
		}
		if(arrayFirst.length != arraySecond.length){
			return false;
		}
		
		for(var i = 0; i < arrayFirst.length; i++){
			if(arrayFirst[i].id != arraySecond[i].id){
				return false;
			}
		}

		return true;
	}
	
    // unhighlight a featureSet
    this.unhighlight = function(interaction){
		this._interactionSet.show();
        for(var i = 0; i < interaction.elements.length; i++){
			interaction.elements[i].attr("stroke", this._saveColours[i]);
		}
    };
    
    // highlight a featureSet
    this.highlight = function(interaction){
		this._interactionSet.hide();
		$(interaction.connections).each(function(){
			this.show();
		});
		
		this._saveColours = new Array();
		for (var i = 0; i < interaction.elements.length; i++) {
			this._saveColours.push(interaction.elements[i].attr("stroke"));
			interaction.elements[i].attr("stroke", "black");
		}
    };
    
    // draw the protein as a black rectangle with dotted lines every 100 AAs
    this.drawProtein = function(x, y, length, height, drawAAlines){
        var protein = this._shapeDrawer.getRectangle(x - 1, y, length + 2, height);
        var r = height/10;
		
		if (r > 5) {
			r = 5
		};
		
		if(length < r){
			r = 1;
		}
		
        protein.attr({
			"fill": "white",
            "r": r,
            "rx": r,
            "ry": 1
        });
        
        this._interactorSet.push(protein);
        y++;
        if (drawAAlines) {
			length = length + x;
			var hundredAA = 100 * this._interactionInformation._pxPerAA;
			var y2 = y + height;
			
			for (var xi = x + hundredAA; xi < length; xi = xi + hundredAA) {
				var line = this._shapeDrawer.getLine(xi, y, xi, y2);
				line.attr("stroke-dasharray", ". ");
				line.attr("stroke-width", 0.5);
				this._interactorSet.push(line);
			}
		}
		protein.toBack();
		return protein;
    };
    
    // arrange interaction levels (so all of them will be clickable)
    this.arrangeInteractions = function(){
        var self = this;
        this._interactions.sort(function(a, b){
            return self.interactionSortFunction(a, b);
        });
        
        for (var i = 1; i < this._interactions.length; i++) {
            // put interaction with smallest area on top and the one with largest area on bottom level
            // so each interaction can receive a mouse event
            this._interactions[i].element.insertAfter(this._interactions[i - 1].element);
        }
    };
    
    // calculating the area of a and b to define their position in the sorted array
    this.interactionSortFunction = function(a, b){
        var bbA = a.element.getBBox();
        var bbB = b.element.getBBox();
        var areaA = bbA.width * bbA.height;
        var areaB = bbB.width * bbB.height;
        
        return areaB - areaA;
    };
    
    // draw legend with used range-combinations
    this.drawLegend = function(x, y){
		var textGap = 10;
        var legendItemWidth = this._interactionInformation._legendItemWidth / this._interactionInformation._pxPerAA;
        var gap = 2 * this._interactionInformation._featureGap;
        var legendHeight = gap;

        var legendPictureWidth = this.calculateLegendPictureWidth(legendItemWidth, textGap);
        var legendRangetypeSectionWidth = this.calculateLegendRangetypeSectionWidth(textGap);
        if(this._interactionInformation.options.legendPosition == "right"){
            x = this._interactionInformation.options.width - (legendPictureWidth + legendRangetypeSectionWidth);
        }

        var legendText = this._shapeDrawer.getText(x, y, "Legend:");
		legendText.attr("font-size", 12);
        y += 10;
        var yText = y + this._interactionInformation._legendItemHeight / 2 + gap + 1;
		var yLine = y + this._interactionInformation._legendItemHeight + gap + 4;
		var fTypesText = this._shapeDrawer.getText(x + textGap, yText, "feature types");
		fTypesText.attr("font-size", 12);
		var line = this._shapeDrawer.getLine(x + textGap, yLine, x + textGap + fTypesText.getBBox().width, yLine);
		line.attr("stroke-width",0.5);
	
		legendHeight = legendHeight + this._interactionInformation._legendItemHeight + 3*gap;
		
		var pictureHeight = this.drawLegendPicture( x, y, legendHeight, legendItemWidth,
                                                    this._interactionInformation._legendItemHeight, gap + 1,
                                                    textGap, legendPictureWidth);
		var itemX = x + legendPictureWidth + textGap;
        legendHeight = this.drawLegendRangetypeSection(itemX, y, yText, yLine, legendItemWidth, legendHeight, textGap, gap);

		if(pictureHeight > legendHeight){
			legendHeight = pictureHeight;
		}

        var legendRect = this._shapeDrawer.getRectangle(x, y, legendPictureWidth + legendRangetypeSectionWidth, legendHeight);
		legendRect.attr({
			"fill": "white",
			"stroke-width": 0.5, 
			"stroke-dasharray": ". "
		});
		legendRect.toBack();
        var line = this._shapeDrawer.getLine(x + legendPictureWidth, y,
											 x + legendPictureWidth, y + legendHeight);
		line.attr({"stroke-width": 0.5, "stroke-dasharray": ". "});
        this._interactionInformation._height = y + legendHeight + 10;
        this._interactionInformation._paper.setSize(this._interactionInformation.options.width, this._interactionInformation._height);
    };

    this.calculateLegendPictureWidth = function(legendItemWidth, textGap){
        var width = 0;
        for (var category in this._interactionInformation._typeCategories) {
			var textObject = this._shapeDrawer.getText(0,0, category);
            var bb = textObject.getBBox();
            if(bb.width > width){
                width = bb.width;
            }
            textObject.remove();
        }
        return width + ((legendItemWidth/2) * this._interactionInformation._pxPerAA) + 4 * textGap;
    }


    this.calculateLegendRangetypeSectionWidth = function(textGap){
        var width = 0;
        var self = this;
        for (var functionName in this._calledFunctions) {
            $(this._calledFunctions[functionName]).each(function(){
                var text = self.getLegendRangetypeItemText(functionName, this.range);
                var textObject = self._shapeDrawer.getText(0,0,text);
                var bb = textObject.getBBox();
                if(bb.width > width){
                    width = bb.width;
                }
                textObject.remove();
            });
        }
        return width + this._interactionInformation._legendItemWidth + 3*textGap;
    };

    this.getLegendRangetypeItemText = function(functionName, range){
        var legendItemText = range.startStatus.names.shortLabel + " - " + range.endStatus.names.shortLabel;

        var category = functionName.replace(/\d*/g, "");

        if (category == self._multipleRangeKeyword) {
            legendItemText = "features with non-continuous positions";
        }
        else

            if (legendItemText == "undetermined - undetermined"){
                legendItemText = "non-positional ";
            }

            if (category != "draw") {
                legendItemText += category;
            }
        return legendItemText;
    }

    this.drawLegendRangetypeSection = function(itemX, y, yText, yLine, legendItemWidth, legendHeight, textGap, gap){
        var itemTextX = 0;
		if (!this._interactionInformation._utils.isEmptyObject(this._calledFunctions)) {
			itemTextX = itemX + this._interactionInformation._legendItemWidth + textGap;

			var rTypesText = this._shapeDrawer.getText(itemX, yText, "range types");
			rTypesText.attr("font-size", 12);
			var line2 = this._shapeDrawer.getLine(itemX, yLine, itemX + rTypesText.getBBox().width, yLine);
			line2.attr("stroke-width", 0.5);

			var self = this;

			for (var functionName in this._calledFunctions) {
				$(this._calledFunctions[functionName]).each(function(){

					var range = this.range;
                    if(range === undefined){
                         range = {};
                    }
					var x1 = 1;
					var x2 = x1 + legendItemWidth;

					if (!(range.begin === undefined || range.end === undefined) &&
					range.end.position == 0 &&
					range.begin.position == 0) {
						x1 = 10;
					}

					var rangeDifference = (legendItemWidth/4);

					if (!(range.begin === undefined)) {
						range.begin.position = x1;
					}
					else if(!(range.beginInterval === undefined)){
						range.beginInterval.begin = x1;
						range.beginInterval.end = x1 + rangeDifference;
					}else{
                        range["begin"] = {"position": x1}
                    }

					if (!(range.end === undefined)) {
						range.end.position = x2;
					}
					else if(!(range.endInterval === undefined)){
						range.endInterval.begin = x2 - rangeDifference;
						range.endInterval.end = x2;
					}else{
                        range["end"] = {"position": x1}
                    }

					var coordinates = null;
					if (self._rangeStatusFunctionCollection[functionName]) {
						coordinates = self._rangeStatusFunctionCollection[functionName](range, y + legendHeight, self._interactionInformation._legendItemHeight, itemX, legendItemWidth, self._interactionInformation._legendItemColour, self._interactionInformation._legendItemRangeColour, self._interactionInformation._legendItemOpacity, "", this.symbol);
					}
					else
						if(self[functionName]){
							coordinates = self[functionName](range, y + legendHeight, self._interactionInformation._legendItemHeight, itemX, legendItemWidth, self._interactionInformation._legendItemColour, self._interactionInformation._legendItemRangeColour, self._interactionInformation._legendItemOpacity, "", this.symbol);
						}
					else
						if (functionName == self._multipleRangeKeyword) {
							range.end.position = range.begin.position;
							var range2 = new Object();

							$.extend(true, range2, range);

							range2.end.position += legendItemWidth;
							range2.begin.position += legendItemWidth;

							var rangeList = [range, range2];
							// categoryKey, colour, rangeColour, opacity, rangeList, y, height, interactorLength
							coordinates = self.drawMultipleRangeList("", self._interactionInformation._legendItemColour, self._interactionInformation._legendItemRangeColour, self._interactionInformation._legendItemOpacity, rangeList, y + legendHeight, self._interactionInformation._legendItemHeight, itemX, legendItemWidth, "", this.symbol);
						}

					if (!(coordinates == null || coordinates.x === undefined)) {
						var legendItemText = self.getLegendRangetypeItemText(functionName, range);
						var text = self._shapeDrawer.getText(itemTextX, y + legendHeight + self._interactionInformation._legendItemHeight / 2, legendItemText);
						legendHeight = legendHeight + self._interactionInformation._legendItemHeight + gap;
					}
				});
			}
	}
        return legendHeight;
    };

	this.drawLegendPicture = function(x, yLegend, startGap, legendItemWidth, legendItemHeight, gap, textGap, pictureWidth){
		var pictureHeight = yLegend;
		var itemX = x + textGap;
		yLegend += startGap;
		
		yLegend = this.drawLegendItemsPerPosition("top", itemX, yLegend, legendItemWidth, legendItemHeight, gap, textGap);

		bottomY = this.drawLegendItemsPerPosition("middle", itemX, yLegend + gap, legendItemWidth, legendItemHeight, gap, textGap);

		this.drawProtein(itemX - 2, yLegend, pictureWidth - 2*textGap, bottomY - yLegend, false);
		yLegend = bottomY + gap;
		yLegend = this.drawLegendItemsPerPosition("bottom", itemX, yLegend, legendItemWidth, legendItemHeight, gap, textGap);
		pictureHeight = yLegend - pictureHeight;
		
		return pictureHeight;
	};
	
	
	this.drawLegendItemsPerPosition = function(position, x, y, legendItemWidth, legendItemHeight, gap, textGap){
		for (var category in this._interactionInformation._typeCategories) {
			var curType = this._interactionInformation._typeCategories[category];
			if (!($.isArray(curType.identifiers)) || curType.identifiers.length > 0) {
				if (curType.position == position) {
					var range = new Object();
					range["begin"] = new Object();
					
					range["end"] = new Object();
					if (curType.symbol == "") {
						range.begin["position"] = 1;
						range.end["position"] = legendItemWidth / 2;
					}
					else {
						range.begin["position"] = 5 / this._interactionInformation._pxPerAA;
						range.end["position"] = 5 / this._interactionInformation._pxPerAA;
					}
					
					var functionName = category + "03350335";
					if (!this._rangeStatusFunctionCollection[functionName]) {
						functionName = "draw03350335";
					}
					this._rangeStatusFunctionCollection[functionName](range, y, legendItemHeight, x, 0, curType.colour, curType.rangeColour, curType.opacity, "", curType.symbol);
					
					var textX = x + legendItemWidth / 2 * this._interactionInformation._pxPerAA + textGap;
					var text = this._shapeDrawer.getText(textX, y + legendItemHeight / 2, category);
					text.attr("font-size", 10);
					y += gap + legendItemHeight;
				}
			}
		}
		return y;
	}
	
    // add participant name at the end of a protein
    this.drawParticipantName = function(x, y, text){
        var name = this._shapeDrawer.getText(x + 20, y, text);
        this._interactorSet.push(name);
    };
    
    // add length at the end of a protein
    this.drawLengthText = function(x, y, text){
        var length = this._shapeDrawer.getText(x + 10, y, text);
        length.attr("font-size", 9);
        this._interactorSet.push(length);
    };
    
    // default drawing function if there is no other one
    this.draw = function(range, y, height, featureStart, interactorLength, colour, rangeColour, opacity, tooltipText, symbol){
        return this._featureDrawer.drawUnrecognisedRangeType(range, y, height, featureStart, interactorLength, colour, rangeColour, opacity, tooltipText, symbol);
    };
}
