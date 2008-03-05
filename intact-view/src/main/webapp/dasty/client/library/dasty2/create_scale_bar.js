// JavaScript Document
//------------------------------------------------------------------------------------------	
// SCALE BAR
//------------------------------------------------------------------------------------------			
function createScaleBar(num_of_bars, seq_width, width_ul, left_div_graphic, width_div_graphic)
	{
		var num_of_bars = num_of_bars - 1;
		bar_aa_distance_list = [];
		bar_px_distance_list = [];
		
		if( zoom_end != 0)
			{		   
				var zoom_width = zoom_end - zoom_start;
				var seq_width = zoom_width;
				
				//var opa = document.getElementById("display_test");
				//var content_opa = opa.innerHTML;
				//opa.innerHTML = (content_opa + " <br>------------<br>temp_zoom_start:" + temp_zoom_start + " zoom_end:" + zoom_end + " zoom_width:" + zoom_width + " temp_zoom_end:" + temp_zoom_end + " seq_stop:" + seq_stop);	
			}
	
		
		createAaDistanceList(num_of_bars, seq_width);
		createPixelDistanceList(seq_width, left_div_graphic);
		
		//alert(bar_px_distance_list);
		
	 var scalebar_ul = document.createElement("ul");
	 scalebar_ul.setAttribute("id", "gr_scalebar_ul");
	 scalebar_ul.setAttribute("class", "gr_scalebar");
	 scalebar_ul.setAttribute("className", "gr_scalebar");
	 //scalebar_ul.setAttribute("style", "width:" + width_ul + "px;");
	 scalebar_ul.style.cssText = "width:" + width_ul + "px;";
	 
	 var scalebar_li = document.createElement("li");
	 scalebar_li.setAttribute("id", "gr_scalebar_li");
	 //scalebar_li.setAttribute("style", "height:" + tittle_height + "px;"); // tittle_height is gloabal!
	 scalebar_li.style.cssText = "height:" + tittle_height + "px;";
	 scalebar_ul.appendChild(scalebar_li);
	 
	 return scalebar_ul;
	}
	
function fillScaleBar()
	{

		if( zoom_end != 0)
			{  
				var zoom_width = zoom_end - zoom_start;
				var seq_width = zoom_width;			
				//alert(zoom_width);
			}
		else
			{
			 var seq_start = sequence_info.sequence_start -1;
			 var seq_stop = sequence_info.sequence_stop;
			 var seq_width = seq_stop - seq_start;
			}

		
			 var height_div = (dasty2.countVisibleLines * (height_graphic_feature + 7)) + (tittle_height * 2) + 1;
			 
			 if(browser_name == "Microsoft Internet Explorer")
				{
					height_div = height_div + 3;
				}
			 
			 var scalebar = document.getElementById("gr_scalebar_li");
			 
			 var div_width = setDivLength(seq_width);
			 
			 for(var b = 0; b < bar_px_distance_list.length; b++)
				{
				 //if(b == 0 || b == (bar_px_distance_list.length -1))
				 	//{
						 // skip drawing
					//}
				 //else
				 	//{
						 // ----------------------------------------------------------------------------------
						 // POSITION BARS
						 // ----------------------------------------------------------------------------------
						 var scalebar_div_graphic = document.createElement("div");
						 //scalebar_div_graphic.setAttribute("class", "gr_div gr_row_0" + bg_color + " gr_cell_02");
						 scalebar_div_graphic.setAttribute("id", "gr_scalebar_div_" + b);
						 scalebar_div_graphic.setAttribute("class", "gr_div gr_scalebar_div");
						 scalebar_div_graphic.setAttribute("className", "gr_div gr_scalebar_div");
						 if( zoom_end != 0)
							{
								//scalebar_div_graphic.setAttribute("style", "top:0px; left:" + (bar_px_distance_list[b] - div_width + 1) + "px; width:" + div_width + "px; height:" + height_div + "px;");
								scalebar_div_graphic.style.cssText = "top:0px; left:" + (bar_px_distance_list[b] - div_width + 1) + "px; width:" + div_width + "px; height:" + height_div + "px;";
							}
						 else
							{
								//scalebar_div_graphic.setAttribute("style", "top:0px; left:" + (bar_px_distance_list[b] - div_width + 1) + "px; width:" + div_width + "px; height:" + height_div + "px;");
								
								scalebar_div_graphic.style.cssText = "top:0px; left:" + (bar_px_distance_list[b] - div_width + 1) + "px; width:" + div_width + "px; height:" + height_div + "px;";
							} // if( zoom_end != 0)
							
						 scalebar.appendChild(scalebar_div_graphic);
						 
						 
				//var opa = document.getElementById("display_test");
				//var content_opa = opa.innerHTML;
				//opa.innerHTML = (content_opa + " <br>------------<br>value:" + bar_px_distance_list[b] + " div:" + div_width);
				//opa.innerHTML = (content_opa + " <br>------------<br>temp_zoom_start:" + temp_zoom_start + " zoom_end:" + zoom_end + " zoom_width:" + zoom_width + " temp_zoom_end:" + temp_zoom_end + " seq_stop:" + seq_stop);
						 
						 
						 // ----------------------------------------------------------------------------------
						 // POSITION NUMBERS
						 // ----------------------------------------------------------------------------------
						 var left_position = bar_px_distance_list[b] + 3;
						 var scalebar_position = document.createElement("span");
						 scalebar_position.setAttribute("class", "gr_scalebar_span");
						 scalebar_position.setAttribute("className", "gr_scalebar_span");
						 //scalebar_position.setAttribute("style", "top:0px; left:" + left_position + "px;");
						 scalebar_position.style.cssText = "top:0px; left:" + left_position + "px;";
						 
						 var scalebar_position_content = document.createTextNode(bar_aa_distance_list[b]);
						 scalebar_position.appendChild(scalebar_position_content);
						 scalebar.appendChild(scalebar_position);
				 
					//} // if(b == 0 || b == (bar_px_distance_list.length -1))
				}  // for(var b = 0; b < bar_px_distance_list.length; b++)	
	}
	
	
