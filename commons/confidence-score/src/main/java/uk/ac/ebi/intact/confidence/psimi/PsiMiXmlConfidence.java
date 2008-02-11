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
import psidev.psi.mi.xml.PsimiXmlReaderException;
import psidev.psi.mi.xml.PsimiXmlWriter;
import psidev.psi.mi.xml.PsimiXmlWriterException;
import psidev.psi.mi.xml.model.*;
import uk.ac.ebi.intact.bridges.blast.BlastConfig;
import uk.ac.ebi.intact.bridges.blast.model.UniprotAc;
import uk.ac.ebi.intact.confidence.ProteinPair;
import uk.ac.ebi.intact.confidence.dataRetriever.AnnotationRetrieverStrategy;
import uk.ac.ebi.intact.confidence.dataRetriever.IntactAnnotationRetrieverImpl;
import uk.ac.ebi.intact.confidence.filter.GOFilter;
import uk.ac.ebi.intact.confidence.maxent.OpenNLPMaxEntClassifier;
import uk.ac.ebi.intact.confidence.model.Attribute;
import uk.ac.ebi.intact.confidence.model.ConfidenceType;
import uk.ac.ebi.intact.confidence.util.AttributeGetter;
import uk.ac.ebi.intact.confidence.util.AttributeGetterImpl;
import uk.ac.ebi.intact.confidence.utils.ParserUtils;

import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.*;

/**
 * Given the classifier, or the necessary data to train the model, this class assigns confidence values to
 * protein-protein interactions in a PSI-MI XML file.
 *
 * @author Irina Armean (iarmean@ebi.ac.uk)
 * @version $Id$
 * @since 1.6.0
 *        <pre> 03-Dec-2007 </pre>
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
    private DecimalFormat df = new DecimalFormat("0.00");

    private File goaFilterFile;

    private AnnotationRetrieverStrategy annoDb;

    /**
     * Constructor for the PSI-MI XML plugin to add scores to a PSI-MI XML file.
     *
     * @param gisModel : the classifying model persisted to file
     * @param config  : the configuration needed for intact-blast bridge
     * @param againstProteins : the list of proteins contained in the high confidence set
     * @param goaFile : the fail with the GOA annotations fo uniprot, for filtering the IEA tagged annotations
     * @param workDir : working directory for the plugin
     * @throws IOException
     */
    public PsiMiXmlConfidence( File gisModel, BlastConfig config, Set<UniprotAc> againstProteins,File goaFile, File workDir ) throws IOException {
        this.blastConfig = config;
        this.againstProteins = againstProteins;
        classifier = new OpenNLPMaxEntClassifier( gisModel );
        this.workDir = workDir;

        this.goaFilterFile = goaFile;

        annoDb = new IntactAnnotationRetrieverImpl();
    }


    public PsiMiXmlConfidence( String hcSetPath, String lcSetPath, File goaFile, File workDir, BlastConfig config ) throws IOException {
        this.workDir = workDir;
        this.blastConfig = config;
        this.classifier = new OpenNLPMaxEntClassifier( hcSetPath, lcSetPath, workDir );
        this.againstProteins = ParserUtils.parseProteins( new File( hcSetPath ) );

        this.goaFilterFile = goaFile;
    }

    public void appendConfidence( File inPsiMiXmlFile, File outPsiMiFile, Set<ConfidenceType> type ) throws PsiMiException {
        PsimiXmlReader reader = new PsimiXmlReader();
        try {
            EntrySet entry = reader.read( inPsiMiXmlFile );
            saveScores( entry.getEntries(), type );
            writeScores( entry, outPsiMiFile );
        } catch ( PsimiXmlReaderException e ) {
            e.printStackTrace();
        }
    }

    private void saveScores( Collection<Entry> entries, Set<ConfidenceType> type ) throws PsiMiException {
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

    private List<Attribute> getAttributes( ProteinPair prteinPair, Set<ConfidenceType> type ) throws PsiMiException {
        List<Attribute> attributes = new ArrayList<Attribute>();
        AttributeGetter ag = new AttributeGetterImpl( this.workDir, annoDb );
        for ( Iterator<ConfidenceType> confTypeIter = type.iterator(); confTypeIter.hasNext(); ) {
            ConfidenceType confidenceType = confTypeIter.next();
             if (confidenceType.equals( ConfidenceType.GO )){
                 try {
                     GOFilter.getInstance().initialize( goaFilterFile );
                 } catch ( IOException e ) {
                     throw new PsiMiException( e);
                 }
             }
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

    private void save( Interaction interaction, double[] scores ) throws PsiMiException {
        Unit u = null;
        try {
            u = Unit.class.newInstance();
            Names names = new Names();
            names.setFullName( "interaction confidence score" );
            names.setShortLabel( "intact confidence" );
            u.setNames( names );
        } catch ( InstantiationException e ) {
            throw new PsiMiException( e);
        } catch ( IllegalAccessException e ) {
           throw new PsiMiException( e);
        }
      
        Confidence conf = new Confidence( u, df.format( scores[classifier.getIndex( "high" )] ) );
        Collection<Confidence> confs = interaction.getConfidences();
        confs.add( conf );
    }


    public void writeScores( EntrySet entry, File outPsiMiFile ) throws PsiMiException {
        PsimiXmlWriter writer = new PsimiXmlWriter();
        try {
            writer.write( entry, outPsiMiFile );
        } catch ( PsimiXmlWriterException e ) {
            throw new PsiMiException( e);
        }
    }


}
