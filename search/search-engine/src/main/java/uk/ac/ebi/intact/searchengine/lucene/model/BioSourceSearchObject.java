/*
* Copyright (c) 2002 The European Bioinformatics Institute, and others.
* All rights reserved. Please see the file LICENSE
* in the root directory of this distribution.
*/
package uk.ac.ebi.intact.searchengine.lucene.model;

import java.util.Map;

/**
 * This class provides a possibility to store the attributes of a biosource to create an index.
 *
 * @author Anja Friedrichsen
 * @version $Id$
 */
public class BioSourceSearchObject extends SearchObject {

    /**
     * constructor that holds all information for a biosource.
     *
     * @param ac          accession number
     * @param shortLabel  short label of the biosource
     * @param fullName    fullname/description of the biosource
     * @param objClass    name of the IntAct class Biosource
     * @param xrefs       Map with xrefs that a specific biosource has. The key is the name of the database and the
     *                    value is the primaryId of the reference
     * @param annotations Map with annotations for a specific biosource, the key is the name of the cvTopic and the
     *                    value is the annotation
     * @param alias       Map with alias, again the key is the description of the alias and the value the name of the
     *                    alias
     */
    public BioSourceSearchObject( final String ac, final String shortLabel,
                                  final String fullName, final String objClass,
                                  final Map xrefs, final Map annotations, final Map alias
    ) {
        super( ac, shortLabel, fullName, objClass, xrefs, annotations, alias );
    }
}