// JavaScript Document


function feature_mouse_action(posX, posY, feature_title, feature_xmlnumber, feature_row, action)
	{

	if(action == 'mouseover')
			{
				createFeatureDetails(feature_row, feature_xmlnumber);
				if (isPDBVisible==true){
					selectAnotationRegion(feature_row, feature_xmlnumber);
				}
				if(show_popup == 1)
					{
						//var rafa = document.getElementById('__tooltip__');
						//rafa.style.display = "none";
						//var opa = document.getElementById("display_test");
						//var content_opa = opa.innerHTML;
						//opa.innerHTML = (content_opa + " <br>------------<br>unique_id: " + unique_id + " / show_popup: " + show_popup);
						
					}
			}
	else if(action == 'mouseclick')
			{
				if(show_popup == 2)
					{
						createFeatureDetails(feature_row, feature_xmlnumber);
						win2(posX, posY, feature_title);
						
						
						
						//TooltipManager = null;
						
						
						
						//TooltipManager.tooltipWindow.removeEventListener('mouseover',arguments.callee,false);
						
						//var rafa = document.getElementById('Pfam-B_2235_529');
						
						//rafa.removeEventListener('mouseover',arguments.callee,false);
						//removeEventListener('click',arguments.callee,false);
						
					}
			}
	}




function win2(setX, setY, feature_id)
{

		  //var scrollbar_height = f_scrollTop();
		  var newX = setX - 14;
		  //var newY = setY + height_graphic_feature - 2;
		  var newY = setY + height_graphic_feature + 2;
		  var feature_content = document.getElementById('display_feature_details').innerHTML;
		  
		  popup_num = popup_num + 1;
		  //Windows.closeAll();
		  //var win = new Window('window_id_' + popup_num, {className: "mac_os_x", title: feature_id});
		  var win = new Window('window_id_' + popup_num, {className: "mac_os_x", title: feature_id, width:260, height:170});
		  win.keepMultiModalWindow=false;
		  //win.getContent().innerHTML = "<h1>Constraint inside  page !!</h1>";
		  win.getContent().innerHTML = feature_content;
		  win.setDestroyOnClose();
		  //win.setHTMLContent(html)
		  //win.setContent('display_feature_details', false, false)
		  win.toFront();
		  
		  //win.showCenter();
		  //win.setConstraint(true, {left:0, right:0});
		  win.setLocation(newY, newX);
		  win.show();
}

function pdbOnWindow(div_id)
	{
		var pdbWin = new Window('pdbWin', {className: "mac_os_x", title: "PROTEIN STRUCTURE", hideEffect:Element.hide, showEffect:Element.show, maximizable: false, resizable: true, zIndex:9999});
		pdbWin.setContent(div_id, true, true);
		pdbWin.setDestroyOnClose();
		pdbWin.show();
		//changeStructure();
	}
	

/*
contentWin = new Window({maximizable: false, resizable: false, hideEffect:Element.hide, showEffect:Element.show, minWidth: 10, destroyOnClose: true}) contentWin.setContent('test_content', true, true) contentWin.show();



function openModalDialog() {
	  debug($('modal_window_content'))
		var win = new Window('modal_window', {className: "dialog", title: "Ruby on Rails",top:100, left:100,  width:300, height:200, zIndex:150, opacity:1, resizable: true})
		//win.getContent().innerHTML = "Hi"
		win.setContent("select")
		win.setDestroyOnClose();
		win.show(true);	
	}

function pdbOnWindow(div_id)
	{
		var pdbWin = new Window('pdbWin', {className: "mac_os_x", maximizable: false, resizable: true, hideEffect:Element.hide, showEffect:Element.show, minWidth: 10);
		pdbWin.setContent(div_id);
		setDestroyOnClose();
		pdbWin.show();
	}
	
*/	


  //TooltipManager.init("tooltip", {url: "tooltip_ajax.html", options: {method: 'get'}}, {showEffect: Element.show, hideEffect: Element.hide});
  //TooltipManager.addHTML("tooltip1", "tooltip_content2");
  //TooltipManager.addAjax("tooltip2", {url: "tooltip_ajax.html", options: {method: 'get'}});
  //TooltipManager.addURL("tooltip3", "tooltip_url.html", 200, 300);

function define_popups()
	{
		if(show_popup == 2)
			{
				show_popup = 1;
				var minus_icon = "<img src=\"img/notick01.gif\" border=\"0\" align=\"absbottom\">&nbsp;";
				var icon = document.getElementById("menu_mo_img_popups");
				icon.innerHTML = minus_icon;
			}
		else
			{
				show_popup = 2;
				var plus_icon = "<img src=\"img/tick01.gif\" border=\"0\" align=\"absbottom\">&nbsp;";
				var icon = document.getElementById("menu_mo_img_popups");
				icon.innerHTML = plus_icon;
			}
	}

		
		