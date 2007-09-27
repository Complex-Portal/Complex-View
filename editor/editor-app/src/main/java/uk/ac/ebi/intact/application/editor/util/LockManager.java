/*
Copyright (c) 2002-2004 The European Bioinformatics Institute, and others.
All rights reserved. Please see the file LICENSE
in the root directory of this distribution.
*/

package uk.ac.ebi.intact.application.editor.util;

import EDU.oswego.cs.dl.util.concurrent.ReadWriteLock;
import EDU.oswego.cs.dl.util.concurrent.WriterPreferenceReadWriteLock;
import org.apache.log4j.Logger;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import uk.ac.ebi.intact.application.editor.struts.framework.util.EditorConstants;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

/**
 * The lock manager keeps a track of edit objects. It uses the AC as a unique
 * identifier for edit objects. Only a single instance of this class is used
 * by multiple users. This class is thread safe.
 *
 * @author Sugath Mudali (smudali@ebi.ac.uk)
 * @version $Id$
 */
public class LockManager {
    /**
     * The logger to log error messages.
     */
    protected static final Log ourLogger = LogFactory.getLog(LockManager.class);

    // The lock object.
    public class LockObject {

        /**
         * The id.
         */
        private String myId;

        /**
         * The owner of the lock.
         */
        private String myOwner;

        /**
         * The time of the lock.
         */
        private Date myLockDate;

        private LockObject(String id, String owner) {
            myId = id;
            myOwner = owner;
            myLockDate = new Date();
        }

        // Needed by admin/locks.jsp to display the id.
        public String getId() {
            return myId;
        }

        public String getOwner() {
            return myOwner;
        }

        public Date getLockDate() {
            return myLockDate;
        }

        // Override Objects's equal method.

        /**
         * Compares <code>obj</code> with this object according to
         * Java's equals() contract. Only returns <tt>true</tt> if the id for both
         * objects match.
         *
         * @param obj the object to compare.
         */
        public boolean equals(Object obj) {
            // Identical to this?
            if (obj == this) {
                return true;
            }
            if ((obj != null) && (getClass() == obj.getClass())) {
                // Can safely cast it.
                return myId.equals(((LockObject) obj).myId);
            }
            return false;
        }
    }

    // ------------------------------------------------------------------------

    // Class Data

    /**
     * The only instance of this class.
     */
    private static LockManager ourInstance = new LockManager();

    // Instance Data

    /**
     * The R/W lock.
     */
    private ReadWriteLock myRWLock = new WriterPreferenceReadWriteLock();

    /**
     * A list of object ids (ACs) which are in use.
     */
    private List myLocks = new ArrayList();

    // Constructors

    /**
     * Default constructor; make it private to stop it from instantiating.
     */
    private LockManager() {
    }

    /**
     * @return the only instance of this class.
     */
    public static LockManager getInstance() {
        return ourInstance;
    }

    /**
     * Checks the existence of given lock.
     *
     * @param ac the id to check for the lock
     * @return true if a lock exists for <code>ac</code>; false is returned for
     *         all other instances.
     */
    public boolean hasLock(String ac) {
        try {
            myRWLock.readLock().acquire();
            for (Iterator iter = myLocks.iterator(); iter.hasNext();) {
                LockObject lo = (LockObject) iter.next();
                if (lo.getId().equals(ac)) {
                    return true;
                }
            }
        }
        catch (InterruptedException ie) {
            ourLogger.info(ie);
        }
        finally {
            myRWLock.readLock().release();
        }
        return false;
    }

    /**
     * Returns the lock object for given id if it exists.
     *
     * @param id the of the lock.
     * @return the lock object for <code>id</code> if a lock exists for it. Null
     *         is returned if there is no lock object.
     */
    public LockObject getLock(String id) {
        try {
            myRWLock.readLock().acquire();
            for (Iterator iter = myLocks.iterator(); iter.hasNext();) {
                LockObject lo = (LockObject) iter.next();
                if (lo.getId().equals(id)) {
                    return lo;
                }
            }
        }
        catch (InterruptedException ie) {
            ourLogger.info(ie);
        }
        finally {
            myRWLock.readLock().release();
        }
        return null;
    }

