package uk.ac.ebi.intact.service.imex;

import java.io.*;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

/**
 * Handling of GZIP files.
 *
 * @author Samuel Kerrien
 * @version $Id$
 * @since 1.6.0
 */
public class GzipUtils {

    /**
     * Compress a file using GZIP.
     *
     * @param in  file to compress
     * @param out compressed file
     * @throws java.io.IOException if an I/O error occurs.
     */
    public static void gzip( File in, File out ) throws IOException {

        // Create the GZIP output stream
        GZIPOutputStream gzout = new GZIPOutputStream( new FileOutputStream( out ) );

        // Open the input file
        FileInputStream fis = new FileInputStream( in );

        // Transfer bytes from the input file to the GZIP output stream
        byte[] buf = new byte[1024];
        int len;
        while ( ( len = fis.read( buf ) ) > 0 ) {
            gzout.write( buf, 0, len );
        }
        fis.close();

        // Complete the GZIP file
        gzout.finish();
        gzout.close();
    }

    /**
     * Uncompress a file using GZIP.
     *
     * @param in  file to uncompress
     * @param out resulting uncompressed file
     * @throws java.io.IOException if an I/O error occurs.
     */
    public static void gunzip( File in, File out ) throws IOException {

        // Open the compressed file
        InputStream is = new GZIPInputStream( new FileInputStream( in ) );

        // Open the output file
        OutputStream os = new FileOutputStream( out );

        // Transfer bytes from the compressed file to the output file
        byte[] buf = new byte[1024];
        int len;
        while ( ( len = is.read( buf ) ) > 0 ) {
            os.write( buf, 0, len );
        }

        // Close the file and stream
        is.close();
        os.close();
    }
}
