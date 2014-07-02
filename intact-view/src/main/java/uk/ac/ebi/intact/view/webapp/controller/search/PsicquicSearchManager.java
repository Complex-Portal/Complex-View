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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hupo.psi.mi.psicquic.registry.ServiceType;
import org.hupo.psi.mi.psicquic.registry.client.PsicquicRegistryClientException;
import org.hupo.psi.mi.psicquic.registry.client.registry.DefaultPsicquicRegistryClient;
import org.hupo.psi.mi.psicquic.registry.client.registry.PsicquicRegistryClient;
import org.hupo.psi.mi.psicquic.wsclient.PsicquicSimpleClient;
import uk.ac.ebi.intact.view.webapp.controller.config.IntactViewConfiguration;

import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

/**
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 */
public class PsicquicSearchManager {

    private static final Log log = LogFactory.getLog(PsicquicSearchManager.class);

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

    private ExecutorService executorService;
    private IntactViewConfiguration intactViewConfiguration;

    public PsicquicSearchManager(ExecutorService executorService, IntactViewConfiguration intactViewConfiguration) {
        if (executorService == null){
            throw new NullPointerException("The psicquicController needs an executorService.");
        }
        if (intactViewConfiguration == null){
            throw new NullPointerException("The psicquicController needs an intactViewConfiguration.");
        }
        this.executorService = executorService;
        this.intactViewConfiguration = intactViewConfiguration;

        try {
            initializeServices();
        } catch (PsicquicRegistryClientException e) {
            FacesContext context = FacesContext.getCurrentInstance();
            if (context != null){
                FacesMessage facesMessage = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Problem counting results in other databases", "Registry not available");
                context.addMessage(null, facesMessage);
            }

            e.printStackTrace();
        }
    }

    public void initializeServices() throws PsicquicRegistryClientException {

        final String psicquicRegistryUrl = intactViewConfiguration.getPsicquicRegistryUrl();

        if (psicquicRegistryUrl == null || psicquicRegistryUrl.length() == 0) {
            return;
        }

        if (services == null || services.isEmpty()) {
            try{
                PsicquicRegistryClient registryClient = new DefaultPsicquicRegistryClient(psicquicRegistryUrl);
                services = registryClient.listActiveServices();
            }
            catch (Throwable e){
                log.error("PSICQUIC registry not available", e);
                services = new ArrayList<ServiceType>();
            }
        }

        if (imexServices == null || imexServices.isEmpty()){
            imexServices = new ArrayList<ServiceType>(services.size());

            for (final ServiceType service : services) {
                if (isImexService(service)){
                    imexServices.add(service);
                }

                // initialise psicquic services
                if (!intactViewConfiguration.getPsicquicClientMap().containsKey(service.getRestUrl())){
                    intactViewConfiguration.getPsicquicClient(service.getRestUrl());
                }
            }
        }

        if (runningTasks == null){
            runningTasks = new ArrayList<Future>();
        }
    }

    private boolean isImexService(ServiceType service){
        return service.getTags().contains("MI:0959");
    }

    public void countResultsInOtherDatabases(String query) {
        resetPsicquicCounts();

        if (query == null || query.length() == 0) {
            query = UserQuery.STAR_QUERY;
        }
        runningTasks.clear();

        try {
            if (services == null){
                initializeServices();
            }
            final String psicquicQuery = query;

            for (final ServiceType service : services) {
                final PsicquicSimpleClient client = intactViewConfiguration.getPsicquicClient(service.getRestUrl());

                if (intactViewConfiguration.getWebappName().contains(service.getName()) || intactViewConfiguration.getDatabaseNamesUsingSameSolr().contains(service.getName())) {
                    continue;
                }

                Callable<PsicquicCountResults> runnable = new Callable<PsicquicCountResults>() {
                    public PsicquicCountResults call() {

                        return processPsicquicQueries(service, psicquicQuery, client);
                    }
                };

                Future<PsicquicCountResults> f = executorService.submit(runnable);
                runningTasks.add(f);
            }
        } catch (PsicquicRegistryClientException e) {
            FacesContext context = FacesContext.getCurrentInstance();
            if (context != null){
                FacesMessage facesMessage = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Problem counting results in other databases", "Registry not available");
                context.addMessage(null, facesMessage);
            }

            e.printStackTrace();
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

    public void checkAndResumePsicquicTasks() {
        List<Future> currentRunningTasks = new ArrayList<Future>(runningTasks);

        for (Future<PsicquicCountResults> f : currentRunningTasks){
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

                runningTasks.remove(f);
            } catch (InterruptedException e) {
                log.error("The psicquic task was interrupted, we cancel the task.", e);
                this.nonRespondingDatabases ++;
                this.nonRespondingImexDatabases ++;
                if (!f.isCancelled()){
                    f.cancel(false);
                }
                runningTasks.remove(f);
            } catch (ExecutionException e) {
                log.error("The psicquic task could not be executed, we cancel the task.", e);
                if (!f.isCancelled()){
                    f.cancel(false);
                }
                runningTasks.remove(f);
            } catch (TimeoutException e) {
                log.error("Service task stopped because of time out " + threadTimeOut + "seconds.");
                this.nonRespondingDatabases ++;
                this.nonRespondingImexDatabases ++;

                if (!f.isCancelled()){
                    f.cancel(false);
                }
                runningTasks.remove(f);
            }
            catch (Throwable e) {
                log.error("The psicquic task could not be executed, we cancel the task.", e);
                if (!f.isCancelled()){
                    f.cancel(false);
                }
                runningTasks.remove(f);
            }
        }
    }

    private PsicquicCountResults processPsicquicQueries(ServiceType service, String query, PsicquicSimpleClient client){
        PsicquicCountResults results = new PsicquicCountResults();

        if (isImexService(service)){
            results.setImex(true);
            collectCountFromImexService(service, query, results, client);
        }

        collectCountFromPsicquicService(service, query, results, client);

        return results;
    }

    private void collectCountFromPsicquicService(ServiceType service, String query, PsicquicCountResults results, PsicquicSimpleClient client) {

        try {

            long count = client.countByQuery(query);

            results.setPsicquicCount((int) count);
            results.setServiceResponding(true);

        } catch (IOException e) {
            log.error("Problem connecting to PSICQUIC service '"+service.getName()+"': "+service.getRestUrl()+" / proxy "+intactViewConfiguration.getProxyHost()+":"+intactViewConfiguration.getProxyPort(), e);

            results.setServiceResponding(false);
        }
    }

    private void collectCountFromImexService(ServiceType service, String query, PsicquicCountResults results, PsicquicSimpleClient client) {
        final String imexQuery = createImexQuery(query);
        results.setImex(true);

        try {

            long count = client.countByQuery(imexQuery);

            results.setImexCount((int) count);
            results.setImexResponding(true);

        } catch (IOException e) {
            log.error("Problem connecting to PSICQUIC service '"+service.getName()+"': / proxy "+intactViewConfiguration.getProxyHost()+":"+intactViewConfiguration.getProxyPort(), e);

            results.setImexResponding(false);
        }
    }

    private String createImexQuery(String query) {
        String filter = "interaction_id:imex";

        if (query != null && query.trim().length() > 0 && !"*".equals(query.trim())) {
            return filter+" AND ("+query.trim()+")";
        } else {
            return filter;
        }
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

    public List<Future> getRunningTasks() {
        return runningTasks;
    }
}
