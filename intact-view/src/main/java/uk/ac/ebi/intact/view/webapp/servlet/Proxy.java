package uk.ac.ebi.intact.view.webapp.servlet;

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
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

        String url = req.getParameter("url");
        if (url == null) {
            throw new ServletException("Parameter 'url' was expected");
        }

        url = URLDecoder.decode(url, "UTF-8");
        externalRequest(url, resp.getWriter());
    }

    // the jsonExporter also acts as an Proxy for the internal javascript AJAX requests
    private void externalRequest(String url, Writer outputWriter) throws IOException {
        HttpClient client = new HttpClient();
        GetMethod method = new GetMethod(url);

        int statusCode = client.executeMethod(method);
        InputStream inputStream = method.getResponseBodyAsStream();
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));

        String line = "";
        while ((line = bufferedReader.readLine()) != null){
            outputWriter.append(line);
        }
        bufferedReader.close();
    }
}
