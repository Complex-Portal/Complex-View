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

// interaction plugin
function initiateInteractionPlugin(interactionAc){
    $('#interaction').remove();
    $('#featureDiv').html('<div id="interaction"/>');

    interactionPlugin = $('#interaction').Interaction({
        width: $("#featureDiv").width(),
        developingMode: true,
        jsonUrl : contextPath + '/json?ac=' + interactionAc,
        proxyUrl: contextPath + '/proxy',
        useProxyForData: false,
        legendPosition: 'right',
        loadingImageUrl: contextPath + '/images/wait_black_indicator.gif'
    });
}

function initiateGraph(){
    jQuery('#jmolRow').append('<td style="height: 450px;' +
                   'width:' + $('#featureDiv').width() + 'px;' +
                   'border-style: solid;' +
                   'border-width:1px;' +
                   'vertical-align:top;">' +
                   'Please click on an element (interactor or feature) in the interaction widget to display 3D information.'+
                   '<div id= "jmolDiv" style="width:100%;height:100%;font-size:12px;"/>' +
                   '</td>"');
    jmolPlugin = jQuery("#jmolDiv").Jmol(
        {
            width: $("#featureDiv").width(),
            height: '420',
            jmolFolder: contextPath + '/resources/javascript/Jmol/resources/jmol-12.0.48',
            warningImageUrl: contextPath + '/images/warning_icon.png',
            loadingImageUrl: contextPath + '/images/wait_black_indicator.gif',
            proxyUrl: contextPath + '/proxy'
        });
}

// widget-interaction
// functions for interaction between interaction representation widget and Jmol widget
function featureSelected(event, params){
    if ($('#displayJmol:checked').val()) {
        interactionPlugin.Interaction('highlightFeature', params.featureId);
        var pdbInformation = jmolPlugin.Jmol('getSelectedRegion');
        if (pdbInformation == null || params.interactorId != pdbInformation.proteinId) {
            jmolPlugin.Jmol('selectProtein', params);
        }
        pdbInformation = jmolPlugin.Jmol('getSelectedRegion');
        if (params.coordinates.positionArray === undefined) {
            jmolPlugin.Jmol('selectRegion', params);
        }
        else {
            jmolPlugin.Jmol('selectPositions', params);
        }
    }
}

function interactorSelected(event, params){
    if ($('#displayJmol:checked').val()) {
        interactionPlugin.Interaction('unhighlightFeature');
        jmolPlugin.Jmol('selectProtein', params);
    }
}

function pdbSelected(event, params){
    if (params != null) {
        interactionPlugin.Interaction('highlightRegion', params.proteinId,
                                       params.start, params.end, "#006666", "3D structure coverage");
    }else{
        interactionPlugin.Interaction('unhighlightRegion');
    }
}

