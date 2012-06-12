/*
 * Copyright (c) 2002 The European Bioinformatics Institute, and others.
 * All rights reserved. Please see the file LICENSE
 * in the root directory of this distribution.
 */
package uk.ac.ebi.intact.application.commons.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;
import java.util.zip.GZIPOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * Provides utility to build ZIP/GZIP files.
 *
 * @author Samuel Kerrien (skerrien@ebi.ac.uk)
 * @version $Id$
 * @since <pre>01-Feb-2006</pre>
 */
public class ZipBuilder {

    private static final int BUFFER_SIZE = 1024;

    /**
     * Build a GZIP file.
     *
     * @param gzipFile output file.
     * @param file     the file to gzip.
     */
    public static void createGZipFile( File gzipFile, File file ) throws IOException {
        // Create the GZIP output stream
        GZIPOutputStream out = new GZIPOutputStream( new FileOutputStream( gzipFile ) );

        // Compress the files
        if ( ! file.canRead() ) {
            System.err.println( "ZipBuilder (GZip): Could not read " + file.getAbsolutePath() );
            return;
        }

        // Open the input file
        String inFilename = "infilename";
        FileInputStream in = new FileInputStream( inFilename );

        try{
            // Transfer bytes from the input file to the GZIP output stream
            byte[] buf = new byte[BUFFER_SIZE];
            int len;
            while ( ( len = in.read( buf ) ) > 0 ) {
                out.write( buf, 0, len );
            }
        }
        finally{
            in.close();

            // Complete the GZIP file
            out.finish();
            out.close();
        }
    }

    /**
     * Compress all given file into a ZIP file.
     *
     * @param zipFile      the output ZIP file.
     * @param includeFiles all File to be compressed
     */
    public static void createZipFile( File zipFile, Collection includeFiles ) throws IOException {
        createZipFile( zipFile, includeFiles, false );
    }

    /**
     * Compress all given file into a ZIP file.
     *
     * @param zipFile      the output ZIP file.
     * @param includeFiles all File to be compressed
     * @param verbose      if true, display verbose output on System.out
     */
    public static void createZipFile( File zipFile, Collection includeFiles, final boolean verbose ) throws IOException {

        if ( verbose ) {
            System.out.println( "ZIP: " + zipFile.getAbsolutePath() );
        }
        // Create a buffer for reading the files
        byte[] buf = new byte[BUFFER_SIZE];

        // Create the ZIP file
        ZipOutputStream out = new ZipOutputStream( new FileOutputStream( zipFile ) );

        try{
            // Compress the files
            for ( Iterator iterator = includeFiles.iterator(); iterator.hasNext(); ) {
                File entryFile = (File) iterator.next();

                if ( verbose ) {
                    System.out.println( "Adding: " + entryFile.getAbsolutePath() );
                }

                if ( ! entryFile.canRead() ) {
                    System.err.println( "ZipBuilder: Could not read " + entryFile.getAbsolutePath() );
                    continue;
                }

                // Add that file to the ZIP file.
                FileInputStream in = new FileInputStream( entryFile );

                try{
                    // Add ZIP entry to output stream.
                    out.putNextEntry( new ZipEntry( entryFile.getName() ) );

                    // Transfer bytes from the file to the ZIP file
                    int len;
                    while ( ( len = in.read( buf ) ) > 0 ) {
                        out.write( buf, 0, len );
                    }
                }
                finally{
                    // Complete the entry
                    out.closeEntry();
                    in.close();
                }
            }
        }
        finally{
            // Complete the ZIP file
            out.close();
        }
    }
}