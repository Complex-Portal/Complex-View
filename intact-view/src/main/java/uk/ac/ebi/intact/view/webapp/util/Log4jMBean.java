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
package uk.ac.ebi.intact.view.webapp.util;

import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.springframework.stereotype.Component;

/**
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 */
@Component
public class Log4jMBean {

    public void activateInfo(String category) {
        LogManager.getLogger(category).setLevel(Level.INFO);
    }

    public void activateDebug(String category) {
        LogManager.getLogger(category).setLevel(Level.DEBUG);
    }

    public void activateWarn(String category) {
        LogManager.getLogger(category).setLevel(Level.WARN);
    }

    public void activateError(String category) {
        LogManager.getLogger(category).setLevel(Level.ERROR);
    }

    public void activateFatal(String category) {
        LogManager.getLogger(category).setLevel(Level.FATAL);
    }
}

