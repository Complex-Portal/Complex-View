/*
 * Copyright (c) 2002 The European Bioinformatics Institute, and others.
 * All rights reserved. Please see the file LICENSE
 * in the root directory of this distribution.
 */
package uk.ac.ebi.intact.confidence.dataRetriever.uniprot;

import org.junit.After;
import org.junit.Assert;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.Test;
import uk.ac.ebi.intact.bridges.blast.model.Sequence;
import uk.ac.ebi.intact.bridges.blast.model.UniprotAc;
import uk.ac.ebi.intact.confidence.model.Identifier;
import uk.ac.ebi.intact.confidence.model.InterProIdentifierImpl;
import uk.ac.ebi.intact.confidence.model.ProteinSimplified;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

/**
 * Test- class for UniprotDataRetriever.
 *
 * @author Irina Armean (iarmean@ebi.ac.uk)
 * @version
 * @since <pre>22 Oct 2007</pre>
 */
//@Ignore
public class UniprotDataRetrieverTest {
	private UniprotDataRetriever udr;

	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
		 udr = new UniprotDataRetriever();
	}

	/**
	 * @throws java.lang.Exception
	 */
	@After
	public void tearDown() throws Exception {
	}

	/**
	 * Test method for {@link uk.ac.ebi.intact.confidence.dataRetriever.uniprot.UniprotDataRetriever#getGOs(uk.ac.ebi.intact.bridges.blast.model.UniprotAc)}.
	 */
	@Test
    /**
     * TODO: test in a way i do not connect to uniprot ex. a uniprot mock
     */
    public final void testGetGos() {
		Collection<Identifier> gos = udr.getGOs(new UniprotAc("P12345"));
		assertTrue(gos.size()!= 0);
//		assertTrue(gos.contains(new GoIdentifierImpl("GO:0000050")));
//		assertTrue(gos.contains(new GoIdentifierImpl("GO:0006457")));
//		assertTrue(gos.contains(new GoIdentifierImpl("GO:0005515")));
		
	}

	/**
	 * Test method for {@link uk.ac.ebi.intact.confidence.dataRetriever.uniprot.UniprotDataRetriever#getIps(uk.ac.ebi.intact.bridges.blast.model.UniprotAc)}.
	 */
	@Test
	public final void testGetIps() {
		Collection<Identifier> ips = udr.getIps(new UniprotAc("P12345"));
		assertEquals(2, ips.size());	
		assertTrue(ips.contains(new InterProIdentifierImpl("IPR004838")));
		assertTrue(ips.contains(new InterProIdentifierImpl("IPR000796")));
	}

    @Test
    public void testGetSeq() throws Exception {
        Sequence seq = udr.getSeq(new UniprotAc("P12345") );
        Assert.assertNotNull(seq);
        assertTrue( seq.getSeq().equalsIgnoreCase("SSWWAHVEMGPPDPILGVTEAYKRDTNSKK"));
    }

    @Test
    public void testGetSequences() throws Exception {
        List<ProteinSimplified> proteins = Arrays.asList( new ProteinSimplified(new UniprotAc("P12345")), new ProteinSimplified( new UniprotAc( "O46201")));
        udr.getSequences( proteins);
        for ( ProteinSimplified prot : proteins){
            if (prot.getUniprotAc().getAcNr().equalsIgnoreCase("P12345")){
                assertTrue(prot.getSequence().getSeq().equalsIgnoreCase("SSWWAHVEMGPPDPILGVTEAYKRDTNSKK"));
            }
            if (prot.getUniprotAc().getAcNr().equalsIgnoreCase("O46201"))  {
                    assertTrue(prot.getSequence().getSeq().equalsIgnoreCase("MEFPNPVLSRIGRSLRTNKGTHYQRMTRMSK"));
            }

        }
    }


}
