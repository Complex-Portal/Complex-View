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
package uk.ac.ebi.intact.service.graph.binary.label;

import psidev.psi.mi.tab.model.Alias;
import psidev.psi.mi.tab.model.Interactor;
import psidev.psi.mi.tab.model.Organism;
import psidev.psi.mi.tab.model.CrossReference;
import uk.ac.ebi.intact.model.InteractorAlias;
import uk.ac.ebi.intact.model.CvAliasType;
import uk.ac.ebi.intact.context.IntactContext;

import java.util.Collection;

/**
 * Uses the aliases of Interactor to create Label for the Node.
 *
 * @author Nadin Neuhauser
 * @version $Id$
 * @since 1.6.0-Snapshot
 */
public class AliasLabelStrategy implements LabelStrategy {

    /**
     * previous implementation returns label with organism name
     * @param interactor The interactor for which the label has to be build
     * @return label
     */
    public String buildDefaultLabelOriginal( Interactor interactor ) {

        String id = null;
        if (interactor.getAliases() != null && !interactor.getAliases().isEmpty()) {


            for ( Alias alias : interactor.getAliases()){
                if (id == null){
                    id =alias.getName().toLowerCase( );
                    break;
                }
            }

            final Organism organism = interactor.getOrganism();
            if ( organism != null &&  !organism.getIdentifiers().isEmpty() ) {
                String organismName = organism.getIdentifiers().iterator().next().getText();
                if (organismName != null && !"-3".equals( organism.getTaxid() ) ){
                    return id + "_" + organismName;
                }
            }


        }
         return id;
        /*else {
            LabelStrategy strategy = new AlternativeLabelStrategy();
            return strategy.buildDefaultLabel( interactor );
        }*/
    }


    /**
     * getProteinDisplayName and other dependent method taken from MitabFunctions.java in intact-view
     * @param interactor The interactor for which the label has to be build
     * @return label
     */
    public String buildDefaultLabel( Interactor interactor ) {
        String label = getProteinDisplayName( interactor );

        if ( label != null ) {
            return label;
        } else {

            label = buildDefaultLabelOriginal( interactor );
            if ( label != null ) {
                return label;
            } else {
                LabelStrategy strategy = new AlternativeLabelStrategy();
                return strategy.buildDefaultLabel( interactor );
            }
        }

    }


    public String buildLabel( Interactor interactor, String database) {
        return buildDefaultLabel( interactor );
    }


    //buildLabel modified for dgi
       public static String getProteinDisplayName(Interactor interactor) {
          String name = null;

           if (!interactor.getAliases().isEmpty()) {
               name = interactor.getAliases().iterator().next().getName();
           } else {
               for ( CrossReference xref : interactor.getAlternativeIdentifiers()) {

                   if ("commercial name".equals(xref.getText())) {
                       name = xref.getIdentifier();
                   }
               }

               if (name == null) {
                   String intactAc = getIntactIdentifierFromCrossReferences(interactor.getIdentifiers());

                   if (intactAc != null) {
                       uk.ac.ebi.intact.model.Interactor intactInteractor = getInteractorByAc(intactAc);
                       InteractorAlias alias = getAliasByPriority(intactInteractor, CvAliasType.GENE_NAME_MI_REF,
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


       private static String getIntactIdentifierFromCrossReferences( Collection xrefs) {
           return getIdentifierFromCrossReferences(xrefs, "intact");
       }

        private static String getIdentifierFromCrossReferences(Collection xrefs, String databaseLabel) {
           for (CrossReference xref : (Collection<CrossReference>) xrefs) {
               if (databaseLabel.equals(xref.getDatabase())) {
                   return xref.getIdentifier();
               }
           }
           return null;
       }


        private static InteractorAlias getAliasByPriority( uk.ac.ebi.intact.model.Interactor intactInteractor, String ... aliasTypes) {
           for (String aliasType : aliasTypes) {
               for (InteractorAlias alias : intactInteractor.getAliases()) {
                   if (alias.getCvAliasType() != null && aliasType.equals(alias.getCvAliasType().getIdentifier())) {
                       return alias;
                   }
               }
           }

           return null;
       }

        private static uk.ac.ebi.intact.model.Interactor getInteractorByAc( String intactAc ) {
           return IntactContext.getCurrentInstance().getDataContext().getDaoFactory().getInteractorDao().getByAc( intactAc );
       }

}
