package uk.ac.ebi.intact.service.graph.binary;

import edu.uci.ics.jung.visualization.*;
import edu.uci.ics.jung.graph.decorators.StringLabeller;
import psidev.psi.mi.tab.PsimiTabReader;
import psidev.psi.mi.tab.model.BinaryInteraction;
import uk.ac.ebi.intact.service.graph.io.jung.RendererFactory;

import javax.swing.*;
import java.io.File;
import java.util.Collection;

public class SimpleGraphDraw {

    public static void main(String[] args) throws Exception {
        File file = TestHelper.getFileByResources("/test-files/brca2.txt", BinaryGraphNetworkFactoryTest.class);
        //File file = new File(BinaryGraphNetworkFactoryTest.class.getResource("/test-files/brca2.txt").getFile());
        PsimiTabReader reader = new PsimiTabReader(true);

        Collection<BinaryInteraction> binaryInteractions = reader.read(file);
        BinaryGraphNetwork graphNetwork = new BinaryGraphNetworkBuilder().createGraphNetwork(binaryInteractions);

        System.out.println(graphNetwork);

        JFrame jf = new JFrame();

        VisualizationViewer vv = new VisualizationViewer(
                new edu.uci.ics.jung.visualization.SpringLayout(graphNetwork),
                RendererFactory.createDefaultRenderer(graphNetwork));
        jf.getContentPane().add(vv);
        jf.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        jf.pack();
        jf.setVisible(true);
    }

    public static edu.uci.ics.jung.visualization.Renderer getRenderer(BinaryGraphNetwork graph) {
        PluggableRenderer renderer = new PluggableRenderer();
        renderer.setVertexStringer(StringLabeller.getLabeller(graph));
        return renderer;
    }



}
