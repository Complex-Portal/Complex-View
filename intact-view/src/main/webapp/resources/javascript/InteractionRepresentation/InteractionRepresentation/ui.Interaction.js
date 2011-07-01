(function($){
    var interactionRepresentation = {
        options: {
            width: 10,
            jsonUrl: '',
            developingMode: false,
			onFeatureClick: null, // function; parameters: protein_ac, feature_ac, event
            proxyUrl: '',
            useProxyForData: true,
            useProxyForOntology: true,
			useProxyForColours: true,
            legendPosition: 'left',
            loadingImageUrl: ''
        },

        // used for the query to OLS
        _MIParent: 'MI:0116', // "feature type"

        // possible positions on a protein, specifying the height of features in this position
        _positionsOnProtein: {
            "top": 7,
            "middle": 10,
            "bottom": 7
        },

		// does all the drawing
        _drawer: null,

		// methods used in all parts of the widget
        _utils: null,

        // - categorizing Identifiers(children of "biological feature" and "experimental feature"),
		// - if you want to use more than one parent term for a category make that the one
		// 	   with an entry in the DAS stylesheet is at first place
        // - if loadChildren is true, children of these terms will be added to the arrays, containing their parent term
        // - specifying there position on the protein, the term used has to be included in "_positionsOnProtein"
        // - if "identifiers" contains a string instead of an array, all features with an id containing this term will
		//   be added to the featureType
		// - if "symbol" is != "", the default representation will be the symbol provided in a function "draw"+value
		//   and the range will be represented by a line, otherwise the feature will be represented as a rectangle

        // - if "stylesheetTerm" is specified the colour will be loaded from the DAS stylesheet
		//   else the specified colour is used. Please provide a colour in case the term is not found in the stylesheet

		_typeCategories: {
            "binding site": {
                "identifiers": ['MI:0117'],
				"loadChildren": true,
                "position": "middle",
				"colour": "#FAB875",
                "opacity": "1",
				"symbol" : ""
            },
            "tag": {
                "identifiers": ['MI:0507', 'MI:0845', 'MI:0856', 'MI:0373', 'MI:0863', 'MI:0950'],
				"loadChildren": true,
                "position": "top",
                "colour": "#99ccff",
                "opacity": "1",
				"symbol" : ""
            },
            "mutation": {
                "identifiers": ['MI:0118'],
				"loadChildren": true,
                "position": "bottom",
				"colour": "#EEEEEE",
                "opacity": 1,
				"symbol" : "",
				"stylesheetTerm" : "MUTAGEN"
            },
            "isotope": {
                "identifiers": ['MI:0253'],
				"loadChildren": true,
                "position": "top",
                "colour": "#F2F200",
                "opacity": "1",
				"symbol" : "Isotope"
            },
            "identified peptide": {
                "identifiers": ['MI:0656'],
				"loadChildren": true,
                "position": "middle",
                "colour": "#FF2600",
                "opacity": "1",
				"symbol" : ""
            },
			"ptm": {
				"identifiers": "MOD:",
				"loadChildren": false,
				"position": "bottom" ,
				"colour": "#AAAAAA",
				"opacity": 1,
				"symbol" : "PTM",
				"stylesheetTerm": "MOD_RES"
			},
			"not recognised": {
				"identifiers": [],
				"loadChildren": false,
				"position": "middle",
				"colour": "#bebebe",
				"opacity": 1,
				"symbol": ""
			}
        },

		_ignoreTerms: new Array(),

		_rangeStatusEquivalents: { "MI:0341": "MI:0340"},

        _MIOntologyUrl: 'http://www.ebi.ac.uk/ontology-lookup/json/termchildren?ontology=MI&depth=1000&termId=',
		_DASStylesheet: 'http://wwwdev.ebi.ac.uk/das-srv/uniprot/das/uniprot/stylesheet',

		// structures needed to manage data --
		_featureTracksPerParticipant: new Array(),
        _featurePositions: new Object(),
        _linkedFeatures: new Object(),
		_joinedFeatures: new Object(),
        _participantsWithFeatures: new Object(),
        _error: false,
        // --

        _featureGap: 0,
        _proteinGap: 0,

        _finishedRequests: new Object(),
        _height: 0,
        _interactionInformation: null,
        _pxPerAA: 0,
        _proteinWidth: 0,
        _proteinX: 0,
        _strokeWidth: 0,
        _initY: 0,

		// legend properties --
		_legendDistanceToImage :0,
        _legendItemWidth: 0,
        _legendItemHeight: 0,
        _legendItemColour: "",
        _legendItemRangeColour: "",
        _legendItemOpacity: 0,
        _tooltipOpacity: 0,
        _tooltipColour: "",
		// --

		// element containing the widget
		_myElement: null,

		// function called by widget framework
        _create: function(){
			this._utils = new Utils();
            this._proteinWidth = this.options.width - 150;
            this._pxPerAA = this._proteinWidth;
            this._proteinX = 12;
            this._strokeWidth = 0.7;
            this._initY = 20;
			this._legendDistanceToImage = 50;
            this._featureGap = 2;
            this._proteinGap = 10;
            this._legendItemHeight = 7;
            this._legendItemWidth = 30;
            this._legendItemColour = "#696969";
            this._legendItemRangeColour = "#bebebe";
            this._legendItemOpacity = 1;
            this._tooltipColour = "#696969";
            this._tooltipOpacity = 0.7;
        },

		// init function called by widget framework
        _init: function(){
		   // prepare data
            this._load();
            var self = this;
			// if all data is loaded
            $(this.element).bind('load_finished', function(){

                $(self.element).empty();

                if(self._error){
                    $(self.element).append("An error occurred while loading the data.");
                }else{
                    self._arrangeFeatures();
                    self._height = self._calculateHeight() + 5;

                    self._myElement = document.getElementById(self.element.attr('id'));
                    self._paper = Raphael(self._myElement, self.options.width, self._height);
                    self._drawer = new ParticipantDrawer(self);

                    self._drawer.drawInteractors();
                }
            });
        },

        _load: function(){
            this._finishedRequests["loadData"] = false;
            this._finishedRequests["loadAllIdentifiers"] = false;
			this._finishedRequests["loadColours"] = false;
            this._loadData();
            this._loadAllIdentifiers();
			this._loadColours();
            if(this.options.loadingImageUrl != ''){
                var loadingImageText = '<img id="progress_image" ' +
                                            'src=" ' + this.options.loadingImageUrl +' " ' +
                                            'alt="Loading..."/>';
                $(this.element).append(loadingImageText);
            }
        },

        // load data of the given json-file
        _loadData: function(){
            var self = this;
			var url = this.options.jsonUrl;
			var proxyUrl = self.options.proxyUrl;

            if(!self.options.useProxyForData){
                $.ajax({
                    url: url,
                    method: 'GET',
                    dataType: 'json',
                    success: function(json){
						if(typeof(json) != 'object'){
							json = $.parseJSON(json);
						}
                        self._interactionInformation = json;
                        self._finishedRequests["loadData"] = true;
                        self._checkRequests();
                    },
                    error: function(){
                        self._alertError(url);
                        self._finishedRequests["loadData"] = true;
                        self._checkRequests();
                    }
                });
            }else{
                url = escape(url);
                $.ajax({
                    url: proxyUrl,
                    method: "GET",
                    dataType: 'json',
                    data: "url=" + url,
                    success: function(json){
						if(typeof(json) != 'object'){
							json = $.parseJSON(json);
						}
                        self._interactionInformation = json;
                        self._finishedRequests["loadData"] = true;
                        self._checkRequests();
                    },
                    error: function(){
                        self._alertError(proxyUrl + "?url=" + url);
                        self._finishedRequests["loadData"] = true;
                        self._checkRequests();
                    }
                });
            }
        },

        // error handling for AJAX requests
        _alertError: function(url){
            this._error = true;
            if(this.options.developingMode){
                console.log("Error occured while loading " + url);
            }
        },


        // send a request to the EBI-Ontology
        _loadAllIdentifiers: function(){
            var self = this;
            var proxyUrl = this.options.proxyUrl;
            if(!self.options.useProxyForOntology){
                $.ajax({
                    url: self._MIOntologyUrl + self._MIParent,
                    method: "GET",
                    success: function(json){
						if(typeof(json) != 'object'){
							json = $.parseJSON(json);
						}
                        self._parseResponse(json);
                        self._finishedRequests["loadAllIdentifiers"] = true;
                        self._checkRequests();
                    },

                    error: function(){
                        self._alertError(url);
                        self._finishedRequests["loadAllIdentifiers"] = true;
                        self._checkRequests();
                    }
                });
            }else{
                var url = escape(self._MIOntologyUrl) + self._MIParent;
                $.ajax({
                    url: proxyUrl,
                    method: "GET",
                    data: "url=" + url,
                    success: function(json){
						if(typeof(json) != 'object'){
							json = $.parseJSON(json);
						}
                        self._parseResponse(json);
                        self._finishedRequests["loadAllIdentifiers"] = true;
                        self._checkRequests();
                    },

                    error: function(){
                        self._alertError(url);
                        self._finishedRequests["loadAllIdentifiers"] = true;
                        self._checkRequests();
                    }
                });
            }
        },

		_loadColours: function(){
			var url = this._DASStylesheet;
			var self = this;
			if (!this.options.useProxyForColours) {
				$.ajax({
					type: "GET",
					url: url,
					dataType: "xml",
					success: function(xml){
						self._parseColours(xml);
						self._finishedRequests["loadColours"] = true;
						self._checkRequests();
					},
					error: function(){
						self._alertError(url);
                        self._finishedRequests["loadColours"] = true;
                        self._checkRequests();
					}
				});
			}else{
				var proxyUrl = this.options.proxyUrl;
				url = escape(url);
                $.ajax({
                    url: proxyUrl,
                    method: "GET",
                    data: "url=" + url,
                    success: function(xml){
						self._parseColours(xml);
						self._finishedRequests["loadColours"] = true;
						self._checkRequests();
                    },

                    error: function(){
                        self._alertError(url);
                        self._finishedRequests["loadColours"] = true;
                        self._checkRequests();
                    }
                });
			}
		},

		// parse the response of the request sent to the EBI-Ontology
        // add children of a term to the array containing it
        _parseResponse: function(json){
            var self = this;
            for (var category in this._typeCategories) {
				if (this._typeCategories[category].loadChildren) {
					var curArray = this._typeCategories[category].identifiers;
					if (this._typeCategories[category].loadChildren) {
						if ($.inArray(json.id, curArray) > -1) {
							$(json.children).each(function(){
								curArray.push(this.id);
							});
						}
					}
				}
            }

            if (!(json.children === undefined)) {
                $(json.children).each(function(){
                    self._parseResponse(this);
                });
            }
        },

		// get colours from stylesheet for categories with defined search term
		_parseColours: function(xml){
			var self = this;
			for(var key in this._typeCategories ){
				var curCategory = this._typeCategories[key];
				if(!(curCategory.stylesheetTerm === undefined)){
					var searchTerm = "TYPE[id = " + curCategory.stylesheetTerm + "]";
					var curType = $(xml).find(searchTerm);
					if (curType.length > 0) {
						this._typeCategories[key].colour = $(curType).find("BGCOLOR").text();
					}else{
						if(this.options.developingMode){
							console.log("Warning: \"" + curCategory.stylesheetTerm + "\" was not found in style sheet");
						}
					}
				}
			}
		},

        // arrange features per protein in tracks, a set of tracks per feature type
        _arrangeFeatures: function(){
            var self = this;
            self._pxPerAA = self._getPxPerAA();
            $(self._interactionInformation.interactions).each(function(){

                var interaction = this;

				self._sortParticipants(interaction);

                // create structure like {MI:0118: {tracks: {1:[start, end], 2: [start, end]}, MI:0117: {..}, .. }}
                // for each participant and store it in array
                $(interaction.participantList.participant).each(function(){

					var participant = this;
                    var lengthsObject = new Object();
                    lengthsObject["interactorRef"] = participant.interactorRef;

                    if (!(participant.featureList === undefined)) {

                        lengthsObject["annotations"] = new Object();

                        // add place to store information for each position on protein
                        for (var position in self._positionsOnProtein) {
                            lengthsObject.annotations[position] = new Object();
                        }

                        var i = 0;
                        // get information for each feature
                        $(participant.featureList.feature).each(function(){
							var feature = this;
							var index = self._equalsPredecessor(feature, participant.featureList.feature, i);
							var id = feature.featureType.xref.primaryRef.id;
							 if ($.inArray(id, self._ignoreTerms) == -1) {
							 	if (index < 0) {
							 		var count = 0;
							 		// find the right position to attach the feature
										for (var category in self._typeCategories) {
											var curPosition = self._typeCategories[category].position;
											var identifiers = self._typeCategories[category].identifiers;

											if ($.isArray(identifiers)) {
												// extract start and end of each feature and store information at right position
												if ($.inArray(id, identifiers) > -1) {
													lengthsObject["annotations"][curPosition][feature.id] = self._getFeatureSides(feature);
													count++;
												}
											}
											else {
												if (id.match(".*" + identifiers + ".*") != null) {
													lengthsObject["annotations"][curPosition][feature.id] = self._getFeatureSides(feature);
													count++;
												}
											}
										}

										if (count == 0) { // feature type not found
											if (self.options.developingMode) {
												console.log("Warning: feature type not recognised: " + feature.featureType.xref.primaryRef.id);
											}

											var curPosition = self._typeCategories["not recognised"].position;

											self._typeCategories["not recognised"].identifiers.push(feature.featureType.xref.primaryRef.id);
											lengthsObject.annotations[curPosition][feature.id] = self._getFeatureSides(feature);
										}
									}
									else {
										// similarFeature found, store id for replacement
										similarFeature = participant.featureList.feature[index];

										// check if similarFeature has to be replaced itself
										if (self._joinedFeatures[similarFeature.id] === undefined) {
											// if not:
											self._joinedFeatures[feature.id] = similarFeature.id;
										}
										else { // else store id of feature, by which the found similar feature will be replaced
											self._joinedFeatures[feature.id] = self._joinedFeatures[similarFeature.id];
										}
									}
								}
							self._featurePositions[feature.id] = i;
							i++;
                        });

                        for (var position in lengthsObject.annotations) {
                            if (!self._utils.isEmptyObject(lengthsObject["annotations"][position])) {
                                // create an annotationOverlaping-object for each object in "lengthsObject"
                                var annotObject = new annotationOverlaping();
                                annotObject.setAnnot(lengthsObject["annotations"][position]);
                                annotObject.run();

                                // replace raw information by tracks
                                lengthsObject["annotations"][position] = annotObject;
                            }
                        }
                    }
                    // store information in global object, identified by the interactor-id
                    self._featureTracksPerParticipant.push(lengthsObject);
                });

				self._relinkFeatures(this);
            });
        },

        // defining the start and end of a feature based on the information in the given json-file
        // these coordinates are altered manually for the use of annotOverlaping.js and should not be used to draw the feature
        // the orginal coordinates should be retrieved from the json-file whilst retrieving the rest of the information
        _getFeatureSides: function(feature){
            var min = Number.MAX_VALUE;
            var max = 0;
            var self = this;
			var resetMin = 0;
			var resetMax = 0;
            // going through all featureRanges, retrieving the minimum start and the maximum end
            // there will be either a "start"/"end" element or a "startInterval"/"endInterval" in the range
            $(feature.featureRangeList.featureRange).each(function(){

                if (!(this.begin === undefined)) {
                    if (min > this.begin.position) {
                        min = this.begin.position;
                    }
                }
                else
                    if (!(this.beginInterval === undefined)) {
                        if (min > this.beginInterval.begin) {
                            min = this.beginInterval.begin;
                        }
						if(resetMax > this.beginInterval.end){
							resetMax = this.endInterval.end;
						}
                    }
                    else { // if no begin is defined, the feature is non positional
                        min = 0;
                    }

                if (!(this.end === undefined)) {
                    if (max < this.end.position) {
                        max = this.end.position;
                    }
                }
                else
                    if (!(this.endInterval === undefined)) {
                        if (max < this.endInterval.end) {
                            max = this.endInterval.end;
                        }
						if(resetMin < this.endInterval.begin){
							resetMin = this.endInterval.begin;
						}
                    }
                    else { // if no end is defined, the feature is non positional
                        max = 0;
                    }
				if(this.startStatus.names.shortLabel.match("n-terminal.*") != null){
					min = 1;
				}

				if(this.endStatus.names.shortLabel.match("c-terminal.*") != null){
					max = self._proteinWidth/self._pxPerAA;
				}
            });

			resetMin = Number(resetMin);
			resetMax = Number(resetMax);
			min = Number(min);
			max = Number(max);

			if(resetMin == 0){
				resetMin = max;
			}

			if(resetMax == 0){
				resetMax = min;
			}

			if(min == 0 && max > 0){
				min = resetMin;
			}

			if(max == 0 && min > 0){
				max = resetMax;
			}

            // adding distance for distinguishing between samples & to be able to draw all feature types

            min = min - 1.5/this._pxPerAA;
			max = max + 1.5/this._pxPerAA;

			return [min, max];
        },

		// put participants with features at the beginning of the array
        _sortParticipants: function(interaction){
            var self = this;
            var participants = interaction.participantList.participant;
			if (participants.length > 1) {
                participants.sort(function(a, b){
                    return self._participantSortingFunction(a, b);
                });
            }
            this._findLinkedFeatures(interaction);

			if (participants.length > 1) {
				participants = this._sortParticipantsWithFeatures(participants);
			}
			interaction.participantList.participant = participants;
        },

        // find linked features and prepare the storage of their coordinates
        _findLinkedFeatures: function(interaction){
            var self = this;
            if (!(interaction.inferredInteractionList === undefined)) {
                $(interaction.inferredInteractionList.inferredInteraction).each(function(){
                    $(this.participant).each(function(){
                        var featureRef = this.participantFeatureRef;
                        if (!(featureRef === undefined)) {
                            self._linkedFeatures[featureRef] = new Object();

							for(var iRef in self._participantsWithFeatures){
								var curObject = self._participantsWithFeatures[iRef];
                                if ($.inArray(featureRef, curObject.featureRefs) > -1) {
                                    curObject.linkedCount++;
                                }
                            }
                        }
                    });
                });
            }
        },

		// checks if a feature equals an predecessor in the given list
		// returns the index of the similar feature or -1 if no similar feature is found
		_equalsPredecessor: function(feature, featureList, index){
			var storeI = 0;
			var equal = false;
			for(var i = 0; (i < index) && !equal; i++){
				equal = this._equalFeatures(feature, featureList[i]);
				if(equal){
					storeI = i;
				}
			}
			return (equal)?storeI:-1;
		},

		// checks if two features are the same by removing the id and compare the rest of the properties
		_equalFeatures: function(feature1, feature2){
			var testFeature1 = this._removeFeatureIds(feature1);
			var testFeature2 = this._removeFeatureIds(feature2);
            return this._equalObjects(testFeature1, testFeature2);
		},

        // remove ids to check if features are similar
        _removeFeatureIds: function(feature){
            var newFeature = $.extend(true, {}, feature);
            delete newFeature.id;

            if(!(newFeature.xref.primaryRef === undefined) && newFeature.xref.primaryRef.db == "intact"){
				delete newFeature.xref.primaryRef;
			}

			if (!(feature.xref.secondaryRef === undefined)) {
                var j = 0;
				for (var i = 0; i < feature.xref.secondaryRef.length; i++) {
					if (feature.xref.secondaryRef[i].db == "intact") {
						newFeature.xref.secondaryRef.splice(j,1);
                        j--;
					}
                    j++;
				}
			}

            return newFeature;
        },

		// checks if two objects have the same values
		_equalObjects: function(object1, object2){
			for(var element in object1){
				if(object2[element] === undefined){
					return false;
				}
			}

			for(var element in object2){
				if(object1[element] === undefined){
					return false;
				}
			}

			for(var element in object1){
				switch(typeof(object1[element])){
					case 'object':
						var equal = this._equalObjects(object1[element], object2[element]);
						if(!equal){
							return false;
						}
						break;
					default:
						var equal = (object1[element] == object2[element]);
						if(!equal){
							return false;
						}
				}
			}
			return true;
		},

		// after searching for similar features and joining them, the inferredInteractionList has to be altered
		_relinkFeatures: function(interaction){
			var self = this;
            if (!(interaction.inferredInteractionList === undefined)) {
                $(interaction.inferredInteractionList.inferredInteraction).each(function(){
                    $(this.participant).each(function(){
						var featureRef = this.participantFeatureRef;
                        if (!(featureRef === undefined)) {
							// if current featureRef is found in joinedFeatures, replace it by the stored id
							if(!(self._joinedFeatures[featureRef] === undefined)){
								this.participantFeatureRef = self._joinedFeatures[featureRef];
							}
						}
					});
				});
			}
		},

        // calculates the height of the needed canvas based on the features extracted from the interaction information
        // if a protein has no features it will be drawn as high as one feature with the position "middle"
        _calculateHeight: function(){
            var height = 0;
            var self = this;
            $(this._featureTracksPerParticipant).each(function(){
                height = height + self._proteinGap;

                var curPositionObject = this;

                for (var position in self._positionsOnProtein) {
                    if ((curPositionObject.annotations === undefined) || curPositionObject.annotations.middle.tracks === undefined) {
                        // add space for participants without features
                        var heightMiddle = self._positionsOnProtein["middle"];
                        height = height + heightMiddle + self._featureGap;
                    }
                    else {
                        var curTracks = curPositionObject.annotations[position].tracks;
                        if (!(curTracks === undefined)) {
                            height = height + (curTracks.length * (self._positionsOnProtein[position] + self._featureGap));
                        }
                    }
                    height = height + self._proteinGap;
                }
            });

            return height;
        },

        // calculate the width of one AA in pixels
        _getPxPerAA: function(){
            var maxProteinLength = 0;

            // get the length of the longest protein
            $(this._interactionInformation.interactors).each(function(){
                if (!(this.sequence === undefined)) {
                    var length = this.sequence.length;
                    if (length > maxProteinLength) {
                        maxProteinLength = length;
                    }
                }
            });

            // calculate the width of one AA in pixels
            return (maxProteinLength > 0)?(this._proteinWidth) / maxProteinLength:1;
        },

        // check whether all registered requests are finished ('true' in this._finishedRequests)
        // and fire event 'load_finished' if all are finished
        _checkRequests: function(){
            var finished = true;
            for (var i in this._finishedRequests) {
                if (!this._finishedRequests[i]) {
                    finished = false;
                }
            }
            if (finished) {
                $(this.element).trigger("load_finished");
            }
        },

		// function passed to javascript-sort function for sorting participants
        _participantSortingFunction: function(a, b){
            var valueA = this._calculateSortValue(a);
            var valueB = this._calculateSortValue(b);

            return valueB - valueA;
        },


		// calculate the value for sorting function and prepare objects for further sorting
        _calculateSortValue: function(a){
            var self = this;
            var valueA = 0;
            if (!(a.featureList === undefined)) {
                if ($.inArray(a.interactorRef, this._participantsWithFeatures) < 0) {
                    this._participantsWithFeatures[a.id] = new Object();
                    this._participantsWithFeatures[a.id]["featureRefs"] = new Array();
                    this._participantsWithFeatures[a.id]["linkedCount"] = 0;
                    $(a.featureList.feature).each(function(){
                        self._participantsWithFeatures[a.id]["featureRefs"].push(this.id);
                    });
                }
                valueA = 1;
            }
            return valueA;
        },

		// sort participants by there linked feature count with the highest value in the middle
		_sortParticipantsWithFeatures: function(participants){
			var self = this;
			var length = this._utils.getObjectKeyCount(this._participantsWithFeatures);
			var withFeatures = participants.splice(0, length);

			withFeatures.sort(function(a,b){
				return self._participantWithFeatureSortingFunction(a,b);
			})
			withFeatures = this._arrangeByCount(withFeatures);
			return $.merge(withFeatures, participants);
		},

		// function passed to javascript-sort function for sorting participants with features
		_participantWithFeatureSortingFunction: function(a,b){
			return  this._participantsWithFeatures[b.id].linkedCount -
					this._participantsWithFeatures[a.id].linkedCount;
		},

		// arrange participants by the number of their linked features with the highest value in the middle
		_arrangeByCount: function(features){
			var arranged = new Array(features.length);
			var x = Math.round(features.length/2 - 0.6);
			var right = x + 1;
			var left = x - 1;
			for(var i = 0; i < features.length; i++){
				arranged[x] = features[i];
				if(i%2 == 0){
					x = right;
					right++;
				}else{
					x=left;
					left--;
				}
			}
			return arranged;
		}
    };

    $.widget("ui.Interaction", interactionRepresentation);
})(jQuery);
