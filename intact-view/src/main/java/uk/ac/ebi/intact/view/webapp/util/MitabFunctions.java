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
 * limitations under the License.
 */
package uk.ac.ebi.intact.view.webapp.util;

import org.apache.commons.collections.map.LRUMap;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.lang.StringUtils;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.queryParser.MultiFieldQueryParser;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.Hits;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import psidev.psi.mi.tab.model.CrossReference;
import uk.ac.ebi.intact.model.CvAliasType;
import uk.ac.ebi.intact.model.CvInteractorType;
import uk.ac.ebi.intact.model.Interactor;
import uk.ac.ebi.intact.model.InteractorAlias;
import uk.ac.ebi.intact.psimitab.model.ExtendedInteractor;
import uk.ac.ebi.intact.psimitab.model.Annotation;
import uk.ac.ebi.intact.psimitab.search.IntactSearchEngine;
import uk.ac.ebi.intact.view.webapp.controller.SearchWebappException;
import uk.ac.ebi.intact.view.webapp.controller.browse.OntologyTermWrapper;

import java.util.Collection;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;

/**
 * Functions to be used in the UI to control the display.
 *
 * @author Prem Anand (prem@ebi.ac.uk)
 * @version $Id$
 * @since 2.0.1
 */
public final class MitabFunctions {

    private static final Log log = LogFactory.getLog( MitabFunctions.class );


    private static final String PROTEIN_MI_REF = "MI:0326";
    private static final String SMALLMOLECULE_MI_REF = "MI:0328";

    private static final String ENZYME = "enzyme";
    private static final String ENZYME_PSI_REF = "MI:0501";

    private static final String ENZYME_TARGET = "enzyme target";
    private static final String ENZYME_TARGET_PSI_REF = "MI:0502";

    private static final String DRUG = "drug";
    private static final String DRUG_PSI_REF = "MI:1094";

    private static final String DRUG_TARGET = "drug target";
    private static final String DRUG_TARGET_PSI_REF = "MI:1095";



    // TODO replace this by EHCache
    private static Map interactorCountCache = new LRUMap(2500);
    private static Map interactionCountCache = new LRUMap(2500);

    //Initials
    private static final String proteinInitial = "PR";
    private static final String smallMoleculeInitial = "SM";

    private MitabFunctions() {
    }

    public static boolean isProtein( ExtendedInteractor interactor ) {

        if ( interactor.getInteractorType() != null ) {
            return ( PROTEIN_MI_REF.equals( interactor.getInteractorType().getIdentifier() ) );
        }

        return false;
    }

    public static boolean isSmallMolecule( ExtendedInteractor interactor ) {

        if ( interactor.getInteractorType() != null ) {
            return ( SMALLMOLECULE_MI_REF.equals( interactor.getInteractorType().getIdentifier() ) );
        }

        return false;
    }

    public static String getInitialForMoleculeType(ExtendedInteractor interactor) {

        if (isProtein(interactor)) {
            return proteinInitial;
        }
        if (isSmallMolecule(interactor)) {
            return smallMoleculeInitial;
        }
        return "-";
    }

    public static boolean isEnzyme( Collection<CrossReference> crossReferences ) {

        for ( CrossReference crossReference : crossReferences ) {
            if ( ENZYME_PSI_REF.equals( crossReference.getIdentifier() ) ) {
                return true;
            }
        }
        return false;
    }

    public static boolean isEnzymeTarget( Collection<CrossReference> crossReferences ) {

        for ( CrossReference crossReference : crossReferences ) {
            if ( ENZYME_TARGET_PSI_REF.equals( crossReference.getIdentifier() ) ) {
                return true;
            }
        }
        return false;
    }


    public static boolean isDrug( Collection<CrossReference> crossReferences ) {

        for ( CrossReference crossReference : crossReferences ) {
            if ( DRUG_PSI_REF.equals( crossReference.getIdentifier() ) ) {
                return true;
            }
        }
        return false;
    }

