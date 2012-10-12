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

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.MultiThreadedHttpConnectionManager;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpHost;
import org.apache.http.conn.params.ConnRoutePNames;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.PoolingClientConnectionManager;
import org.apache.solr.client.solrj.impl.HttpSolrServer;
import org.hupo.psi.mi.psicquic.wsclient.PsicquicSimpleClient;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import uk.ac.ebi.intact.view.webapp.IntactViewException;
import uk.ac.ebi.intact.view.webapp.controller.BaseController;

import javax.faces.bean.ApplicationScoped;
import javax.persistence.EntityManagerFactory;
import java.io.*;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.Proxy;
import java.net.SocketAddress;
import java.util.*;

/**
 * IntactView configuration bean.
 *
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 */
@ApplicationScoped
public class IntactViewConfiguration extends BaseController implements InitializingBean, DisposableBean {

    private static final Log log = LogFactory.getLog( IntactViewConfiguration.class );

    private static final String WEBAPP_NAME = "webapp.name";
    private static final String WEBAPP_LOGO_URL = "webapp.logo.url";
    private static final String INTACT_VIEW_APPROOT = "intact.view.approot";
    private static final String INTACT_MENU_URL = "intact.menu.url";
    private static final String INTACT_NEWS_URL = "intact.news.url";
    private static final String INTACT_DOTM_URL = "intact.dotm.url";
    private static final String INTACT_FTP_URL = "intact.ftp.url";
    private static final String INTACT_FAQ_URL = "intact.faq.url";
    private static final String INTACT_NEWS_MAXINBOX = "intact.news.maxinbox";
    private static final String INTACT_WARNING_MSG = "intact.warning.msg";
    private static final String INTACT_CONFIGFILE = "intact.configfile";
    private static final String INTACT_GRAPH_MAX_INTERACTION_COUNT = "intact.graph.maxInteractionCount";
    private static final String INTACT_SOLR_INTERACTIONS_URL = "intact.solr.interactions.url";
    private static final String INTACT_SOLR_ONTOLOGIES_URL = "intact.solr.ontologies.url";
    private static final String INTACT_HIERARCHVIEW_URL = "intact.hierarchview.url";
    private static final String INTACT_HIERARCHVIEW_IMAGEURL = "intact.hierarchview.imageurl";
    private static final String INTACT_HIERARCHVIEW_SEARCHURL = "intact.hierarchview.searchurl";
    private static final String INTACT_HIERARCHVIEW_MAXINTERACTIONS = "intact.hierarchview.maxinteractions";
    private static final String INTACT_CHEBI_URL = "intact.chebi.url";
    private static final String INTACT_CHEBI_SEARCH_ENABLED = "intact.chebi.search";
    private static final String INTACT_CHEBI_SEARCH_PATH = "intact.chebi.search.path";
    private static final String INTACT_SECRET = "intact.secret";
    private static final String INTACT_SEARCH_ONTOLOGIES_MAXSUGGESTIONS = "intact.search.ontologies.maxsuggestions";
    private static final String INTACT_GOOGLE_ANALYTICS_TRACKER = "intact.google.analytics.tracker";
    private static final String INTACT_RECIPIENTS = "intact.mail.recipients";
    private static final String PROXY_HOST = "intact.proxy.host";
    private static final String PROXY_PORT = "intact.proxy.port";
    private static final String PSICQUIC_REGISTRY_URL = "psicquic.registry.url";
    private static final String PSICQUIC_VIEW_URL = "psicquic.view.url";
    private static final String IMEX_VIEW_URL = "imex.view.url";
    private static final String MAX_SIZE_XML_EXPORT = "intact.maxsize.xml";
    private static final String MAX_SIZE_XGMML_EXPORT = "intact.maxsize.xgmml";
    private static final String ONTOLOGY_LUCENE_DIRECTORY = "intact.ontologies.lucene.directory";

    @Autowired
    private EntityManagerFactory entityManagerFactory;

    private String appRoot;
    private String menuUrl;
    private String newsUrl;
    private String dotmUrl;
    private String ftpUrl;
    private String faqUrl;
    private int maxNewsInBox;
    private String warningMessage;
    private String configFile;
    private int graphMaxInteractionCount;
    private String solrInteractionsUrl;
    private String solrOntologiesUrl;
    private String hierarchViewUrl;
    private String hierarchViewImageUrl;
    private String hierarchViewSearchUrl;
    private String hierarchViewMaxInteractions;
    private String chebiUrl;
    private boolean chebiSearchEnabled;
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
    private Proxy proxy;
    private int maxSizeXmlExport;
    private int maxSizeXgmmlExport;

