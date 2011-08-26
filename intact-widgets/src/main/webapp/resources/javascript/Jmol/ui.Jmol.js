// This widget integrates a Jmol applet in your website.
// You can interact with it through the methods "getSelectedRegion", "reset", "selectProtein"
//											    "selectRegion", "unselectRegion" and "selectPositions"  
(function($){
	var Jmol = {
		options:{
			height: 200, // height of the widget
			width: 1000, // width of the whole widget (applet + control elements)
			jmolFolder: '',
			warningImageUrl: '',
			loadingImageUrl: '',
			proxyUrl: ''
		},
		
		/**
		 * Jmol Variables
		 */
		_jmoljarfile: "JmolApplet.jar",
		
		/**
		 *  Plugin variables
		 */
		_pdb_files_url: 'http://www.ebi.ac.uk/pdbe-srv/view/files/',
		_alignment_url: 'http://www.rcsb.org/pdb/rest/das/pdb_uniprot_mapping/alignment?query=',
		_alignments: [],
		_regionAlignments: [],
		_proteinId: "",
		_id: 'Jmol',
		_regionId: 'region',

        _minStart: Number.MAX_VALUE,
		_maxEnd: 0,


		_selectedStart: undefined,
		_selectedEnd: undefined,
		_selectedPositions: undefined,
		_selectedAlignment: "",
	
		_proteinInformation: new Object(),
		_regionInformation: new Object(),
	
		_jmolInitialized: false,
	
		_displaySurface: undefined,
		_displayPolar: undefined,
		_displayUnpolar: undefined,
		_displayPositive: undefined,
		_displayNegative: undefined,
        _halosOn: undefined,
	
		_create: function(){},
		
		_init: function(){
			widgetElement = this.element;
			this._initializeJmol();
		},
	
		// creates all required html elements and the applet for the widget and adds events to all check boxes and radio buttons
		_initializeJmol: function(url){
			var self = this;
			
			var jmolDivId = "Jmol0";
			var table = '<table style="width:'+ this.options.width +';height:'+ this.options.height +'">'+
				'<tr style="width:100%;height:100%;font-size:12">'+
					'<td style="text-align:left">'+
						'<div id="'+ jmolDivId +'" style="position:relative"/>'+
						'<div id="error" style="position:relative;width:100%;height:100%;vertical-align:text-top"/>' +
					'</td>' +
					'<td style="width:25%;height:100%;text-align:left;vertical-align:text-top" id="informationColumn">'+
						'Available PDB structures<br/><select id="' + this._regionId + '_select">'+
						'<option/></select>' +
						'<br/><span id="interactorInformation" />'+
						'<span id="featureInformation"/><br/><br/>'+
						'<span id="controlSection">' +
							'<b> Control elements </b><br/>' +
							'<input id="surfaceCheck" type="checkbox" name="surfaceCheck" value="surfaceCheck"/> Display Surface<br/><br/>' +
							'<input id="polarCheck" type="checkbox" name="polarCheck" value="polarCheck"/> Display hydrophylic residues<br/><br/>' +
							'<input id="unpolarCheck" type="checkbox" name="unpolarCheck" value="unpolarCheck"/> Display hydrophobic residues<br/><br/>' +
							'<input id="positiveCheck" type="checkbox" name="positiveCheck" value="positiveCheck"/> Display basic(+) residues<br/><br/>' +
							'<input id="negativeCheck" type="checkbox" name="negativeCheck" value="negativeCheck"/> Display acidic(-) residues<br/><br/>' +
                            '<u> Selection </u><br/>' +
                            '<input id="translucentRadio" type="radio" name="selection" value="translucentRadio" checked="translucentRadio"/> Translucent' +
                            '<input id="halosRadio" type="radio" name="selection" value="halosRadio"/> Halos<br/><br/>' +
						'</span>' +	
					'</td>'+
				'</tr>' + 
			'</table>';
			
			$(this.element).html(table);
			if(this.options.loadingImageUrl != ''){
				$('#interactorInformation').html(
					'<br/><image id="loadingImage" alt="Loading..." src="' + this.options.loadingImageUrl + '"/>');	
			}
			
			$('#' + this._regionId + '_select').change(function(){
				self._on_selection_change();
			});
			
			$('#surfaceCheck').click(function(event){
				self._displaySurface = $('#surfaceCheck:checked').val();
				self._colorSurface();
			});
			
			$('#polarCheck').click(function(){
				self._displayPolar = $('#polarCheck:checked').val();
				self._color();
			});
			
			$('#unpolarCheck').click(function(){
				self._displayUnpolar = $('#unpolarCheck:checked').val();
				self._color();
			});
			
			$('#positiveCheck').click(function(){
				self._displayPositive = $('#positiveCheck:checked').val();
				self._color();
			});
			
			$('#negativeCheck').click(function(){
				self._displayNegative = $('#negativeCheck:checked').val();
				self._color();
			});

            $('input[name="selection"]').change(function(){
                self._halosOn = $('#halosRadio:checked').val();
                self._color();
            });

            $('#halosRadio').val('checked');

			jmolInitialize(this.options.jmolFolder, this._jmoljarfile);
			jmolSetDocument(0);
			jmolSetAppletColor("white");
			jmolSetCallback("messageCallback", 'pdbLoadingError');
			
			// can't use "85%" because of wrong display in Google Chrome
			var appletWidth = this.options.width * 0.75;
			
			var url = this.options.proxyUrl + '?url=' + this._pdb_files_url;
			
			$("#" + jmolDivId).html(
				jmolApplet([appletWidth, this.options.height], 'defaultDirectory = "' + url +'"'));
			$('#controlSection').hide();
			$('#informationColumn').hide();
		},
	
		// gives users more information about the currently displayed pdb file
		getSelectedRegion: function(){
			var selectedPdb = this._alignments[$('#' + this._regionId + '_select').val()];
			if (selectedPdb === undefined) {
				return null;
			}
			else {
				return {
					proteinId: selectedPdb[1].intObjectId,
					start: selectedPdb[1].start,
					end: selectedPdb[1].end
				};
			}
		},
		
		// deletes the currently displayed pdb file and all displayed information	
		reset: function(){
			jmolScriptWait('zap');
			$('#informationColumn').hide();
			$('#controlSection').hide();		        
			$("#interactorInformation").empty();
			$("#featureInformation").empty();
		},
	
		// get all pdb files for a given uniprot id
		selectProtein: function(interactorInformation){
			var interactorId = interactorInformation.interactorId;
		    this._selectedStart = undefined;
		    this._selectedEnd = undefined;
			this._selectedPositions = undefined;
			this._unhighlight();
			$('#informationColumn').show();
			$('#featureInformation').empty();
			if(this.options.loadingImageUrl != ''){
				$('#interactorInformation').html(
					'<br/><image id="loadingImage" alt="Loading..." src="' + this.options.loadingImageUrl + '"/>');	
			}
			if (interactorId != this._proteinId) {
				this._minStart = Number.MAX_VALUE;
				this._maxEnd = 0;
				this._selectedAlignment = undefined;
				this._alignments = [];
				this._regionAlignments = [];
				this._proteinId = interactorId;
				this._proteinInformation = interactorInformation;
				this._loadXML();
			}else{
				this._selectedAlignment = $('#' + this._regionId + '_select').val();
				$('#' + this._regionId + '_select').html(this._createSelect(this._alignments));
				$('#' + this._regionId + '_select').val(this._selectedAlignment);
				this._initJmol();
			}
		},
	
		// shows only the pdb files that contain the given region and highlights the atoms in this region    
		selectRegion: function(featureInformation){
			this._regionInformation = featureInformation;
			var start = featureInformation.coordinates.x;
			var end = featureInformation.coordinates.x2;
			this._selectedPositions = undefined;
		    this._selectedAlignment = $('#' + this._regionId + '_select').val();
			if ((start != this._selectedStart) || (end != this._selectedEnd)) {
				this._selectedStart = start;
				this._selectedEnd = end;
				this._regionAlignments = {};
				var i = 0;
				for (al in this._alignments) {
					var uni = this._alignments[al][1];
					if ((start >= uni.start && start <= uni.end) || (end >= uni.start && end <= uni.end) || (start < uni.start && end > uni.end)) {
						this._regionAlignments[this._alignments[al][0].intObjectId] = this._alignments[al];
						i++;
					}
				}
				$('#' + this._regionId + '_select').html(this._createSelect(this._regionAlignments));
				$('#' + this._regionId + '_select').val(this._selectedAlignment);
			}
			this._on_selection_change();
		},
	
		// shows only the pdb files that contain the given positions and highlights the atoms at this positions
		selectPositions: function(featureInformation){
			var positions = featureInformation.coordinates.positionArray;
			this._regionInformation = featureInformation;
			this._selectedStart = undefined;
			this._selectedEnd = undefined;
		    this._selectedAlignment = $('#' + this._regionId + '_select').val();
			if ((this._selectedPositions === undefined) || !this._arraysEqual(positions, this._selectedPositions)) {
				this._selectedPositions = positions;
				this._regionAlignments = {};
				var i = 0;
				for (al in this._alignments) {
					var add = false;
					var uni = this._alignments[al][1];
					$(positions).each(function(){
						if(this > uni.start && this < uni.end){
							add = true;
							return;
						}
					});
					if(add){
						this._regionAlignments[this._alignments[al][0].intObjectId] = this._alignments[al];
					}
				}
				$('#' + this._regionId + '_select').html(this._createSelect(this._regionAlignments));
				$('#' + this._regionId + '_select').val(this._selectedAlignment);
			}
			this._on_selection_change();
		},
	
		// reverts highlighting
		unselectRegion: function(){
	    	this._selectedStart = undefined;
	    	this._selectedEnd = undefined;
			this._selectedPositions = undefined;
			this._unhighlight();
		},
	
		// makes an ajax request to get the pdb files for the given uniprot id
		_loadXML: function(){
			var self = this;
			$.ajax({
				url: this.options.proxyUrl,
				data: 'url=' + self._alignment_url + self._proteinId,
				dataType: 'text/xml',
				success: function(xml){
					self._parse_response(xml);
				},
				async: false,
				error: function(){
					console.log("Error getting xml file.")
				}
			});
		},
	
		// parses the xml file from the request and stores the information in an easy to acces way
		_parse_response: function(xml){
		    this._alignments = {};
		    var i = 0;
			var self = this;
		    $(xml).find('block').each(function(){
		        var chd = $(this).children();
		        var segment0 = self._create_node(chd[0]);
		        var segment1 = self._create_node(chd[1]);
		        
		        var arr = [];
		        arr.push(segment0);
		        arr.push(segment1);
		        self._alignments[segment0.intObjectId] = arr || [];
		        i++;
				
			    if (self._minStart > segment1.start) {
			        self._minStart = segment1.start;
			    }
			    
			    if (self._maxEnd < segment1.end) {
			        self._maxEnd = segment1.end;
			    }
		    });
			this._regionAlignments = this._alignments;
		    this._initJmol();
		},
		
		// creates a node for the datastructur that holds the xml information
		_create_node: function(segment){
			var start = parseInt($(segment).attr('start'));
		    var end = parseInt($(segment).attr('end'));
		    var obj = {
		        intObjectId: $(segment).attr('intObjectId') || '',
		        start: start || '',
		        end: end || ''
		    }
		    return obj;
		},
		
		// adds all available pdb files to a dropdown box 
		// or displays the fact that there are no pdb files for the given id
		_initJmol: function(){
			var self = this;
		    if (!this._isEmptyObject(this._alignments)) {
				$('#Jmol0').show();
				$('#error').hide();
				$('#' + this._regionId + '_select').html(this._createSelect(this._alignments));
				this._selectedAlignment = $('#' + this._regionId + '_select').val();			
		        this._on_selection_change();
		    } else {
				$(document).trigger('pdb_selected');
				$('#controlSection').hide();
				$('#informationColumn').hide();
				$('#Jmol0').hide();
				$('#error').show();
				
				var errorMessage = '<span style="color:gray;">';
				if(this.options.warningImageUrl != ''){
					errorMessage += '<img id="warningImage" src="'+ this.options.warningImageUrl +'"/>';
				}
				
				if (this._proteinInformation.interactorName === undefined) {
					errorMessage += 'No structural information available for ' + this._proteinInformation.interactorId + '</span>';
				}
				else {
					errorMessage += 'No structural information available for ' + this._proteinInformation.interactorName + '</span>';
				}
		        $("#error").html(errorMessage);
		    }
		},
		
		// creates a drop-down box with the help of the given datastructure
		// is displays the name and the covered uniprot region
		_createSelect: function(curAlignments){
		    var sel_arr = [];
		    var i = 0;
		    for (var al in curAlignments) {
				var text = al + " (" + curAlignments[al][1].start + " - " + curAlignments[al][1].end + ")";
		        sel_arr[i] = '<option value=' + text + '>' + text + '</option>';
		        i++;
		    }
		    return sel_arr.join('');
		},
		
		// function that is called if the value of the dropdown box is changed
		// checks whether there are available pdb files 
		// if not displays this fact
		// if yes: parses the value of the dropdown box and set the pdb file 
		_on_selection_change: function(){
			// if no region/positions are defined
			// or region/positions are defined and there are pdb files for this region available
			if (  ((this._selectedStart === undefined && 
					this._selectedEnd === undefined) && this._selectedPositions === undefined)
					
					|| !this._isEmptyObject(this._regionAlignments)) {
				
				$('#featureInformation').empty();
				if(this.options.loadingImageUrl != ''){
					$('#interactorInformation').html(
						'<br/><image id="loadingImage" alt="Loading..." src="' + this.options.loadingImageUrl + '"/>');	
				}		
						
				var pdb_id = $('#' + this._regionId + '_select').val();
				
				var selected_pdb = pdb_id.toLowerCase();
				selected_pdb = selected_pdb.substring(0, selected_pdb.indexOf('.'));
				
				var tmp_arr = this._alignments[pdb_id] || [];
				var log_msg = '';
				
				var start = tmp_arr[1].start;
				var end = tmp_arr[1].end;

				this._setPdb(selected_pdb);
			}
		    else {
				if (!this._isEmptyObject(this._alignments)) {
					$(document).trigger('pdb_selected', null);
					$("#interactorInformation").empty();
					$("#featureInformation").empty();
					this._proteinId = undefined;
					$('#Jmol0').hide();
					$('#error').show();
					var errorMessage = '<span style="color:gray;">';
					if (this.options.warningImageUrl != '') {
						errorMessage += '<img id="warningImage" src="' + this.options.warningImageUrl + '"/>';
					}
					errorMessage += 'No structural information available for this region</span>';
					$("#error").html(errorMessage);
				}
		    }
		},
		
		// sets the "to be displayed" pdb file and displays information about it
		// also triggers the event that a new pdb file was selected
		_setPdb: function(selected_pdb){
			var pdb_id = this._alignments[$('#' + this._regionId + '_select').val()];
			$(document).trigger('pdb_selected', {proteinId: pdb_id[1].intObjectId,
												 start: pdb_id[1].start,
												 end: pdb_id[1].end
												}
								);
			
			var scr = 'load ' + selected_pdb  + '.pdb;';
			
			if(Number(this.options.width) > Number(this.options.height)){
				scr += 'zoom 60;';
			}
	
			jmolScriptWait(scr);
			this._color();
			
			var informationText = '<br/><u>Displayed protein:</u>' +
								  '<br/>Accession: ' + this._proteinInformation.interactorId + '<br/>';
			
			if(!(this._proteinInformation.interactorName === undefined)){
				informationText += 'Name: '+ this._proteinInformation.interactorName + '<br/>';
			}
			
			$('#interactorInformation').html(informationText);
			$('#controlSection').show();
			if (!(this._selectedStart === undefined || this._selectedEnd === undefined)) {
				this._on_selection(null, {
					start: this._selectedStart,
					end: this._selectedEnd
				});
			}
			if(!(this._selectedPositions === undefined)){
				this._on_selection_position(null, {
		            positions: this._selectedPositions
		        });
			}
		},
		
		// on the ebi page there is a redirect for deprecated pdb files
		// if this happens the value of the redirect is parsed and loaded
		pdbLoadingError: function(applet, message){
		    var message = '' + message;
		    if (message.indexOf("script ERROR: unrecognized file format") != -1) {
		        var index = message.indexOf("<a href=");
		        if (index != -1) {
		            var link = message.substring(index + 9, message.indexOf(">", index) - 1);
		            index = link.lastIndexOf("/") + 1;
		            var selected_pdb = link.substring(index, link.lastIndexOf('.'));
		            this._setPdb(selected_pdb);
		        }
		    }
		},
		
		// reverts the highlighting of a region
		_unhighlight: function(){
            var scr = 'select all; ';
            if(this._halosOn === undefined){
                scr += 'color translucent 1; ';
            }else{
                scr += 'selectionHalos off; ';
            }
            scr += 'select none;'

			jmolScriptWait(scr);
		},
		
		// function that highlights the atoms in the given region
		// has the same interface as in Dasty
		_on_selection: function(e, params){
		    var start = parseInt(params.start);
		    var end = parseInt(params.end);
		    var align = this._alignments[$('#' + this._regionId + '_select').val()];
		    var uni = align[1];
		    if ((start >= uni.start && start <= uni.end) || (end >= uni.start && end <= uni.end) || 
				(start < uni.start && end > uni.end)) {
		        if (start < uni.start) {
		            start = uni.start;
		        }
		        if (end > uni.end) {
		            end = uni.end;
		        }
		        var pdb = align[0];
		        var amin = end - start;
		        start = start - uni.start + pdb.start;
		        end = start + amin;
		        var scr = this._getSelectionScript(start + ' - ' + end);
		        jmolScriptWait(scr);
				
				var highlightedStart = 0;
				var highlightedEnd = 0;
				
				if(uni.start > this._selectedStart){
					highlightedStart = uni.start;
				}else{
					highlightedStart = this._selectedStart;
				}
				
				if(uni.end < this._selectedEnd){
					highlightedEnd = uni.end;
				}else{
					highlightedEnd = this._selectedEnd;
				}
				
				$('#informationColumn').show();

                var infoText = '<br/><br/><u>Selected region:</u><br/>Requested: ';
                if(this._selectedStart == this._selectedEnd){
                    infoText += this._selectedStart;
                }else{
                    infoText += (this._selectedStart + '..' + this._selectedEnd);
                }
                infoText +='</br>Highlighted: ';
                if(highlightedStart == highlightedEnd){
                    infoText += highlightedStart;
                }else{
                    infoText += (highlightedStart + '..' + highlightedEnd);
                }
                $('#featureInformation').html(infoText);
		    }
		    else {
		        this._unhighlight();
		    }
		},
		
		// function that highlights the atoms at the given positions
		_on_selection_position: function(e, params){
		    var align = this._alignments[$('#' + this._regionId + '_select').val()];
		    var uni = align[1];
			
			var arr = '';
			var highlightedArr = '';
			for (var i = 0; i < params.positions.length; ++i) {
				arr += params.positions[i] + ', ';
				if(params.positions[i] > uni.start && params.positions[i] < uni.end){
					highlightedArr += params.positions[i] + ', ';
				}
			}
			arr = arr.substring(0, arr.lastIndexOf(','));
			highlightedArr = highlightedArr.substring(0, highlightedArr.lastIndexOf(','));
			var scr = this._getSelectionScript(highlightedArr);
		    jmolScriptWait(scr);
			$('#featureInformation').html('<br/><br/><u>Selected Positions:</u><br/>' +
												'Requested: ' + arr + '<br/>' +
												'Highlighted: ' + highlightedArr);
		},
		
		// highlight given region
		_getSelectionScript: function(positionText){
			var scr;
            if(this._halosOn === undefined){
                scr = 'select not ' +	positionText + '; color translucent 0.7; selectionHalos off';
            }else{
                scr = 'select ' + positionText + '; color translucent 1; selectionHalos on; ';
            }
			return scr;		
		},


        // colors the surface of the structure
		_colorSurface: function(){
			var scr;
			if (this._displaySurface !== undefined) {
				scr = 'select all; isoSurface solvent 1.4; color isoSurface translucent 0.8;select none';
			}else{
				scr = 'select all; isoSurface off; select none';
			}
			jmolScriptWait(scr);
		},


        // colors the structure depending on the checked check boxes
		_color: function(){
			var scr = 'select all; cartoon on; wireframe off; spacefill off;';
			
			if(this._displayNegative || this._displayPositive || this._displayPolar || this._displayUnpolar){
				scr += 'color lightgrey;';
			}else{
				scr += 'color chain;';
			}
			if(this._displayPositive !== undefined){
				scr += 'select basic;color blue;select all;';
			}
			if(this._displayNegative !== undefined){
				scr += 'select acidic;color red;select all;';
			}
			if(this._displayPolar !== undefined){
				scr += 'select polar;color cyan;select all;'
			}
			if(this._displayUnpolar !== undefined){
				scr += 'select hydrophobic;color salmon;select all;'
			}
			scr += 'select none';

            jmolScriptWait(scr);
            if(this._selectedStart !== undefined){
                this._on_selection(null, {
					start: this._selectedStart,
					end: this._selectedEnd
				});
            }

            if(this._selectedPositions !== undefined){
                 this._on_selection_position(null, {
		            positions: this._selectedPositions
		        });
            }
		},
		
		// check whether an object is empty
		_isEmptyObject: function(obj){
		    for (var i in obj) {
		        if (obj.hasOwnProperty(i)) {
		            return false;
		        }
		    }
		    return true;
		},
		
		// expects sorted arrays
		_arraysEqual: function (array1, array2){
			if (array1.length != array2.length) { return false; }
			for(var i = 0; i < array1.length; ++i){
				if(array1[i] != array2[i]){
					return false;
				}
			}	
			return true;
		}

	};
	$.widget("ui.Jmol", Jmol);
	
})(jQuery);

// on the ebi page there is a redirect for deprecated pdb files
// if this happens the value of the redirect is parsed and loaded
// since this method is called by the Jmol applet it has to be a globally accessable method
var widgetElement;

function pdbLoadingError(applet, message){
	widgetElement.Jmol('pdbLoadingError', applet, message);
}