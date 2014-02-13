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
}
function checkPageNumberBottom(){
    var readed = document.forms["Bottom"]["pageFake"].value;
    var final;
    if (/^\d+$/.test(readed)){
        final = readed.valueOf();
        final = final - 1;
        document.forms["Bottom"]["page"].value = final.toString();
    }
}