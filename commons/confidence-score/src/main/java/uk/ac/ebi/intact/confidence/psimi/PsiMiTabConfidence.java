/*
 * Copyright (c) 2002 The European Bioinformatics Institute, and others.
 * All rights reserved. Please see the file LICENSE
 * in the root directory of this distribution.
 */
package uk.ac.ebi.intact.confidence.psimi;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import psidev.psi.mi.tab.PsimiTabReader;
import psidev.psi.mi.tab.PsimiTabWriter;
import psidev.psi.mi.tab.model.BinaryInteraction;
import psidev.psi.mi.tab.model.Confidence;
import psidev.psi.mi.tab.model.ConfidenceImpl;
import psidev.psi.mi.tab.model.CrossReference;
import psidev.psi.mi.xml.converter.ConverterException;
import uk.ac.ebi.intact.bridges.blast.BlastConfig;
import uk.ac.ebi.intact.bridges.blast.model.UniprotAc;
import uk.ac.ebi.intact.confidence.ProteinPair;
import uk.ac.ebi.intact.confidence.maxent.OpenNLPMaxEntClassifier;
import uk.ac.ebi.intact.confidence.model.Attribute;
import uk.ac.ebi.intact.confidence.model.ConfidenceType;
import uk.ac.ebi.intact.confidence.util.AttributeGetter;
import uk.ac.ebi.intact.confidence.util.AttributeGetterImpl;
import uk.ac.ebi.intact.confidence.utils.ParserUtils;
import uk.ac.ebi.intact.psimitab.IntActBinaryInteraction;
import uk.ac.ebi.intact.psimitab.IntActColumnHandler;

import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * writes to the psimi-tab file the score values
 *
 * @author Irina Armean (iarmean@ebi.ac.uk)
 * @since 1.6.0
 * <pre>28 Aug 2007</pre>
 */
public class PsiMiTabConfidence {
    /**
     * Sets up a logger for that class.
     */
    public static final Log log = LogFactory.getLog( PsiMiTabConfidence.class );

    private File workDir;

    private OpenNLPMaxEntClassifier classifier;
    private BlastConfig blastConfig;
    private Set<UniprotAc> againstProteins;

    public PsiMiTabConfidence( File gisModel, BlastConfig config, Set<UniprotAc> againstProteins, File workDir ) throws IOException {
        if ( !( gisModel.exists() && workDir.exists() ) ) {
            throw new NullPointerException( "GisModel File or workDir does not exist!" );
        }
        if ( config == null || againstProteins == null ) {
            throw new NullPointerException( "BlastConfig or againstProteins-Set is null, please make sure they are not null!" );
        }
        this.blastConfig = config;
        this.againstProteins = againstProteins;
        classifier = new OpenNLPMaxEntClassifier( gisModel );
        this.workDir = workDir;
    }

    public PsiMiTabConfidence( String hcSetPath, String lcSetPath, File workDir, BlastConfig config ) throws IOException {
        this.workDir = workDir;
        this.blastConfig = config;
        this.classifier = new OpenNLPMaxEntClassifier( hcSetPath, lcSetPath, workDir );
        this.againstProteins = fetchAgainstProteins(new File(hcSetPath) );
    }

    private Set<UniprotAc> fetchAgainstProteins( File hcSet ) throws IOException {
        return ParserUtils.parseProteins( hcSet );
        //TODO: replace the ParserUtils class with one of the model parsers
//        BinaryInteractionAttributesReader biar = new BinaryInteractionAttributesReaderImpl();
//        List<BinaryInteractionAttributes> list = biar.read( hcSet);

    }

    public void appendConfidence( File inPsiMiFile, boolean hasHeaderLine, File outPsiMiFile, Set<ConfidenceType> type ) throws PsiMiException {
        PsimiTabReader reader = new PsimiTabReader( hasHeaderLine );
        reader.setBinaryInteractionClass( IntActBinaryInteraction.class );
        reader.setColumnHandler( new IntActColumnHandler() );
        try {
            Iterator<BinaryInteraction> psiMiIterator = reader.iterate( inPsiMiFile );
            Collection<BinaryInteraction> interactions = saveScores( psiMiIterator, type );

            writeScores( interactions, hasHeaderLine, outPsiMiFile );
        } catch ( IOException e ) {
            throw new PsiMiException( e);
        } catch ( ConverterException e ) {
            throw new PsiMiException( e );
        }
    }

