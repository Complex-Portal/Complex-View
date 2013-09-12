package uk.ac.ebi.intact.service.complex;

import org.apache.solr.client.solrj.impl.HttpSolrServer;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;

/**
 * This class will clean a solr server
 *
 * @author Marine Dumousseau (marine@ebi.ac.uk)
 * @version $Id$
 * @since <pre>12/09/13</pre>
 */

public class SolrCleanerTasklet implements Tasklet {

    private String solrUrl;

    public SolrCleanerTasklet() {
    }

    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
        if (solrUrl == null){
            throw new IllegalStateException("A solr url is expected.");
        }
        // delete all previous records
        HttpSolrServer solrServer = new HttpSolrServer(solrUrl);
        solrServer.deleteByQuery("*:*");

        // optimize here
        solrServer.optimize();

        contribution.getExitStatus().addExitDescription("Cleared: " + solrUrl);

        solrServer.shutdown();

        return RepeatStatus.FINISHED;
    }

    public void setSolrUrl(String solrUrl) {
        if (solrUrl != null){
            this.solrUrl = solrUrl;
        }
    }
}