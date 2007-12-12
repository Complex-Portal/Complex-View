/*
 * Copyright (c) 2002 The European Bioinformatics Institute, and others. All
 * rights reserved. Please see the file LICENSE in the root directory of this
 * distribution.
 */
package uk.ac.ebi.intact.confidence.dataRetriever;

import org.junit.*;
import uk.ac.ebi.intact.confidence.ProteinPair;
import uk.ac.ebi.intact.confidence.expansion.SpokeExpansion;
import uk.ac.ebi.intact.confidence.global.GlobalTestData;
import uk.ac.ebi.intact.confidence.model.InteractionSimplified;
import uk.ac.ebi.intact.confidence.model.ProteinSimplified;
import uk.ac.ebi.intact.confidence.util.DataMethods;
import uk.ac.ebi.intact.context.IntactContext;
import uk.ac.ebi.intact.core.unit.IntactBasicTestCase;
import uk.ac.ebi.intact.core.unit.mock.MockIntactContext;
import uk.ac.ebi.intact.core.unit.mock.MockInteractionDao;
import uk.ac.ebi.intact.model.InteractionImpl;

import java.io.*;
import java.util.*;
import java.util.regex.Pattern;

/**
 * Tests the IntActDb class.
 * 
 * @author Irina Armean (iarmean@ebi.ac.uk)
 * @version
 * @since
 * 
 * <pre>
 * 17 Aug 2007
 * </pre>
 */
public class IntactDbRetrieverTest extends IntactBasicTestCase {

	private Set<String>		acs;
	private IntactDbRetriever	intactdb;
	private File				testDir;

	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
		acs = new HashSet<String>(Arrays.asList("EBI-987097", "EBI-446104", "EBI-79835", "EBI-297231", "EBI-1034130"));
		testDir = GlobalTestData.getInstance().getTargetDirectory();
		intactdb = new IntactDbRetriever(testDir, new SpokeExpansion());
	}

	/**
	 * @throws java.lang.Exception
	 */
	@After
	public void tearDown() throws Exception {
	}

	/**
	 * 
	 */
	@Test
	public final void testRead() {
		List<InteractionSimplified> interactions = intactdb.read(acs);
		Assert.assertEquals(acs.size(), interactions.size());
        for (int i = 0; i <  interactions.size() ; i++){
            Assert.assertTrue(acs.contains( interactions.get( i).getAc()));
        }
	}

	@Test
	public final void testReadConfidenceSets() throws DataRetrieverException {
		File file = new File(testDir.getPath(), "medconf_test.txt");
		try {

			FileWriter fw = new FileWriter(file);
			long start = System.currentTimeMillis();
			intactdb.setDbNrForTest(true);
			intactdb.retrieveMediumConfidenceSet(fw);
			fw.close();
			long end = System.currentTimeMillis();
			long time = end - start;
			System.out.println("time for intact retrieve in milis: " + time);

            checkOutput(file);

            FileWriter fw2 = new FileWriter(new File(testDir.getPath(), "time.txt"));
			fw2.write("time in milis: " + time);
			fw2.close();
		} catch (FileNotFoundException e1) {
			throw new DataRetrieverException( e1);
		} catch (IOException e) {
			throw new DataRetrieverException( e);
		}

		DataMethods dm = new DataMethods();
		List<ProteinPair> interactionsMC = dm.readImportProteinPairs(file);
		try {
			dm.export(interactionsMC, new FileWriter(new File(testDir.getPath(), "mc_test.txt")));
		} catch (IOException e) {
			throw new DataRetrieverException( e);
		}
		
	}

    private void checkOutput( File file ) {
        try {
            String regex = ".*;.*";
            BufferedReader br = new BufferedReader(new FileReader(file));
            String line = "";
            while((line =br.readLine()) != null ){
                Assert.assertTrue(Pattern.matches(regex, line));
            }
        } catch ( FileNotFoundException e ) {
            e.printStackTrace();
        } catch ( IOException e ) {
            e.printStackTrace();
        }

    }

    @Test
	@Ignore
	public final void testReadMediumConf() throws DataRetrieverException {
		MockIntactContext.initMockContext();
		MockIntactContext.configureMockDaoFactory().setMockInteractionDao(new ConfMockInteractionDao());
		// TODO: set up a mock CvContext
		// MockIntactContext.getCurrentInstance().setCurrentInstance(new
		// ConfMockIntactContext());

		testReadConfidenceSets();

		IntactContext.getCurrentInstance().close();
	}

	private class ConfMockInteractionDao extends MockInteractionDao {

		@Override
		public int countAll() {
			return 60;
		}

		@Override
		public List<InteractionImpl> getAll(int firstResult, int maxResults) {
			List<InteractionImpl> interactions = new ArrayList<InteractionImpl>();

			if (firstResult == 0) {
				for (int i = 0; i < maxResults; i++) {
					interactions.add((InteractionImpl) getMockBuilder().createInteractionRandomBinary());
				}
			}

			return interactions;
		}

	}

	// @SuppressWarnings("serial")
	// private class ConfMockIntactContext extends MockIntactContext{
	// @Override
	// public CvContext getCvContext() {
	// return super.getCvContext();
	// }
	// //TODO: ask bruno to show me how to mock a CvContext
	// }
	//	
	// private class ConfMockCvObjectDao extends MockBaseDao<T> {
	//
	// @Override
	// public CvContext getCvContext() {
	// // from MockIntactContext
	// // TODO Auto-generated method stub
	// return super.getCvContext();
	// }
	//
	// @Override
	// public List<CvObject> getByPsiMiRefCollection(Collection<String> psiMis)
	// {
	// // TODO Auto-generated method stub
	// return super.getByPsiMiRefCollection(psiMis);
	// }
	//
	// @Override
	// public CvContext getByMiRef() {
	// MockCvObjectDao<CvObject> mockCv = new MockCvObjectDao<CvObject>();
	// // mockCv.getByPsiMiRef(psiMiRef)
	//
	// return super.getCvContext();
	// }
	// //getByLabel

	private void printProtein(InteractionSimplified interaction) {
		for (ProteinSimplified item : interaction.getInteractors()) {
			System.out.print(item.getUniprotAc() + "; ");
		}
		System.out.println();
	}
}
