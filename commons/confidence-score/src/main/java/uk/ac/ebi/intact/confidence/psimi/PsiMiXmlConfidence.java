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
package uk.ac.ebi.intact.confidence.psimi;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import psidev.psi.mi.xml.PsimiXmlReader;
import psidev.psi.mi.xml.PsimiXmlWriter;
import psidev.psi.mi.xml.converter.ConverterException;
import psidev.psi.mi.xml.model.*;
import uk.ac.ebi.intact.bridges.blast.BlastConfig;
import uk.ac.ebi.intact.bridges.blast.model.UniprotAc;
import uk.ac.ebi.intact.confidence.ProteinPair;
import uk.ac.ebi.intact.confidence.maxent.OpenNLPMaxEntClassifier;
import uk.ac.ebi.intact.confidence.model.Attribute;
import uk.ac.ebi.intact.confidence.model.ConfidenceType;
import uk.ac.ebi.intact.confidence.util.AttributeGetter;
import uk.ac.ebi.intact.confidence.util.AttributeGetterImpl;
import uk.ac.ebi.intact.confidence.utils.ParserUtils;
import uk.ac.ebi.intact.model.CvConfidenceType;
import uk.ac.ebi.intact.model.Institution;

import javax.xml.bind.JAXBException;
import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * TODO comment that class header
 *
 * @author Irina Armean (iarmean@ebi.ac.uk)
 * @version $Id$
 * @since TODO specify the maven artifact version
 *        <pre>
 *                             03-Dec-2007
 *                             </pre>
 */
public class PsiMiXmlConfidence {
    /**
     * Sets up a logger for that class.
     */
    public static final Log log = LogFactory.getLog( PsiMiXmlConfidence.class );

    private File workDir;

    private OpenNLPMaxEntClassifier classifier;
    private BlastConfig blastConfig;
    private Set<UniprotAc> againstProteins;

    public PsiMiXmlConfidence( File gisModel, BlastConfig config, Set<UniprotAc> againstProteins, File workDir ) throws IOException {
        this.blastConfig = config;
        this.againstProteins = againstProteins;
        classifier = new OpenNLPMaxEntClassifier( gisModel );
        this.workDir = workDir;
    }


    public PsiMiXmlConfidence( String hcSetPath, String lcSetPath, File workDir, BlastConfig config ) throws IOException {
        this.workDir = workDir;
        this.blastConfig = config;
        this.classifier = new OpenNLPMaxEntClassifier( hcSetPath, lcSetPath, workDir );
        this.againstProteins = ParserUtils.parseProteins( new File( hcSetPath ) );
    }

    public void appendConfidence( File inPsiMiXmlFile, File outPsiMiFile, Set<ConfidenceType> type ) {
        PsimiXmlReader reader = new PsimiXmlReader();
        try {
            EntrySet entry = reader.read( inPsiMiXmlFile );
            saveScores( entry.getEntries(), type );
            writeScores( entry, outPsiMiFile );
        } catch ( IOException e ) {
            e.printStackTrace();
        } catch ( JAXBException e ) {
            e.printStackTrace();
        } catch ( ConverterException e ) {
            e.printStackTrace();
        }
    }

    private void saveScores( Collection<Entry> entries, Set<ConfidenceType> type ) {
        for ( Iterator<Entry> iterator = entries.iterator(); iterator.hasNext(); ) {
            Entry entry = iterator.next();
            Collection<Interaction> interactions = entry.getInteractions();
            for ( Iterator<Interaction> interactionIterator = interactions.iterator(); interactionIterator.hasNext(); )
            {
                Interaction interaction = interactionIterator.next();
                Collection<Participant> participants = interaction.getParticipants();
                if ( participants.size() != 2 ) {
                    log.warn( "Interaction: " + interaction.getId() + " has more than 2 participants => skipped!" );
                } else {
                    ProteinPair pp = retireveProteinPair( participants );
                    if ( pp != null ) {
                        List<Attribute> attribs = getAttributes( pp, type );
                        if ( log.isInfoEnabled() ) {
                            log.info( "interaction: " + pp.toString() + " attribs: " + attribs );
                        }
                        save( interaction, classifier.evaluate( attribs ) );
                    }
                    else {
                        save(interaction, classifier.evaluate( new ArrayList<Attribute>(0)));
                    }
                }
            }
        }
    }

