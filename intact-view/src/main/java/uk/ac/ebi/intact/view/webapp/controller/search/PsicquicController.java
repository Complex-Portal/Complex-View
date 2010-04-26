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
package uk.ac.ebi.intact.view.webapp.controller.search;

import org.apache.commons.io.IOUtils;
import org.hupo.psi.mi.psicquic.registry.ServiceType;
import org.hupo.psi.mi.psicquic.registry.client.PsicquicRegistryClientException;
import org.hupo.psi.mi.psicquic.registry.client.registry.DefaultPsicquicRegistryClient;
import org.hupo.psi.mi.psicquic.registry.client.registry.PsicquicRegistryClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;
import uk.ac.ebi.intact.view.webapp.controller.BaseController;
import uk.ac.ebi.intact.view.webapp.controller.config.IntactViewConfiguration;

import javax.faces.event.ActionEvent;
import java.io.IOException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 */
@Controller
@Scope("currentSearch")
public class PsicquicController extends BaseController {

    @Autowired
    private IntactViewConfiguration intactViewConfiguration;

    // psicquic
    private List<ServiceType> services;
    private List<ServiceType> imexServices;
    private int countInOtherDatabases = -1;
    private int countInOtherImexDatabases = -1;
    private int otherDatabasesWithResults;
    private int otherImexDatabasesWithResults;

    private boolean psicquicQueryRunning;
    private String lastPsicquicSearch;

    public PsicquicController() {
    }

    public void doPsicquicQuery(ActionEvent evt) {
        if (psicquicQueryRunning) {
            return;
        }

        psicquicQueryRunning = true;

        try {

                String query = getUserQuery().getSearchQuery();

                if (query == null || query.length() == 0) {
                    query = "*";
                }

            if (!query.equals(lastPsicquicSearch)) {
                countResultsInOtherDatabases(query);
                lastPsicquicSearch = query;
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            psicquicQueryRunning = false;
        }

    }

    private void countResultsInOtherDatabases(String query) throws PsicquicRegistryClientException {
        final String psicquicRegistryUrl = intactViewConfiguration.getPsicquicRegistryUrl();

        if (psicquicRegistryUrl == null || psicquicRegistryUrl.length() == 0) {
            return;
        }

        if (services == null) {
            PsicquicRegistryClient registryClient = new DefaultPsicquicRegistryClient(psicquicRegistryUrl);
            services = registryClient.listActiveServices();
        }

        countInOtherDatabases = 0;
        otherDatabasesWithResults = 0;
        countInOtherImexDatabases = 0;
        otherImexDatabasesWithResults = 0;

        imexServices = new ArrayList<ServiceType>(services.size());


        for (ServiceType service : services) {
            boolean isImexService = false;

            for (String tag : service.getTags()) {
                if ("MI:0959".equals(tag)) {
                    imexServices.add(service);
                    isImexService = true;
                    break;
                }
            }

            if (intactViewConfiguration.getWebappName().contains(service.getName())) {
                continue;
            }

            List<String> lines = null;
            try {

                int psicquicCount = 0;
                int imexCount = 0;

                psicquicCount = countInPsicquicService(service, query);

                if (isImexService) {
                    final String imexQuery = createImexQuery(query);
                    imexCount = countInPsicquicService(service, imexQuery);
                }

                countInOtherDatabases += psicquicCount;
                countInOtherImexDatabases += imexCount;

                if (psicquicCount > 0) {
                    otherDatabasesWithResults++;
                }

                if (imexCount > 0) {
                    otherImexDatabasesWithResults++;
                }

            } catch (Throwable e) {
                e.printStackTrace();
            }

        }
    }

    private int countInPsicquicService(ServiceType service, String query) {
        int psicquicCount = 0;

        try {
            String encoded = URLEncoder.encode(query, "UTF-8");
            encoded = encoded.replaceAll("\\+", "%20");

            String url = service.getRestUrl()+"query/"+ encoded +"?format=count";
            String strCount = IOUtils.toString(new URL(url).openStream());
            psicquicCount = Integer.parseInt(strCount);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return psicquicCount;
    }

    private String createImexQuery(String query) {
        String filter = "interaction_id:imex";

        if (query != null && query.trim().length() > 0 && !"*".equals(query.trim())) {
            return filter+" AND ("+query.trim()+")";
        } else {
            return filter;
        }
    }

    private UserQuery getUserQuery() {
        return (UserQuery) getBean("userQuery");
    }

    public int getCountInOtherDatabases() {
        return countInOtherDatabases;
    }

    public List<ServiceType> getServices() {
        return services;
    }

    public int getOtherDatabasesWithResults() {
        return otherDatabasesWithResults;
    }

    public boolean isPsicquicQueryRunning() {
        return psicquicQueryRunning;
    }

    public int getCountInOtherImexDatabases() {
        return countInOtherImexDatabases;
    }

    public int getOtherImexDatabasesWithResults() {
        return otherImexDatabasesWithResults;
    }

    public List<ServiceType> getImexServices() {
        return imexServices;
    }
}
