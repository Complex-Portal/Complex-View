/*
 Copyright (c) 2002-2004 The European Bioinformatics Institute, and others.
 All rights reserved. Please see the file LICENSE
 in the root directory of this distribution.
 */

package uk.ac.ebi.intact.application.editor.struts.view.feature;

import org.apache.log4j.Logger;
import org.apache.struts.action.ActionMessage;
import org.apache.struts.action.ActionErrors;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import uk.ac.ebi.intact.application.editor.struts.framework.util.EditorConstants;
import uk.ac.ebi.intact.application.editor.struts.view.AbstractEditKeyBean;
import uk.ac.ebi.intact.application.editor.util.DaoProvider;
import uk.ac.ebi.intact.business.IntactException;
import uk.ac.ebi.intact.context.IntactContext;
import uk.ac.ebi.intact.model.CvFuzzyType;
import uk.ac.ebi.intact.model.Range;
import uk.ac.ebi.intact.persistence.dao.CvObjectDao;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Bean to store a range for a feature.
 *
 * @author Sugath Mudali (smudali@ebi.ac.uk)
 * @version $Id$
 */
public class RangeBean extends AbstractEditKeyBean {

    // Class Data
    protected static final Log LOGGER = LogFactory.getLog(RangeBean.class);

    /**
     * pattern 1: starting with ? or c or n
     * pattern 2: starting with < or > and optional - followed by digits
     * pattern 3: starting with optional -, .. followed by an optional - and digits
     */
    private static final Pattern ourRangePattern =
            Pattern.compile("^(\\?|c|n)$|^(<|>)?+(-)*?(\\d+)$|^(-)*?(\\d+)\\.\\.(-)*?(\\d+)$");

    // Instance Data

    /**
     * Reference to the range this instance is created with.
     */
    private Range myRange;

    /**
     * The from range as a string.
     */
    private String myFromRange;

    /**
     * The to range as a string.
     */
    private String myToRange;

    /**
     * The link.
     */
    private boolean myLink;

    /**
     * Handler to the fuzzy type converter.
     */
    private CvFuzzyType.Converter myFTConverter = CvFuzzyType.Converter.getInstance();

    // Class Methods

    /**
     * For test purposes only
     *
     * @return return the pattern to parse a range.
     */
    public static Pattern testGetRangePattern() {
        return ourRangePattern;
    }

    /**
     * Default constructor. Needs for adding new ranges to a feature.
     */
    public RangeBean() {
    }

    /**
     * Instantiate an object of this class from a Range instance. The key is set
     * to a default value (unique).
     *
     * @param range the <code>Range</code> object.
     */
    public RangeBean(Range range) {
        initialize(range);
    }

    /**
     * Instantiate an object of this class from a Range instance and a key.
     *
     * @param range the <code>Range</code> object.
     * @param key the key to assigned to this bean.
     */
    public RangeBean(Range range, long key) {
        super(key);
        initialize(range);
    }

    /**
     * Instantiate an object of this class using ranges.
     *
     * @param fromRange the from range as a string
     * @param toRange the to range as a string
     * @param linked true if the bean is linked.
     * <code>toRange</code> are ignored.
     * @throws IntactException for errors in retrieving CvFuzzyTypes
     */
    public RangeBean(String fromRange, String toRange, boolean linked)
            throws IntactException {
        // The match result for from range
        Matcher fromMatcher = ourRangePattern.matcher(fromRange);
        if (!fromMatcher.matches()) {
            throw new IllegalArgumentException("Unable to parse the from range");
        }
        String fromFuzzyType = myFTConverter.getFuzzyShortLabel(fromMatcher);
        int[] fromRanges = getRangeValues(fromFuzzyType, fromMatcher);

        // The match result for to range.
        Matcher toMatcher = ourRangePattern.matcher(toRange);
        if (!toMatcher.matches()) {
            throw new IllegalArgumentException("Unable to parse the to range");
        }
        String toFuzzyType = myFTConverter.getFuzzyShortLabel(toMatcher);
        int[] toRanges = getRangeValues(toFuzzyType, toMatcher);

        // Construct a range and set fuzzy types.
        Range range = new Range(IntactContext.getCurrentInstance().getConfig().getInstitution(),fromRanges[0],
                fromRanges[1], toRanges[0], toRanges[1], null);
        range.setLinked(linked);

        // Set the from and to fuzzy types.
        range.setFromCvFuzzyType(getFuzzyType(fromFuzzyType));
        range.setToCvFuzzyType(getFuzzyType(toFuzzyType));
        // This needs to be done after setting the fuzzy types.
        range.setUndetermined();
        // Initialize the bean with the new range.
        initialize(range);
    }

