/*
 * Copyright (c) 2002 The European Bioinformatics Institute, and others. All
 * rights reserved. Please see the file LICENSE in the root directory of this
 * distribution.
 */

package uk.ac.ebi.intact.application.mine.business;


/**
 * The class <tt>Constants</tt> stores several constants which are used in the
 * MiNe application.
 * 
 * @author Andreas Groscurth
 */
public class Constants {

    // the name of the logger
    public static final String LOGGER_NAME = "mine";

    public static final String USER = "user";
    public static final String PARAMETER = "AC";
    public static final String ERROR = "error";
    public static final String SUCCESS = "success";
    public static final String AMBIGOUS = "ambiguous";

    // Mine parameters
    // Maximum number 
    public static final int MAX_NUMBER_RESULTS = 30;

    public static final int MAX_INTERACTION_SIZE = 5;

    // number of proteins you can search for in a query
    public static final int MAX_SEARCH_NUMBER = 7;

    public static final String SEARCH = "search";
    public static final Integer SINGLETON_GRAPHID = new Integer( Integer.MIN_VALUE );
    public static final String GRAPH_HELPER = "graphHelper";
    public static final String COMMA = ",";

    // MiNe relevant database table
    public static final String INTERACTION_TABLE = "ia_interactions";

    /**
     * Column name for the IA_Interactions table
     */
    public static final String COLUMN_protein1_ac = "protein1_ac";
    public static final String COLUMN_shortlabel1 = "shortlabel1";
    public static final String COLUMN_protein2_ac = "protein2_ac";
    public static final String COLUMN_shortlabel2 = "shortlabel2";
    public static final String COLUMN_taxid = "taxid";
    public static final String COLUMN_interaction_ac = "interaction_ac";
    public static final String COLUMN_weight = "weight";
    public static final String COLUMN_graphid = "graphid";
}