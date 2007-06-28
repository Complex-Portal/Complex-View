package uk.ac.ebi.intact.searchengine.lucene.model;

import java.util.Map;

/**
 * Provides a possibility to store the attributes of a Protein to create an index.
 *
 * @author Anja Friedrichsen
 * @version $Id$
 */
public class ProteinSearchObject extends SearchObject {

    /**
     * Constructs an ProteinSearchObject object.
     *
     * @param ac          accession number
     * @param shortLabel  short label of the protein
     * @param fullName    fullname/description of the protein
     * @param objClass    name of the IntAct class
     * @param xrefs       Map with xrefs that a specific protein has. The key is the name of the database and the value
     *                    is the primaryId of the reference
     * @param annotations Map with annotations for a specific protein, the key is the name of the cvTopic and the value
     *                    is the annotation
     * @param alias       Map with alias, again the key is the description of the alias and the value the name of the
     *                    alias
     */
    public ProteinSearchObject( final String ac, final String shortLabel,
                                final String fullName, final String objClass,
                                final Map xrefs, final Map annotations,
                                final Map alias
    ) {

        super( ac, shortLabel, fullName, objClass, xrefs, annotations, alias );
    }
}