/*
 * Copyright (c) 2002 The European Bioinformatics Institute, and others.
 * All rights reserved. Please see the file LICENSE
 * in the root directory of this distribution.
 */
package uk.ac.ebi.intact.webapp.search.servlet;

import org.apache.log4j.Logger;
import uk.ac.ebi.intact.context.IntactContext;
import uk.ac.ebi.intact.util.SearchReplace;
import uk.ac.ebi.intact.webapp.search.business.Constants;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Searches the IntAct database and display the number of hits as well as all matching identifiers.
 *
 * @author Samuel Kerrien (skerrien@ebi.ac.uk)
 * @version $Id$
 * @since <pre>13-Feb-2006</pre>
 */
public class ExternalServiceSearchServlet extends HttpServlet implements Externalizable {

    private transient static Logger logger = Logger.getLogger( Constants.LOGGER_NAME );

    ////////////////////////////
    // Constants

    public static final String VERSION = "0.2";

    public static final String NEW_LINE = System.getProperty( "line.separator", "\\n" );

    public static final String TEXT_MIME_TYPE = "text/plain";

    public static final String QUERY_PARAM = "query";

    public static final String NO_RESULT_OUTPUT = "0";

    ///////////////////////////
    // Externalizable

    public void readExternal( ObjectInput in ) throws IOException, ClassNotFoundException {
        // the logger is transient as it is not serializable, so reset it upon deserialization.
        logger = Logger.getLogger( Constants.LOGGER_NAME );
    }

    public void writeExternal( ObjectOutput out ) throws IOException {
    }

    /////////////////////////////
    // HttpServlet

    protected void doGet( HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse )
            throws ServletException, IOException {

        // handle parameters
        String query = httpServletRequest.getParameter( QUERY_PARAM );

        httpServletResponse.setContentType( TEXT_MIME_TYPE );

        // write servlet output
        ServletOutputStream out = httpServletResponse.getOutputStream();
        out.write( search( query ).getBytes() );
        out.flush();
        out.close();
    }

    public String getServletInfo() {
        return "ExternalServiceSearch " + VERSION + " by Samuel Kerrien (skerrien@ebi.ac.uk)";
    }

    /////////////////////////////////
    // private methods

    private static String search( String query ) {

        if ( query == null ) {
            return NO_RESULT_OUTPUT; // TODO Should we decide upon ERROR code ?
        }

        query = query.toLowerCase(); // the IA_SEARCH table only contains lowercased value.

        // default operator that will be used to build the SQL query in case there are not % in the query.
        String operator = "=";

        if ( query.indexOf( "*" ) != -1 ) {
            query = SearchReplace.replace( query, "*", "%" );
            operator = "LIKE";
        }

        try {

            PreparedStatement statement = getConnection().prepareStatement( "SELECT distinct ac " +
                                                                       "FROM IA_SEARCH " +
                                                                       "WHERE value " + operator + " ?" );
            statement.setString( 1, query );
            ResultSet resultSet = statement.executeQuery();

            int count = 0;
            StringBuffer sb = new StringBuffer( 1024 );
            while ( resultSet.next() ) {
                String ac = resultSet.getString( 1 );

                sb.append( ac );
                sb.append( NEW_LINE );

                count++;
            }

            statement.close();

            // do not close the JDBC connection, helper.closeStore() will take care of handing it to the pool.

            return count + NEW_LINE + sb.toString();

        } catch ( SQLException e ) {
            logger.error( "An error occured while searching the IA_SEARCH table", e );
            return NO_RESULT_OUTPUT;
        } 
    }

    private static Connection getConnection()
    {
        return IntactContext.getCurrentInstance().getDataContext().getDaoFactory().connection();
    }
}