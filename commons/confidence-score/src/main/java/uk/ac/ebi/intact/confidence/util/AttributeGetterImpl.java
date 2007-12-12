/**
 * Copyright 2007 The European Bioinformatics Institute, and others.
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
package uk.ac.ebi.intact.confidence.util;

import org.junit.Assert;
import uk.ac.ebi.intact.bridges.blast.BlastConfig;
import uk.ac.ebi.intact.bridges.blast.BlastService;
import uk.ac.ebi.intact.bridges.blast.BlastServiceException;
import uk.ac.ebi.intact.bridges.blast.EbiWsWUBlast;
import uk.ac.ebi.intact.bridges.blast.model.Sequence;
import uk.ac.ebi.intact.bridges.blast.model.UniprotAc;
import uk.ac.ebi.intact.confidence.ProteinPair;
import uk.ac.ebi.intact.confidence.attribute.AlignmentFileMaker;
import uk.ac.ebi.intact.confidence.attribute.GoFilter;
import uk.ac.ebi.intact.confidence.dataRetriever.uniprot.UniprotDataRetriever;
import uk.ac.ebi.intact.confidence.model.Attribute;
import uk.ac.ebi.intact.confidence.model.Identifier;
import uk.ac.ebi.intact.confidence.model.ProteinSimplified;
import uk.ac.ebi.intact.confidence.utils.CombineToAttribs;
import uk.ac.ebi.intact.confidence.utils.ConversionUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * TODO comment that class header
 *
 * @author Irina Armean (iarmean@ebi.ac.uk)
 * @version $Id$
 * @since TODO specify the maven artifact version
 *        <pre>
 *                             29-Nov-2007
 *                             </pre>
 */
public class AttributeGetterImpl implements AttributeGetter {
    private UniprotDataRetriever uniprotKb;
    private File workDir;

    public AttributeGetterImpl( File workDir ) {
        this.workDir = workDir;
        uniprotKb = new UniprotDataRetriever();
    }

    public List<Attribute> fetchGoAttributes( ProteinPair proteinPair ) {
        Set<Identifier> gosA = uniprotKb.getGos( new UniprotAc( proteinPair.getFirstId() ) );
        Set<Identifier> gosB = uniprotKb.getGos( new UniprotAc( proteinPair.getSecondId() ) );
        GoFilter.filterForbiddenGos( gosA );
        GoFilter.filterForbiddenGos( gosB );
        List<Attribute> attribs = CombineToAttribs.combine( gosA, gosB);
        //combine( gosA, gosB );
        return attribs;
    }

//    protected List<Attribute> combine( Set<Identifier> idsA, Set<Identifier> idsB ) {
//        List<Attribute> attributes = new ArrayList<Attribute>();
//        for ( Iterator<Identifier> idItA = idsA.iterator(); idItA.hasNext(); ) {
//            Identifier idA = idItA.next();
//            for ( Iterator<Identifier> idItB = idsB.iterator(); idItB.hasNext(); ) {
//                Identifier idB = idItB.next();
//                if ( !idA.equals( idB ) ) {
//                    Attribute attr = properAttribute( idA, idB ); //new GoPairAttribute( new GoTermPair( goIdA.getId(), goIdB.getId() ) );
//                    // for the reverse part the GoTermPair comes in action, because it sorts the names
//                    if ( !attributes.contains( attr ) ) {
//                        attributes.add( attr );
//                    }
//                }
//            }
//        }
//        return attributes;
//    }
//
//    private Attribute properAttribute( Identifier idA, Identifier idB ) {
//        if ( idA instanceof GoIdentifierImpl && idB instanceof GoIdentifierImpl ) {
//            return new IdentifierAttributeImpl<GoIdentifierImpl>( new GoIdentifierImpl(idA.getId()), new GoIdentifierImpl(idB.getId() ) );
//        } else if ( idA instanceof InterProIdentifierImpl && idB instanceof InterProIdentifierImpl ) {
//            return new IdentifierAttributeImpl<InterProIdentifierImpl>( new InterProIdentifierImpl( idA.getId()), new InterProIdentifierImpl(idB.getId() ) );
//        }
//        return null;
//    }

    public List<Attribute> fetchIpAttributes( ProteinPair proteinPair ) {
        Set<Identifier> ipsA = uniprotKb.getIps( new UniprotAc( proteinPair.getFirstId() ) );
        Set<Identifier> ipsB = uniprotKb.getIps( new UniprotAc( proteinPair.getSecondId() ) );
        List<Attribute> attribs = CombineToAttribs.combine( ipsA, ipsB);
                //combine( ipsA, ipsB );
        return attribs;
    }

    public List<Attribute> fetchAlignAttributes( ProteinPair proteinPair, Set<UniprotAc> againstProt, BlastConfig config ) {
        Set<ProteinSimplified> prots = fetchSeq( proteinPair );
        try {
            BlastService bs = new EbiWsWUBlast( config.getDatabaseDir(), config.getTableName(), config.getBlastArchiveDir(), config.getEmail(), config.getNrPerSubmission() );
            AlignmentFileMaker alignmentMaker = new AlignmentFileMaker( new Float( 0.001 ), workDir, bs );
            Set<ProteinSimplified> results = alignmentMaker.blast( prots, againstProt );
            if ( results != null ) {
                ProteinSimplified[] protsArray = results.toArray( new ProteinSimplified[results.size()] );
                Assert.assertEquals( 2, protsArray.length);
                Set<Identifier> idsA = new HashSet<Identifier>( ConversionUtils.convert2Id( protsArray[0].getAlignments() ) );
                Set<Identifier> idsB = new HashSet<Identifier>( ConversionUtils.convert2Id( protsArray[1].getAlignments() ) );
                List<Attribute> attribs = CombineToAttribs.combine( idsA, idsB);
                // combine(idaA, idsB)
                return attribs;
            }

            return new ArrayList<Attribute>(0);
        } catch ( BlastServiceException e ) {
            e.printStackTrace();
        }
        return new ArrayList<Attribute>(0);
    }

    private Set<ProteinSimplified> fetchSeq( ProteinPair proteinPair ) {
        UniprotAc acA = new UniprotAc( proteinPair.getFirstId() );
        Sequence seqA = uniprotKb.getSeq( acA );
        UniprotAc acB = new UniprotAc( proteinPair.getSecondId() );
        Sequence seqB = uniprotKb.getSeq( acB );
        ProteinSimplified protA = new ProteinSimplified( acA, seqA );
        ProteinSimplified protB = new ProteinSimplified( acB, seqB );
        Set<ProteinSimplified> prots = new HashSet<ProteinSimplified>( 2 );
        prots.add( protA );
        prots.add( protB );
        return prots;
    }

    public List<Attribute> fetchAllAttributes( ProteinPair proteinPair, Set<UniprotAc> againstProt, BlastConfig config ) {
        List<Attribute> all = new ArrayList<Attribute>();
        all.addAll( fetchIpAttributes( proteinPair ) );
        all.addAll( fetchGoAttributes( proteinPair ) );
        all.addAll( fetchAlignAttributes( proteinPair, againstProt, config ) );
        return all;
    }
}
