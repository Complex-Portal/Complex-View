package uk.ac.ebi.intact.service.graph.binary;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Assert;
import static org.junit.Assert.assertEquals;
import org.junit.Test;
import psidev.psi.mi.tab.PsimiTabReader;
import psidev.psi.mi.tab.model.BinaryInteraction;
import uk.ac.ebi.intact.psimitab.IntActBinaryInteraction;
import uk.ac.ebi.intact.psimitab.IntActColumnHandler;
import uk.ac.ebi.intact.service.graph.binary.merger.BinaryGraphNetworkMerger;
import uk.ac.ebi.intact.service.graph.binary.merger.InteractionBasedMerger;
import uk.ac.ebi.intact.service.graph.io.GraphIOUtils;

import java.io.File;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collection;


public class BinaryGraphNetworkFactoryTest {

    public static final Log log = LogFactory.getLog(BinaryGraphNetworkFactoryTest.class);

    @Test
    public void createTest() throws Exception {
        File file = TestHelper.getFileByResources( "/test-files/brca2-simple.txt", BinaryGraphNetworkFactoryTest.class);
        //File file = new File(BinaryGraphNetworkFactoryTest.class.getResource("/test-files/brca2-simple.txt").getFile());
        PsimiTabReader reader = new PsimiTabReader(true);

        Collection<BinaryInteraction> binaryInteractions = reader.read(file);
        BinaryGraphNetwork graphNetwork = new BinaryGraphNetworkBuilder().createGraphNetwork(binaryInteractions);
        Assert.assertNotNull(graphNetwork);
    }

    @Test
    public void createTest_2() throws Exception {
        File file = TestHelper.getFileByResources( "/test-files/brca2_IntActBinaryInteraction.txt", BinaryGraphNetworkFactoryTest.class);
        //File file = new File(BinaryGraphNetworkFactoryTest.class.getResource("/test-files/brca2-simple.txt").getFile());
        PsimiTabReader reader = new PsimiTabReader(true);
        reader.setBinaryInteractionClass( IntActBinaryInteraction.class );
        reader.setColumnHandler( new IntActColumnHandler() );

        Collection<BinaryInteraction> binaryInteractions = reader.read(file);
        BinaryGraphNetwork graphNetwork = new BinaryGraphNetworkBuilder().createGraphNetwork(binaryInteractions);
        System.out.println( graphNetwork.toString());
    }

//    @Test
//    public void exportTest() throws Exception {
//        File file = TestHelper.getFileByResources( "/test-files/brca2-simple.txt", BinaryGraphNetworkFactoryTest.class);
//        //File file = new File(BinaryGraphNetworkFactoryTest.class.getResource("/test-files/brca2-simple.txt").getFile());
//        PsimiTabReader reader = new PsimiTabReader(true);
//
//        Collection<BinaryInteraction> binaryInteractions = reader.read(file);
//        BinaryGraphNetwork graphNetwork = new BinaryGraphNetworkBuilder().createGraphNetwork(binaryInteractions);
//
//        Writer writer = new StringWriter();
//
//        GraphIOUtils.exportNetworkToGraphML(graphNetwork, writer);
//
//        System.out.println(writer.toString());
//    }


    @Test
    public void fusionTest() throws Exception{
        PsimiTabReader reader = new PsimiTabReader(true);
        reader.setBinaryInteractionClass( IntActBinaryInteraction.class );
        reader.setColumnHandler( new IntActColumnHandler() );

        BinaryGraphNetworkBuilder builder = new BinaryGraphNetworkBuilder();

        File brca2 = TestHelper.getFileByResources( "/test-files/brca2_IntActBinaryInteraction.txt", BinaryGraphNetworkFactoryTest.class);
        Collection<BinaryInteraction> binaryInteractions = reader.read(brca2);
        Collection<String> centralAcs = new ArrayList();
        centralAcs.add("EBI-79792");
        centralAcs.add("EBI-1034100");
        BinaryGraphNetwork graphNetwork1 = builder.createGraphNetwork(binaryInteractions, centralAcs);
        assertEquals( 1, graphNetwork1.getCentralNodes().size());
        assertEquals( 7, graphNetwork1.getNodes().size());

        File fancd2 = TestHelper.getFileByResources( "/test-files/fancd2.txt", BinaryGraphNetworkFactoryTest.class);
        binaryInteractions = reader.read(fancd2);
        centralAcs = new ArrayList();
        centralAcs.add("EBI-359343");
        BinaryGraphNetwork graphNetwork2 = builder.createGraphNetwork(binaryInteractions, centralAcs);
        assertEquals( 1, graphNetwork2.getCentralNodes().size());
        assertEquals( 9, graphNetwork2.getNodes().size());

        BinaryGraphNetworkMerger merger = new InteractionBasedMerger();
        BinaryGraphNetwork network = merger.mergeGraphNetworks( graphNetwork1, graphNetwork2);
        assertEquals(2, network.getCentralNodes().size());
    }
}



