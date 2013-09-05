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
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;

import javax.annotation.PostConstruct;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.faces.model.SelectItem;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.Serializable;
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
@Scope( "session" )
public class ColumnContextController implements Serializable{

    private static final Log log = LogFactory.getLog( ColumnContextController.class );

    private static String COOKIE_COLS_NAME = "intact.cols.view";

    private static String COOKIE_MINIMAL_VALUE = "min cols";
    private static String COOKIE_BASIC_VALUE = "basic cols";
    private static String COOKIE_STANDARD_VALUE = "std cols";
    private static String COOKIE_EXPANDED_VALUE = "ext cols";
    private static String COOKIE_COMPLETE_VALUE = "comp cols";

    private String[] selectedColumns;
    private List<SelectItem> columnsSelectItems;
    private static String MOLECULE_A_NAME = "moleculeA.name";
    private static String MOLECULE_A_LINKS = "moleculeA.links";
    private static String MOLECULE_B_NAME = "moleculeB.name";
    private static String MOLECULE_B_LINKS = "moleculeB.links";
    private static String MOLECULE_A_ID = "moleculeA.uniqueId";
    private static String MOLECULE_A_ALTID = "moleculeA.alternativeId";
    private static String MOLECULE_B_ID = "moleculeB.uniqueId";
    private static String MOLECULE_B_ALTID = "moleculeB.alternativeId";
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
    private static String EXPANSION_METHOD = "interaction.expansionmethod";
    private static String MOLECULE_A_BIOLOGICAL_ROLE = "moleculeA.biorole";
    private static String MOLECULE_B_BIOLOGICAL_ROLE = "moleculeB.biorole";
    private static String MOLECULE_A_EXPERIMENTAL_ROLE = "moleculeA.exprole";
    private static String MOLECULE_B_EXPERIMENTAL_ROLE = "moleculeB.exprole";
    private static String MOLECULE_A_INTERACTOR_TYPE = "moleculeA.interactortype";
    private static String MOLECULE_B_INTERACTOR_TYPE = "moleculeB.interactortype";
    private static String MOLECULE_A_XREFS = "moleculeA.xrefs";
    private static String MOLECULE_B_XREFS = "moleculeB.xrefs";
    private static String INTERACTION_XREFS = "interaction.xrefs";
    private static String MOLECULE_A_ANNOTATION = "moleculeA.annotations";
    private static String MOLECULE_B_ANNOTATION = "moleculeB.annotations";
    private static String INTERACTION_ANNOTATION = "interaction.annotations";
    private static String HOST_ORGANISM = "interaction.hostorganism";
    private static String PARAMETERS_INTERACTION = "interaction.parameters";
    private static String CREATED_DATE = "created.date";
    private static String UPDATE_DATE = "update.date";
    private static String MOLECULE_A_CHECKSUM = "moleculeA.checksum";
    private static String MOLECULE_B_CHECKSUM = "moleculeB.checksum";
    private static String INTERACTION_CHECKSUM = "interaction.checksum";
    private static String NEGATIVE = "interaction.negative";
    private static String MOLECULE_A_FEATURE = "moleculeA.features";
    private static String MOLECULE_B_FEATURE = "moleculeB.features";
    private static String MOLECULE_A_STOICHIOMETRY = "moleculeA.stc";
    private static String MOLECULE_B_STOICHIOMETRY = "moleculeB.stc";
    private static String MOLECULE_A_IDENTIFICATION = "moleculeA.pmethod";
    private static String MOLECULE_B_IDENTIFICATION = "moleculeB.pmethod";

    private String[] simpleColumns;

    private String[] basicColumns;

    private String[] expandedColumns;

    private String[] completeColumns;

    private String[] minimumColumns;

    public ColumnContextController() {
    }

