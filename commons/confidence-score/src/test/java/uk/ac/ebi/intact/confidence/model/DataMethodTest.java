package uk.ac.ebi.intact.confidence.model;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import uk.ac.ebi.intact.confidence.expansion.SpokeExpansion;
import uk.ac.ebi.intact.core.unit.IntactBasicTestCase;

public class DataMethodTest extends IntactBasicTestCase{

	private String filepath;
	private DataMethods d;
	private Collection<InteractionSimplified> binaryIntS;
	
	@Before
	public void setUp() throws Exception {
		filepath = DataMethodTest.class.getResource("xC.txt").getPath();  
		d = new DataMethods (new SpokeExpansion());
		
		ProteinSimplified comp1 = new ProteinSimplified("P12345","neutral");
		ProteinSimplified comp2 = new ProteinSimplified("Q12345","neutral");
		ProteinSimplified comp3 = new ProteinSimplified("R12345","neutral");
		ProteinSimplified comp4 = new ProteinSimplified("S12345","neutral");
		InteractionSimplified intS1 = new InteractionSimplified("EBI-1234", Arrays.asList(comp1, comp2));
		InteractionSimplified intS2 = new InteractionSimplified("EBI-1234", Arrays.asList(comp3, comp4));
		binaryIntS = Arrays.asList(intS1, intS2);
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testExactRead() {
		Collection<String> interacts;
		Collection<String> ACs = Arrays.asList("EBI-987097", "EBI-446104", "EBI-79835", "EBI-297231", "EBI-1034130");
		try {
			interacts = d.exactRead(filepath);
			int i =0;
			for (String string : interacts) {
				assert(string.equals(ACs.toArray()[i]));
				i++;
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	@Test
	public void testRead() {
		Collection<InteractionSimplified> interactions;
		Collection<String> ACs = Arrays.asList("EBI-987097", "EBI-446104", "EBI-79835", "EBI-297231", "EBI-1034130");
		try {
			interactions = d.read(filepath);
			int i =0;
			for (InteractionSimplified interaction : interactions) {
				assert(interaction.equals((ACs.toArray()[i])));
				i++;
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	@Test 	
	public void testExpand() {
		//TODO: replace the sysouts with log entries and assertions
		Collection<InteractionSimplified> interactions;
		Collection<String> ACs = Arrays.asList("EBI-446104", "EBI-79835", "EBI-297231", "EBI-1034130", "EBI-987097");
		try {
			interactions = d.read(filepath);
			int i =0;
			for (InteractionSimplified interaction : interactions) {
				assert(interaction.getAc().equals((ACs.toArray()[i])));
				i++;
			}
			
			Collection<InteractionSimplified> expandedInteractions = d.expand(interactions);
			printout(expandedInteractions);
			
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private void printout(Collection<InteractionSimplified> expandedInteractions) {
		for (InteractionSimplified item : expandedInteractions) {
			System.out.print(item.getAc()+": ");
			printProtein(item);
		}		
	}

	private void printProtein(InteractionSimplified interaction) {
		for (ProteinSimplified item : interaction.getInteractors()) {
			System.out.print(item.getUniprotAc()+"; ");
		}
		System.out.println();
	}

	@Test
	public void testGenerate(){
		String path = DataMethodTest.class.getResource("yeastSmall.fasta").getPath();
		File inFile = new File(path);
		Collection<String> yeastProteins = d.readFasta(inFile, null);
		Collection<InteractionSimplified> generatedInteractions = d.generateLCInteractions(yeastProteins, 
				binaryIntS, binaryIntS, binaryIntS, 2);
		Assert.assertEquals(2, generatedInteractions.size());
		printout(generatedInteractions);
	}

	@Test
	public void testExport(){
		File file = new File(DataMethodTest.class.getResource("testConf.txt").getFile());
		d.export(binaryIntS, file, false);
		try {
			Collection<String> acs = d.exactRead(file.getPath());
			for (int i =0; i< acs.size(); i++) {
				Assert.assertTrue(((String)acs.toArray()[i]).startsWith(((InteractionSimplified)(binaryIntS.toArray()[i])).getAc()));
			}
		} catch (IOException e) {
			e.printStackTrace();
		}	
	}
	
	@Test
	public void testExportOnlyUniprotAcs(){
		File file = new File(DataMethodTest.class.getResource("testConf.txt").getFile());
		d.export(binaryIntS, file, true);
		try {	
			Collection<String> acs = d.exactRead(file.getPath());
			for (int i =0; i< acs.size(); i++) {
				InteractionSimplified intS = (InteractionSimplified) binaryIntS.toArray()[i];
				Assert.assertTrue(((String)acs.toArray()[i]).contains(((ProteinSimplified)(intS.getInteractors().toArray()[i])).getUniprotAc()));
			}
		} catch (IOException e) {
			e.printStackTrace();
		}	
	}
	
	@Test
	public void testReadFasta(){
		String path = DataMethodTest.class.getResource("yeastSmall.fasta").getPath();
		File inFile = new File(path);
		File outFile = new File("E:\\iarmean\\dataHC\\yeastProteins.txt");
		Collection<String> acs = d.readFasta(inFile,outFile);
		Assert.assertEquals(3, acs.size());
	}
}
