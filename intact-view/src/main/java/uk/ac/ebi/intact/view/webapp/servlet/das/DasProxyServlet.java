/*
 * Copyright 2001-2008 The European Bioinformatics Institute.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package uk.ac.ebi.intact.view.webapp.servlet.das;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.net.*;
import java.util.HashMap;
import java.util.Map;

/**
 * Das Proxy implementation
 *
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 */
public class DasProxyServlet extends HttpServlet {

    /**
     * Sets up a logger for that class.
     */
    private static final Log log = LogFactory.getLog(DasProxyServlet.class);

    /**
     * Default timeout for connection is 3 seconds
     */
    private static final int DEFAULT_TIMEOUT = 3;

    /**
     * If using a proxy, the name the host
     */
    private String proxyHost = null;

    /**
     * if using a proxy, the port number
     */
    private Integer proxyPort = null;

    /**
     * If true, caching is enabled
     */
    private boolean cachingEnabled = true;


    private File cacheDir;

    /**
     * Stores the DAS status codes
     */
    private Map<Integer, String> dasStatusCodes;

    @Override
    public void init(ServletConfig config) throws ServletException {
        this.proxyHost = System.getProperty("proxyHost", null);
        String portValue = System.getProperty("proxyPort", null);

        if (portValue != null) {
            proxyPort = Integer.valueOf(portValue);
        }

        String cachingValue = config.getInitParameter("uk.ac.ebi.intact.ENABLE_CACHING");
        String cacheFolder = config.getInitParameter("uk.ac.ebi.intact.CACHE_DIR");

        if (cachingValue != null) {
            cachingEnabled = Boolean.valueOf(cachingValue);
        }
        if (cacheFolder != null) {
            cacheDir = new File(cacheFolder);
        } else {
            cacheDir = new File(System.getProperty("java.io.tmpdir"), "dasty/");
        }

        // status codes
        this.dasStatusCodes = new HashMap<Integer, String>();
        dasStatusCodes.put(200, "OK, data follows");
        dasStatusCodes.put(400, "Bad command");
        dasStatusCodes.put(401, "Bad data source");
        dasStatusCodes.put(402, "Bad command arguments");
        dasStatusCodes.put(403, "Bad reference object");
        dasStatusCodes.put(404, "Bad stylesheet");
        dasStatusCodes.put(405, "Coordinate error");
        dasStatusCodes.put(500, "Server error");
        dasStatusCodes.put(501, "Unimplemented feature");
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

        String urlValue = req.getParameter("url");
        String serverUrl = req.getParameter("s");
        String method = req.getParameter("m");
        String query = req.getParameter("q");
        String timeoutValue = req.getParameter("t");
        String regAuthority = req.getParameter("a");
        String regLabel = req.getParameter("l");
        String regType = req.getParameter("c");

        // This is the parameter used in the Dasty3 adn BioJS proxy
        if(urlValue != null){
            serverUrl = urlValue;
            method = "url";
        } else {
            // add trailing slash to the server if missing
            if (!serverUrl.endsWith("/")) {
                serverUrl = serverUrl + "/";
            }
        }

        if (log.isTraceEnabled()) {
            log.trace("Received request: " + req.getRequestURL() + "?" + req.getQueryString());
        }



//        if (log.isTraceEnabled()) {
//            log.debug("Received request for: serverUrl="+serverUrl+"; "+method+"="+method+"; query="+query+
//            "; timeout="+timeoutValue+"; regAuthority="+regAuthority+"; regLabel="+regLabel+"; regType="+regType);
//        }

        // define the timeout
        int timeout;

        if (timeoutValue != null) {
            timeout = Integer.parseInt(timeoutValue);
        } else {
            timeout = DEFAULT_TIMEOUT;
        }

        // this represents a cacheFile
        final CacheFile cachedFile = new CacheFile(cacheDir, method, query);

        // check the cache if necessary

        InputStream inputStreamToReturn = null;
        int dasCode = 200;
        CacheWriter cacheWriter = null;

        try{
            // check if the answer is cached, otherwise execute the URL query to destination server
            if (cachingEnabled && cachedFile.getFile().exists()) {
                // file is in cache
                if (log.isTraceEnabled()) log.trace("File found in cache: "+cachedFile.getFile());

                inputStreamToReturn = new FileInputStream(cachedFile.getFile());
            } else {
                // generate the URL to the DAS Server
                String urlStr = generateUrl(serverUrl, method, query, regAuthority, regLabel, regType);

                URL url = new URL(urlStr);

                if (log.isDebugEnabled()) log.debug("Connecting to URL: " + url);

                // Create the URL connection, using the http proxy if necessary
                URLConnection urlConnection;

                if (proxyHost != null) {
                    urlConnection = url.openConnection(new Proxy(Proxy.Type.HTTP, new InetSocketAddress(proxyHost, proxyPort)));
                } else {
                    urlConnection = url.openConnection();
                }

                urlConnection.setConnectTimeout(timeout * 1000);
                urlConnection.setReadTimeout(10 * 1000);

                try {
                    urlConnection.connect();

                    if ("pdb".equals(method)) {
                        resp.setContentType("text/plain");
                    } else {
                        resp.setContentType("text/xml");
                    }

                    // check the das status code
                    String codeValue = urlConnection.getHeaderField("X-Das-Status");

                    // evaluate the DAS status code
                    if (codeValue != null) {
                        dasCode = Integer.parseInt(codeValue.split(" ")[0]);
                    } else {
                        dasCode = 200;
                    }

                    inputStreamToReturn = urlConnection.getInputStream();

                    cacheWriter = new CacheWriter(cachedFile);

                } catch (Exception e) {
                    dasCode = 401;
                    log.error("Problem opening connection to: "+urlStr+" - "+e.getMessage());
                }
            }

            // return the response if the code is 200, otherwise return an exception snippet
            if (dasCode == 200) {
                writeResponse(resp, cacheWriter, inputStreamToReturn);
            } else {
                failWithMessage(resp, dasCode + " " + dasStatusCodes.get(dasCode));
            }
        }
        finally {
            if (cachingEnabled && cacheWriter != null) {
                cacheWriter.close();
            }
            if (inputStreamToReturn != null){
                inputStreamToReturn.close();
            }
        }
    }

