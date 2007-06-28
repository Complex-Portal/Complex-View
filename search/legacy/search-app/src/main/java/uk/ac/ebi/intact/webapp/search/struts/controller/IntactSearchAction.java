package uk.ac.ebi.intact.webapp.search.struts.controller;

import uk.ac.ebi.intact.webapp.search.business.IntactUserIF;
import uk.ac.ebi.intact.webapp.search.struts.framework.IntactBaseAction;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

/**
 * Abstraction of the IntactBaseAction. Gives access to the IntActUser.
 *
 * @author Michael Kleen
 * @version IntactSearchAction.java Date: Feb 17, 2005 Time: 4:13:44 PM
 */
public abstract class IntactSearchAction extends IntactBaseAction {

    private HttpServletRequest myRequest;

    /**
     * Specifies the request value.
     *
     * @param request a HttpServletRequest object specifying the request value
     */
    public void setRequest( final HttpServletRequest request ) {
        this.myRequest = request;
    }

    /**
     * Returns the intact user value.
     *
     * @return an Intact User.
     */
    public IntactUserIF getIntactUser() {

        // Session to access various session objects. This will create
        //a new session if one does not exist.
        HttpSession session = super.getSession( myRequest );

        // Handle to the Intact User.
        IntactUserIF user = super.getIntactUser( session );
        if ( user == null ) {
            //just set up a new user for the session
            user = super.setupUser( myRequest );
        }
        return user;
    }

    /**
     * Returns the search URL value.
     *
     * @return a search URL.
     */
    public String getSearchURL() {
        String contextPath = myRequest.getContextPath();
        String appPath = getServlet().getServletContext().getInitParameter( "searchLink" );
        String searchURL = contextPath.concat( appPath );
        return searchURL;
    }

}