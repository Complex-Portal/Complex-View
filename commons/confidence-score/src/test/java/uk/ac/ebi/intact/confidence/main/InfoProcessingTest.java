/**
 * Copyright 2008 The European Bioinformatics Institute, and others.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package uk.ac.ebi.intact.confidence.main;

import org.junit.Test;
import org.junit.Ignore;

import java.io.File;
import java.io.Writer;
import java.io.FileWriter;
import java.util.Iterator;
import java.util.Set;
import java.util.HashSet;

import uk.ac.ebi.intact.confidence.model.io.BlastInputReader;
import uk.ac.ebi.intact.confidence.model.io.impl.BlastInputReaderImpl;
import uk.ac.ebi.intact.confidence.model.io.impl.ProteinAnnotationWriterImpl;
import uk.ac.ebi.intact.confidence.model.io.ProteinAnnotationWriter;
import uk.ac.ebi.intact.confidence.model.ProteinAnnotation;
import uk.ac.ebi.intact.confidence.global.GlobalTestData;
import uk.ac.ebi.intact.bridges.blast.model.*;
import uk.ac.ebi.intact.bridges.blast.client.BlastClient;

/**
 * Test class for InfoProcessing.
 * (Step 1a)
 *
 * @author Irina Armean (iarmean@ebi.ac.uk)
 * @version $Id$
 * @since 1.6.0
 *        <pre>
 *        14-Jan-2008
 *        </pre>
 */
public class InfoProcessingTest {

    @Test
    @Ignore
    public void testBlast() throws Exception {
        File seqFile = new File (GlobalTestData.getTargetDirectory(), "InfoGatheringTest/mc_set_seq.txt");
        //new File(GlobalTestData.getTargetDirectory(), "InfoGatheringTest\\test_seq.txt");
        //new File("E:\\tmp\\12-01_data\\hc_set_seq.txt");

        File outFile = new File( "/net/nfs6/vol1/homes/iarmean/tmp//medconf_set_seq_anno.txt");
        if (outFile.exists()){
            outFile.delete();
        }

        BlastInputReader bir = new BlastInputReaderImpl();
        File dbFolder = new File ("/net/nfs6/vol1/homes/iarmean/tmp//blastDb");
        //new File("H:\\tmp\\blastDb");
        File blastArchiveDir = new File ("/net/nfs7/vol22/sp-pro5/20080216_iarmean");
        Float threshold = new Float (0.001);
        InfoProcessing ip = new InfoProcessing(dbFolder, blastArchiveDir, "iarmean@ebi.ac.uk", 25);
        ProteinAnnotationWriter paw = new ProteinAnnotationWriterImpl();

        Set<BlastInput> bis = new HashSet<BlastInput>();
        Iterator<BlastInput> iterator = bir.iterate( seqFile );
        while(iterator.hasNext()){
            BlastInput bi = iterator.next();
            if (!bis.contains( bi)){
                ProteinAnnotation pa =ip.blast(bi, threshold);
                paw.append( pa, outFile );
                bis.add( bi );
            }
        }
        ip.close();
    }

    @Test
    @Ignore
    /**
     * compare the differences of the expectation values between uniprot and intact.
     */
    public void testBlastThreshold() throws Exception {
        BlastClient bc = new BlastClient("iarmean@ebi.ac.uk");
        Job job = bc.blast( new BlastInput(new UniprotAc("A5JYZ4"), new Sequence("METFTGYLKSTCFHQISPYPPSIMSIQVKVHANILILSFIECLRMPMHRQIRYSLKNNTI\n" +
                                                                       "TYFNESDVDWSSAIDYQCPDCLIGKSTKHRHIKGSRLKYQNSYEPFQYLHTDIFGPVHNL\n" +
                                                                       "PKSAPSYFISFTDETTKLRWVYPLHDRREDSILDVFTTILAFIKNQFQASVLVIQMDRGS\n" +
                                                                       "EYTNRTLHKFLEKNGITPCYTTTADSRAHGVAERLNRTLLDDCRTQLQCSGLPNHLWFSA\n" +
                                                                       "IEFSTIVRNSLASPKSKKSARQHAGLAGLDISTLLPFGQPVIVNDHNPNSKIHPRGIPGY\n" +
                                                                       "ALHPSRNSYGYIIYLPSLKKTVDTTNYVILQGKESRLDQFNYDALTFDEDLNRLTASYHS\n" +
                                                                       "FIASNEIQESNDLNIESDHDFQSDIELHPEQPRNVLSKAVSPTDSTPPSTHTEDSKRVSK\n" +
                                                                       "TNIRAPREVDPNISESNILPSKKRSSTPQISNIESTGSGGMHKLNVPLLAPMSQSNTHES\n" +
                                                                       "SHASKSKDFRHSDSYSENETNHTNVPISSTGGTNNKTVPQISDQETEKRIIHRSPSIDAS\n" +
                                                                       "PPENNSSHNIVPIKTPTTVSEQNTEESIIADLPLPDLPPESPTEFPDPFKELPPINSHQT\n" +
                                                                       "NSSLGGIGDSNAYTTINSKKRSLEDNETEIKVSRDTWNTKNMRSLEPPRSKKRIHLIAAV\n" +
                                                                       "KAVKSIKPIRTTLRYDEAITYNKDIKEKEKYIEAYHKEVNQLLKMNTWDTDKYYDRKEID\n" +
                                                                       "PKRVINSMFIFNKKRDGTHKARFVARGDIQHPDTYDTGMQSNTVHHYALMTSLSLALDNN\n" +
                                                                       "YYITQLDISSAYLYADIKEELYIRPPPHLGMNDKLIRLKKSLYGLKQSGANWYETIKSYL\n" +
                                                                       "IKQCGMEEVRGWSCVFKNSQVTICLFVDDMILFSKDLNANKKIITTLKKQYDTKIINLGE\n" +
                                                                       "SDNEIQYDILGLEIKYQRGKYMKLGMEKSLTEKLPKLNVPLNPKGKKLRAPGQPGLYIDQ\n" +
                                                                       "DELEIDEDEYKEKVHEMQKLIGLASYVGYKFRFDLLYYINTLAQHILFPSRQVLDMTYEL\n" +
                                                                       "IQFMWDTRDKQLIWHKNKPTEPDNKLVAISDASYGNQPYYKSQIGNIYLLNGKVIGGKST\n" +
                                                                       "KASLTCTSTTEAEIHAISESVPLLNNLSYLIQELNKKPIIKGLLTDSRSTISIIKSTNEE\n" +
                                                                       "KFRNRFFGTKAMRLRDEVSGNNLYVYYIETKKNIADVMTKPLPIKTFKLLTNKWIH")) );
        System.out.println(job);
          while (!job.getStatus().equals( BlastJobStatus.DONE )){
              bc.checkStatus( job );
          }
        System.out.println(job);

        if (job.getStatus().equals( BlastJobStatus.DONE )){
            BlastOutput bo = bc.getResult( job );
            Writer w = new FileWriter("E:\\A5JKZ4.xml");
            w.append( bo.getResult() );
            w.close();
        }

    }

}
