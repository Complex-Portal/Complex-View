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
package uk.ac.ebi.intact.view.webapp.controller.details.complex;

import com.google.common.collect.Lists;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;
import uk.ac.ebi.intact.view.webapp.controller.BaseController;

import java.util.Collection;
import java.util.List;

/**
 * Holds the labels to be displayed in the table header of the similar interaction search result page.
 *
 * @author Samuel Kerrien (skerrien@ebi.ac.uk)
 * @version $Id$
 * @since 1.6.0
 */
@Controller( "tableHeaderBean" )
@Scope("session")
public class TableHeaderController extends BaseController {

    private List<String> tableHeaderLabels;

    public TableHeaderController() {
    }

    public void setLabels( Collection<SimpleInteractor> interactors ) {
        tableHeaderLabels = Lists.newArrayListWithCapacity( interactors.size() );
        for ( SimpleInteractor interactor : interactors ) {
            tableHeaderLabels.add( interactor.getShortLabel() );
        }
    }

    public String[] getLabelArray() {
        return tableHeaderLabels.toArray( new String[]{} );
    }
}