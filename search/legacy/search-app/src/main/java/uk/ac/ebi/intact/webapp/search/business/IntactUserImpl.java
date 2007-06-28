/*
Copyright (c) 2002 The European Bioinformatics Institute, and others.
All rights reserved. Please see the file LICENSE
in the root directory of this distribution.
*/

package uk.ac.ebi.intact.webapp.search.business;

import uk.ac.ebi.intact.business.IntactException;
import uk.ac.ebi.intact.context.IntactContext;
import uk.ac.ebi.intact.model.IntactObject;
import uk.ac.ebi.intact.persistence.dao.DaoFactory;

import javax.servlet.http.HttpSessionBindingEvent;
import javax.servlet.http.HttpSessionBindingListener;
import java.io.Serializable;
import java.sql.SQLException;
import java.util.Collection;

/**
 * Stores information about an Intact Web user session. Instead of binding multiple objects, only an object of this
 * class is bound to a session, thus serving a single access point for multiple information.
 * <p>
 * This class implements the <tt>ttpSessionBindingListener</tt> interface for it can be notified of session time outs.
 *
 * @author Chris Lewington, Sugath Mudali (smudali@ebi.ac.uk)
 * @version $Id$
 */
public class IntactUserImpl<T extends IntactObject> implements IntactUserIF<T>, HttpSessionBindingListener, Serializable {

    /**
     * Reference to the DAO.
     */
    //private IntactHelper helper;

    private String myHelpLink;

    private String searchValue;

    private Class<T> searchClass;

    private String binaryValue;

    private String viewValue;

    private String filterValue;

    /**
     * Managment of the object chunk - Chunk to display
     */
    private int selectedChunk;


    /**
     * Constructs an instance of this class with given mapping file and the name of the data source class. Side-effects
     * of this constructor are that the User instance also has an <code>IntactHelper</code> and an
     * <code>XmlBuilder</code> created for use during a user session.
     *
     * @param mapping the name of the mapping file.
     * @param dsClass the class name of the Data Source.
     * @throws IntactException     thrown for any error in creating an IntactHelper, XmlBuilder etc
     */
    public IntactUserImpl( String mapping, String dsClass ) {
   
    }

    /**
     * Constructs an IntactUserImpl object.
     *
     * @throws IntactException ...
     */
    public IntactUserImpl() throws IntactException {

        //this.helper = new IntactHelper();
    }

    public int getSelectedChunk() {
        return selectedChunk;
    }

    public void setSelectedChunk( int selectedChunk ) {
        this.selectedChunk = selectedChunk;
    }

    // Implements HttpSessionBindingListener

    /**
     * Will call this method when an object is bound to a session. Not doing anything.
     */
    public void valueBound( HttpSessionBindingEvent event ) {
    }

    /**
     * Will call this method when an object is unbound from a session.
     */
    public void valueUnbound( HttpSessionBindingEvent event ) {
       /*
        try {
            this.helper.closeStore();
        }
        catch ( IntactException ie ) {
            //failed to close the store - not sure what to do here yet....
        }  */
    }

    // Implementation of IntactUserI interface.

    public String getUserName() {
        try
        {
            return getDaoFactory().getBaseDao().getDbUserName();
        }
        catch (SQLException e)
        {
            e.printStackTrace();
        }

        return null;
    }

    public String getDatabaseName() {
        String dbName = null;
        try
        {
            dbName = getDaoFactory().getBaseDao().getDbName();

            if (dbName.contains("/"))
            {
                dbName = dbName.substring(dbName.lastIndexOf("/"), dbName.length());
            }
        }
        catch (SQLException e)
        {
            e.printStackTrace();
        }

        return dbName;
    }

//    public String getSearchCritera() {
//        return this.searchCriteria;
//    }

    public <T extends IntactObject> Collection<T> search( Class<T> objectType, String searchParam,
                              String searchValue ) throws IntactException {
        //return helper.search( objectType, searchParam, searchValue );

        return getDaoFactory().getIntactObjectDao(objectType).getColByPropertyName(searchParam, searchValue);
    }

    public Class<T> getSearchClass() {
        return searchClass;
    }

    public void setSearchClass( Class<T> searchClass ) {
        this.searchClass = searchClass;
    }

    public String getBinaryValue() {
        return this.binaryValue;
    }

    public void setBinaryValue( String binaryValue ) {
        this.binaryValue = binaryValue;
    }

    public String getSearchValue() {
        return searchValue;
    }

    public void setSearchValue( String searchValue ) {
        this.searchValue = searchValue;
    }

    public void setHelpLink( String link ) {
        myHelpLink = link;
    }

    public String getHelpLink() {
        return myHelpLink;
    }

    public String getView() {
        return viewValue;
    }

    public void setView( String viewValue ) {
        this.viewValue = viewValue;
    }

    public void setFilter( String filterValue ) {
        this.filterValue = filterValue;
    }

    public String getFilter() {
        return this.filterValue;
    }

    private DaoFactory getDaoFactory()
    {
        return IntactContext.getCurrentInstance().getDataContext().getDaoFactory();
    }

}
