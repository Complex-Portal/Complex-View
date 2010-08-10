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
package uk.ac.ebi.intact.editor.config;

/**
 * Holds the configuration of the application.
 *
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 */
public class EditorConfig {

    private String instanceName;
    private String databaseUrl;
    private String logoUrl;

    private float defaultStoichiometry = 0;

    public EditorConfig() {
    }

    public String getInstanceName() {
        return instanceName;
    }

    public void setInstanceName( String instanceName ) {
        this.instanceName = instanceName;
    }

    public String getDatabaseUrl() {
        return databaseUrl;
    }

    public void setDatabaseUrl( String databaseUrl ) {
        this.databaseUrl = databaseUrl;
    }

    public String getLogoUrl() {
        return logoUrl;
    }

    public void setLogoUrl( String logoUrl ) {
        this.logoUrl = logoUrl;
    }

    public float getDefaultStoichiometry() {
        return defaultStoichiometry;
    }

    public void setDefaultStoichiometry(float defaultStoichiometry) {
        this.defaultStoichiometry = defaultStoichiometry;
    }
}
