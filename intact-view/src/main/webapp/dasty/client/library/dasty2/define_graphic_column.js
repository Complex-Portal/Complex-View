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

