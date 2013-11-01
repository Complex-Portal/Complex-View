package uk.ac.ebi.intact.site.complex.utils;

import org.springframework.ui.ModelMap;
import javax.servlet.http.HttpServletRequest;

import java.util.HashMap;

/**
 * @author Oscar Forner (oforner@ebi.ac.uk)
 * @version $Id$
 * @since 22/10/13
 */
public class OptionsRequestIndex {

    HttpServletRequest request ;
    HashMap<String, String> set = new HashMap<String,String>();

    public OptionsRequestIndex(HttpServletRequest httpServletRequest) {
        request = httpServletRequest;
        defaultOptions();
    }

    private void defaultOptions() {
        set.put ( TagNamesIndex.TITLE_PAGE, "Complex Searcher &lt; IntAct &lt; EMBL-EBI" ) ;
        set.put ( TagNamesIndex.APP_NAME , "Complex Searcher" ) ;
        set.put ( TagNamesIndex.ABOUT , "Complex Searcher" ) ;
        set.put ( TagNamesIndex.QUERY , "*" ) ;
        set.put ( TagNamesIndex.RESULT_HIDDEN , "hidden" ) ;
        set.put ( TagNamesIndex.RESULT ,"" ) ;
        set.put ( TagNamesIndex.RESULT_NUMBER , "" ) ;
        set.put ( TagNamesIndex.NUMBER , "10" ) ;
        set.put ( TagNamesIndex.FIRST , "0" ) ;
    }

    public void setTagValue(String tag, String value){
        if ( tag != null && value != null ){
            if ( ! set.containsKey(tag) )
                set.remove(tag);
            set.put(tag,value);
        }
    }

    public void SetTagValuesToModel(ModelMap model) {
        for( String key : set.keySet() )
        model.addAttribute( key, set.get(key) );
    }

}
