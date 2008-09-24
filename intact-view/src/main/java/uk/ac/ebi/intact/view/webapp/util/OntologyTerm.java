/**
 * Copyright 2008 The European Bioinformatics Institute, and others.
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

/**
 * TODO comment that class header
*
* @author Bruno Aranda (baranda@ebi.ac.uk)
* @version $Id$
*/
public class OntologyTerm {

    private String identifier;
    private String label;
    private String databaseLabel;

    public OntologyTerm(String identifier, String label, String databaseLabel) {
        this.identifier = identifier;
        this.label = label;
        this.databaseLabel = databaseLabel;
    }

    public String getIdentifier() {
        return identifier;
    }

    public String getLabel() {
        return label;
    }

    public String getDatabaseLabel() {
        return databaseLabel;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append("OntologyTerm");
        sb.append("{databaseLabel='").append(databaseLabel).append('\'');
        sb.append(", identifier='").append(identifier).append('\'');
        sb.append(", label='").append(label).append('\'');
        sb.append('}');
        return sb.toString();
    }
}
