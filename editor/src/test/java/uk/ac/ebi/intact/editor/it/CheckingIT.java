package uk.ac.ebi.intact.editor.it;

import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
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
        Publication publication = createRandomPublicationAs("curator");
        Experiment exp = publication.getExperiments().iterator().next();

        final String oldAnnotationText = "old comment";
        exp.addAnnotation(getMockBuilder().createAnnotation(oldAnnotationText, null, CvTopic.CORRECTION_COMMENT));
        
        getCorePersister().saveOrUpdate(publication);

        goToExperimentPage(exp.getAc());
        loginAs("curator", driver);

        // When: I update the annotation text and click on save
        clickOnTheExperimentAnnotationsTab();

        final String correctionCommentTextBoxId = "annotationsTable:0:0:annotationTxt";
        final WebElement correctionTextBox = driver.findElement(By.id(correctionCommentTextBoxId));

        correctionTextBox.click();
        correctionTextBox.clear();
        correctionTextBox.sendKeys(oldAnnotationText + ". New comment");

        takeScreenshot("/tmp/lala2.png", driver);

		driver.findElement(By.id("unsavedSaveButton")).click();


        // Then: the correction comment should be updated
        final WebElement newCorrectionTextBox = driver.findElement(By.id(correctionCommentTextBoxId));
        assertThat(newCorrectionTextBox.getText(), is(equalTo(oldAnnotationText+". New comment")));
    }

    private Publication createRandomPublicationAs(String userName) {
        Publication publicationRandom = getMockBuilder().createPublicationRandom();
        lifecycleManager.getNewStatus().assignToCurator(publicationRandom, getUserByLogin(userName));
        lifecycleManager.getAssignedStatus().startCuration(publicationRandom);

        Experiment randomExp = getMockBuilder().createDeterministicExperiment();
        randomExp.setPublication(publicationRandom);
        publicationRandom.addExperiment(randomExp);
        return publicationRandom;
    }

    private void clickOnTheExperimentAnnotationsTab() {
        driver.findElement(By.xpath("//div[@id='experimentTabs']/ul/li[3]/a/em")).click();
    }
}
