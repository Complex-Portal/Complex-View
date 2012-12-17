/**
 * This function starts all the dasty processes. 
 * It is mandatory to be included in the HTML response.
 * ej: <body onload="start_dasty(1);">
 * @param url_control choose between 3 different ways of start Dasty2
 * 0: 	Makes Dasty2 independent from the URL. You need to tell dasty2 what default configuration values you want to use.
 * 		Use set_query to define them ... set_query('P05067', 'BioSapiens', 3, 'index.html'). 
 * 		Recommended option if you embed dasty2 in your own web site.
 * 1:	Dasty2 check first the attributes from the URL. Recomended!
 * 2:	Use it for redirections
 */
function start_dasty(url_control) {
    jQuery.noConflict();
    var dpath = getURLParam('path');

    if (dpath != null) {
        set_dasty_path(dpath);
    }

	dasty_url_control = url_control;
	query_id_null = 0; // "0" means that query ID is not null
	if(url_control == 0) {
		start_globals();
	} else if(url_control == 1) {
		start_url();
	} else {
		DastyRedirector();
	}
}

var dasty_path;

function set_dasty_path(path) {
    dasty_path = path;
}

/**
 * Use this function in body load when start_dasty(0) and when the serach module is hidden or off.
 * onLoad="set_query('P05067', 'BioSapiens', 3), start_dasty(0);"
 * @param q_id Id to query (uniprot id)
 * @param q_label Each DAS - source can be assigned one or more Labels. This is currently used to describe by which project a DAS source is provided. 
 * @param q_timeout Time that dasty will wait for each server's answer
 * @param q_template The template for the look and feel of the response HTML(biosapiens,interactor, index) 
 */
function set_query(q_id, q_label, q_timeout, q_template) {
	query_id = "";
	filterLabel = "";
	timeout = 3;
	dasty_mainpage_name = "";
	
	default_query_parameters();
	
	if( q_id == null || q_id == "") { query_id = default_query_id; } else { query_id = q_id; };
	if( q_label == null || q_label == "") { filterLabel = default_filterLabel; } else { filterLabel = q_label; };
	if( q_timeout == null || q_timeout == "") { timeout = default_timeout; } else { timeout = q_timeout; };
	if( q_template == null || q_template == "") { dasty_mainpage_name = default_dasty_mainpage_name; } else { filterLabel = template; };
	
}

/**
 * Start Dasty2 using the parameters in the URL
 */
function start_url() {	
	query_id = "";
	filterLabel = "";
	timeout = 3;	
	
	dasty_mainpage_name = findDastyHtmlPageName('page_name') + "." + findDastyHtmlPageName('ext_name');
	configuration();
	default_query_parameters();
	
	setDastyURLParam();
}

/**
 * Initialize all the necessary Global variables used by Dasty2
 */
