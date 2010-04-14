/**
 * Copyright 2009 The European Bioinformatics Institute, and others.
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
package uk.ac.ebi.intact.view.webapp.controller.application;

import org.apache.commons.collections.ListUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.myfaces.orchestra.conversation.annotations.ConversationName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;
import uk.ac.ebi.intact.view.webapp.controller.search.SearchController;

import javax.annotation.PostConstruct;
import javax.faces.model.SelectItem;
import java.util.*;

/**
 * This class is used to configure columns(select and unselect columns to view) in Interaction view
 *
 * @author Prem Anand (prem@ebi.ac.uk)
 * @version $Id$
 * @since TODO specify the maven artifact version
 */

@Controller( "configureColumn" )
@Scope( "conversation.access" )
@ConversationName( "general" )
public class ConfigureColumnBean {

    private static final Log log = LogFactory.getLog( ConfigureColumnBean.class );

    @Autowired
    private SearchController searchBean;
    private Map<String, Boolean> columnMap;

    private List<String> columns;
    private List<SelectItem> columnsSelectItems;
    private static String MOLECULE_A_NAME = "moleculeA.name";
    private static String MOLECULE_A_LINKS = "moleculeA.links";
    private static String MOLECULE_B_NAME = "moleculeB.name";
    private static String MOLECULE_B_LINKS = "moleculeB.links";
    private static String MOLECULE_A_ALIASES = "moleculeA.aliases";
    private static String MOLECULE_B_ALIASES = "moleculeB.aliases";
    private static String MOLECULE_A_SPECIES = "moleculeA.species";
    private static String MOLECULE_B_SPECIES = "moleculeB.species";
    private static String FIRST_AUTHOR = "interaction.firstauthor";
    private static String PUBMED_IDENTIFIER = "interaction.pubmedid";
    private static String INTERACTION_TYPE = "interaction.interactiontype";
    private static String INTERACTION_DETECTION_METHOD = "interaction.detectionmethod";
    private static String SOURCE_DATABASE = "interaction.sourcedb";
    private static String INTERACTION_AC = "interaction.ac";
    private static String CONFIDENCE_VALUE = "interaction.confidencevalue";
    private static String MOLECULE_A_EXPERIMENTAL_ROLE = "moleculeA.exprole";
    private static String MOLECULE_B_EXPERIMENTAL_ROLE = "moleculeB.exprole";
    private static String MOLECULE_A_BIOLOGICAL_ROLE = "moleculeA.biorole";
    private static String MOLECULE_B_BIOLOGICAL_ROLE = "moleculeB.biorole";
    private static String MOLECULE_A_PROPERTIES = "moleculeA.properties";
    private static String MOLECULE_B_PROPERTIES = "moleculeB.properties";
    private static String MOLECULE_A_INTERACTOR_TYPE = "moleculeA.interactortype";
    private static String MOLECULE_B_INTERACTOR_TYPE = "moleculeB.interactortype";
    private static String HOST_ORGANISM = "interaction.hostorganism";
    private static String EXPANSION_METHOD = "interaction.expansionmethod";
    private static String DATASET = "interaction.dataset";

    private boolean isMoleculeASelected = true;
    private boolean isMoleculeALinksSelected = true;
    private boolean isMoleculeBNameSelected = true;
    private boolean isMoleculeBLinksSelected = true;
    private boolean isMoleculeAAliasesSelected = true;
    private boolean isMoleculeBAliasesSelected = true;
    private boolean isMoleculeASpeciesSelected = true;
    private boolean isMoleculeBSpeciesSelected = true;
    private boolean isFirstAuthorSelected;
    private boolean isPubMedIdentifierSelected = true;
    private boolean isInteractionTypeSelected;
    private boolean isInteractionDetectionMethodSelected =true;
    private boolean isSourceDatabaseSelected;
    private boolean isInteractionACSelected = true;
    private boolean isConfidenceValueSelected;
    private boolean isMoleculeAExperimentalRoleSelected;
    private boolean isMoleculeBExperimentalRoleSelected;
    private boolean isMoleculeABiologicalRoleSelected;
    private boolean isMoleculeBBiologicalRoleSelected;
    private boolean isMoleculeAPropertiesSelected;
    private boolean isMoleculeBPropertiesSelected;
    private boolean isMoleculeAInteractorTypeSelected;
    private boolean isMoleculeBInteractorTypeSelected;
    private boolean isHostOrganismSelected;
    private boolean isExpansionMethodSelected =true;
    private boolean isDatasetSelected;