    // Read/Write only properties.

    /**
     * Returns the from range.
     *
     * @return the from range as a string for display purposes.
     */
    public String getFromRange() {
        return myFromRange;
    }

    /**
     * Sets the from range only if the undetermined is false.
     * @param fromRange the from range as a string
     */
    public void setFromRange(String fromRange) {
        myFromRange = fromRange.trim();
    }

    /**
     * Returns the to range.
     *
     * @return the to range as a string for display purposes.
     */
    public String getToRange() {
        return myToRange;
    }

    /**
     * Sets the to range only if the undetermined is false.
     * @param toRange the from range as a string
     */
    public void setToRange(String toRange) {
        myToRange = toRange.trim();
    }

    /**
     * Returns the link as a string.
     *
     * @return the link as a string (true or false).
     */
    public boolean getLink() {
        return myLink;
    }

    /**
     * Sets the link.
     *
     * @param link the link to set as a string object.
     */
    public void setLink(boolean link) {
        myLink = link;
    }

    // Override Object's toString() method to display the range.

    public String toString() {
        return myFromRange + "-" + myToRange;
    }

    /**
     * Returns true if given bean is equivalent to the current bean.
     *
     * @param rb the bean to compare.
     * @return true if ranges and link are equivalent to corresponding value in
     *  <code>rb</code>; false is returned for all other instances.
     */
    public boolean isEquivalent(RangeBean rb) {
        return  rb.getFromRange().equals(getFromRange()) && rb.getToRange().equals(
                getToRange()) && myLink == rb.myLink;
    }

    /**
     * Validates the vlaues for from and to ranges.
     *
     * @return null if no errors found in validating from/to ranges.
     */
    public ActionErrors validate(String prefix) {
        // The errors to return.
        ActionErrors errors = null;

        // Stores the range values.
        int[] ranges;

        // Holds values for from range.
        int fromStart = 0;
        int fromEnd = 0;

        try {
            // Parse ranges.
            ranges = getFromRangeValues();

            // Split ranges to start and end.
            fromStart = ranges[0];
            fromEnd = ranges[1];

            // Check the validity of ranges.
            if (fromStart > fromEnd) {
                errors = new ActionErrors();
                errors.add(prefix + ".fromRange",
                        new ActionMessage("error.feature.range.interval.invalid"));
            }
        }
        catch (IllegalArgumentException iae) {
            LOGGER.error("", iae);
            errors = new ActionErrors();
            errors.add(prefix + ".fromRange",
                    new ActionMessage("error.feature.range.invalid"));
        }
        // Don't check any further if we have errors.
        if (errors != null) {
            return errors;
        }

        // Holds values for to range.
        int toStart = 0;
        int toEnd = 0;

        try {
            // Parse ranges.
            ranges = getToRangeValues();

            // Split ranges to start and end.
            toStart = ranges[0];
            toEnd = ranges[1];

            // Check the validity of ranges.
            if (toStart > toEnd) {
                errors = new ActionErrors();
                errors.add(prefix + ".toRange",
                        new ActionMessage("error.feature.range.interval.invalid"));
            }
        }
        catch (IllegalArgumentException iae) {
            LOGGER.error("", iae);
            errors = new ActionErrors();
            errors.add(prefix + ".toRange", new ActionMessage("error.feature.range.invalid"));
        }
        // Don't check any further if we have errors.
        if (errors != null) {
            return errors;
        }
        // Need to validate the from and start ranges. These validations are
        // copied from Range constructor.
        if (fromEnd < fromStart) {
            errors = new ActionErrors();
            errors.add(prefix + ".range",
                    new ActionMessage("error.feature.range.fromEnd.less.fromStart"));
        }
        else if (toEnd < toStart) {
            errors = new ActionErrors();
            errors.add(prefix + ".range",
                    new ActionMessage("error.feature.range.toEnd.less.toStart"));
        }
        else if (fromEnd > toStart) {
            errors = new ActionErrors();
            errors.add(prefix + ".range",
                    new ActionMessage("error.feature.range.fromEnd.more.toStart"));
        }
        else if (fromStart > toEnd) {
            errors = new ActionErrors();
            errors.add(prefix + ".range",
                    new ActionMessage("error.feature.range.fromStart.more.toEnd"));
        }
        else if (fromStart > toStart) {
            errors = new ActionErrors();
            errors.add(prefix + ".range",
                    new ActionMessage("error.feature.range.fromStart.more.toStart"));
        }
        return errors;
    }

