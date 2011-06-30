/**
 * Copyright 2011 The European Bioinformatics Institute, and others.
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
package uk.ac.ebi.intact.view.webapp.servlet;

import net.sf.saxon.TransformerFactoryImpl;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.myfaces.orchestra.conversation.annotations.ConversationName;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.annotation.Transactional;
import psidev.psi.mi.xml.PsimiXmlVersion;
import psidev.psi.mi.xml.PsimiXmlWriter;
import psidev.psi.mi.xml.model.EntrySet;
import psidev.psi.mi.xml.stylesheets.XslTransformException;
import uk.ac.ebi.intact.core.context.IntactContext;
import uk.ac.ebi.intact.core.persistence.dao.entry.IntactEntryFactory;
import uk.ac.ebi.intact.dataexchange.psimi.xml.exchange.PsiExchange;
import uk.ac.ebi.intact.model.IntactEntry;

import javax.persistence.FlushModeType;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.URIResolver;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import java.io.*;
import java.net.URLDecoder;

/**
 * Outputs JSON corresponding to an interaction.
 *
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 */
@Component
public class JsonExporter extends HttpServlet {

    private static final String NEW_LINE = System.getProperty("line.separator");

    private class CustomURIResolver implements URIResolver{

        private String path;

        public CustomURIResolver(String path){
            this.path = path;
        }

        public Source resolve(String href, String base){
            return new StreamSource(new File(path + "/" + href));
        }
    }

    @Override
    @Transactional(readOnly = true)
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String interactionAc = req.getParameter("ac");
        String url = req.getParameter("url");
        if (interactionAc == null && url == null) {
            throw new ServletException("Parameter 'ac' or 'url' was expected");
        }

        if(interactionAc != null && url != null){
            throw new ServletException("Only parameter 'ac' OR 'url' was expected");
        }

        if(interactionAc != null){
            internalRequest(interactionAc, resp.getWriter());
        }else{
            url = URLDecoder.decode(url, "UTF-8");
            externalRequest(url, resp.getWriter());
        }
    }

    private void internalRequest(String interactionAc, Writer outputWriter) throws  ServletException{
        InputStream xmlStream = createXmlStream(interactionAc);
        InputStream xslt = JsonExporter.class.getResourceAsStream("/META-INF/ConvertXMLToJSONInteraction.xslt");
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(xmlStream));

        try {
            runXslt(bufferedReader, xslt, outputWriter);
        } catch (XslTransformException e) {
            throw new ServletException("Problem writing JSON", e);
        }
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

    private InputStream createXmlStream(String interactionAc) throws ServletException {
        EntrySet entrySet = createEntrySet(interactionAc);

        PsimiXmlWriter writer = new PsimiXmlWriter(PsimiXmlVersion.VERSION_254);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        try {
            writer.write(entrySet, baos);
        } catch (Exception e) {
            throw new ServletException("Problem writing XML", e);
        }

        return new ByteArrayInputStream(baos.toByteArray());
    }

    private EntrySet createEntrySet(String interactionAc) {
        IntactContext context = IntactContext.getCurrentInstance();

        context.getDaoFactory().getEntityManager().setFlushMode(FlushModeType.COMMIT);
        IntactEntry intactEntry = IntactEntryFactory.createIntactEntry(context)
                .addInteractionWithAc(interactionAc);

        EntrySet entrySet = createEntrySet(intactEntry);

        return entrySet;
    }

    private EntrySet createEntrySet(IntactEntry intactEntry) {
        PsiExchange psiExchange = (PsiExchange) IntactContext.getCurrentInstance().getSpringContext().getBean("psiExchange");
        EntrySet entrySet = psiExchange.exportToEntrySet(intactEntry);

        return entrySet;
    }

    private void runXslt( Reader inputReader, InputStream xslt, Writer outputWriter ) throws XslTransformException {
        // JAXP reads data using the Source interface

        Source xmlSource = new StreamSource( inputReader );
        Source xsltSource = null;
        String path = "";

        xsltSource = new StreamSource( xslt );

        // the factory pattern supports different XSLT processors
        // Saxon Transformer used because of need for XSLT 2.0
        TransformerFactory transFact = new TransformerFactoryImpl();

        // setting the path for xsl:include in xslt file
        transFact.setURIResolver(new CustomURIResolver("src/main/resources/META-INF/"));
        try {
            Transformer trans = transFact.newTransformer( xsltSource );
            trans.transform(xmlSource, new StreamResult(outputWriter));

        } catch ( Exception e ) {
            throw new XslTransformException( "An error occured while transforming the XML", e );
        }
    }
}
