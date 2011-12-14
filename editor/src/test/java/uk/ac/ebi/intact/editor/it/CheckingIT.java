package uk.ac.ebi.intact.editor.it;

import org.junit.Test;
import org.openqa.selenium.By;
import org.springframework.beans.factory.annotation.Autowired;
import uk.ac.ebi.intact.core.lifecycle.LifecycleManager;
import uk.ac.ebi.intact.model.CvTopic;
import uk.ac.ebi.intact.model.Experiment;
import uk.ac.ebi.intact.model.Publication;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static uk.ac.ebi.intact.editor.Constants.BASE_URL;

/**
 * TODO comment this
 *
 * @author Marine Dumousseau (marine@ebi.ac.uk)
 * @version $Id$
 * @since <pre>14/12/11</pre>
 */

public class CheckingIT extends EditorIT {

    @Autowired
    private LifecycleManager lifecycleManager;

    @Test
    public void changeCorrectionCommentUsingAnnotations() throws Exception {
        // Given: I am in experiment page and I want to update a correction comment using the annotation tab
        Publication publicationRandom = getMockBuilder().createPublicationRandom();
        lifecycleManager.getNewStatus().assignToCurator(publicationRandom, getUserByLogin("curator"));
        lifecycleManager.getAssignedStatus().startCuration(publicationRandom);

        Experiment randomExp = getMockBuilder().createDeterministicExperiment();
        randomExp.setPublication(publicationRandom);
        publicationRandom.addExperiment(randomExp);

        randomExp.addAnnotation(getMockBuilder().createAnnotation("old comment", null, CvTopic.CORRECTION_COMMENT));
        getCorePersister().saveOrUpdate(publicationRandom);

        driver.get(BASE_URL+"/experiment/"+randomExp.getAc());
        loginAs("curator", driver);

        // When: I update the annotation text and click on save
        takeScreenshot("/tmp/lala1.png", driver);
        driver.findElement(By.xpath("//div[@id='experimentTabs']/ul/li[3]/a/em")).click();
		driver.findElement(By.id("j_id793073516_78eea143:0:0:j_id793073516_78eea0f7")).click();
		driver.findElement(By.id("j_id793073516_78eea143:0:0:j_id793073516_78eea0f7")).sendKeys("New comment");

        takeScreenshot("/tmp/lala2.png", driver);

		driver.findElement(By.id("unsavedSaveButton")).click();


        // Then: the correction comment should be updated
        assertThat(driver.getTitle(), is(equalTo("Dashboard")));
    }
}
