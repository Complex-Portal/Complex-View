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

import org.apache.commons.httpclient.DefaultHttpMethodRetryHandler;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.HttpMethodRetryHandler;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.params.HttpMethodParams;
import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.myfaces.orchestra.conversation.annotations.ConversationName;
import org.hupo.psi.mi.psicquic.registry.ServiceType;
import org.hupo.psi.mi.psicquic.registry.client.PsicquicRegistryClientException;
import org.hupo.psi.mi.psicquic.registry.client.registry.DefaultPsicquicRegistryClient;
import org.hupo.psi.mi.psicquic.registry.client.registry.PsicquicRegistryClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;
import uk.ac.ebi.intact.view.webapp.application.PsicquicThreadConfig;
import uk.ac.ebi.intact.view.webapp.controller.BaseController;
import uk.ac.ebi.intact.view.webapp.controller.config.IntactViewConfiguration;

import javax.faces.context.FacesContext;
import javax.faces.event.ComponentSystemEvent;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

/**
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 */
@Controller
@Scope("conversation.access")
@ConversationName("general")
public class PsicquicController extends BaseController {

    private static final Log log = LogFactory.getLog(PsicquicController.class);

    @Autowired
    private IntactViewConfiguration intactViewConfiguration;

    // psicquic
    private List<ServiceType> services;
    private List<ServiceType> imexServices;
    private List<Future> runningTasks;
    private int countInOtherDatabases = -1;
    private int countInOtherImexDatabases = -1;
    private int otherDatabasesWithResults;
    private int otherImexDatabasesWithResults;
    private int nonRespondingDatabases = -1;
    private int nonRespondingImexDatabases = -1;
    private int threadTimeOut = 5;

    public PsicquicController() {
    }

    private boolean isImexService(ServiceType service){
        return service.getTags().contains("MI:0959");
    }

    public void countResultsInOtherDatabases(ComponentSystemEvent event) {
        if (!FacesContext.getCurrentInstance().isPostback()) {
            try {
                final String psicquicRegistryUrl = intactViewConfiguration.getPsicquicRegistryUrl();

                if (psicquicRegistryUrl == null || psicquicRegistryUrl.length() == 0) {
                    return;
                }

                boolean areImexServicesInitialized = true;

                if (services == null) {
                    PsicquicRegistryClient registryClient = new DefaultPsicquicRegistryClient(psicquicRegistryUrl);
                    services = registryClient.listActiveServices();
                }

                resetPsicquicCounts();

                if (imexServices == null){
                    imexServices = new ArrayList<ServiceType>(services.size());

                    areImexServicesInitialized = false;
                }

                String query = getUserQuery().getSearchQuery();

                if (query == null || query.length() == 0) {
                    query = "*";
                }

                final String psicquicQuery = query;

                PsicquicThreadConfig threadConfig = (PsicquicThreadConfig) getBean("psicquicThreadConfig");

                ExecutorService executorService = threadConfig.getExecutorService();

                if (runningTasks == null){
                    runningTasks = new ArrayList<Future>();
                }
                else {
                    runningTasks.clear();
                }

                for (final ServiceType service : services) {
                    if (!areImexServicesInitialized){

                        if (isImexService(service)){
                            imexServices.add(service);
                        }
                    }

                    if (intactViewConfiguration.getWebappName().contains(service.getName())) {
                        continue;
                    }

                    Callable<PsicquicCountResults> runnable = new Callable<PsicquicCountResults>() {
                        public PsicquicCountResults call() {

                            return processPsicquicQueries(service, psicquicQuery);
                        }
                    };

                    Future<PsicquicCountResults> f = executorService.submit(runnable);
                    runningTasks.add(f);
                }

                checkAndResumePsicquicTasks();

            } catch (PsicquicRegistryClientException e) {
                addErrorMessage("Problem counting results in other databases", "Registry not available");
                e.printStackTrace();
            }
        }
    }

    private void resetPsicquicCounts() {
        countInOtherDatabases = 0;
        otherDatabasesWithResults = 0;
        countInOtherDatabases = 0;
        otherImexDatabasesWithResults = 0;
        countInOtherImexDatabases = 0;
        nonRespondingDatabases = 0;
        nonRespondingImexDatabases = 0;
    }

