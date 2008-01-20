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
package uk.ac.ebi.intact.confidence.utils;

import uk.ac.ebi.intact.confidence.model.InteractionSimplified;
import uk.ac.ebi.intact.confidence.model.ProteinSimplified;
import uk.ac.ebi.intact.confidence.model.GoIdentifierImpl;
import uk.ac.ebi.intact.confidence.model.InterProIdentifierImpl;
import uk.ac.ebi.intact.model.*;
import uk.ac.ebi.intact.model.util.CvObjectUtils;
import uk.ac.ebi.intact.model.util.ProteinUtils;
import uk.ac.ebi.intact.model.util.AnnotatedObjectUtils;
import uk.ac.ebi.intact.bridges.blast.model.UniprotAc;
import uk.ac.ebi.intact.bridges.blast.model.Sequence;
import uk.ac.ebi.intact.persistence.dao.CvObjectDao;

import java.util.Collection;
import java.util.List;
import java.util.ArrayList;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Util class for getting specific data from a Interaction(uk.ac.intact.model)
 *
 * @author Irina Armean (iarmean@ebi.ac.uk)
 * @version $Id$
 * @since 1.6.0
 */
public class InteractionUtils {
      /**
     * Sets up a logger for that class.
     */
    public static final Log log = LogFactory.getLog( InteractionUtils.class );


      /**
     * saves the interaction information: interactionAc, the protein, the
     * proteins role into an InteractionSimplified object
     *
     * @param interaction
     * @return InteractionSimplified
     */
    public static InteractionSimplified saveInteractionInformation( Interaction interaction ) {
        InteractionSimplified interactionS = new InteractionSimplified();

        interactionS.setAc( interaction.getAc() );

        Collection<Component> components = interaction.getComponents();
        List<ProteinSimplified> proteins = new ArrayList<ProteinSimplified>();
        for ( Component comp : components ) {
            Interactor interactor = comp.getInteractor();

            String role = "-";
            CvExperimentalRole expRole = comp.getCvExperimentalRole();
            CvObjectXref psiMiXref = CvObjectUtils.getPsiMiIdentityXref( expRole );
            if ( CvExperimentalRole.BAIT_PSI_REF.equals( psiMiXref.getPrimaryId() ) ) {
                role = "bait";
            }
            if ( CvExperimentalRole.PREY_PSI_REF.equals( psiMiXref.getPrimaryId() ) ) {
                role = "prey";
            }

            // this is because an interactor could be a small molecule, you want
            // to make sure you have a protein
            if ( Protein.class.isAssignableFrom( interactor.getClass() ) && fromUniprot( interactor )
                 && ProteinUtils.isFromUniprot( ( Protein ) interactor ) ) {
                ProteinSimplified protein = saveProteinInformation( ( Protein ) interactor );
                if ( protein != null ) {
                    protein.setRole( role );
                    proteins.add( protein );
                }
            }
        }
        if (proteins.size() == 1){
            proteins.add( proteins.get( 0 ));
        }
        interactionS.setInteractors( proteins );

        return interactionS;
    }


    /**
     * saves the uniprotAc and the sequence into the new protein object
     *
     * @param protein
     * @return ProteinSimplified object
     */
    public static ProteinSimplified saveProteinInformation( Protein protein ) {
        ProteinSimplified proteinS = new ProteinSimplified();
        InteractorXref uniprotXref = ProteinUtils.getUniprotXref( protein );
        if ( uniprotXref != null ) {
            try {
                proteinS.setUniprotAc( new UniprotAc( uniprotXref.getPrimaryId() ) );
            } catch ( IllegalArgumentException e ) {
                if ( log.isDebugEnabled() ) {
                    log.debug( "UniprotAc not recognized as a valid one:>" + uniprotXref.getPrimaryId() + "<" );
                }
            }
        } else {
            proteinS.setUniprotAc( null );
        }
        if ( protein.getSequence() != null ) {
            proteinS.setSequence( new Sequence( protein.getSequence() ) );
        } else {
            if (log.isInfoEnabled()){
                log.info( "seq was null for: " + protein.getAc() );
            }
        }

        //TODO: solve this somehow
//        CvObjectDao<CvDatabase> cvDatabase = daoFactory.getCvObjectDao( CvDatabase.class );
//        CvDatabase cvGo = cvDatabase.getByPsiMiRef( CvDatabase.GO_MI_REF );
//        if ( cvGo == null ) {
//            throw new NullPointerException( "CvDatabase GO must not be null, check that it exists in the database!" );
//        }
//        Collection<Xref> goRefs = AnnotatedObjectUtils.searchXrefs( protein, cvGo );
//        for ( Xref xref : goRefs ) {
//            GoIdentifierImpl go = new GoIdentifierImpl( xref.getPrimaryId() );
//            proteinS.addGo( go );
//        }
//
//        CvDatabase cvIp = cvDatabase.getByPsiMiRef( CvDatabase.INTERPRO_MI_REF );
//        if ( cvIp == null ) {
//            throw new NullPointerException(
//                    "CvDatabase InterPro must not be null, check that the it exists in the database!" );
//        }
//        Collection<Xref> ipRefs = AnnotatedObjectUtils.searchXrefs( protein, cvIp );
//        for ( Xref xref : ipRefs ) {
//            InterProIdentifierImpl ip = new InterProIdentifierImpl( xref.getPrimaryId() );
//            proteinS.addInterProId( ip );
//        }

        return proteinS;
    }

    private static boolean fromUniprot( Interactor interactor ) {
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
}
