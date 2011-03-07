/** Collapsible Panel **/

function handleReturnKey(event, btnId) {
    if (event.keyCode == 13) {
        document.getElementById(btnId).click();
    }
}

/**
 * Toggle the visibility of a triplet of components.
 *
 * @param first
 * @param second
 * @param third
 */
function ia_toggle_visibility(first, second, third) {
   ia_toggleDisplayById(first);
   ia_toggleDisplayById(second);
   ia_toggleDisplayById(third);
}

/**
 * Toggle the visibility of a component by id.
 * 
 * @param id the id of the component to show/hide.
 */
function ia_toggleDisplayById(id) {
   var e = document.getElementById(id);

    if (e == null) alert('No component found with id: '+id);

   ia_toggleDisplay(e);
}

function ia_toggleDisplay(e) {
   if(e.style.display == 'block')
      ia_hide(e);
   else
      ia_show(e);
}

function ia_hideById(id) {
   var e = document.getElementById(id);

    if (e == null) alert('No component found with id: '+id);

   ia_hide(e);
}

function ia_hide(e) {
   e.style.display = 'none';
}

function ia_showById(id) {
   var e = document.getElementById(id);

    if (e == null) alert('No component found with id: '+id);

   ia_show(e);
}

function ia_show(e) {
   e.style.display = 'block';
}

/**
 * Focus a component.
 *
 * @param id the id of the component to show/hide.
 */
function ia_focusById(id) {
    var e = document.getElementById(id);
    ia_focus(e);
}

function ia_focus(e) {
   e.focus();
}

function ia_focusAndSelectById(id) {
    var e = document.getElementById(id);
    ia_focusAndSelect(e);
}

function ia_focusAndSelect(e) {
   ia_focus(e);
   ia_select(e);
}

function ia_selectById(id) {
    var e = document.getElementById(id);
    ia_select(e);
}

function ia_select(e) {
   e.select();
}

function ia_disableButton(id, innerHTML) {
    var e = document.getElementById(id);

    if(e != null) {
        e.disabled = true;
        e.innerHTML = innerHTML;
    }
}

function ia_enableButton(id, innerHTML) {
    var e = document.getElementById(id);

    if(e != null) {
        e.disabled = false;
        e.innerHTML = innerHTML;
    }
}