    private String psicquicRegistryUrl;
    private String psicquicViewUrl;
    private String imexViewUrl;

    private HttpSolrServer solrServer;
    private HttpSolrServer ontologySolrServer;

    private HttpClient httpClientWithProxy;
    private HttpClient httpClientWithoutProxy;
    private Map<String, PsicquicSimpleClient> psicquicClientMap;

    private List<String> databaseNamesUsingSameSolr;

    private String ontologyLuceneDirectory;

    public IntactViewConfiguration() {
    }

    public void afterPropertiesSet() throws Exception {
        storeIfNew();

        initializeDatabaseNamesUsingSameSolr();

        initializePsicquicClientMap();

        initializeSolrServer();

        initializeOntologyServer();

        initializeHttpClients();

    }

    private void initializeHttpClients() {
        // initialise http clients
        if (httpClientWithProxy == null) {
            httpClientWithProxy = new HttpClient(new MultiThreadedHttpConnectionManager());

            if (isValueSet(proxyHost) && isValueSet(proxyPort)) {
                httpClientWithProxy.getHostConfiguration().setProxy(proxyHost, Integer.valueOf(proxyPort));

                log.info("Setting HTTPClient using proxy: " + proxyHost + ":" + proxyPort);
            } else {
                log.info("Setting HTTPClient with NO PROXY");
            }
        }
        if (httpClientWithoutProxy == null) {
            httpClientWithoutProxy = new HttpClient(new MultiThreadedHttpConnectionManager());
        }
    }

    private void initializeOntologyServer() {
        // initialize ontology solr server
        if (solrOntologiesUrl != null) {
            if (ontologySolrServer == null) {
                try {
                    ontologySolrServer = createSolrServer(solrOntologiesUrl);
                } catch (MalformedURLException e) {
                    throw new IntactViewException("Malformed Solr URL: "+ solrOntologiesUrl, e);
                }
            }
        }
    }

    private void initializeSolrServer() {
        // initialize solr interaction servers
        if (solrInteractionsUrl != null) {
            if (solrServer == null) {
                try {
                    solrServer = createSolrServer(solrInteractionsUrl);
                } catch (MalformedURLException e) {
                    throw new IntactViewException("Malformed Solr URL: " + solrInteractionsUrl, e);
                }

            }
        }
    }

    private void initializePsicquicClientMap() {
        if (psicquicClientMap == null){
            psicquicClientMap = new HashMap<String, PsicquicSimpleClient>(26);
        }
    }

    private void initializeDatabaseNamesUsingSameSolr() {
        if (databaseNamesUsingSameSolr == null){
            databaseNamesUsingSameSolr = new ArrayList<String>();
        }
    }

