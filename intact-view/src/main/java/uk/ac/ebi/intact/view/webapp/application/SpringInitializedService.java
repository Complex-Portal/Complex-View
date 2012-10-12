package uk.ac.ebi.intact.view.webapp.application;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;

/**
 * This class is for all classes that need to be initialized with a transaction
 *
 * @author Marine Dumousseau (marine@ebi.ac.uk)
 * @version $Id$
 * @since <pre>10/09/12</pre>
 */
@Service
public class SpringInitializedService implements ApplicationListener<ContextRefreshedEvent> {

    @Autowired
    private TransactionTemplate tt;
    private static final Log log = LogFactory.getLog(SpringInitializedService.class);

    public SpringInitializedService() {
    }

    public SpringInitializedService(PlatformTransactionManager tm){
        tt.setTransactionManager(tm);
    }

    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        tt.execute(new TransactionCallbackWithoutResult() {
            @Override
            protected void doInTransactionWithoutResult(TransactionStatus status) {
                try{
                    initialize();
                }
                catch (Exception ex){
                    log.error("Error during initialization",ex);
                    status.setRollbackOnly();
                }
            }
        });

    }
    public void initialize(){
    }

    public synchronized void onReload(){
        tt.execute(new TransactionCallbackWithoutResult() {
            @Override
            protected void doInTransactionWithoutResult(TransactionStatus status) {
                try{
                    reload();
                }
                catch (Exception ex){
                    log.error("Error during initialization",ex);
                    status.setRollbackOnly();
                }
            }
        });
    }

    public synchronized void reload(){
    }
}
