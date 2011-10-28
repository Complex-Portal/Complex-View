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
package uk.ac.ebi.intact.psicquic.ws.config;

import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.transaction.annotation.Transactional;
import uk.ac.ebi.intact.core.context.IntactContext;
import uk.ac.ebi.intact.model.meta.DbInfo;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Place-holder for the configuration. Initialized by Spring.
 *
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 */
@Configurable
public class PsicquicConfig {

    private String groupId;
    private String artifactId;
    private String version;
    private String restSpecVersion;
    private String soapSpecVersion;
    private String solrServerUrl;
    private String proxyHost;
    private String proxyPort;
    private String propertiesAsStrings;
    private String queryFilter;
    private String implementationName;

    private String statsDirectory;

    public PsicquicConfig() {
    }

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public String getArtifactId() {
        return artifactId;
    }

    public void setArtifactId(String artifactId) {
        this.artifactId = artifactId;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getRestSpecVersion() {
        return restSpecVersion;
    }

    public void setRestSpecVersion(String restSpecVersion) {
        this.restSpecVersion = restSpecVersion;
    }

    public String getSoapSpecVersion() {
        return soapSpecVersion;
    }

    public void setSoapSpecVersion(String soapSpecVersion) {
        this.soapSpecVersion = soapSpecVersion;
    }

    public String getSolrServerUrl() {
        return solrServerUrl;
    }

    public void setSolrServerUrl( String solrServerUrl ) {
        this.solrServerUrl = solrServerUrl;
    }

    public String getStatsDirectory() {
        return statsDirectory;
    }

    public void setStatsDirectory(String statsDirectory) {
        this.statsDirectory = statsDirectory;
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

    public String getQueryFilter() {
        return queryFilter;
    }

    public void setQueryFilter(String queryFilter) {
        this.queryFilter = queryFilter;
    }

    @Transactional
    public Map<String,String> getProperties() {
        String propsAsString = getPropertiesAsStrings();

        if (propsAsString == null) return Collections.EMPTY_MAP;

        Map<String,String> propMap = new HashMap<String, String>();

        String[] props = propsAsString.split(",");

        for (String prop : props) {
            String[] propTokens = prop.trim().split("=");

            if (propTokens.length > 1) {
                propMap.put(propTokens[0], propTokens[1]);
            } 
        }

        List<DbInfo> dbInfos = IntactContext.getCurrentInstance().getDaoFactory()
                .getDbInfoDao().getAll();

        for (DbInfo dbInfo : dbInfos) {
            propMap.put(dbInfo.getKey(), dbInfo.getValue());
        }

        propMap.put("psicquic.rest.spec.version", getRestSpecVersion());
        propMap.put("psicquic.soap.spec.version", getSoapSpecVersion());
        propMap.put("psicquic.implementation.name", getImplementationName());
        propMap.put("psicquic.implementation.version", getVersion());

        return propMap;
    }

    public String getPropertiesAsStrings() {
        return propertiesAsStrings;
    }

    public void setPropertiesAsStrings(String propertiesAsStrings) {
        this.propertiesAsStrings = propertiesAsStrings;
    }

    public String getImplementationName() {
        return implementationName;
    }

    public void setImplementationName(String implementationName) {
        this.implementationName = implementationName;
    }
}
