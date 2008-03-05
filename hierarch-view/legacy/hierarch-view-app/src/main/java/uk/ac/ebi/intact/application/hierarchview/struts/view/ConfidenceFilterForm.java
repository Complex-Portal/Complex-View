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
package uk.ac.ebi.intact.application.hierarchview.struts.view;

import uk.ac.ebi.intact.application.hierarchview.struts.framework.IntactBaseForm;
import uk.ac.ebi.intact.application.hierarchview.struts.view.utils.ConfidenceFilter;
import uk.ac.ebi.intact.application.hierarchview.business.Constants;
import uk.ac.ebi.intact.application.hierarchview.business.IntactUserI;
import uk.ac.ebi.intact.application.hierarchview.business.IntactUser;
import org.apache.log4j.Logger;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionErrors;

import javax.servlet.http.HttpServletRequest;

/**
 * Form bean for the confidence tab filter form of the confidenceTab2.jsp page.
 * This form has the following fields, with default values in square brackets:
 * score - entering the threshold score
 * relation - drop down list with options
 *
 * @author Irina Armean (iarmean@ebi.ac.uk)
 * @version $Id$
 * @since 1.6.0-SNAPSHOT
 */
public class ConfidenceFilterForm extends IntactBaseForm {
    public static Logger logger = Logger.getLogger( Constants.LOGGER_NAME );

    // --------------------------------------------------- Instance Variables

    /**
        * Identifier for when "first method" button is pressed.
        */
       private static final String first = "FIRST_METHOD";

       /**
        * Identifier for when "second method" button is pressed.
        */
       private static final String second ="SECOND_METHOD";


    private static final String inclusive = "INCLUSIVE";
    private static final String exclusive = "EXCLUSIVE";

    // the queryString of the confidence value threshold to filter.
    private String relation = null;
    private Double confidenceValue = null;

    private Double minConfidenceValue = null;
    private Double maxConfidenceValue = null;
    private String clusivity = null; // inclusive or exclusive

    /**
     * The filtering method choosen: with value and relation or between (a,b) + inclusive/exclusive
     * That attribute can be null because in case only one method is available,
     * the form dont show the choice to make easier the user interface.
     */
    private String method = null;   //"Confidence";

    private String actionName;

    public boolean isFirstMethod() {
        return first.equalsIgnoreCase( method );
    }

    public boolean isSecondMethod(){
        return second.equalsIgnoreCase( "SECOND_METHOD");
    }

    public Double getConfidenceValue() {
        return confidenceValue;
    }   

    public void setConfidenceValue( Double confidenceValue ) {
        this.confidenceValue = confidenceValue;
    }

    public String getRelation() {
        return relation;
    }

    public void setRelation( String relation ) {
        this.relation = relation;
    }

    public Double getMinConfidenceValue() {
        return minConfidenceValue;
    }

    public void setMinConfidenceValue( Double minConfidenceValue ) {
        this.minConfidenceValue = minConfidenceValue;
    }

    public Double getMaxConfidenceValue() {
        return maxConfidenceValue;
    }

    public void setMaxConfidenceValue( Double maxConfidenceValue ) {
        this.maxConfidenceValue = maxConfidenceValue;
    }

    public boolean isInclusive(){
        return inclusive.equalsIgnoreCase( clusivity );
    }

    public boolean isExclusive(){
        return exclusive.equalsIgnoreCase( clusivity );
    }

    public String getClusivity() {
        return clusivity;
    }

    public void setClusivity( String clusivity ) {
        this.clusivity = clusivity;
    }

    /**
     * Sets the action.
     *
     * @param action the action for the form.
     */
    public void setAction( String action ) {
        actionName = action;
    }


    public String getAction() {
        return actionName;
    }

    public String getMethod() {
        return ( this.method );
    }


    public void setMethod( String method ) {
        this.method = method;
    }



    /**
     * Reset all properties to their default values.
     *
     * @param mapping The mapping used to select this instance
     * @param request The servlet request we are processing
     */
    public void reset( ActionMapping mapping, HttpServletRequest request ) {

        this.method = null;
        this.relation = null;
        this.confidenceValue = new Double(0.00);
        this.minConfidenceValue = new Double(0.00);
        this.maxConfidenceValue = new Double(0.00);
        this.clusivity = null;

        IntactUserI user = IntactUser.getCurrentInstance( request.getSession() );

        if (user != null) {
            final ConfidenceFilter confidenceFilterValues = user.getConfidenceFilterValues();

            if (confidenceFilterValues != null) {
                setMethod( confidenceFilterValues.getMethod() );
                setMinConfidenceValue( confidenceFilterValues.getLowerBoundary() );
                setMaxConfidenceValue( confidenceFilterValues.getUpperBoundary() );
                setRelation( confidenceFilterValues.getRelation() );
                setConfidenceValue( confidenceFilterValues.getThresholdScore() );
                setClusivity( confidenceFilterValues.getClusivity() );
            } else {
                setMethod ("FIRST_METHOD");
                setClusivity( "INCLUSIVE" );
            }

        }

    } // reset


    /**
     * Validate the properties that have been set from this HTTP request,
     * and return an <code>ActionErrors</code> object that encapsulates any
     * validation errors that have been found.  If no errors are found, return
     * <code>null</code> or an <code>ActionErrors</code> object with no
     * recorded error messages.
     *
     * @param mapping The mapping used to select this instance
     * @param request The servlet request we are processing
     */
    public ActionErrors validate( ActionMapping mapping,
                                  HttpServletRequest request ) { 

        if ( confidenceValue < 0 || confidenceValue > 1){
            addError("error.filter.score.bounded");
        }

        if (( confidenceValue == 0 && relation.equals(">") ) || ( confidenceValue == 1 && relation.equals("<"))){
            addMessage("error.filter.score.semantic");
        }

        if (minConfidenceValue != 0 || maxConfidenceValue != 0){
            if (minConfidenceValue > maxConfidenceValue){
                addMessage("error.filter.score.semantic.between");
            }
        }

        if ( !isMessagesEmpty() ) {
            /* save messages in the context, that feature is not included in Struts 1.1
             * currently it's only possible to manage ActionErrors when validating a form.
             */
            saveMessages( request );
        }

        return getErrors();
    }


    public String toString() {
        StringBuffer sb = new StringBuffer( "FillterForm[confidenceValue=" );
        sb.append( confidenceValue );
        if ( relation != null ) {
            sb.append( ", relation=" );
            sb.append( relation );
        }
        sb.append( "]" );
        return ( sb.toString() );
    }
}
