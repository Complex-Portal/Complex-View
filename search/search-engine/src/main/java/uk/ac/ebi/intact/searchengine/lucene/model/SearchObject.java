package uk.ac.ebi.intact.searchengine.lucene.model;

import java.util.Map;

/**
 * Provides method to get the information of a search object.
 *
 * @author Anja Friedrichsen
 * @version $Id$
 */
public class SearchObject {

    /**
     * the accession number of one search object
     */
    private String ac;

    /**
     * Short label of one search object
     */
    private String shortLabel;

    /**
     * Fullname/description of one search object
     */
    private String fullName;

    /**
     * Class name of that specific search object
     */
    private String objClass;

    /**
     * Map holding the cross references for one search object
     */
    private Map xrefs;

    /**
     * Map holding the annotation for one search object
     */
    private Map annotations;

    /**
     * Map holding the alias for one search object
     */
    private Map alias;

    /**
     * This constructor is forbidden.
     */
    private SearchObject() {
    }

    /**
     * Constructs a SearchObject object.
     *
     * @param ac          accession number of one search object
     * @param shortLabel  short label of one search object
     * @param fullName    fullname/description of one search object
     * @param objClass    IntAct class name of that specific search object
     * @param xrefs       Map holding the cross references for one search object, the key is the database name and the
     *                    value is a collection of the primaryIds
     * @param annotations Map holding the annotation for one search object, the key is the name of the annotation topic
     *                    and the value is the annotation
     * @param alias       Map holding the alias for one search object, the key is the name of the alias type and the
     *                    value is a collection of the alias names
     */
    protected SearchObject( final String ac, final String shortLabel, final String fullName,
                            final String objClass, final Map xrefs, final Map annotations, final Map alias
    ) {

        this.ac = ac;
        this.shortLabel = shortLabel;
        this.fullName = fullName;
        this.objClass = objClass;
        this.xrefs = xrefs;
        this.annotations = annotations;
        this.alias = alias;
    }

    /**
     * Getter for the accession number of one search object.
     *
     * @return accession number of the search object
     */
    public String getAc() {
        return ac;
    }

    /**
     * Getter for the short label of one search object.
     *
     * @return short label of the search object
     */
    public String getShortLabel() {
        return shortLabel;
    }

    /**
     * Getter for the fullname/description of one search object.
     *
     * @return fullname of the search object
     */
    public String getFullName() {
        return fullName;
    }

    /**
     * Getter for the IntAct class name of that search object.
     *
     * @return IntAct class name of the search object
     */
    public String getObjClass() {
        return objClass;
    }

    /**
     * Getter for the cross references of one search object.
     *
     * @return Map with cross references
     */
    public Map getXRefs() {
        return xrefs;
    }

    /**
     * Getter for the annotations of one search object.
     *
     * @return Map with annotations of the search object
     */
    public Map getAnnotations() {
        return annotations;
    }

    /**
     * Getter for the alias of one search object.
     *
     * @return Map containing the alias' of the search object
     */
    public Map getAlias() {
        return alias;
    }
}