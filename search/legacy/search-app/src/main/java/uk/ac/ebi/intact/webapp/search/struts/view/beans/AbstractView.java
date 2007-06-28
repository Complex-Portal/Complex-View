/**
 * Copyright (c) 2002-2006 The European Bioinformatics Institute, and others.
 * All rights reserved. Please see the file LICENSE
 * in the root directory of this distribution.
 */
package uk.ac.ebi.intact.webapp.search.struts.view.beans;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import uk.ac.ebi.intact.webapp.search.SearchWebappContext;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

/**
 * TODO comment this
 *
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id:AbstractView.java 6452 2006-10-16 17:09:42 +0100 (Mon, 16 Oct 2006) baranda $
 * @since <pre>31-May-2006</pre>
 */
public abstract class AbstractView
{

    /**
     * Log for this class
     */
    public static final Log log = LogFactory.getLog(AbstractView.class);

    private HttpServletRequest request;
    private HttpSession session;

    public AbstractView(HttpServletRequest request)
    {
        this.request = request;
        this.session = request.getSession();
    }

    public int getCurrentPage(){
        return SearchWebappContext.getCurrentInstance().getCurrentPage();
    }

    public void setCurrentPage(int page){
        SearchWebappContext.getCurrentInstance().setCurrentPage(page);
    }

    public int getItemsPerPage() {
        return SearchWebappContext.getCurrentInstance().getResultsPerPage();
    }

    public abstract int getTotalItems();

    protected HttpServletRequest getRequest()
    {
        return request;
    }

    protected HttpSession getSession()
    {
        return session;
    }

}