    private static ResourceBundle rb = null;

    static {

        rb = ResourceBundle.getBundle( "uk.ac.ebi.intact.Messages" );
        if ( rb == null ) {
            throw new NullPointerException( "Please check if the Messages.properties file exist and is declared in faces-config under the resource-bundle element" );
        }
      
    }




    public ConfigureColumnBean() {

        //initialize column Map
        this.columnMap = new LinkedHashMap<String, Boolean>();

        columnMap.put( MOLECULE_A_NAME, isMoleculeASelected );
        columnMap.put( MOLECULE_A_LINKS, isMoleculeALinksSelected );
        columnMap.put( MOLECULE_B_NAME, isMoleculeBNameSelected );
        columnMap.put( MOLECULE_B_LINKS, isMoleculeBLinksSelected );
        columnMap.put( MOLECULE_A_ALIASES, isMoleculeAAliasesSelected );
        columnMap.put( MOLECULE_B_ALIASES, isMoleculeBAliasesSelected );
        columnMap.put( MOLECULE_A_SPECIES, isMoleculeASpeciesSelected );
        columnMap.put( MOLECULE_B_SPECIES, isMoleculeBSpeciesSelected );
        columnMap.put( FIRST_AUTHOR, isFirstAuthorSelected );
        columnMap.put( PUBMED_IDENTIFIER, isPubMedIdentifierSelected );
        columnMap.put( INTERACTION_TYPE, isInteractionTypeSelected );
        columnMap.put( INTERACTION_DETECTION_METHOD, isInteractionDetectionMethodSelected );
        columnMap.put( SOURCE_DATABASE, isSourceDatabaseSelected );
        columnMap.put( INTERACTION_AC, isInteractionACSelected );
        columnMap.put( CONFIDENCE_VALUE, isConfidenceValueSelected );
        columnMap.put( MOLECULE_A_EXPERIMENTAL_ROLE, isMoleculeAExperimentalRoleSelected );
        columnMap.put( MOLECULE_B_EXPERIMENTAL_ROLE, isMoleculeBExperimentalRoleSelected );
        columnMap.put( MOLECULE_A_BIOLOGICAL_ROLE, isMoleculeABiologicalRoleSelected );
        columnMap.put( MOLECULE_B_BIOLOGICAL_ROLE, isMoleculeBBiologicalRoleSelected );
        columnMap.put( MOLECULE_A_PROPERTIES, isMoleculeAPropertiesSelected );
        columnMap.put( MOLECULE_B_PROPERTIES, isMoleculeBPropertiesSelected );
        columnMap.put( MOLECULE_A_INTERACTOR_TYPE, isMoleculeAInteractorTypeSelected );
        columnMap.put( MOLECULE_B_INTERACTOR_TYPE, isMoleculeBInteractorTypeSelected );
        columnMap.put( HOST_ORGANISM, isHostOrganismSelected );
        columnMap.put( EXPANSION_METHOD, isExpansionMethodSelected );
        columnMap.put( DATASET, isDatasetSelected );
    }

    @PostConstruct
    public void loadColumns() {
        if ( log.isDebugEnabled() ) {
            log.debug( "Preloading columns" );
        }
        columns = preselectColumns();
        columnsSelectItems = createSelectItems();
    }

    /**
     * Preselect the default columns based on the expandedView property
     *
     * @return List of selected columns
     */
    private List<String> preselectColumns() {

        List<String> columns = new ArrayList<String>();
        if ( searchBean.isExpandedView() ) {
            return populateColumns( columns, true );
        } else {
            return populateColumns( columns, false );
        }

    }

