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

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hupo.psi.mi.psicquic.registry.ServiceType;
import org.hupo.psi.mi.psicquic.registry.client.PsicquicRegistryClientException;
import org.hupo.psi.mi.psicquic.registry.client.registry.DefaultPsicquicRegistryClient;
import org.hupo.psi.mi.psicquic.registry.client.registry.PsicquicRegistryClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;
import uk.ac.ebi.intact.view.webapp.controller.BaseController;
import uk.ac.ebi.intact.view.webapp.controller.config.IntactViewConfiguration;

import java.io.IOException;
import java.io.InputStream;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 */
@Controller
@Scope("currentSearch")
public class PsicquicController extends BaseController {

    private static final Log log = LogFactory.getLog(PsicquicController.class);

    @Autowired
    private IntactViewConfiguration intactViewConfiguration;

    // psicquic
    private List<ServiceType> services;
    private List<ServiceType> imexServices;
    private int countInOtherDatabases = -1;
    private int countInOtherImexDatabases = -1;
    private int otherDatabasesWithResults;
    private int otherImexDatabasesWithResults;

    public PsicquicController() {
    }

     public void countResultsInOtherDatabases() throws PsicquicRegistryClientException {
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
        countInOtherDatabases = 0;
        otherImexDatabasesWithResults = 0;
        countInOtherImexDatabases = 0;

        imexServices = new ArrayList<ServiceType>(services.size());

        String query = getUserQuery().getSearchQuery();

        if (query == null || query.length() == 0) {
            query = "*";
        }

        final String psicquicQuery = query;

        final ExecutorService executorService = Executors.newCachedThreadPool();

        for (final ServiceType service : services) {
            boolean imexTagFound = false;

            for (String tag : service.getTags()) {
                if ("MI:0959".equals(tag)) {
                    imexServices.add(service);
                    imexTagFound = true;
                    break;
                }
            }

            final boolean isImexService = imexTagFound;

            if (intactViewConfiguration.getWebappName().contains(service.getName())) {
                continue;
            }

            Runnable runnable = new Runnable() {
                public void run() {
                    try {

                        int psicquicCount = countInPsicquicService(service, psicquicQuery);

                        int imexCount = 0;


                        if (isImexService) {
                            final String imexQuery = createImexQuery(psicquicQuery);
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
            };

            executorService.submit(runnable);


        }

        executorService.shutdown();

        try {
            executorService.awaitTermination(10, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private int countInPsicquicService(ServiceType service, String query) {
        int psicquicCount = 0;

        String url = null;

        try {

            String encoded = URLEncoder.encode(query, "UTF-8");
            encoded = encoded.replaceAll("\\+", "%20");

            url = service.getRestUrl()+"query/"+ encoded +"?format=count";

            HttpClient httpClient = intactViewConfiguration.getHttpClientBasedOnUrl(url);

            HttpMethod method = new GetMethod(url);
            final int returnCode = httpClient.executeMethod(method);

            if (returnCode != 200) {
                log.error("HTTP Error "+returnCode+" connecting to PSICQUIC service '"+service.getName()+"': "+url+" / proxy "+intactViewConfiguration.getProxyHost()+":"+intactViewConfiguration.getProxyPort());
            } else {
                final InputStream input = method.getResponseBodyAsStream();
                String strCount = IOUtils.toString(input);
                input.close();
                psicquicCount = Integer.parseInt(strCount);
            }
        } catch (IOException e) {
            log.error("Problem connecting to PSICQUIC service '"+service.getName()+"': "+url+" / proxy "+intactViewConfiguration.getProxyHost()+":"+intactViewConfiguration.getProxyPort(), e);
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