    /**
     * Allows access to the range object this bean is created with.
     *
     * @return the Range instance this bean is wrapped.
     *         <p/>
     *         <b>Notde:</b> The range is not updated. This method is used to
     *         delete existing ranges.
     */
    public Range getRange() {
        return myRange;
    }

    /**
     * Construts a new Range or updates the existing range using the current
     * values in the bean. <b>Must </b> call {@link #validate(String)} method
     * prior to calling this method.
     *
     * @return a new Range using values from the bean if this is a new range or
     *         else the existing range is updated.
     * @throws IntactException for errors in searching <code>CvFuzzyType</code>s.
     * @throws IllegalArgumentException for errors in constructing a Range
     * object (ie., thrown from the constructor of the Range class).
     * <p/>
     * <pre>
     *  pre: validate(String)
     * </pre>
     */
    public Range getUpdatedRange() throws IntactException, IllegalArgumentException {
        // The range as an int array.
        int[] ranges = getRangeValues();

        // Split the ranges to parts.
        int fromStart = ranges[0];
        int fromEnd = ranges[1];
        int toStart = ranges[2];
        int toEnd = ranges[3];

        // Need to validate the from and start ranges. These validations are
        // copied from Range constructor. Alternative is to create a dummy
        // range object to validate inputs.
        if (fromEnd < fromStart) {
            throw new IllegalArgumentException(
                    "End of 'from' interval must be bigger than the start!");
        }
        if (toEnd < toStart) {
            throw new IllegalArgumentException(
                    "End of 'to' interval must be bigger than the start!");
        }
        if (fromEnd > toStart) {
            throw new IllegalArgumentException(
                    "The 'from' and 'to' intervals cannot overlap!");
        }
        if (fromStart > toEnd) {
            throw new IllegalArgumentException(
                    "The 'from' interval starts beyond the 'to' interval!");
        }
        if (fromStart > toStart) {
            throw new IllegalArgumentException(
                    "The 'from' interval cannot begin during the 'to' interval!");
        }
        // Update the ranges.
        myRange.setFromIntervalStart(fromStart);
        myRange.setFromIntervalEnd(fromEnd);
        myRange.setToIntervalStart(toStart);
        myRange.setToIntervalEnd(toEnd);

        // The from/to fuzzy types.
        Matcher fromMatcher = ourRangePattern.matcher(myFromRange);
        String fromType = myFTConverter.getFuzzyShortLabel(fromMatcher);

        Matcher toMatcher = ourRangePattern.matcher(myToRange);
        String toType = myFTConverter.getFuzzyShortLabel(toMatcher);

        myRange.setFromCvFuzzyType(getFuzzyType(fromType));
        myRange.setToCvFuzzyType(getFuzzyType(toType));
        myRange.setLinked(myLink);
        myRange.setUndetermined();

        return myRange;
    }

    // Helper methods