    private void checkAndResumePsicquicTasks() {

        for (Future<PsicquicCountResults> f : runningTasks){
            try {
                PsicquicCountResults results = f.get(threadTimeOut, TimeUnit.SECONDS);

                if (results.isImex()){
                    if (results.isImexResponding() && results.getImexCount() > 0){
                        countInOtherImexDatabases += results.getImexCount();
                        otherImexDatabasesWithResults ++;
                    }
                    else if (!results.isImexResponding()) {
                        nonRespondingImexDatabases ++;
                    }
                }

                if (results.isServiceResponding() && results.getPsicquicCount() > 0){
                    countInOtherDatabases += results.getPsicquicCount();
                    otherDatabasesWithResults ++;
                }
                else if (!results.isServiceResponding()){
                    nonRespondingDatabases ++;
                }
            } catch (InterruptedException e) {
                log.error("The psicquic task was interrupted, we cancel the task.", e);
                f.cancel(true);
                this.nonRespondingDatabases ++;
                this.nonRespondingImexDatabases ++;
                if (!f.isCancelled()){
                    f.cancel(true);
                }
            } catch (ExecutionException e) {
                log.error("The psicquic task could not be executed, we cancel the task.", e);
                if (!f.isCancelled()){
                    f.cancel(true);
                }
            } catch (TimeoutException e) {
                log.error("Service task stopped because of time out " + threadTimeOut + "seconds.", e);
                this.nonRespondingDatabases ++;
                this.nonRespondingImexDatabases ++;

                if (!f.isCancelled()){
                   f.cancel(true);
                }
            }
        }

        runningTasks.clear();
    }

    private PsicquicCountResults processPsicquicQueries(ServiceType service, String query){
        PsicquicCountResults results = new PsicquicCountResults();

        if (isImexService(service)){
            results.setImex(true);
            collectCountFromImexService(service, query, results);
        }

        collectCountFromPsicquicService(service, query, results);

        return results;
    }

