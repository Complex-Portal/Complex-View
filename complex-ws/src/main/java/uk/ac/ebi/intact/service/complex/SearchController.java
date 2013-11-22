package uk.ac.ebi.intact.service.complex;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import uk.ac.ebi.intact.dataexchange.psimi.solr.complex.ComplexFieldNames;

@Controller
@RequestMapping("/")
public class SearchController {
    /********************************/
    /*      Private attributes      */
    /********************************/
    @Autowired
    private DataProvider dataProvider ;

     /*
     -- BASIC KNOWLEDGE ABOUT SPRING MVC CONTROLLERS --
      * They look like the next one:
      @RequestMapping(value = "/<path to listen>/{<variable>}")
	  public <ResultType> search(@PathVariable String <variable>) {
          ...
	  }

	  * First of all, we have the @RequestMapping annotation where you can
	    use these options:
	     - headers: Same format for any environment: a sequence of
	                "My-Header=myValue" style expressions
	     - method: The HTTP request methods to map to, narrowing the primary
	               mapping: GET, POST, HEAD, OPTIONS, PUT, DELETE, TRACE.
	     - params: Same format for any environment: a sequence of
	               "myParam=myValue" style expressions
	     - value: Ant-style path patterns are also supported (e.g. "/myPath/*.do").

      * Next we have the function signature, with the result type to return,
        the name of the function and the parameters to it. We could see the
        @PathVariable in the parameters it is to say that the content between
        { and } is assigned to this variable. NOTE: They must have the same name

        Moreover, we can have @RequestedParam if we need to read or use a parameter
        provided using "?name=value" way. WE WANT TO DO THAT WITH THE FORMAT,
        BUT THIS PARAMETER IS CONTROLLED BY THE ContentNegotiatingViewResolver BEAN
        IN THE SPRING FILE.
     */

    /****************************/
    /*      Public methods      */
    /****************************/
    /*
     - We can access to that method using:
         http://<servername>:<port>/search/<something to query>
       and
         http://<servername>:<port>/search/<something to query>?format=<type>
     - If we do not use the format parameter we take the values in the headers
     - If we use the format parameter we do not mind in the headers
     - Only listen request via GET never via POST.
     - Does not change the query.
     */
    @RequestMapping(value = "/search/{query}", method = RequestMethod.GET)
	public ComplexRestResult search(@PathVariable String query,
                                    @RequestParam (required = false) String first,
                                    @RequestParam (required = false) String number) {
        // Get parameters (if we have them)
        int f, n;
        // If we have first parameter parse it to integer
        if ( first != null ) f = Integer.parseInt(first);
        // else set first parameter to 0
        else f = 0;
        // If we have number parameter parse it to integer
        if ( number != null ) n = Integer.parseInt(number);
        // else set number parameter to max integer - first (to avoid problem)
        else n = Integer.MAX_VALUE - f;

        // Retrieve data using that parameters and return it
        return this.dataProvider.getData( query, f, n );
	}

    /*
     - We can access to that method using:
         http://<servername>:<port>/interactor/<something to query>
       and
         http://<servername>:<port>/interactor/<something to query>?format=<type>
     - If we do not use the format parameter we take the values in the headers
     - If we use the format parameter we do not mind in the headers
     - Only listen request via GET never via POST.
     - Force to query only in the id, alias and pxref fields.
     */
    @RequestMapping(value = "/interactor/{query}", method = RequestMethod.GET)
    public ComplexRestResult searchInteractor(@PathVariable String query,
                                              @RequestParam (required = false) String first,
                                              @RequestParam (required = false) String number) {
        // Get parameters (if we have them)
        int f, n;
        // If we have first parameter parse it to integer
        if ( first != null ) f = Integer.parseInt(first);
            // else set first parameter to 0
        else f = 0;
        // If we have number parameter parse it to integer
        if ( number != null ) n = Integer.parseInt(number);
            // else set number parameter to max integer - first (to avoid problem)
        else n = Integer.MAX_VALUE - f;

        // Query improvement. Force to query only in the id, alias and pxref
        // fields.
        StringBuilder improvedQuery = new StringBuilder();
        improvedQuery.append(ComplexFieldNames.INTERACTOR_ID)
                     .append(":(")
                     .append(query)
                     .append(")");
        improvedQuery.append(ComplexFieldNames.INTERACTOR_ALIAS)
                     .append(":(")
                     .append(query)
                     .append(")");
        improvedQuery.append(ComplexFieldNames.INTERACTOR_XREF)
                     .append(":(")
                     .append(query)
                     .append(")");
        // Retrieve data using that parameters and return it
        return this.dataProvider.getData( query, f, n );
    }

