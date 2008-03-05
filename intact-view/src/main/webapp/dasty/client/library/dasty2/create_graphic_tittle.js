//------------------------------------------------------------------------------------------	
// GRAPHIC TITTLE
//------------------------------------------------------------------------------------------			
function createGraphicTittle(tagId, width_ul, left_div_category, width_div_category, left_div_type, width_div_type, left_div_id, width_div_id, left_div_graphic, width_div_graphic, left_div_server, width_div_server, height_div, show_col_id_temp, left_div_warning, width_div_warning)
	{
        var div = document.getElementById(tagId);
        var my_ul = document.createElement("ul");
		my_ul.setAttribute("id", "gr_list_tittle");
		my_ul.setAttribute("class", "gr_list_class");
		my_ul.setAttribute("className", "gr_list_class");
		//my_ul.setAttribute("style", "width:" + width_ul + "px;"); // tagId, width_ul
		my_ul.style.cssText = "width:" + width_ul + "px;";
		
		//------------------------------------------
		// LIsts
		//------------------------------------------
		var mycurrent_li = document.createElement("li");
		mycurrent_li.setAttribute("id", "gr_item_tittle");
		//mycurrent_li.setAttribute("style", "height:" + tittle_height + "px;"); // height_li // tittle_height is global!
		mycurrent_li.style.cssText = "height:" + tittle_height + "px;";
		
		//------------------------------------------
		// CATEGORIes
		//------------------------------------------
		if (show_col_category == 1)
		   {			
			var mycurrent_div_category = document.createElement("div");
			mycurrent_div_category.setAttribute("class", "gr_div gr_row_tittle");
			mycurrent_div_category.setAttribute("className", "gr_div gr_row_tittle");
			//mycurrent_div_category.setAttribute("style", "top:0px; left:" + left_div_category + "px; width:" + width_div_category + "px; height:" + height_div + "px;");	
			mycurrent_div_category.style.cssText = "top:0px; left:" + left_div_category + "px; width:" + width_div_category + "px; height:" + height_div + "px;";
				
			var mycurrent_category_text = document.createElement("a");
			mycurrent_category_text.setAttribute("class", "gr_text_02");	
			mycurrent_category_text.setAttribute("className", "gr_text_02");
			//mycurrent_category_text.setAttribute("href", "");
			
			
			//mycurrent_category_text.setAttribute("onclick", "sorting('category')");
			mycurrent_category_text.onclick = new Function("sorting('category');");
				
			var mycurrent_category_text_content = document.createTextNode("CATEGORY ");
			var mycurrent_category_img_content = document.createElement("img");
			if(sortOrderCategory == 1)
				{
					mycurrent_category_img_content.setAttribute("src", dasty_path+"img/arrowup.gif");
				}
			else if(sortOrderCategory == -1)
				{
					mycurrent_category_img_content.setAttribute("src", dasty_path+"img/arrowdown.gif");
				}
			else
				{
					mycurrent_category_img_content.setAttribute("src", dasty_path+"img/arrownone.gif");
				}
				
			mycurrent_category_text.appendChild(mycurrent_category_text_content);
			mycurrent_category_text.appendChild(mycurrent_category_img_content);
			mycurrent_div_category.appendChild(mycurrent_category_text);
			
			mycurrent_li.appendChild(mycurrent_div_category); // left_div_category, width_div_category, height_div 
		   }
		   
		//------------------------------------------
		// TYPEs
		//------------------------------------------
		if (show_col_type == 1)
		   {			
			var mycurrent_div_type = document.createElement("div");
			mycurrent_div_type.setAttribute("class", "gr_div gr_row_tittle");
			mycurrent_div_type.setAttribute("className", "gr_div gr_row_tittle");
			//mycurrent_div_type.setAttribute("style", "top:0px; left:" + left_div_type + "px; width:" + width_div_type + "px; height:" + height_div + "px;");	
			mycurrent_div_type.style.cssText = "top:0px; left:" + left_div_type + "px; width:" + width_div_type + "px; height:" + height_div + "px;";
				
			var mycurrent_type_text = document.createElement("a");
			mycurrent_type_text.setAttribute("class", "gr_text_02");	
			mycurrent_type_text.setAttribute("className", "gr_text_02");
			//mycurrent_type_text.setAttribute("href", "");
			
			//mycurrent_type_text.setAttribute("onclick", "sorting('type')");
			mycurrent_type_text.onclick = new Function("sorting('type');");
				
			var mycurrent_type_text_content = document.createTextNode("TYPE ");
			var mycurrent_type_img_content = document.createElement("img");
			if(sortOrderType== 1)
				{
					mycurrent_type_img_content.setAttribute("src", dasty_path+"img/arrowup.gif");
				}
			else if(sortOrderType == -1)
				{
					mycurrent_type_img_content.setAttribute("src", dasty_path+"img/arrowdown.gif");
				}
			else
				{
					mycurrent_type_img_content.setAttribute("src", dasty_path+"img/arrownone.gif");
				}
				
			mycurrent_type_text.appendChild(mycurrent_type_text_content);
			mycurrent_type_text.appendChild(mycurrent_type_img_content);
			mycurrent_div_type.appendChild(mycurrent_type_text);
				
			mycurrent_li.appendChild(mycurrent_div_type); // left_div_type, width_div_type, height_div
		   }
		   
		//------------------------------------------
		// IDs
		//------------------------------------------
		if (show_col_id_temp == 1)
		  {			
			var mycurrent_div_id = document.createElement("div");
			mycurrent_div_id.setAttribute("class", "gr_div gr_row_tittle");
			mycurrent_div_id.setAttribute("className", "gr_div gr_row_tittle");
			//mycurrent_div_id.setAttribute("style", "top:0px; left:" + left_div_id + "px; width:" + width_div_id + "px; height:" + height_div + "px;");
			mycurrent_div_id.style.cssText = "top:0px; left:" + left_div_id + "px; width:" + width_div_id + "px; height:" + height_div + "px;";
				
			var mycurrent_id_text = document.createElement("a");
			mycurrent_id_text.setAttribute("class", "gr_text_02");	
			mycurrent_id_text.setAttribute("className", "gr_text_02");	
			//mycurrent_id_text.setAttribute("href", "");
			
			//mycurrent_id_text.setAttribute("onclick", "sorting('id')");
			mycurrent_id_text.onclick = new Function("sorting('id');");
				
			var mycurrent_id_text_content = document.createTextNode("ID ");
				
			mycurrent_id_text.appendChild(mycurrent_id_text_content);
			mycurrent_div_id.appendChild(mycurrent_id_text);
				
			mycurrent_li.appendChild(mycurrent_div_id); // left_div_id, width_div_id, height_div
		  }
		  
		//------------------------------------------
		// FEATUREs
		//------------------------------------------
				
			var mycurrent_div_graphic = document.createElement("div");
			mycurrent_div_graphic.setAttribute("class", "gr_div gr_row_tittle gr_cell_02");	
			mycurrent_div_graphic.setAttribute("className", "gr_div gr_row_tittle gr_cell_02");	
			//mycurrent_div_graphic.setAttribute("style", "top:0px; left:" + left_div_graphic + "px; width:" + width_div_graphic + "px; height:" + height_div + "px;");	
			mycurrent_div_graphic.style.cssText = "top:0px; left:" + left_div_graphic + "px; width:" + width_div_graphic + "px; height:" + height_div + "px;";
			
			//var mycurrent_graphic_text = document.createElement("a");
			var mycurrent_graphic_text = document.createElement("span");
			mycurrent_graphic_text.setAttribute("class", "gr_text_02");	
			mycurrent_graphic_text.setAttribute("className", "gr_text_02");	
			//mycurrent_graphic_text.setAttribute("href", "");
				
			var mycurrent_graphic_text_content = document.createTextNode("FEATURE ANNOTATONS ");
				
			mycurrent_graphic_text.appendChild(mycurrent_graphic_text_content);
			mycurrent_div_graphic.appendChild(mycurrent_graphic_text);
				
			mycurrent_li.appendChild(mycurrent_div_graphic); // left_div_graphic, width_div_graphic, height_div
			
			
/*
		//------------------------------------------
		// VERSION
		//------------------------------------------
		if (show_col_warning == 1)
		   {			
			var mycurrent_div_warning = document.createElement("div");
			mycurrent_div_warning.setAttribute("class", "gr_div gr_row_tittle");
			mycurrent_div_warning.setAttribute("className", "gr_div gr_row_tittle");
			mycurrent_div_warning.style.cssText = "top:0px; left:" + left_div_warning + "px; width:" + width_div_warning + "px; height:" + height_div + "px;";
				
			var mycurrent_warning_text = document.createElement("a");
			mycurrent_warning_text.setAttribute("class", "gr_text_02");	
			mycurrent_warning_text.setAttribute("className", "gr_text_02");
			//mycurrent_warning_text.setAttribute("href", "");
			
			//mycurrent_warning_text.setAttribute("onclick", "sorting('type')");
			mycurrent_warning_text.onclick = new Function("sorting('version');");
				
			var mycurrent_warning_text_content = document.createTextNode("");
			var mycurrent_warning_img_content = document.createElement("img");
			if(sortOrderVersion== 1)
				{
					mycurrent_warning_img_content.setAttribute("src", "img/arrowup.gif");				
				}
			else if(sortOrderVersion == -1)
				{
					mycurrent_warning_img_content.setAttribute("src", "img/arrowdown.gif");
				}
			else
				{
					mycurrent_warning_img_content.setAttribute("src", "img/arrownone.gif");
				}
				
			mycurrent_warning_text.appendChild(mycurrent_warning_text_content);
			mycurrent_warning_text.appendChild(mycurrent_warning_img_content);
			mycurrent_div_warning.appendChild(mycurrent_warning_text);
				
			mycurrent_li.appendChild(mycurrent_div_warning); // left_div_type, width_div_type, height_div
		   }
*/
			
			
		//------------------------------------------
		// SERVERs
		//------------------------------------------
		if (show_col_server == 1)
		   {			
			var mycurrent_div_server = document.createElement("div");
			mycurrent_div_server.setAttribute("class", "gr_div gr_row_tittle");
			mycurrent_div_server.setAttribute("className", "gr_div gr_row_tittle");
			//mycurrent_div_server.setAttribute("style", "top:0px; left:" + left_div_server + "px; width:" + width_div_server + "px; height:" + height_div + "px;");	
			mycurrent_div_server.style.cssText = "top:0px; left:" + left_div_server + "px; width:" + width_div_server + "px; height:" + height_div + "px;";	
				
			var mycurrent_server_text = document.createElement("a");
			mycurrent_server_text.setAttribute("class", "gr_text_02");	
			mycurrent_server_text.setAttribute("className", "gr_text_02");
			//mycurrent_server_text.setAttribute("href", "");
			
			//mycurrent_server_text.setAttribute("onclick", "sorting('server')");
			mycurrent_server_text.onclick = new Function("sorting('server');")
				
			var mycurrent_server_text_content = document.createTextNode("DAS SOURCE ");
			var mycurrent_server_img_content = document.createElement("img");
			if(sortOrderServer == 1)
				{
					mycurrent_server_img_content.setAttribute("src", dasty_path+"img/arrowup.gif");
				}
			else if(sortOrderServer == -1)
				{
					mycurrent_server_img_content.setAttribute("src", dasty_path+"img/arrowdown.gif");
				}
			else
				{
					mycurrent_server_img_content.setAttribute("src", dasty_path+"img/arrownone.gif");
				}
				
			mycurrent_server_text.appendChild(mycurrent_server_text_content);
			mycurrent_server_text.appendChild(mycurrent_server_img_content);
			mycurrent_div_server.appendChild(mycurrent_server_text);
				
			mycurrent_li.appendChild(mycurrent_div_server); // left_div_server, width_div_server, height_div
		   }
			
		my_ul.appendChild(mycurrent_li);

	    div.appendChild(my_ul);
	}
			