package uk.ac.ebi.intact.editor.it;

import org.junit.Test;
import org.openqa.selenium.By;
import uk.ac.ebi.intact.model.Experiment;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

public class ParticipantsIT extends EditorIT {

	@Test
	public void importInteractorFromUniprotSuccessfully() throws Exception {
        Experiment experiment = getDaoFactory().getExperimentDao().getByShortLabel("bigexp-2012-1");

        // Given: I want to import a participant from uniprot in the participant page
        final String participantAc = experiment.getInteractions().iterator().next().getComponents().iterator().next().getAc();
        goToParticipantPage(participantAc);
        loginAs("curator");

        // When: I import P12365 using the Import... dialog
        clickOnInteractorImport();
        searchInteractorUsing("P12365");
        importInteractorSelectedByDefault();

        // Then: the shortlabel of the interactor should be cata2_maize
        assertThat(valueForElement(By.id("interactorTxt")), is(equalTo("P12365")));
	}

    @Test
    public void importParticipantFromUniprotSuccessfully() throws Exception {
        Experiment experiment = getDaoFactory().getExperimentDao().getByShortLabel("bigexp-2012-1");

        // Given: I want to import a participant from chebi in the participant page
        final String interactionAc = experiment.getInteractions().iterator().next().getAc();

        goToInteractionPage(interactionAc);
        loginAs("curator");

        // When: I import P12365 using the Import... dialog
        clickOnParticipantImport();
        searchParticipantsUsing("P12365");
        importParticipantSelectedByDefault();

        // Then: the id of the participant should be CHEBI:73726
        assertEquals("P12365", driver.findElement(By.id("interactionTabs:participantsTable:2:participantId")).getText());
    }

    @Test
    public void importInteractorFromChebiSuccessfully() throws Exception {
        Experiment experiment = getDaoFactory().getExperimentDao().getByShortLabel("bigexp-2012-1");

        // Given: I want to import a participant from chebi in the participant page
        final String participantAc = experiment.getInteractions().iterator().next().getComponents().iterator().next().getAc();
        goToParticipantPage(participantAc);
        loginAs("curator");

        // When: I import P12345 using the Import... dialog
        clickOnInteractorImport();
        searchInteractorUsing("CHEBI:73726");
        importInteractorSelectedByDefault();

        // Then: the shortlabel of the interactor should be CHEBI:73726
        assertThat(valueForElement(By.id("interactorTxt")), is(equalTo("CHEBI:73726")));
    }

    @Test
    public void importParticipantFromChebiSuccessfully() throws Exception {
        Experiment experiment = getDaoFactory().getExperimentDao().getByShortLabel("bigexp-2012-1");

        // Given: I want to import a participant from chebi in the participant page
        final String interactionAc = experiment.getInteractions().iterator().next().getAc();

        goToInteractionPage(interactionAc);
        loginAs("curator");

        // When: I import P12345 using the Import... dialog
        clickOnParticipantImport();
        searchParticipantsUsing("CHEBI:73726");
        importParticipantSelectedByDefault();

        // Then: the id of the participant should be CHEBI:73726
        assertEquals("CHEBI:73726", driver.findElement(By.id("interactionTabs:participantsTable:2:participantId")).getText());
    }


    @Test
    public void importParticipantsFromChebiAndUniprotSuccessfully() throws Exception {
        Experiment experiment = getDaoFactory().getExperimentDao().getByShortLabel("bigexp-2012-1");

        // Given: I want to import a participant from chebi in the participant page
        final String interactionAc = experiment.getInteractions().iterator().next().getAc();

        goToInteractionPage(interactionAc);
        loginAs("curator");

        // When: I import P12345 using the Import... dialog
        clickOnParticipantImport();
        searchParticipantsUsing("CHEBI:73726,P12365");
        importParticipantSelectedByDefault();

        // Then: the id of the participant should be CHEBI:73726
        assertEquals("CHEBI:73726", driver.findElement(By.id("interactionTabs:participantsTable:2:participantId")).getText());
        assertEquals("P12365", driver.findElement(By.id("interactionTabs:participantsTable:3:participantId")).getText());

    }

    private void searchInteractorUsing(String query) {
        final By queryElement = By.id("ipDialogPanel:searchInteractorTxt");
        driver.findElement(queryElement).clear();
        driver.findElement(queryElement).sendKeys(query);
        driver.findElement(By.id("ipDialogPanel:interactorSearchBtn")).click();
        waitUntilElementIsPresent(By.id("candidatesDialogContent:importSelected"));
    }

    private void searchParticipantsUsing(String query) {
        final By queryElement = By.id("interactionTabs:ipDialogPanel:searchParticipantTxt");
        driver.findElement(queryElement).clear();
        driver.findElement(queryElement).sendKeys(query);
        driver.findElement(By.id("interactionTabs:ipDialogPanel:participantSearchBtn")).click();
        waitUntilElementIsPresent(By.id("interactionTabs:candidatesDialogContent:importSelected"));
    }

    private void clickOnInteractorImport() {
        waitUntilElementIsPresent(By.id("importInteractorBtn"));
        driver.findElement(By.id("importInteractorBtn")).click();
        waitUntilElementIsPresent(By.id("ipDialogPanel:searchInteractorTxt"));
    }

    private void clickOnParticipantImport(){
        waitUntilElementIsPresent(By.id("interactionTabs:importParticipantBtn"));
        driver.findElement(By.id("interactionTabs:importParticipantBtn")).click();
        waitUntilElementIsPresent(By.id("interactionTabs:ipDialogPanel:searchParticipantTxt"));
    }

    private void importInteractorSelectedByDefault() {
        driver.findElement(By.id("candidatesDialogContent:importSelected")).click();
        waitUntilLoadingIsComplete();
    }

    private void importParticipantSelectedByDefault() {
        driver.findElement(By.id("interactionTabs:candidatesDialogContent:importSelected")).click();
        waitUntilLoadingIsComplete();
    }


}
