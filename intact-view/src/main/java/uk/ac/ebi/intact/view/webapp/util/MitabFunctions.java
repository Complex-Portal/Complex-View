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

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import psidev.psi.mi.tab.model.CrossReference;
import uk.ac.ebi.intact.psimitab.IntactBinaryInteraction;
import uk.ac.ebi.intact.psimitab.model.Annotation;
import uk.ac.ebi.intact.psimitab.model.ExtendedInteractor;
import uk.ac.ebi.intact.psimitab.util.IntactPsimitabUtils;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.net.URLDecoder;
import java.util.*;

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

    private static final String ENZYME_PSI_REF = "MI:0501";

    private static final String ENZYME_TARGET_PSI_REF = "MI:0502";

    private static final String DRUG_PSI_REF = "MI:1094";

    private static final String DRUG_TARGET_PSI_REF = "MI:1095";


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

   

    public static String[] getInitialForMoleculeType( ExtendedInteractor interactor ) {

        String[] typeAndDesc = new String[2];
        if ( interactor.getInteractorType() != null ) {
            typeAndDesc[0] = getFirstLetterofEachToken( interactor.getInteractorType().getText() );
            typeAndDesc[1] = typeAndDesc[0] + " = " + interactor.getInteractorType().getText();
        } else {
            typeAndDesc[0] = "-";
            typeAndDesc[1] = "-";
        }

        return typeAndDesc;
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



    public static String[] getInitialForCrossReference( Collection<CrossReference> crossReferences ) {

        String[] roleAndDesc = new String[2];
        String role;
        if ( crossReferences.size() == 1 ) {
            role = crossReferences.iterator().next().getText();
            if ( role != null && !( "unspecified role".equals( role ) ) ) {
                roleAndDesc[0] = getFirstLetterofEachToken( role ).toUpperCase();
                roleAndDesc[1] = roleAndDesc[0] + " = " + role;
            } else {
                roleAndDesc[0] = "-";
                roleAndDesc[1] = "-";
            }
        } else {
            Set<String> rolesSymbol = new HashSet<String>();
            Set<String> rolesDesc = new HashSet<String>();
           
            for ( CrossReference crossReference : crossReferences ) {
                if ( crossReference.getText() != null && !"unspecified role".equals( crossReference.getText() ) ) {
                    String symbol = getFirstLetterofEachToken( crossReference.getText());
                    rolesSymbol.add( symbol );
                    rolesDesc.add( symbol + " = "+ crossReference.getText() );
                }
            }

            if ( rolesSymbol.size() == 1 ) {
                String symbol = rolesSymbol.iterator().next();
                String desc = rolesDesc.iterator().next();
                if ( symbol != null && symbol.length() > 0 ) {
                    roleAndDesc[0] = symbol;
                    roleAndDesc[1] = symbol + " = "+desc;
                } else {
                    roleAndDesc[0] = "-";
                    roleAndDesc[1] = "-";
                }
            }
            else if ( rolesSymbol.size() > 1 ) {
                roleAndDesc[0] = StringUtils.join( rolesSymbol.toArray(), "," );
                roleAndDesc[1] = StringUtils.join( rolesDesc.toArray(), "," );
            }

        }
        return roleAndDesc;
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


    public static String getInteractorDisplayName( ExtendedInteractor interactor ) {
        IntactBinaryInteraction ibi = new IntactBinaryInteraction(interactor, interactor);
        return IntactPsimitabUtils.getInteractorAName(ibi);
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

    /**
     * Filter the given collection by removing any xref that have any of the two filters.
     * @param xrefs
     * @param databaseFilter
     * @param textFilter
     * @return
     */
    public static Collection getExclusionFilteredCrossReferences( Collection xrefs, String databaseFilter, String textFilter ) {
        if ( databaseFilter == null && textFilter == null) {
            throw new NullPointerException( "You must give at least one non null filter" );
        }

        List<CrossReference> filteredList = new ArrayList<CrossReference>();

        for ( CrossReference xref : ( Collection<CrossReference> ) xrefs ) {
            if ( (databaseFilter != null && !databaseFilter.equals( xref.getDatabase() ))
                 &&
                 (textFilter != null && !textFilter.equals( xref.getText() ) ) ) {
                filteredList.add( xref );
            }
        }
        return filteredList;
    }

    public static boolean getSelectedFromMap( Map columnMap, String columnName ) {

        if ( columnMap.containsKey( columnName ) ) {
            return ( Boolean ) columnMap.get( columnName );
        }
         return false;
     }

    public static String encodeURL( String toEncode ) throws UnsupportedEncodingException {
        String s = "";
        if ( toEncode != null ) {
            s = URLEncoder.encode( toEncode, "UTF-8" );
        }
        return s;
    }

    public static String decodeURL( String toDecode ) throws UnsupportedEncodingException {
        String s = "";
        if ( toDecode != null ) {
            s = URLDecoder.decode( toDecode, "UTF-8" );
        }
        return s;
    }
}