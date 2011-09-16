function submitEnter(commandId, e)
{
    var keycode;
    if (window.event)
        keycode = window.event.keyCode;
    else if (e)
        keycode = e.which;
    else
        return true;

    if (keycode == 13) {
        document.getElementById(commandId).click();
        return false;
    } else
        return true;

}

function ia_submitToReactome(selectedIds) {
    var ids = selectedIds.replace(/,/g, '\r');

    var reactomeForm = document.createElement('form');
    reactomeForm.method='post';
    reactomeForm.action='http://www.reactome.org/cgi-bin/skypainter2';
    reactomeForm.enctype='multipart/form-data';
    reactomeForm.name='skypainter';
    reactomeForm.target='_blank';

    var inputQuery = ia_createHiddenInput('QUERY', ids);
    var inputDb = ia_createHiddenInput('DB', 'gk_current');
    var inputSubmit = ia_createHiddenInput('SUBMIT', '1');
    reactomeForm.appendChild(inputQuery);
    reactomeForm.appendChild(inputDb);
    reactomeForm.appendChild(inputSubmit);

    document.getElementById('intactForm').parentNode.appendChild(reactomeForm);
    reactomeForm.submit();
}

function ia_createHiddenInput(name,value) {
    var input = document.createElement('input');
    input.type = 'hidden';
    input.name = name;
    input.value = value;
    return input;
}

function showhide(id){
    if (document.getElementById){
        obj = document.getElementById(id);
        if (obj.style.display == "none"){
            obj.style.display = "";
        } else {
            obj.style.display = "none";
        }
    }
}

function show(id){
    if (document.getElementById){
        obj = document.getElementById(id);
        if( obj == null ) {
            alert("SHOW: Could not find : 'id'")
        }
        if (obj.style.display == "none"){
            obj.style.display = "block";
        }
    }
}

function hide(id){
    if (document.getElementById){
        obj = document.getElementById(id);
        if( obj == null ) {
            alert("HIDE: Could not find : 'id'")
        }
        if (obj.style == null || obj.style.display == "block" || obj.style.display == ""){
            obj.style.display = "none";
        }
    }
}

///////////////////////////////
// Cytoscape Web interactions

function selectMerged() {
    document.getElementById('mainPanels:mergeOn').style.fontWeight='bold';
    document.getElementById('mainPanels:mergeOff').style.fontWeight='normal';

    merged = true;
    vis.edgesMerged( merged );
}

function unselectMerged() {
    document.getElementById('mainPanels:mergeOn').style.fontWeight='normal';
    document.getElementById('mainPanels:mergeOff').style.fontWeight='bold';

    merged = false;
    vis.edgesMerged( merged );
}

function selectForceDirectedLayout() {
    document.getElementById('mainPanels:forceDirectedLayout').style.fontWeight='bold';
    document.getElementById('mainPanels:radialLayout').style.fontWeight='normal';
    document.getElementById('mainPanels:circleLayout').style.fontWeight='normal';
    vis.layout('ForceDirected');
}

function selectRadialLayout() {
    document.getElementById('mainPanels:forceDirectedLayout').style.fontWeight='normal';
    document.getElementById('mainPanels:radialLayout').style.fontWeight='bold';
    document.getElementById('mainPanels:circleLayout').style.fontWeight='normal';
    vis.layout('Radial');
}

function selectCircleLayout() {
    document.getElementById('mainPanels:forceDirectedLayout').style.fontWeight='normal';
    document.getElementById('mainPanels:radialLayout').style.fontWeight='normal';
    document.getElementById('mainPanels:circleLayout').style.fontWeight='bold';
    vis.layout('Circle');
}

function graphResize() {
    var heigth = $(window).height() - 290;
    var x = document.getElementById('cytoscapeweb');
    x.style.height = heigth + 'px';
}