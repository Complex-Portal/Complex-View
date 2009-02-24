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

import org.springframework.beans.factory.annotation.Autowired;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.httpclient.*;
import org.apache.commons.httpclient.params.HttpClientParams;
import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.impl.CommonsHttpSolrServer;
import uk.ac.ebi.intact.view.webapp.controller.BaseController;
import uk.ac.ebi.intact.view.webapp.IntactViewException;

import javax.persistence.EntityManagerFactory;
import javax.annotation.PostConstruct;
import java.io.*;
import java.util.Properties;
import java.net.MalformedURLException;

/**
 * IntactView configuration bean.
 *
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 */
public class IntactViewConfiguration extends BaseController {

    private static final Log log = LogFactory.getLog( IntactViewConfiguration.class );

    private static final String WEBAPP_NAME = "webapp.name";
    private static final String WEBAPP_LOGO_URL = "webapp.logo.url";
    private static final String INTACT_VIEW_APPROOT = "intact.view.approot";
    private static final String INTACT_MENU_URL = "intact.menu.url";
    private static final String INTACT_NEWS_URL = "intact.news.url";
    private static final String INTACT_NEWS_MAXINBOX = "intact.news.maxinbox";
    private static final String INTACT_CONFIGFILE = "intact.configfile";
    private static final String INTACT_GRAPH_MAX_INTERACTION_COUNT = "intact.graph.maxInteractionCount";
    private static final String INTACT_SOLR_INTERACTIONS_URL = "intact.solr.interactions.url";
    private static final String INTACT_SOLR_ONTOLOGIES_URL = "intact.solr.ontologies.url";
    private static final String INTACT_HIERARCHVIEW_URL = "intact.hierarchview.url";
    private static final String INTACT_HIERARCHVIEW_IMAGEURL = "intact.hierarchview.imageurl";
    private static final String INTACT_HIERARCHVIEW_MAXINTERACTIONS = "intact.hierarchview.maxinteractions";
    private static final String INTACT_CHEBI_URL = "intact.chebi.url";
    private static final String INTACT_CHEBI_SEARCH_PATH = "intact.chebi.search.path";
    private static final String INTACT_DASTY_URL = "intact.dasty.url";
    private static final String INTACT_SECRET = "intact.secret";
    private static final String INTACT_SEARCH_ONTOLOGIES_MAXSUGGESTIONS = "intact.search.ontologies.maxsuggestions";
    private static final String INTACT_GOOGLE_ANALYTICS_TRACKER = "intact.google.analytics.tracker";
    private static final String INTACT_RECIPIENTS = "intact.mail.recipients";
    private static final String PROXY_HOST = "intact.proxy.host";
    private static final String PROXY_PORT = "intact.proxy.port";

    @Autowired
    private EntityManagerFactory entityManagerFactory;

    private String appRoot;
    private String menuUrl;
    private String newsUrl;
    private int maxNewsInBox;
    private String warningMessage;
    private String configFile;
    private int graphMaxInteractionCount;
    private String solrInteractionsUrl;
    private String solrOntologiesUrl;
    private String hierarchViewUrl;
    private String hierarchViewImageUrl;
    private String hierarchViewMaxInteractions;
    private String chebiUrl;
    private String chebiChemicalSearchPath;
    private String intactSecret;
    private String googleAnalyticsTracker;
    private int maxOntologySuggestions;
    private String mailRecipients;
    private String webappName;
    private String webappLogoUrl;
    private String webappVersion;
    private String webappBuildNumber;
    private String dastyUrl;
    private String proxyHost;
    private String proxyPort;

    public IntactViewConfiguration() {
    }

    @PostConstruct
    public void storeIfNew() {
        if (new File(configFile).exists()) {
            try {
                loadConfiguration(new FileInputStream(configFile));
            } catch (IOException e) {
                throw new IntactViewException("Problem loading config properties from: "+configFile, e);
            }
        } else {
            if (log.isInfoEnabled()) log.info("Properties file not found at: "+configFile);
            try {
                storeConfiguration();
            } catch (IOException e) {
                throw new IntactViewException("Problem storing config properties for the first time at: "+configFile, e);
            }
        }
    }

