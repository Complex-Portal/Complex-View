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
package uk.ac.ebi.intact.editor.controller.curate;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;
import uk.ac.ebi.intact.editor.controller.BaseController;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 */
@Controller
@Scope("session")
public class UnsavedChangeManagerController extends BaseController {

    private Map<String,UnsavedChangeManager> unsavedMap;

    public UnsavedChangeManagerController() {
        this.unsavedMap = new HashMap<String, UnsavedChangeManager>();
    }

    public UnsavedChangeManager getUnsavedChangeManager(String ac) {
        if (unsavedMap.containsKey(ac)) {
            return unsavedMap.get(ac);
        }

        UnsavedChangeManager unsavedChangeManager = new UnsavedChangeManager();

        if (ac != null) {
            unsavedMap.put(ac, unsavedChangeManager);
        }

        return unsavedChangeManager; 
    }
}
