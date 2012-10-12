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
package uk.ac.ebi.intact.view.webapp.controller;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 */
@Controller
@Scope("currentSearch")
public class ContextController extends BaseController {

    private int activeTabIndex;
    private int interactorActiveTabIndex;

    private Map<Integer,Boolean> loadedTabStatus;

    public ContextController() {
        loadedTabStatus = new HashMap<Integer, Boolean>();
    }

    public void clearLoadedTabs() {
        loadedTabStatus.clear();
    }

    public int getActiveTabIndex() {
        return activeTabIndex;
    }

    public void setActiveTabIndex(int activeTabIndex) {
        this.activeTabIndex = activeTabIndex;
        setTabLoaded(activeTabIndex);
    }

    public int getInteractorActiveTabIndex() {
        return interactorActiveTabIndex;
    }

    public void setInteractorActiveTabIndex(int interactorActiveTabIndex) {
        this.interactorActiveTabIndex = interactorActiveTabIndex;
    }

    public boolean isTabLoaded(int i) {
        if (loadedTabStatus.containsKey(i)) {
            return loadedTabStatus.get(i);
        } else {
            loadedTabStatus.put(i, false);
            return false;
        }
    }

    public void setTabLoaded(int i) {
        loadedTabStatus.put(i, true);
    }

    public Map<Integer, Boolean> getLoadedTabStatus() {
        return loadedTabStatus;
    }
}