    public void loadConfiguration(InputStream is) throws IOException {
        if (log.isInfoEnabled()) log.info("Loading properties from: "+configFile);

        Properties properties = new Properties();
        properties.load(is);

        webappName = properties.getProperty(WEBAPP_NAME, webappName);
        webappLogoUrl = properties.getProperty(WEBAPP_LOGO_URL, webappLogoUrl);
        appRoot = properties.getProperty(INTACT_VIEW_APPROOT, appRoot);
        menuUrl = properties.getProperty(INTACT_MENU_URL, menuUrl);
        newsUrl = properties.getProperty(INTACT_NEWS_URL, newsUrl);
        maxNewsInBox = Integer.parseInt(properties.getProperty(INTACT_NEWS_MAXINBOX, String.valueOf(maxNewsInBox)));
        configFile = properties.getProperty(INTACT_CONFIGFILE, configFile);
        graphMaxInteractionCount = Integer.parseInt(properties.getProperty(INTACT_GRAPH_MAX_INTERACTION_COUNT, String.valueOf(graphMaxInteractionCount)));
        solrInteractionsUrl = properties.getProperty(INTACT_SOLR_INTERACTIONS_URL, solrInteractionsUrl);
        solrOntologiesUrl = properties.getProperty(INTACT_SOLR_ONTOLOGIES_URL, solrOntologiesUrl);
        hierarchViewUrl = properties.getProperty(INTACT_HIERARCHVIEW_URL, hierarchViewUrl);
        hierarchViewImageUrl = properties.getProperty(INTACT_HIERARCHVIEW_IMAGEURL, hierarchViewImageUrl);
        hierarchViewMaxInteractions = properties.getProperty(INTACT_HIERARCHVIEW_MAXINTERACTIONS, hierarchViewMaxInteractions);
        chebiUrl = properties.getProperty(INTACT_CHEBI_URL, chebiUrl);
        chebiChemicalSearchPath = properties.getProperty(INTACT_CHEBI_SEARCH_PATH, chebiChemicalSearchPath);
        dastyUrl = properties.getProperty(INTACT_DASTY_URL, dastyUrl);
        intactSecret = properties.getProperty(INTACT_SECRET, intactSecret);
        maxOntologySuggestions = Integer.parseInt(properties.getProperty(INTACT_SEARCH_ONTOLOGIES_MAXSUGGESTIONS, String.valueOf(maxOntologySuggestions)));
        googleAnalyticsTracker = properties.getProperty(INTACT_GOOGLE_ANALYTICS_TRACKER, googleAnalyticsTracker);
        mailRecipients = properties.getProperty(INTACT_RECIPIENTS, mailRecipients);
        proxyHost = properties.getProperty(PROXY_HOST, proxyHost);
        proxyPort = properties.getProperty(PROXY_PORT, proxyPort);
    }

    public void storeConfiguration() throws IOException {
        if (log.isInfoEnabled()) log.info("Storing properties at: "+configFile);

        Properties properties = new Properties();
        addProperty(properties, WEBAPP_NAME, webappName);
        addProperty(properties, WEBAPP_LOGO_URL, webappLogoUrl);
        addProperty(properties, INTACT_VIEW_APPROOT, appRoot);
        addProperty(properties, INTACT_MENU_URL, menuUrl);
        addProperty(properties, INTACT_NEWS_URL, newsUrl);
        addProperty(properties, INTACT_NEWS_MAXINBOX, String.valueOf(maxNewsInBox));
        addProperty(properties, INTACT_CONFIGFILE, configFile);
        addProperty(properties, INTACT_GRAPH_MAX_INTERACTION_COUNT, String.valueOf(graphMaxInteractionCount));
        addProperty(properties, INTACT_SOLR_INTERACTIONS_URL, solrInteractionsUrl);
        addProperty(properties, INTACT_SOLR_ONTOLOGIES_URL, solrOntologiesUrl);
        addProperty(properties, INTACT_HIERARCHVIEW_URL, hierarchViewUrl);
        addProperty(properties, INTACT_HIERARCHVIEW_IMAGEURL, hierarchViewImageUrl);
        addProperty(properties, INTACT_HIERARCHVIEW_MAXINTERACTIONS, hierarchViewMaxInteractions);
        addProperty(properties, INTACT_CHEBI_URL, chebiUrl);
        addProperty(properties, INTACT_CHEBI_SEARCH_PATH, chebiChemicalSearchPath);
        addProperty(properties, INTACT_DASTY_URL, dastyUrl);
        addProperty(properties, INTACT_SECRET, intactSecret);
        addProperty(properties, INTACT_SEARCH_ONTOLOGIES_MAXSUGGESTIONS, String.valueOf(maxOntologySuggestions));
        addProperty(properties, INTACT_GOOGLE_ANALYTICS_TRACKER, googleAnalyticsTracker);
        addProperty(properties, INTACT_RECIPIENTS, mailRecipients);
        addProperty(properties, PROXY_HOST, proxyHost);
        addProperty(properties, PROXY_PORT, proxyPort);

        Writer writer = new FileWriter(configFile);
        properties.store(writer, webappName+ " configuration");
        writer.close();

    }

