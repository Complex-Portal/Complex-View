package uk.ac.ebi.intact.service.imex;

import org.junit.Assert;
import org.junit.Test;
import uk.ac.ebi.intact.core.unit.IntactBasicTestCase;
import uk.ac.ebi.intact.core.unit.IntactMockBuilder;
import uk.ac.ebi.intact.model.Experiment;
import uk.ac.ebi.intact.model.IntactEntry;
import uk.ac.ebi.intact.model.Publication;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;

/**
 * ImexExporter Tester.
 *
 * @author Samuel Kerrien (skerrien@ebi.ac.uk)
 * @version $Id$
 * @since 1.6.0
 */
public class ImexExporterTest extends IntactBasicTestCase {

    //////////////////////////
    // Utility methods
    private Publication buildPublication( String pmid, int experimentCount, int interactionCount ) {
        IntactMockBuilder mockBuilder = new IntactMockBuilder();
        Publication pub = mockBuilder.createPublication( pmid );
        mockBuilder.createPrimaryReferenceXref( pub, pmid );
        for ( int i = 0; i < experimentCount; i++ ) {
            Experiment e1 = mockBuilder.createExperimentRandom( interactionCount );
            mockBuilder.createPrimaryReferenceXref( e1, pmid );
            Assert.assertEquals( interactionCount, e1.getInteractions().size() );
            e1.setPublication( pub );
            pub.addExperiment( e1 );
        }
        return pub;
    }

    private File getTargetDirectory() {
        String outputDirPath = ImexExporterTest.class.getResource( "/" ).getFile();
        Assert.assertNotNull( outputDirPath );
        File outputDir = new File( outputDirPath );
        // we are in test-classes, move one up
        outputDir = outputDir.getParentFile();
        Assert.assertNotNull( outputDir );
        Assert.assertTrue( outputDir.isDirectory() );
        Assert.assertEquals( "target", outputDir.getName() );
        return outputDir;
    }

    //////////////////
    // Tests

    @Test
    public void buildEntry() throws Exception {

        final String pmid = "12345678";
        Publication pub = buildPublication( pmid, 2, 3 );

        ImexExporter imexExporter = new ImexExporter();
        IntactEntry intactEntry = imexExporter.buildEntry( pub );
        Assert.assertNotNull( intactEntry );

        Assert.assertEquals( 2, intactEntry.getExperiments().size() );
        for ( Experiment experiment : intactEntry.getExperiments() ) {
            Assert.assertEquals( 3, experiment.getInteractions().size() );
        }
    }

    @Test
    public void exportImexFile() throws Exception {
        File target = getTargetDirectory();

        final String pmid = "12345678";
        Publication pub1 = buildPublication( pmid, 2, 3 );
        Publication pub2 = buildPublication( pmid, 1, 5 );
        Publication pub3 = buildPublication( pmid, 3, 2 );

        ImexExporter imexExporter = new ImexExporter();

        Collection<IntactEntry> entries = new ArrayList<IntactEntry>();
        entries.add( imexExporter.buildEntry( pub1 ) );
        entries.add( imexExporter.buildEntry( pub2 ) );
        entries.add( imexExporter.buildEntry( pub3 ) );

        imexExporter.exportImexFile( entries, target, true, true );

        // check that the files are there
        String today = imexExporter.getTodayImexExportFileName();

        File xmlFile = new File( target, today + ".xml");
        Assert.assertTrue( xmlFile.exists() );
        Assert.assertTrue( xmlFile.length() > 0 );

        File expandedXmlFile = new File( target, today + ".expanded.xml");
        Assert.assertTrue( expandedXmlFile.exists() );
        Assert.assertTrue( expandedXmlFile.length() > 0 );

        File gzippedFile = new File( target, today + ".xml.gz");
        Assert.assertTrue( gzippedFile.exists() );
        Assert.assertTrue( gzippedFile.length() > 0 );

        // uncompress the export file and check that the lenght is the same
        File ungzippedFile = new File( target, "ungzipped.xml");
        GzipUtils.gunzip( gzippedFile, ungzippedFile );
        Assert.assertTrue( ungzippedFile.exists() );
        Assert.assertTrue( ungzippedFile.length() > 0 );
        Assert.assertEquals( expandedXmlFile.length(), ungzippedFile.length() );        
    }
}