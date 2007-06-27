/*
Copyright (c) 2002 The European Bioinformatics Institute, and others.
All rights reserved. Please see the file LICENSE
in the root directory of this distribution.
*/
package uk.ac.ebi.intact.application.statisticView.struts.view;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * User: Michael Kleen mkleen@ebi.ac.uk Date: Mar 17, 2005 Time: 5:16:48 PM
 */
public class IntactStatisticsBean {

    private static final Log log = LogFactory.getLog( IntactStatisticsBean.class );

    private final String contextPath;
    private String experimentChartUrl;
    private String interactionChartUrl;
    private String proteinChartUrl;
    private String binaryChartUrl;
    private String cvTermChartUrl;
    private String bioSourceChartUrl;
    private String evidenceChartUrl;
    private String detectionChartUrl;
    private int experimentCount;
    private int interactionCount;
    private int proteinCount;
    private int cvTermCount;
    private int binaryInteractionCount;


    public IntactStatisticsBean( String contextPath ) {
        this.contextPath = contextPath;
    }


    public int getInteractionCount() {
        return interactionCount;
    }

    public void setInteractionCount( int interactionCount ) {
        this.interactionCount = interactionCount;
    }

    public int getProteinCount() {
        return proteinCount;
    }

    public void setProteinCount( int proteinCount ) {
        this.proteinCount = proteinCount;
    }


    public int getExperimentCount() {
        return experimentCount;
    }

    public void setExperimentCount( int experimentCount ) {
        this.experimentCount = experimentCount;
    }

    public int getCvTermCount() {
        return cvTermCount;
    }

    public void setCvTermCount( int cvTermCount ) {
        this.cvTermCount = cvTermCount;
    }

    public int getBinaryInteractionCount() {
        return binaryInteractionCount;
    }

    public void setBinaryInteractionCount( int binaryInteractionCount ) {
        this.binaryInteractionCount = binaryInteractionCount;
    }

    public String getExperimentChartUrl() {
        return contextPath + experimentChartUrl;
    }

    public String getInteractionChartUrl() {
        return contextPath + interactionChartUrl;
    }

    public String getProteinChartUrl() {
        return contextPath + proteinChartUrl;
    }

    public String getBinaryChartUrl() {
        return contextPath + binaryChartUrl;
    }

    public String getCvTermChartUrl() {
        return contextPath + cvTermChartUrl;
    }

    public String getBioSourceChartUrl() {
        return contextPath + bioSourceChartUrl;
    }

    public String getEvidenceChartUrl() {
        return contextPath + evidenceChartUrl;
    }

    public String getDetectionChartUrl() {
        return contextPath + detectionChartUrl;
    }


    public void setExperimentChartUrl( String experimentChartUrl ) {
        this.experimentChartUrl = experimentChartUrl;
    }

    public void setInteractionChartUrl( String interactionChartUrl ) {
        this.interactionChartUrl = interactionChartUrl;
    }

    public void setProteinChartUrl( String proteinChartUrl ) {
        this.proteinChartUrl = proteinChartUrl;
    }

    public void setBinaryChartUrl( String binaryChartUrl ) {
        this.binaryChartUrl = binaryChartUrl;
    }

    public void setCvTermChartUrl( String cvTermChartUrl ) {
        this.cvTermChartUrl = cvTermChartUrl;
    }

    public void setBioSourceChartUrl( String bioSourceChartUrl ) {
        this.bioSourceChartUrl = bioSourceChartUrl;
    }

    public void setEvidenceChartUrl( String evidenceChartUrl ) {
        this.evidenceChartUrl = evidenceChartUrl;
    }

    public void setDetectionChartUrl( String detectionChartUrl ) {
        this.detectionChartUrl = detectionChartUrl;
    }

    public static final String PROTEINS = "Proteins";
    public static final String INTERACTIONS = "Interactions";
    public static final String BINARY_INTERACTIONS = "Binary interactions";
    public static final String EXPERIMENTS = "Experiments";
    public static final String CV_TERMS = "Terms";
    public static final String INTERACTIONS_PER_BIOSOURCE = "Interactions per organism";
    public static final String INTERACTIONS_PER_IDENTIFICATION = "Interactions per identification method";

    public List getDisplayBeans() {
        List result = new ArrayList();

        result.add( new DisplayStatisticsBean( PROTEINS,
                                               proteinCount + "",
                                               "Number of proteins in the database" ) );

        result.add( new DisplayStatisticsBean( INTERACTIONS,
                                               interactionCount + "",
                                               "Number of interactions and complexes" ) );

        result.add( new DisplayStatisticsBean( BINARY_INTERACTIONS,
                                               binaryInteractionCount + "",
                                               "Number of interactions, n-ary interactions expanded according to the \"spoke\" model" ) );

        result.add( new DisplayStatisticsBean( EXPERIMENTS,
                                               experimentCount + "",
                                               "Distinct experiments" ) );

        result.add( new DisplayStatisticsBean( CV_TERMS,
                                               cvTermCount + "",
                                               "Controlled vocabulary terms" ) );

        result.add( new DisplayStatisticsBean( INTERACTIONS_PER_BIOSOURCE,
                                               "-",
                                               "<a href=\"#" + INTERACTIONS_PER_BIOSOURCE + "\" class=\"red_bold_small\">(Link to detailed statistics)</a>" ) );

        result.add( new DisplayStatisticsBean( INTERACTIONS_PER_IDENTIFICATION,
                                               "-",
                                               "<a href=\"#" + INTERACTIONS_PER_IDENTIFICATION + "\" class=\"red_bold_small\">(Link to detailed statistics)</a>" ) );

        return result;
    }
}