    private void addProperty(Properties properties, String key, String value) {
        if (isValueSet(value)) {
            properties.setProperty(key, value);
        }
    }

    private boolean isValueSet(String value) {
        return value != null && !value.startsWith("$");
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

    public String getWarningMessage() {
        return warningMessage;
    }

    public void setWarningMessage( String warningMessage ) {
        this.warningMessage = warningMessage;
    }

    public String getConfigFile() {
        return configFile;
    }

    public void setConfigFile(String configFile) {
        this.configFile = configFile;
    }

    public int getGraphMaxInteractionCount() {
        return graphMaxInteractionCount;
    }

    public void setGraphMaxInteractionCount( int graphMaxInteractionCount ) {
        this.graphMaxInteractionCount = graphMaxInteractionCount;
    }

    public String getSolrInteractionsUrl() {
        return solrInteractionsUrl;
    }

    public void setSolrInteractionsUrl(String solrServerUrl) {
        this.solrInteractionsUrl = solrServerUrl;
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


    public void setDastyUrl(String dastyUrl) {this.dastyUrl = dastyUrl;}

    public String getDastyUrl() { return dastyUrl; }

    public SolrServer getInteractionSolrServer() {
        if (solrInteractionsUrl != null) {
            try {

                HttpClient httpClient = createHttpClient();

                return new CommonsHttpSolrServer(solrInteractionsUrl, httpClient);
            } catch (MalformedURLException e) {
                throw new IntactViewException("Malformed Solr URL: "+ solrInteractionsUrl, e);
            }
        }

        return null;
    }

    public SolrServer getOntologySolrServer() {
        if (solrInteractionsUrl != null) {
            try {
                HttpClient httpClient = createHttpClient();

                return new CommonsHttpSolrServer(solrOntologiesUrl, httpClient);
            } catch (MalformedURLException e) {
                throw new IntactViewException("Malformed Solr URL: "+ solrOntologiesUrl, e);
            }
        }

        return null;
    }

    private HttpClient createHttpClient() {
        HttpClient httpClient = new HttpClient();

        if (isValueSet(proxyHost) && proxyHost.trim().length() > 0 &&
                isValueSet(proxyPort) && proxyPort.trim().length() > 0) {
            httpClient.getHostConfiguration().setProxy(proxyHost, Integer.valueOf(proxyPort));
        }
        return httpClient;
    }

    public void setSolrOntologiesUrl(String solrOntologiesUrl) {
        this.solrOntologiesUrl = solrOntologiesUrl;
    }

    public String getSolrOntologiesUrl() {
        return solrOntologiesUrl;
    }

    public String getProxyHost() {
        return proxyHost;
    }

    public void setProxyHost(String proxyHost) {
        this.proxyHost = proxyHost;
    }

    public String getProxyPort() {
        return proxyPort;
    }

    public void setProxyPort(String proxyPort) {
        this.proxyPort = proxyPort;
    }
}