function createAaDistanceList(num_of_bars, seq_width)
	{
		var num_of_bars_temp = num_of_bars;
		if(seq_width < 16 && num_of_bars_temp > 4)
			{
				var num_of_bars = 4;
			}
		if(seq_width < 8 && num_of_bars_temp > 2)
			{
				var num_of_bars = 2;
			}
			
		var bar_aa_distance = (seq_width / num_of_bars);
		//alert(bar_aa_distance);
		var bar_aa_distance_correction = parseInt(bar_aa_distance/10);
		//alert(bar_aa_distance_correction);
		if(bar_aa_distance_correction > 0)
			{

				var bar_aa_distance_correction_value = parseInt(bar_aa_distance_correction.toString() + "0");
			}
		else
			{
				var bar_aa_distance_correction_value = parseInt(bar_aa_distance);
			}
		//bar_aa_distance_correction = bar_aa_distance;
		//alert(bar_aa_distance_correction);
		//var num_of_bars_correction = num_of_bars -1;
		
		bar_aa_distance_list.push(1);
		for(var b = 1; b < num_of_bars; b++)
			{
				bar_aa_distance_list.push(bar_aa_distance_correction_value * b);
			}
		bar_aa_distance_list.push(seq_width);
		 
		//alert(bar_aa_distance_list);
	}
	
	
	
	

function createPixelDistanceList(seq_width, left_div_graphic)
	{
		for(var b = 0; b < bar_aa_distance_list.length; b++)
			{
				bar_px_distance_list.push(((width_div_graphic_correction * bar_aa_distance_list[b])/seq_width) + left_div_graphic);
				// bar_px_distance_list.push(((width_div_graphic_correction * bar_aa_distance_list[b])/seq_width) + 1 + left_div_graphic);
			}	
		if( zoom_end != 0)
			{
				if(zoom_start == 0 || zoom_start == 1)
					{
						// Don't add startZoom to bar_aa_distance_list
					}
				else
					{
						addZoomToBar_aa_distance_list();
					}
			}
	}
	
function addZoomToBar_aa_distance_list()
	{
		//var aa_start = parseInt(zoom_start/10);
		for(var b = 0; b < bar_aa_distance_list.length; b++)
			{
				// bar_aa_distance_list[b] = bar_aa_distance_list[b] + parseInt(aa_start.toString() + "0");
				bar_aa_distance_list[b] = bar_aa_distance_list[b] + (zoom_start);
			}
	}
	
	
function setDivLength(seq_width)
	{
		var div_width_temp = width_div_graphic_correction / seq_width;
		var div_width = 0;
		
		if (div_width_temp < 1)
			{
				div_width = 1;
			}
		else
			{
				div_width = div_width_temp;
			}
		return div_width
	}