package uk.ac.ebi.intact.webapp.search.advancedSearch.util.logging;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Properties;

public class CustomisedDailyRollingFileAppender extends org.apache.log4j.DailyRollingFileAppender {

    // ----------------------------------------------------------- static content

    /**
     * The option the user can use to add the hostname in the file path
     */
    private static final String HOSTNAME_FLAG = "$hostname";
    private static final String USERTNAME_FLAG = "$username";
    private static final String TEMP_DIR_FLAG = "$tmp";

    private static String hostname;
    private static String username;
    private static String tempDir;

    static {

        /*
         * Try to get some information for eventual later use.
         */

        // hostname
        try {
            hostname = InetAddress.getLocalHost().getHostName();
        } catch (UnknownHostException e) {
            e.printStackTrace();
            hostname = "noHostnameFound";
        }

        // username
        Properties props = System.getProperties();
        username = props.getProperty("user.name");

        tempDir = props.getProperty("java.io.tmpdir");
        if (tempDir == null) {
            tempDir = "";
        }
    }

    // ----------------------------------------------------------- private method

    /**
     * Replace all occurence of a string pattern by a replacement string and return the
     * result string.
     *
     * @param inputStr       the string to search an replace in
     * @param patternStr     the pattern string to replace
     * @param replacementStr the replacement string
     * @return the result string
     */
    private String replace(String inputStr, String patternStr, String replacementStr) {
        int patternLength = patternStr.length();
        int inputLength = inputStr.length();
        int indexOfFlag = inputStr.indexOf(patternStr, 0);

        while (indexOfFlag > -1) {
            inputStr = inputStr.substring(0, indexOfFlag) +
                    replacementStr +
                    inputStr.substring(indexOfFlag + patternLength, inputLength);
            /*
             * Search for any other match after the current replacement
             * We don't take into account overlapping match
             */
            indexOfFlag = inputStr.indexOf(patternStr, indexOfFlag + patternLength);
        }

        return inputStr;
    }


    /**
     * Apply a set of customisation on the filename. That modification is done only once.
     *
     * @param filename the filename to customise
     * @return the customised filename.
     */
    private String customizeFilename(String filename) {
        filename = replace(filename, HOSTNAME_FLAG, hostname);
        filename = replace(filename, USERTNAME_FLAG, username);
        filename = replace(filename, TEMP_DIR_FLAG, tempDir);

        // Path converstion
        if (File.separator.equals("/")) {
            // unix
            filename = replace(filename, "\\", "/");

        } else if (File.separator.equals("\\")) {
            // windows
            filename = replace(filename, "/", "\\");
        }


        return filename;
    }


    // ----------------------------------------------------------- public method

    /**
     * Over definition of that method inherited from FileAppender.
     * It allows to specify in the file path the hostname by using $hostname
     * <p/>
     * e.g. if myAppender.File=/tmp/$hostname_myfile.log and your host is mycomputer.ebi.ac.uk,
     * it will become /tmp/mycomputer.ebi.ac.uk_myfile.log,
     *
     * @throws IOException
     */
    public void setFile(String filename, boolean append, boolean bufferedIO, int bufferSize)
            throws IOException {
        filename = customizeFilename(filename);
        System.out.println("Write File to " + filename);
        super.setFile(filename, append, bufferedIO, bufferSize);
    }
}
