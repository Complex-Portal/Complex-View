package uk.ac.ebi.intact.editor.it;

import org.junit.Test;
import org.openqa.selenium.By;
import uk.ac.ebi.intact.model.Experiment;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class ParticipantsIT extends EditorIT {

	@Test
	public void importProteinFromUniprotSuccessfully() throws Exception {
        Experiment experiment = getDaoFactory().getExperimentDao().getByShortLabel("bigexp-2012-1");
        
        // Given: I want to import a participant from uniprot in the participant page
        final String participantAc = experiment.getInteractions().iterator().next().getComponents().iterator().next().getAc();
        goToParticipantPage(participantAc);
        loginAs("curator");

        // When: I import P12345 using the Import... dialog
        clickOnInteractorImport();
        searchParticipantsUsing("P12365");
        importSelectedByDefault();

        // Then: the shortlabel of the interactor should be cata2_maize
        assertThat(valueForElement(By.id("interactorTxt")), is(equalTo("P12365")));
	}

    private void searchParticipantsUsing(String query) {
        final By queryElement = By.id("ipDialogPanel:searchInteractorTxt");
        driver.findElement(queryElement).clear();
        driver.findElement(queryElement).sendKeys(query);
        driver.findElement(By.id("ipDialogPanel:interactorSearchBtn")).click();
        waitUntilElementIsVisible(By.id("candidatesDialogContent:importSelected"));
    }

    private void clickOnInteractorImport() {
        waitUntilElementIsVisible(By.id("importInteractorBtn"));
        driver.findElement(By.id("importInteractorBtn")).click();
        waitUntilElementIsVisible(By.id("ipDialogPanel:searchInteractorTxt"));
    }

    private void importSelectedByDefault() {
        driver.findElement(By.id("candidatesDialogContent:importSelected")).click();
        waitUntilLoadingIsComplete();
    }


}
