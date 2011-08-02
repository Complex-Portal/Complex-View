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


///////////////////////////////
// Cytoscape Web interactions

function selectMerged() {
    document.getElementById('mergeOn').style.fontWeight='bold';
    document.getElementById('mergeOff').style.fontWeight='normal';

    merged = true;
    vis.edgesMerged( merged );
}

function unselectMerged() {
    document.getElementById('mergeOn').style.fontWeight='normal';
    document.getElementById('mergeOff').style.fontWeight='bold';

    merged = false;
    vis.edgesMerged( merged );
}

function selectForceDirectedLayout() {
    document.getElementById('forceDirectedLayout').style.fontWeight='bold';
    document.getElementById('radialLayout').style.fontWeight='normal';
    document.getElementById('circleLayout').style.fontWeight='normal';
    vis.layout('ForceDirected');
}

function selectRadialLayout() {
    document.getElementById('forceDirectedLayout').style.fontWeight='normal';
    document.getElementById('radialLayout').style.fontWeight='bold';
    document.getElementById('circleLayout').style.fontWeight='normal';
    vis.layout('Radial');
}

function selectCircleLayout() {
    document.getElementById('forceDirectedLayout').style.fontWeight='normal';
    document.getElementById('radialLayout').style.fontWeight='normal';
    document.getElementById('circleLayout').style.fontWeight='bold';
    vis.layout('Circle');
}

function graphResize() {
    var heigth = $(window).height() - 290;
    var x = document.getElementById('cytoscapeweb');
    x.style.height = heigth + 'px';
}