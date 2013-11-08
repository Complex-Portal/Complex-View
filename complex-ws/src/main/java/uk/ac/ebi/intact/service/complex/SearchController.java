package uk.ac.ebi.intact.service.complex;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import uk.ac.ebi.intact.dataexchange.psimi.solr.complex.ComplexSolrSearcher;

@Controller
@RequestMapping("/search")
public class SearchController {

    @Autowired
    private ComplexSolrSearcher searcher ;
    private DataProvider dataProvider = new DataProvider() ;

    @RequestMapping(value = "/{query}",method = RequestMethod.GET)
	public @ResponseBody ComplexRestResult search(@PathVariable String query/*, @RequestParam (required = false) String format*/) {
        //if (format == null) format = "json";
        return dataProvider.getData(query, this.searcher);
	}
}