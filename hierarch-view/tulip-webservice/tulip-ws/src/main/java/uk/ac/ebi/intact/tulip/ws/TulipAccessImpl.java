/*
Copyright (c) 2002 The European Bioinformatics Institute, and others.
All rights reserved. Please see the file LICENSE
in the root directory of this distribution.
*/
package uk.ac.ebi.intact.tulip.ws;

// JDK
import org.apache.axis.MessageContext;
import org.apache.axis.session.Session;

import java.io.*;
import java.util.*;

/**
 * Purpose : <br>
 * Allows the user to send a TLP file to Tulip and get back the node's coordinate.
 *
 * @author Samuel KERRIEN (skerrien@ebi.ac.uk)
 */

public class TulipAccessImpl implements TulipAccess {

    /**************/
    /** StrutsConstants */
    /**************/

    /**
     * Where is it possible to find the property file int which that class find its parameter
     * such as the location of the console.
     * Currently the property file is provided in the Jar file of the web service, int the root.
     */
    private static String PROPERTY_FILE_LOCATION = "/WebService.properties";


    private static String ERROR_MESSAGE_KEY = "messages";

    /**
     * Definition of the Tulip supported mode (based on the following list)
     *
     *  #define Circular 1
     *  #define GEM 2
     *  #define GeneralGraph 4
     *  #define GeneralGraph3D 8
     *  #define GeneralGraphBox 16
     *  #define Random 32
     *  #define SpringElectrical 64
     *  #define Tute 128
     *
     *  for the section Sizes : viewSize
     *
     *  #define Auto_sizing 256
     *  #define FitToLabel 512
     *
     *  for the section metric : viewMetric
     *  Be careful -> this action action create a color proxy (necessary) with a linear default color proxy.
     *                If you want to change te color proxy then call for the modes colors : see below
     *
     *  #define ConnecandTree 1024
     *  #define Barycenter 2048
     *  #define Cluster 4096
     *  #define DagLevel 8192
     *
     *  for the section color : view color
     *  #define Linear 16384
     *  #define Distribution 32768
     *
     */
    private static String DEFAULT_MASK = "0";

    private static String CIRCULAR_MASK          = "1";
    private static String GEM_MASK               = "2";
    private static String GENERAL_GRAPH_MASK     = "4";
    private static String GENERAL_GRAPH_3D_MASK  = "8";
    private static String GENERAL_GRAPH_BOX_MASK = "16";
    private static String RANDOM_MASK            = "32";
    private static String SPRING_ELECTRICAL_MASK = "64";
    private static String TUTE_MASK              = "128";

    private static String AUTO_SIZING_MASK  = "256";
    private static String FIT_TO_LABEL_MASK = "512";

    private static String CONNECAND_TREE_MASK = "1024";
    private static String BARYCENTER_MASK     = "2048";
    private static String CLUSTER_MASK        = "4096";
    private static String DAG_LEVEL_MASK      = "8192";

    private static String LINEAR_MASK       = "16384";
    private static String DISTRIBUTION_MASK = "32768";



    /************************
     *  Instance variables  *
     ************************/

    /**
     * Where the console binary file is stored on the hard disk.
     */
    private String consoleLocation = null;
    private String tlpRepositoryPath = null;

    /**
     * Error messages in order to allow the user to know what happen on the web service
     */
    private Collection errorMessages;




    /****************************************/
    /** Public methods over the web service */
    /****************************************/