function start_globals() {	
	MM_preloadImages("img/blast_icon.gif", "css/window/mac_os_x/B.png", "css/window/mac_os_x/B_Main.png", "css/window/mac_os_x/BL.png", "css/window/mac_os_x/BL_Main.png", "css/window/mac_os_x/BR.png", "css/window/mac_os_x/BR_Main.png", "css/window/mac_os_x/close.gif", "css/window/mac_os_x/L.png", "css/window/mac_os_x/L_Main.png", "css/window/mac_os_x/maximize.gif", "css/window/mac_os_x/minimize.gif", "css/window/mac_os_x/R.png", "css/window/mac_os_x/R_Main.png", "css/window/mac_os_x/T.png", "css/window/mac_os_x/T_Main.png", "css/window/mac_os_x/TL.png", "css/window/mac_os_x/TL_Main.png", "css/window/mac_os_x/TR.png", "css/window/mac_os_x/TR_Main.png");
	
	ontologyPrefix = ["GO:", "BS:", "ECO:", "MOD:", "SO:"];
	
	browser_name = navigator.appName;
	browser_version = navigator.appVersion;
	
	dasty_mainpage_name = findDastyHtmlPageName('page_name') + "." + findDastyHtmlPageName('ext_name');
	configuration();
	default_query_parameters();
	
	if (typeof query_id == "undefined") { query_id = default_query_id; };
	if (typeof filterLabel == "undefined") { filterLabel = default_filterLabel; };
	if (typeof timeout == "undefined") { timeout = default_timeout; };
	
	checkSectionIcons();
	checkMoColumnIcons();
	


//------------------------------------------------------------------------------------------	
//  NECESSARY GLOBAL VARIABLES USED BY DASTY2
//------------------------------------------------------------------------------------------	
	
	dasty2 = new Object;
	
	// CREATED_IN: ajax.js 				// USED_IN: parse_sequence.js, parse_sequece.js, parse_serverlist.js, parse_stylesheet.js, request_xml.js
	// CREATED_IN: request_xml.js		// USED_IN: parse_feature.js
	
	feature_info = [];
	// CREATED_IN: parse_feature.js		// USED_IN: create_graphic.js, create_feature_details.js
	
	annotation_version = [];
	
	stylesheet_properties_info = [];
	// CREATED_IN: parse_stylesheet.js 	// USED_IN: create_graphics.js

	//type_counter = [];
	// CREATED_IN: parse_feature.js 	// USED_IN: create_graphic.js
	
	//category_counter = [];
	// CREATED_IN: parse_features.js 	// USED_IN: No yet!!!
	
	new_feature_list_info = [];
	new_feature_list_info2 = [];
	// CREATED_IN: reorganize_types.js		// USED_IN: create_graphic.js 

	sequence = '';
	// CREATED_IN: parse_sequence.js	// USED_IN: functions.js => highlightSequence()
	
	//sequence_info = [];
	sequence_info = new Object();
	
	selStart = 1; // stores selection starting position
	useHighlight = true;  // if true, sequence will be transformed to support highlight technique
	// CREATED_IN: parse_sequence.js	// USED_IN:
	
	//das_server = '';
	// CREATED_IN: request_features.js 	// USED_IN: parse_feature.js
	
	//featureXML_num = -1;
	
	// CREATED_IN: request_features.js	// USED_IN: request_xmls.js, parse_feature.js
	
	bg_color = 1;
	// CREATED_IN: global.js			// USED_IN: create_graphic.js

	one_feature_list = [];
	
	expanded_feature_list = [];
	
	sortOrderType = 0;
	sortOrderMethod = 0;
	sortOrderCategory = 0;
	sortOrderServer = 0;
	sortOrderVersion = 0;
	
	zoom_start = 0;
	zoom_end = 0;
	
	
	show_graphic_tittle_temp = 1; // boolean 0/1. 0=no / 1=yes
	show_scale_bar_temp = 1;  // boolean 0/1. 0=no / 1=yes
	show_slide_bar_temp = 1;
	
	features_row_number = 0;
	
	width_div_graphic_correction = 0;
	
	bar_aa_distance_list = [];
	
	bar_px_distance_list = [];
	
	isExpanded = 0; // boolean 0/1. 0=no / 1=yes
	

	
	
	//display = "free";
	//standby = [];
	count_displayed_groups = 0;
	
	elements_http_request = [];
	
	//das_registry_label = [];
	
	progress_bar_width = getPxWidthFromStyle("progress_bar_empty");
	
	servers_count = 0;
	
	server_checking = "";
	
	warning_count = 0;
	no_feature_results_count = 0;
	feature_results_count = 0;
	
	results_XML_order = [];
	
	popup_num = 0;
	
	
	
	dasty2.loadedDasSources = [];
	dasty2.excludedDasSources = [];
	dasty2.loadedDasSources_temp = [];
	
	dasty2.firstRequestNumber = 0;
	dasty2.validFirstSources = [];
	
	dasty2.typesLoaded = []; // It seems it is not beeing used. Try "dasty2.typesLoaded2".
	dasty2.categoriesLoaded = [];
	dasty2.IdlinesPerType = [];
	dasty2.IdlinesPerCategory =[];
	dasty2.IdlinesPerServer = [];
	dasty2.temporalTree =[];
	dasty2.temporalArray =[];
	
	dasty2.line_id_name2 = new Array(); // Hash
	dasty2.line_id_name2_length = 0;
	
	dasty2.countVisibleLines = 0;
	
	dasty2.firstTimeSortByType = false;
	


	dasty2.typesLoaded2 = [];
	dasty2.countIdType = [];

	dasty2.countNPF = 0;
	
	dasty2.decor_tr_npf = 0;
	
	
	//PDB CHAIN
	structureChain="";
	segmentUniprot=null;
	segmentPDB=null;
	


//------------------------------------------------------------------------------------------	
// START APPLICATION
//------------------------------------------------------------------------------------------
	
	document.getElementById("system_information").innerHTML = "... please be patient, Dasty2 is loading data from the DAS servers";
	document.getElementById("progress_bar_1").innerHTML = "";
 	document.getElementById("progress_bar_2").style.width = 0;
	document.getElementById('feature_id_box').value = query_id;
	document.getElementById("graphic_width_px").value = graphic_width;
	filterLabel = filterLabel.replace(/%20/g, " ")
	
	//myDasty.init();
	start();
	
	
  } // function start_globals()
  
/**
 * Verify what kind of registry has been selected(local or external) and start its proccess.
 */
function start() { 
	if(use_das_registry == true) {
		makeDasRegistryRequest();
	} else {
		das_registry_label = [];
		das_registry_label.push("any");
		createLabelOptions(das_registry_label, "feature_label_list_select");
		makeSequenceRequest();
	}
    startBiojsHpa();
}	
