package uk.ac.ebi.intact.service.complex.view;

import org.jsoup.Jsoup;
import org.jsoup.examples.HtmlToPlainText;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

@Controller
public class WebAppController {
    @Autowired
    RestConnection restConnection;

    /****************************/
    /***   Public functions   ***/
    /****************************/

    // HOME
    @RequestMapping(value = "/", method = RequestMethod.GET)
    public String HomeController(ModelMap model, HttpServletRequest request) {
        defaultModelMapValues(model, request);
        model.addAttribute("page_title", "Complex Home");
        return "home";
    }

    // SEARCH
    @RequestMapping(value = "/", method = RequestMethod.POST)
	public String SearchController(@RequestParam String query,
                                   @RequestParam ( required = false ) String page,
                                   @RequestParam ( required = false ) String filter,
                                   @RequestParam ( required = false ) String type,
                                   ModelMap model,
                                   HttpSession session,
                                   HttpServletRequest request)
    {
        query = cleanQuery(query);
        Page pageInfo = restConnection.getPage(page, query);
        ComplexRestResult results = restConnection.query(query, pageInfo, filter, type);
		session.setAttribute("results", results);
        session.setAttribute("pageInfo", pageInfo);
        defaultModelMapValues(model, request);
        model.addAttribute("page_title", "Complex Search");
        return "results";
	}

    // DETAILS
    @RequestMapping(value = "/details/{ac}", method = RequestMethod.GET)
    public String DetailsController(@PathVariable String ac,
                                    ModelMap model,
                                    HttpSession session,
                                    HttpServletRequest request) {
        ComplexDetails details = restConnection.getDetails(cleanQuery(ac), QueryTypes.DETAILS.value);
        session.setAttribute("details", details);
        defaultModelMapValues(model, request);
        model.addAttribute("page_title", "Complex Details");
        return "details";
    }

    // HELP
    @RequestMapping(value = "/help/", method = RequestMethod.GET)
    public String HelpController(ModelMap model,
                                 HttpServletRequest request) {
        defaultModelMapValues(model, request);
        model.addAttribute("page_title", "Complex Help");
        return "help";
    }

    /*****************************/
    /***   Private functions   ***/
    /*****************************/
    private void defaultModelMapValues(ModelMap model, HttpServletRequest request) {
        model.addAttribute("complex_portal_name", "Intact Complex Portal");
        model.addAttribute("complex_home_url", request.getContextPath());
        model.addAttribute("complex_help_url", request.getContextPath() + "/help/");
        //model.addAttribute("complex_documentation_url", request.getContextPath() + "/documentation/");
        //model.addAttribute("complex_about_url", request.getContextPath() + "/about/" );
    }

    private String cleanQuery(String query) {
        query = new HtmlToPlainText().getPlainText(Jsoup.parse(query));
        return query.trim();
    }

}