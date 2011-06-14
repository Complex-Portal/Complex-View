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
public class ColumnContextController {

    private static final Log log = LogFactory.getLog( ColumnContextController.class );

    private static String COOKIE_COLS_NAME = "intact.cols.view";
    private static String COOKIE_SHOWICONS_NAME = "intact.icons.show";

    private static String COOKIE_MINIMAL_VALUE = "min cols";
    private static String COOKIE_BASIC_VALUE = "basic cols";
    private static String COOKIE_STANDARD_VALUE = "std cols";
    private static String COOKIE_EXPANDED_VALUE = "ext cols";

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

    private boolean showTypeRoleIcons;

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
        } else {
            selectStandardColumns();
        }

        String showIconsCookie = readCookie(COOKIE_SHOWICONS_NAME);

        if (showIconsCookie != null) {
            showTypeRoleIcons = Boolean.valueOf(showIconsCookie);
        } else {
            setShowIcons(true);
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

        for (Cookie cookie : request.getCookies()) {
            if (name.equals(cookie.getName())) {
                return cookie.getValue();
            }
        }

        return null;
    }

    private String[] getSimpleColumns() {
        return new String[] {
            MOLECULE_A_NAME, MOLECULE_A_LINKS, MOLECULE_B_NAME, MOLECULE_B_LINKS, MOLECULE_A_ALIASES, MOLECULE_B_ALIASES, MOLECULE_A_SPECIES, MOLECULE_B_SPECIES,
                FIRST_AUTHOR, PUBMED_IDENTIFIER, INTERACTION_DETECTION_METHOD, INTERACTION_AC, EXPANSION_METHOD
        };
    }

    private String[] getBasicColumns() {
        return new String[] { MOLECULE_A_NAME, MOLECULE_A_LINKS, MOLECULE_B_NAME, MOLECULE_B_LINKS, INTERACTION_DETECTION_METHOD, INTERACTION_AC};
    }

    private String[] getExpandedColumns() {
        String[] allColumns = getSimpleColumns();

        return (String[]) ArrayUtils.addAll(allColumns, new String[] {
                FIRST_AUTHOR, INTERACTION_TYPE, SOURCE_DATABASE, CONFIDENCE_VALUE, MOLECULE_A_EXPERIMENTAL_ROLE, MOLECULE_B_EXPERIMENTAL_ROLE, MOLECULE_A_BIOLOGICAL_ROLE,
                MOLECULE_B_BIOLOGICAL_ROLE, MOLECULE_A_PROPERTIES, MOLECULE_B_PROPERTIES, MOLECULE_A_INTERACTOR_TYPE, MOLECULE_B_INTERACTOR_TYPE, HOST_ORGANISM, DATASET
        });
    }

    private String[] getMinimumColumns() {
        return new String[] { MOLECULE_A_NAME, MOLECULE_B_NAME, INTERACTION_AC};
    }

    public void selectStandardColumns() {
        selectedColumns = getSimpleColumns();
        writeCookie(COOKIE_COLS_NAME, COOKIE_STANDARD_VALUE);
    }

    public void selectExpandedColumns() {
        selectedColumns = getExpandedColumns();
        writeCookie(COOKIE_COLS_NAME, COOKIE_EXPANDED_VALUE);
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

        for ( String columnKey : getExpandedColumns()) {
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

    public boolean isShowIcons() {
        return showTypeRoleIcons;
    }

    public void setShowIcons(boolean showTypeRoleIcons) {
        this.showTypeRoleIcons = showTypeRoleIcons;

        writeCookie(COOKIE_SHOWICONS_NAME, String.valueOf(showTypeRoleIcons));
    }
}
