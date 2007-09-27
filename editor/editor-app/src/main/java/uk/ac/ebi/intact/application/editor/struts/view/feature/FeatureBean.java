/*
Copyright (c) 2002-2004 The European Bioinformatics Institute, and others.
All rights reserved. Please see the file LICENSE
in the root directory of this distribution.
*/

package uk.ac.ebi.intact.application.editor.struts.view.feature;

import uk.ac.ebi.intact.application.editor.struts.view.AbstractEditKeyBean;
import uk.ac.ebi.intact.application.editor.util.DaoProvider;
import uk.ac.ebi.intact.business.IntactException;
import uk.ac.ebi.intact.model.Feature;
import uk.ac.ebi.intact.model.Range;
import uk.ac.ebi.intact.persistence.dao.FeatureDao;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Feature bean.
 *
 * @author Sugath Mudali (smudali@ebi.ac.uk)
 * @version $Id$
 */
public class FeatureBean extends AbstractEditKeyBean {
    protected static Log log = LogFactory.getLog(FeatureViewBean.class);


    // Instance Data

    /**
     * Reference to the range this instance is created with.
     */
    private Feature myFeature;

    /**
     * The short label
     */
    private String myShortLabel;

    /**
     * The full name of this feature.
     */
    private String myFullName;

    /**
     * The feature type.
     */
    private String myType;

    /**
     * The feature detection.
     */
    private String myDetection;

    /**
     * A list of range beans.
     */
    private List myRanges = new ArrayList();

    /**
     * The AC of the bound domain, we need this as Feature shortlabels are not
     * unique.
     */
    private String myBoundDomainAc;

    /**
     * The short label of feature this feature interacts with.
     */
    private String myBoundDomain = "";

    /**
     * True if this feature is checked. Default is not.
     */
    private boolean myChecked;

    /**
     * True if this bein selected. Default is not selected.
     */
    private boolean mySelected;
    
    /**
     * Default constructor. Only visible to classes in the same package.
     */
    FeatureBean() {}

    /**
     * Instantiate an object of this class from a Feature instance.
     *
     * @param feature the <code>Feature</code> object.
     */
    public FeatureBean(Feature feature) {
        initialize(feature, true);
    }

    /**
     * Instantiate an object of this class from a Feature instance
     * and a key
     * @param feature the <code>Feature</code> object.
     * @param key the key to assigned to this bean.
     */
    public FeatureBean(Feature feature, long key) {
        super(key);
        initialize(feature, true);
    }

    /**
     * Instantiate an object of this class from a Feature instance
     * and a short label
     * @param feature the <code>Feature</code> object.
     * @param label the short label to assign to this bean.
     */
    public FeatureBean(Feature feature, String label) {
        initialize(feature, false);
        setShortLabel(label);
    }

    // Read only properties.

    public String getAc() {
        return myFeature.getAc();
    }

    public String getShortLabel() {
        return myShortLabel;
    }

    public String getFullName() {
        return myFullName;
    }

    public String getRanges() {
        // The range to return.
        String range = "";
        boolean first = true;
        for (Iterator iterator = myRanges.iterator(); iterator.hasNext();) {
            RangeBean rangeBean = (RangeBean) iterator.next();
            if (first) {
                first = false;
            }
            else {
                range += ", ";
            }
            range += rangeBean.toString();

        }
        return range;
    }

    public String getType() {
        return myType;
    }

    public String getDetection() {
        return myDetection;
    }

    public String getComponentAc() {
        return myFeature.getComponent().getAc();
    }

    public boolean hasBoundDomain() {
        return myBoundDomain.length() != 0;
    }

    // Read/Write methods for JSPs

    public void setShortLabel(String label) {
        myShortLabel = label;
    }

    public String getBoundDomainAc() {
        return myBoundDomainAc;
    }

    public void setBoundDomainAc(String ac) {
        myBoundDomainAc = ac;
    }

    public String getBoundDomain() {
        return myBoundDomain;
    }

    public void setBoundDomain(String label) {
        myBoundDomain = label;
    }

    public boolean isChecked() {
        return myChecked;
    }

    public void setChecked(boolean linked) {
        myChecked = linked;
    }

    public void setFeatureCmd(String value) {
        mySelected = true;
    }

    // Other methods

    public void unselect() {
        mySelected = false;
    }
    
    public boolean isSelected() {
        return mySelected;
    }

    public Feature getFeature() {
         if(myFeature != null && myFeature.getAc()!= null){
            FeatureDao featureDao = DaoProvider.getDaoFactory().getFeatureDao();
            myFeature = featureDao.getByAc(myFeature.getAc());
        }
        return myFeature;
    }

    /**
     * Updates the internal Feature with the new values from the form.
     * @throws IntactException for errors in searching the database.
     */
    public Feature getUpdatedFeature() throws IntactException {
        if(myFeature != null && myFeature.getAc()!= null && (!"".equals(myFeature.getAc()))){
            FeatureDao featureDao = DaoProvider.getDaoFactory().getFeatureDao();
            myFeature = featureDao.getByAc(myFeature.getAc());
            log.debug("myFeature was null, we reloaded it");
        }
        // Need to update the short label because cloning an interaction also
        // clones a Feature (changes it shortlabel).
        myFeature.setShortLabel(getShortLabel());
        // Set the bound domain if it isn't empty.
        if (hasBoundDomain()) {
            FeatureDao featureDao = DaoProvider.getDaoFactory().getFeatureDao();
            Feature boumdDomain = featureDao.getByAc(myBoundDomainAc);
            myFeature.setBoundDomain(boumdDomain);
        }
        return myFeature;
    }

    // For debugging.

    public String toString() {
        return "AC: " + getAc() + " Short Label: " + getShortLabel()
                + " Range: " + getRanges();
    }

    // Write methods. Only visible within this package.

    void setFullName(String fullname) {
        myFullName = fullname;
    }

    void addRange(RangeBean rb) {
        myRanges.add(rb);
    }

    Iterator getRangeList() {
        return myRanges.iterator();
    }

    private void initialize(Feature feature, boolean assignSL) {
        myFeature = feature;
        if (assignSL) {
            myShortLabel = feature.getShortLabel();
        }
        myFullName = feature.getFullName();
        myType = feature.getCvFeatureType().getShortLabel();
        myDetection = feature.getCvFeatureIdentification() == null ? ""
                : feature.getCvFeatureIdentification().getShortLabel();

        // Loop through the ranges.
        for (Iterator iter = feature.getRanges().iterator(); iter.hasNext();) {
            Range range = (Range) iter.next();
            myRanges.add(new RangeBean(range));
        }

        Feature boundFeature = feature.getBoundDomain();
        if (boundFeature != null) {
            myBoundDomainAc = boundFeature.getAc();
            myBoundDomain = boundFeature.getShortLabel();
        }
    }
}
