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
import uk.ac.ebi.intact.view.webapp.controller.JpaBaseController;
import uk.ac.ebi.intact.core.persistence.dao.CvObjectDao;

import javax.faces.context.FacesContext;

/**
 * CvObject dialog controller.
 *
 * @author Samuel Kerrien (skerrien@ebi.ac.uk)
 * @version $Id$
 * @since 1.6.0
 */
@Controller( "cvBean" )
@Scope( "conversation.access" )
@ConversationName( "general" )
public class CvObjectDialogController extends JpaBaseController {

    private static final Log log = LogFactory.getLog( CvObjectDialogController.class );

    private CvObject cvObject;

    public CvObject getCvObject() {
        return cvObject;
    }

    public void setCvObject( CvObject cvObject ) {
        this.cvObject = cvObject;
    }

    public void setObjectAc( String ac ) {
        if ( log.isDebugEnabled() ) {
            log.debug( "Calling setObjectAc( '"+ ac +"' )..." );
        }
        cvObject = getDaoFactory().getCvObjectDao().getByAc( ac );
        if( cvObject == null ) {
            addErrorMessage( "No CvObject found in the database for ac: " + ac, "" );
        }
    }

    public void setIdentifier (String id) {
        CvObjectDao cvObjectDao;

        if (id.contains("@")) {
            String[] tokens = id.split("@");
            String className = tokens[0];
            id = tokens[1];

            Class type = null;
            try {
                type = Class.forName(className);
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
                addErrorMessage("No cv object class of type", type.toString());
            }
            cvObjectDao = getDaoFactory().getCvObjectDao(type);
        } else {
            cvObjectDao = getDaoFactory().getCvObjectDao();
        }

        cvObject = cvObjectDao.getByPsiMiRef( id );
        if( cvObject == null ) {
            addErrorMessage( "No CvObject found in the database with identifier: " + id, "" );
        }
    }

    public String getClassName() {
        return cvObject.getClass().getSimpleName();
    }
}