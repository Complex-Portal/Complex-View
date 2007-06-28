package uk.ac.ebi.intact.webapp.search.struts.controller;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import uk.ac.ebi.intact.model.Searchable;
import uk.ac.ebi.intact.searchengine.SearchClass;
import uk.ac.ebi.intact.webapp.search.SearchWebappContext;
import uk.ac.ebi.intact.webapp.search.struts.framework.IntactBaseAction;
import uk.ac.ebi.intact.webapp.search.struts.util.SearchConstants;
import uk.ac.ebi.intact.webapp.search.struts.view.beans.SingleResultViewBean;
import uk.ac.ebi.intact.webapp.search.struts.view.beans.TooLargeViewBean;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.Collection;
import java.util.Map;


/**
 * Provides the actions to handle the case if the retriving resultset from the searchaction is too big. The Action
 * creates a basic statistic which gives an overview over the resultsset. this statistics will be shown in the
 * web-interface.
 *
 * @author Michael Kleen (mkleen@ebi.ac.uk)
 * @version $Id:TooLargeAction.java 6452 2006-10-16 17:09:42 +0100 (Mon, 16 Oct 2006) baranda $
 */
public class TooLargeAction extends IntactBaseAction {

    private static final Log logger = LogFactory.getLog(TooLargeAction.class);

    /**
     * Counts the complete result information in 4 different categories this is necessary because all controlled
     * vocabulary terms should count in the same category.
     *
     * @param mapping  The ActionMapping used to select this instance
     * @param form     The optional ActionForm bean for this request (if any)
     * @param request  The HTTP request we are processing
     * @param response The HTTP response we are creating
     *
     * @return an ActionForward object
     */
    public ActionForward execute( ActionMapping mapping, ActionForm form,
                                  HttpServletRequest request,
                                  HttpServletResponse response ) {

        logger.debug( "tooLarge action: the resultset contains to many objects" );

        //TODO use here a iteratable map instead

        // get the resultinfo from the initial request from the search action
        final Map<Class<? extends Searchable>,Integer> resultInfo =
                SearchWebappContext.getCurrentInstance().getCurrentResultCount();
        logger.debug( "Result info: "+ resultInfo );

        final Collection someKeys = resultInfo.keySet();

        int cvCount = 0;
        int proteinCount = 0;
        int nucleicAcidCount = 0;
        int experimentCount = 0;
        int interactionCount = 0;
        int smallMoleculeCount = 0;

        // count for any type of searchable objects in the resultset to generate the statistic
        // this is done by creating from the classname a class and check then for classtype

        for (Object objClass : someKeys)
        {
            Class clazz;

            if (objClass instanceof Class)
            {
                clazz = (Class)objClass;
            }
            else
            {
                try
                {
                    clazz = Class.forName(objClass.toString());
                }
                catch (ClassNotFoundException e)
                {
                    logger.error("tooLarge action: the resultset contains to an object which is no " +
                        "assignable from an intactType: "+objClass.toString());
                    return mapping.findForward(SearchConstants.FORWARD_FAILURE);
                }
            }

            String className = clazz.getName();

            logger.debug("tooLarge action: searching for class " + className);

            SearchClass searchClass = SearchClass.valueOfMappedClass(clazz);

            if (searchClass == SearchClass.PROTEIN)
            {
                proteinCount += resultInfo.get(objClass);
            }
            if (searchClass == SearchClass.NUCLEIC_ACID)
            {
                nucleicAcidCount += resultInfo.get(objClass);
            }
            else if (searchClass == SearchClass.EXPERIMENT)
            {
                experimentCount += resultInfo.get(objClass);
            }
            else if (searchClass == SearchClass.INTERACTION)
            {
                interactionCount += resultInfo.get(objClass);
            }
            else if (searchClass == SearchClass.SMALL_MOLECULE)
            {
                smallMoleculeCount += resultInfo.get(objClass);
            }
            else if (searchClass.isCvObjectSubclass())
            {
                cvCount += resultInfo.get(objClass);
            }
        } // for

        // get the helplink count the results and create with them  a couple of viewbeans for the jsp

        HttpSession session = super.getSession( request );

        Object objQuery = session.getAttribute( SearchConstants.SEARCH_CRITERIA );

        if (objQuery == null) throw new NullPointerException("Attribute: searchCriteria");

        String query = objQuery.toString();


        TooLargeViewBean tooLargeViewBean = new TooLargeViewBean();

        if ( experimentCount > 0 ) {
            tooLargeViewBean.add( new SingleResultViewBean( SearchClass.EXPERIMENT.getShortName(),
                                                            experimentCount, query ) );
        }

        if ( interactionCount > 0 ) {
            tooLargeViewBean.add( new SingleResultViewBean( SearchClass.INTERACTION.getShortName(),
                                                            interactionCount, query ) );
        }

        if ( proteinCount > 0 ) {
            tooLargeViewBean.add( new SingleResultViewBean( SearchClass.PROTEIN.getShortName(),
                                                            proteinCount, query ) );
        }

        if ( nucleicAcidCount > 0 ) {
            tooLargeViewBean.add( new SingleResultViewBean( SearchClass.NUCLEIC_ACID.getShortName(),
                                                            nucleicAcidCount, query ) );
        }

        if ( smallMoleculeCount > 0 ) {
            tooLargeViewBean.add( new SingleResultViewBean( SearchClass.SMALL_MOLECULE.getShortName(),
                                                            smallMoleculeCount, query ) );
        }

        if ( cvCount > 0 ) {
            tooLargeViewBean.add( new SingleResultViewBean( SearchClass.CV_OBJECT.getShortName(), cvCount, query ) );
        }

        // add the viewbean to the request and forward to the jsp
        request.setAttribute( SearchConstants.VIEW_BEAN, tooLargeViewBean );
        return mapping.findForward( SearchConstants.FORWARD_RESULTS );
    }
}