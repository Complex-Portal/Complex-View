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
package uk.ac.ebi.intact.application.hierarchview.struts.view.utils;

/**
 * Container for the confidence filter data.
 *
 * @author Irina Armean (iarmean@ebi.ac.uk)
 * @version $Id$
 * @since 1.6.0-SNAPSHOT
 */
public class ConfidenceFilter {

    private String method;
    // for the first method
    private double thresholdScore;
    private String relation;

    // for the second method
    private double lowerBoundary;
    private double upperBoundary;
    private String clusivity;

    private ConfidenceFilter(){
        this.method = "";
        thresholdScore =0;
        relation ="";
        lowerBoundary = 0;
        upperBoundary = 0;
        clusivity = "";
    }

    public ConfidenceFilter( String method, double thresholdScore, String relation, String clusivity, double lowerBoundary,  double upperBoundary ) {
        this();
        this.method = method;
        this.clusivity = clusivity;
        this.lowerBoundary = lowerBoundary;
        this.thresholdScore = thresholdScore;
        this.relation = relation;
        this.upperBoundary = upperBoundary;
    }

    public ConfidenceFilter( String method, double thresholdScore, String relation) {
        this();
        this.thresholdScore = thresholdScore;
        this.relation = relation;
        this.method = method;
    }

    public ConfidenceFilter( String method, String clusivity, double upperBoundary, double lowerBoundary ) {
        this();
        this.method = method;
        this.clusivity = clusivity;
        this.upperBoundary = upperBoundary;
        this.lowerBoundary = lowerBoundary;
    }

    public double getThresholdScore() {
        return thresholdScore;
    }

    public void setThresholdScore( double thresholdScore ) {
        this.thresholdScore = thresholdScore;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod( String method ) {
        this.method = method;
    }

    public String getRelation() {
        return relation;
    }

    public void setRelation( String relation ) {
        this.relation = relation;
    }

    public double getLowerBoundary() {
        return lowerBoundary;
    }

    public void setLowerBoundary( double lowerBoundary ) {
        this.lowerBoundary = lowerBoundary;
    }

    public double getUpperBoundary() {
        return upperBoundary;
    }

    public void setUpperBoundary( double upperBoundary ) {
        this.upperBoundary = upperBoundary;
    }

    public String getClusivity() {
        return clusivity;
    }

    public void setClusivity( String clusivity ) {
        this.clusivity = clusivity;
    }
}
