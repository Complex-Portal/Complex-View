/*
 * Copyright (c) 2002 The European Bioinformatics Institute, and others.
 * All rights reserved. Please see the file LICENSE
 * in the root directory of this distribution.
 */
package uk.ac.ebi.intact.application.statisticView.business.publications;

import uk.ac.ebi.intact.model.CvDatabase;
import uk.ac.ebi.intact.model.CvXrefQualifier;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This class provides various utility methods used within the Statistics project.
 *
 * @author ckohler (ckohler@ebi.ac.uk)
 * @version $Id$
 * @since <pre>14-Feb-2006</pre>
 */

public class StatisticUtils {

    private final static String EXPERIMENT_SHORTLABEL_REGEXP = "[a-z]+-[0-9]{4}[a-z]*-[0-9]";
    private final static Pattern EXPERIMENT_SHORTLABEL_PATTERN = Pattern.compile( EXPERIMENT_SHORTLABEL_REGEXP );

    /**
     * Removes the detailed identification of an Experiment's shortlabel.<br> <i>Example</i>: gavin-2006-2 becomes
     * gavin-2006
     *
     * @param inputString the String to be clipped.
     *
     * @return The original inputString, if the shortlabel indicates that only one method has been used (ie.
     *         gavin2006-1). <br> Returns the clipped String ( ie. li-2004), if the shortlabel indicates more used
     *         methods. li-2004 has infact 2 <br> methods (=> li-2004-1 and li-2004-2).
     */
    public static String clipString( String inputString ) throws IllegalArgumentException {

        // Determine if EXPERIMENT_SHORTLABEL_PATTERN exists in input
        //CharSequence inputStr = inputString;
        Matcher matcher = EXPERIMENT_SHORTLABEL_PATTERN.matcher( inputString );
        boolean matchFound = matcher.find();
        if ( !matchFound ) {
            throw new IllegalArgumentException( "inputString not in valid format" );
        }

        // Get matching string
        String match = matcher.group();

        int lastHyphenIndex = match.lastIndexOf( "-" );
        int matchLength = match.length();
        String matchSubstring = match.substring( lastHyphenIndex, matchLength );
        if ( matchSubstring.equals( "-1" ) ) {
            return inputString;
        } else {
            return match.substring( 0, lastHyphenIndex );
        }
    }

    /**
     * SQL Statement for building up the precalculated table (pubmedID, total, known interactions, new interactions)
     *
     * @return the sql Statement to create the precalculated Table used for Statistic Chart
     */
    public static String getPrecalculatedTable() {

        // creates final table with columns: pubmed_id, count_total, known_interactions (= #bI that are in more
        // than one pubmed_id) and  new_interactions (#bI, that are inly in one pubmed_id)
        return "SELECT pubmed_id\n" +
               "     ,total_interactions\n" +
               "     ,NVL(known_interactions,0) known_interactions\n" +
               "     ,total_interactions - NVL(known_interactions,0)  new_interactions\n" +
               "FROM\n" +

               // get per pubmed_id total
               "  (SELECT pubmed_id pubmed_id, COUNT(*) total_interactions " +
               "  FROM IA_INTERACTIONS\n" +
               "  GROUP BY pubmed_id) T\n" +
               "  LEFT OUTER JOIN\n" +

               // join with information per pubmed_id known_interactions
               "  (SELECT p2.pubmed_id p2_pubmed_id " +
               "      , COUNT(*) known_interactions\n" +
               "      FROM\n" +
               "      (SELECT I.pubmed_id, I.protein1_ac,I.protein2_ac\n" +
               "      FROM IA_INTERACTIONS I\n" +

               // all the interactions that are in more than one pubmed id
               "          ,(SELECT protein1_ac " +
               "                 ,protein2_ac\n" +
               "          FROM                      " +

               // filter cases where interactions are more times in the same pubmed
               "(SELECT pubmed_id " +
               "                     ,protein1_ac\n" +
               "                     ,protein2_ac\n" +
               "              FROM IA_INTERACTIONS R\n" +
               "              GROUP BY pubmed_id\n" +
               "                         ,protein1_ac\n" +
               "                       ,protein2_ac)\n" +
               "          GROUP BY protein1_ac\n" +
               "                 ,protein2_ac\n" +
               "          HAVING COUNT (*) > 1) P\n" +
               "      WHERE I.protein1_ac = P.protein1_ac\n" +
               "      AND I.protein2_ac = P.protein2_ac)    p2\n" +
               "  GROUP BY pubmed_id    )\n" +
               "ON (T.pubmed_id = p2_pubmed_id)        " +

               // final rows in table are ordered descendently, starting with the largest number of binary Interactions
               "ORDER BY total_interactions DESC ";
    }

    /**
     * @param pubmedID used to search its related Experiment.
     *
     * @return a SQL statement which searches for the Experiment having the passes pubmedID.
     */
    public static String getExperimentShortlabelByPubmedID( String pubmedID ) {
        return "select e.ac, e.shortlabel " +
               "from ia_experiment e, ia_xref x, ia_controlledvocab cv1, ia_controlledvocab cv2 " +
               "where cv1.shortlabel = '" + CvDatabase.PUBMED + "' " +
               "and cv2.shortlabel =  '" + CvXrefQualifier.PRIMARY_REFERENCE + "' " +
               "and cv2.ac = x.qualifier_ac" +
               " and cv1.ac = x.database_ac" +
               " and e.ac = x.parent_ac" +
               " and x.primaryId = '" + pubmedID + "'";
    }

    /**
     * calculates the total runtime of an application.
     *
     * @param startTime the initial time.
     */
    public static void getTotalRunTime( long startTime ) {
        System.out.println( "total runtime: " + ( ( System.currentTimeMillis() - startTime ) / 1000 ) + " s" );
    }
}
