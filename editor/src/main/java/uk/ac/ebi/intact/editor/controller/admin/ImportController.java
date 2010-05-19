package uk.ac.ebi.intact.editor.controller.admin;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.myfaces.orchestra.conversation.annotations.ConversationName;
import org.primefaces.event.FileUploadEvent;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;
import uk.ac.ebi.intact.core.context.IntactContext;
import uk.ac.ebi.intact.dataexchange.psimi.xml.exchange.PsiExchange;
import uk.ac.ebi.intact.dataexchange.psimi.xml.exchange.PsiExchangeFactory;

import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;
import java.io.IOException;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 */
@Controller
@Scope( "conversation.access" )
@ConversationName( "admin" )
public class ImportController {

    private static final Log log = LogFactory.getLog( ImportController.class );

    private URL[] urlsToImport;

//    public void handleFileUpload(FileUploadEvent event) {
//        if (log.isInfoEnabled()) log.info("Uploaded: "+ event.getFile().getFileName());
//
//        FacesMessage msg = new FacesMessage("Succesful", event.getFile().getFileName() + " is uploaded.");
//        FacesContext.getCurrentInstance().addMessage(null, msg);
//    }

    public void startImport(ActionEvent evt) {
        ExecutorService executorService = Executors.newSingleThreadExecutor();

        final PsiExchange psiExchange = PsiExchangeFactory.createPsiExchange(IntactContext.getCurrentInstance().getSpringContext());

        for (final URL url : urlsToImport) {
            Runnable runnable = new Runnable() {
                public void run() {
                    try {
                        if (log.isInfoEnabled()) log.info("Importing: "+url);
                        psiExchange.importIntoIntact(url.openStream());
                    } catch (IOException e) {
                        throw new RuntimeException("Problem importing: "+url, e);
                    }
                }
            };

            executorService.submit(runnable);
        }

        executorService.shutdown();

        try {
            executorService.awaitTermination(1, TimeUnit.HOURS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public URL[] getUrlsToImport() {
        return urlsToImport;
    }

    public void setUrlsToImport(URL[] urlsToImport) {
        this.urlsToImport = urlsToImport;
    }
}
