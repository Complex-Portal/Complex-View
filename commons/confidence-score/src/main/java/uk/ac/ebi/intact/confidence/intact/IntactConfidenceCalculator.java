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
package uk.ac.ebi.intact.confidence.intact;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import uk.ac.ebi.intact.bridges.blast.BlastConfig;
import uk.ac.ebi.intact.bridges.blast.BlastServiceException;
import uk.ac.ebi.intact.bridges.blast.model.UniprotAc;
import uk.ac.ebi.intact.confidence.ProteinPair;
import uk.ac.ebi.intact.confidence.dataRetriever.AnnotationRetrieverStrategy;
import uk.ac.ebi.intact.confidence.dataRetriever.IntactAnnotationRetrieverImpl;
import uk.ac.ebi.intact.confidence.filter.FilterException;
import uk.ac.ebi.intact.confidence.filter.GOAFilter;
import uk.ac.ebi.intact.confidence.filter.GOAFilterMapImpl;
import uk.ac.ebi.intact.confidence.maxent.OpenNLPMaxEntClassifier;
import uk.ac.ebi.intact.confidence.model.*;
import uk.ac.ebi.intact.confidence.util.AttributeGetter;
import uk.ac.ebi.intact.confidence.util.AttributeGetterException;
import uk.ac.ebi.intact.confidence.util.AttributeGetterImpl;
import uk.ac.ebi.intact.confidence.utils.ParserUtils;
import uk.ac.ebi.intact.model.*;
import uk.ac.ebi.intact.model.Confidence;
import uk.ac.ebi.intact.model.util.CvObjectUtils;
import uk.ac.ebi.intact.model.util.ProteinUtils;

import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * Writes confidence values to IntAct.
 *
 * @author Irina Armean (iarmean@ebi.ac.uk)
 * @version $Id$
 * @since 1.6.0
 *        <pre>
 *        16-Jan-2008
 *        </pre>
 */
public class IntactConfidenceCalculator implements IntactScoreCalculator{
      /**
     * Sets up a logger for that class.
     */
    public static final Log log = LogFactory.getLog( IntactConfidenceCalculator.class );

    //TODO: is there a better way to do this?
    private CvConfidenceType cvConfidenceType;

    private File workDir;

    private OpenNLPMaxEntClassifier classifier;
    private BlastConfig blastConfig;
    private Set<UniprotAc> againstProteins;
    private DecimalFormat df = new DecimalFormat("0.00");

    private AnnotationRetrieverStrategy annoDb;
    private File goaFile;
    private GOAFilter goaFilter;

    //////////////////
    // Constructor(s).
     public IntactConfidenceCalculator( File gisModel, BlastConfig config, Set<UniprotAc> againstProteins, File goaUniprotFile, File workDir ) throws IOException {
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

        annoDb = new IntactAnnotationRetrieverImpl();
        goaFile = goaUniprotFile;
        goaFilter = new GOAFilterMapImpl();
    }

    public IntactConfidenceCalculator( OpenNLPMaxEntClassifier gisModel, BlastConfig config, Set<UniprotAc> againstProteins,File goaUniprotFile, File workDir ) throws IOException {
        if ( config == null || againstProteins == null ) {
            throw new NullPointerException( "BlastConfig or againstProteins-Set is null, please make sure they are not null!" );
        }
        this.blastConfig = config;
        this.againstProteins = againstProteins;
        classifier = gisModel;
        this.workDir = workDir;

        annoDb = new IntactAnnotationRetrieverImpl();
        goaFile = goaUniprotFile;
        goaFilter = new GOAFilterMapImpl();
    }

    public IntactConfidenceCalculator( String hcSetPath, String lcSetPath, File workDir,File goaUniprotFile, BlastConfig config ) throws IOException {
        this.workDir = workDir;
        this.blastConfig = config;
        this.classifier = new OpenNLPMaxEntClassifier( hcSetPath, lcSetPath, workDir );
        this.againstProteins = fetchAgainstProteins(new File(hcSetPath) );

        annoDb = new IntactAnnotationRetrieverImpl();
        goaFile = goaUniprotFile;
        goaFilter = new GOAFilterMapImpl();
    }

    public IntactConfidenceCalculator(ConfidenceSet highConfidence, ConfidenceSet lowConfidence) {
        this.workDir = new File(System.getProperty( "java.io.tmpdir" ));
        classifier = new OpenNLPMaxEntClassifier( highConfidence, lowConfidence);

        annoDb = new IntactAnnotationRetrieverImpl();

    }

    ////////////////////
    // Public Method(s).
    public void setConfidenceType( CvConfidenceType cvConfidenceType ) {
        this.cvConfidenceType = cvConfidenceType;
    }

    public void calculate(List<InteractionImpl> interactions, boolean override) throws AttributeGetterException, FilterException {
        calculate( interactions, this.classifier, override );
    }

