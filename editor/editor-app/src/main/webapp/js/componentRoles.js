//
// This method is called when the experimental role is updated.
// The method updates the biological role to neutral if it is
// currently not set to a specific value.
//
function doc_get( id ) {
var obj = null;
//if( document.getElementById ) {
//obj = document.getElementById(n);
obj = document.getElementById?document.getElementById(id):document.all?document.all[id]:document.layers[id];
/*} else {
switch(MODE) {
case 'IE6' : obj = document.all[n]; break;
case 'NS6' : obj = document.getElementById(n);
}
} */
//if( !obj ) { alert( "missing obj: "+ n ); }
return obj;
}

//
// This method is called when the experimental role is updated.
// The method updates the biological role to neutral if it is
// currently not set to a specific value.
//
function updateBiologicalRole()
{
	// The editor-webapp takes care of putting the role 'neutral' in position 1
        var exp_neutralIndex = 1;

    var exp = doc_get('expRole');
    var bio = doc_get('bioRole');
    var expRole = exp.value;
    var bioRole = bio.value;

    // alert("ExpRole(" + expRole + ")\nBioRole("+ bioRole +")");

	if( expRole != "--- Select ---" ) {
		// a selcection has been made
		// alert("Not none");

		if( expRole != "neutral component" ) {
			// alert("Not neutral");
			// update biological role to unspecified
			// alert( "Trying to update biological role to 'unspecified role'" );
			if( bioRole == "--- Select ---" ) {
				// alert( "updating biological role to neutral" );
				bio.selectedIndex = exp_neutralIndex;
			}
		}
	}
}
//
// This method is called when the biological role is updated.
// The method updates the experimental role to unspecified if it is
// currently not set to a specific value.
//
function updateExperimentalRole()
{
	// The editor-webapp takes care of putting the role 'unspecified' in position 1
        var bio_unspecifiedIndex = 1;

        var exp = doc_get('expRole');
        var bio = doc_get('bioRole');
	    var expRole = exp.value;
	    var bioRole = bio.value;

	// alert("ExpRole(" + expRole + ")\nBioRole("+ bioRole +")");

	if( bioRole != "--- Select ---" ) {
		// a selcection has been made
		// alert("Not none");

		if( bioRole != "unspecified" ) {
			// alert("Not neutral");
			// update biological role to unspecified
			// alert( "Trying to update biological role to 'unspecified role'" );
			if( expRole == "--- Select ---" ) {
				// alert( "updating experimental role to unspecified" );
				exp.selectedIndex = bio_unspecifiedIndex;
			}
		}
	}
}