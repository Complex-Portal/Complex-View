/*
Copyright (c) 2002-2003 The European Bioinformatics Institute, and others.
All rights reserved. Please see the file LICENSE
in the root directory of this distribution.
*/

package uk.ac.ebi.intact.application.editor.struts.view;

import uk.ac.ebi.intact.application.editor.business.EditorService;

/**
 * Generic edit bean with keys. Acts as a super class for CommentBean and
 * XreferenceBean classes.
 *
 * @author Sugath Mudali (smudali@ebi.ac.uk)
 * @version $Id$
 */
public abstract class AbstractEditKeyBean extends AbstractEditBean implements Cloneable {
    // Beginning of Inner classes

    // ------------------------------------------------------------------------

    /**
     * Inner class to generate unique ids to use primary keys for CommentBean
     * class.
     */
    private static class UniqueID {

        /**
         * The initial value. All the unique ids are based on this value for any
         * (all) user(s).
         */
        private static long theirCurrentTime = System.currentTimeMillis();

        /**
         * Returns a unique id using the initial seed value.
         */
        private static synchronized long get() {
            return theirCurrentTime++;
        }
    }

    /**
     * The unique identifier for this bean.
     */
    private long myKey;

    /**
     * Default constructor; the internal key is set to a default value.
     */
    public AbstractEditKeyBean() {
        this(UniqueID.get());
    }

    /**
     * Constructs with given key.
     * @param key
     */
    public AbstractEditKeyBean(long key) {
        myKey = key;
    }

    // Override Objects's equal method.

    /**
     * Compares <code>obj</code> with this object according to
     * Java's equals() contract. Only returns <tt>true</tt> if the internal
     * keys for both objects match. Made it final to allow slice comparision
     * without violating transitivity law for equals() method.
     *
     * @param obj the object to compare.
     */
    public final boolean equals(Object obj) {
        // Identical to this?
        if (obj == this) {
            return true;
        }
        // Allow for slice comparision.
        if ((obj != null) && (obj instanceof AbstractEditKeyBean)) {
            return myKey == ((AbstractEditKeyBean) obj).myKey;
        }
        return false;
    }

    public int hashCode() {
        int result = 0;
        // Long attribute converted to twon ints and factored in.
        result = 17 * result + (int)(myKey >>> 32);
        result = 17 * result + (int)(myKey & 0xFFFFFFFF);
        return result;
    }

    /**
     * Makes a clone of this object.
     *
     * @return a cloned version of the current instance.
     * @throws CloneNotSupportedException for errors in cloning.
     */
    public Object clone() throws CloneNotSupportedException {
        AbstractEditKeyBean abstractEditKeyBean =  (AbstractEditKeyBean) super.clone();
        abstractEditKeyBean.setKey();
        return abstractEditKeyBean;
    }

    /**
     * Returns the key assigned to this bean.
     * @return the key as a long.
     */
    public long getKey() {
        return myKey;
    }

    /**
     * Set the Key with using the next UniqueID.get method.
     */
    private void setKey() {
        myKey = UniqueID.get();
    }

    // Protected methods

    /**
     * Helper method for subclasses to access the editor service.
     * @return the only editor service for the application.
     */
    protected EditorService getService() {
        return EditorService.getInstance();
    }

    /**
     * Resets the internal key to a new value.
     */
//    public void resetKey() {
//        myKey = UniqueID.get();
//    }
}
