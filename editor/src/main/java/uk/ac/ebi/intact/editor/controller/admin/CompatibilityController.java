/**
 * Copyright 2010 The European Bioinformatics Institute, and others.
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
package uk.ac.ebi.intact.editor.controller.admin;

import org.apache.myfaces.orchestra.conversation.annotations.ConversationName;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameter;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersInvalidException;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.repository.JobExecutionAlreadyRunningException;
import org.springframework.batch.core.repository.JobInstanceAlreadyCompleteException;
import org.springframework.batch.core.repository.JobRestartException;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;
import uk.ac.ebi.intact.editor.controller.BaseController;

import javax.annotation.Resource;
import javax.faces.event.ActionEvent;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 */
@Controller
@Scope( "conversation.access" )
@ConversationName( "admin" )
public class CompatibilityController extends BaseController {

    @Resource( name = "editorJobLauncher" )
    private JobLauncher jobLauncher;

    public CompatibilityController() {
    }

    public void launchPublicationSync( ActionEvent evt ) {
        Job job = getJob( "publicationSyncJob" );

        String jobId = String.valueOf( System.currentTimeMillis() );

        launchJob(job, jobId);
    }

    public void launchExperimentSync( ActionEvent evt ) {
        Job job = getJob( "experimentSyncJob" );

        String jobId = String.valueOf( System.currentTimeMillis() );

        launchJob(job, jobId);
    }

    private void launchJob(Job job, String jobId) {
        Map<String, JobParameter> jobParameterMap = new HashMap<String, JobParameter>();
        jobParameterMap.put( "jobId", new JobParameter( jobId ) );

        try {
            jobLauncher.run( job, new JobParameters( jobParameterMap ) );

            addInfoMessage( "Job started", "Job ID: " + jobId );
        } catch ( JobExecutionAlreadyRunningException e ) {
            addErrorMessage( "Job is already running", "Job ID: " + jobId );
            e.printStackTrace();
        } catch ( JobRestartException e ) {
            addErrorMessage( "Cannot restart job", "Job ID: " + jobId );
            e.printStackTrace();
        } catch ( JobInstanceAlreadyCompleteException e ) {
            addErrorMessage( "Job already complete", "Job ID: " + jobId );
            e.printStackTrace();
        } catch ( JobParametersInvalidException e ) {
            addErrorMessage( "Invalid job parameters", "Job ID: " + jobId );
            e.printStackTrace();
        }
    }

    private Job getJob( String jobName ) {
        return ( Job ) getSpringContext().getBean( jobName );
    }
}
