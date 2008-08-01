/**
 * Copyright 2007 The European Bioinformatics Institute, and others.
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
 *  limitations under the License.
 */
package uk.ac.ebi.intact.view.webapp.util;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import uk.ac.ebi.intact.view.webapp.controller.application.OlsBean;
import uk.ac.ebi.intact.view.webapp.IntactViewException;
import uk.ac.ebi.intact.binarysearch.webapp.generated.SearchConfig;
import uk.ac.ebi.intact.util.DesEncrypter;
import uk.ac.ebi.intact.util.ols.Term;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import javax.faces.context.FacesContext;
import javax.servlet.ServletContext;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import java.io.*;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;

/**
 * TODO comment this!
 *
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 */
public class WebappUtils {

    private static final Log log = LogFactory.getLog(WebappUtils.class);

    public static final String INTERACTION_TYPE_TERM = WebappUtils.class + ".INTERACTION_TYPE_TERM";
    public static final String INTERACTION_TYPES = WebappUtils.class + ".INTERACTION_TYPES";
    public static final String DETECTION_METHOD_TERM = WebappUtils.class + ".DETECTION_METHOD_TERM";
    public static final String DETECTION_METHODS = WebappUtils.class + ".DETECTION_METHODS";


    private static final String SECRET_INIT_PARAM_NAME = "psidev.SECRET";

    private WebappUtils() {
    }

    public static SearchConfig readConfiguration(String configFileXml) throws IntactViewException {
        File file = new File(configFileXml);

        if (!file.exists()) return null;
        if (file.isDirectory()) return null;

        SearchConfig config;
        try {
            config = (SearchConfig) readConfigXml(new FileInputStream(file));
        }
        catch (Exception e) {
            throw new IntactViewException(e);
        }

        return config;
    }

    public static synchronized void writeConfiguration(SearchConfig config, OutputStream output) {
        try {
            JAXBContext jc = JAXBContext.newInstance(SearchConfig.class.getPackage().getName());
            Marshaller marshaller = jc.createMarshaller();
            marshaller.setProperty(Marshaller.JAXB_ENCODING, "UTF-8");
            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
            marshaller.marshal(config, output);
        }
        catch (JAXBException e) {
            throw new IntactViewException(e);
        }
    }

    public static void writeConfiguration(SearchConfig config, File outputFile) {
        try {
            FileOutputStream fos = new FileOutputStream(outputFile);
            writeConfiguration(config, fos);
            fos.close();
        }
        catch (IOException e) {
            throw new IntactViewException(e);
        }
    }

    public static SearchConfig.Indexes.Index getDefaultIndex(SearchConfig config) {

        for (SearchConfig.Indexes.Index index : config.getIndexes().getIndices()) {
            if (index.isDefault()) {
                return index;
            }
        }

        return null;
    }

    private static Object readConfigXml(InputStream is) throws JAXBException {
        JAXBContext jc = JAXBContext.newInstance(SearchConfig.class.getPackage().getName());
        Unmarshaller unmarshaller = jc.createUnmarshaller();
        return unmarshaller.unmarshal(is);
    }

    public static String encrypt(FacesContext facesContext, String str) throws IntactViewException {
        return new DesEncrypter(secretKey(facesContext)).encrypt(str);
    }

    public static String decrypt(FacesContext facesContext, String str) throws IntactViewException {
        return new DesEncrypter(secretKey(facesContext)).decrypt(str);
    }

    public static int countItemsInIndex(String directory) throws IOException {
        Directory indexDir = FSDirectory.getDirectory(directory, false);

        IndexReader reader = new IndexSearcher(indexDir).getIndexReader();
        int items = reader.maxDoc();
        reader.close();

        return items;
    }

    private static SecretKey secretKey(FacesContext facesContext) {
        try {
            String secret = facesContext.getExternalContext().getInitParameter(SECRET_INIT_PARAM_NAME);
            byte[] bytes = new Base64().decode(secret.getBytes());

            return new SecretKeySpec(bytes, "DES");
        }
        catch (Exception e) {
            throw new IntactViewException(e);
        }
    }

    public static void loadOlsTerms(ServletContext ctx) throws RemoteException {
        if (log.isInfoEnabled()) log.info("Loading OLS terms using the Web Service");

        if (log.isDebugEnabled()) log.debug("\tLoading Interaction Types...");
        Term interactionTypeTerm = uk.ac.ebi.intact.util.ols.OlsUtils.getMiTerm("MI:0190");

        if (log.isDebugEnabled()) log.debug("\tLoading Interaction Detection Methods...");
        Term detectionMethodTerm = uk.ac.ebi.intact.util.ols.OlsUtils.getMiTerm("MI:0001");

        List<Term> interactionTypeTerms = OlsBean.childrenFor(interactionTypeTerm, new ArrayList<Term>());
        List<Term> detectionMethodTerms = OlsBean.childrenFor(detectionMethodTerm, new ArrayList<Term>());

        ctx.setAttribute(INTERACTION_TYPE_TERM, interactionTypeTerm);
        ctx.setAttribute(INTERACTION_TYPES, interactionTypeTerms);
        ctx.setAttribute(DETECTION_METHOD_TERM, detectionMethodTerm);
        ctx.setAttribute(DETECTION_METHODS, detectionMethodTerms);
    }
}