    /**
     * get the computed TLP content from tulip
     * @param tlpContent tlp content to compute
     * @param optionMask the option of the Tulip process
     * @return the computed tlp file content or <b>null</b> if an error occurs.
     */
    public ProteinCoordinate[] getComputedTlpContent ( String tlpContent, String optionMask ) {

        errorMessages = new Vector ();
        MessageContext ctx = MessageContext.getCurrentContext();
        Session session = ctx.getSession();
        session.set (ERROR_MESSAGE_KEY, errorMessages);

        // init the service
        readProperties (PROPERTY_FILE_LOCATION);

        if (null == consoleLocation) {
            // send back a meaningful message (maybe via an Exception)
            addErrorMessage ("Unable to find the console binary file, indeed, impossible to calculate coordinates.");
            return null;
        }

        // get a unique key
        String sessionKey = getUniqueIdentifier ();

        String inputFile  = tlpRepositoryPath + sessionKey + ".in";
        String outputFile = tlpRepositoryPath + sessionKey + ".out";

        // Store the content in a file on hard disk
        if (!storeInputFile (inputFile, tlpContent)) {
            addErrorMessage ("Unable to store on the hard (" + inputFile + ") disk the content of the TLP file.");
            deleteFile (inputFile);
            return null;
        }

        // call the tulip client in a new process
        if (!computeTlpFile (inputFile, outputFile, optionMask)) {
            deleteFile (inputFile);
            deleteFile (outputFile);
            return null;
        }

        // Read the content of the generated file and create a collection of proteinCoordinate
        Collection coordinateSet = parseOutputFile (outputFile);

        // delete temporary files
        deleteFile (inputFile);
        deleteFile (outputFile);

        // create the array
        ProteinCoordinate[] pc = new ProteinCoordinate[coordinateSet.size()];
        Iterator iterator = coordinateSet.iterator ();
        int i = 0;
        while (iterator.hasNext()) {
            pc[i++] = (ProteinCoordinate) iterator.next();
        }

        // Send back the computed content
        return pc;

    } // computeTlpContent


    /**
     * Allows the user to get messages produced byte the web service
     *
     * @param hasToBeCleaned delete all messages after sended them back to the client
     * @return an array of messages or <b>null</b> if no messages are stored.
     *
     **/
    public String[] getErrorMessages (boolean hasToBeCleaned) {

        MessageContext ctx = MessageContext.getCurrentContext();
        Session session = ctx.getSession();
        Collection errorMessages = (Collection) session.get (ERROR_MESSAGE_KEY);

        if (null == errorMessages)
            return null;

        // Don't try to use the Collection.toArray() method, it give you back Object[]
        // and also an Exception when you send back to the client (even with a String[] cast)

        int count = errorMessages.size();
        String[] messagesArray = new String[count];

        count = 0;
        for (Iterator iterator = errorMessages.iterator(); iterator.hasNext(); ) {
            messagesArray [count++] = (String) iterator.next();
        }

        // clean up messages
        if (hasToBeCleaned) cleanErrorMessages ();

        return messagesArray;

    } // getErrorMessages



    /**
     * Clean message list
     */
    public void cleanErrorMessages () {

        if (null == errorMessages) {
            errorMessages = new Vector ();
            return;
        }

        errorMessages.clear ();

    } // cleanErrorMessages







    /*********************/
    /** Internal methods */
    /*********************/


    /**
     * Add a message to the message list
     *
     * @param aMessage the message to add
     */
    private void addErrorMessage (String aMessage) {

        if (null == errorMessages) errorMessages = new Vector ();
        errorMessages.add (aMessage);

    } // addErrorMessage



    /**
     * Read from the property file all needed properties
     *
     */
    private void readProperties (String filename) {

        InputStream is = null;

        // get the content of the property file
        is = TulipAccessImpl.class.getResourceAsStream (filename);

        Properties properties = null;

        try {
            // Create the Properties object with the content of the red file
            if (is != null) {
                properties = new Properties ();
                properties.load (is);
            } else {
                addErrorMessage ("Unable to find the property file : " + filename );
            }
        } catch (IOException ioe) {
            addErrorMessage ("Unable to read the content of the property file : " + filename );
        }


        if (null != properties) {
            // look for properties ...
            this.consoleLocation = properties.getProperty ("webService.console.location");
            this.tlpRepositoryPath = properties.getProperty ("webService.tlpRepository.path");
        }

        if (null == this.consoleLocation) {
            addErrorMessage ("Unable to find the following property : <b>webService.console.location</b> " +
                    "in the property file ("+ filename +")");
        } else {
            // TODO: check if the file is physically on the hard disk
        }

        String separator = System.getProperty ("file.separator");

        if (false == this.tlpRepositoryPath.endsWith(separator)) {
            this.tlpRepositoryPath += separator;
        }

    } // readProperties


