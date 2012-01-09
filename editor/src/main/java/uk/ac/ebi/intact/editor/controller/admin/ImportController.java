package uk.ac.ebi.intact.editor.controller.admin;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.myfaces.orchestra.conversation.annotations.ConversationName;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;
import uk.ac.ebi.intact.core.context.IntactContext;
import uk.ac.ebi.intact.dataexchange.psimi.xml.exchange.PsiExchange;
import uk.ac.ebi.intact.dataexchange.psimi.xml.exchange.PsiExchangeFactory;
import uk.ac.ebi.intact.editor.controller.BaseController;

import javax.faces.event.ActionEvent;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;

/**
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 */
@Controller
@Scope( "conversation.access" )
@ConversationName( "admin" )
public class ImportController extends BaseController {

    private static final Log log = LogFactory.getLog( ImportController.class );

    private URL[] urlsToImport;

    public void createSampleData(ActionEvent evt) {
        CvManagement cvManagement = (CvManagement) IntactContext.getCurrentInstance().getSpringContext().getBean("cvManagement");
        cvManagement.updateCvs();

        try {
            urlsToImport = new URL[] {new URL("ftp://ftp.ebi.ac.uk/pub/databases/intact/2010-05-07/psi25/pmid/2010/10037752.xml"),
                           new URL("ftp://ftp.ebi.ac.uk/pub/databases/intact/2010-05-07/psi25/pmid/2010/10570129.xml")};
            startImport(evt);
        } catch (MalformedURLException e) {
            throw new RuntimeException("Problem importing data", e);
        }
    }

    public void startImport(ActionEvent evt) {
        final PsiExchange psiExchange = PsiExchangeFactory.createPsiExchange(IntactContext.getCurrentInstance().getSpringContext());

//        ExecutorService executorService = Executors.newSingleThreadExecutor();
//
//
        for (final URL url : urlsToImport) {
//            Runnable runnable = new Runnable() {
//                public void run() {
                    try {
                        if (log.isInfoEnabled()) log.info("Importing: "+url);
                        psiExchange.importIntoIntact(url.openStream());
                    } catch (IOException e) {
                        handleException(e);
                        return;
                    }
//                }
//            };
//
//            executorService.submit(runnable);
        }
//
//        executorService.shutdown();
//
//        try {
//            executorService.awaitTermination(1, TimeUnit.HOURS);
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
        addInfoMessage("File successfully imported", Arrays.asList(urlsToImport).toString());
    }

    public URL[] getUrlsToImport() {
        return urlsToImport;
    }

    public void setUrlsToImport(URL[] urlsToImport) {
        this.urlsToImport = urlsToImport;
    }
}
