package uk.ac.ebi.intact.service.graph.binary;

import edu.uci.ics.jung.visualization.VisualizationViewer;
import psidev.psi.mi.tab.PsimiTabReader;
import psidev.psi.mi.tab.model.BinaryInteraction;
import uk.ac.ebi.intact.service.graph.binary.BinaryRenderer;

import javax.swing.*;
import java.io.File;
import java.util.Collection;

public class SimpleGraphDraw {

    public static void main(String[] args) throws Exception {
        File file = new File(BinaryGraphNetworkFactoryTest.class.getResource("/test-files/brca2-simple.txt").getFile());
        PsimiTabReader reader = new PsimiTabReader(true);

        Collection<BinaryInteraction> binaryInteractions = reader.read(file);
        BinaryGraphNetwork graphNetwork = new BinaryGraphNetworkBuilder().createGraphNetwork(binaryInteractions);

        System.out.println(graphNetwork);


        JFrame jf = new JFrame();

        VisualizationViewer vv = new VisualizationViewer(
                new edu.uci.ics.jung.visualization.SpringLayout(graphNetwork),
                new BinaryRenderer(graphNetwork));
        jf.getContentPane().add(vv);
        jf.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        jf.pack();
        jf.setVisible(true);
    }



}