    private ProteinPair retireveProteinPair( Collection<Participant> participants ) {
        Iterator<Participant> iterPart = participants.iterator();
        Interactor intA = iterPart.next().getInteractor();
        Interactor intB = iterPart.next().getInteractor();
        String uniprotA = retrieveUniprotId( intA );
        String uniprotB = retrieveUniprotId( intB );
        ProteinPair pp = null;
        if ( uniprotA != null && uniprotB != null ) {
            pp = new ProteinPair( uniprotA, uniprotB );
        }
        return pp;
    }

    private String retrieveUniprotId( Interactor interactor ) {
        Xref xref = interactor.getXref();
        DbReference refA = xref.getPrimaryRef();
        if ( refA.getDb().equalsIgnoreCase( "uniprotkb" ) && refA.getRefType().equalsIgnoreCase( "identity" ) ) {
            return refA.getId();
        }
        return null;
    }

//    protected void saveScores( Collection<BinaryInteraction> psiMiInts, ConfidenceType type ) {
//        for ( Iterator<BinaryInteraction> iter = psiMiInts.iterator(); iter.hasNext(); ) {
//            BinaryInteraction interaction = iter.next();
//            List<Attribute> attribs = getAttributes( interaction, type );
//            if (log.isInfoEnabled()){
//                log.info ("interaction: " + interaction.getInteractorA() +";" + interaction.getInteractorB()+" attribs: " + attribs );
//            }
//            save( interaction, classifier.evaluate( attribs ) );
//        }
//    }

    private List<Attribute> getAttributes( ProteinPair prteinPair, Set<ConfidenceType> type ) {
        List<Attribute> attributes = new ArrayList<Attribute>();
        AttributeGetter ag = new AttributeGetterImpl( this.workDir );
        for ( Iterator<ConfidenceType> confTypeIter = type.iterator(); confTypeIter.hasNext(); ) {
            ConfidenceType confidenceType = confTypeIter.next();
            attributes.addAll( getAttributes( ag, prteinPair, confidenceType ) );
        }
        return attributes;
    }

    private List<Attribute> getAttributes( AttributeGetter ag, ProteinPair proteinPair, ConfidenceType type ) {
        switch ( type ) {
            case GO:
                return ag.fetchGoAttributes( proteinPair );
            case InterPRO:
                return ag.fetchIpAttributes( proteinPair );
            case Alignment:
                return ag.fetchAlignAttributes( proteinPair, this.againstProteins, this.blastConfig );
            case ALL:
                return ag.fetchAllAttributes( proteinPair, this.againstProteins, this.blastConfig );
            default:
                return ag.fetchAllAttributes( proteinPair, this.againstProteins, this.blastConfig );
        }
    }

    private void save( Interaction interaction, double[] scores ) {
        Unit u = null;
        try {
            u = Unit.class.newInstance();
            Names names = new Names();
            names.setFullName( "interaction confidence score" );
            names.setShortLabel( "intact conf score" );
            u.setNames( names );
        } catch ( InstantiationException e ) {
            e.printStackTrace();
        } catch ( IllegalAccessException e ) {
            e.printStackTrace();
        }
      
        Confidence conf = new Confidence( u, Double.toString( scores[classifier.getIndex( "high" )] ) );
        Collection<Confidence> confs = interaction.getConfidences();
        confs.add( conf );
    }


    public void writeScores( EntrySet entry, File outPsiMiFile ) {
        PsimiXmlWriter writer = new PsimiXmlWriter();
        try {
            writer.write( entry, outPsiMiFile );
        } catch ( IOException e ) {
            e.printStackTrace();
        } catch ( ConverterException e ) {
            e.printStackTrace();
        } catch ( JAXBException e ) {
            e.printStackTrace();
        }
    }


}
