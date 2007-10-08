package uk.ac.ebi.intact.confidence.util;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import uk.ac.ebi.intact.confidence.expansion.SpokeExpansion;
import uk.ac.ebi.intact.confidence.global.GlobalTestData;
import uk.ac.ebi.intact.confidence.model.InteractionSimplified;
import uk.ac.ebi.intact.confidence.model.ProteinSimplified;
import uk.ac.ebi.intact.confidence.util.DataMethods;
import uk.ac.ebi.intact.core.unit.IntactBasicTestCase;

public class DataMethodTest extends IntactBasicTestCase {

	private String						filepath;
	private DataMethods					d;
	private List<InteractionSimplified>	binaryIntS;
	private File						testDir;

	@Before
	public void setUp() throws Exception {
		filepath = DataMethodTest.class.getResource("xC.txt").getPath();
		d = new DataMethods();
		binaryIntS = GlobalTestData.getInstance().getBinaryInteractions();
		testDir = new File(GlobalTestData.getInstance().getTargetDirectory().getPath(),"DataMethodsTest");
		testDir.mkdir();
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	//TODO: remove, under unix it did not work :((
	public void testExactRead() {
		List<String> interacts;
		List<String> ACs = Arrays.asList("EBI-987097", "EBI-446104", "EBI-79835", "EBI-297231", "EBI-1034130");
		try {
			interacts = d.readExact(filepath, true);
			Assert.assertEquals(ACs.size(), interacts.size());
			for (String ac : ACs) {
				Assert.assertTrue(interacts.contains(ac));
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Test	
	public void testRead() {
		List<InteractionSimplified> interactions;
		List<String> ACs = Arrays.asList("EBI-987097", "EBI-446104", "EBI-79835", "EBI-297231", "EBI-1034130");
		try {
			System.out.println(filepath);
			String tmpDirPath = GlobalTestData.getInstance().getTargetDirectory().getPath() + "/DataMethodTest/";
			interactions = d.read(filepath, tmpDirPath);
			Assert.assertEquals(ACs.size(), interactions.size());
			for (InteractionSimplified interaction : interactions) {
				Assert.assertTrue(ACs.contains(interaction.getAc()));
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Test
	public void testExpand() {
		// TODO: replace the sysouts with log entries and assertions
		List<InteractionSimplified> interactions;
		List<String> ACs = Arrays.asList("EBI-446104", "EBI-79835", "EBI-297231", "EBI-1034130", "EBI-987097");
		try {
			String tmpDirPath = GlobalTestData.getInstance().getTargetDirectory().getPath() + "/DataMethodTest/";
			interactions = d.read(filepath, tmpDirPath);
			int i = 0;
			for (InteractionSimplified interaction : interactions) {
				assert (ACs.contains(interaction.getAc()));
				i++;
			}

			List<InteractionSimplified> expandedInteractions = d.expand(interactions, new SpokeExpansion());
			printout(expandedInteractions);

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void printout(List<InteractionSimplified> expandedInteractions) {
		for (InteractionSimplified item : expandedInteractions) {
			System.out.print(item.getAc() + ": ");
			printProtein(item);
		}
	}

	private void printProtein(InteractionSimplified interaction) {
		for (ProteinSimplified item : interaction.getInteractors()) {
			System.out.print(item.getUniprotAc() + "; ");
		}
		System.out.println();
	}

	@Test
	public void testGenerate() {
		File inFile = new File(DataMethodTest.class.getResource("yeastSmall.fasta").getFile());
		HashSet<String> yeastProteins = d.readFasta(inFile, null);
		List<InteractionSimplified> generatedInteractions = d.generateLcInteractions(yeastProteins, binaryIntS,
				binaryIntS, binaryIntS, 2);
		Assert.assertEquals(2, generatedInteractions.size());
		printout(generatedInteractions);
	}

	@Test
	public void testExportToFile() {
		// it only works if the file exists
		// File file = new
		// File(DataMethodTest.class.getResource("testConf.txt").getFile());
		File file = new File(testDir.getPath(),"testExportToFile.txt");
		d.export(binaryIntS, file, false);
		List<InteractionSimplified> acs = d.readImport(file);
		Assert.assertEquals(acs.size(), binaryIntS.size());
		for (int i = 0; i < acs.size(); i++) {
			String ebiAcExpected = ((InteractionSimplified) (binaryIntS.toArray()[i])).getAc();
			String ebiAcObserverd = ((InteractionSimplified) (acs.toArray()[i])).getAc();
			Assert.assertEquals(ebiAcExpected, ebiAcObserverd);
		}
	}

	@Test
	public void testExportToOutputStreamWriter() {
		// this did not work because the file should be already present, which
		// he was not
		// File file = new
		// File(DataMethodTest.class.getResource("testExportOutputStreamWriter.txt").getFile());
		File file = new File(testDir.getPath(),"testExportToOutputStreamWriter.txt");
		d.export(binaryIntS, file, false);
		List<InteractionSimplified> interactions = d.readImport(file);
		Assert.assertEquals(interactions.size(), binaryIntS.size());
		for (int i = 0; i < interactions.size(); i++) {
			String ebiAcExpected = ((InteractionSimplified) (binaryIntS.toArray()[i])).getAc();
			String ebiAcObserverd = ((InteractionSimplified) (interactions.toArray()[i])).getAc();
			Assert.assertEquals(ebiAcExpected, ebiAcObserverd);
		}
	}

	@Test
	public void testExportOnlyUniprotAcs() {
		// it works only if the file is already created
		// File file = new
		// File(DataMethodTest.class.getResource("testConf.txt").getFile());
		File file = new File(testDir.getPath(),"testExportOnlyUniprotAcs.txt");
		d.export(binaryIntS, file, true);
		try {
			List<String> acs = d.readExact(file.getPath(), false);
			for (int i = 0; i < acs.size(); i++) {
				InteractionSimplified intS = (InteractionSimplified) binaryIntS.toArray()[i];
				Assert.assertTrue(((String) acs.toArray()[i]).contains(((ProteinSimplified) (intS.getInteractors()
						.toArray()[i])).getUniprotAc()));
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Test
	public void testReadImport() {
		String path = DataMethodTest.class.getResource("EbiAcUniprot.txt").getPath();
		File file = new File(path);
		List<InteractionSimplified> interactions = d.readImport(file);
		Assert.assertEquals(7, interactions.size());
	}

	@Test
	public void testReadFasta() {
		String path = DataMethodTest.class.getResource("yeastSmall.fasta").getPath();
		File inFile = new File(path);
		File outFile = new File(testDir.getPath(), "yeastProteins.txt");
		HashSet<String> acs = d.readFasta(inFile, outFile);
		Assert.assertEquals(3, acs.size());
	}

	@Test
	public void testCheckFormatEBI() {
		HashSet<String> ebiACs = new HashSet(Arrays.asList("EBI-blabla", "EBI-446104", "EBI-79835", "EBI-297231",
				"EBI-1034130"));
		int ebiACsSize = ebiACs.size();
		ebiACs = d.checkFormat(ebiACs, true);
		Assert.assertEquals(ebiACsSize - 1, ebiACs.size());
	}

	@Test
	public void testCheckFormatUniprot() {
		HashSet<String> uniprotACs = new HashSet(Arrays.asList("P12345", "PQ1234", "Q123456", "EBI297", "EBI-1034130"));
		uniprotACs = d.checkFormat(uniprotACs, false);
		// TODO: check if the regex is corect for uniprot
		Assert.assertEquals(1, uniprotACs.size());
	}

	@Test
	public void testGetProteins() {
		List<String> proteins = d.getProteins(this.binaryIntS);
		List<String> expected = GlobalTestData.getInstance().getBinaryProteins();
		Assert.assertEquals(expected.size(), proteins.size());
		int i = 0;
		for (String string : expected) {
			Assert.assertTrue(string.equals(((String) proteins.toArray()[i])));
			i++;
		}
	}
}
