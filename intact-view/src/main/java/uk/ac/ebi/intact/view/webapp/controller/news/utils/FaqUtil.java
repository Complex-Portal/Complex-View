/**
 * 
 */
package uk.ac.ebi.intact.view.webapp.controller.news.utils;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import uk.ac.ebi.intact.view.webapp.controller.news.items.Faq;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import java.io.InputStream;
import java.net.URL;

/**
 *
 * @author Erik Pfeiffenberger (epfeif@ebi.ac.uk)
 * @version $Id
 */
public class FaqUtil {

    private static final Log log = LogFactory.getLog( FaqUtil.class );
    
    private FaqUtil() {
    }

    public static Faq readFaq(String faqXml) {
        Faq objFaq;
        try {
            URL datasetsUrl = new URL(faqXml);
            InputStream datasetStream = datasetsUrl.openStream();

            try{
                objFaq = (Faq) readFaqXml(datasetStream);
            }
            finally {
                datasetStream.close();
            }

        } catch (Throwable e) {
            log.error("Error while loading FAQ from: " + faqXml, e);
            objFaq = new Faq();
        }

        return objFaq;
    }
    
    private static Object readFaqXml(InputStream is) throws JAXBException {
        JAXBContext jc = JAXBContext.newInstance(Faq.class.getPackage().getName());
        Unmarshaller unmarshaller = jc.createUnmarshaller();
        return unmarshaller.unmarshal(is);
    }
}