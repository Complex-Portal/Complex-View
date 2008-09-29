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
package uk.ac.ebi.intact.view.webapp.controller.config;

import org.springframework.stereotype.Controller;
import org.springframework.beans.factory.annotation.Autowired;

import javax.persistence.EntityManagerFactory;

/**
 * TODO comment that class header
 *
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 */
public class IntactViewConfiguration {

    @Autowired
    private EntityManagerFactory entityManagerFactory;

    private String appRoot;
    private String menuUrl;
    private String newsUrl;
    private int maxNewsInBox;
    private String configFile;
    private String defaultIndexLocation;
    private String defaultInteractorIndexLocation;
    private String hierarchViewUrl;
    private String hierarchViewImageUrl;
    private String hierarchViewMaxInteractions;
    private String chebiUrl;
    private String chebiChemicalSearchPath;
    private String intactSecret;
    private int luceneMaxCombinations;
    private boolean doNotUseOls;
    private String googleAnalyticsTracker;
    private int maxOntologySuggestions;
    private String mailRecipients;
    private String webappName;
    private String webappLogoUrl;
    private String webappVersion;
    private String webappBuildNumber;

    public IntactViewConfiguration() {
    }


    public void closeEntityManagerFactory() {
        entityManagerFactory.close();
    }

    public String getAppRoot() {
        return appRoot;
    }

    public void setAppRoot(String appRoot) {
        this.appRoot = appRoot;
    }

    public String getMenuUrl() {
        return menuUrl;
    }

    public void setMenuUrl(String menuUrl) {
        this.menuUrl = menuUrl;
    }

    public String getNewsUrl() {
        return newsUrl;
    }

    public void setNewsUrl(String newsUrl) {
        this.newsUrl = newsUrl;
    }

    public int getMaxNewsInBox() {
        return maxNewsInBox;
    }

    public void setMaxNewsInBox(int maxNewsInBox) {
        this.maxNewsInBox = maxNewsInBox;
    }

    public String getConfigFile() {
        return configFile;
    }

    public void setConfigFile(String configFile) {
        this.configFile = configFile;
    }

    public String getDefaultIndexLocation() {
        return defaultIndexLocation;
    }

    public void setDefaultIndexLocation(String defaultIndexLocation) {
        this.defaultIndexLocation = defaultIndexLocation;
    }

    public String getDefaultInteractorIndexLocation() {
        return defaultInteractorIndexLocation;
    }

    public void setDefaultInteractorIndexLocation(String defaultInteractorIndexLocation) {
        this.defaultInteractorIndexLocation = defaultInteractorIndexLocation;
    }

    public String getHierarchViewUrl() {
        return hierarchViewUrl;
    }

    public void setHierarchViewUrl(String hierarchViewUrl) {
        this.hierarchViewUrl = hierarchViewUrl;
    }

    public String getHierarchViewImageUrl() {
        return hierarchViewImageUrl;
    }

    public void setHierarchViewImageUrl(String hierarchViewImageUrl) {
        this.hierarchViewImageUrl = hierarchViewImageUrl;
    }

    public String getHierarchViewMaxInteractions() {
        return hierarchViewMaxInteractions;
    }

    public void setHierarchViewMaxInteractions(String hierarchViewMaxInteractions) {
        this.hierarchViewMaxInteractions = hierarchViewMaxInteractions;
    }

    public String getChebiUrl() {
        return chebiUrl;
    }

    public void setChebiUrl(String chebiUrl) {
        this.chebiUrl = chebiUrl;
    }

    public String getChebiChemicalSearchPath() {
        return chebiChemicalSearchPath;
    }

    public void setChebiChemicalSearchPath(String chebiChemicalSearchPath) {
        this.chebiChemicalSearchPath = chebiChemicalSearchPath;
    }

    public String getIntactSecret() {
        return intactSecret;
    }

    public void setIntactSecret(String intactSecret) {
        this.intactSecret = intactSecret;
    }

    public int getLuceneMaxCombinations() {
        return luceneMaxCombinations;
    }

    public void setLuceneMaxCombinations(int luceneMaxCombinations) {
        this.luceneMaxCombinations = luceneMaxCombinations;
    }

    public String getGoogleAnalyticsTracker() {
        return googleAnalyticsTracker;
    }

    public void setGoogleAnalyticsTracker(String googleAnalyticsTracker) {
        this.googleAnalyticsTracker = googleAnalyticsTracker;
    }

    public int getMaxOntologySuggestions() {
        return maxOntologySuggestions;
    }

    public void setMaxOntologySuggestions(int maxOntologySuggestions) {
        this.maxOntologySuggestions = maxOntologySuggestions;
    }

    public void setMailRecipients(String mailRecipients) {this.mailRecipients = mailRecipients;}

    public String getMailRecipients() { return mailRecipients; }

    public void setWebappName(String webappName) {this.webappName = webappName;}

    public String getWebappName() { return webappName; }

    public void setWebappLogoUrl(String webappLogoUrl) {this.webappLogoUrl = webappLogoUrl;}

    public String getWebappLogoUrl() { return webappLogoUrl; }

    public void setWebappVersion(String webappVersion) {this.webappVersion = webappVersion;}

    public String getWebappVersion() { return webappVersion; }

    public void setWebappBuildNumber(String webappBuildNumber) {this.webappBuildNumber = webappBuildNumber;}

    public String getWebappBuildNumber() { return webappBuildNumber; }
}
