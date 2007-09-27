/*
 * Contains common JavaScript routines for the editor
 * Author: smudali@ebi.ac.uk
 * Version: $Id$
 */

// This is a global variable to setup a window.
var newWindow;

// Create a new window if it hasnt' created before and bring it to the
// front if it is focusable.
function makeNewWindow(link) {
    if (!newWindow || newWindow.closed) {
        newWindow = window.open(link, "display", "scrollbars=yes,height=700,width=840,resizable=yes");
        newWindow.focus();
    }
    else if (newWindow.focus) {
        newWindow.focus();
        newWindow.location.href = link;
    }
}

// Displays the link from Xref's primary id in the same window as the
// search window.
function showXrefPId(link) {
    makeNewWindow(link);
}

// Avoid that when the cursor is on a text field and if the user press the enter key it simulate a click on
// the first button of the form.
function handleEnter (field, event) {
    var keyCode = event.keyCode ? event.keyCode : event.which ? event.which : event.charCode;
    if (keyCode == 13) {
        return false;
    }
    return true;
}
