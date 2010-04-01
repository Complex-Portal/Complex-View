/** Collapsible Panel **/

/**
 * Toggle the visibility of a triplet of components.
 *
 * @param first
 * @param second
 * @param third
 */
function toggle_visibility(first, second, third) {
   toggleById(first);
   toggleById(second);
   toggleById(third);
}

/**
 * Toggle the visibility of a component by id.
 * 
 * @param id the id of the component to show/hide.
 */
function toggleById(id) {
   var e = document.getElementById(id);
   if(e.style.display == 'block')
      e.style.display = 'none';
   else
      e.style.display = 'block';
}