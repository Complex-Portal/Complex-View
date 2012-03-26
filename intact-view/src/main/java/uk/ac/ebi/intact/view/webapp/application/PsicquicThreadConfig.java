package uk.ac.ebi.intact.view.webapp.application;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Controller;

import javax.faces.bean.ApplicationScoped;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * This class contains some configuration to use multithreading when querying for PSICQUIC services
 *
 * @author Marine Dumousseau (marine@ebi.ac.uk)
 * @version $Id$
 * @since <pre>26/03/12</pre>
 */
@Controller
@ApplicationScoped
public class PsicquicThreadConfig implements InitializingBean{

    private ExecutorService executorService;
    private int maxNumberThreads = 500;

    public PsicquicThreadConfig(){
    }

    public ExecutorService getExecutorService() {
        return executorService;
    }

    public void setExecutorService(ExecutorService executorService) {
        this.executorService = executorService;
    }

    public int getMaxNumberThreads() {
        return maxNumberThreads;
    }

    public void setMaxNumberThreads(int maxNumberThreads) {
        this.maxNumberThreads = maxNumberThreads;
    }

    public void shutDownThreadContext(){
        executorService.shutdown();

        try {
            executorService.awaitTermination(10, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        executorService = Executors.newFixedThreadPool(maxNumberThreads);
    }
}
