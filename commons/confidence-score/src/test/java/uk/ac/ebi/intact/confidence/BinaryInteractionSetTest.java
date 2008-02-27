/*
 * Copyright (c) 2002 The European Bioinformatics Institute, and others.
 * All rights reserved. Please see the file LICENSE
 * in the root directory of this distribution.
 */
package uk.ac.ebi.intact.confidence;

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import uk.ac.ebi.intact.bridges.blast.model.UniprotAc;
import uk.ac.ebi.intact.confidence.global.GlobalTestData;
import uk.ac.ebi.intact.confidence.model.BinaryInteraction;
import uk.ac.ebi.intact.confidence.model.io.BinaryInteractionReader;
import uk.ac.ebi.intact.confidence.model.io.impl.BinaryInteractionReaderImpl;
import uk.ac.ebi.intact.confidence.utils.ParserUtils;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * BinaryInteractionSet tests + stats.
 *
 * @author Irina Armean (iarmean@ebi.ac.uk)
 * @version
 * @since <pre>3 Oct 2007</pre>
 */
//@Ignore
public class BinaryInteractionSetTest {


	public static void main(String[] args) throws IOException{
		new BinaryInteractionSetTest().Stat();
//		new BinaryInteractionSetTest().uniqueProts();
	}
	
	@Test
    public void uniqueProts() throws IOException{
		String path = BinaryInteractionSetTest.class.getResource("medconf_test.txt").getPath();
		BinaryInteractionSet biS = new BinaryInteractionSet(path);
			Set<String> proteinsBiS = biS.getAllProtNames();
			Set<UniprotAc> prots = new HashSet<UniprotAc>(proteinsBiS.size());
			for (String ac : proteinsBiS) {
				prots.add(new UniprotAc(ac));
			}

            Assert.assertEquals(24, prots.size());

            File protsFile = new File( GlobalTestData.getInstance().getTargetDirectory(),"proteinsOrderToBlast.txt");
            System.out.println("protsOut: " + protsFile.getPath());
			Writer w = new FileWriter(protsFile);
			for (UniprotAc uniprotAc : prots) {
				w.append(uniprotAc +"\n");
			}
			w.close();
	}

	@Test
    @Ignore
    public void Stat() throws IOException{
		String path ="E:\\iarmean\\backupData\\15.02 - IWEB2 - full filter\\highconf_set.txt";
        BinaryInteractionReader biar = new BinaryInteractionReaderImpl();
        List<BinaryInteraction> bis = biar.read( new File(path) );
        Set<BinaryInteraction> bisSet = new HashSet<BinaryInteraction>(bis);
        Set<UniprotAc> prots = ParserUtils.parseProteins( new File(path) );

        System.out.println( "highconf: lines: "+ bis.size() + " unique: " + bisSet.size() + " prots : " + prots.size() );

        path = "E:\\iarmean\\backupData\\15.02 - IWEB2 - full filter\\medconf_set.txt";
        List<BinaryInteraction> bis2 = biar.read(new File(path));
        Set<BinaryInteraction> bisSet2 = new HashSet<BinaryInteraction>(bis2);
        Set<UniprotAc> prots2 = ParserUtils.parseProteins( new File(path) );

        prots.addAll( prots2 );
        System.out.println( "medconf: lines: "+ bis2.size() + " unique: " + bisSet2.size() + " prots : " + prots2.size()  );

        path = "E:\\iarmean\\backupData\\15.02 - IWEB2 - full filter\\lowconf_set.txt";
        List<BinaryInteraction> bis3 = biar.read(new File(path));
        Set<BinaryInteraction> bisSet3 = new HashSet<BinaryInteraction>(bis3);
        Set<UniprotAc> prots3 = ParserUtils.parseProteins( new File(path) );

        prots.addAll( prots3 );
        System.out.println( "lowconf: lines: "+ bis3.size() + " unique: " + bisSet3.size() + " prots : " + prots3.size()  );
        System.out.println( "total unique prots: " + prots.size() );


    }

    @Test
    @Ignore
    public void stats() throws IOException{
             String path ="E:\\iarmean\\backupData\\benchmark\\from 15.10\\HC100.txt";
        //"E:\\iarmean\\backupData\\benchmark\\HC100-2310.txt";

		BinaryInteractionSet biS = new BinaryInteractionSet(path);
			Set<String> prots = biS.getAllProtNames();
			int nr = biS.getAllProtNames().size();
			System.out.println("HC100 prots : " + nr + " #int: " + biS.size());

			path = "E:\\iarmean\\backupData\\benchmark\\from 15.10\\HC50.txt";
            //"E:\\iarmean\\backupData\\benchmark\\HC050-2310.txt";
			BinaryInteractionSet biS2 = new BinaryInteractionSet(path);
			prots.addAll(biS2.getAllProtNames());
			nr = biS2.getAllProtNames().size();
			System.out.println("HC50 prots : " + nr + " #int: " + biS2.size());

			path = "E:\\iarmean\\backupData\\benchmark\\from 15.10\\HC25.txt";
            //"E:\\iarmean\\backupData\\benchmark\\HC025-2310.txt";
			BinaryInteractionSet biS3 = new BinaryInteractionSet(path);
			prots.addAll(biS3.getAllProtNames());
			nr = biS3.getAllProtNames().size();
			System.out.println("HC25 prots : " + nr + " #int: " + biS3.size());

            path = "E:\\iarmean\\backupData\\benchmark\\from 15.10\\HC12-5.txt";
            //"E:\\iarmean\\backupData\\benchmark\\HC025-2310.txt";
			BinaryInteractionSet biS4 = new BinaryInteractionSet(path);
			prots.addAll(biS4.getAllProtNames());
			nr = biS4.getAllProtNames().size();
			System.out.println("HC12-5 prots : " + nr + " #int: " + biS4.size());

            path = "E:\\iarmean\\backupData\\benchmark\\from 15.10\\HC6-25.txt";
            //"E:\\iarmean\\backupData\\benchmark\\HC025-2310.txt";
			BinaryInteractionSet biS5 = new BinaryInteractionSet(path);
			prots.addAll(biS5.getAllProtNames());
			nr = biS5.getAllProtNames().size();
			System.out.println("HC25 prots : " + nr + " #int: " + biS5.size());

            System.out.println("total unique prots: " + prots.size());
    }
	
}