    @PostConstruct
    public void loadColumns() {
       String colsCookie = readCookie(COOKIE_COLS_NAME);

        if (colsCookie != null) {
            if (COOKIE_MINIMAL_VALUE.equals(colsCookie)) {
                selectMininumColumns();
            } else if (COOKIE_BASIC_VALUE.equals(colsCookie)) {
                selectBasicColumns();
            } else if (COOKIE_STANDARD_VALUE.equals(colsCookie)) {
                selectStandardColumns();
            } else if (COOKIE_EXPANDED_VALUE.equals(colsCookie)) {
                selectExpandedColumns();
            }
            else if (COOKIE_COMPLETE_VALUE.equals(colsCookie)) {
                selectCompleteColumns();
            }
        } else {
			selectBasicColumns();
        }
    }

    private void writeCookie(String name, String value) {
        final ExternalContext externalContext = FacesContext.getCurrentInstance().getExternalContext();

        Cookie cookie = new Cookie(name, value);
        cookie.setMaxAge(3600 * 24 * 200);

        HttpServletResponse response = (HttpServletResponse) externalContext.getResponse();
        response.addCookie(cookie);
    }

    private String readCookie(String name) {
        final ExternalContext externalContext = FacesContext.getCurrentInstance().getExternalContext();
        HttpServletRequest request = (HttpServletRequest) externalContext.getRequest();

        if (request.getCookies() != null) {
            for (Cookie cookie : request.getCookies()) {
                if (name.equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }

        return null;
    }

    private String[] getSimpleColumns() {
        if (this.simpleColumns == null){
            simpleColumns = new String[] {
                    MOLECULE_A_NAME, MOLECULE_A_LINKS, MOLECULE_B_NAME, MOLECULE_B_LINKS,
                    MOLECULE_A_SPECIES, MOLECULE_B_SPECIES,INTERACTION_TYPE, CONFIDENCE_VALUE, PUBMED_IDENTIFIER,
                    INTERACTION_DETECTION_METHOD, SOURCE_DATABASE, INTERACTION_AC, EXPANSION_METHOD
            };
        }
        return simpleColumns;
    }

    private String[] getBasicColumns() {
        if (basicColumns == null){
            basicColumns =new String[] {
                    MOLECULE_A_NAME, MOLECULE_A_LINKS, MOLECULE_B_NAME, MOLECULE_B_LINKS,
                    INTERACTION_DETECTION_METHOD, SOURCE_DATABASE, INTERACTION_AC
            };
        }
        return basicColumns;
    }

    private String[] getExpandedColumns() {
        if (expandedColumns == null){
            expandedColumns =new String[] {
                    MOLECULE_A_NAME, MOLECULE_A_LINKS, MOLECULE_B_NAME, MOLECULE_B_LINKS,
                    MOLECULE_A_SPECIES, MOLECULE_B_SPECIES,INTERACTION_TYPE, SOURCE_DATABASE, CONFIDENCE_VALUE, PUBMED_IDENTIFIER,
                    INTERACTION_DETECTION_METHOD, INTERACTION_AC, EXPANSION_METHOD, MOLECULE_A_BIOLOGICAL_ROLE, MOLECULE_B_BIOLOGICAL_ROLE,
                    MOLECULE_A_EXPERIMENTAL_ROLE, MOLECULE_B_EXPERIMENTAL_ROLE, MOLECULE_A_INTERACTOR_TYPE, MOLECULE_B_INTERACTOR_TYPE,
                    MOLECULE_A_XREFS, MOLECULE_B_XREFS, INTERACTION_XREFS,
                    HOST_ORGANISM, PARAMETERS_INTERACTION, NEGATIVE, MOLECULE_A_FEATURE, MOLECULE_B_FEATURE, MOLECULE_A_STOICHIOMETRY, MOLECULE_B_STOICHIOMETRY, MOLECULE_A_IDENTIFICATION,
                    MOLECULE_B_IDENTIFICATION
            };
        }
        return expandedColumns;
    }

    private String[] getCompleteColumns() {
        if (completeColumns == null){
            completeColumns = new String[] {
                    MOLECULE_A_ID, MOLECULE_B_ID, MOLECULE_A_ALTID, MOLECULE_B_ALTID,
                    MOLECULE_A_ALIASES, MOLECULE_B_ALIASES,INTERACTION_DETECTION_METHOD, FIRST_AUTHOR, PUBMED_IDENTIFIER,
                    MOLECULE_A_SPECIES, MOLECULE_B_SPECIES, INTERACTION_TYPE, SOURCE_DATABASE, INTERACTION_AC,
                    CONFIDENCE_VALUE, EXPANSION_METHOD, MOLECULE_A_BIOLOGICAL_ROLE, MOLECULE_B_BIOLOGICAL_ROLE,
                    MOLECULE_A_EXPERIMENTAL_ROLE, MOLECULE_B_EXPERIMENTAL_ROLE, MOLECULE_A_INTERACTOR_TYPE, MOLECULE_B_INTERACTOR_TYPE, MOLECULE_A_XREFS, MOLECULE_B_XREFS,
                    INTERACTION_XREFS, MOLECULE_A_ANNOTATION, MOLECULE_B_ANNOTATION, INTERACTION_ANNOTATION, HOST_ORGANISM, PARAMETERS_INTERACTION, CREATED_DATE,
                    UPDATE_DATE, MOLECULE_A_CHECKSUM, MOLECULE_B_CHECKSUM, INTERACTION_CHECKSUM, NEGATIVE, MOLECULE_A_FEATURE, MOLECULE_B_FEATURE,
                    MOLECULE_A_STOICHIOMETRY, MOLECULE_B_STOICHIOMETRY, MOLECULE_A_IDENTIFICATION, MOLECULE_B_IDENTIFICATION
            };
        }
        return completeColumns;
    }

    private String[] getMinimumColumns() {
        if (minimumColumns == null){
           minimumColumns = new String[] { MOLECULE_A_NAME, MOLECULE_B_NAME, SOURCE_DATABASE, INTERACTION_AC};
        }
        return minimumColumns;
    }

    public void selectStandardColumns() {
        selectedColumns = getSimpleColumns();
        writeCookie(COOKIE_COLS_NAME, COOKIE_STANDARD_VALUE);
    }

    public void selectExpandedColumns() {
        selectedColumns = getExpandedColumns();
        writeCookie(COOKIE_COLS_NAME, COOKIE_EXPANDED_VALUE);
    }

    public void selectCompleteColumns() {
        selectedColumns = getCompleteColumns();
        writeCookie(COOKIE_COLS_NAME, COOKIE_COMPLETE_VALUE);
    }

    public void selectBasicColumns() {
        this.selectedColumns = getBasicColumns();
        writeCookie(COOKIE_COLS_NAME, COOKIE_BASIC_VALUE);
    }

    public void selectMininumColumns() {
        this.selectedColumns = getMinimumColumns();
        writeCookie(COOKIE_COLS_NAME, COOKIE_MINIMAL_VALUE);
    }

    public boolean isColumnVisible(String columnKey) {
        return ArrayUtils.contains(selectedColumns, columnKey);
    }

    private List<SelectItem> createSelectItems() {
        ResourceBundle rb = ResourceBundle.getBundle( "uk.ac.ebi.intact.Messages" );

        List<SelectItem> selectItems = new ArrayList<SelectItem>();

        // add molecule name and links, then relies on complete columns of mitab 2.7
        selectItems.add( new SelectItem(MOLECULE_A_NAME, rb.getString(MOLECULE_A_NAME).trim()) );
        selectItems.add( new SelectItem(MOLECULE_A_LINKS, rb.getString(MOLECULE_A_LINKS).trim()) );
        selectItems.add( new SelectItem(MOLECULE_B_NAME, rb.getString(MOLECULE_B_NAME).trim()) );
        selectItems.add( new SelectItem(MOLECULE_B_LINKS, rb.getString(MOLECULE_B_LINKS).trim()) );

        for ( String columnKey : getCompleteColumns()) {
            selectItems.add( new SelectItem(columnKey, rb.getString(columnKey).trim()) );
        }

        return selectItems;
    }

    public String[] getSelectedColumns() {
        return selectedColumns;
    }

    public void setSelectedColumns(String[] selectedColumns) {
        if (selectedColumns != null && selectedColumns.length > 0) {
            this.selectedColumns = selectedColumns;
        }
    }

    public List<SelectItem> getColumnsSelectItems() {
        if (columnsSelectItems == null) {
            columnsSelectItems = createSelectItems();
        }
        return columnsSelectItems;
    }
}