    private void writeResponse(HttpServletResponse resp, CacheWriter cacheWriter, InputStream input) throws ServletException {
        try {
            BufferedReader in = new BufferedReader(new InputStreamReader(input));
            try{
                String line;
                while ((line = in.readLine()) != null) {
                    final String lineStr = line + System.getProperty("line.separator");
                    resp.getWriter().write(lineStr);

                    if (cachingEnabled && cacheWriter != null) {
                        cacheWriter.write(lineStr);
                    }
                }
            }
            finally {
                in.close();
            }

        } catch (IOException e) {
            throw new ServletException(e);
        }
    }

    private String generateUrl(String serverUrl, String method, String id,
                               String regAuthority, String regLabel, String regType
    ) throws ServletException {
        StringBuilder url = new StringBuilder();
        url.append(serverUrl);

        if ("sequence".equals(method) || "features".equals(method)) {
            url.append(method).append("?segment=").append(encode(id));
        } else if ("pdb".equals(method)) {
            url.append(id);
        } else if ("alignment".equals(method)) {
            url.append(method).append("?query=").append(encode(id));
        } else if ("ontology".equals(method)) {
            url.append(id);
        } else if ("types".equals(method) || "stylesheet".equals(encode(method))) {
            url.append(method);
        } else if ("registry".equals(method)) {
            String appendChar = "?";
            url.append(appendChar);
            if (regAuthority != null) {
                url.append("&authority=").append(encode(regAuthority));
            }
            if (regLabel != null) {
                url.append("&label=").append(encode(regLabel));
            }
            if (regType != null) {
                url.append("&type=").append(encode(regType));
            }
        } else if ("url".equals(method)) {
            // No need to apend anything. Just the URL.
        } else {
            throw new ServletException("Method not supported: " + method);
        }

        return url.toString();
    }

    public void failWithMessage(HttpServletResponse response, String message) throws IOException {
        response.getWriter().write("<exception>" + message + "</exception>");
        if (log.isErrorEnabled()) log.error(message);
    }

    private static String encode(String valueToEncode) {
        try {
            return URLEncoder.encode(valueToEncode, "utf-8");
        } catch (UnsupportedEncodingException e) {
            // should never happen
            e.printStackTrace();
        }
        return valueToEncode;
    }

    private class CacheFile {

        private File cacheDir;
        private String method;
        private String query;

        private CacheFile(File cacheDir, String method, String query) {
            this.cacheDir = cacheDir;
            this.method = method;
            this.query = query;
        }

        public File getCacheDir() {
            return cacheDir;
        }

        public String getMethod() {
            return method;
        }

        public String getQuery() {
            return query;
        }

        public File getFile() {
            String queryPart = (query != null)? "/"+query : "";
            return new File(cacheDir, method + queryPart);
        }
    }

    private class CacheWriter extends Writer {

        private FileWriter fileWriter;
        private CacheFile cacheFile;

        private CacheWriter(CacheFile cacheFile) throws IOException {
            this.cacheFile = cacheFile;
        }

        public void write(char cbuf[], int off, int len) throws IOException {
              if (canFileBeCached(cacheFile)) {
                getWriter().write(cbuf, off, len);
            }
        }

        public Writer getWriter() throws IOException{
            if (fileWriter == null) {
                checkFile();
                fileWriter = new FileWriter(cacheFile.getFile());
            }

            return fileWriter;
        }

        public void flush() throws IOException {
            if (canFileBeCached(cacheFile) && cacheFile.getFile().exists()) {
                getWriter().flush();
            }
        }

        public void close() throws IOException {
            if (canFileBeCached(cacheFile) && cacheFile.getFile().exists()) {
                getWriter().close();
            }
        }

        private void checkFile() {
            if (!cacheFile.getFile().getParentFile().exists()) {
                cacheFile.getFile().getParentFile().mkdirs();
            }
        }

        private boolean canFileBeCached(CacheFile cacheFile) {
            return ("ontology".equals(cacheFile.getMethod()) ||
                    "stylesheet".equals(cacheFile.getMethod()));
        }
    }
}