dojo.require("dojo.NodeList-traverse");

function displayFileUpload(){
    if (document.forms['ebiForm']['sourceSelector'][0].checked){
        dojo.query('.urlUpload').style('display', 'none');
        dojo.query('.localFileUpload').style('display', 'block');
    }
    else{
        dojo.query('.urlUpload').style('display', 'block');
        dojo.query('.localFileUpload').style('display', 'none');
    }
}

dojo.addOnLoad(function(){
    dojo.query('.showErrorContextDetails').onclick(function(){
        var thisList = new dojo.NodeList(this);
        thisList.parent().children('.errorContextDetails').style('display', 'block');
        thisList.parent().children('.showErrorContextDetails').style('display', 'none');
    });

    dojo.query('.hideErrorContextDetails').onclick(function(){
        var thisList = new dojo.NodeList(this);
        thisList.parent().children('.errorContextDetails').style('display', 'none');
        thisList.parent().children('.showErrorContextDetails').style('display', null);
    });

    displayFileUpload();
});
