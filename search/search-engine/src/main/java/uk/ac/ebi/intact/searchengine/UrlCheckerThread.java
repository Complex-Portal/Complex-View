/*
 * Copyright (c) 2002 The European Bioinformatics Institute, and others.
 * All rights reserved. Please see the file LICENSE
 * in the root directory of this distribution.
 */
package uk.ac.ebi.intact.searchengine;

import org.apache.commons.httpclient.*;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.params.HttpMethodParams;

import java.io.IOException;
import java.io.Serializable;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Checks that a URL is live using a dedicated Thread.
 *
 * @author Samuel Kerrien (skerrien@ebi.ac.uk)
 * @version $Id$
 * @since <pre>07-Feb-2006</pre>
 */
public class UrlCheckerThread extends Thread implements Serializable {

    public static final boolean DEBUG = false;

    public static final String PROTOCOL_HTTP = "http";

    ////////////////////////////
    // Instance variables

    /**
     * Is the job finished.
     */
    private boolean finished = false;

    /**
     * The the URL given valid.
     */
    private boolean isValidUrl = false;

    /**
     * The URL to check upon.
     */
    private String url;

    //////////////////////////////
    // Constructor

    /**
     * Constructs a UrlCheckerThread.
     *
     * @param aUrl the url to check on.
     */
    public UrlCheckerThread( String aUrl ) {
        super( "URL Checker: " + aUrl );
        url = aUrl;
    }

    //////////////////////////////
    // Override of Thread

    /**
     * Runs the checking logic.
     */
    public void run() {

        if ( DEBUG ) {
            System.out.println( "Running check on " + url );
        }

        try {
            URL aUrl = new URL( url );

            if ( aUrl.getProtocol().equals( PROTOCOL_HTTP ) ) {
                isValidUrl = checkHttpUrl( url );
            } else {
                // general test
                isValidUrl = checkUrl( aUrl );
            }
        } catch ( MalformedURLException e ) {
            e.printStackTrace();
            isValidUrl = false;
        }

        finished = true;

        if ( DEBUG ) {
            System.out.println( "Finished (live=" + isValidUrl + ")" );
        }
    }

    ////////////////////////////
    // URL checking

    /**
     * Check upon HTTP URL.
     *
     * @param aUrl the URL to check upon.
     *
     * @return
     */
    private boolean checkHttpUrl( String aUrl ) {

        if ( aUrl == null ) {
            return false;
        }

        HttpClient client = new HttpClient();

        // Create a method instance.
        GetMethod method = new GetMethod( aUrl );

        // some server display a nice HTTP 404 page upon such status, in which case the status becomes 200 (ie. OK).
        // Setting the redirect to false should prevent redirect from happening.
        method.setFollowRedirects( false );

        // Provide custom retry handler if necessary
        method.getParams().setParameter( HttpMethodParams.RETRY_HANDLER, new DefaultHttpMethodRetryHandler( 1, false ) );

        try {
            // Execute the method.
            int statusCode = client.executeMethod( method );

            if ( DEBUG ) {
                System.out.println( "statusCode = " + statusCode );
            }
            if ( statusCode != HttpStatus.SC_OK ) {
                if ( DEBUG ) {
                    System.out.println( "Method failed: " + method.getStatusLine() );
                }
                return false;
            }

            String redirectLocation = null;
            Header locationHeader = method.getResponseHeader( "location" );

            if ( locationHeader != null ) {
                redirectLocation = locationHeader.getValue();
                if ( DEBUG ) {
                    System.out.println( "redirectLocation = " + redirectLocation );
                }
            } else {
                if ( DEBUG ) {
                    System.out.println( "redirectLocation = " + redirectLocation );
                }

                // The response is invalid and did not provide the new location for
                // the resource.  Report an error or possibly handle the response
                // like a 404 Not Found error.
            }

            // if everything went well, then the URL is said valid.
            return true;

        } catch ( HttpException e ) {
            if ( DEBUG ) {
                System.err.println( "Fatal protocol violation: " + e.getMessage() );
            }
            e.printStackTrace();
        } catch ( IOException e ) {
            if ( DEBUG ) {
                System.err.println( "Fatal transport error: " + e.getMessage() );
                e.printStackTrace();
            }
        } catch ( Exception e ) {
            if ( DEBUG ) {
                System.err.println( "Fatal error: " + e.getMessage() );
                e.printStackTrace();
            }
        } finally {
            // Release the connection.
            method.releaseConnection();
        }

        return false;
    }


    /**
     * Checks if a URL is live.
     *
     * @param aUrl the URL to check upon
     *
     * @return true of the URL is live, false otherwise.
     */
    private boolean checkUrl( URL aUrl ) {

        if ( aUrl == null ) {
            return false;
        }

        try {
            try {
                HttpURLConnection.setFollowRedirects( false );
            } catch ( SecurityException e ) {
                e.printStackTrace();
            }

            // Opening the stream will fail if the file is not found (java.io.FileNotFoundException).
            aUrl.openStream().close();

            return true;

        } catch ( Exception e ) {
            if ( DEBUG ) {
                e.printStackTrace();
            }
            return false;
        }
    }

    /////////////////////////////
    // Getters

    /**
     * Answers the question: "Has the check upon the given URL been completed ?".
     *
     * @return true if the check has been completed, false otherwise.
     */
    public boolean hasFinished() {
        return finished;
    }

    public static final int CHUNK_OF_TIME = 50; // milliseconds

    /**
     * Answers the question: "Has the check upon the given URL been completed ?".
     * <p/>
     * In order not to optimize the sleep time of the thread, we break the time given by the user into chunks of 50ms
     * ... so even if the user ask for a minute, we will release as soon as the job has completed.
     *
     * @param waitIfNotFinished count of millisecond to sleep in case the processing is still not finished upon call.
     *
     * @return true if the check has been completed, false otherwise.
     */
    public boolean hasFinished( long waitIfNotFinished ) {
        if ( !finished ) {
            try {
                if ( waitIfNotFinished > 0 ) {
                    if ( DEBUG ) {
                        System.out.println( "Waiting " + waitIfNotFinished + "ms" );
                    }

                    // wait until we reach the maximum set by the user.
                    int sum = 0;
                    do {
                        if ( DEBUG ) {
                            System.out.println( sum + "ms : finished = " + finished );
                        }
                        Thread.currentThread().sleep( CHUNK_OF_TIME );
                        sum += CHUNK_OF_TIME;
                    } while ( sum < waitIfNotFinished && ( !finished ) );
                }
            } catch ( InterruptedException e ) {
                e.printStackTrace();
            }
        }

        if ( DEBUG ) {
            System.out.println( "After waiting, finished flag was " + finished );
        }

        return finished;
    }

    /**
     * Answers the question: "is the given URL live ?".
     *
     * @return true if after checking the URL was found to be live.
     */
    public boolean isValidUrl() {
        return isValidUrl;
    }

    /**
     * Gives the URL checked upon.
     *
     * @return
     */
    public String getUrl() {
        return url;
    }
}