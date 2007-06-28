/*
* Copyright (c) 2002 The European Bioinformatics Institute, and others.
* All rights reserved. Please see the file LICENSE
* in the root directory of this distribution.
*/
package uk.ac.ebi.intact.searchengine;

import uk.ac.ebi.intact.model.*;

/**
 * Contains several constants, that aer used in the SearchEngine package.
 *
 * @author Anja Friedrichsen
 * @version $Id$
 */
public interface SearchEngineConstants {

    // names of the search objects
    public static final String INTERACTION = "interaction";
    public static final String EXPERIMENT = "experiment";
    public static final String PROTEIN = "protein";
    public static final String CVOBJECT = "cvobject";
    public static final String BIOSOURCE = "biosource";


    // basic field for all search objects
    public static final String AC = "ac";
    public static final String SHORTLABEL = "shortlabel";
    public static final String FULLNAME = "fullname";
    public static final String XREF = "xref";
    public static final String ANNOTATION = "annotation";
    public static final String OBJCLASS = "objclass";
    public static final String ALIAS = "alias";

    // fields for the CvIdentifikation
    public static final String IDENT_AC = "identification_ac";
    public static final String IDENT_SHORTLABEL = "identification_shortlabel";
    public static final String IDENT_FULLNAME = "identification_fullname";

    // fields for the CvInteraction
    public static final String INTERACTION_AC = "interaction_ac";
    public static final String INTERACTION_SHORTLABEL = "interaction_shortlabel";
    public static final String INTERACTION_FULLNAME = "interaction_fullname";

    // fields for the CvInteractionType
    public static final String INTERACTION_TYPE_AC = "interactiontype_ac";
    public static final String INTERACTION_TYPE_SHORTLABEL = "interactiontype_shortlabel";
    public static final String INTERACTION_TYPE_FULLNAME = "interactiontype_fullname";

    /**
     * The name of the hierarchView properties file.
     */
    public static final String HV_PROPS = "HierarchView";


    // SQL query to get all experiment out of the database
    public static final String EXPERIMENT_QUERY =
            " SELECT E.ac as ac, " +
            "E.shortlabel as shortlabel, " +
            "E.fullname as fullname, " +
            "'" + Experiment.class.getName() + "' as objclass " +
            "FROM ia_experiment E ";

    // SQL query to get all interactions out of the database
    public static final String INTERACTION_QUERY =
            " SELECT I.ac as ac, " +
            "I.shortlabel as shortlabel, " +
            "I.fullname as fullname, " +
            "I.objclass as objclass " +
            "FROM ia_interactor I " +
            "WHERE I.objclass = '" + InteractionImpl.class.getName() + "'";

    // SQL query to get all proteins out of the database
    public static final String PROTEIN_QUERY =
            " SELECT I.ac as ac, " +
            "I.shortlabel as shortlabel, " +
            "I.fullname as fullname, " +
            "I.objclass as objclass " +
            "FROM ia_interactor I " +
            "WHERE I.objclass = '" + ProteinImpl.class.getName() + "'";

    // SQL query to get all cvObjects out of the database
    public static final String CV_OBJECT_QUERY =
            " SELECT CV.ac as ac, " +
            "CV.shortlabel as shortlabel, " +
            "CV.fullname as fullname, " +
            "'" + CvObject.class.getName() + "' as objclass " +
            "FROM ia_controlledvocab CV ";

    // SQL query to get all cvObjects out of the database
    public static final String BIOSOURCE_QUERY =
            "SELECT B.ac as ac, " +
            "B.shortlabel as shortlabel, " +
            "B.fullname as fullname, " +
            "'" + BioSource.class.getName() + "' as objclass " +
            "FROM ia_biosource B";
}
