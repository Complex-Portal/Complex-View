package uk.ac.ebi.intact.service.imex;

import org.junit.Assert;
import org.junit.Test;
import uk.ac.ebi.intact.core.unit.IntactBasicTestCase;
import uk.ac.ebi.intact.core.unit.IntactMockBuilder;
import uk.ac.ebi.intact.model.*;

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

    // internal counter for assigning unique IMEx identifier
    private int imexId = 1;

    private synchronized String getNextImexIdentifier() {
        String id = "IM-" + imexId;
        imexId++;
        return id;
    }

    private Publication buildPublication( String pmid, int experimentCount, int interactionCount,
                                          boolean acceptAllExperiments,
                                          boolean autoAddImexIdentifiers ) {

        IntactMockBuilder mockBuilder = new IntactMockBuilder();
        Publication pub = mockBuilder.createPublication( pmid );
        pub.addXref( mockBuilder.createPrimaryReferenceXref( pub, pmid ) );

        for ( int i = 0; i < experimentCount; i++ ) {
            Experiment experiment = mockBuilder.createExperimentRandom( interactionCount );
            mockBuilder.createPrimaryReferenceXref( experiment, pmid );
            Assert.assertEquals( interactionCount, experiment.getInteractions().size() );
            experiment.setPublication( pub );
            pub.addExperiment( experiment );

            if ( acceptAllExperiments ) {
                // accept experiment
                CvTopic accepted = new CvTopic( experiment.getOwner(), CvTopic.ACCEPTED );
                Annotation a = new Annotation( experiment.getOwner(), accepted );
                experiment.addAnnotation( a );
            }

            if ( autoAddImexIdentifiers ) {
                // add IMEx identifier
                CvDatabase imex = mockBuilder.createCvObject( CvDatabase.class, "MI:0670", "imex" );
                Assert.assertEquals( 1, imex.getXrefs().size() );

                for ( Interaction interaction : experiment.getInteractions() ) {
                    String id = getNextImexIdentifier();
                    InteractorXref xref = new InteractorXref( experiment.getOwner(), imex, id, null );
                    interaction.addXref( xref );
                }
            }
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

    @Test(expected=ImexExporterException.class)
    public void buildEntry_experiment_not_accepted() throws Exception {

        final String pmid = "12345678";
        Publication pub = buildPublication( pmid, 2, 3, false, true );

        ImexExporter imexExporter = new ImexExporter();
        IntactEntry intactEntry = imexExporter.buildEntry( pub );
        Assert.fail( "The dataset contains some non accepted experiment. It should have failed." );
    }

    @Test(expected=ImexExporterException.class)
    public void buildEntry_interaction_without_imex_id() throws Exception {

        final String pmid = "12345678";
        Publication pub = buildPublication( pmid, 2, 3, true, false );

        ImexExporter imexExporter = new ImexExporter();
        IntactEntry intactEntry = imexExporter.buildEntry( pub );
        Assert.fail( "the dataset contains some interaction without IMEx identifier. It should have failed." );
    }

    @Test
    public void buildEntry() throws Exception {

        final String pmid = "12345678";
        Publication pub = buildPublication( pmid, 2, 3, true, true );

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
        Publication pub1 = buildPublication( pmid, 2, 3, true, true );
        Publication pub2 = buildPublication( pmid, 1, 5, true, true );
        Publication pub3 = buildPublication( pmid, 3, 2, true, true );

        ImexExporter imexExporter = new ImexExporter();

        Collection<IntactEntry> entries = new ArrayList<IntactEntry>();
        entries.add( imexExporter.buildEntry( pub1 ) );
        entries.add( imexExporter.buildEntry( pub2 ) );
        entries.add( imexExporter.buildEntry( pub3 ) );

        imexExporter.exportImexFile( entries, target, true, true );

        // check that the files are there
        String today = imexExporter.getTodayImexExportFileName();

        File xmlFile = new File( target, today + ".xml" );
        Assert.assertTrue( xmlFile.exists() );
        Assert.assertTrue( xmlFile.length() > 0 );

        File expandedXmlFile = new File( target, today + ".expanded.xml" );
        Assert.assertTrue( expandedXmlFile.exists() );
        Assert.assertTrue( expandedXmlFile.length() > 0 );

        File gzippedFile = new File( target, today + ".xml.gz" );
        Assert.assertTrue( gzippedFile.exists() );
        Assert.assertTrue( gzippedFile.length() > 0 );

        // uncompress the export file and check that the lenght is the same
        File ungzippedFile = new File( target, "ungzipped.xml" );
        GzipUtils.gunzip( gzippedFile, ungzippedFile );
        Assert.assertTrue( ungzippedFile.exists() );
        Assert.assertTrue( ungzippedFile.length() > 0 );
        Assert.assertEquals( expandedXmlFile.length(), ungzippedFile.length() );
    }
}