    /*
     - We can access to that method using:
         http://<servername>:<port>/complex/<something to query>
       and
         http://<servername>:<port>/complex/<something to query>?format=<type>
     - If we do not use the format parameter we take the values in the headers
     - If we use the format parameter we do not mind in the headers
     - Only listen request via GET never via POST.
     - Force to query only in the complex_id, complex_alias and complex_xref
       fields.
     */
    @RequestMapping(value = "/complex/{query}", method = RequestMethod.GET)
    public ComplexRestResult searchInteraction(@PathVariable String query,
                                               @RequestParam (required = false) String first,
                                               @RequestParam (required = false) String number) {
        // Get parameters (if we have them)
        int f, n;
        // If we have first parameter parse it to integer
        if ( first != null ) f = Integer.parseInt(first);
            // else set first parameter to 0
        else f = 0;
        // If we have number parameter parse it to integer
        if ( number != null ) n = Integer.parseInt(number);
            // else set number parameter to max integer - first (to avoid problem)
        else n = Integer.MAX_VALUE - f;

        // Query improvement. Force to query only in the complex_id,
        // complex_alias and complex_xref fields.
        StringBuilder improvedQuery = new StringBuilder();
        improvedQuery.append(ComplexFieldNames.COMPLEX_ID)
                .append(":(")
                .append(query)
                .append(")");
        improvedQuery.append(ComplexFieldNames.COMPLEX_ALIAS)
                .append(":(")
                .append(query)
                .append(")");
        improvedQuery.append(ComplexFieldNames.COMPLEX_XREF)
                .append(":(")
                .append(query)
                .append(")");
        // Retrieve data using that parameters and return it
        return this.dataProvider.getData( improvedQuery.toString(), f, n );
    }

    /*
     - We can access to that method using:
         http://<servername>:<port>/organism/<something to query>
       and
         http://<servername>:<port>/organism/<something to query>?format=<type>
     - If we do not use the format parameter we take the values in the headers
     - If we use the format parameter we do not mind in the headers
     - Only listen request via GET never via POST.
     - Force to query only in the organism_name and species fields.
     */
    @RequestMapping(value = "/organism/{query}", method = RequestMethod.GET)
    public ComplexRestResult searchOrganism(@PathVariable String query,
                                            @RequestParam (required = false) String first,
                                            @RequestParam (required = false) String number) {
        // Get parameters (if we have them)
        int f, n;
        // If we have first parameter parse it to integer
        if ( first != null ) f = Integer.parseInt(first);
            // else set first parameter to 0
        else f = 0;
        // If we have number parameter parse it to integer
        if ( number != null ) n = Integer.parseInt(number);
            // else set number parameter to max integer - first (to avoid problem)
        else n = Integer.MAX_VALUE - f;

        // Query improvement. Force to query only in the organism_name and
        // species (complex_organism) fields.
        StringBuilder improvedQuery = new StringBuilder();
        improvedQuery.append(ComplexFieldNames.ORGANISM_NAME)
                .append(":(")
                .append(query)
                .append(")");
        improvedQuery.append(ComplexFieldNames.COMPLEX_ORGANISM)
                .append(":(")
                .append(query)
                .append(")");
        // Retrieve data using that parameters and return it
        return this.dataProvider.getData( improvedQuery.toString(), f, n );
    }

}
