/**
 * Copyright 2008 The European Bioinformatics Institute, and others.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package uk.ac.ebi.intact.application.hierarchview.struts.controller;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import uk.ac.ebi.intact.application.hierarchview.business.IntactUserI;
import uk.ac.ebi.intact.application.hierarchview.exception.SessionExpiredException;
import uk.ac.ebi.intact.application.hierarchview.struts.framework.IntactBaseAction;
import uk.ac.ebi.intact.application.hierarchview.struts.view.ConfidenceFilterForm;
import uk.ac.ebi.intact.application.hierarchview.struts.view.utils.Compare;
import uk.ac.ebi.intact.application.hierarchview.struts.view.utils.SourceBean;
import uk.ac.ebi.intact.application.hierarchview.struts.view.utils.ConfidenceFilter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.*;

/**
 * TODO comment that class header
 * Wrapper form for the filter options in the confidenceTab.jsp
 *
 * @author Irina Armean (iarmean@ebi.ac.uk)
 * @version $Id$
 * @since 1.6.0-SNAPSHOT
 */
public class ConfidenceFilterAction extends IntactBaseAction {
    private static final Log logger = LogFactory.getLog( ConfidenceFilterAction.class );

    public ActionForward execute( ActionMapping mapping,
                                  ActionForm form,
                                  HttpServletRequest request,
                                  HttpServletResponse response )
            throws IOException, ServletException, SessionExpiredException {

        // Clear any previous errors.
        clearErrors();

        // get the current session
        HttpSession session = getSession( request );

        // retreive user fron the session
        IntactUserI user = getIntactUser( session );

        if ( form != null ) {
            // Use the NameForm to get the request parameters
            ConfidenceFilterForm confidenceFilterForm = ( ConfidenceFilterForm ) form;
            double score = confidenceFilterForm.getConfidenceValue();
            String relation = confidenceFilterForm.getRelation();
            double minScore = confidenceFilterForm.getMinConfidenceValue();
            double maxScore = confidenceFilterForm.getMaxConfidenceValue();
            String clusivity = confidenceFilterForm.getClusivity();

            String method = confidenceFilterForm.getMethod();

            List<SourceBean> sources = ( ArrayList ) session.getAttribute( "sources" );
            Set<String> selectedKeys = new HashSet<String>();
            if ( confidenceFilterForm.isFirstMethod() ) {
                selectedKeys = firstFilteringMethod( sources, score, relation );
            } else if (confidenceFilterForm.isSecondMethod()) {
                selectedKeys = secondFilteringMethod(sources, minScore, maxScore, confidenceFilterForm.isInclusive(), confidenceFilterForm.isExclusive());
            }

            user.setSelectedKeys( selectedKeys );
            ConfidenceFilter confFilter = new ConfidenceFilter(method, score, relation);
            confFilter.setLowerBoundary( minScore);
            confFilter.setUpperBoundary( maxScore );
            confFilter.setClusivity( clusivity );
            user.setConfidenceFilterValues( confFilter );

            return mapping.findForward( "success" );
        }

        // Forward control to the specified success URI
        return ( mapping.findForward( "success" ) );
    }

    private Set<String> secondFilteringMethod( List<SourceBean> sources, double minScore, double maxScore, boolean inclusive, boolean exclusive) {
        if (inclusive && exclusive){
            throw new IllegalArgumentException( "Both inclusive and exclusive are set!");
        }
        Set<String> selectedKeys = new HashSet<String>();
        String type = "Confidence";
        for ( Iterator<SourceBean> iter = sources.iterator(); iter.hasNext(); ) {
            SourceBean sourceBean = iter.next();
            if (sourceBean.getType().equalsIgnoreCase( type )){
                double sourceD = Double.valueOf( sourceBean.getId() );
                if (inclusive){
                    if (minScore <= sourceD && sourceD <= maxScore){
                        sourceBean.setSelected( true );
                        selectedKeys.add(sourceBean.getId());
                    }
                } else if (exclusive){
                     if (minScore < sourceD && sourceD < maxScore){
                        sourceBean.setSelected( true );
                        selectedKeys.add(sourceBean.getId());
                    }
                }
            }
        }

        return selectedKeys;
    }

    private Set<String> firstFilteringMethod( List<SourceBean> sources, double score, String relation ) {
        Set<String> selectedKeys = new HashSet<String>();
        String type = "Confidence";     //TODO: look from where i could get it

        for ( Iterator<SourceBean> iter = sources.iterator(); iter.hasNext(); ) {
            SourceBean sourceBean = iter.next();
            if ( sourceBean.getType().equalsIgnoreCase( type ) ) {
                Double sourced = Double.valueOf( sourceBean.getId() );
                if ( Compare.compare( sourced, score, relation ) ) {
                    sourceBean.setSelected( true );
                    selectedKeys.add( sourceBean.getId() );
                }
            }

        }
        return selectedKeys;
    }
}