    public synchronized void storeIfNew() {
        if (new File(configFile).exists()) {
            try {
                FileInputStream inputStream = new FileInputStream(configFile);
                try{
                    loadConfiguration(inputStream);
                }
                finally {
                    inputStream.close();
                }
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

    public synchronized void loadConfiguration(InputStream is) throws IOException {
        if (log.isInfoEnabled()) log.info("Loading properties from: "+configFile);

        Properties properties = new Properties();
        properties.load(is);

        webappName = properties.getProperty(WEBAPP_NAME, webappName);
        webappLogoUrl = properties.getProperty(WEBAPP_LOGO_URL, webappLogoUrl);
        appRoot = properties.getProperty(INTACT_VIEW_APPROOT, appRoot);
        menuUrl = properties.getProperty(INTACT_MENU_URL, menuUrl);
        newsUrl = properties.getProperty(INTACT_NEWS_URL, newsUrl);
        dotmUrl = properties.getProperty(INTACT_DOTM_URL, dotmUrl);
        ftpUrl = properties.getProperty(INTACT_FTP_URL, ftpUrl);
        faqUrl = properties.getProperty(INTACT_FAQ_URL, faqUrl);
        maxNewsInBox = Integer.parseInt(properties.getProperty(INTACT_NEWS_MAXINBOX, String.valueOf(maxNewsInBox)));
        warningMessage = properties.getProperty(INTACT_WARNING_MSG, warningMessage);
        configFile = properties.getProperty(INTACT_CONFIGFILE, configFile);
        graphMaxInteractionCount = Integer.parseInt(properties.getProperty(INTACT_GRAPH_MAX_INTERACTION_COUNT, String.valueOf(graphMaxInteractionCount)));
        solrInteractionsUrl = properties.getProperty(INTACT_SOLR_INTERACTIONS_URL, solrInteractionsUrl);
        solrOntologiesUrl = properties.getProperty(INTACT_SOLR_ONTOLOGIES_URL, solrOntologiesUrl);
        hierarchViewUrl = properties.getProperty(INTACT_HIERARCHVIEW_URL, hierarchViewUrl);
        hierarchViewImageUrl = properties.getProperty(INTACT_HIERARCHVIEW_IMAGEURL, hierarchViewImageUrl);
        hierarchViewSearchUrl = properties.getProperty(INTACT_HIERARCHVIEW_SEARCHURL, hierarchViewSearchUrl);
        hierarchViewMaxInteractions = properties.getProperty(INTACT_HIERARCHVIEW_MAXINTERACTIONS, hierarchViewMaxInteractions);
        chebiUrl = properties.getProperty(INTACT_CHEBI_URL, chebiUrl);
        chebiSearchEnabled = Boolean.valueOf(properties.getProperty(INTACT_CHEBI_SEARCH_ENABLED, "true"));
        chebiChemicalSearchPath = properties.getProperty(INTACT_CHEBI_SEARCH_PATH, chebiChemicalSearchPath);
        intactSecret = properties.getProperty(INTACT_SECRET, intactSecret);
        maxOntologySuggestions = Integer.parseInt(properties.getProperty(INTACT_SEARCH_ONTOLOGIES_MAXSUGGESTIONS, String.valueOf(maxOntologySuggestions)));
        googleAnalyticsTracker = properties.getProperty(INTACT_GOOGLE_ANALYTICS_TRACKER, googleAnalyticsTracker);
        mailRecipients = properties.getProperty(INTACT_RECIPIENTS, mailRecipients);
        psicquicRegistryUrl = properties.getProperty(PSICQUIC_REGISTRY_URL, psicquicRegistryUrl);
        psicquicViewUrl = properties.getProperty(PSICQUIC_VIEW_URL, psicquicViewUrl);
        imexViewUrl = properties.getProperty(IMEX_VIEW_URL, imexViewUrl);
        maxSizeXmlExport = Integer.parseInt(properties.getProperty(MAX_SIZE_XML_EXPORT, String.valueOf(maxSizeXmlExport)));
        maxSizeXgmmlExport = Integer.parseInt(properties.getProperty(MAX_SIZE_XGMML_EXPORT, String.valueOf(maxSizeXgmmlExport)));
        this.ontologyLuceneDirectory = properties.getProperty(ONTOLOGY_LUCENE_DIRECTORY, ontologyLuceneDirectory);
    }

    public synchronized void storeConfiguration() throws IOException {
        if (log.isInfoEnabled()) log.info("Storing properties at: "+configFile);

        Properties properties = new Properties();
        addProperty(properties, WEBAPP_NAME, webappName);
        addProperty(properties, WEBAPP_LOGO_URL, webappLogoUrl);
        addProperty(properties, INTACT_VIEW_APPROOT, appRoot);
        addProperty(properties, INTACT_MENU_URL, menuUrl);
        addProperty(properties, INTACT_NEWS_URL, newsUrl);
        addProperty(properties, INTACT_DOTM_URL, dotmUrl);
        addProperty(properties, INTACT_FTP_URL, ftpUrl);
        addProperty(properties, INTACT_FAQ_URL, faqUrl);
        addProperty(properties, INTACT_NEWS_MAXINBOX, String.valueOf(maxNewsInBox));
        addProperty(properties, INTACT_WARNING_MSG, warningMessage);
        addProperty(properties, INTACT_CONFIGFILE, configFile);
        addProperty(properties, INTACT_GRAPH_MAX_INTERACTION_COUNT, String.valueOf(graphMaxInteractionCount));
        addProperty(properties, INTACT_SOLR_INTERACTIONS_URL, solrInteractionsUrl);
        addProperty(properties, INTACT_SOLR_ONTOLOGIES_URL, solrOntologiesUrl);
        addProperty(properties, INTACT_HIERARCHVIEW_URL, hierarchViewUrl);
        addProperty(properties, INTACT_HIERARCHVIEW_IMAGEURL, hierarchViewImageUrl);
        addProperty(properties, INTACT_HIERARCHVIEW_MAXINTERACTIONS, hierarchViewMaxInteractions);
        addProperty(properties, INTACT_CHEBI_URL, chebiUrl);
        addProperty(properties, INTACT_CHEBI_SEARCH_ENABLED, String.valueOf(chebiSearchEnabled));
        addProperty(properties, INTACT_CHEBI_SEARCH_PATH, chebiChemicalSearchPath);
        addProperty(properties, INTACT_SECRET, intactSecret);
        addProperty(properties, INTACT_SEARCH_ONTOLOGIES_MAXSUGGESTIONS, String.valueOf(maxOntologySuggestions));
        addProperty(properties, INTACT_GOOGLE_ANALYTICS_TRACKER, googleAnalyticsTracker);
        addProperty(properties, INTACT_RECIPIENTS, mailRecipients);
        addProperty(properties, PSICQUIC_REGISTRY_URL, psicquicRegistryUrl);
        addProperty(properties, PSICQUIC_VIEW_URL, psicquicViewUrl);
        addProperty(properties, IMEX_VIEW_URL, imexViewUrl);
        addProperty(properties, MAX_SIZE_XML_EXPORT, String.valueOf(maxSizeXmlExport));
        addProperty(properties, MAX_SIZE_XGMML_EXPORT, String.valueOf(maxSizeXgmmlExport));
        addProperty(properties, ONTOLOGY_LUCENE_DIRECTORY, ontologyLuceneDirectory);

        final FileOutputStream os = new FileOutputStream( configFile );
        properties.store( os, webappName+ " configuration");
        os.flush();
        os.close();
    }


    public synchronized void shutDownServers(){

        if (solrServer != null){
            solrServer.shutdown();
            solrServer = null;
        }
        if (ontologySolrServer != null){
            ontologySolrServer.shutdown();
            ontologySolrServer = null;
        }
        if (entityManagerFactory != null){
            entityManagerFactory.close();
        }
    }
    private void addProperty(Properties properties, String key, String value) {
        if (isValueSet(value)) {
            properties.setProperty(key, value);
        }
    }

    private boolean isValueSet(String value) {
        return value != null && !value.startsWith("$") && value.length() > 0;
    }

    private void closeEntityManagerFactory() {
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

    public String getDotmUrl() {
        return dotmUrl;
    }

    public void setDotmUrl( String dotmUrl ) {
        this.dotmUrl = dotmUrl;
    }

    public String getFtpUrl() {
        return ftpUrl;
    }

    public void setFtpUrl( String ftpUrl ) {
        this.ftpUrl = ftpUrl;
    }

    public String getFaqUrl() {
        return faqUrl;
    }

    public void setFaqUrl( String faqUrl ) {
        this.faqUrl = faqUrl;
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

    public String getHierarchViewSearchUrl() {
        return hierarchViewSearchUrl;
    }

    public void setHierarchViewSearchUrl( String hierarchViewSearchUrl ) {
        this.hierarchViewSearchUrl = hierarchViewSearchUrl;
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

    public HttpSolrServer getInteractionSolrServer() {
        return solrServer;
    }

    public HttpSolrServer getOntologySolrServer() {

        return ontologySolrServer;
    }

    private HttpSolrServer createSolrServer(String solrUrl) throws MalformedURLException {
        org.apache.http.client.HttpClient httpClient = getHttpClientBasedOnUrl(solrUrl);

        HttpSolrServer solrServer = new HttpSolrServer(solrUrl, httpClient);
        solrServer.setAllowCompression(true);
        solrServer.setConnectionTimeout(5000);
        solrServer.setConnectionTimeout(5000);

        return solrServer;
    }

    private org.apache.http.client.HttpClient getHttpClientBasedOnUrl(String url) {
        SchemeRegistry schemeRegistry = new SchemeRegistry();
        schemeRegistry.register(new Scheme("http", 80, PlainSocketFactory
                .getSocketFactory()));
        schemeRegistry.register(new Scheme("https", 443, SSLSocketFactory
                .getSocketFactory()));

        PoolingClientConnectionManager cm = new PoolingClientConnectionManager(schemeRegistry);
        cm.setMaxTotal(128);
        cm.setDefaultMaxPerRoute(32);

        org.apache.http.client.HttpClient httpClient = new DefaultHttpClient(cm);

        if (url.contains("localhost") || url.contains("127.0.0.1")) {
            return httpClient;
        }

        if (isValueSet(proxyHost) && proxyHost.trim().length() > 0 &&
                isValueSet(proxyPort) && proxyPort.trim().length() > 0) {
            try{
                HttpHost proxy = new HttpHost(proxyHost, Integer.parseInt(proxyPort));
                httpClient.getParams().setParameter(ConnRoutePNames.DEFAULT_PROXY, proxy);
            }
            catch (Exception e){
                log.error("Impossible to create proxy host:"+proxyHost+", port:"+proxyPort,e);
            }
        }
        return httpClient;
    }

    public synchronized HttpClient getCommonsHttpClientBasedOnUrl(String url) {
        if (url.contains("localhost") || url.contains("127.0.0.1")) {
            return getHttpClientWithoutProxy();
        }
        return getHttpClientWithProxy();
    }

    protected HttpClient getHttpClientWithProxy() {

        return httpClientWithProxy;
    }

    protected HttpClient getHttpClientWithoutProxy() {

        return httpClientWithoutProxy;
    }

    public Map<String, PsicquicSimpleClient> getPsicquicClientMap() {
        return psicquicClientMap;
    }

    public synchronized PsicquicSimpleClient getPsicquicClient(String rest) {

        if (this.psicquicClientMap.containsKey(rest)){
            return this.psicquicClientMap.get(rest);
        }

        PsicquicSimpleClient simpleClient;

        if (proxyPort != null && proxyPort.length() > 0){
            try{
                int port = Integer.parseInt(proxyPort);
                SocketAddress address = new InetSocketAddress(proxyHost, port);
                this.proxy = new Proxy(Proxy.Type.HTTP, address);
            }
            catch(Exception e){
                log.error("Cannot create proxy using port " + proxyPort);
            }
        }

        if (proxy != null) {
            simpleClient = new PsicquicSimpleClient(rest, proxy);

        } else {
            log.info("Setting PSICQUIC httpClient using proxy with NO PROXY");
            simpleClient = new PsicquicSimpleClient(rest);
        }

        this.psicquicClientMap.put(rest, simpleClient);

        return simpleClient;
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

    public boolean isChebiSearchEnabled() {
        return chebiSearchEnabled;
    }

    public void setChebiSearchEnabled(boolean chebiSearchEnabled) {
        this.chebiSearchEnabled = chebiSearchEnabled;
    }

    public String getPsicquicRegistryUrl() {
        return psicquicRegistryUrl;
    }

    public void setPsicquicRegistryUrl(String psicquicRegistryUrl) {
        this.psicquicRegistryUrl = psicquicRegistryUrl;
    }

    public String getPsicquicViewUrl() {
        return psicquicViewUrl;
    }

    public void setPsicquicViewUrl(String psicquicViewUrl) {
        this.psicquicViewUrl = psicquicViewUrl;
    }

    public String getImexViewUrl() {
        return imexViewUrl;
    }

    public void setImexViewUrl(String imexViewUrl) {
        this.imexViewUrl = imexViewUrl;
    }

    public int getMaxSizeXmlExport() {
        return maxSizeXmlExport;
    }

    public void setMaxSizeXmlExport(int maxSizeXmlExport) {
        this.maxSizeXmlExport = maxSizeXmlExport;
    }

    public int getMaxSizeXgmmlExport() {
        return maxSizeXgmmlExport;
    }

    public void setMaxSizeXgmmlExport(int maxSizeXgmmlExport) {
        this.maxSizeXgmmlExport = maxSizeXgmmlExport;
    }

    public List<String> getDatabaseNamesUsingSameSolr() {
        return databaseNamesUsingSameSolr;
    }

    public void setDatabaseNamesUsingSameSolr(List<String> databaseNamesUsingSameSolr) {
        this.databaseNamesUsingSameSolr = databaseNamesUsingSameSolr;
    }

    public String getOntologyLuceneDirectory() {
        return ontologyLuceneDirectory;
    }

    public void setOntologyLuceneDirectory(String ontologyLuceneDirectory) {
        this.ontologyLuceneDirectory = ontologyLuceneDirectory;
    }

    @Override
    public void destroy() throws Exception {
        shutDownServers();
    }
}
