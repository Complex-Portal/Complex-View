/*
Copyright (c) 2002 The European Bioinformatics Institute, and others.
All rights reserved. Please see the file LICENSE
in the root directory of this distribution.
*/

package uk.ac.ebi.intact.application.editor.exception;

import java.io.PrintStream;
import java.io.PrintWriter;

/**
 * This is the common superclass for all application.exception; This
 * class and its subclasses support the chained exception facility.
 *
 * @author Sugath Mudali (smudali@ebi.ac.uk)
 * @version $Id$
 */
public class BaseException extends Exception {

    /**
     * The root exception.
     */
    private Throwable myRootException;

    /**
     * The message key to search in the resource file.
     */
    private String myMessageKey;

    /**
     * The message arguments.
     */
    private String[] myMessageArgs;

    // Constructors

    /**
     * Default constructor.
     */
    public BaseException() {
    }

    /**
     * Constructor with a message.
     * @param msg the message explaing the exception.
     */
    public BaseException(String msg) {
        super(msg);
    }

    /**
     * Constructor that takes a root exception.
     * @param cause the root exception.
     */
    public BaseException(Throwable cause) {
        myRootException = cause;
    }

    /**
     * Print both the normal and root stack traces.
     * @param writer the writer to print the stack trace.
     */
    public void printStackTrace(PrintWriter writer) {
        super.printStackTrace(writer);
        if (getRootCause() != null) {
            getRootCause().printStackTrace(writer);
        }
        writer.flush();
    }

    /**
     * Print both the normal and root stack traces.
     * @param out the output stream.
     */
    public void printStackTrace(PrintStream out) {
        printStackTrace(new PrintWriter(out));
    }

    /**
     * Print both the normal and root stack traces to the std err.
     */
    public void printStackTrace() {
        printStackTrace(System.err);
    }

    /**
     * Return the root exception, if one exists.
     * @return the root exception if it exists or else null.
     */
    public Throwable getRootCause() {
        return myRootException;
    }

    /**
     * Sets the message key.
     * @param key the key to search in the application resources file.
     */
    public void setMessageKey(String key) {
        myMessageKey = key;
    }

    /**
     * Returns the message key
     * @return the key to search in the application resources file.
     */
    public String getMessageKey() {
        return myMessageKey;
    }

    /**
     * The arguments to pass to place holders in the application resources file.
     * @param args an array of arguments to pass to place holders.
     */
    public void setMessageArgs(String[] args) {
        myMessageArgs = args;
    }

    /**
     * Returns an array of arguments to pass to place holders application
     * resources file.
     * @return an array of String arguments to pass to place holders.
     */
    public String[] getMessageArgs() {
        return myMessageArgs;
    }
}
