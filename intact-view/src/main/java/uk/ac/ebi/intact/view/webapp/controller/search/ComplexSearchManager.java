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
import uk.ac.ebi.intact.view.webapp.controller.config.IntactViewConfiguration;

import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

/**
 * @author Marine Dumousseau (marine@ebi.ac.uk)
 */
public class ComplexSearchManager {

    private static final Log log = LogFactory.getLog(ComplexSearchManager.class);

    private List<Future> runningTasks;
    private int complexCount = -1;
    private int threadTimeOut = 5;
    private boolean isComplexServiceResponding = true;
    private int connectionTimeout = 5000;
    private int readTimeout = 5000;

    private ExecutorService executorService;
    private IntactViewConfiguration intactViewConfiguration;

    public ComplexSearchManager(ExecutorService executorService, IntactViewConfiguration intactViewConfiguration) {
        if (executorService == null){
            throw new NullPointerException("The ComplexSearchManager needs an executorService.");
        }
        if (intactViewConfiguration == null){
            throw new NullPointerException("The ComplexSearchManager needs an intactViewConfiguration.");
        }
        this.executorService = executorService;
        this.intactViewConfiguration = intactViewConfiguration;

        runningTasks = new ArrayList<Future>();
    }

    public void countNumberOfComplexes(String query) {
        resetComplexCounts();

        if (query == null || query.length() == 0) {
            query = UserQuery.STAR_QUERY;
        }
        runningTasks.clear();

        try {
            final String complexQuery = query;

            if (intactViewConfiguration.getComplexWsUrl() == null) {
                isComplexServiceResponding = false;
                complexCount=-1;
            }

            Callable<Long> runnable = new Callable<Long>() {
                public Long call() {

                    return collectCountFromIntactComplexService(complexQuery, intactViewConfiguration.getComplexWsUrl());
                }
            };

            Future<Long> f = executorService.submit(runnable);
            runningTasks.add(f);
        } catch (Exception e) {
            FacesContext context = FacesContext.getCurrentInstance();
            if (context != null){
                FacesMessage facesMessage = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Problem counting IntAct complex search results", "Complex webservice not available");
                context.addMessage(null, facesMessage);
            }

            e.printStackTrace();
        }
    }

    private void resetComplexCounts() {
        complexCount = 0;
        isComplexServiceResponding = true;
    }

    public void checkAndResumeComplexTasks() {
        List<Future> currentRunningTasks = new ArrayList<Future>(runningTasks);

        for (Future<Long> f : currentRunningTasks){
            try {
                Long results = f.get(threadTimeOut, TimeUnit.SECONDS);

                if (results == null){
                    isComplexServiceResponding = false;
                    complexCount = -1;
                }
                else{
                    isComplexServiceResponding = true;
                    complexCount = results.intValue();
                }

                runningTasks.remove(f);
            } catch (InterruptedException e) {
                log.error("The complex search task was interrupted, we cancel the task.", e);
                this.isComplexServiceResponding = false;
                if (!f.isCancelled()){
                    f.cancel(false);
                }
                runningTasks.remove(f);
            } catch (ExecutionException e) {
                log.error("The complex search task could not be executed, we cancel the task.", e);
                if (!f.isCancelled()){
                    f.cancel(false);
                }
                runningTasks.remove(f);
            }  catch (TimeoutException e) {
                log.error("Service task stopped because of time out " + threadTimeOut + "seconds.");
                this.isComplexServiceResponding = false;

                if (!f.isCancelled()){
                    f.cancel(false);
                }
                runningTasks.remove(f);
            }
            catch (Throwable e) {
                log.error("The complex search task could not be executed, we cancel the task.", e);
                if (!f.isCancelled()){
                    f.cancel(false);
                }
                runningTasks.remove(f);
            }
        }
    }

    private Long collectCountFromIntactComplexService(String query, String complexWsUrl) {

        try {

            return countBy(query, complexWsUrl);
        } catch (IOException e) {
            log.error("Problem connecting to IntAct Complex service", e);

        }
        return null;
    }

    public int getComplexCount() {
        return complexCount;
    }

    public boolean isComplexServiceResponding() {
        return isComplexServiceResponding;
    }

    public int getThreadTimeOut() {
        return threadTimeOut;
    }

    public void setThreadTimeOut(int threadTimeOut) {
        this.threadTimeOut = threadTimeOut;
    }

    public int getReadTimeout() {
        return readTimeout;
    }

    public void setReadTimeout(int readTimeout) {
        this.readTimeout = readTimeout;
    }

    public int getConnectionTimeout() {
        return connectionTimeout;
    }

    public void setConnectionTimeout(int connectionTimeout) {
        this.connectionTimeout = connectionTimeout;
    }

    public List<Future> getRunningTasks() {
        return runningTasks;
    }

    private Long countBy(String query, String complexWsUrl) throws IOException {
        HttpURLConnection connection = null;
        final String encodedQuery = encodeQuery(query);
        InputStream inputStream = null;

        URL url = createUrl(encodedQuery, complexWsUrl);
        if (url == null){
            return null;
        }

        String strCount;
        try{
            connection = (HttpURLConnection) url.openConnection();

            connection.setConnectTimeout(connectionTimeout);
            connection.setReadTimeout(readTimeout);

            connection.connect();

            inputStream = connection.getInputStream();

            strCount = streamToString(inputStream);
            strCount = strCount.replaceAll("\n", "");
        }
        finally {
            if (inputStream != null){
               inputStream.close();
            }
            if (connection != null){
                connection.disconnect();
            }
        }

        return strCount.length() > 0 ? Long.parseLong(strCount) : null;
    }

    private String encodeQuery(String query) {
        String encodedQuery;

        try {
            encodedQuery = URLEncoder.encode(query, "UTF-8");
            encodedQuery = encodedQuery.replaceAll("%22", "&quot;");
            encodedQuery = encodedQuery.replaceAll("\\+", "%20");
        } catch (UnsupportedEncodingException e) {
            throw new IllegalStateException("UTF-8 should be supported");
        }
        return encodedQuery;
    }

    private URL createUrl(String encodedQuery, String complexWsUrl) {
        if (complexWsUrl == null){
            return null;
        }
        String strUrl =complexWsUrl +"/count/"+encodedQuery;

        URL url;
        try {
            url = new URL(strUrl);
        } catch (MalformedURLException e) {
            throw new IllegalArgumentException("Problem creating URL: "+strUrl, e);
        }
        return url;
    }

    private String streamToString(InputStream is) throws IOException {
        if (is == null) return "";

        StringBuilder sb = new StringBuilder();
        String line;

        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
            try {
                while ((line = reader.readLine()) != null) {
                    sb.append(line).append("\n");
                }
            }
            finally{
                if (reader != null){
                    reader.close();
                }
            }

        } finally {
            if (is != null){
                is.close();
            }
        }
        return sb.toString();
    }
}