    protected Collection<BinaryInteraction> saveScores( Iterator<BinaryInteraction> psiMiIterator, Set<ConfidenceType> type ) {
        Collection<BinaryInteraction> interactions = new ArrayList<BinaryInteraction>();
        while ( psiMiIterator.hasNext() ) {
            BinaryInteraction interaction = psiMiIterator.next();
            if ( interactionValid( interaction ) ) {
                List<Attribute> attribs = getAttributes( interaction, type );
                if ( log.isInfoEnabled() ) {
                    log.info( "interaction: " + interaction.getInteractorA() + ";" + interaction.getInteractorB() + " attribs: " + attribs );
                }
                save( interaction, classifier.evaluate( attribs ) );
            } else {
                save( interaction, classifier.evaluate( new ArrayList<Attribute>( 0 ) ) );
            }
            interactions.add( interaction );
        }
        return interactions;
    }

    private boolean interactionValid( BinaryInteraction interaction ) {
        String[] acs = getUniprotAcs( interaction );
        return ( acs.length == 2 ? true : false );
    }

    protected List<Attribute> getAttributes( BinaryInteraction interaction, Set<ConfidenceType> type ) {
        List<Attribute> attributes = new ArrayList<Attribute>();
        for ( Iterator<ConfidenceType> confTypeIter = type.iterator(); confTypeIter.hasNext(); ) {
            ConfidenceType confidenceType = confTypeIter.next();
            List<Attribute> attribs = getAttributes( interaction, confidenceType );
            if ( attribs != null ) {
                attributes.addAll( attribs );
            }
        }
        return attributes;
    }

    private List<Attribute> getAttributes( BinaryInteraction interaction, ConfidenceType type ) {
        String[] acs = getUniprotAcs( interaction );
        AttributeGetter ag = new AttributeGetterImpl( this.workDir );
        switch ( type ) {
            case GO:
                return ag.fetchGoAttributes( new ProteinPair( acs[0], acs[1] ) );
            case InterPRO:
                return ag.fetchIpAttributes( new ProteinPair( acs[0], acs[1] ) );
            case Alignment:
                return ag.fetchAlignAttributes( new ProteinPair( acs[0], acs[1] ), this.againstProteins, this.blastConfig );
            case ALL:
                return ag.fetchAllAttributes( new ProteinPair( acs[0], acs[1] ), this.againstProteins, this.blastConfig );
            default:
                return ag.fetchAllAttributes( new ProteinPair( acs[0], acs[1] ), this.againstProteins, this.blastConfig );
        }
    }

    private String[] getUniprotAcs( BinaryInteraction interaction ) {
        String uniprotA = "";
        Collection<CrossReference> xrefsA = interaction.getInteractorA().getIdentifiers();
        for ( Iterator<CrossReference> crossReferenceIterator = xrefsA.iterator(); crossReferenceIterator.hasNext(); ) {
            CrossReference crossReference = crossReferenceIterator.next();
            if ( crossReference.getDatabase().equalsIgnoreCase( "uniprotkb" ) ) {
                uniprotA = crossReference.getIdentifier();
            }
        }
        String uniprotB = "";
        Collection<CrossReference> xrefsB = interaction.getInteractorB().getIdentifiers();
        for ( Iterator<CrossReference> crossReferenceIterator = xrefsB.iterator(); crossReferenceIterator.hasNext(); ) {
            CrossReference crossReference = crossReferenceIterator.next();
            if ( crossReference.getDatabase().equalsIgnoreCase( "uniprotkb" ) ) {
                uniprotB = crossReference.getIdentifier();
            }
        }
        if ( uniprotA.equalsIgnoreCase( "" ) || uniprotB.equalsIgnoreCase( "" ) ) {
            return new String[0];
        } else {
            String[] result = {uniprotA, uniprotB};
            return result;
        }
    }

    private void save( BinaryInteraction interaction, double[] scores ) {
        List<Confidence> confVals = new ArrayList<Confidence>( 1 );
        Confidence conf1 = new ConfidenceImpl( "intact confidence", Double.toString( scores[classifier.getIndex( "high" )] ) );
        confVals.add( conf1 );
        interaction.setConfidenceValues( confVals );
    }


    public void writeScores( Collection<BinaryInteraction> interactions, boolean hasHeaderLine, File outPsiMiFile ) {
        PsimiTabWriter writer = new PsimiTabWriter();
        writer.setHeaderEnabled( hasHeaderLine );
        writer.setBinaryInteractionClass( IntActBinaryInteraction.class );
        writer.setColumnHandler( new IntActColumnHandler() );
        try {
            writer.write( interactions, outPsiMiFile );
        } catch ( IOException e ) {
            e.printStackTrace();
        } catch ( ConverterException e ) {
            e.printStackTrace();
        }
    }
}
