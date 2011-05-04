dojo.require("dojo.NodeList-traverse");
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
});