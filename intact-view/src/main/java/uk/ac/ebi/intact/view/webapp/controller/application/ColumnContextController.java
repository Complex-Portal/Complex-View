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

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.myfaces.orchestra.conversation.annotations.ConversationName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;
import uk.ac.ebi.intact.view.webapp.controller.search.SearchController;

import javax.annotation.PostConstruct;
import javax.faces.model.SelectItem;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

/**
 * This class is used to configure columns(select and unselect columns to view) in Interaction view
 *
 * @author Prem Anand (prem@ebi.ac.uk)
 * @version $Id$
 */

@Controller( "columnContext" )
@Scope( "conversation.access" )
@ConversationName( "general" )
public class ColumnContextController {

    private static final Log log = LogFactory.getLog( ColumnContextController.class );

    @Autowired
    private SearchController searchBean;

    private String[] selectedColumns;
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

    public ColumnContextController() {

    }

    @PostConstruct
    public void loadColumns() {
        if ( log.isDebugEnabled() ) {
            log.debug( "Preloading columns" );
        }
        selectStandardColumns();
    }

    private String[] getSimpleColumns() {
        return new String[] {
            MOLECULE_A_NAME, MOLECULE_A_LINKS, MOLECULE_B_NAME, MOLECULE_B_LINKS, MOLECULE_A_ALIASES, MOLECULE_B_ALIASES, MOLECULE_A_SPECIES, MOLECULE_B_SPECIES,
                PUBMED_IDENTIFIER, INTERACTION_DETECTION_METHOD, INTERACTION_AC, EXPANSION_METHOD
        };
    }

    private String[] getExpandedColumns() {
        String[] allColumns = getSimpleColumns();

        return (String[]) ArrayUtils.addAll(allColumns, new String[] {
                FIRST_AUTHOR, INTERACTION_TYPE, SOURCE_DATABASE, CONFIDENCE_VALUE, MOLECULE_A_EXPERIMENTAL_ROLE, MOLECULE_B_EXPERIMENTAL_ROLE, MOLECULE_A_BIOLOGICAL_ROLE,
                MOLECULE_B_BIOLOGICAL_ROLE, MOLECULE_A_PROPERTIES, MOLECULE_B_PROPERTIES, MOLECULE_A_INTERACTOR_TYPE, MOLECULE_B_INTERACTOR_TYPE, HOST_ORGANISM, DATASET
        });
    }

    public boolean isColumnVisible(String columnKey) {
        return ArrayUtils.contains(selectedColumns, columnKey);
    }

    private String[] getMinimumColumns() {
        return new String[] { MOLECULE_A_NAME, MOLECULE_B_NAME, INTERACTION_AC};
    }

    public void selectStandardColumns() {
        selectedColumns = getSimpleColumns();
    }

    public void selectExpandedColumns() {
        selectedColumns = getExpandedColumns();
    }

    public void selectMininumColumns() {
        this.selectedColumns = getMinimumColumns();
    }


    private List<SelectItem> createSelectItems() {
        ResourceBundle rb = ResourceBundle.getBundle( "uk.ac.ebi.intact.Messages" );

        List<SelectItem> selectItems = new ArrayList<SelectItem>();

        for ( String columnKey : getExpandedColumns()) {
            selectItems.add( new SelectItem(columnKey, rb.getString(columnKey).trim()) );
        }

        return selectItems;
    }

    public String[] getSelectedColumns() {
        return selectedColumns;
    }

    public void setSelectedColumns(String[] selectedColumns) {
        this.selectedColumns = selectedColumns;
    }

    public List<SelectItem> getColumnsSelectItems() {
        if (columnsSelectItems == null) {
            columnsSelectItems = createSelectItems();
        }
        return columnsSelectItems;
    }
}
