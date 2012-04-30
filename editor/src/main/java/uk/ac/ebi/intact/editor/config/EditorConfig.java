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

import uk.ac.ebi.intact.core.annotations.PersistentConfiguration;
import uk.ac.ebi.intact.core.annotations.PersistentProperty;
import uk.ac.ebi.intact.editor.controller.BaseController;

/**
 * Holds the configuration of the application.
 *
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 */
@PersistentConfiguration
public class EditorConfig extends BaseController {

    private String buildVersion;
    private String buildTimestamp;

    @PersistentProperty
    private String instanceName;
    private String databaseUrl;
    private String usersDatabaseUrl;

    @PersistentProperty
    private String logoUrl;

    @PersistentProperty
    private String googleUsername;

    @PersistentProperty
    private String googlePassword;

    @PersistentProperty(defaultValue = "0")
    private float defaultStoichiometry;

    @PersistentProperty(defaultValue = "imex curation")
    private String defaultCurationDepth;

    @PersistentProperty(defaultValue = "5")
    private int revertDecisionTime;

    @PersistentProperty(defaultValue = "ebi")
    private String theme;
    
    private String imexCentralUsername;
    private String imexCentralPassword;
    private String imexCentralEndPoint;

    public EditorConfig() {
    }

    public String getBuildVersion() {
        return buildVersion;
    }

    public void setBuildVersion(String buildVersion) {
        this.buildVersion = buildVersion;
    }

    public String getBuildTimestamp() {
        return buildTimestamp;
    }

    public void setBuildTimestamp(String buildTimestamp) {
        this.buildTimestamp = buildTimestamp;
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

    public String getGoogleUsername() {
        return googleUsername;
    }

    public void setGoogleUsername(String googleUsername) {
        this.googleUsername = googleUsername;
    }

    public String getGooglePassword() {
        return googlePassword;
    }

    public void setGooglePassword(String googlePassword) {
        this.googlePassword = googlePassword;
    }

    public String getUsersDatabaseUrl() {
        return usersDatabaseUrl;
    }

    public void setUsersDatabaseUrl(String usersDatabaseUrl) {
        this.usersDatabaseUrl = usersDatabaseUrl;
    }

    public String getDefaultCurationDepth() {
        return defaultCurationDepth;
    }

    public void setDefaultCurationDepth(String defaultCurationDepth) {
        this.defaultCurationDepth = defaultCurationDepth;
    }

    public int getRevertDecisionTime() {
        return revertDecisionTime;
    }

    public void setRevertDecisionTime(int revertDecisionTime) {
        this.revertDecisionTime = revertDecisionTime;
    }

    public String getTheme() {
        return theme;
    }

    public void setTheme(String theme) {
        this.theme = theme;
    }

    public String getImexCentralUsername() {
        return imexCentralUsername;
    }

    public void setImexCentralUsername(String imexCentralUsername) {
        this.imexCentralUsername = imexCentralUsername;
    }

    public String getImexCentralPassword() {
        return imexCentralPassword;
    }

    public void setImexCentralPassword(String imexCentralPassword) {
        this.imexCentralPassword = imexCentralPassword;
    }

    public String getImexCentralEndPoint() {
        return imexCentralEndPoint;
    }

    public void setImexCentralEndPoint(String imexCentralEndPoint) {
        this.imexCentralEndPoint = imexCentralEndPoint;
    }
}
