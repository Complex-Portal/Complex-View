package uk.ac.ebi.intact.service.complex;

import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.common.SolrInputDocument;
import org.hupo.psi.mi.psicquic.registry.ServiceType;
import org.hupo.psi.mi.psicquic.registry.client.registry.DefaultPsicquicRegistryClient;
import org.hupo.psi.mi.psicquic.registry.client.registry.PsicquicRegistryClient;
import org.hupo.psi.mi.psicquic.wsclient.PsicquicSimpleClient;
import psidev.psi.mi.tab.PsimiTabReader;
import psidev.psi.mi.tab.model.BinaryInteraction;
import psidev.psi.mi.tab.model.CrossReference;
import uk.ac.ebi.intact.bridges.ontologies.term.OntologyTerm;
import uk.ac.ebi.intact.core.context.IntactContext;
import uk.ac.ebi.intact.dataexchange.psimi.solr.ontology.OntologySearcher;
import uk.ac.ebi.intact.model.*;

import java.io.InputStream;
import java.util.*;

/**
 * @author Oscar Forner (oforner@ebi.ac.uk)
 * @version $Id$
 * @since 14/08/13
 */
public class ComplexSolrConverter {
    /********************************/
    /*      Private attributes      */
    /********************************/
    private ComplexSolrEnricher complexSolrEnricher ;

    /**************************/
    /*      Constructors      */
    /**************************/
    public ComplexSolrConverter ( OntologySearcher ontologySearcher ) {
        this.complexSolrEnricher = new ComplexSolrEnricher ( ontologySearcher ) ;
    }
    public ComplexSolrConverter ( SolrServer solrServer ) {
        this ( new OntologySearcher ( solrServer ) ) ;
    }