    private void collectCountFromPsicquicService(ServiceType service, String query, PsicquicCountResults results) {

        /*String url = null;
        HttpMethod method = null;
        try {

            String encoded = URLEncoder.encode(query, "UTF-8");
            encoded = encoded.replaceAll("\\+", "%20");

            url = service.getRestUrl()+"query/"+ encoded +"?format=count";


            HttpClient httpClient = intactViewConfiguration.getPsicquicHttpClient();

            method = createHttpMethodWithoutRetry(url);

            final int returnCode = httpClient.executeMethod(method);

            if (returnCode != 200) {
                log.error("HTTP Error " + returnCode + " connecting to PSICQUIC service '" + service.getName() + "': " + url + " / proxy " + intactViewConfiguration.getProxyHost() + ":" + intactViewConfiguration.getProxyPort());
                method.releaseConnection();

                results.setServiceResponding(false);

            } else {
                results.setServiceResponding(true);

                final InputStream input = method.getResponseBodyAsStream();
                String strCount = IOUtils.toString(input);
                input.close();
                results.setPsicquicCount(Integer.parseInt(strCount));

                method.releaseConnection();
            }
        } catch (IOException e) {
            log.error("Problem connecting to PSICQUIC service '"+service.getName()+"': "+url+" / proxy "+intactViewConfiguration.getProxyHost()+":"+intactViewConfiguration.getProxyPort(), e);

            if (method != null){
                method.releaseConnection();
            }

            results.setServiceResponding(false);
        }*/

        HttpURLConnection connection = null;
        String countUrl = null;
        try {
            String encoded = URLEncoder.encode(query, "UTF-8");
            encoded = encoded.replaceAll("\\+", "%20");

            String separator = (service.getRestUrl().endsWith( "/" ) ? "" : "/" );
            countUrl = service.getRestUrl() + separator + "query/" + encoded + "?format=count";

            URL url = new URL(countUrl);
            connection = (HttpURLConnection) url.openConnection();
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(5000);

            connection.connect();

            results.setServiceResponding(true);
            String strCount = IOUtils.toString(connection.getInputStream());
            results.setPsicquicCount(Integer.parseInt(strCount));

        } catch (SocketTimeoutException se) {
            log.error("Problem connecting to PSICQUIC service '"+service.getName()+"': "+countUrl, se);

            results.setServiceResponding(false);
        } catch (IOException e) {
            log.error("Problem connecting to PSICQUIC service '"+service.getName()+"': "+countUrl, e);

            results.setServiceResponding(false);
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    private void collectCountFromImexService(ServiceType service, String query, PsicquicCountResults results) {
        final String imexQuery = createImexQuery(query);
        results.setImex(true);

        /*String url = null;
        HttpMethod method = null;
        try {

            String encoded = URLEncoder.encode(imexQuery, "UTF-8");
            encoded = encoded.replaceAll("\\+", "%20");

            url = service.getRestUrl()+"query/"+ encoded +"?format=count";

            HttpClient httpClient = intactViewConfiguration.getPsicquicHttpClient();

            method = createHttpMethodWithoutRetry(url);

            final int returnCode = httpClient.executeMethod(method);

            if (returnCode != 200) {
                log.error("HTTP Error " + returnCode + " connecting to IMEx service '" + service.getName() + "': " + url + " / proxy " + intactViewConfiguration.getProxyHost() + ":" + intactViewConfiguration.getProxyPort());
                method.releaseConnection();

                results.setImexResponding(false);
            } else {
                results.setImexResponding(true);

                final InputStream input = method.getResponseBodyAsStream();
                String strCount = IOUtils.toString(input);
                input.close();
                results.setImexCount(Integer.parseInt(strCount));

                method.releaseConnection();
            }
        } catch (IOException e) {
            log.error("Problem connecting to PSICQUIC service '"+service.getName()+"': "+url+" / proxy "+intactViewConfiguration.getProxyHost()+":"+intactViewConfiguration.getProxyPort(), e);

            if (method != null){
                method.releaseConnection();
            }

            results.setImexResponding(false);
        }*/

        HttpURLConnection connection = null;
        String countUrl = null;
        try {
            String encoded = URLEncoder.encode(imexQuery, "UTF-8");
            encoded = encoded.replaceAll("\\+", "%20");

            String separator = (service.getRestUrl().endsWith( "/" ) ? "" : "/" );
            countUrl = service.getRestUrl() + separator + "query/" + encoded + "?format=count";

            URL url = new URL(countUrl);
            connection = (HttpURLConnection) url.openConnection();
            connection.setConnectTimeout(10000);
            connection.setReadTimeout(10000);

            connection.connect();

            results.setImexResponding(true);
            String strCount = IOUtils.toString(connection.getInputStream());
            results.setImexCount(Integer.parseInt(strCount));

        } catch (SocketTimeoutException se) {
            log.error("Problem connecting to IMEx service '"+service.getName()+"': "+countUrl, se);

            results.setImexResponding(false);
        } catch (IOException e) {
            log.error("Problem connecting to IMEx service '"+service.getName()+"': "+countUrl, e);

            results.setImexResponding(false);
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    private HttpMethod createHttpMethodWithoutRetry(String url) {
        HttpMethod method = new GetMethod(url);
        HttpMethodRetryHandler handler = new DefaultHttpMethodRetryHandler(0, false);
        HttpMethodParams params = new HttpMethodParams();
        params.setParameter(HttpMethodParams.RETRY_HANDLER, handler);
        method.setParams(params);

        return method;
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

    public int getNonRespondingDatabases() {
        return nonRespondingDatabases;
    }

    public int getNonRespondingImexDatabases() {
        return nonRespondingImexDatabases;
    }

    public int getThreadTimeOut() {
        return threadTimeOut;
    }

    public void setThreadTimeOut(int threadTimeOut) {
        this.threadTimeOut = threadTimeOut;
    }

    private class PsicquicCountResults {

        private int psicquicCount;
        private int imexCount;
        private boolean isServiceResponding;
        private boolean isImexResponding;
        private boolean isImex;

        public PsicquicCountResults(){

        }

        public boolean isImex() {
            return isImex;
        }

        public void setImex(boolean imex) {
            isImex = imex;
        }

        public int getPsicquicCount() {
            return psicquicCount;
        }

        public void setPsicquicCount(int psicquicCount) {
            this.psicquicCount = psicquicCount;
        }

        public int getImexCount() {
            return imexCount;
        }

        public void setImexCount(int imexCount) {
            this.imexCount = imexCount;
        }

        public boolean isServiceResponding() {
            return isServiceResponding;
        }

        public void setServiceResponding(boolean serviceResponding) {
            isServiceResponding = serviceResponding;
        }

        public boolean isImexResponding() {
            return isImexResponding;
        }

        public void setImexResponding(boolean imexResponding) {
            isImexResponding = imexResponding;
        }
    }

}
