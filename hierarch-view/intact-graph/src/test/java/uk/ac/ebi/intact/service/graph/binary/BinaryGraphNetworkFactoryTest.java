package uk.ac.ebi.intact.service.graph.binary;

import edu.uci.ics.jung.graph.decorators.StringLabeller;
import edu.uci.ics.jung.visualization.PluggableRenderer;
import edu.uci.ics.jung.visualization.VisualizationViewer;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Assert;
import org.junit.Test;
import psidev.psi.mi.tab.PsimiTabReader;
import psidev.psi.mi.tab.model.BinaryInteraction;
import uk.ac.ebi.intact.psimitab.IntActBinaryInteraction;
import uk.ac.ebi.intact.psimitab.IntActColumnHandler;
import uk.ac.ebi.intact.service.graph.binary.merger.BinaryGraphNetworkMerger;
import uk.ac.ebi.intact.service.graph.binary.merger.InteractionBasedMerger;
import uk.ac.ebi.intact.service.graph.io.GraphIOUtils;
import uk.ac.ebi.intact.service.graph.io.jung.RendererFactory;

import javax.swing.*;
import java.io.File;
import java.io.StringWriter;
import java.io.Writer;
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

    @Test
    public void exportTest() throws Exception {
        File file = TestHelper.getFileByResources( "/test-files/brca2-simple.txt", BinaryGraphNetworkFactoryTest.class);
        //File file = new File(BinaryGraphNetworkFactoryTest.class.getResource("/test-files/brca2-simple.txt").getFile());
        PsimiTabReader reader = new PsimiTabReader(true);

        Collection<BinaryInteraction> binaryInteractions = reader.read(file);
        BinaryGraphNetwork graphNetwork = new BinaryGraphNetworkBuilder().createGraphNetwork(binaryInteractions);

        Writer writer = new StringWriter();

        GraphIOUtils.exportNetworkToGraphML(graphNetwork, writer);

        System.out.println(writer.toString());
    }


    @Test
    public void fusionTest() throws Exception{
        PsimiTabReader reader = new PsimiTabReader(true);
        reader.setBinaryInteractionClass( IntActBinaryInteraction.class );
        reader.setColumnHandler( new IntActColumnHandler() );

        BinaryGraphNetworkBuilder builder = new BinaryGraphNetworkBuilder();

        File brca2 = TestHelper.getFileByResources( "/test-files/brca2_IntActBinaryInteraction.txt", BinaryGraphNetworkFactoryTest.class);
        Collection<BinaryInteraction> binaryInteractions = reader.read(brca2);
        BinaryGraphNetwork graphNetwork1 = builder.createGraphNetwork(binaryInteractions);
        System.out.println( graphNetwork1.toString());

        File fancd2 = TestHelper.getFileByResources( "/test-files/fancd2.txt", BinaryGraphNetworkFactoryTest.class);
        binaryInteractions = reader.read(fancd2);
        BinaryGraphNetwork graphNetwork2 = builder.createGraphNetwork(binaryInteractions);
        System.out.println( graphNetwork2.toString());

        BinaryGraphNetworkMerger merger = new InteractionBasedMerger();
        BinaryGraphNetwork network = merger.mergeGraphNetworks( graphNetwork1, graphNetwork2);
        System.out.println( network.toString());
    }

    public static void main(String[] args) throws Exception {

        PsimiTabReader reader = new PsimiTabReader(true);
        reader.setBinaryInteractionClass( IntActBinaryInteraction.class );
        reader.setColumnHandler( new IntActColumnHandler() );

        BinaryGraphNetworkBuilder builder = new BinaryGraphNetworkBuilder();

        File brca2 = TestHelper.getFileByResources( "/test-files/brca2_IntActBinaryInteraction.txt", BinaryGraphNetworkFactoryTest.class);
        Collection<BinaryInteraction> binaryInteractions = reader.read(brca2);
        BinaryGraphNetwork graphNetwork1 = builder.createGraphNetwork(binaryInteractions);
        System.out.println( graphNetwork1.toString());

        File fancd2 = TestHelper.getFileByResources( "/test-files/fancd2.txt", BinaryGraphNetworkFactoryTest.class);
        binaryInteractions = reader.read(fancd2);
        BinaryGraphNetwork graphNetwork2 = builder.createGraphNetwork(binaryInteractions);
        System.out.println( graphNetwork2.toString());

        BinaryGraphNetworkMerger merger = new InteractionBasedMerger();
        BinaryGraphNetwork network = merger.mergeGraphNetworks( graphNetwork1, graphNetwork2);
        System.out.println( network.toString());

        JFrame jf1 = new JFrame();

        VisualizationViewer vv1 = new VisualizationViewer(
                new edu.uci.ics.jung.visualization.SpringLayout(graphNetwork1),
                RendererFactory.createDefaultRenderer(graphNetwork1));
        jf1.getContentPane().add(vv1);
        jf1.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        jf1.setTitle("graphNetwork1");
        jf1.pack();
        jf1.setVisible(true);

        JFrame jf2 = new JFrame();

        VisualizationViewer vv2 = new VisualizationViewer(
                new edu.uci.ics.jung.visualization.SpringLayout(graphNetwork2),
                RendererFactory.createDefaultRenderer(graphNetwork2));
        jf2.getContentPane().add(vv2);
        jf2.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        jf2.setTitle("graphNetwork2");
        jf2.pack();
        jf2.setVisible(true);


        JFrame jf = new JFrame();

        VisualizationViewer vv = new VisualizationViewer(
                new edu.uci.ics.jung.visualization.SpringLayout(network),
                RendererFactory.createDefaultRenderer(network));
        jf.getContentPane().add(vv);
        jf.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        jf.setTitle("graphNetwork");
        jf.pack();
        jf.setVisible(true);


    }

    public static edu.uci.ics.jung.visualization.Renderer getRenderer(BinaryGraphNetwork graph) {
        PluggableRenderer renderer = new PluggableRenderer();
        renderer.setVertexStringer( StringLabeller.getLabeller(graph));
        return renderer;
    }


}



