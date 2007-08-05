package uk.ac.ebi.intact.service.imex;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import psidev.psi.mi.stylesheets.XslTransformException;
import psidev.psi.mi.stylesheets.XslTransformerUtils;
import uk.ac.ebi.intact.dataexchange.psimi.xml.exchange.PsiExchange;
import uk.ac.ebi.intact.model.Experiment;
import uk.ac.ebi.intact.model.IntactEntry;
import uk.ac.ebi.intact.model.Interaction;
import uk.ac.ebi.intact.model.Publication;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;

/**
 * Imex utility that allows selection of publication and export to an IMEx formatted PSI-MI XML 2.5 file.
 *
 * @author Samuel Kerrien
 * @Version $Id$
 * @Since 1.6.0
 */
public class ImexExporter {

    public static final Log log = LogFactory.getLog( ImexExporter.class );

    /**
     * Simple representation of a Date.
     * <p/>
     * Will be used to name our IMEx files.
     */
    private static final SimpleDateFormat SIMPLE_DATE_FORMATER = new SimpleDateFormat( "yyyy-MM-dd" );

    /**
     * Given a publication, build a PSI-MI XML entry.
     * <p/>
     * Note: a transaction has to be open outside the scope of this method.
     *
     * @param publication the publication to convert.
     * @return a non null entry.
     */
    public IntactEntry buildEntry( Publication publication ) {

        if ( publication == null ) {
            throw new NullPointerException();
        }

        IntactEntry entry = new IntactEntry();
        Collection<Interaction> interactions = new ArrayList<Interaction>();
        for ( Experiment exp : publication.getExperiments() ) {
            interactions.addAll( exp.getInteractions() );
        }
        entry.setInteractions( interactions );

        return entry;
    }

    public void exportImexFile( IntactEntry entry, File outputFile, boolean doExpandXml, boolean doGzip )
            throws ImexExporterException {

        if ( entry == null ) {
            throw new NullPointerException( "You must give a non null entry" );
        }

        Collection<IntactEntry> entries = new ArrayList<IntactEntry>( 1 );
        entries.add( entry );
        exportImexFile( entries, outputFile, doExpandXml, doGzip );
    }

    public String getTodayImexExportFileName() {
        return SIMPLE_DATE_FORMATER.format( new Date() ); // YYYY-MM-DD
    }

    /**
     * Checks is a given file is writeable.
     *
     * @param f              the file to chekc on.
     * @param failIfExists   if set to false, no exception is thrown if the given file exists.
     * @param deleteIdExists if set to true, and the given file exists, it get's deleted.
     *
     * @throws NullPointerException
     * @throws IllegalArgumentException
     */
    private void checkFileWriteable( File f, boolean failIfExists, boolean deleteIdExists ) {
        if ( f == null ) {
            throw new NullPointerException( "You must give a non null file" );
        }

        if ( deleteIdExists ) {
            if ( f.exists() ) {
                if ( !f.delete() ) {
                    if ( f.isDirectory() ) {
                        throw new IllegalArgumentException( "Failed to delete directory upon user request (it might not be empty): " + f.getAbsolutePath() );
                    } else {
                        throw new IllegalArgumentException( "Failed to delete file upon user request: " + f.getAbsolutePath() );
                    }
                }
            }
        }

        if ( failIfExists ) {
            if ( f.exists() ) {
                throw new IllegalArgumentException( "The given file already exists: " + f.getAbsolutePath() );
            }
        }

        if ( f.exists() && !f.canWrite() ) {
            throw new IllegalArgumentException( "No write access on the file: " + f.getAbsolutePath() );
        }
    }

    /**
     * Exports the given collection of IntactEntry in an IMEx export file. PSI expansion and GZIPing can be enabled.
     *
     * @param entries         entries to be exported.
     * @param outputDirectory output file.
     * @param doExpandXml     if set to <code>true</code>, the output file will be expanded.
     * @param doGzip          if set to <code>true</code>, the output file will be GZIPed.
     * @throws ImexExporterException should an error uccur during the processing.
     */
    public void exportImexFile( Collection<IntactEntry> entries, File outputDirectory, boolean doExpandXml, boolean doGzip )
            throws ImexExporterException {

        if ( entries == null ) {
            throw new NullPointerException( "You must give a non null colleciton of IntactEntriy" );
        }

        checkFileWriteable( outputDirectory, false, false );

        if ( !outputDirectory.isDirectory() ) {
            throw new IllegalArgumentException( "This is not a directory: " + outputDirectory.getAbsolutePath() );
        }

        File xmlFile = new File( outputDirectory, getTodayImexExportFileName() + ".xml" );

        checkFileWriteable( xmlFile, true, false );

        try {
            if ( log.isDebugEnabled() ) {
                log.debug( "Converting " + entries.size() + " IntactEntry to PSI-MI XML 2.5..." );
            }
            FileWriter writer = new FileWriter( xmlFile );
            PsiExchange.exportToPsiXml( writer, entries.toArray( new IntactEntry[entries.size()] ) );
            writer.flush();
            writer.close();
        } catch ( IOException ioe ) {
            throw new ImexExporterException( "An error occured while converting IntAct object to the PSI-MI " +
                                             "model in file: " + outputDirectory.getAbsolutePath(), ioe );
        }

        if ( doExpandXml ) {
            File expandedXmlFile = new File( outputDirectory, getTodayImexExportFileName() + ".expanded.xml" );
            if ( log.isDebugEnabled() ) {
                log.debug( "Preparing temporary file for XML expansion:" + expandedXmlFile.getAbsolutePath() );
            }

            checkFileWriteable( expandedXmlFile, true, true );

            log.debug( "Expanding PSI-MI XML 2.5 using Stylesheet..." );
            try {
                XslTransformerUtils.expandPsiMi25( xmlFile, expandedXmlFile );
            } catch ( XslTransformException e ) {
                throw new ImexExporterException( "An error occured while expanding PSI-MI XML 2.5" +
                                                 xmlFile.getAbsolutePath(), e );
            }

            // Makes sure xmlFile points to the file that has to be GZIPed or returned
            xmlFile = expandedXmlFile;
        }

        if ( doGzip ) {
            try {
                File gzippedFile = new File( outputDirectory, getTodayImexExportFileName() + ".xml.gz" );
                checkFileWriteable( gzippedFile, false, true );
                GzipUtils.gzip( xmlFile, gzippedFile );
            } catch ( IOException e ) {
                throw new ImexExporterException( "An error occured while GZIP-ing file: " + xmlFile.getAbsolutePath(), e );
            }
        }
    }

    public boolean check( Publication pub ) throws ImexExporterException {

        for ( Iterator<Experiment> iterator = pub.getExperiments().iterator(); iterator.hasNext(); ) {
            Experiment experiment = iterator.next();
            // Are all the experiments got an accepted flag
            if ( !isAccepted( experiment ) ) {
                // throw new ImexExporterException( "Experiment " + experiment.getShortLabel() + " wasn't accepted, Abort." );
                return false;
            }

            // Have all the interactions got a IMEx ID ?
            for ( Iterator<Interaction> iterator1 = experiment.getInteractions().iterator(); iterator1.hasNext(); ) {
                Interaction interaction = iterator1.next();
                if ( !hasImexIdentifier( interaction ) ) {
                    return false;
                }
            }
        }

        return true;
    }

    public boolean hasImexIdentifier( Interaction interaction ) {
        return false;
    }

    public boolean isAccepted( Experiment experiment ) {
        return false;
    }
}
