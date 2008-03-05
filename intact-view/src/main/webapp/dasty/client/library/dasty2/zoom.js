// JavaScript Document
//------------------------------------------------------------------------------------------	
// ZOOM
//------------------------------------------------------------------------------------------			
function zoom(startId, endId)
	{
		document.getElementById("system_information").innerHTML = "";
		document.getElementById("system_information").innerHTML = "Dasty2 is zooming the graphic ...";
		zoom_start = parseInt(document.getElementById(startId).value - 1);
		zoom_end = parseInt(document.getElementById(endId).value);
		//alert("zoom beggining");
		
		if (isNaN(zoom_start) == true || isNaN(zoom_end) == true)
		  {
			  document.getElementById("system_information").innerHTML = "<span style=\"color:#CC0000\">One of the zoom values is not a number. </span><br />... Please correct it and try again.";
		  }
		else
		  {
			//alert(zoom_start + "/" + zoom_end);
			
			var seq_start = parseInt(sequence_info.sequence_start -1);
			var seq_stop = parseInt(sequence_info.sequence_stop);
			
			var temp_zoom_end = zoom_end;
			var temp_zoom_start = zoom_start;
			
			var zoom_message = "";
			var zoom_message2 = "";
			var zoom_error = 0;
			//if(temp_zoom_start > temp_zoom_end){zoom_start = seq_start; zoom_end = seq_stop;}
			//var temp_zoom_end = zoom_end;
			//var temp_zoom_start = zoom_start;
			//if(temp_zoom_start < seq_start){zoom_start = seq_start;}
			//if(temp_zoom_end > seq_stop){zoom_end = seq_stop;}
			
							if(temp_zoom_start < 0 && temp_zoom_end < 0)
							  {
								  zoom_message = "Zoom error! ... The start and end zoom value can not be negative."; 
								  zoom_message2 = "... Please correct it and try again."
								  zoom_start = seq_start;
								  zoom_end = seq_stop;
								  zoom_error = 1;
							  }
							else if(temp_zoom_start < 0 && temp_zoom_end > 0)
							  {
								  if(temp_zoom_end > seq_stop)
									{
										zoom_message = "Zoom error! ... The start zoom value can not be negative and the end zoom value can not be greater than the lenght of the sequence.";  
										zoom_message2 = "... Please correct it and try again."
										zoom_end = seq_stop;
										zoom_start = seq_start;
										zoom_error = 1;
									}
								  else
									{
										zoom_message = "Zoom error! ... The start zoom value can not be negative.";
										zoom_message2 = "... Dasty2 finished to zoom the graphic setting the start zoom value to " + (seq_start + 1)
										zoom_start = seq_start;
										zoom_error = 2;
									}
							  }
							else if(temp_zoom_start > 0 && temp_zoom_end < 0)
							  {
								  if(temp_zoom_start > seq_stop)
									{
										zoom_message = "Zoom error! ... The end value can not be negative and the start value can not be greater than the lenght of the sequence."; 
										zoom_message2 = "... Please correct it and try again."
										zoom_end = seq_stop;
										zoom_start = seq_start;
										zoom_error = 1;
									}
								  else
									{
										zoom_message = "Zoom error! ... The end zoom value can not be negative."; 
										zoom_message2 = "... Dasty2 finished to zoom the graphic setting the end zoom value to " + seq_stop
										
										zoom_end = seq_stop;
										zoom_error = 2;
									}
								  
							  }
							else if(temp_zoom_start > seq_stop && temp_zoom_end > seq_stop)
							  {
								  zoom_message = "Zoom error! ... The start and end zoom value can not be greater than the lenght of the sequence.";
								  zoom_message2 = "... Please correct it and try again."
								  
								  zoom_start = seq_start;
								  zoom_end = seq_stop;
								  zoom_error = 1;
							  }		
							 
							 
							var temp_zoom_end = zoom_end;
							var temp_zoom_start = zoom_start;
							
							if(temp_zoom_start > temp_zoom_end && zoom_error == 0)
								{
									zoom_message = "Zoom error! ... The start zoom value can not be greater than end zoom value.";
									zoom_message2 = "... Please correct it and try again."
									
									zoom_start = seq_start;
									zoom_end = seq_stop;
									zoom_error = 1;
								}
			
			
			//alert(zoom_start + "/" + zoom_end + "/" + temp_zoom_end + "/" + seq_stop);
			if(zoom_error == 0)
				{
					sorting();
					document.getElementById("system_information").innerHTML = "<span style=\"color:#999999\">... Dasty2 finished to zoom the graphic.</span>";
				}
			else if(zoom_error == 1)
				{
					// Display warning message
				}
			else if(zoom_error == 2)
				{
					// Execute sort() and display warning message
					sorting();
				}
				
			if(zoom_error == 1 || zoom_error == 2)
				{
					document.getElementById("system_information").innerHTML = "<span style=\"color:#CC0000\">" + zoom_message + "</span><br><span style=\"color:#999999\">" + zoom_message2; + "</span>"
				}
			
	  } // if (isNaN(zoom_start) == true || isNaN(zoom_end) == true)
	}
	
function resetZoom(startId, endId)
	{
		zoom_start = parseInt(sequence_info.sequence_start) - 1;
		zoom_end = parseInt(sequence_info.sequence_stop);
		//alert(zoom_start + "/" + zoom_end);
		document.getElementById(startId).value = zoom_start + 1;
		document.getElementById(endId).value = zoom_end;
		
		document.getElementById("system_information").innerHTML = "Dasty2 is resetting the graphic zoom to the default values ...";
		sorting();
		document.getElementById("system_information").innerHTML = "<span style=\"color:#999999\">... Dasty2 finished to reset the zoom values for the graphic.</span>";
	}