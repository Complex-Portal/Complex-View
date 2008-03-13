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
package uk.ac.ebi.intact.confidence.dataRetriever;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import uk.ac.ebi.intact.bridges.blast.model.Sequence;
import uk.ac.ebi.intact.bridges.blast.model.UniprotAc;
import uk.ac.ebi.intact.business.IntactTransactionException;
import uk.ac.ebi.intact.confidence.model.GoIdentifierImpl;
import uk.ac.ebi.intact.confidence.model.Identifier;
import uk.ac.ebi.intact.confidence.model.InterProIdentifierImpl;
import uk.ac.ebi.intact.core.persister.PersisterHelper;
import uk.ac.ebi.intact.core.unit.IntactBasicTestCase;
import uk.ac.ebi.intact.model.*;
import uk.ac.ebi.intact.persistence.dao.CvObjectDao;

import java.util.Set;

/**
 * Test class for the IntAct annotation retriever.
 *
 * @author Irina Armean (iarmean@ebi.ac.uk)
 * @version $Id$
 * @since 1.6.0
 */
public class IntactAnnotationRetrieverImplTest extends IntactBasicTestCase {

    @Before
    public void init() throws IntactTransactionException {
        Protein p1 = getMockBuilder().createProtein( "P12345", "prot1" );
        Protein p2 = getMockBuilder().createProtein( "P12346", "prot2" );

        Experiment expr = getMockBuilder().createDeterministicExperiment();
        Interaction interaction1 = getMockBuilder().createInteraction( "interaction1", p1, p2, expr );

        CvDatabase goDb = getMockBuilder().createCvObject( CvDatabase.class, CvDatabase.GO_MI_REF, CvDatabase.GO );
        CvDatabase ipDb = getMockBuilder().createCvObject( CvDatabase.class, CvDatabase.INTERPRO_MI_REF, CvDatabase.INTERPRO );
        CvXrefQualifier qualifier = getMockBuilder().getIdentityQualifier();

        //add gos
        p1.addXref( getMockBuilder().createXref( p1, "GO:0005759", qualifier, goDb ) );
        p1.addXref( getMockBuilder().createXref( p1, "GO:0051082", qualifier, goDb ) );
        p1.addXref( getMockBuilder().createXref( p1, "GO:0000050", qualifier, goDb ) );
        p1.addXref( getMockBuilder().createXref( p1, "GO:0006457", qualifier, goDb ) );

        //add ips
        p1.addXref( getMockBuilder().createXref( p1,"IPR004838", qualifier, ipDb ));
        p1.addXref( getMockBuilder().createXref( p1,"IPR000796", qualifier, ipDb ));

        //add Seq
        p1.setSequence( "TestSequence" );

        PersisterHelper.saveOrUpdate( goDb, ipDb );
        PersisterHelper.saveOrUpdate( interaction1 );
        CvObjectDao<CvDatabase> cvDatabase = getDaoFactory().getCvObjectDao( CvDatabase.class );
        CvDatabase cvGo = cvDatabase.getByPsiMiRef( CvDatabase.GO_MI_REF );
        Assert.assertNotNull( cvGo );
    }


    @Test
    public void getGOs() throws Exception {
        AnnotationRetrieverStrategy ars = new IntactAnnotationRetrieverImpl();
        Set<Identifier> gos = ars.getGOs( new UniprotAc( "P12345" ) );
        Assert.assertNotNull( gos );
        Assert.assertEquals( 4, gos.size() );
        Assert.assertTrue( gos.contains( new GoIdentifierImpl( "GO:0005759" ) ) );
        Assert.assertTrue( gos.contains( new GoIdentifierImpl( "GO:0051082" ) ) );
        Assert.assertTrue( gos.contains( new GoIdentifierImpl( "GO:0000050" ) ) );
        Assert.assertTrue( gos.contains( new GoIdentifierImpl( "GO:0006457" ) ) );
    }

    @Test
    public void getIps() throws Exception {
        AnnotationRetrieverStrategy ars = new IntactAnnotationRetrieverImpl();
        Set<Identifier> ips = ars.getIps( new UniprotAc( "P12345" ) );
        Assert.assertNotNull( ips );
        Assert.assertEquals( 2, ips.size() );
        Assert.assertTrue( ips.contains( new InterProIdentifierImpl( "IPR004838" ) ) );
        Assert.assertTrue( ips.contains( new InterProIdentifierImpl( "IPR000796" ) ) );
    }

    @Test
    public void getSeq() throws Exception {
       AnnotationRetrieverStrategy ars = new IntactAnnotationRetrieverImpl();
       Sequence seq = ars.getSeq( new UniprotAc( "P12345" ) );
       Assert.assertNotNull( seq );
       Assert.assertTrue( seq.getSeq().equalsIgnoreCase( "testSequence" ));
    }


//    private void init(){
//        File pgConfigFile = new File(IntactAnnotationRetrieverImplTest.class.getResource("/hibernate.iweb2.cfg.xml").getFile());
//        IntactContext.initStandaloneContext(pgConfigFile);
//    }

}
