package uk.ac.ebi.intact.service.exporter;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import psidev.psi.mi.tab.PsimiTabReader;
import psidev.psi.mi.tab.model.BinaryInteraction;
import psidev.psi.mi.xml.converter.ConverterException;

/**
 * @author Nadin
 *
 */
public class BinaryInteractionConverterTest {

	/**
	 * 
	 * @throws Exception
	 */
	@Before
	public void setUp() throws Exception {
	}

	/**
	 * 
	 * @throws Exception
	 */
	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testConvertWeb2Tab() throws IOException, ConverterException {
		
		BinaryInteractionConverter biConverter = new BinaryInteractionConverter();
		
		File tabFile = new File("src/test/resources/mitabsamples/chen.txt");
		PsimiTabReader tabReader = new PsimiTabReader(true);
		Collection<BinaryInteraction> binaryInteractions = tabReader.read(tabFile);
		
		List<uk.ac.ebi.intact.binarysearch.wsclient.generated.BinaryInteraction> webInteractions = biConverter.convertTab2Web(binaryInteractions);
		
		Collection<BinaryInteraction> tabInteractions = biConverter.convertWeb2Tab(webInteractions);
		
		assertEquals(binaryInteractions, tabInteractions);
		
	}

}