    /**
     * Intialize the member variables using the given Range object.
     *
     * @param range <code>Range</code> object to populate this bean.
     */
    private void initialize(Range range) {
        myRange = range;

        // Saves the from type as a short label
        String fromType = "";

        // Set the fuzzy type first as they are used in set range methods.
        if (range.getFromCvFuzzyType() != null) {
            fromType = range.getFromCvFuzzyType().getShortLabel();
        }
        myFromRange = Range.getRange(fromType, range.getFromIntervalStart(),
                range.getFromIntervalEnd());

        // Saves the to type as a short label
        String toType = "";

        if (range.getToCvFuzzyType() != null) {
            toType = range.getToCvFuzzyType().getShortLabel();
        }
        myToRange = Range.getRange(toType, range.getToIntervalStart(),
                range.getToIntervalEnd());

        myLink = range.isLinked();
    }

    /**
     * Returns an array of ints.
     *
     * @return an array of ints. int[0] = fromStart, int[1] = fromEnd, int[2] =
     *         toStart and int[3] toEnd.
     */
    private int[] getRangeValues() {
        // The array to return.
        int[] ranges = new int[4];

        // The array to hold the result.
        int[] results;
        results = getFromRangeValues();

        // Copy the from range values.
        ranges[0] = results[0];
        ranges[1] = results[1];

        // The match result for to range.
        results = getToRangeValues();

        // Copy the to range values.
        ranges[2] = results[0];
        ranges[3] = results[1];
        return ranges;
    }

    /**
     * A convenient method to get from range values in an int array.
     *
     * @return an arry of ints. int[0] = fromStart and int[1] = fromEnd
     */
    private int[] getFromRangeValues() {
        Matcher matcher = ourRangePattern.matcher(myFromRange);
        String fuzzyType = myFTConverter.getFuzzyShortLabel(matcher);

        return getRangeValues(fuzzyType, matcher);
    }

    /**
     * A convenient method to get to range values in an int array.
     *
     * @return an arry of ints. int[0] = toStart and int[1] = toEnd
     */
    private int[] getToRangeValues() {
        Matcher matcher = ourRangePattern.matcher(myToRange);
        String fuzzyType = myFTConverter.getFuzzyShortLabel(matcher);
        return getRangeValues(fuzzyType, matcher);
    }

    /**
     * Returns an array of ints.
     *
     * @param fuzzyType the fuzzy type as a string
     * @param matcher the matcher object to extract int values to return.
     * @return an array of ints. int[0] = fromStart, int[1] = fromEnd, int[2] =
     *         toStart and int[3] toEnd.
     */
    private int[] getRangeValues(String fuzzyType, Matcher matcher) {
        int[] ranges = new int[2];

        // No further parsing for single character types.
        if (CvFuzzyType.isSingleType(fuzzyType)) {
            return ranges;
        }
        if (fuzzyType.equals(CvFuzzyType.RANGE)) {
            // Range, 1..2 type
            // From value.
            ranges[0] = Integer.parseInt(matcher.group(6));
            // Check for negative values.
            if (matcher.group(5) != null) {
                ranges[0] *= -1;
            }
            // End value
            ranges[1] = Integer.parseInt(matcher.group(8));
            // Check for negative values.
            if (matcher.group(7) != null) {
                ranges[1] *= -1;
            }
        }
        else {
            // Other type, 2, <2 or >2
            ranges[0] = Integer.parseInt(matcher.group(4));
            if (matcher.group(3) != null) {
                ranges[0] *= -1;
            }
            ranges[1] = ranges[0];
        }
        return ranges;
    }

    private CvFuzzyType getFuzzyType(String type) throws IntactException {
        // Set the from and to fuzzy types.
        CvFuzzyType fuzzyType = null;
        if (type.length() != 0) {
            CvObjectDao<CvFuzzyType> cvObjectDao = DaoProvider.getDaoFactory().getCvObjectDao(CvFuzzyType.class);
            fuzzyType = cvObjectDao.getByShortLabel(type);
        }
        return fuzzyType;
    }
}