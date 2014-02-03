// Popup window code
function newPopup(url) {
    popupWindow = window.open(
        url,'popUpWindow','height=700,width=800,left=10,top=10,resizable=yes,scrollbars=yes,toolbar=yes,menubar=no,location=no,directories=no,status=yes')
};
$(function(){
    $("#details").sortable("option", "items", "> div"); //"option", "items", "> div"
    $("#details").disableSelection();
})