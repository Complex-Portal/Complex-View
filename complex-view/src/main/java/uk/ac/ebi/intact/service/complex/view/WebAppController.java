package uk.ac.ebi.intact.service.complex.view;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpSession;

@Controller
public class WebAppController {
    @Autowired
    RestConnection restConnection;

    @RequestMapping(value = "/{query}", method = RequestMethod.GET)
	public String SearchController(@PathVariable String query,
                                   @RequestParam ( required = false ) String first,
                                   @RequestParam ( required = false ) String number,
                                   @RequestParam ( required = false ) String filter,
                                   @RequestParam ( required = false ) String type,
                                   ModelMap model,
                                   HttpSession session)
    {
		session.setAttribute("table",restConnection.query (query,
                                                           first,
                                                           number,
                                                           filter,
                                                           type) );
		return "results";
	}
}