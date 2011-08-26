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
