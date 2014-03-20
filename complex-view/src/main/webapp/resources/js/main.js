// Popup window code
function newPopup(url) {
    popupWindow = window.open(
        url,'popUpWindow','height=700,width=800,left=10,top=10,resizable=yes,scrollbars=yes,toolbar=yes,menubar=no,location=no,directories=no,status=yes')
};

// Check page number
function checkPageNumberTop(){
    var readed = document.forms["Top"]["pageFake"].value;
    var final;
    if (/^\d+$/.test(readed)){
        final = readed.valueOf();
        final = final - 1;
        document.forms["Top"]["page"].value = final.toString();
    }
};

function checkPageNumberBottom(){
    var readed = document.forms["Bottom"]["pageFake"].value;
    var final;
    if (/^\d+$/.test(readed)){
        final = readed.valueOf();
        final = final - 1;
        document.forms["Bottom"]["page"].value = final.toString();
    }
};

function goBack(){
    window.history.back()
};

function clearFilters(){
    var species = document.forms['filtersForm']['species'];
    if (typeof species.length != 'undefined') {
        for (var i = 0; i < species.length; i++){
            species[i].checked = false;
        }
    }
    else{
        species.checked = false;
    }
    var types = document.forms['filtersForm']['types'];
    if (typeof types.length != 'undefined') {
        for (var i = 0; i < types.length; i++){
            types[i].checked = false;
        }
    }
    else{
        types.checked = false;
    }
    var bioroles = document.forms['filtersForm']['bioroles'];
    if (typeof bioroles.length != 'undefined') {
        for (var i = 0; i < bioroles.length; i++){
            bioroles[i].checked = false;
        }
    }
    else{
        bioroles.checked = false;
    }
}

$(document).on({
    'mouseenter': function (e) {
        $(this).tooltip('show');
    },
    'mouseeleave': function (e) {
        $(this).tooltip('hide');
    }
}, '*[rel=tooltip]');

$(document).ready(function()
    {
        $("#participants").tablesorter();
        $("#crossReferences").tablesorter();
    }
);
