/*
 Copyright (c) 2002-2005 The European Bioinformatics Institute, and others.
 All rights reserved. Please see the file LICENSE
 in the root directory of this distribution.
 */

package uk.ac.ebi.intact.application.editor.struts.view.interaction;

import uk.ac.ebi.intact.application.commons.util.XrefHelper;
import uk.ac.ebi.intact.application.editor.business.EditorService;
import uk.ac.ebi.intact.application.editor.struts.view.AbstractEditBean;
import uk.ac.ebi.intact.application.editor.struts.view.wrappers.ResultRowData;
import uk.ac.ebi.intact.model.Experiment;
import uk.ac.ebi.intact.model.Xref;

import java.util.Iterator;
import java.util.Date;

/**
 * This class contains data for an Experiment row in the Interaction editor.
 *
 * @author Sugath Mudali (smudali@ebi.ac.uk)
 * @version $Id$
 */
public class ExperimentRowData extends ResultRowData {

    /**
     * The underlying Experiment. Could be null if none assigned to it (e.g. search).
     */
    private Experiment myExperiment;

    /**
     * This contains HTML script for pubmed link.
     */
    private String myPubMedLink;

    /**
     * The short label as a link.
     */
    private String myShortLabelLink;

    /**
     * This constructor is mainly used for creating an instance to find it in a
     * collection.
     * @param ac the ac is required as it is used for equals method.
     */
    public ExperimentRowData(String ac) {
        this(ac, null, null, null, null, null, null);
    }

    /**
     * Creates an instance of this class using given Experiment.
     * @param exp the Experiment to wrap this instance around.
     */
    public ExperimentRowData(Experiment exp) {
        this(exp.getAc(), exp.getShortLabel(), exp.getFullName(), exp.getCreator(),
             exp.getUpdator(), exp.getCreated(),exp.getUpdated());
        myExperiment = exp;
        for (Iterator iter = exp.getXrefs().iterator(); iter.hasNext(); ) {
            Xref xref = (Xref) iter.next();
            if (xref.getCvXrefQualifier().getShortLabel().equals("primary-reference")) {
                setPubMedLink(xref);
            }
        }
    }

    /**
     * Creates an instance of this class using ac, shortlabel and fullname.
     * @param ac
     * @param shortlabel
     * @param fullname
     */

     public ExperimentRowData(String ac, String shortlabel, String fullname, String creator, String updator, Date created, Date updated) {
        super(ac, shortlabel, fullname, creator, updator,created,updated);
        if (shortlabel != null) {
            setShortLabelLink();
        }
    }

    public Experiment getExperiment() {
        return myExperiment;
    }

    /**
     * Override the super method to return the topic with a link to show its
     * contents in a window.
     * @return the topic as a browsable link.
     */
    public String getShortLabelLink() {
        return myShortLabelLink;
    }

    public void setPubMedLink(Xref xref) {
        String link = XrefHelper.getPrimaryIdLink(xref);
        // javascipt to display the link is only for a valid link.
        if (link.startsWith("http://")) {
            myPubMedLink = "<a href=\"" + "javascript:showXrefPId('"
                    + link + "')\"" + ">" + xref.getPrimaryId() + "</a>";
        }
    }

    /**
     * Returns the pubmed id as a link.
     * @return the pubmed id as a browsable link.
     */
    public String getPubMedLink() {
        return myPubMedLink;
    }

    // Helper methods.

    /**
     * Constructs the short label link.
     */
    private void setShortLabelLink() {
        myShortLabelLink = AbstractEditBean.getLink(EditorService.getTopic(
                Experiment.class), getShortLabel());
    }
}