    public void calculate( InteractionImpl interaction, boolean override ) throws AttributeGetterException, FilterException {
        AttributeGetter aG = new AttributeGetterImpl(workDir, annoDb, goaFilter);
        try {
            long start = System.currentTimeMillis();
            calculate( interaction, aG, this.classifier, override );
            if (log.isInfoEnabled()){
                long time= System.currentTimeMillis() - start;
                double sec = time / 1000;
                log.info( "to calculate score for interaction("+interaction.getAc() + ", " + interaction.getShortLabel()+") took: " + sec +" sec");
            }
        } catch ( FilterException e ) {
            throw e;
        }
    }


    public void calculate(List<InteractionImpl> interactions, OpenNLPMaxEntClassifier model, boolean override) throws AttributeGetterException, FilterException {
        AttributeGetter aG = new AttributeGetterImpl(workDir, annoDb, goaFilter);
        for ( Iterator<InteractionImpl> iter = interactions.iterator(); iter.hasNext(); ) {
            InteractionImpl interaction =  iter.next();

            long start = System.currentTimeMillis();
            calculate(interaction, aG, model, override);

            if (log.isDebugEnabled()){
                long time= System.currentTimeMillis() - start;
                double sec = time / 1000;
                log.debug( "to calculate score for interaction("+interaction.getAc() + ", " + interaction.getShortLabel()+") took: " + sec +" sec");
            }

        }
    }

     public String confidenceScore( BinaryInteraction interaction) throws AttributeGetterException {
        List<Attribute> attributes = getAttributes( interaction, ConfidenceType.ALL );
        double [] scores = classifier.evaluate( attributes );
        return df.format( scores[classifier.getIndex( "high" )] );
    }

    ////////////////////
    // Private Methods.
    private void calculate( InteractionImpl interaction, AttributeGetter aG, OpenNLPMaxEntClassifier model, boolean override ) throws AttributeGetterException, FilterException {
        if ( isInteractionEligible( interaction ) ) {
            //InteractionSimplified interactionS = InteractionUtils.saveInteractionInformation(interaction);
            boolean confidencePresent = confidenceExists(interaction);
            if (!override && confidencePresent ){
                if (log.isDebugEnabled()){
                    log.debug ("for interaction(" + interaction.getAc() +") + override(" + override + ") confidencePresent(" + confidencePresent +") => conf calculation skipped" );
                }
                return;
            }
            BinaryInteraction bi = convertToBin( interaction );
            if (bi!= null){
                goaFilter.initialize( goaFile );
                List<Attribute> attribs = aG.fetchAllAttributes( new ProteinPair( bi.getFirstId().getId(), bi.getSecondId().getId() ), againstProteins, blastConfig );
                double[] scores = model.evaluate( attribs );
                String value = df.format( scores[classifier.getIndex( "high" )] );
                if (log.isTraceEnabled()){
                    log.trace("interaction(" + interaction.getAc()+") + attribus(" + printAttribs(attribs) +") => score = " + value );
                }
                if (override && confidencePresent){
                    Confidence conf  = getConfidence(interaction);
                    if (conf == null  && log.isErrorEnabled()){
                        log.error("Not null confidence expected "+ interaction.getShortLabel());                        
                    } else {
                        conf.setValue( value );
                        if ( log.isTraceEnabled() ) {
                            log.trace( "confidence overriden" );
                        }
                    }
                } else {
                    Confidence conf = new Confidence( interaction.getOwner(), value );
                    conf.setCvConfidenceType( cvConfidenceType);
                    interaction.addConfidence( conf );
                     if (log.isTraceEnabled()){
                        log.trace("confidence added");
                    }
                }
            }
        }
    }

    private String printAttribs( List<Attribute> attribs ) {
        if (attribs == null){
            return "null";
        }
        String attribstStr = "";
        if ( attribs.size() == 0){
            return attribstStr;
        }
        for ( Iterator<Attribute> iter = attribs.iterator(); iter.hasNext(); ) {
            Attribute attribute =  iter.next();
            attribstStr +=","  +attribute.convertToString();
        }
        return attribstStr.substring( 1 );
    }

    private Confidence getConfidence( InteractionImpl interaction ) {
        for ( Iterator<Confidence> iter = interaction.getConfidences().iterator(); iter.hasNext(); ) {
            Confidence confidence =  iter.next();
            if (confidence.getCvConfidenceType().getShortLabel().equalsIgnoreCase( cvConfidenceType.getShortLabel() )){
                return confidence;
            }
        }
        return null;
    }

    private boolean confidenceExists( InteractionImpl interaction ) {
        int nr = 0;
        for ( Iterator<Confidence> iter = interaction.getConfidences().iterator(); iter.hasNext(); ) {
            Confidence confidence =  iter.next();
            if (confidence.getCvConfidenceType().getShortLabel().equalsIgnoreCase( cvConfidenceType.getShortLabel() )  ){
                nr++;
            }
        }
        if (nr == 1) {
            return true;
        }
        if(nr > 1){
            log.warn( "Found more than only one Confidnece of type: " + cvConfidenceType + " for interaction " + interaction.getShortLabel() );
            return true;
        }
        return false;
    }

