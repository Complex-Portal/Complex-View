/*
 * Copyright (c) 2002 The European Bioinformatics Institute, and others.
 * All rights reserved. Please see the file LICENSE
 * in the root directory of this distribution.
 */
package uk.ac.ebi.intact.application.statisticView.business.publications.jfreechart;

import uk.ac.ebi.intact.application.statisticView.business.publications.model.ExperimentBean;
import uk.ac.ebi.intact.application.statisticView.business.publications.model.PublicationStatisticsBean;

/**
 * @author ckohler (ckohler@ebi.ac.uk)
 * @version $Id$
 * @since <pre>24-Feb-2006</pre>
 */
public class StackedBarData {

    ////////////////////////
    // Instance variable

    private PublicationStatisticsBean publicationStatisticsBean;
    private ExperimentBean experimentBean;

    /////////////////////////
    // Constructor

    public StackedBarData( PublicationStatisticsBean publicationStatisticsBean,
                           ExperimentBean experimentBean
    ) {
        this.publicationStatisticsBean = publicationStatisticsBean;
        this.experimentBean = experimentBean;
    }

    //////////////////////////
    // Getters

    public PublicationStatisticsBean getPublicationStatisticsBean() {
        return publicationStatisticsBean;
    }

    public ExperimentBean getExperimentBean() {
        return experimentBean;
    }

    ///////////////////////////////
    // Object overload

    public String toString() {
        return "StackedBarData{" +
               "publicationStatisticsBean=" + publicationStatisticsBean +
               ", experimentBean=" + experimentBean +
               '}';
    }
}