    public static boolean isDrugTarget( Collection<CrossReference> crossReferences ) {

        for ( CrossReference crossReference : crossReferences ) {
            if ( DRUG_TARGET_PSI_REF.equals( crossReference.getIdentifier() ) ) {
                return true;
            }
        }
        return false;
    }

   
    public static String getInitialForCrossReference(Collection<CrossReference> crossReferences) {

        String role = "-";
        if (crossReferences.size() == 1) {
            role = crossReferences.iterator().next().getText();
            if (role != null && !("unspecified role".equals(role))) {
                return role.substring(0, 2).toUpperCase();
            } else {
                return "-";
            }
        } else {
            List<String> roles = new ArrayList<String>();
            for ( CrossReference crossReference : crossReferences ) {
                if ( crossReference.getText() != null && !"unspecified role".equals( crossReference.getText() ) ) {
                    roles.add( getFirstLetterofEachToken( crossReference.getText() ) );
                }
            }

            if(roles.size()==1){
                role = roles.iterator().next();
            }
            if(roles.size()>1){
                role = StringUtils.join( roles.toArray(),"," );
            }

        }
        return role;
    }


  
    public static String getFirstLetterofEachToken(String stringToken) {
        String s = "";
        if (stringToken.split("\\s+").length == 1) {
            return stringToken.substring(0, 2).toUpperCase();
        }
        for (String str : stringToken.split("\\s+")) {
            s = s + str.substring(0, 1).toUpperCase();
        }
        return s;
    }

    public static int countHits(String searchQuery, String directory) {

        try {
            IndexSearcher searcher = new IndexSearcher(directory);

            String[] defaultFields = new IntactSearchEngine("").getSearchFields();

            long startTime = System.currentTimeMillis();

            QueryParser parser = new MultiFieldQueryParser(defaultFields, new StandardAnalyzer());

            Query query = parser.parse(searchQuery);
            Hits hits = searcher.search(query);

            if ( log.isTraceEnabled() ) {
                log.trace("Counted: "+query.toString()+" - "+hits.length()+" / Elapsed time: "+(System.currentTimeMillis()-startTime)+" ms - Directory: "+directory  );
            }

            int count = hits.length();

            searcher.close();

            return count;

        } catch (Exception e) {
            throw new SearchWebappException("Cannot count hits using query: "+searchQuery+" / in index: "+directory, e);
        }
    }

    public static OntologyTermWrapper populateCounts(OntologyTermWrapper otw, String interactorDirectory, String interactionDirectory, String interactorType) {
        int interactorCount;
        int interactionCount = 0;

        if (otw.getInteractorCount() > 0) {
            // counts have been calculated already, stop here.
            return otw;
        }

        String interactorSearchQuery=null;

        if( CvInteractorType.PROTEIN_MI_REF.equals( interactorType )){
          interactorSearchQuery = otw.getInteractorSearchQuery()+" AND typeA:\""+ CvInteractorType.PROTEIN_MI_REF+"\"";
        }else if(CvInteractorType.SMALL_MOLECULE_MI_REF.equals( interactorType )){
          interactorSearchQuery = otw.getInteractorSearchQuery()+" AND typeA:\""+ CvInteractorType.SMALL_MOLECULE_MI_REF+"\"";
        }

        if ( log.isTraceEnabled() ) {
            log.trace( "InteractorSearchQuery: " +interactorSearchQuery );
        }

        if (interactorCountCache.containsKey(interactorSearchQuery)) {
            interactorCount = (Integer) interactorCountCache.get(interactorSearchQuery);
        } else {
            interactorCount = countHits(interactorSearchQuery, interactorDirectory);

            interactorCountCache.put(interactorSearchQuery, interactorCount);
        }

        if (interactorCount > 0) {

            final String interactionSearchQuery = otw.getInteractionSearchQuery();

            if ( log.isTraceEnabled() ) {
                log.trace(" InteractionSearchQuery: " + interactionSearchQuery  );
            }

            if (interactionCountCache.containsKey(interactionSearchQuery)) {
                interactionCount = (Integer) interactionCountCache.get(interactionSearchQuery);
            } else {
                interactionCount = countHits(interactionSearchQuery, interactionDirectory);

                interactionCountCache.put(interactionSearchQuery, interactionCount);
            }
        }

        otw.setInteractorCount(interactorCount);
        otw.setInteractionCount(interactionCount);

        return otw;
    }

