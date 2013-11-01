package uk.ac.ebi.intact.site.complex.controller;

import org.apache.solr.client.solrj.SolrServerException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import uk.ac.ebi.intact.dataexchange.psimi.solr.complex.ComplexResultIterator;
import uk.ac.ebi.intact.dataexchange.psimi.solr.complex.ComplexSearchResults;
import uk.ac.ebi.intact.dataexchange.psimi.solr.complex.ComplexSolrSearcher;
import uk.ac.ebi.intact.site.complex.utils.OptionsRequestIndex;
import uk.ac.ebi.intact.site.complex.utils.SearcherIndex;
import uk.ac.ebi.intact.site.complex.utils.TagNamesIndex;

import javax.servlet.http.HttpServletRequest;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

@Controller
@RequestMapping("/")
public class IndexController {
    //String solrUrl = "http://localhost:8983/solr/core_complex_pub";
    String solrUrl = "http://ves-ebi-49.ebi.ac.uk:8080/intact/solr/core_complex_pub";
    ComplexSolrSearcher complexSolrSearcher = null ;
    @RequestMapping(method = RequestMethod.GET)
    public String index(HttpServletRequest request, ModelMap model) {
        OptionsRequestIndex optionsRequestIndex = new OptionsRequestIndex(request);
        String query = request.getParameter("complex-searchbox") ;
        String numberS = request.getParameter("number");
        String firstS = request.getParameter("first");
        String result = "" ;
        int number_results = 0;
        int number, first;
        if ( query != null ) {
            try {
                query = URLDecoder.decode(query, "UTF-8");
                complexSolrSearcher = SearcherIndex.getSearcher(this.solrUrl);
                number = numberS != null ? Integer.parseInt(numberS) : 10;
                first = firstS != null ? Integer.parseInt(firstS) : 0;
                ComplexResultIterator resultIterator = complexSolrSearcher.search(query, first, number, null);
                while ( resultIterator.hasNext() ) {
                    number_results++;
                    ComplexSearchResults results = resultIterator.next() ;
                    result += "<input type=\"checkbox\" name=\"complex\" value=\""+ results.getComplexAC() +"\" ><b> #" + number_results + " " + results.getComplexName() + " (<i>" + results.getOrganismName() + "</i>):</b><br>&nbsp;" ;
                    result += results.getDescription().substring(0,100) + "...<br>&nbsp;IntAct AC: " + results.getComplexAC() + "</input><br><br>\n";
                }

            } catch (SolrServerException e) {
                e.printStackTrace();
            }
            catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            optionsRequestIndex.setTagValue( TagNamesIndex.QUERY , query) ;
            optionsRequestIndex.setTagValue( TagNamesIndex.RESULT_HIDDEN , "");
            optionsRequestIndex.setTagValue( TagNamesIndex.RESULT ,result);
            optionsRequestIndex.setTagValue( TagNamesIndex.RESULT_NUMBER , String.valueOf(number_results));
            optionsRequestIndex.setTagValue( TagNamesIndex.NUMBER , numberS);
            optionsRequestIndex.setTagValue( TagNamesIndex.FIRST , firstS);
        }
        optionsRequestIndex.SetTagValuesToModel(model);
        return "index";
    }
}
