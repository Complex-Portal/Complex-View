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
package uk.ac.ebi.intact.confidence.model.io;

import org.junit.Test;
import org.junit.Assert;

import java.io.File;

import uk.ac.ebi.intact.bridges.blast.model.BlastInput;
import uk.ac.ebi.intact.bridges.blast.model.UniprotAc;
import uk.ac.ebi.intact.bridges.blast.model.Sequence;
import uk.ac.ebi.intact.confidence.model.io.impl.BlastInputReaderImpl;

import java.util.List;
import java.util.Arrays;
import java.util.Iterator;

/**
 * Test class for BalstInputReader.
 *
 * @author Irina Armean (iarmean@ebi.ac.uk)
 * @version $Id$
 * @since 1.6.0
 *        <pre>
 *        14-Jan-2008
 *        </pre>
 */
public class BlastInputReaderTest {

    @Test
    public void read() throws Exception {
        File inFile = new File (BlastInputReaderTest.class.getResource( "BlastInput.txt" ).getPath());
        BlastInputReader bir = new BlastInputReaderImpl();
        List<BlastInput> observed = bir.read( inFile );
        List<BlastInput> expected = expectedInfo();

        //check observed
        Assert.assertEquals( expected.size(), observed.size() );
        for (int i = 0; i< expected.size(); i++){
            BlastInput expectedBi = expected.get( i );
            BlastInput observedBi = observed.get( i );

            Assert.assertEquals( expectedBi.getUniprotAc(), observedBi.getUniprotAc() );
            Assert.assertEquals( expectedBi.getSequence(), observedBi.getSequence() );
        }
    }

    @Test
    public void iterate() throws Exception {
        File inFile = new File (BlastInputReaderTest.class.getResource( "BlastInput.txt" ).getPath());
        BlastInputReader bir = new BlastInputReaderImpl();
        List<BlastInput> expected = expectedInfo();
        int  nr = 0;
        for (Iterator<BlastInput> iter = bir.iterate( inFile ); iter.hasNext();){
            BlastInput observedBi = iter.next();
            BlastInput expectedBi = expected.get( nr );

            Assert.assertEquals( expectedBi.getUniprotAc(), observedBi.getUniprotAc() );
            Assert.assertEquals( expectedBi.getSequence(), observedBi.getSequence() );
            nr ++;
        }

        Assert.assertEquals( expected.size(), nr );
    }


    private List<BlastInput> expectedInfo() {
        Sequence seq = new Sequence("malvtlqrsptpsaasssasnseleagseedrklnlslsesffmvkgaalflqqgsspqgqrslqhphkhagdlpqhlqvminllrcedriklavrlesawadrvrymvvvyssgrqdteenillgvdfsskesksctigmvlrlwsdtkihldgdggfsvstagrmhifkpvsvqamwsalqvlhkacevarrhnyfpggvaliwatyyescisseqscinewnamqdlestrpdspalfvdkptegerterlikaklrsimmsqdlenvtskeirnelekqmncnlkelkefidnemllilgqmdkpslifdhlylgsewnasnleelqgsgvdyilnvtreidnffpglfayhnirvydeettdllahwneayhfinkakrnhskclvhckmgvsrsastviayamkefgwplekaynyvkqkrsitrpnagfmrqlseyegildaskqrhnklwrqqtdsslqqpvddpagpgdflpetpdgtpesqlpflddaaqpglgpplpccfrrlsdpllpspedetgslvhledperealleeaappaevhrparqpqqgsglcekdvkkklefgspkgrsgsllqveetereeglgagrwgqlptqldqnllnsenlnnnskrscpngmeddaifgilnkvkpsykscadcmyptasgapeasrercedpnapaictqpaflphitsspvahlasrsrvpekpasgptepppflppagsrradtsgpgagaaleppasllepsretpkvlpkslllknshcdknppstevvikeesspkkdmkpakdlrllfsnesekpttnsylmqhqesiiqlqkaglvrkhtkelerlksvpadpappsrdgpasrleasipeesqdpaalhelgplvmpsqagsdekseaapasleggslkspppffyrldhtssfskdflkticytptsssmssnltrssssdsihsvrgkpglvkqrtqeietrlrlagltvssplkrshslaklgsltfstedlsseadpstvadsqdttlsessflhepqgtprdpaatskpsgkpapenlkspswmsks");
        BlastInput bi = new BlastInput(new UniprotAc("Q8WYL5"),seq);

        Sequence seq1 = new Sequence("msfrkvvrqskfrhvfgqpvkndqcyedirvsrvtwdstfcavnpkflaviveasgggaflvlplsktgridkayptvcghtgpvldidwcphndeviasgsedctvmvwqipengltspltepvvvleghtkrvgiiawhptarnvllsagcdnvvliwnvgtaeelyrldslhpdliynvswnhngslfcsackdksvriidprrgtlvaerekahegarpmraifladgkvfttgfsrmserqlalwdpenleepmalqeldssngallpfydpdtsvvyvcgkgdssiryfeiteeppyihflntftskepqrgmgsmpkrglevskceiarfyklherkcepivmtvprksdlfqddlypdtagpeaaleaeewvsgrdadpilislreayvpskqrdlkisrrnvlsdsrpamapgsshlgapastttaadatpsgslarageagkleevmqelralralvkeqgdricrleeqlgrmengda");
        BlastInput bi1 = new BlastInput(new UniprotAc("Q9BR76"), seq1);

        Sequence seq2 = new Sequence("mdqreilqkfldeaqskkitkeefaneflklkrqstkykadktypttvaekpknikknrykdilpydysrvelslitsdedssyinanfikgvygpkayiatqgplsttlldfwrmiweysvliivmacmeyemgkkkcerywaepgemqlefgpfsvsceaekrksdyiirtlkvkfnsetrtiyqfhyknwpdhdvpssidpileliwdvrcyqeddsvpicihcsagcgrtgvicaidytwmllkdgiipenfsvfsliremrtqrpslvqtqeqyelvynavlelfkrqmdvirdkhsgtesqakhcipeknhtlqadsyspnlpksttkaakmmnqqrtkmeikesssfdfrtseisakeelvlhpaksstsfdflelnysfdknadttmkwqtkafpivgeplqkhqsldlgsllfegcsnskpvnaagryfnskvpitrtkstpfeliqqretkevdskenfsylesqphdscfvemqaqkvmhvssaelnyslpydskhqirnasnvkhhdssalgvysyiplvenpyfsswppsgtsskmsldlpekqdgtvfpssllptsstslfsyynshdslslnsptnissllnqesavlatapriddeippplpvrtpesfivveeagefspnvpkslssavkvkigtslewggtsepkkfddsvilrpsksvklrspkselhqdrsspppplpertlesffladedcmqaqsietystsypdtmenstsskqtlktpgksftrskslkilrnmkksicnscppnkpaesvqsnnsssflnfgfanrfskpkgprnppptwni");
        BlastInput bi2 = new BlastInput(new UniprotAc("Q9Y2R2"), seq2);

        return Arrays.asList( bi, bi1, bi2 );
    }

}