    public static String getIntactIdentifierFromCrossReferences(Collection xrefs) {
        return getIdentifierFromCrossReferences(xrefs, "intact");
    }

     public static String getUniprotIdentifierFromCrossReferences(Collection xrefs) {
        return getIdentifierFromCrossReferences(xrefs, "uniprotkb");
    }

    public static String getChebiIdentifierFromCrossReferences(Collection xrefs) {
        return getIdentifierFromCrossReferences(xrefs, "chebi");
    }

    public static String getIdentifierFromCrossReferences(Collection xrefs, String databaseLabel) {
        for (CrossReference xref : (Collection<CrossReference>) xrefs) {
            if (databaseLabel.equals(xref.getDatabase())) {
                return xref.getIdentifier();
            }
        }
        return null;
    }

    /**
     * Gets the name for a protein, getting the first available after evaluating in this order:
     * gene name > commercial name > synonim > locus > orf > AC.
     * @param interactor
     * @return
     */
    public static String getProteinDisplayName(ExtendedInteractor interactor) {
       String name = null;

        if (!interactor.getAliases().isEmpty()) {
            name = interactor.getAliases().iterator().next().getName();
        } else {
            for (CrossReference xref : interactor.getAlternativeIdentifiers()) {

                if ("commercial name".equals(xref.getText())) {
                    name = xref.getIdentifier();
                }
            }

            if (name == null) {
                String intactAc = getIntactIdentifierFromCrossReferences(interactor.getIdentifiers());

                if (intactAc != null) {
                    Interactor intactInteractor = Functions.getInteractorByAc(intactAc);
                    InteractorAlias alias = getAliasByPriority(intactInteractor, CvAliasType.GENE_NAME_MI_REF,
                                                                        "MI:2003", // commercial name
                                                                        CvAliasType.GO_SYNONYM_MI_REF,
                                                                        CvAliasType.LOCUS_NAME_MI_REF,
                                                                        CvAliasType.ORF_NAME_MI_REF);
                    if (alias != null) {
                        name = alias.getName();
                    } else {
                        name = intactInteractor.getAc();
                    }
                }
            }
        }

        return name;
    }


    public static boolean isApprovedDrug( String drugType ) {
        if ( drugType != null ) {
            if ( drugType.toLowerCase().contains( "approved".toLowerCase() ) ) {
                return true;
            }
        }
        return false;
    }


    public static String getDrugStatus( ExtendedInteractor interactor ) {
        if ( interactor.getAnnotations() != null ) {
            for ( Annotation annotation : interactor.getAnnotations() ) {
                if ("drug type".equals(annotation.getType()) && annotation.getText() != null) {
                    return annotation.getText();
                }
            }
        }
        return "-";
    }

    private static InteractorAlias getAliasByPriority(Interactor intactInteractor, String ... aliasTypes) {
        for (String aliasType : aliasTypes) {
            for (InteractorAlias alias : intactInteractor.getAliases()) {
                if (alias.getCvAliasType() != null && aliasType.equals(alias.getCvAliasType().getIdentifier())) {
                    return alias;
                }
            }
        }

        return null;
    }


    public static Collection getFilteredCrossReferences( Collection xrefs, String filter ) {
        if ( filter == null ) {
            throw new NullPointerException( "You must give a non null filter" );
        }

        List<CrossReference> filteredList = new ArrayList<CrossReference>();

        for ( CrossReference xref : ( Collection<CrossReference> ) xrefs ) {
            if ( filter.equals( xref.getText() ) ) {
                filteredList.add( xref );
            }
        }
        return filteredList;
    }

}
