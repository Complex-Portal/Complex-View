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
package uk.ac.ebi.intact.view.webapp.controller.details;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.myfaces.orchestra.conversation.annotations.ConversationName;
import org.apache.myfaces.orchestra.viewController.annotations.PreRenderView;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;
import uk.ac.ebi.intact.model.Interaction;
import uk.ac.ebi.intact.model.CvObject;
import uk.ac.ebi.intact.model.BioSource;
import uk.ac.ebi.intact.view.webapp.controller.JpaBaseController;

import javax.faces.context.FacesContext;

/**
 * BioSource dialog controller.
 *
 * @author Samuel Kerrien (skerrien@ebi.ac.uk)
 * @version $Id$
 * @since 1.6.0
 */
@Controller( "biosourceBean" )
@Scope( "conversation.access" )
@ConversationName( "general" )
public class BioSourceDialogController extends JpaBaseController {

    private static final Log log = LogFactory.getLog( BioSourceDialogController.class );

    private BioSource bioSource;

    public BioSource getBioSource() {
        return bioSource;
    }

    public void setBioSource( BioSource bioSource ) {
        this.bioSource = bioSource;
    }

    public void setObjectAc( String ac ) {
        bioSource = getDaoFactory().getBioSourceDao().getByAc( ac );
        if( bioSource == null ) {
            addErrorMessage( "No CvObject found in the database for ac: " + ac, "" );
        }
    }
}