package uk.ac.ebi.intact.widgets.webapp.servlet;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.GetMethod;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.net.URLDecoder;

/**
 * Created by IntelliJ IDEA.
 * User: cjandras
 * Date: 07/07/11
 * Time: 13:22
 * To change this template use File | Settings | File Templates.
 */
public class Proxy extends HttpServlet {
    private static final String NEW_LINE = System.getProperty("line.separator");
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

        try {
            String url = req.getParameter("url");
            if (url == null) {
                throw new ServletException("Parameter 'url' was expected");
            }

            url = URLDecoder.decode(url, "UTF-8");
            externalRequest(url, resp.getWriter());

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

  /**
     * Calls url and writes response on Writer
     * @param url url to call
     * @param outputWriter writer for response
     * @throws IOException
     */
    private void externalRequest(String url, Writer outputWriter) throws IOException {
        HttpClient client = new HttpClient();
        GetMethod method = new GetMethod(url);

        int statusCode = client.executeMethod(method);

        InputStream inputStream = method.getResponseBodyAsStream();
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));

        try{
            String line = "";
            while ((line = bufferedReader.readLine()) != null) {
                outputWriter.append(line + '\n');
            }
            outputWriter.flush();
        }
        finally {
            bufferedReader.close();
            inputStream.close();
        }
    }
}
