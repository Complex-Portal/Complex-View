/*
 * Copyright (c) 2002 The European Bioinformatics Institute, and others.
 * All rights reserved. Please see the file LICENSE
 * in the root directory of this distribution.
 */
package uk.ac.ebi.intact.imex.idassigner.id;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Utility class taking care of all IMEx ID handling such as formatting, parsing, etc.
 *
 * @author Samuel Kerrien (skerrien@ebi.ac.uk)
 * @version $Id$
 * @since <pre>15-May-2006</pre>
 */
public abstract class IMExIdTransformer {

    /**
     * Prefix of an IMEx ID.
     */
    private static final String IMEX_ID_PREFIX = "IM-";

    /**
     * Pattern of an IMEx ID.
     */
    private static final Pattern IMEX_ID_PATTERN = Pattern.compile( "^" + IMEX_ID_PREFIX + "(\\d+)" );

    /**
     * Delimiter for a simple range.
     */
    private static final String SIMPLE_RANGE_DELIMITER = "..";

    /**
     * Pattern of a delimiter for a simple range.
     */
    private static final Pattern IMEX_SIMPLE_RANGE_PATTERN = Pattern.compile( "^(\\d+)" + SIMPLE_RANGE_DELIMITER + "(\\d+)" );

    ////////////////////////////////////
    // IMEx ID conversion

    public static String formatIMExId( long id ) {

        if ( id <= 0 ) {
            throw new IllegalArgumentException( "IMEx IDs must be greater than zero." );
        }

        return IMEX_ID_PREFIX + Long.toString( id );
    }

    /**
     * Convert a Collection of unformatted ids into formatted ids.
     *
     * @param ids collection of unformatted ids.
     *
     * @return a non null collection of formatted ids.
     */
    public static Collection<String> formatIMExIds( Collection<Long> ids ) {
        Collection<String> formattedIds = new ArrayList<String>( ids.size() );

        for ( Long id : ids ) {
            formattedIds.add( formatIMExId( id ) );
        }

        return formattedIds;
    }

    public static long parseIMExId( String formattedId ) {

        long id;

        Matcher matcher = IMEX_ID_PATTERN.matcher( formattedId );
        if ( ! matcher.matches() ) {
            throw new IllegalArgumentException( "You must give a well formatted IMEx ID (example: IM-123): " + formattedId );
        } else {
            String strId = matcher.group( 1 );
            id = Long.parseLong( strId );
        }

        return id;
    }

    /**
     * Converts a Collection of formatted IMEx IDs into a Collection of unformatted IDs.
     *
     * @param formattedIds collection to convert.
     *
     * @return a non null collection of unfirmatted IDs.
     */
    public static Collection<Long> parseIMExIds( Collection<String> formattedIds ) {
        Collection<Long> ids = new ArrayList<Long>();

        for ( String formattedId : formattedIds ) {
            ids.add( parseIMExId( formattedId ) );
        }

        return ids;
    }

    ////////////////////////////////////
    // Simple range conversion

    /**
     * Converts an IMExRange into a simple range.
     * <p/>
     * Note: repeated ids will be used only once, that is 1, 2, 2, 3, 4 will give 1..4. 
     *
     * @param ids the range to convert in a simple representation.
     *
     * @return a well formatted simple range.
     */
    public static List<IMExRange> buildRanges( Collection<Long> ids ) {

        if ( ids == null ) {
            throw new IllegalArgumentException( "You must give a non null IMEx range." );
        }

        List<IMExRange> ranges = new ArrayList<IMExRange>();

        // order the list of IDs
        List<Long> sortedIds = new ArrayList<Long>( ids );
        Collections.sort( sortedIds );

        // first id of a range
        long first = 0;

        // last id of a range
        long last = -1;

        // current id
        long previous = 0;

        Iterator<Long> iterator = sortedIds.iterator();
        first = iterator.next();
        previous = first;

        while ( iterator.hasNext() ) {
            long id = iterator.next();

            if ( id == ( previous + 1 ) || id == previous ) {
                // the range is continuous ... keep going ...
            } else {
                // the range is discontinuous, warning !
                last = previous;

                ranges.add( new IMExRange( first, last ) );

                first = id;
                last = -1;
            }

            previous = id;
        }

        last = previous;
        ranges.add( new IMExRange( first, last ) );

        // return list of ranges
        return ranges;
    }

    /**
     * Converts an IMExRange into a simple range.
     *
     * @param range the range to convert in a simple representation.
     *
     * @return a well formatted simple range.
     */
    public static String formatSimpleRange( IMExRange range ) {

        if ( range == null ) {
            throw new IllegalArgumentException( "You must give a non null IMEx range." );
        }

        return range.getFrom() + SIMPLE_RANGE_DELIMITER + range.getTo();
    }

    /**
     * Convert a simple range into an IMExRange.
     *
     * @param simpleRange the simple range to convert.
     *
     * @return an IMExRange or an Exception will be thrown.
     */
    public static IMExRange parseSimpleRange( String simpleRange ) {

        Matcher matcher = IMEX_SIMPLE_RANGE_PATTERN.matcher( simpleRange );
        if ( ! matcher.matches() ) {
            throw new IllegalArgumentException( "You must give a well formatted simple IMEx Range (example: 12..15): " + simpleRange );
        } else {
            String strFrom = matcher.group( 1 );
            int from = Integer.parseInt( strFrom );

            String strTo = matcher.group( 2 );
            int to = Integer.parseInt( strTo );

            IMExRange range = new IMExRange( from, to );

            return range;
        }
    }

    ////////////////////////////////////
    // Range to Collection

    /**
     * Build a list of IDs from an IMExRange.
     *
     * @param range the range.
     *
     * @return
     */
    public static Collection<String> getFormattedIMExIds( IMExRange range ) {

        if ( range == null ) {
            throw new IllegalArgumentException( "IMEx range must not be null." );
        }

        // build collection
        // Example: 10..15 = 6 => 15 - 10 + 1
        Collection<String> ids = new ArrayList<String>( (int) ( range.getTo() - range.getFrom() + 1 ) );

        Iterator<Long> iterator = range.iterator();

        while ( iterator.hasNext() ) {
            long id = iterator.next().longValue();
            ids.add( formatIMExId( id ) );
        }

        // return iterator
        return ids;
    }

    /**
     * Build a list of IDs from an IMExRange.
     *
     * @param range the range.
     *
     * @return
     */
    public static Collection<Long> getUnformattedIMExIds( IMExRange range ) {

        if ( range == null ) {
            throw new IllegalArgumentException( "IMEx range must not be null." );
        }

        // build collection
        // Example: 10..15 = 6 => 15 - 10 + 1
        Collection<Long> ids = new ArrayList<Long>( (int) ( range.getTo() - range.getFrom() + 1 ) );

        Iterator<Long> iterator = range.iterator();

        while ( iterator.hasNext() ) {
            Long id = iterator.next();
            ids.add( id );
        }

        // return iterator
        return ids;
    }
}