    /*****************************/
    /*      Convert Methods      */
    /*****************************/
    public SolrInputDocument convertComplexToSolrDocument (
            InteractionImpl complex,
            SolrInputDocument solrDocument )
            throws  Exception {

        //////////////////////////
        ///   COMPLEX FIELDS   ///
        //////////////////////////

        // add info to publication_id and complex_id field: ac, owner, (db, id and db:id) from xrefs
        solrDocument.addField ( ComplexFieldNames.COMPLEX_ID, complex.getAc ( ) ) ;
        if ( complex.getOwner ( ) != null ) {
            solrDocument.addField ( ComplexFieldNames.COMPLEX_ID, complex.getOwner ( ) .getShortLabel() ) ;
            solrDocument.addField ( ComplexFieldNames.COMPLEX_ID, complex.getOwner ( ) .getShortLabel( ) + ":" + complex.getAc ( ) ) ;
        }
        String db, id ;
        for ( Xref xref : complex.getXrefs ( ) ) {
            id = xref.getPrimaryId ( ) ;
            db = xref.getCvDatabase ( ) .getShortLabel ( ) ;
            solrDocument.addField ( ComplexFieldNames.COMPLEX_ID, id ) ;
            solrDocument.addField ( ComplexFieldNames.COMPLEX_ID, db ) ;
            solrDocument.addField ( ComplexFieldNames.COMPLEX_ID, new StringBuilder ( )
                        .append ( db ) .append ( ":" ) .append ( id ) .toString ( ) ) ;
        }
        // add info to complex_alias field: short label, full name, (name and type) from  alias
        // and add info to complex_name
        solrDocument.addField ( ComplexFieldNames.COMPLEX_ALIAS, complex.getShortLabel ( ) ) ;
        solrDocument.addField ( ComplexFieldNames.COMPLEX_ALIAS, complex.getFullName ( ) ) ;
        String a_name, a_type;
        boolean one = false;
        for ( Alias alias : complex.getAliases ( ) ) {
            a_name = alias.getName ( ) ;
            a_type = alias.getCvAliasType ( ) .getShortLabel ( ) ;
            solrDocument.addField ( ComplexFieldNames.COMPLEX_ALIAS, a_name ) ;
            solrDocument.addField ( ComplexFieldNames.COMPLEX_ALIAS, a_type ) ;
            solrDocument.addField ( ComplexFieldNames.COMPLEX_ALIAS, new StringBuilder ( )
                        .append ( a_type ) .append ( ":" ) .append ( a_name ) .toString ( ) ) ;
            if ( ! one && alias.getCvAliasType ( ) . getShortLabel ( ) .equals ( "recommended name" ) ) {
                one = true ;
                solrDocument.addField ( ComplexFieldNames.COMPLEX_NAME, alias.getName ( ) ) ;
            }
        }
        if ( ! one ) {
            solrDocument.addField ( ComplexFieldNames.COMPLEX_NAME, complex.getShortLabel ( ) ) ;
        }
        // add info to source and source_f, complex_organism, complex_organism_f and organism_name fields:
        CvObject cvObject = null ;
        for ( Experiment experiment : complex.getExperiments ( ) ) {
            cvObject = experiment.getCvInteraction ( ) ;
            solrDocument.addField ( ComplexFieldNames.SOURCE, cvObject.getShortLabel ( ) ) ;
            solrDocument.addField ( ComplexFieldNames.SOURCE_F, cvObject.getShortLabel ( ) ) ;
            solrDocument.addField ( ComplexFieldNames.SOURCE, cvObject.getFullName ( ) ) ;

            OntologyTerm ontologyTerm = complexSolrEnricher.findOntologyTermByName ( experiment.getBioSource ( ) .getShortLabel ( ) ) ;
            solrDocument.addField ( ComplexFieldNames.COMPLEX_ORGANISM, ontologyTerm.getName ( ) ) ;
            solrDocument.addField ( ComplexFieldNames.COMPLEX_ORGANISM_F, ontologyTerm.getName ( ) ) ;
            solrDocument.addField ( ComplexFieldNames.ORGANISM_NAME, ontologyTerm.getName ( ) ) ;
        }
        // add info to complex_organism_ontology field:
        // It will do by the enricher
        // add info to interaction_type field:
        CvInteractionType type = complex.getCvInteractionType ( ) ;
        solrDocument.addField ( ComplexFieldNames.INTERACTION_TYPE, type.getShortLabel ( ) ) ;
        solrDocument.addField ( ComplexFieldNames.INTERACTION_TYPE, type.getFullName ( ) ) ;
        solrDocument.addField ( ComplexFieldNames.INTERACTION_TYPE, type.getIdentifier ( ) ) ;
        for ( Alias alias : type.getAliases ( ) ) {
            solrDocument.addField ( ComplexFieldNames.INTERACTION_TYPE, alias.getName ( ) ) ;
        }
        // add info to interaction_type_f field:
        solrDocument.addField ( ComplexFieldNames.INTERACTION_TYPE_F, type.getFullName ( ) ) ;
        // add info to interaction_type_ontology field:
        // It will do by the enricher
        // add info to complex_xref field:
        // It will do by the enricher
        // add info to complex_xref_ontology field:
        // It will do by the enricher
        // add info to complex_AC field:
        solrDocument.addField ( ComplexFieldNames.COMPLEX_AC, complex.getAc ( ) ) ;
        // add info to description field:
        for ( Annotation annotation : complex.getAnnotations ( ) ) {
            if ( annotation.getCvTopic ( ) .getShortLabel ( ) .equals ( "curated_complex" ) ) {
                solrDocument.addField ( ComplexFieldNames.DESCRIPTION, annotation.getAnnotationText ( ) ) ;
                break ; // We only want the first one
            }
        }

        /////////////////////////////
        ///   INTERACTOR FIELDS   ///
        /////////////////////////////

        // add info to interactor_id field:
        // to avoid stack overflow problems I change the recursive algorithm to this iterative algorithm
        Stack < Interactor > stack = new Stack < Interactor > ( ) ;
        stack.push ( complex ) ;
        Set < String > indexed = new HashSet < String > ( ) ;
        indexed.add ( complex.getAc ( ) ) ;
        Interactor interactorAux;
        boolean stc = false;
        float number_participants = 0.0f , stoichiometry = 0.0f ;
        do {
            interactorAux = stack.pop ( ) ;
            // if interactorAux is an instance of InteractionImpl we need to get all its components
            if ( interactorAux instanceof InteractionImpl ) {
                String AC = null ;
                for ( Component component : ( ( InteractionImpl) interactorAux ) .getComponents ( ) ) {
                    AC = component.getAc ( ) ;
                    stoichiometry = component.getStoichiometry ( ) ;
                    if ( ! indexed.contains ( AC ) ) {
                        stack.push ( component.getInteractor ( ) ) ;
                        stc |= stoichiometry != 0.0f ;
                        indexed.add ( AC ) ;
                    }
                    number_participants += stoichiometry == 0.0f ? 1 : stoichiometry ;
                }
            }
            // now, we get the information of the interactorAux
            String shortLabel = null ;
            String ID = null;
            solrDocument.addField ( ComplexFieldNames.INTERACTOR_ID, interactorAux.getAc ( ) ) ;
            for ( InteractorXref xref : interactorAux.getXrefs ( ) ) {
                //solrDocument.addField ( ComplexFieldNames.INTERACTOR_ID, xref.getAc ( ) ) ;
                CvXrefQualifier cvXrefQualifier = xref.getCvXrefQualifier ( ) ;
                if ( cvXrefQualifier != null &&
                        (  cvXrefQualifier.getIdentifier ( ) .equals ( CvXrefQualifier.IDENTITY_MI_REF )
                        || cvXrefQualifier.getIdentifier ( ) .equals ( CvXrefQualifier.SECONDARY_AC_MI_REF )
                        || cvXrefQualifier.getShortLabel ( ) .equalsIgnoreCase ( "intact-secondary" ) ) ) {
                    shortLabel = xref.getCvDatabase ( ) .getShortLabel ( ) ;
                    ID = xref.getPrimaryId() ;
                    solrDocument.addField ( ComplexFieldNames.INTERACTOR_ID, ID ) ;
                    solrDocument.addField ( ComplexFieldNames.INTERACTOR_ID, shortLabel ) ;
                    solrDocument.addField ( ComplexFieldNames.INTERACTOR_ID,
                           new StringBuilder ( ) .append ( shortLabel ) .append ( ":" ) .append ( ID ) .toString ( ) ) ;
                }
                else {
                    if ( xref.getCvDatabase ( ) .getIdentifier() .equals(CvDatabase.GO_MI_REF) ) {
                        solrDocument.addField ( ComplexFieldNames.INTERACTOR_XREF_ONTOLOGY, ID ) ;
                    }
                    solrDocument.addField ( ComplexFieldNames.INTERACTOR_XREF, ID ) ;
                    solrDocument.addField ( ComplexFieldNames.INTERACTOR_XREF, shortLabel ) ;
                    solrDocument.addField ( ComplexFieldNames.INTERACTOR_XREF,
                           new StringBuilder ( ) .append ( shortLabel ) .append ( ":" ) .append ( ID ) .toString ( ) ) ;
                    if ( xref.getCvXrefQualifier ( ) != null ){
                        solrDocument.addField ( ComplexFieldNames.INTERACTOR_XREF, cvXrefQualifier.getShortLabel() ) ;
                    }
                }
            }
        } while ( ! stack.isEmpty ( ) ) ;
        solrDocument.addField ( ComplexFieldNames.STC, stc ) ;
        solrDocument.addField(ComplexFieldNames.PARAM, complex.getParameters().isEmpty()) ;

        // add info to features, features_f, biorole, biorole_f, interactor_alias and interactor_alias_f fields:
        CvBiologicalRole biologicalRole = null ;
        CvFeatureType featureType = null ;
        for ( Component component : complex.getComponents ( ) ) {
            biologicalRole = component.getCvBiologicalRole ( ) ;
            solrDocument.addField ( ComplexFieldNames.BIOROLE, biologicalRole.getShortLabel ( ) ) ;
            solrDocument.addField ( ComplexFieldNames.BIOROLE_F, biologicalRole.getShortLabel ( ) ) ;
            solrDocument.addField ( ComplexFieldNames.BIOROLE, biologicalRole.getFullName ( ) ) ;
            for ( Feature feature : component.getFeatures ( ) ) {
                featureType = feature.getCvFeatureType ( ) ;
                solrDocument.addField ( ComplexFieldNames.FEATURES, featureType.getShortLabel ( ) ) ;
                solrDocument.addField ( ComplexFieldNames.FEATURES_F, featureType.getShortLabel ( ) ) ;
                solrDocument.addField ( ComplexFieldNames.FEATURES, featureType.getFullName ( ) ) ;
            }

            solrDocument.addField ( ComplexFieldNames.INTERACTOR_ALIAS, component.getShortLabel ( ) ) ;
            solrDocument.addField ( ComplexFieldNames.INTERACTOR_ALIAS, component.getFullName ( ) ) ;
            solrDocument.addField ( ComplexFieldNames.INTERACTOR_ALIAS_F, component.getShortLabel ( ) ) ;
            solrDocument.addField ( ComplexFieldNames.INTERACTOR_ALIAS_F, component.getFullName ( ) ) ;
            for ( Alias alias : component.getAliases ( ) ) {
                a_name = alias.getName ( ) ;
                a_type = alias.getCvAliasType ( ) .getShortLabel() ;
                solrDocument.addField ( ComplexFieldNames.INTERACTOR_ALIAS, a_name ) ;
                solrDocument.addField ( ComplexFieldNames.INTERACTOR_ALIAS, a_type ) ;
                solrDocument.addField ( ComplexFieldNames.INTERACTOR_ALIAS, new StringBuilder ( )
                        .append ( a_type ) .append ( ":" ) .append(a_name) .toString() ) ;
            }
        }
        // add info to interactor_type field:
        solrDocument.addField ( ComplexFieldNames.INTERACTOR_TYPE,
                complex.getCvInteractionType ( ) .getFullName ( ) ) ;
        // add info to interactor_type_ontology field:
        // It will do by the enricher

        ///////////////////////////
        ///   PSICQUIC FIELDS   ///
        ///////////////////////////
        // Most of psicquic fields have been indexed above

        // add info to biorole_ontology field:
        // It will do by the enricher
        // add info to features_ontology field:
        // It will do by the enricher

        ////////////////////////
        ///   OTHER FIELDS   ///
        ////////////////////////

        // add info to source_ontology field:
        // It will do by the enricher
        // add info to number_participants field:
        solrDocument.addField ( ComplexFieldNames.NUMBER_PARTICIPANTS, number_participants ) ;
        //add info to publication_id
        // that was the old code
        /*InteractionImpl interaction = IntactContext.getCurrentInstance ( )
                                        .getDaoFactory ( )
                                        .getInteractionDao ( )
                                        .getByAc ( complex.getAc ( ) ) ;
        for ( Experiment experiment : interaction.getExperiments ( ) ) {
            for ( Xref xref : experiment.getPublication ( ) .getXrefs ( ) ) {
                if ( xref.hasValidPrimaryId ( ) ) {
                    solrDocument.addField ( ComplexFieldNames.PUBLICATION_ID, xref.getPrimaryId ( ) ) ;
                }
            }
        }
        //add info to update
        solrDocument.addField ( ComplexFieldNames.UDATE, complex.getUpdated ( ) .getDate ( ) ) ;
        */
        // that is the new code
        DefaultPsicquicRegistryClient defaultPsicquicRegistryClient = new DefaultPsicquicRegistryClient ( ) ;
        // I need to create a Map to get the real name for the Data Base
        PsicquicSimpleClient psicquicSimpleClient = null ;
        String DB = null ;
        String ID = null ;
        PsimiTabReader psimiTabReader = new PsimiTabReader ( ) ;
        for ( ServiceType serviceType : defaultPsicquicRegistryClient.listActiveServices ( ) ) {
            psicquicSimpleClient = new PsicquicSimpleClient ( serviceType.getRestUrl ( ) ) ;
            InputStream inputStream = psicquicSimpleClient.getByInteraction (
                    // it needs a map to the real names of the DB
                    new StringBuilder ( ) .append ( serviceType.getName ( ) )
                    .append ( "/" )
                    .append ( serviceType.getRestUrl ( ) )
                    .toString ( )
            ) ;
            for ( BinaryInteraction interaction : psimiTabReader.read ( inputStream ) ){
                for ( Object crossReference : interaction.getPublications ( ) ) {
                    DB = ((CrossReference)crossReference).getDatabase ( ) ;
                    ID = ((CrossReference)crossReference).getIdentifier ( ) ;
                    solrDocument.addField ( ComplexFieldNames.PUBLICATION_ID, DB ) ;
                    solrDocument.addField ( ComplexFieldNames.PUBLICATION_ID, ID ) ;
                    solrDocument.addField ( ComplexFieldNames.PUBLICATION_ID, new StringBuilder ( )
                            .append ( DB )
                            .append ( ":" )
                            .append ( ID )
                            .toString ( )
                    ) ;
                }
            }

        }



        /////////////////////////
        ///   ENRICH FIELDS   ///
        /////////////////////////

        // Enrich the Solr Document and return that
        return complexSolrEnricher.enrich ( complex, solrDocument ) ;
    }

    public SolrInputDocument convertComplexToSolrDocument ( InteractionImpl complex ) throws Exception {
        return convertComplexToSolrDocument ( complex, new SolrInputDocument ( ) ) ;
    }


}
