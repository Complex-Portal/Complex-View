// JavaScript Document

/**
*  CONFIGURATION VARIABLES
*/

function configuration()
{
	/**
	*  TEMPLATES VARIABLES
	*/
	if(dasty_mainpage_name == "ebi.php")
		{
			/**
			* VARIABLES FOR THE EBI TEMPLATE
			*/
				isPDBVisible=true;
				isOntologyTreeVisible=true;
				collapsedOntologyTypeTerms = ['binding_site','polypeptide_region','polypeptide_sequencing_information'];

				sequence_limit = 110; // Define the "number" of aa per line in the sequence view. 75

				graphic_width = 1100; //1100
				height_graphic_feature = 7; // Height for the features shown in the graphic.
				tittle_height = height_graphic_feature + 6;

				col_category_width = 170;
				col_type_width = 190;
				col_method_width = 110;
				col_id_width = 110; //110
				col_warning_width = 26;
				col_server_width = 110;

				// Positional features columns -> 0=no / 1=yes
				show_col_category = 1;
				show_col_type = 1;
				show_col_method = 0;
				show_col_id = 1; //label
				show_col_graphic = 1;
				show_col_warning = 1;
				show_col_server = 1;

				// Non positional features columns -> 0=no / 1=yes
				show_col_category_npf = 1;
				show_col_type_npf = 1;
				show_col_method_npf = 0;
				show_col_label_npf = 1;
				show_col_note_npf = 1;
				show_col_server_npf = 1;
				show_col_score_npf = 0;
				show_col_version_npf = 1;
				show_col_featureid_npf = 0;

				// 0=no / 1=yes
				show_graphic_tittle = 1;
				show_scale_bar = 1;
				show_slide_bar =1;
				show_popup =2;

				// boolean 0/1. 0=no / 1=yes
				color_line_background = 0;

				vertical_bars = 12;

				// If true it allows to group features from the same source with the have the same ID
				dasty2_grouping = false;

				// Do you want to exclude DAS sources from the visuzlization?
				excluded_das_sources = []; // No
				//excluded_das_sources = ['netphos', 'tmhmm']; // Yes, Netphos and Tmhmm
				excluded_das_sources = ['CATH Structural Domains in UniProt', 'cath_uniprot_mapping', 'cbs_ptm', 'cbs_func', 'cbs_sort', 'PDBsum_protprot', 'PDBsum_ligands', 'PDBsum_DNAbinding', 'UniProt Tryptic Peptides', 'UniProt GO Annotation', 'uniprot aristotle'];


				// Do you want to load at the beggining one specific DAS source?
				first_das_source = []; // No
				//first_das_source = ['pride', 'uniprot']; // Yes, Pride and Uniprot following this order.

				// Insert all the columns avaliable in the order required ( show or hide each column with show_col_xxx_npf = 0/1 )
				non_positional_features_coulmns = ["type_id", "method_data", "feature_label", "feature_id", "note_data", "link_data", "version", "score_data", "annotation_server", "type_category"];

				//non_positional_feature_table_width= "98%";
				//non_positional_feature_table_width= "900px";

				use_das_registry = true;

		}
	else if(dasty_mainpage_name == "uniprot.php")
		{
			/**
			* VARIABLES FOR THE UNIPROT BETA TEMPLATE
			*/
				isPDBVisible=true;
				isOntologyTreeVisible=true;
				collapsedOntologyTypeTerms = ['binding_site','polypeptide_region','polypeptide_sequencing_information'];

				sequence_limit = 110; // Define the "number" of aa per line in the sequence view. 75

				graphic_width = 770;
				height_graphic_feature = 7; // Height for the features shown in the graphic.
				tittle_height = height_graphic_feature + 6;

				col_category_width = 170;
				col_type_width = 200;
				col_method_width = 110;
				col_id_width = 110;
				col_warning_width = 26;
				col_server_width = 140;

				// boolean 0/1. 0=no / 1=yes
				show_col_category = 0;
				show_col_type = 1;
				show_col_method = 0;
				show_col_id = 1;
				show_col_graphic = 1;
				show_col_warning = 1;
				show_col_server = 1;

				// Non positional features columns -> 0=no / 1=yes
				show_col_category_npf = 1;
				show_col_type_npf = 1;
				show_col_method_npf = 1;
				show_col_label_npf = 0;
				show_col_note_npf = 1;
				show_col_server_npf = 1;
				show_col_score_npf = 0;
				show_col_version_npf = 0;
				show_col_featureid_npf = 1;

				// boolean 0/1. 0=no / 1=yes
				show_graphic_tittle = 1;
				show_scale_bar = 1;
				show_slide_bar =1;
				show_popup =2;

				// boolean 0/1. 0=no / 1=yes
				color_line_background = 0;

				vertical_bars = 12;

				// If true it allows to group features from the same source with the have the same ID
				dasty2_grouping = false;

				// Do you want to exclude DAS sources from the visuzlization?
				//excluded_das_sources = []; // No
				//excluded_das_sources = ['netphos', 'tmhmm']; // Yes, Netphos and Tmhmm
				excluded_das_sources = ['uniprot', 'CATH Structural Domains in UniProt', 'cath_uniprot_mapping', 'cbs_ptm', 'cbs_func', 'cbs_sort', 'PDBsum_protprot', 'PDBsum_ligands', 'PDBsum_DNAbinding', 'UniProt Tryptic Peptides', 'UniProt GO Annotation', 'uniprot aristotle'];


				// Do you want to load at the beggining one specific DAS source?
				first_das_source = []; // No
				//first_das_source = ['pride', 'uniprot']; // Yes, Pride and Uniprot following this order.
				//first_das_source = ['signal'];

				// Insert all the columns avaliable in the order required ( show or hide each column with show_col_xxx_npf = 0/1 )
				non_positional_features_coulmns = ["type_id", "feature_label", "score_data", "version", "method_data", "link_data", "feature_id", "note_data", "annotation_server", "type_category"];

				//non_positional_feature_table_width= "98%";
				//non_positional_feature_table_width= "900px";

				use_das_registry = true;

		}
	else if(dasty_mainpage_name == "biosapiens.html")
		{
			/**
			* VARIABLES FOR THE BIOSAPIENS TEMPLATE
			*/
				isPDBVisible=true;
				isOntologyTreeVisible=true;
				collapsedOntologyTypeTerms = ['binding_site','polypeptide_region','polypeptide_sequencing_information'];

				sequence_limit = 110; // Define the "number" of aa per line in the sequence view. 75

				graphic_width = 900;
				height_graphic_feature = 7; // Height for the features shown in the graphic.
				tittle_height = height_graphic_feature + 6;

				col_category_width = 170;
				col_type_width = 200;
				col_method_width = 110;
				col_id_width = 110;
				col_warning_width = 26;
				col_server_width = 140;

				// boolean 0/1. 0=no / 1=yes
				show_col_category = 0;
				show_col_type = 1;
				show_col_method = 0;
				show_col_id = 0;
				show_col_graphic = 1;
				show_col_warning = 1;
				show_col_server = 1;

				// Non positional features columns -> 0=no / 1=yes
				show_col_category_npf = 1;
				show_col_type_npf = 1;
				show_col_method_npf = 1;
				show_col_label_npf = 1;
				show_col_note_npf = 1;
				show_col_server_npf = 1;
				show_col_score_npf = 0;
				show_col_version_npf = 1;
				show_col_featureid_npf = 0;

				// boolean 0/1. 0=no / 1=yes
				show_graphic_tittle = 1;
				show_scale_bar = 1;
				show_slide_bar =1;
				show_popup =2;

				// boolean 0/1. 0=no / 1=yes
				color_line_background = 0;

				vertical_bars = 8;

				// If true it allows to group features from the same source with the have the same ID
				dasty2_grouping = false;

				// Do you want to exclude DAS sources from the visuzlization?
				excluded_das_sources = []; // No
				//excluded_das_sources = ['netphos', 'tmhmm']; // Yes, Netphos and Tmhmm
				excluded_das_sources = ['CATH Structural Domains in UniProt', 'cath_uniprot_mapping', 'cbs_ptm', 'cbs_func', 'cbs_sort', 'PDBsum_protprot', 'PDBsum_ligands', 'PDBsum_DNAbinding', 'UniProt Tryptic Peptides', 'UniProt GO Annotation', 'uniprot aristotle'];


				// Do you want to load at the beggining one specific DAS source?
				first_das_source = []; // No
				//first_das_source = ['pride', 'uniprot']; // Yes, Pride and Uniprot following this order.
				//first_das_source = ['signal'];

				// Insert all the columns avaliable in the order required ( show or hide each column with show_col_xxx_npf = 0/1 )
				non_positional_features_coulmns = ["type_id", "method_data", "feature_label", "feature_id", "note_data", "link_data", "version", "score_data", "annotation_server", "type_category"];


				//non_positional_feature_table_width= "98%";
				//non_positional_feature_table_width= "900px";

				use_das_registry = true;
		}
	else
		{
						/**
			* VARIABLES FOR THE EBI TEMPLATE
			*/
				isPDBVisible=true;
				isOntologyTreeVisible=true;
				collapsedOntologyTypeTerms = ['binding_site','polypeptide_region','polypeptide_sequencing_information'];

				sequence_limit = 110; // Define the "number" of aa per line in the sequence view. 75

				graphic_width = 1100; //1100
				height_graphic_feature = 7; // Height for the features shown in the graphic.
				tittle_height = height_graphic_feature + 6;

				col_category_width = 170;
				col_type_width = 190;
				col_method_width = 110;
				col_id_width = 110; //110
				col_warning_width = 26;
				col_server_width = 110;

				// Positional features columns -> 0=no / 1=yes
				show_col_category = 1;
				show_col_type = 1;
				show_col_method = 0;
				show_col_id = 1; //label
				show_col_graphic = 1;
				show_col_warning = 1;
				show_col_server = 1;

				// Non positional features columns -> 0=no / 1=yes
				show_col_category_npf = 1;
				show_col_type_npf = 1;
				show_col_method_npf = 0;
				show_col_label_npf = 1;
				show_col_note_npf = 1;
				show_col_server_npf = 1;
				show_col_score_npf = 0;
				show_col_version_npf = 1;
				show_col_featureid_npf = 0;

				// 0=no / 1=yes
				show_graphic_tittle = 1;
				show_scale_bar = 1;
				show_slide_bar =1;
				show_popup =2;

				// boolean 0/1. 0=no / 1=yes
				color_line_background = 0;

				vertical_bars = 12;

				// If true it allows to group features from the same source with the have the same ID
				dasty2_grouping = false;

				// Do you want to exclude DAS sources from the visuzlization?
				excluded_das_sources = []; // No
				//excluded_das_sources = ['netphos', 'tmhmm']; // Yes, Netphos and Tmhmm
				excluded_das_sources = ['CATH Structural Domains in UniProt', 'cath_uniprot_mapping', 'cbs_ptm', 'cbs_func', 'cbs_sort', 'PDBsum_protprot', 'PDBsum_ligands', 'PDBsum_DNAbinding', 'UniProt Tryptic Peptides', 'UniProt GO Annotation', 'uniprot aristotle'];


				// Do you want to load at the beggining one specific DAS source?
				first_das_source = ['UniProt','InterPro','PRIDE','IntAct']; // No
				//first_das_source = ['pride', 'uniprot']; // Yes, Pride and Uniprot following this order.

				// Insert all the columns avaliable in the order required ( show or hide each column with show_col_xxx_npf = 0/1 )
				non_positional_features_coulmns = ["type_id", "method_data", "feature_label", "feature_id", "note_data", "link_data", "version", "score_data", "annotation_server", "type_category"];

				//non_positional_feature_table_width= "98%";
				//non_positional_feature_table_width= "900px";

				use_das_registry = false;

		}

	/** Column index for non-positional panel --> [ name_column ] = index */
	non_positional_features_columns_index = new Object();
	for ( var i = 0; i < non_positional_features_coulmns.length; i++)
		non_positional_features_columns_index [ non_positional_features_coulmns[i] ] = i;


	/**
	* DAS SOURCES VARIALES
	*/
		stylesheet_url = [];
		sequence_url = [];
		feature_url = [];

		/**
		* PROXY
		*/
			/**
			* PHP PROXY
			*/
				//proxy_url = '../server/proxy.php';
			/**
			* CGI PROXY
			*/
				//proxy_url = 'http://www.ebi.ac.uk/cgi-bin/dasty/proxy.cgi';
				//proxy_url = 'http://www.ebi.ac.uk/~rafael/cgi-bin/proxy.cgi';
				//proxy_url = 'http://localhost/cgi-bin/proxy.cgi';
				//proxy_url = 'http://wwwdev.ebi.ac.uk/cgi-bin/dasty/proxy.cgi';


			/* Java PROXY
			*/
				proxy_url = './das.dasProxy';

        /**
		* STYLESHEET
		*/
			stylesheet_url[0] = ['uniprot', proxy_url + '?t=' + timeout + '&m=stylesheet&s=http://www.ebi.ac.uk/das-srv/uniprot/das/uniprot/'];
			//stylesheet_url[0] = ['uniprot', 'files/stylesheet.xml']; // LOCAL STYLESHEET

		/**
		* REFERENCE SERVER
		*/
			sequence_url[0] = ['uniprot', proxy_url + '?t=' + timeout + '&m=sequence&q=' + query_id + '&s=http://www.ebi.ac.uk/das-srv/uniprot/das/uniprot/']; // UNIPROT REFERENCE SERVER
			//sequence_url[0] = ['uniprot', 'files/seq_A4_Human_uniprot02.xml']; // LOCAL SEQUENCE (just for testing purposes)
			//if(dasty_mainpage_name.indexOf("interactor") != -1)
			//	{
			//		sequence_url[0] = ['uniprot', proxy_url + '?t=' + timeout + '&m=sequence&q=' + query_id + '&s=http://wwwdev.ebi.ac.uk/dgi/das-dgi/das/dgi/'];
			//	}

		/**
		* ANNOTATION SERVERS
		*  - If use_das_registry = false please Set specific DAS annotation servers
		*/

			if(use_das_registry == true)
				{
					/**
					* ANNOTATIONS FROM THE DAS REGISTRY
					*/
						das_registry_url = proxy_url + '?t=' + timeout + '&m=registry&c=protein%20sequence&a=UniProt&s=http://www.dasregistry.org/das1/sources';
						//das_registry_url = proxy_url + '?m=registry&s=http://das.sanger.ac.uk/registry/das1/sources/';
						//das_registry_url = 'files/dasregistry250508.xml'; // LOCAL REGISTRY

							// Exceptions. For example ...
//							if(dasty_mainpage_name.indexOf("interactor") != -1)
//								{
//									das_registry_url = 'files/dasregistry020208.xml';
//								}
				}
			else
				{
					/**
					* SPECIFIC DAS ANNOTATION SERVERS
					*/
						feature_url_prefix = proxy_url + '?m=features&q=' + query_id + '&t=' + timeout + '&s=';
                    feature_url[0] = {id : 'IntAct', url : feature_url_prefix + 'http://www.ebi.ac.uk/intact/das/intact/'};
                    feature_url[1] = {id : 'UniProt', url : feature_url_prefix + 'http://www.ebi.ac.uk/das-srv/uniprot/das/uniprot/'};
                    feature_url[2] = {id : 'InterPro', url : feature_url_prefix + 'http://www.ebi.ac.uk/das-srv/interpro/das/InterPro/'};
                    feature_url[3] = {id : 'PRIDE', url : feature_url_prefix + 'http://www.ebi.ac.uk/pride-das/das/PrideDataSource/'};
                    //feature_url[3] = {id : 'chebi', url : feature_url_prefix + '${das.annotationserver.chebi.url}'};
						//feature_url[1] = {id : 'msdmotif', url : feature_url_prefix + 'http://www.ebi.ac.uk/msd-srv/msdmotif/das/s3dm/'};
						//feature_url[2] = {id : 'netphos', url : feature_url_prefix + 'http://genome.cbs.dtu.dk:9000/das/netphos/'};
						//feature_url[3] = {id : 'uniprot2', url : feature_url_prefix + 'http://tc-test-1.ebi.ac.uk:8113/tc-test/proteomics/das-srv/uniprot/das/uniprot/'};
						//feature_url[4] = {id : 'interpro', url : feature_url_prefix + 'http://www.ebi.ac.uk/das-srv/interpro/das/InterPro/'};
                    //feature_url[1] = {id : 'intact-S4', url : feature_url_prefix + 'http://www.ebi.ac.uk/enfin-srv/s4-das-srv/das/intact-s4/'};



					/**
					* SPECIFIC DAS ANNOTATION SERVERS. LOCAL COPIES.
					*/
						//feature_url[1] = {id : 'uniprot', url : 'files/fea_A4_Human_uniprot03.xml'};
                        //feature_url[0] = {id : 'intact3', url : 'files/fea_P04637_intact.xml'};
                        //feature_url[1] = {id : 'info', url : 'files/info.xml'};

						//feature_url[1] = {id : 'cbs_total', url : 'files/fea_A4_Human_cbs_total.xml'};
						//feature_url[2] = {id : 'netphos', url : 'files/fea_A4_Human_netphos.xml'};
						//feature_url[3] = {id : 'intact', url : 'files/intact_EBI-466029.xml'};
					//	if(dasty_mainpage_name.indexOf("interactor") != -1)
					//		{
					//			feature_url_prefix = proxy_url + '?m=features&q=' + query_id + '&t=' + timeout + '&s=';
					//			feature_url[0] = {id : 'intact', url : feature_url_prefix + 'http://wwwdev.ebi.ac.uk/dgi/das-intact/das/intact'};
					//		}
				}

			/**
			* ALIGNMENTS
			*/
					uniprot_pdb_alignment = proxy_url + '?t=30&m=alignment&q=' + query_id + '&s=http://das.sanger.ac.uk/das/msdpdbsp/';
					//uniprot_pdb_alignment = proxy_url + '?t=30&m=alignment&q=' + query_id + '&s=http://das.sanger.ac.uk/das/biojavapdbuniprot/';
			/**
			* ONTOLOGY
			*/
				onto_path_type = proxy_url + '?t=10&m=ontology&q=biosapiens_lite.xml&s=' + dasty_path + '/ontology/';
				onto_path_category = proxy_url + '?t=10&m=ontology&q=categories.xml&s=' + dasty_path + '/ontology/';
				//onto_path_type = proxy_url + '?t=30&m=ontology&q=biosapiens.obo_types.xml&s=http://localhost/dasty2/server/pdb/';
				//onto_path_category = proxy_url + '?t=30&m=ontology&q=categories.xml&s=http://localhost/dasty2/server/pdb/';

			/**
			* Path to load pdb files
			*/
				path_pdb_files=dasty_path +"/server/pdb/";
}


/**
* DEFAULT QUERY PARAMETERS
*/
function default_query_parameters()
{
	//default_query_id = "P05067";
	default_query_id = "";
	default_filterLabel = "BioSapiens";
	default_timeout = 3;
	default_dasty_mainpage_name = "interactorview.html"
}
