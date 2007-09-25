package uk.ac.ebi.intact.service.graph.binary;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Collection;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jdom.Document;
import org.jdom.output.XMLOutputter;
import org.junit.Test;

import psidev.psi.mi.tab.PsimiTabReader;
import psidev.psi.mi.tab.model.BinaryInteraction;
import psidev.psi.mi.xml.converter.ConverterException;


public class BinaryGraphNetworkFactoryTest {
	
	public static final Log log = LogFactory.getLog(BinaryGraphNetworkFactoryTest.class);

	@Test
	public void createTest() throws Exception {
				       
		PsimiTabReader reader = new PsimiTabReader(true);

		File file = new File("src/test/java/uk/ac/ebi/intact/service/graph/binary/brca2.txt");
		
		if (file.canRead()) {
			Collection<BinaryInteraction> binaryInteractions = reader.read(file);
			BinaryGraphNetwork graphNetwork = BinaryGraphNetworkFactory.createBinaryGraphNetwork(binaryInteractions);
		} else {
		 System.err.println("File couldn't read by PsimiTabReader!");
		}	
	}
	
	@Test
	public void exportTest() throws GraphException, IOException, ConverterException{
		
		PsimiTabReader reader = new PsimiTabReader(true);

		File file = new File("src/test/java/uk/ac/ebi/intact/service/graph/binary/brca2.txt");
		
		if (file.canRead()) {
			Collection<BinaryInteraction> binaryInteractions = reader.read(file);
			BinaryGraphNetwork graphNetwork = BinaryGraphNetworkFactory.createBinaryGraphNetwork(binaryInteractions);
			Document doc = BinaryGraphNetworkFactory.export(graphNetwork);
			
			try {
				XMLOutputter outputter = new XMLOutputter(" ",true);
				FileOutputStream output = new FileOutputStream("src/test/java/uk/ac/ebi/intact/service/graph/binary/graphml.xml");
				outputter.output(doc,output);
				
			} catch (Exception e) {
				  log.warn("Could not write Document to File");
			}
			
		} else {
		 log.warn("File couldn't read by PsimiTabReader!");
		}	
		
		
	}
	
		
}