    /**
     * Returns the owner for given id if it exists.
     *
     * @param id the of the lock.
     * @return the owner of <code>id</code> if there is an owner or an empty
     *         street is returned.
     */
    public String getOwner(String id) {
        LockObject lock = getLock(id);
        if (lock != null) {
            return lock.getOwner();
        }
        return "";
    }

    /**
     * Obtains a lock.
     *
     * @param id the id to obtain the lock for.
     * @param owner the onwer of the lock.
     * @return true if a lock was acquired successfully; false is returned for
     *         all other instances.
     */
    public boolean acquire(String id, String owner) {
        // Get any existing lock.
        LockObject lo = getLock(id);

        // Have we already got the lock?
        if (lo != null) {
            if (lo.getOwner().equals(owner)) {
                // The same user is trying the same lock; allow it.
                return true;
            }
            else {
                // Different user; don't allow it.
                return false;
            }
        }
        try {
            // Need to lock with the write lock as we are changing locks collection.
            myRWLock.writeLock().acquire();
            // There is no lock associated with the id; create a new lock.
            myLocks.add(new LockObject(id, owner));
        }
        catch (InterruptedException ie) {
            ourLogger.info(ie);
        }
        finally {
            myRWLock.writeLock().release();
        }
        return true;
    }

    /**
     * Removes a lock for given id.
     *
     * @param id the id for the lock. This could be null if the method is called
     * on a newly created object. Since there is lock associated with a new
     * object, this method simply returns for a null argument.
     */
    public void release(String id) {
        // Check for null id.
        if (id == null) {
            return;
        }
        // The lock to remove.
        LockObject lock = null;
        try {
            myRWLock.readLock().acquire();
            for (Iterator iter = myLocks.iterator(); iter.hasNext();) {
                LockObject lo = (LockObject) iter.next();
                if (lo.getId().equals(id)) {
                    lock = lo;
                    break;
                }
            }
        }
        catch (InterruptedException ie) {
            ourLogger.info(ie);
        }
        finally {
            myRWLock.readLock().release();
        }
        if (lock != null) {
            try {
                // Need to get the write lock to remove.
                myRWLock.writeLock().acquire();
                myLocks.remove(lock);
            }
            catch (InterruptedException ie) {
                ourLogger.info(ie);
            }
            finally {
                myRWLock.writeLock().release();
            }
        }
    }

    /**
     * Release all the locks held by given owner.
     *
     * @param owner the owner to release the locks for.
     */
    public void releaseAllLocks(String owner) {
        // Holds locks to release; to avoid concurrent modification ex.
        List locks = getLocks(owner);
        try {
            // Need to get the write lock to remove.
            myRWLock.writeLock().acquire();
            // Iterate through the temp locks and remove one by one from the cache.
            for (Iterator iter = locks.iterator(); iter.hasNext();) {
                myLocks.remove(iter.next());
            }
        }
        catch (InterruptedException ie) {
            ourLogger.info(ie);
        }
        finally {
            myRWLock.writeLock().release();
        }
    }

    /**
     * Returns a list locks held by given user
     *
     * @param owner the onwer to get the locks for.
     * @return a list of locks held by <code>owner</code>. None of the lock objects
     *         are cloned because LockObjects are immutable. The list can be empty
     *         if <code>owner</code> has no locks.
     */
    public List getLocks(String owner) {
        // The locks to return.
        List locks = new ArrayList();

        try {
            myRWLock.readLock().acquire();
            for (Iterator iter = myLocks.iterator(); iter.hasNext();) {
                LockObject lock = (LockObject) iter.next();
                if (lock.getOwner().equals(owner)) {
                    locks.add(lock);
                }
            }
        }
        catch (InterruptedException ie) {
            ourLogger.info(ie);
        }
        finally {
            myRWLock.readLock().release();
        }
        return locks;
    }

    /**
     * Returns a clone of the current locks.
     *
     * @return a  clone of the current locks. None of the lock objects
     *         are cloned because LockObjects are immutable.
     */
    public List getLocks() {
        List list = null;
        try {
            myRWLock.readLock().acquire();
            list = (List) ((ArrayList) myLocks).clone();
        }
        catch (InterruptedException ie) {
            ourLogger.info(ie);
        }
        finally {
            myRWLock.readLock().release();
        }
        return list;
    }
}
