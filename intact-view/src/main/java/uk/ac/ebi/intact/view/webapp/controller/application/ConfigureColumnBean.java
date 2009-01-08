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

import org.springframework.stereotype.Controller;
import org.springframework.context.annotation.Scope;
import org.springframework.beans.factory.annotation.Autowired;
import org.apache.myfaces.orchestra.conversation.annotations.ConversationName;
import org.apache.myfaces.trinidad.context.RequestContext;
import org.apache.myfaces.trinidad.event.ReturnEvent;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.logging.Log;
import org.apache.commons.collections.ListUtils;

import javax.annotation.PostConstruct;
import javax.faces.model.SelectItem;
import java.util.*;


import uk.ac.ebi.intact.view.webapp.controller.search.SearchController;

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


    private static final String COMPOUND_NAME = "Compound Name";
    private static final String COMPOUND_LINKS = "Compound Links";
    private static final String TARGET_NAME = "Target Name";
    private static final String TARGET_LINKS = "Target Links";
    private static final String COMPOUND_ALIASES = "Compound Aliases";
    private static final String TARGET_ALIASES = "Target Aliases";
    private static final String COMPOUND_SPECIES = "Compound Species";
    private static final String TARGET_SPECIES = "Target Species";
    private static final String FIRST_AUTHOR = "First Author";
    private static final String PUBMED_IDENTIFIER = "PubMed Identifier";
    private static final String INTERACTION_TYPE = "Interaction Type";
    private static final String INTERACTION_DETECTION_METHOD = "Interaction Detection Method";
    private static final String SOURCE_DATABASE = "Source Database";
    private static final String INTERACTION_AC = "Interaction AC";
    private static final String CONFIDENCE_VALUE = "Confidence Value";
    private static final String COMPOUND_EXPERIMENTAL_ROLE = "Compound Experimental Role";
    private static final String TARGET_EXPERIMENTAL_ROLE = "Target Experimental Role";
    private static final String COMPOUND_BIOLOGICAL_ROLE = "Compound Biological Role";
    private static final String TARGET_BIOLOGICAL_ROLE = "Target Biological Role";
    private static final String COMPOUND_PROPERTIES = "Compound Properties";
    private static final String TARGET_PROPERTIES = "Target Properties";
    private static final String COMPOUND_INTERACTOR_TYPE = "Compound Interactor Type";
    private static final String TARGET_INTERACTOR_TYPE = "Target Interactor Type";
    private static final String HOST_ORGANISM = "Host Organism";
    private static final String EXPANSION_METHOD = "Expansion Method";
    private static final String DATASET = "Dataset";

    private boolean isCompoundNameSelected = true;
    private boolean isCompoundLinksSelected = true;
    private boolean isTargetNameSelected = true;
    private boolean isTargetLinksSelected = true;
    private boolean isCompoundAliasesSelected = true;
    private boolean isTargetAliasesSelected = true;
    private boolean isCompoundSpeciesSelected = true;
    private boolean isTargetSpeciesSelected = true;
    private boolean isFirstAuthorSelected;
    private boolean isPubMedIdentifierSelected = true;
    private boolean isInteractionTypeSelected;
    private boolean isInteractionDetectionMethodSelected;
    private boolean isSourceDatabaseSelected = true;
    private boolean isInteractionACSelected = true;
    private boolean isConfidenceValueSelected;
    private boolean isCompoundExperimentalRoleSelected;
    private boolean isTargetExperimentalRoleSelected;
    private boolean isCompoundBiologicalRoleSelected;
    private boolean isTargetBiologicalRoleSelected;
    private boolean isCompoundPropertiesSelected;
    private boolean isTargetPropertiesSelected;
    private boolean isCompoundInteractorTypeSelected;
    private boolean isTargetInteractorTypeSelected;
    private boolean isHostOrganismSelected;
    private boolean isExpansionMethodSelected;
    private boolean isDatasetSelected;


    public ConfigureColumnBean() {

        //initialize column Map
        this.columnMap = new LinkedHashMap<String, Boolean>();

        columnMap.put( COMPOUND_NAME, isCompoundNameSelected );
        columnMap.put( COMPOUND_LINKS, isCompoundLinksSelected );
        columnMap.put( TARGET_NAME, isTargetNameSelected );
        columnMap.put( TARGET_LINKS, isTargetLinksSelected );
        columnMap.put( COMPOUND_ALIASES, isCompoundAliasesSelected );
        columnMap.put( TARGET_ALIASES, isTargetAliasesSelected );
        columnMap.put( COMPOUND_SPECIES, isCompoundSpeciesSelected );
        columnMap.put( TARGET_SPECIES, isTargetSpeciesSelected );
        columnMap.put( FIRST_AUTHOR, isFirstAuthorSelected );
        columnMap.put( PUBMED_IDENTIFIER, isPubMedIdentifierSelected );
        columnMap.put( INTERACTION_TYPE, isInteractionTypeSelected );
        columnMap.put( INTERACTION_DETECTION_METHOD, isInteractionDetectionMethodSelected );
        columnMap.put( SOURCE_DATABASE, isSourceDatabaseSelected );
        columnMap.put( INTERACTION_AC, isInteractionACSelected );
        columnMap.put( CONFIDENCE_VALUE, isConfidenceValueSelected );
        columnMap.put( COMPOUND_EXPERIMENTAL_ROLE, isCompoundExperimentalRoleSelected );
        columnMap.put( TARGET_EXPERIMENTAL_ROLE, isTargetExperimentalRoleSelected );
        columnMap.put( COMPOUND_BIOLOGICAL_ROLE, isCompoundBiologicalRoleSelected );
        columnMap.put( TARGET_BIOLOGICAL_ROLE, isTargetBiologicalRoleSelected );
        columnMap.put( COMPOUND_PROPERTIES, isCompoundPropertiesSelected );
        columnMap.put( TARGET_PROPERTIES, isTargetPropertiesSelected );
        columnMap.put( COMPOUND_INTERACTOR_TYPE, isCompoundInteractorTypeSelected );
        columnMap.put( TARGET_INTERACTOR_TYPE, isTargetInteractorTypeSelected );
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
            columns.add( COMPOUND_NAME );
            columns.add( COMPOUND_LINKS );
            columns.add( TARGET_NAME );
            columns.add( TARGET_LINKS );
            columns.add( COMPOUND_ALIASES );
            columns.add( TARGET_ALIASES );
            columns.add( COMPOUND_SPECIES );
            columns.add( TARGET_SPECIES );
            columns.add( FIRST_AUTHOR );
            columns.add( PUBMED_IDENTIFIER );
            columns.add( INTERACTION_TYPE );
            columns.add( INTERACTION_DETECTION_METHOD );
            columns.add( SOURCE_DATABASE );
            columns.add( INTERACTION_AC );
            columns.add( INTERACTION_AC );
            columns.add( CONFIDENCE_VALUE );
            columns.add( COMPOUND_EXPERIMENTAL_ROLE );
            columns.add( TARGET_EXPERIMENTAL_ROLE );
            columns.add( COMPOUND_BIOLOGICAL_ROLE );
            columns.add( TARGET_BIOLOGICAL_ROLE );
            columns.add( COMPOUND_PROPERTIES );
            columns.add( TARGET_PROPERTIES );
            columns.add( COMPOUND_INTERACTOR_TYPE );
            columns.add( TARGET_INTERACTOR_TYPE );
            columns.add( HOST_ORGANISM );
            columns.add( EXPANSION_METHOD );
            columns.add( DATASET );
        } else {
            columns.add( COMPOUND_NAME );
            columns.add( COMPOUND_LINKS );
            columns.add( TARGET_NAME );
            columns.add( TARGET_LINKS );
            columns.add( COMPOUND_ALIASES );
            columns.add( TARGET_ALIASES );
            columns.add( COMPOUND_SPECIES );
            columns.add( TARGET_SPECIES );
            columns.add( PUBMED_IDENTIFIER );
            columns.add( SOURCE_DATABASE );
            columns.add( INTERACTION_AC );
        }
        return columns;
    }

    private List<SelectItem> createSelectItems() {
        List<SelectItem> selectItems = new ArrayList<SelectItem>();

        for ( String s : columnMap.keySet() ) {
            selectItems.add( new SelectItem( s ) );
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
        if ( log.isDebugEnabled() ) {
            log.debug( " updateColumn called" );
        }
        searchBean.setExpandedView( false );
        Object returnedValue = columns;
        RequestContext.getCurrentInstance().returnFromDialog( returnedValue, null );
        return null;
    }

    /**
     * Action called when Expanded button is pressed form configure_columns dialog
     * this action has to return to the handReturn listener which listens from the parent window
     *
     * @return null
     */
    public String selectAll() {
        if ( log.isDebugEnabled() ) {
            log.debug( "select all called" );
        }
        searchBean.setExpandedView( true );
        columns = new ArrayList<String>();
        columns = populateColumns( columns, true );
        RequestContext.getCurrentInstance().returnFromDialog( columns, null );
        return null;
    }

    /**
     * Action called when None button is pressed form configure_columns dialog
     * this action has to return to the handReturn listener which listens from the parent window
     *
     * @return null
     */
    public String selectNone() {

        if ( log.isDebugEnabled() ) {
            log.debug( "select none called" );
        }

        searchBean.setExpandedView( false );
        columns = new ArrayList<String>();
        //atleast one column should be selected otherwise nullpointer exception is thrown by jsf view
        columns.add( COMPOUND_NAME );
        log.debug( "columns size from selectNone " + columns.size() );
        RequestContext.getCurrentInstance().returnFromDialog( columns, null );
        return null;
    }

    /**
     * Action called when Standard button is pressed form configure_columns dialog
     * this action has to return to the handReturn listener which listens from the parent window
     *
     * @return null
     */
    public String selectDefault() {
        if ( log.isDebugEnabled() ) {
            log.debug( "select default called" );
        }

        searchBean.setExpandedView( false );
        columns = new ArrayList<String>();
        columns = populateColumns( columns, false );
        log.debug( "columns size from selectDefault " + columns.size() );
        RequestContext.getCurrentInstance().returnFromDialog( columns, null );
        return null;

    }

    /**
     * The returnListener called from the main parent window
     *
     * @param event ReturnEvent
     */
    public void handleReturn( ReturnEvent event ) {
        Object returnedValue = event.getReturnValue();
        log.debug( " handleReturn called  from ConfigureColumn " + returnedValue );

        List<String> selectedColumns = ( List<String> ) returnedValue;
        List<String> allColumns = new ArrayList( columnMap.keySet() );
        List<String> unselectedColumns = ListUtils.subtract( allColumns, selectedColumns );


        for ( String column : selectedColumns ) {
            log.debug( " column selected " + column );
            //get the keys and set the value to false...
            if ( columnMap.containsKey( column ) ) {
                columnMap.put( column, true );
            }
        }

        for ( String unselectedColumn : unselectedColumns ) {
            if ( columnMap.containsKey( unselectedColumn ) ) {
                columnMap.put( unselectedColumn, false );
            }
        }

    }

    //getters & setters

    public Map<String, Boolean> getColumnMap() {
        return columnMap;
    }

    public void setColumnMap( Map<String, Boolean> columnMap ) {
        this.columnMap = columnMap;
    }

    public boolean isCompoundNameSelected() {
        return isCompoundNameSelected;
    }

    public void setCompoundNameSelected( boolean compoundNameSelected ) {
        isCompoundNameSelected = compoundNameSelected;
    }

    public boolean isCompoundLinksSelected() {
        return isCompoundLinksSelected;
    }

    public void setCompoundLinksSelected( boolean compoundLinksSelected ) {
        isCompoundLinksSelected = compoundLinksSelected;
    }

    public boolean isTargetNameSelected() {
        return isTargetNameSelected;
    }

    public void setTargetNameSelected( boolean targetNameSelected ) {
        isTargetNameSelected = targetNameSelected;
    }

    public boolean isTargetLinksSelected() {
        return isTargetLinksSelected;
    }

    public void setTargetLinksSelected( boolean targetLinksSelected ) {
        isTargetLinksSelected = targetLinksSelected;
    }

    public boolean isCompoundAliasesSelected() {
        return isCompoundAliasesSelected;
    }

    public void setCompoundAliasesSelected( boolean compoundAliasesSelected ) {
        isCompoundAliasesSelected = compoundAliasesSelected;
    }

    public boolean isCompoundSpeciesSelected() {
        return isCompoundSpeciesSelected;
    }

    public void setCompoundSpeciesSelected( boolean compoundSpeciesSelected ) {
        isCompoundSpeciesSelected = compoundSpeciesSelected;
    }

    public boolean isTargetSpeciesSelected() {
        return isTargetSpeciesSelected;
    }

    public void setTargetSpeciesSelected( boolean targetSpeciesSelected ) {
        isTargetSpeciesSelected = targetSpeciesSelected;
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

    public boolean isCompoundExperimentalRoleSelected() {
        return isCompoundExperimentalRoleSelected;
    }

    public void setCompoundExperimentalRoleSelected( boolean compoundExperimentalRoleSelected ) {
        isCompoundExperimentalRoleSelected = compoundExperimentalRoleSelected;
    }

    public boolean isTargetExperimentalRoleSelected() {
        return isTargetExperimentalRoleSelected;
    }

    public void setTargetExperimentalRoleSelected( boolean targetExperimentalRoleSelected ) {
        isTargetExperimentalRoleSelected = targetExperimentalRoleSelected;
    }

    public boolean isCompoundBiologicalRoleSelected() {
        return isCompoundBiologicalRoleSelected;
    }

    public void setCompoundBiologicalRoleSelected( boolean compoundBiologicalRoleSelected ) {
        isCompoundBiologicalRoleSelected = compoundBiologicalRoleSelected;
    }

    public boolean isTargetBiologicalRoleSelected() {
        return isTargetBiologicalRoleSelected;
    }

    public void setTargetBiologicalRoleSelected( boolean targetBiologicalRoleSelected ) {
        isTargetBiologicalRoleSelected = targetBiologicalRoleSelected;
    }

    public boolean isCompoundPropertiesSelected() {
        return isCompoundPropertiesSelected;
    }

    public void setCompoundPropertiesSelected( boolean compoundPropertiesSelected ) {
        isCompoundPropertiesSelected = compoundPropertiesSelected;
    }

    public boolean isTargetPropertiesSelected() {
        return isTargetPropertiesSelected;
    }

    public void setTargetPropertiesSelected( boolean targetPropertiesSelected ) {
        isTargetPropertiesSelected = targetPropertiesSelected;
    }

    public boolean isCompoundInteractorTypeSelected() {
        return isCompoundInteractorTypeSelected;
    }

    public void setCompoundInteractorTypeSelected( boolean compoundInteractorTypeSelected ) {
        isCompoundInteractorTypeSelected = compoundInteractorTypeSelected;
    }

    public boolean isTargetInteractorTypeSelected() {
        return isTargetInteractorTypeSelected;
    }

    public void setTargetInteractorTypeSelected( boolean targetInteractorTypeSelected ) {
        isTargetInteractorTypeSelected = targetInteractorTypeSelected;
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


    public boolean isTargetAliasesSelected() {
        return isTargetAliasesSelected;
    }

    public void setTargetAliasesSelected( boolean targetAliasesSelected ) {
        isTargetAliasesSelected = targetAliasesSelected;
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