    /**
     * get the line separator string.
     * It allows to use the same separator int the service and int the client
     * to keep the multiplateform aspect.
     *
     * @return the line separator
     */
    private String getLineSeparator () {
        return System.getProperty ("line.separator");
    } // getLineSeparator


    /** Purpose : <br>
     * Creates a pure identifier that is unique with respect to the host on which it is generated
     *
     * @return a unique key
     */
    private String getUniqueIdentifier () {

        return new java.rmi.server.UID().toString();

    } // getSessionKey



    /** Purpose : <br>
     * Store the content int a file called <i>anInputFile</i>.
     *
     * @param anInputFile the path of the file to write in.
     * @param aTlpContent the string to write in the file.
     * @return true if the file is right written, false else.
     */
    private boolean storeInputFile (String anInputFile, String aTlpContent) {

        try {
            FileWriter fileWriter = new FileWriter (anInputFile);
            BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
            bufferedWriter.write(aTlpContent);
            bufferedWriter.close();
            fileWriter.close();
        } catch (IOException e) {
            return false;
        }
        return true;

    } // storeInputFile



    /** Purpose : <br>
     * Call the tulip client by creating a new process.
     * client syntax is the following :
     *            cli <IP tulip> <PORT tulip> <input file> <output file>
     *
     * @param anInputFile the path of the input TLP file
     * @param anOutputFile the path of the output TLP file
     * @param aMask the mask allows to choose the tulip mode used to compute the tlp file
     * @return true if the computed file is well generated, false else.
     */
    private boolean computeTlpFile (String anInputFile, String anOutputFile, String aMask) {

        // test the validity of the mask, set to a default value if it's wrong.
        if ((null == aMask) || (0 == aMask.length())) {
            // warning log : mask undefined
            aMask = DEFAULT_MASK;
        } else {
            int i = 0;
            try {
                i = Integer.parseInt (aMask);
            } catch (NumberFormatException nfe) {
                // warning log : invalid mask format
                aMask = DEFAULT_MASK;
            }
            if ((i < 0) || (i > 65535)) {
                // warning log : mask out of range
                aMask = DEFAULT_MASK;
            }
        }

        try {
            Runtime runtime = Runtime.getRuntime();

            /**
             * Use the remote Tulip access via the cli <--> Serveur binary files.
             */

//       Process process = runtime.exec (TULIP_BINARY_CLIENT_FILE + " " +
// 				      TULIP_IP + " " + 
// 				      TULIP_PORT + " " + 
// 				      anInputFile + " " + 
// 				      anOutputFile + " " +
// 				      aMask);

            /**
             * Use the console binary file to access Tulip library, indeed,
             * Tulip have to be installed on the same conputer.
             */

            Process process = runtime.exec (consoleLocation + " " +
                    anInputFile + " " +
                    anOutputFile + " " +
                    aMask);

            // wait for the end of the tuip process, can be long if the TLP file is big.
            process.waitFor();
        } catch ( Exception e ) {
            // an error occurs during execution of the process
            addErrorMessage ("An error occured during the Tulip processing.");
            return false;
        }

        // test if the output file had been created
        File outputFile = new File(anOutputFile);
        if (!outputFile.exists()) {
            // an error occur on Tulip, no generated file
            addErrorMessage ("After the Tulip processing, no output file produced.");
            return false;
        }
        return true;

    } // computeTlpFile