    private List<String> populateColumns( List<String> columns, boolean isExpanded ) {

        if ( isExpanded ) {
            columns.add( MOLECULE_A_NAME );
            columns.add( MOLECULE_A_LINKS );
            columns.add( MOLECULE_B_NAME );
            columns.add( MOLECULE_B_LINKS );
            columns.add( MOLECULE_A_ALIASES );
            columns.add( MOLECULE_B_ALIASES );
            columns.add( MOLECULE_A_SPECIES );
            columns.add( MOLECULE_B_SPECIES );
            columns.add( FIRST_AUTHOR );
            columns.add( PUBMED_IDENTIFIER );
            columns.add( INTERACTION_TYPE );
            columns.add( INTERACTION_DETECTION_METHOD );
            columns.add( SOURCE_DATABASE );
            columns.add( INTERACTION_AC );
            columns.add( CONFIDENCE_VALUE );
            columns.add( MOLECULE_A_EXPERIMENTAL_ROLE );
            columns.add( MOLECULE_B_EXPERIMENTAL_ROLE );
            columns.add( MOLECULE_A_BIOLOGICAL_ROLE );
            columns.add( MOLECULE_B_BIOLOGICAL_ROLE );
            columns.add( MOLECULE_A_PROPERTIES );
            columns.add( MOLECULE_B_PROPERTIES );
            columns.add( MOLECULE_A_INTERACTOR_TYPE );
            columns.add( MOLECULE_B_INTERACTOR_TYPE );
            columns.add( HOST_ORGANISM );
            columns.add( EXPANSION_METHOD );
            columns.add( DATASET );
        } else {
            columns.add( MOLECULE_A_NAME );
            columns.add( MOLECULE_A_LINKS );
            columns.add( MOLECULE_B_NAME );
            columns.add( MOLECULE_B_LINKS );
            columns.add( MOLECULE_A_ALIASES );
            columns.add( MOLECULE_B_ALIASES );
            columns.add( MOLECULE_A_SPECIES );
            columns.add( MOLECULE_B_SPECIES );
            columns.add( PUBMED_IDENTIFIER );
            columns.add( EXPANSION_METHOD );
            columns.add( INTERACTION_AC );
            columns.add( INTERACTION_DETECTION_METHOD );
        }
        return columns;
    }

    private List<SelectItem> createSelectItems() {
        List<SelectItem> selectItems = new ArrayList<SelectItem>();

        for ( String s : columnMap.keySet() ) {
            selectItems.add( new SelectItem( s,rb.getString(s ).trim()) );
        }

        return selectItems;
    }

    /**
     * Action called when update button is pressed form configure_columns dialog
     * this action has to return to the handReturn listener which listens from the parent window
     *
     * @return null
     */
    public String updateColumn() {
        searchBean.setExpandedView( false );
        Object returnedValue = columns;
        //RequestContext.getCurrentInstance().returnFromDialog( returnedValue, null );
        return null;
    }

    /**
     * Action called when Expanded button is pressed form configure_columns dialog
     * this action has to return to the handReturn listener which listens from the parent window
     *
     * @return null
     */
    public String selectAll() {
        searchBean.setExpandedView( true );
        columns = new ArrayList<String>();
        columns = populateColumns( columns, true );
        return null;
    }

    /**
     * Action called when None button is pressed form configure_columns dialog
     * this action has to return to the handReturn listener which listens from the parent window
     *
     * @return null
     */
    public String selectNone() {
        searchBean.setExpandedView( false );
        columns = new ArrayList<String>();
        //atleast one column should be selected otherwise nullpointer exception is thrown by jsf view
        columns.add( MOLECULE_A_NAME );

        return null;
    }

    /**
     * Action called when Standard button is pressed form configure_columns dialog
     * this action has to return to the handReturn listener which listens from the parent window
     *
     * @return null
     */
    public String selectDefault() {
        searchBean.setExpandedView( false );
        columns = new ArrayList<String>();
        columns = populateColumns( columns, false );
        return null;

    }

