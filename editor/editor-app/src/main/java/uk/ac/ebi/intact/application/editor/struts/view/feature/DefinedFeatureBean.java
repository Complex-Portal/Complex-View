/*
Copyright (c) 2002-2004 The European Bioinformatics Institute, and others.
All rights reserved. Please see the file LICENSE
in the root directory of this distribution.
*/

package uk.ac.ebi.intact.application.editor.struts.view.feature;

/**
 * Bean to store a defined feature.
 *
 * @author Sugath Mudali (smudali@ebi.ac.uk)
 * @version $Id$
 */
public class DefinedFeatureBean {
    // Class Data

    /**
     * The only instance of this class.
     */
    private static DefinedFeatureBean ourInstance = new DefinedFeatureBean();

    // Instance Data

    private FeatureBean myFeatureBean = new FeatureBean();

    /**
     * Returns the only instance of this class. Visibility is limited to this
     * package as it is accessible via {@link FeatureViewBean#getDefinedFeature()}.
     * @return the only instance of this class.
     */
    static DefinedFeatureBean getInstance() {
        return ourInstance;
    }

    /**
     * Creates an instance of undefined feature type.
     */
    private DefinedFeatureBean() {
        // Sets the range as undefined.
        RangeBean rb = new RangeBean();
        rb.setFromRange("?");
        rb.setToRange("?");

        myFeatureBean.addRange(rb);
        myFeatureBean.setShortLabel("undetermined");
        myFeatureBean.setFullName("Undetermined feature position");
    }

    // Read only methods

    public String getRanges() {
        return myFeatureBean.getRanges();
    }

    public String getShortLabel() {
        return myFeatureBean.getShortLabel();
    }

    public String getFullName() {
        return myFeatureBean.getFullName();
    }

    /**
     * Returns the first element (only) of the ranges.
     * @return the only range element as a bean.
     */
    public RangeBean getDefinedRange() {
        return (RangeBean) myFeatureBean.getRangeList().next();
    }
}