    /** Purpose : <br>
     * Read a file that the path is given in parameter and store the content in a string.
     *
     * @param anOutputFile the path of the file to read
     * @return the content of the file or null if a problems occur.
     */
    private String readOutputFile (String anOutputFile) {

        StringBuffer stringBuffer = new StringBuffer ();
        try {
            FileReader fileReader = new FileReader(anOutputFile);
            BufferedReader bufferedReader = new BufferedReader(fileReader);
            String currentLine;
            String newLine = getLineSeparator();

            while( (currentLine = bufferedReader.readLine()) != null ){
                stringBuffer.append (currentLine + newLine);
            }

            bufferedReader.close();
            fileReader.close ();
        } catch (IOException e) {
            addErrorMessage ("Error during reading of the output file : " + anOutputFile);
            return null;
        }
        return stringBuffer.toString();

    } // readOutputFile



    /**
     * Parse the Tulip conputed TLP file to grab coordinates of each protein
     *
     * Here is the format of the section to parse :
     *
     * (property  0 layout "viewLayout"
     * (default "(105.000000,966.000000,359.000000)" "()" )
     * (node 1 "(215.500000,7.000000,0.000000)")
     * (node 2 "(57.500000,-288.000000,0.000000)")
     * (node 3 "(40.500000,191.000000,0.000000)")
     * (...)
     * )
     *
     * @param anOutputFile the path of the file to read
     * @return a collection of ProteinCoordinate
     */
    public Collection parseOutputFile (String anOutputFile) {

        ArrayList collection = new ArrayList ();

        try {
            FileReader fileReader = new FileReader(anOutputFile);
            BufferedReader bufferedReader = new BufferedReader(fileReader);
            String currentLine;

            while (( currentLine = bufferedReader.readLine() ) != null ){
                if ( currentLine.equals ("") ) continue;

                // We look, now, word by word
                StringTokenizer st = new StringTokenizer (currentLine);

                if (st.nextToken().equals ("(property")) {
                    st.nextToken();
                    st.nextToken();
                    currentLine = st.nextToken();
                    // test null


                    // viewLayout treatment : parse and store computed coordinates
                    if (currentLine.equals ("\"viewLayout\"")) {

                        currentLine = bufferedReader.readLine(); // get pass the default line
                        currentLine = bufferedReader.readLine();
                        // test null

                        while (! currentLine.equals (")") ) {
                            int index;
                            float x,y;
                            StringBuffer buf;

                            st = new StringTokenizer (currentLine);

                            if (!st.nextToken().equals ("(node"))
                                throw new IOException ("Imported data don't contain the good number of nodes");

                            index   = (new Integer(st.nextToken())).intValue();
                            buf     = new StringBuffer (st.nextToken()); /* = "(15.0000, 45.2540, 78.454)" */
                            buf.delete (0,2);
                            buf.delete (buf.length() - 3, buf.length());

                            st = new StringTokenizer (buf.toString(), ",");
                            x  = Float.parseFloat (st.nextToken());
                            y  = Float.parseFloat (st.nextToken());

                            // Store coordinates
                            ProteinCoordinate pc = new ProteinCoordinate (index, x, y);
                            collection.add (pc);

                            currentLine = bufferedReader.readLine();
                        } // while

                        // stop reading file

                    } // if "viewLayout"
                } // if "property"
            } // while

            bufferedReader.close();
            fileReader.close ();
        } catch (IOException e) {
            e.printStackTrace();
            addErrorMessage ("Error during the parsing of the output file");
            return null;
        }

        return (Collection) collection;

    } // parseOutputFile



    /** Purpose : <br>
     * Delete a file from its pathname.
     *
     * @param aFilename the path of the file
     */
    private void deleteFile (String aFilename) {

        try {
            File file = new File (aFilename);
            file.delete ();
        } catch (Exception e) {
            addErrorMessage ("Unable to delete the file : " + aFilename);
            e.printStackTrace();
        }

    } // deleteFile


} // TulipAccessImpl