    /**
     * The returnListener called from the main parent window
     *
     * @param event ReturnEvent
     */
//    public void handleColumnDialogReturn( ReturnEvent event ) {
//        Object returnedValue = event.getReturnValue();
//
//        if (returnedValue == null) {
//            return;
//        }
//
//        List<String> selectedColumns = ( List<String> ) returnedValue;
//        List<String> allColumns = new ArrayList( columnMap.keySet() );
//        List<String> unselectedColumns = ListUtils.subtract( allColumns, selectedColumns );
//
//
//        for ( String column : selectedColumns ) {
//            //get the keys and set the value to false...
//            if ( columnMap.containsKey( column ) ) {
//                columnMap.put( column, true );
//            }
//        }
//
//        for ( String unselectedColumn : unselectedColumns ) {
//            if ( columnMap.containsKey( unselectedColumn ) ) {
//                columnMap.put( unselectedColumn, false );
//            }
//        }
//
//    }

    //getters & setters

    public Map<String, Boolean> getColumnMap() {
        return columnMap;
    }

    public void setColumnMap( Map<String, Boolean> columnMap ) {
        this.columnMap = columnMap;
    }

    public boolean isMoleculeASelected() {
        return isMoleculeASelected;
    }

    public void setMoleculeASelected( boolean moleculeASelected ) {
        isMoleculeASelected = moleculeASelected;
    }

    public boolean isMoleculeALinksSelected() {
        return isMoleculeALinksSelected;
    }

    public void setMoleculeALinksSelected( boolean moleculeALinksSelected ) {
        isMoleculeALinksSelected = moleculeALinksSelected;
    }

    public boolean isMoleculeBNameSelected() {
        return isMoleculeBNameSelected;
    }

    public void setMoleculeBNameSelected( boolean moleculeBNameSelected ) {
        isMoleculeBNameSelected = moleculeBNameSelected;
    }

    public boolean isMoleculeBLinksSelected() {
        return isMoleculeBLinksSelected;
    }

    public void setMoleculeBLinksSelected( boolean moleculeBLinksSelected ) {
        isMoleculeBLinksSelected = moleculeBLinksSelected;
    }

    public boolean isMoleculeAAliasesSelected() {
        return isMoleculeAAliasesSelected;
    }

    public void setMoleculeAAliasesSelected( boolean moleculeAAliasesSelected ) {
        isMoleculeAAliasesSelected = moleculeAAliasesSelected;
    }

    public boolean isMoleculeASpeciesSelected() {
        return isMoleculeASpeciesSelected;
    }

    public void setMoleculeASpeciesSelected( boolean moleculeASpeciesSelected ) {
        isMoleculeASpeciesSelected = moleculeASpeciesSelected;
    }

    public boolean isMoleculeBSpeciesSelected() {
        return isMoleculeBSpeciesSelected;
    }

    public void setMoleculeBSpeciesSelected( boolean moleculeBSpeciesSelected ) {
        isMoleculeBSpeciesSelected = moleculeBSpeciesSelected;
    }

    public boolean isFirstAuthorSelected() {
        return isFirstAuthorSelected;
    }

    public void setFirstAuthorSelected( boolean firstAuthorSelected ) {
        isFirstAuthorSelected = firstAuthorSelected;
    }

    public boolean isPubMedIdentifierSelected() {
        return isPubMedIdentifierSelected;
    }

    public void setPubMedIdentifierSelected( boolean pubMedIdentifierSelected ) {
        isPubMedIdentifierSelected = pubMedIdentifierSelected;
    }

    public boolean isInteractionTypeSelected() {
        return isInteractionTypeSelected;
    }

    public void setInteractionTypeSelected( boolean interactionTypeSelected ) {
        isInteractionTypeSelected = interactionTypeSelected;
    }

    public boolean isInteractionDetectionMethodSelected() {
        return isInteractionDetectionMethodSelected;
    }

    public void setInteractionDetectionMethodSelected( boolean interactionDetectionMethodSelected ) {
        isInteractionDetectionMethodSelected = interactionDetectionMethodSelected;
    }

    public boolean isSourceDatabaseSelected() {
        return isSourceDatabaseSelected;
    }

    public void setSourceDatabaseSelected( boolean sourceDatabaseSelected ) {
        isSourceDatabaseSelected = sourceDatabaseSelected;
    }

    public boolean isInteractionACSelected() {
        return isInteractionACSelected;
    }

    public void setInteractionACSelected( boolean interactionACSelected ) {
        isInteractionACSelected = interactionACSelected;
    }

