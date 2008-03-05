// JavaScript Document

function progBar(percentage)
{
	//alert(percentage);
	var total_px = parseInt(progress_bar_width*percentage/100);
   	if (total_px <= progress_bar_width )
   	{
	    if (total_px > 40){document.getElementById("progress_bar_1").innerHTML = percentage + "%";}
     	document.getElementById("progress_bar_2").style.width = total_px + "px";	
   	}
}

function getPxWidthFromStyle(TagId)
{
	var width = document.getElementById(TagId).style.width;
	var width_value = width.split("px");
	return width_value[0];
}

function getPxHeightFromStyle(TagId)
{
	var height = document.getElementById(TagId).style.height;
	var height_value = height.split("px");
	return height_value[0];
}