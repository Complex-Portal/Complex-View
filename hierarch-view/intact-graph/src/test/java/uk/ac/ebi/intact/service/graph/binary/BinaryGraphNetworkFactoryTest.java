package uk.ac.ebi.intact.service.graph.binary;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Test;
import psidev.psi.mi.tab.PsimiTabReader;
import psidev.psi.mi.tab.model.BinaryInteraction;

import java.io.File;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Collection;

import uk.ac.ebi.intact.service.graph.io.GraphIOUtils;


public class BinaryGraphNetworkFactoryTest {

    public static final Log log = LogFactory.getLog(BinaryGraphNetworkFactoryTest.class);

    @Test
    public void createTest() throws Exception {
        File file = new File(BinaryGraphNetworkFactoryTest.class.getResource("/test-files/brca2-simple.txt").getFile());
        PsimiTabReader reader = new PsimiTabReader(true);

        Collection<BinaryInteraction> binaryInteractions = reader.read(file);
        BinaryGraphNetwork graphNetwork = new BinaryGraphNetworkBuilder().createGraphNetwork(binaryInteractions);
    }

    @Test
    public void exportTest() throws Exception {
        File file = new File(BinaryGraphNetworkFactoryTest.class.getResource("/test-files/brca2-simple.txt").getFile());
        PsimiTabReader reader = new PsimiTabReader(true);

        Collection<BinaryInteraction> binaryInteractions = reader.read(file);
        BinaryGraphNetwork graphNetwork = new BinaryGraphNetworkBuilder().createGraphNetwork(binaryInteractions);

        Writer writer = new StringWriter();

        GraphIOUtils.exportNetworkToGraphML(graphNetwork, writer);

        System.out.println(writer.toString());
    }


}



