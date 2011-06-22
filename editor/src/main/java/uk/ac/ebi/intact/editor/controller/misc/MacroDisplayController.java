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
package uk.ac.ebi.intact.editor.controller.misc;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;
import uk.ac.ebi.intact.editor.controller.JpaAwareController;

import javax.faces.event.ComponentSystemEvent;

/**
 *
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 */
@Controller
@Scope( "session" )
public class MacroDisplayController extends JpaAwareController {

    @Autowired
    private MyNotesController myNotesController;

    private String macroName;
    private QueryMacro queryMacro;

    public MacroDisplayController() {
    }

    public void load(ComponentSystemEvent evt) {
        if (macroName == null) {
            throw new IllegalStateException("Nothing to load for macro: "+macroName);
        }

        for (QueryMacro macro : myNotesController.getQueryMacros()) {
            if (macroName.equals(macro.getName())) {
                queryMacro = macro;
                break;
            }
        }

        if (queryMacro == null) {
            throw new IllegalArgumentException("Macro not found: "+macroName);
        }
    }

    public String getMacroName() {
        return macroName;
    }

    public void setMacroName(String macroName) {
        this.macroName = macroName;
    }

    public QueryMacro getQueryMacro() {
        return queryMacro;
    }

    public void setQueryMacro(QueryMacro queryMacro) {
        this.queryMacro = queryMacro;
    }
}
