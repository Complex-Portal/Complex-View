package uk.ac.ebi.intact.editor.it;

import org.junit.Test;
import org.openqa.selenium.By;
import org.springframework.test.annotation.DirtiesContext;
import uk.ac.ebi.intact.model.Experiment;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class ParticipantsIT extends EditorIT {

	@Test
    @DirtiesContext
	public void importProteinFromUniprotSuccessfully() throws Exception {
        Experiment experiment = getMockBuilder().createExperimentRandom(1);
        getCorePersister().saveOrUpdate(experiment);
        
        // Given: I want to import a participant from uniprot in the participant page
        final String participantAc = experiment.getInteractions().iterator().next().getComponents().iterator().next().getAc();
        goToParticipantPage(participantAc);
        loginAs("curator");

        // When: I import P12345 using the Import... dialog
        clickOnInteractorImport();
        searchParticipantsUsing("P12345");
        importSelectedByDefault();

        // Then: the value of the Interactor should be P12345
        assertThat(driver.findElement(By.id("interactorTxt")).getText(), is(equalTo("P12345")));
	}

    private void importSelectedByDefault() {
        driver.findElement(By.id("candidatesDialogContent:importSelected")).click();
        waitUntilElementHasText(By.id("interactorTxt"), "P12345");
    }

    private void searchParticipantsUsing(String query) {
        final By queryElement = By.id("ipDialogPanel:searchInteractorTxt");
        driver.findElement(queryElement).clear();
        driver.findElement(queryElement).sendKeys(query);
        driver.findElement(By.id("ipDialogPanel:interactorSearchBtn")).click();
        waitUntilElementIsVisible(By.id("candidatesDialogContent:importSelected"));
    }

    private void clickOnInteractorImport() {
        driver.findElement(By.id("importInteractorBtn")).click();
        waitUntilElementIsVisible(By.id("ipDialogPanel:searchInteractorTxt"));
    }

    private String identityForParticipantInFirstRow() {
        final By firstIdInRowElement = By.xpath("//span[@id=\"interactionTabs:participantsTable:0:participantId\"]");
        return driver.findElement(firstIdInRowElement).getText();
    }


}
