package uk.ac.ebi.intact.editor.application;

import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Controller;

import javax.faces.bean.ApplicationScoped;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * This class allows to control the number of threads in the editor
 *
 * @author Marine Dumousseau (marine@ebi.ac.uk)
 * @version $Id$
 * @since <pre>11/05/12</pre>
 */
@Controller
@ApplicationScoped
public class SearchThreadConfig implements InitializingBean, DisposableBean {

    private ExecutorService executorService;
    private int maxNumberThreads = 250;

    public SearchThreadConfig(){
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
        executorService.shutdownNow();
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        executorService = Executors.newFixedThreadPool(maxNumberThreads);
    }

    @Override
    public void destroy() throws Exception {
        shutDownThreadContext();
    }

}
