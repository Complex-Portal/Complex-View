package uk.ac.ebi.intact.service.complex.view;

import org.jsoup.Jsoup;
import org.jsoup.examples.HtmlToPlainText;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Controller
public class WebAppController {
    @Autowired
    RestConnection restConnection;

    /****************************/
    /***   Public functions   ***/
    /****************************/

    // HOME
    @RequestMapping(value = "/", method = RequestMethod.GET)
    public String goHome(ModelMap model, HttpServletRequest request) {
        setDefaultModelMapValues(model, request);
        model.addAttribute("page_title", "Complex Home");
        return "home";
    }

    // SEARCH
    @RequestMapping(value = "/", method = RequestMethod.POST)
	public String search(@RequestParam String query,
                                   @RequestParam ( required = false ) String page,
                                   @RequestParam ( required = false ) String [] species,
                                   @RequestParam ( required = false ) String [] types,
                                   @RequestParam ( required = false ) String [] bioroles,
                                   @RequestParam ( required = false ) String facets,
                                   @RequestParam ( required = false ) String type,
                                   ModelMap model,
                                   HttpSession session,
                                   HttpServletRequest request)
    {
        model.addAttribute("complex_search_form", request.getRequestURL().toString());
        query = cleanQuery(query);
        String filters = buildFilters(species, types, bioroles);
        Page pageInfo = restConnection.getPage(page, query, filters, facets);
        ComplexRestResult results = restConnection.query(query, pageInfo, filters, facets, type);
        session.setAttribute("results", results);
        if (pageInfo.getTotalNumberOfElements() != 0) {
            if ( results != null ) {
                Map<String, List<ComplexFacetResults>> facetResults = results.getFacets();
                session.setAttribute("species", facetResults.get("species_f"));
                session.setAttribute("types", facetResults.get("ptype_f"));
                session.setAttribute("bioroles", facetResults.get("pbiorole_f"));
            }
            session.setAttribute("pageInfo", pageInfo);
            List<String> speciesList = null;
            if ( species != null ) {
                speciesList = new ArrayList<String>();
                Collections.addAll(speciesList, species);
            }
            session.setAttribute("speciesSelected",speciesList);
            List<String> typesList = null;
            if ( types != null ) {
                typesList = new ArrayList<String>();
                Collections.addAll(typesList, types);
            }
            session.setAttribute("typesSelected", typesList);
            List<String> biorolesList = null;
            if ( bioroles != null ) {
                biorolesList = new ArrayList<String>();
                Collections.addAll(biorolesList, bioroles);
            }
            session.setAttribute("biorolesSelected", biorolesList);
            setDefaultModelMapValues(model, request);
            model.addAttribute("page_title", "Complex Search");
            return "results";
        }
        else{
            setDefaultModelMapValues(model, request);
            return "noResults";
        }
	}

    // DETAILS
    @RequestMapping(value = "/details/{ac}", method = RequestMethod.GET)
    public String showDetails(@PathVariable String ac,
                                    ModelMap model,
                                    HttpSession session,
                                    HttpServletRequest request) {
        ComplexDetails details = restConnection.getDetails(cleanQuery(ac), QueryTypes.DETAILS.value);
        session.setAttribute("details", details);
        setDefaultModelMapValues(model, request);
        model.addAttribute("complex_search_form", request.getRequestURL().toString().split("details/")[0]);
        model.addAttribute("page_title", "Complex Details");
        return "details";
    }

    // HELP
    @RequestMapping(value = "/help/", method = RequestMethod.GET)
    public String goHelp(ModelMap model,
                                 HttpServletRequest request) {
        setDefaultModelMapValues(model, request);
        model.addAttribute("page_title", "Complex Help");
        model.addAttribute("complex_search_form", request.getRequestURL().toString().split("help/")[0]);
        return "help";
    }

    // DOCUMENTATION
    @RequestMapping(value = "/documentation/", method = RequestMethod.GET)
    public String goDocumentation(ModelMap model,
                                 HttpServletRequest request) {
        setDefaultModelMapValues(model, request);
        model.addAttribute("page_title", "Complex Documentation");
        model.addAttribute("complex_search_form", request.getRequestURL().toString().split("documentation/")[0]);
        return "documentation";
    }

    // ABOUT
    @RequestMapping(value = "/about/", method = RequestMethod.GET)
    public String goAbout(ModelMap model,
                                  HttpServletRequest request) {
        setDefaultModelMapValues(model, request);
        model.addAttribute("page_title", "Complex About");
        model.addAttribute("complex_search_form", request.getRequestURL().toString().split("about/")[0]);
        return "about";
    }

    /*****************************/
    /***   Private functions   ***/
    /*****************************/
    private void setDefaultModelMapValues(ModelMap model, HttpServletRequest request) {
        model.addAttribute("complex_portal_name", "Complex Portal");
        model.addAttribute("complex_home_url", request.getContextPath());
        model.addAttribute("complex_search_url", request.getContextPath());
        model.addAttribute("complex_help_url", request.getContextPath() + "/help/");
        model.addAttribute("complex_documentation_url", request.getContextPath() + "/documentation/");
        model.addAttribute("complex_contact_url", "mailto:intact-help@ebi.ac.uk?Subject=Complex%20Portal");
        model.addAttribute("complex_about_url", request.getContextPath() + "/about/" );
        model.addAttribute("intact_url", "http://www.ebi.ac.uk/intact/");
        model.addAttribute("facetFields", "species_f,ptype_f,pbiorole_f");
    }

    private String cleanQuery(String query) {
        query = new HtmlToPlainText().getPlainText(Jsoup.parse(query));
        return query.trim();
    }

    private String buildFilters( String[] species, String[] complexType, String[] sources ){
        StringBuilder filters = null;

        if ( species != null || complexType != null || sources != null ) {
            filters = new StringBuilder();
            if ( species != null ) {
                filters.append(buildFilter("species_f", species)).append(",");
            }
            if ( complexType != null ) {
                filters.append(buildFilter("ptype_f", complexType)).append(",");
            }
            if ( sources != null ) {
                filters.append(buildFilter("pbiorole_f", sources)).append(",");
            }
        }

        return filters != null ? filters.substring(0, filters.length() - 1) : null;
    }

    private String buildFilter ( String filterName, String [] filter ) {
        if ( filter != null ) {
            StringBuilder f = new StringBuilder().append(filterName).append(":(");
            for ( int i = 0 ; i < filter.length - 1 ; ++i ) {
                f.append("\"").append(filter[i]).append("\" ");
            }
            f.append("\"").append(filter[filter.length - 1]).append("\")");
            return f.toString();
        }
        return null;
    }


}