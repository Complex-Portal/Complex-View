// JavaScript Document
//------------------------------------------------------------------------------------------	
// Hide/Show columns in the graphic
//------------------------------------------------------------------------------------------			
function define_graphic_column(column_name)
	{
		var plus_icon = "<img src=\"img/tick01.gif\" border=\"0\" align=\"absbottom\">&nbsp;";
		var minus_icon = "<img src=\"img/notick01.gif\" border=\"0\" align=\"absbottom\">&nbsp;";
		
		switch (column_name)
		  {
			case "category" :
				if(show_col_category == 1)
					{
						show_col_category = 0;
						var icon = document.getElementById("menu_mo_img_category_column");
						icon.innerHTML = minus_icon;
					}
				else
					{
						show_col_category = 1;
						var icon = document.getElementById("menu_mo_img_category_column");
						icon.innerHTML = plus_icon;
					}
				break;
				
			case "method" :
				if(show_col_method == 1)
					{
						show_col_method = 0;
						var icon = document.getElementById("menu_mo_img_method_column");
						icon.innerHTML = minus_icon;
					}
				else
					{
						show_col_method = 1;
						var icon = document.getElementById("menu_mo_img_method_column");
						icon.innerHTML = plus_icon;
					}
				break;
						
			case "type" :
				if(show_col_type == 1)
					{
						show_col_type = 0;
						var icon = document.getElementById("menu_mo_img_type_column");
						icon.innerHTML = minus_icon;
					}
				else
					{
						show_col_type = 1;
						var icon = document.getElementById("menu_mo_img_type_column");
						icon.innerHTML = plus_icon;
					}
				break;
				
			case "server" :
				if(show_col_server == 1)
					{
						show_col_server = 0;
						var icon = document.getElementById("menu_mo_img_server_column");
						icon.innerHTML = minus_icon;
					}
				else
					{
						show_col_server = 1;
						var icon = document.getElementById("menu_mo_img_server_column");
						icon.innerHTML = plus_icon;
					}
				break;
				
			case "id" :
				if(show_col_id == 1)
					{
						show_col_id = 0;
						var icon = document.getElementById("menu_mo_img_id_column");
						icon.innerHTML = minus_icon;
					}
				else
					{
						show_col_id = 1;
						var icon = document.getElementById("menu_mo_img_id_column");
						icon.innerHTML = plus_icon;
					}
				break;
		  }	
		sorting();
	}

function define_graphic_column_npf(column_name)
{
		switch (column_name)
		  {
			case "type_category" :
				if(show_col_category_npf == 1)
					{
						show_col_category_npf = 0;
						setCheckboxOptionsNPF("menu_mo_img_category_column_npf", false);
						toggleColumnNPF(column_name, false);	
					}
				else
					{
						show_col_category_npf = 1;
						setCheckboxOptionsNPF("menu_mo_img_category_column_npf", true);
						toggleColumnNPF(column_name, true);	
					}
				break;
				
			case "type_id" :
				if(show_col_type_npf == 1)
					{
						show_col_type_npf = 0;
						setCheckboxOptionsNPF("menu_mo_img_type_column_npf", false);
						toggleColumnNPF(column_name, false);	
					}
				else
					{
						show_col_type_npf = 1;
						setCheckboxOptionsNPF("menu_mo_img_type_column_npf", true);
						toggleColumnNPF(column_name, true);	
					}
				break;
						
			case "feature_label" :
				if(show_col_label_npf == 1)
					{
						show_col_label_npf = 0;
						setCheckboxOptionsNPF("menu_mo_img_label_column_npf", false);
						toggleColumnNPF(column_name, false);					
					}
				else
					{
						show_col_label_npf = 1;
						setCheckboxOptionsNPF("menu_mo_img_label_column_npf", true);
						toggleColumnNPF(column_name, true);	
					}
				break;
				
			case "note_data" :
				if(show_col_note_npf == 1)
					{
						show_col_note_npf = 0;
						setCheckboxOptionsNPF("menu_mo_img_note_column_npf", false);
						toggleColumnNPF(column_name, false);	
					}
				else
					{
						show_col_note_npf = 1;
						setCheckboxOptionsNPF("menu_mo_img_note_column_npf", true);
						toggleColumnNPF(column_name, true);	
					}
				break;
				
			case "score_data" :
				if(show_col_score_npf == 1)
					{
						show_col_score_npf = 0;
						setCheckboxOptionsNPF("menu_mo_img_score_column_npf", false);
						toggleColumnNPF(column_name, false);	
					}
				else
					{
						show_col_score_npf = 1;
						setCheckboxOptionsNPF("menu_mo_img_score_column_npf", true);
						toggleColumnNPF(column_name, true);	
					}
				break;
			case "annotation_server" :
				if(show_col_server_npf == 1)
					{
						show_col_server_npf = 0;
						setCheckboxOptionsNPF("menu_mo_img_server_column_npf", false);
						toggleColumnNPF(column_name, false);	
					}
				else
					{
						show_col_server_npf = 1;
						setCheckboxOptionsNPF("menu_mo_img_server_column_npf", true);
						toggleColumnNPF(column_name, true);	
					}
				break;
			case "method_data" :
				if(show_col_method_npf == 1)
					{
						show_col_method_npf = 0;
						setCheckboxOptionsNPF("menu_mo_img_method_column_npf", false);
						toggleColumnNPF(column_name, false);	
					}
				else
					{
						show_col_method_npf = 1;
						setCheckboxOptionsNPF("menu_mo_img_method_column_npf", true);
						toggleColumnNPF(column_name, true);						
					}
				break;
			case "version" :
				if(show_col_version_npf == 1)
					{
						show_col_version_npf = 0;
						setCheckboxOptionsNPF("menu_mo_img_version_column_npf", false);
						toggleColumnNPF(column_name, false);	
					}
				else
					{
						show_col_version_npf = 1;
						setCheckboxOptionsNPF("menu_mo_img_version_column_npf", true);
						toggleColumnNPF(column_name, true);						
					}
				break;
			case "feature_id" :
				if(show_col_featureid_npf == 1)
					{
						show_col_featureid_npf = 0;
						setCheckboxOptionsNPF("menu_mo_img_featureid_column_npf", false);
						toggleColumnNPF(column_name, false);	
					}
				else
					{
						show_col_featureid_npf = 1;
						setCheckboxOptionsNPF("menu_mo_img_featureid_column_npf", true);
						toggleColumnNPF(column_name, true);						
					}
				break;
		  }	
	}

function setCheckboxOptionsNPF ( imgId, check )
{
	var iconHTML = (!check) ? "<img src=\"img/notick01.gif\" border=\"0\" align=\"absbottom\">&nbsp;"
					   : "<img src=\"img/tick01.gif\" border=\"0\" align=\"absbottom\">&nbsp;";
	
	var icon = document.getElementById(imgId);
	icon.innerHTML = iconHTML;
}

function toggleColumnNPF (columnName, showCol) 
{	
	/* Index of the column to show/hide */ 
	var columnIndex = non_positional_features_columns_index [ columnName ];	
	
	/* Show or hide a column */
	var display = (showCol) ? '' : 'none';
	
	/* Hide the columns */
	var table = document.getElementById('non_positional_features');
	for (var r = 0; r < table.rows.length; r++)
		table.rows[r].cells[columnIndex].style.display = display;
}
