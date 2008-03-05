// JavaScript Document
//------------------------------------------------------------------------------------------	
// ZOOM SLIDE BAR
//------------------------------------------------------------------------------------------			
function createSlideBar(width_ul, left_div_graphic, width_div_graphic)
	{		
	 var slidebar_ul = document.createElement("ul");
	 slidebar_ul.setAttribute("id", "gr_slidebar_ul");
	 slidebar_ul.setAttribute("class", "gr_slidebar");
	 slidebar_ul.setAttribute("className", "gr_slidebar");
	 //slidebar_ul.setAttribute("style", "width:" + width_ul + "px;");
	 //slidebar_ul.style.setAttribute('cssText', "width:" + width_ul + "px;");
	 slidebar_ul.style.cssText = "width:" + width_ul + "px;";
	 
	 var slidebar_li = document.createElement("li");
	 slidebar_li.setAttribute("id", "gr_slidebar_li");
	 //slidebar_li.setAttribute("style", "height:" + tittle_height + "px;"); // tittle_height is gloabal!
	 //slidebar_li.style.setAttribute('cssText', "height:" + tittle_height + "px;");
	 slidebar_li.style.cssText = "height:" + tittle_height + "px;";
	 slidebar_ul.appendChild(slidebar_li);
	 
	 var slidebar_div = document.createElement("div");
	 slidebar_div.setAttribute("id", "gr_slidebar_div_track");
	 //slidebar_div.setAttribute("style", "width:"+ width_div_graphic +"px;background-color:#E4E4E4;height:5px;position:relative;left:" + (left_div_graphic + 1) + "px;");
	 //slidebar_div.style.setAttribute('cssText', "width:"+ width_div_graphic +"px;background-color:#E4E4E4;height:5px;position:relative;left:" + (left_div_graphic + 1) + "px;");
	 slidebar_div.style.cssText = "width:"+ width_div_graphic +"px;background-color:#E4E4E4;height:5px;position:relative;left:" + (left_div_graphic + 1) + "px;";
	 slidebar_li.appendChild(slidebar_div);
	 
	 var slidebar_div_thumb_01 = document.createElement("div");
	 slidebar_div_thumb_01.setAttribute("id", "gr_slidebar_div_thumb_01");
	 //slidebar_div_thumb_01.setAttribute("style", "cursor:e-resize;position:absolute;top:0;left:0;width:5px;height:10px;background-color:#f00;opacity:.70; filter: alpha(opacity=70);");
	 //slidebar_div_thumb_01.style.setAttribute('cssText', "cursor:e-resize;position:absolute;top:0;left:0;width:5px;height:10px;background-color:#f00;opacity:.70; filter: alpha(opacity=70);");
	 slidebar_div_thumb_01.style.cssText = "cursor:e-resize;position:absolute;top:0;left:0;width:5px;height:10px;background-color:#f00;opacity:.70; filter: alpha(opacity=70);";
	 slidebar_div.appendChild(slidebar_div_thumb_01);

	 var slidebar_div_thumb_02 = document.createElement("div");
	 slidebar_div_thumb_02.setAttribute("id", "gr_slidebar_div_thumb_02");
	 //slidebar_div_thumb_02.setAttribute("style", "cursor:w-resize;position:absolute;top:0;left:0;width:5px;height:10px;background-color:#f00;opacity:.70; filter: alpha(opacity=70);");
	 //slidebar_div_thumb_02.style.setAttribute('cssText', "cursor:w-resize;position:absolute;top:0;left:0;width:5px;height:10px;background-color:#f00;opacity:.70; filter: alpha(opacity=70);");
	 slidebar_div_thumb_02.style.cssText = "cursor:w-resize;position:absolute;top:0;left:0;width:5px;height:10px;background-color:#f00;opacity:.70; filter: alpha(opacity=70);";
	 slidebar_div.appendChild(slidebar_div_thumb_02);
	 

  //<div id="track6" style="width:500px;background-color:#aaa;height:5px;position:relative;">
    //<div id="handle6-1" style="position:absolute;top:0;left:0;width:5px;height:10px;background-color:#f00;"> </div>
    //<div id="handle6-2" style="position:absolute;top:0;left:0;width:5px;height:10px;background-color:#0f0;"> </div>
  //</div>
	 
	 return slidebar_ul;
	}
	

	
	
function activateSlideBar(start_feature, end_feature, handle_start_id, handle_end_id, track_id, show_start_id, show_end_id)
  {
	  	var end_feature_temp = end_feature;
	  	if(end_feature_temp == 0 || end_feature_temp == "")
			{
				start_feature = sequence_info.sequence_start;
				end_feature = sequence_info.sequence_stop;
			}	
			
	    var slider = new Control.Slider([handle_start_id,handle_end_id],track_id,{
		range:$R(parseInt(start_feature),parseInt(end_feature)), // start seq, end seq
        sliderValue:[parseInt(start_feature),parseInt(end_feature)], // start seq, end seq
        restricted:true,
		//onSlide:function(v){document.getElementById(show_start_id).value = parseInt(v[0]); document.getElementById(show_end_id).value =parseInt(v[1]);}});
		onSlide:function(v){v = v; document.getElementById(show_start_id).value = parseInt(v[0]); document.getElementById(show_end_id).value =parseInt(v[1]);}});
        //onChange:function(v){$('debug6').innerHTML='Changed: '+ v[0]; $(name_debug).innerHTML='Changed: '+ v[1];}});
        //onSlide:function(v){$('debug6').innerHTML='slide: '+ v},
        //onChange:function(v){$('debug6').innerHTML='changed! '+v.inspect()}});
  }