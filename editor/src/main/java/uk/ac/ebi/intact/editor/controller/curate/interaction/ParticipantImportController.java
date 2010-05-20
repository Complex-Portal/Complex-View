/**
 * Copyright 2010 The European Bioinformatics Institute, and others.
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
package uk.ac.ebi.intact.editor.controller.curate.interaction;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.myfaces.orchestra.conversation.annotations.ConversationName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;
import uk.ac.ebi.intact.editor.controller.BaseController;
import uk.ac.ebi.intact.uniprot.model.UniprotProtein;
import uk.ac.ebi.intact.uniprot.service.UniprotRemoteService;

import javax.faces.event.ActionEvent;
import java.util.Collection;

/**
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 */
@Controller
@Scope( "conversation.access" )
@ConversationName( "general" )
public class ParticipantImportController extends BaseController {

    private static final Log log = LogFactory.getLog( ParticipantImportController.class );

    @Autowired
    private UniprotRemoteService uniprotRemoteService;

    private String[] participantsToImport;

    public void importParticipants( ActionEvent evt ) {
        for (String participantToImport : participantsToImport) {
            log.debug( "Importing participant: "+ participantToImport );

            final Collection<UniprotProtein> candidates = uniprotRemoteService.retrieve(participantToImport);

            System.out.println("Candidates in uniprot: "+candidates.size());
        }

    }

    public String[] getParticipantsToImport() {
        return participantsToImport;
    }

    public void setParticipantsToImport(String[] participantsToImport) {
        this.participantsToImport = participantsToImport;
    }
}