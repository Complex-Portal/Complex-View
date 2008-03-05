// JavaScript Document
//------------------------------------------------------------------------------------------	
// Change Graphic width
//------------------------------------------------------------------------------------------			
function change_graphic_width(graphic_width_ID)
	{
		document.getElementById("system_information").innerHTML = "Dasty2 is resizing the graphic ...";
		var pixels = parseInt(document.getElementById(graphic_width_ID).value);
		//alert(pixels);
		var minimun_feature_size = 50;
		var minimun_size = (col_category_width * show_col_category) + (col_type_width * show_col_type) + (col_id_width * show_col_id) + (col_server_width * show_col_server) + (col_warning_width * show_col_warning) + minimun_feature_size;
		if(pixels > minimun_size)
			{
				graphic_width = parseInt(pixels);
				document.getElementById("graphic_width_px").value = graphic_width;
				sorting();
				document.getElementById("system_information").innerHTML = "<span style=\"color:#999999\">... Dasty2 finished to resize the graphic.</span>";
			}
		else
			{
				document.getElementById("system_information").innerHTML = "<span style=\"color:#CC0000\">The graphic width can not be lower than the minimun width (" + minimun_size + " px)</span><br />If you wish to reduce the minimun width value try to hide some graphic columns.";
			}
	}
	

/*
function info_message(type)
	{
		switch (type)
		  {
			case "sort" :
				document.getElementById("system_information").innerHTML = "Dasty2 is sorting the graphic ...";
				break;
			case "zoom" :
				document.getElementById("system_information").innerHTML = "Dasty2 is zooming the graphic ...";
				break;
			case "resize" :
				document.getElementById("system_information").innerHTML = "Dasty2 is resizing the graphic ...";
				break;
			case "show_hide" :
				document.getElementById("system_information").innerHTML = "Dasty2 is modifying the columns of the graphic ...";
				break;
			case "show_hide" :
				document.getElementById("system_information").innerHTML = "Dasty2 is modifying the columns of the graphic ...";
				break;
		  }	
	}
*/