    private BinaryInteraction convertToBin( Interaction interaction ) {
        Identifier idA = null;
        Identifier idB = null;
        Collection<Component> comps = interaction.getComponents();
        if (comps.size() <= 2){
            for ( Component comp: comps) {
                Interactor interactor = comp.getInteractor();
                 if ( Protein.class.isAssignableFrom( interactor.getClass() ) && fromUniprot( interactor )
                    && ProteinUtils.isFromUniprot( ( Protein ) interactor ) ) {
                     Protein prot = (Protein) interactor;
                     InteractorXref uniprotXref = ProteinUtils.getUniprotXref( prot );
                     if (uniprotXref != null){
                         try {
                        if (idA == null){
                            idA = new UniprotIdentifierImpl(uniprotXref.getPrimaryId());
                        } else if (idB == null){
                            idB = new UniprotIdentifierImpl( uniprotXref.getPrimaryId());
                        } else {
                            if (log.isTraceEnabled()){
                                log.trace("Interaction not binary! " + interaction.getAc());
                            }
                        }
                         } catch (IllegalArgumentException e ){
                             String msg = e.getMessage();
                             if (msg.startsWith( "Ac must be a valid uniprotAc!" )){
                                 if (log.isWarnEnabled()){
                                    log.warn(msg);
                                 }
                                 return null;
                             }                                                           
                         }
                         if (comp.getStoichiometry() >1 ){
                             if (idB == null){
                                 idB = idA;
                             } else {
                                 if ( log.isTraceEnabled() ) {
                                     log.trace( "Interaction must be binary and only one stoichiometry>1 ! " + interaction.getAc() );
                                 }
                             }
                         }
                     }
                }
            }
        }

        if (idA != null && idB != null){
            return new BinaryInteraction(idA, idB);
        } else {         
            return null;
        }
    }

    private boolean isInteractionEligible( InteractionImpl interaction ) {
        int nr = 0;

        for ( Component comp : interaction.getComponents() ) {
            Interactor interactor = comp.getInteractor();
            if ( interactor != null && Protein.class.isAssignableFrom( interactor.getClass() )
                 && ProteinUtils.isFromUniprot( ( Protein ) interactor ) && fromUniprot( interactor ) ) {
                nr += 1;
                if (comp.getStoichiometry() > 1 ){
                    return true;
                }
            }
            if ( nr == 2 ) {
                return true;
            }
        }

        return false;
    }

    private boolean fromUniprot( Interactor interactor ) {
        if ( interactor == null ) {
            return false;
        }
        Collection<InteractorXref> xrefs = interactor.getXrefs();
        for ( InteractorXref interactorXref : xrefs ) {
            CvDatabase db = interactorXref.getCvDatabase();
            CvObjectXref dbXref = CvObjectUtils.getPsiMiIdentityXref( db );
            if ( dbXref == null && log.isWarnEnabled()) {
                log.warn( "dbXref == null, db: " + db + " interactor ac: " + interactor.getAc() );
                return false;
            }
            if ( CvDatabase.UNIPROT_MI_REF.equals( db.getMiIdentifier() ) ) {
                CvXrefQualifier qualifier = interactorXref.getCvXrefQualifier();
                CvObjectXref qualifierXref = CvObjectUtils.getPsiMiIdentityXref( qualifier );
                // if the uniprotAc are marked for removal
                if ( qualifierXref == null ) {
                    if ( log.isWarnEnabled() ) {
                        log.warn( "qualifierXref is null for interactor :" + interactor.getAc() + " db qualifier: " + qualifier.getAc() );
                    }
                    return false;
                }

                if ( CvXrefQualifier.IDENTITY_MI_REF.equals( qualifierXref.getPrimaryId() ) ) {
                    return true;
                }
            }
        }
        return false;
    }


     private Set<UniprotAc> fetchAgainstProteins( File hcSet ) throws IOException {
        return ParserUtils.parseProteins( hcSet );
     }

    private List<Attribute> getAttributes( BinaryInteraction interaction, ConfidenceType type ) throws AttributeGetterException {
        ProteinPair pp = new ProteinPair(interaction.getFirstId().getId(), interaction.getSecondId().getId());
        AttributeGetter ag = new AttributeGetterImpl( this.workDir, annoDb, goaFilter );
        switch ( type ) {
            case GO:
                return ag.fetchGoAttributes( pp);
            case InterPRO:
                return ag.fetchIpAttributes(pp);
            case Alignment:
                try {
                    return ag.fetchAlignAttributes( pp , this.againstProteins, this.blastConfig );
                } catch ( BlastServiceException e ) {
                    throw new AttributeGetterException( e);
                }
            case ALL:
                return ag.fetchAllAttributes( pp, this.againstProteins, this.blastConfig );
            default:
                return ag.fetchAllAttributes( pp, this.againstProteins, this.blastConfig );
        }
    }


}
