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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import uk.ac.ebi.intact.bridges.blast.BlastConfig;
import uk.ac.ebi.intact.bridges.blast.BlastService;
import uk.ac.ebi.intact.bridges.blast.BlastServiceException;
import uk.ac.ebi.intact.bridges.blast.EbiWsWUBlast;
import uk.ac.ebi.intact.bridges.blast.model.Sequence;
import uk.ac.ebi.intact.bridges.blast.model.UniprotAc;
import uk.ac.ebi.intact.confidence.ProteinPair;
import uk.ac.ebi.intact.confidence.attribute.AlignmentFileMaker;
import uk.ac.ebi.intact.confidence.dataRetriever.AnnotationRetrieverStrategy;
import uk.ac.ebi.intact.confidence.filter.FilterException;
import uk.ac.ebi.intact.confidence.filter.GOAFilter;
import uk.ac.ebi.intact.confidence.model.Attribute;
import uk.ac.ebi.intact.confidence.model.Identifier;
import uk.ac.ebi.intact.confidence.model.ProteinSimplified;
import uk.ac.ebi.intact.confidence.model.UniprotIdentifierImpl;
import uk.ac.ebi.intact.confidence.utils.CombineToAttribs;
import uk.ac.ebi.intact.confidence.utils.ConversionUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * For a given uniprot protein pair,
 * gets the annotations, blasts, filters
 *  and computes the attributes.
 *
 * @author Irina Armean (iarmean@ebi.ac.uk)
 * @version $Id$
 * @since 1.6.0
 *        <pre>
 *                             29-Nov-2007
 *                             </pre>
 */
public class AttributeGetterImpl implements AttributeGetter {
    /**
     * Sets up a logger for that class.
     */
    public static final Log log = LogFactory.getLog( AttributeGetterImpl.class );

    private AnnotationRetrieverStrategy annoDb;
    private File workDir;
    private GOAFilter goaFilter;


    public AttributeGetterImpl( File workDir, AnnotationRetrieverStrategy annoatationRetriever, GOAFilter filter ) {
        this.workDir = workDir;
        annoDb = annoatationRetriever;
        goaFilter = filter;
    }

    /**
     *  
     * @param proteinPair
     * @return
     */
    public List<Attribute> fetchGoAttributes( ProteinPair proteinPair ) throws AttributeGetterException {
        UniprotAc first = new UniprotAc( proteinPair.getFirstId() );
        UniprotAc second = new UniprotAc( proteinPair.getSecondId() );
        Set<Identifier> gosA = annoDb.getGOs( first );
        Set<Identifier> gosB = annoDb.getGOs( second );
        try {
            goaFilter.filterGO( new UniprotIdentifierImpl(first), gosA );
            goaFilter.filterGO( new UniprotIdentifierImpl(second), gosA );
            List<Attribute> attribs = CombineToAttribs.combine( gosA, gosB);
            return attribs;
        } catch ( FilterException e ) {
           throw new AttributeGetterException(e);
        }
    }

    public List<Attribute> fetchIpAttributes( ProteinPair proteinPair ) {
        Set<Identifier> ipsA = annoDb.getIps( new UniprotAc( proteinPair.getFirstId() ) );
        Set<Identifier> ipsB = annoDb.getIps( new UniprotAc( proteinPair.getSecondId() ) );
        List<Attribute> attribs = CombineToAttribs.combine( ipsA, ipsB);
        return attribs;
    }

    public List<Attribute> fetchAlignAttributes( ProteinPair proteinPair, Set<UniprotAc> againstProt, BlastConfig config ) throws BlastServiceException {
        Set<ProteinSimplified> prots = fetchSeq( proteinPair );
        try {
            BlastService bs = new EbiWsWUBlast( config.getDatabaseDir(), config.getTableName(), config.getBlastArchiveDir(), config.getEmail(), config.getNrPerSubmission() );
            AlignmentFileMaker alignmentMaker = new AlignmentFileMaker( new Float( 0.001 ), workDir, bs );
            long start = System.currentTimeMillis();
            Set<ProteinSimplified> results = alignmentMaker.blast( prots, againstProt );
            if (log.isInfoEnabled()){
                long time = System.currentTimeMillis() - start;
                long sec = time /1000;
                log.info("blasting 2 prots ("+ proteinPair.getFirstId() + ", " + proteinPair.getSecondId()+") it took: "+ sec + " sec");
            }


            if ( results != null ) {
                ProteinSimplified[] protsArray = results.toArray( new ProteinSimplified[results.size()] );

                Set<Identifier> idsA = new HashSet<Identifier>( ConversionUtils.convert2Id( protsArray[0].getAlignments() ) );
                Set<Identifier> idsB;
                if (protsArray.length == 2) {
                    idsB = new HashSet<Identifier>( ConversionUtils.convert2Id( protsArray[1].getAlignments() ) );
                } else {
                    idsB = idsA;
                }
                List<Attribute> attribs = CombineToAttribs.combine( idsA, idsB);
                return attribs;
            }

            return new ArrayList<Attribute>(0);
        } catch ( BlastServiceException e ) {
           throw e;
        }
    }

    private Set<ProteinSimplified> fetchSeq( ProteinPair proteinPair ) {
        UniprotAc acA = new UniprotAc( proteinPair.getFirstId() );
        Sequence seqA = annoDb.getSeq( acA );
        UniprotAc acB = new UniprotAc( proteinPair.getSecondId() );
        Sequence seqB = annoDb.getSeq( acB );
        ProteinSimplified protA = new ProteinSimplified( acA, seqA );
        ProteinSimplified protB = new ProteinSimplified( acB, seqB );
        Set<ProteinSimplified> prots = new HashSet<ProteinSimplified>( 2 );
        prots.add( protA );
        prots.add( protB );
        return prots;
    }

    public List<Attribute> fetchAllAttributes( ProteinPair proteinPair, Set<UniprotAc> againstProt, BlastConfig config ) throws AttributeGetterException {
        List<Attribute> all = new ArrayList<Attribute>();
        all.addAll( fetchIpAttributes( proteinPair ) );
        all.addAll( fetchGoAttributes( proteinPair ) );
        try {
            all.addAll( fetchAlignAttributes( proteinPair, againstProt, config ) );
        } catch ( BlastServiceException e ) {
            throw new AttributeGetterException( e);
        }
        return all;
    }
}
