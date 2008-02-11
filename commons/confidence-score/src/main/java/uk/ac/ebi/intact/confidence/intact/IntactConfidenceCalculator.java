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
import uk.ac.ebi.intact.bridges.blast.model.UniprotAc;
import uk.ac.ebi.intact.confidence.ProteinPair;
import uk.ac.ebi.intact.confidence.dataRetriever.AnnotationRetrieverStrategy;
import uk.ac.ebi.intact.confidence.dataRetriever.IntactAnnotationRetrieverImpl;
import uk.ac.ebi.intact.confidence.maxent.OpenNLPMaxEntClassifier;
import uk.ac.ebi.intact.confidence.model.*;
import uk.ac.ebi.intact.confidence.util.AttributeGetter;
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

    private File workDir;

    private OpenNLPMaxEntClassifier classifier;
    private BlastConfig blastConfig;
    private Set<UniprotAc> againstProteins;
    private DecimalFormat df = new DecimalFormat("0.00");

    private AnnotationRetrieverStrategy annoDb;


     public IntactConfidenceCalculator( File gisModel, BlastConfig config, Set<UniprotAc> againstProteins, File workDir ) throws IOException {
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
    }

    public IntactConfidenceCalculator( OpenNLPMaxEntClassifier gisModel, BlastConfig config, Set<UniprotAc> againstProteins, File workDir ) throws IOException {
        if ( config == null || againstProteins == null ) {
            throw new NullPointerException( "BlastConfig or againstProteins-Set is null, please make sure they are not null!" );
        }
        this.blastConfig = config;
        this.againstProteins = againstProteins;
        classifier = gisModel;
        this.workDir = workDir;

        annoDb = new IntactAnnotationRetrieverImpl();
    }

    public IntactConfidenceCalculator( String hcSetPath, String lcSetPath, File workDir, BlastConfig config ) throws IOException {
        this.workDir = workDir;
        this.blastConfig = config;
        this.classifier = new OpenNLPMaxEntClassifier( hcSetPath, lcSetPath, workDir );
        this.againstProteins = fetchAgainstProteins(new File(hcSetPath) );

         annoDb = new IntactAnnotationRetrieverImpl();
    }

    public IntactConfidenceCalculator(ConfidenceSet highConfidence, ConfidenceSet lowConfidence) {
        this.workDir = new File(System.getProperty( "java.io.tmpdir" ));
        classifier = new OpenNLPMaxEntClassifier( highConfidence, lowConfidence);

        annoDb = new IntactAnnotationRetrieverImpl();

    }

    public String confidenceScore( BinaryInteraction interaction){
        List<Attribute> attributes = getAttributes( interaction, ConfidenceType.ALL );
        double [] scores = classifier.evaluate( attributes );
        return df.format( scores[classifier.getIndex( "high" )] );
    }   


    public void calculate(List<InteractionImpl> interactions, OpenNLPMaxEntClassifier model){
        AttributeGetter aG = new AttributeGetterImpl(workDir, annoDb);
        for ( Iterator<InteractionImpl> iter = interactions.iterator(); iter.hasNext(); ) {
            InteractionImpl interaction =  iter.next();
            calculate(interaction, aG, model);

        }
    }

    public void calculate(List<InteractionImpl> interactions){
        calculate( interactions, this.classifier );
    }

    public void calculate( InteractionImpl interaction ) {
        AttributeGetter aG = new AttributeGetterImpl(workDir, annoDb);
        calculate( interaction, aG, this.classifier );
    }


    ////////////////////
    // Private Methods.
    private void calculate( InteractionImpl interaction, AttributeGetter aG, OpenNLPMaxEntClassifier model ) {
        if ( isInteractionEligible( interaction ) ) {
            //InteractionSimplified interactionS = InteractionUtils.saveInteractionInformation(interaction);
            if (interaction.getConfidences().size() != 0){
                return;
            }
            BinaryInteraction bi = convertToBin( interaction );
            if (bi!= null){
                List<Attribute> attribs = aG.fetchAllAttributes( new ProteinPair( bi.getFirstId().getId(), bi.getSecondId().getId() ), againstProteins, blastConfig );
                double[] scores = model.evaluate( attribs );
                String value = df.format( scores[classifier.getIndex( "high" )] );
                Confidence conf = new Confidence( interaction.getOwner(), value );
                interaction.addConfidence( conf );
            }
        }
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
                        if (idA == null){
                            idA = new UniprotIdentifierImpl(uniprotXref.getPrimaryId());
                        } else if (idB == null){
                            idB = new UniprotIdentifierImpl( uniprotXref.getPrimaryId());
                        } else {
                            if (log.isInfoEnabled()){
                                log.info("Interaction must be binary! " + interaction.getAc());
                            }
                        }
                         if (comp.getStoichiometry() >1 ){
                             if (idB == null){
                                 idB = idA;
                             } else {
                                 if ( log.isInfoEnabled() ) {
                                     log.info( "Interaction must be binary and only one stoichiometry>1 ! " + interaction.getAc() );
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
            if ( dbXref == null ) {
                log.info( "dbXref == null, db: " + db + " interactor ac: " + interactor.getAc() );
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

    private List<Attribute> getAttributes( BinaryInteraction interaction, ConfidenceType type ) {
        ProteinPair pp = new ProteinPair(interaction.getFirstId().getId(), interaction.getSecondId().getId());
        AttributeGetter ag = new AttributeGetterImpl( this.workDir, annoDb );
        switch ( type ) {
            case GO:
                return ag.fetchGoAttributes( pp);
            case InterPRO:
                return ag.fetchIpAttributes(pp);
            case Alignment:
                return ag.fetchAlignAttributes( pp , this.againstProteins, this.blastConfig );
            case ALL:
                return ag.fetchAllAttributes( pp, this.againstProteins, this.blastConfig );
            default:
                return ag.fetchAllAttributes( pp, this.againstProteins, this.blastConfig );
        }
    }


}
