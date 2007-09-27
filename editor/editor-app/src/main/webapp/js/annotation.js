/*
 * Validates an annotation text. Returns true only for a keystroke that is
 * within 31 and 127 of unicode characters or a backspace and the last two characters
 * not two consecutive spaces.
 * Author: smudali@ebi.ac.uk
 * Version: $Id$
 */
function validateComment(element, evt) {
    var keyCode = evt.which ? evt.which : evt.keyCode;
    //window.alert(keyCode);
    // Allow backspace or else a user can't delete his/own text!!
    var desc = element.value;//document.forms[0].elements['newAnnotation.description'].value;
    var keyCode = evt.which ? evt.which : evt.keyCode;
    if( keyCode != 8 ){
        if (desc.charAt(desc.length - 1) == ' ' && desc.charAt(desc.length - 2) == ' ') {
            //keyCode == 32) {
            window.alert("Multiple spaces are not allowed");
            return false;
        }
    }


    var s = element.value;
    var o="";
    unicodeCount=0;
    for( m=0;s.charAt(m);++m ) {
        if ( (c=s.charCodeAt(m)) < 128 && c != 38 ) {
            o+=s.charAt(m);
        } else if (c==38) {
            o+="&";
        } else {
            o+="&#"+c+";";
            unicodeCount++;
        }

    }
    if( keyCode != 8 ){
        if( unicodeCount > 0 ) {
            msg = "The character you entered is not allowed. Only Unicode characters from 0020";
            msg += "(space) to 007E(~) are allowed : '"+o+"`'" ;
            o="";
            window.alert(msg);
            return false;
        }
    }

    return true;
}
