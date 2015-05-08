package uk.ac.ebi.intact.service.complex.view;

import org.jsoup.Jsoup;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.util.HtmlUtils;
import uk.ac.ebi.intact.dataexchange.psimi.solr.complex.ComplexFieldNames;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Controller
public class WebAppController {
    @Autowired
    RestConnection restConnection;
    private final String facets = new StringBuilder() .append(ComplexFieldNames.COMPLEX_ORGANISM_F).append(",")
                                    .append(ComplexFieldNames.INTERACTOR_TYPE_F).append(",")
                                    .append(ComplexFieldNames.BIOROLE_F).toString();

    /****************************/
    /***   Public functions   ***/
    /****************************/

    // POST SEARCH
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
                                   HttpServletRequest request) throws Exception {
        model.addAttribute("complex_search_form", request.getRequestURL().toString());
        model.addAttribute("page_title", "Complex Search");
        setDefaultModelMapValues(model, request);
        query = cleanQuery(query);
        if ( !query.equals("") && query.length()> 0 ) {
            String filters = buildFilters(species, types, bioroles);
            Page pageInfo = restConnection.getPage(page, query, filters, this.facets);
            if (pageInfo.getTotalNumberOfElements() != 0) {
                ComplexRestResult results = restConnection.query(query, pageInfo, filters, this.facets, type);
                session.setAttribute("pageInfo", pageInfo);
                session.setAttribute("results", results);
                if (results != null) {
                    Map<String, List<ComplexFacetResults>> facetResults = results.getFacets();
                    session.setAttribute("species", facetResults.get(ComplexFieldNames.COMPLEX_ORGANISM_F));
                    session.setAttribute("types", facetResults.get(ComplexFieldNames.INTERACTOR_TYPE_F));
                    session.setAttribute("bioroles", facetResults.get(ComplexFieldNames.BIOROLE_F));
                }
                List<String> speciesList = null;
                if (species != null) {
                    speciesList = new ArrayList<String>();
                    Collections.addAll(speciesList, species);
                }
                session.setAttribute("speciesSelected", speciesList);
                List<String> typesList = null;
                if (types != null) {
                    typesList = new ArrayList<String>();
                    Collections.addAll(typesList, types);
                }
                session.setAttribute("typesSelected", typesList);
                List<String> biorolesList = null;
                if (bioroles != null) {
                    biorolesList = new ArrayList<String>();
                    Collections.addAll(biorolesList, bioroles);
                }
                session.setAttribute("biorolesSelected", biorolesList);
                return "results";
            } else {
                return "noResults";
            }
        }
        return "home";
	}

    // GET SEARCH
    @RequestMapping(value = "/", method = RequestMethod.GET)
    public String search(@RequestParam ( required = false ) String q,
                         @RequestParam ( required = false ) String page,
                         @RequestParam ( required = false ) String [] species,
                         @RequestParam ( required = false ) String [] types,
                         @RequestParam ( required = false ) String [] bioroles,
                         @RequestParam ( required = false ) String type,
                         ModelMap model,
                         HttpSession session,
                         HttpServletRequest request) throws Exception {
        model.addAttribute("complex_search_form", request.getRequestURL().toString());
        setDefaultModelMapValues(model, request);
        if ( q !=null && !q.equals("") && q.length()> 0 ) {
            model.addAttribute("page_title", "Complex Search");
            q = cleanQuery(q);
            session.setAttribute("htmlOriginalQuery", HtmlUtils.htmlEscape(q));
            String filters = buildFilters(species, types, bioroles);
            Page pageInfo = restConnection.getPage(page, q, filters, facets);
            if (pageInfo.getTotalNumberOfElements() != 0) {
                ComplexRestResult results = restConnection.query(q, pageInfo, filters, facets, type);
                session.setAttribute("pageInfo", pageInfo);
                session.setAttribute("results", results);
                if (results != null) {
                    Map<String, List<ComplexFacetResults>> facetResults = results.getFacets();
                    session.setAttribute("species", facetResults.get(ComplexFieldNames.COMPLEX_ORGANISM_F));
                    session.setAttribute("types", facetResults.get(ComplexFieldNames.INTERACTOR_TYPE_F));
                    session.setAttribute("bioroles", facetResults.get(ComplexFieldNames.BIOROLE_F));
                }
                List<String> speciesList = null;
                if (species != null) {
                    speciesList = new ArrayList<String>();
                    Collections.addAll(speciesList, species);
                }
                session.setAttribute("speciesSelected", speciesList);
                List<String> typesList = null;
                if (types != null) {
                    typesList = new ArrayList<String>();
                    Collections.addAll(typesList, types);
                }
                session.setAttribute("typesSelected", typesList);
                List<String> biorolesList = null;
                if (bioroles != null) {
                    biorolesList = new ArrayList<String>();
                    Collections.addAll(biorolesList, bioroles);
                }
                session.setAttribute("biorolesSelected", biorolesList);
                return "results";
            } else {
                return "noResults";
            }
        }
        model.addAttribute("page_title", "Complex Home");
        Page total = restConnection.getPage(null, "*", null, this.facets);
        ComplexRestResult result = restConnection.query("*", total, null, this.facets, null);
        session.setAttribute("stats_total", total.getTotalNumberOfElements());
        for (String key : result.getFacets().keySet()) {
            if (key.equalsIgnoreCase(ComplexFieldNames.COMPLEX_ORGANISM_F)) {
                session.setAttribute("stats_species", result.getFacets().get(key).size());
            }
            if (key.equalsIgnoreCase(ComplexFieldNames.INTERACTOR_TYPE_F)) {
                session.setAttribute("stats_interactor", result.getFacets().get(key).size());
            }
            if (key.equalsIgnoreCase(ComplexFieldNames.BIOROLE_F)) {
                session.setAttribute("stats_biorole", result.getFacets().get(key).size());
            }
        }
        return "home";
    }

    // DETAILS
    @RequestMapping(value = "/details/{ac}", method = RequestMethod.GET)
    public String showDetails(@PathVariable String ac,
                                    ModelMap model,
                                    HttpSession session,
                                    HttpServletRequest request) throws Exception {
        ComplexDetails details = restConnection.getDetails(cleanQuery(ac), QueryTypes.DETAILS.value);
        session.setAttribute("details", details);
        String json = restConnection.getJsonToVisualize(ac);
        model.addAttribute("json_rest", json);
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

    // STATISTICS
    @RequestMapping(value = "/stats/", method = RequestMethod.GET)
    public String goStatistics(ModelMap model,
                               HttpServletRequest request,
                               HttpSession session) throws Exception {
        setDefaultModelMapValues(model, request);
        Page total = restConnection.getPage(null, "*", null, this.facets);
        ComplexRestResult result = restConnection.query("*", total, null, this.facets, null);
//        session.setAttribute("stats_total", total.getTotalNumberOfElements());
        for (String key : result.getFacets().keySet()) {
            if (key.equalsIgnoreCase(ComplexFieldNames.COMPLEX_ORGANISM_F)) {
                session.setAttribute("stats_species", result.getFacets().get(key));
            }
            if (key.equalsIgnoreCase(ComplexFieldNames.INTERACTOR_TYPE_F)) {
                session.setAttribute("stats_interactor", result.getFacets().get(key));
            }
            if (key.equalsIgnoreCase(ComplexFieldNames.BIOROLE_F)) {
                session.setAttribute("stats_biorole", result.getFacets().get(key));
            }
        }
        model.addAttribute("page_title", "Complex Statistics");
        model.addAttribute("complex_search_form", request.getRequestURL().toString().split("stats/")[0]);
        return "stats";
    }

    // DOWNLOAD
    @RequestMapping(value = "/download/", method = RequestMethod.GET)
    public String goDownload(ModelMap model,
                          HttpServletRequest request) {
        setDefaultModelMapValues(model, request);
        model.addAttribute("page_title", "Complex Download");
        model.addAttribute("complex_search_form", request.getRequestURL().toString().split("download/")[0]);
        return "download";
    }

    /*****************************/
    /***   Private functions   ***/
    /*****************************/
    private void setDefaultModelMapValues(ModelMap model, HttpServletRequest request) {
        model.addAttribute("complex_portal_name", "Complex Portal");
        model.addAttribute("complex_home_url", request.getContextPath());
        model.addAttribute("complex_search_url", request.getContextPath());
        model.addAttribute("complex_download_url", request.getContextPath() + "/download/");
        model.addAttribute("complex_help_url", request.getContextPath() + "/help/");
        model.addAttribute("complex_documentation_url", request.getContextPath() + "/documentation/");
        model.addAttribute("complex_contact_url", "http://www.ebi.ac.uk/support/index.php?query=intact");
        model.addAttribute("complex_about_url", request.getContextPath() + "/about/" );
        model.addAttribute("complex_stats_url", request.getContextPath() + "/stats/");
        model.addAttribute("intact_url", "http://www.ebi.ac.uk/intact/");
        model.addAttribute("complex_ftp_url", this.restConnection.getFtpUrl());
        model.addAttribute("complex_download_form", this.restConnection.getWSUrl() + "export/");
    }

    private String cleanQuery(String query) {
        return Jsoup.parse(query).text().trim();
    }

    private String buildFilters( String[] species, String[] complexType, String[] sources ){
        StringBuilder filters = null;

        if ( species != null || complexType != null || sources != null ) {
            filters = new StringBuilder();
            if ( species != null ) {
                filters.append(buildFilter(ComplexFieldNames.COMPLEX_ORGANISM_F, species)).append(",");
            }
            if ( complexType != null ) {
                filters.append(buildFilter(ComplexFieldNames.INTERACTOR_TYPE_F, complexType)).append(",");
            }
            if ( sources != null ) {
                filters.append(buildFilter(ComplexFieldNames.BIOROLE_F, sources)).append(",");
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

    @ExceptionHandler(ComplexPortalException.class)
    public ModelAndView handleComplexPortalException(ComplexPortalException e, HttpServletResponse response, HttpServletRequest request, HttpSession session){
        ModelAndView model = new ModelAndView("error/503");
        setDefaultModelMapValues(model.getModelMap(), request);
        return model;
    }

    @ExceptionHandler(Exception.class)
    public ModelAndView allExceptions(Exception e, HttpServletResponse response, HttpServletRequest request, HttpSession session){
        ModelAndView model = new ModelAndView("error/404");
        setDefaultModelMapValues(model.getModelMap(), request);
        return model;
    }

}