    public boolean isConfidenceValueSelected() {
        return isConfidenceValueSelected;
    }

    public void setConfidenceValueSelected( boolean confidenceValueSelected ) {
        isConfidenceValueSelected = confidenceValueSelected;
    }

    public boolean isMoleculeAExperimentalRoleSelected() {
        return isMoleculeAExperimentalRoleSelected;
    }

    public void setMoleculeAExperimentalRoleSelected( boolean moleculeAExperimentalRoleSelected ) {
        isMoleculeAExperimentalRoleSelected = moleculeAExperimentalRoleSelected;
    }

    public boolean isMoleculeBExperimentalRoleSelected() {
        return isMoleculeBExperimentalRoleSelected;
    }

    public void setMoleculeBExperimentalRoleSelected( boolean moleculeBExperimentalRoleSelected ) {
        isMoleculeBExperimentalRoleSelected = moleculeBExperimentalRoleSelected;
    }

    public boolean isMoleculeABiologicalRoleSelected() {
        return isMoleculeABiologicalRoleSelected;
    }

    public void setMoleculeABiologicalRoleSelected( boolean moleculeABiologicalRoleSelected ) {
        isMoleculeABiologicalRoleSelected = moleculeABiologicalRoleSelected;
    }

    public boolean isMoleculeBBiologicalRoleSelected() {
        return isMoleculeBBiologicalRoleSelected;
    }

    public void setMoleculeBBiologicalRoleSelected( boolean moleculeBBiologicalRoleSelected ) {
        isMoleculeBBiologicalRoleSelected = moleculeBBiologicalRoleSelected;
    }

    public boolean isMoleculeAPropertiesSelected() {
        return isMoleculeAPropertiesSelected;
    }

    public void setMoleculeAPropertiesSelected( boolean moleculeAPropertiesSelected ) {
        isMoleculeAPropertiesSelected = moleculeAPropertiesSelected;
    }

    public boolean isMoleculeBPropertiesSelected() {
        return isMoleculeBPropertiesSelected;
    }

    public void setMoleculeBPropertiesSelected( boolean moleculeBPropertiesSelected ) {
        isMoleculeBPropertiesSelected = moleculeBPropertiesSelected;
    }

    public boolean isMoleculeAInteractorTypeSelected() {
        return isMoleculeAInteractorTypeSelected;
    }

    public void setMoleculeAInteractorTypeSelected( boolean moleculeAInteractorTypeSelected ) {
        isMoleculeAInteractorTypeSelected = moleculeAInteractorTypeSelected;
    }

    public boolean isMoleculeBInteractorTypeSelected() {
        return isMoleculeBInteractorTypeSelected;
    }

    public void setMoleculeBInteractorTypeSelected( boolean moleculeBInteractorTypeSelected ) {
        isMoleculeBInteractorTypeSelected = moleculeBInteractorTypeSelected;
    }

    public boolean isHostOrganismSelected() {
        return isHostOrganismSelected;
    }

    public void setHostOrganismSelected( boolean hostOrganismSelected ) {
        isHostOrganismSelected = hostOrganismSelected;
    }

    public boolean isExpansionMethodSelected() {
        return isExpansionMethodSelected;
    }

    public void setExpansionMethodSelected( boolean expansionMethodSelected ) {
        isExpansionMethodSelected = expansionMethodSelected;
    }

    public boolean isDatasetSelected() {
        return isDatasetSelected;
    }

    public void setDatasetSelected( boolean datasetSelected ) {
        isDatasetSelected = datasetSelected;
    }


    public boolean isMoleculeBAliasesSelected() {
        return isMoleculeBAliasesSelected;
    }

    public void setMoleculeBAliasesSelected( boolean moleculeBAliasesSelected ) {
        isMoleculeBAliasesSelected = moleculeBAliasesSelected;
    }

    public List<String> getColumns() {
        return columns;
    }

    public void setColumns( List<String> columns ) {
        this.columns = columns;
    }

    public List<SelectItem> getColumnsSelectItems() {
        return columnsSelectItems;
    }

    public void setColumnsSelectItems( List<SelectItem> columnsSelectItems ) {
        this.columnsSelectItems = columnsSelectItems;
    }
}
