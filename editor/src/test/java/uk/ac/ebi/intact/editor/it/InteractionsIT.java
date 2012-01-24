package uk.ac.ebi.intact.editor.it;

import org.junit.Test;
import org.openqa.selenium.By;
import org.springframework.test.annotation.DirtiesContext;
import uk.ac.ebi.intact.model.Experiment;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class InteractionsIT extends EditorIT {

	@Test
	public void importProteinFromUniprotSuccessfully() throws Exception {
        Experiment experiment = getDaoFactory().getExperimentDao().getByShortLabel("bigexp-2012-1");

        // Given: I want to import a participant from uniprot in the interaction page
        final String interactionAc = experiment.getInteractions().iterator().next().getAc();
        goToInteractionPage(interactionAc);
        loginAs("curator");

        // When: I import P12345 using the Import... dialog
        showImportParticipantsDialog();
        searchParticipantsUsing("P12345");
        importSelectedByDefault();

        // Then: the first participant in the table should be the new P12345
        assertThat(identityForParticipantInFirstRow(), is(equalTo("P12345")));
	}

    private void importSelectedByDefault() {
        driver.findElement(By.id("interactionTabs:candidatesDialogContent:importSelected")).click();
        waitUntilElementIsVisible(By.linkText("Participants (3)"));
    }

    private void searchParticipantsUsing(String query) {
        driver.findElement(By.id("interactionTabs:ipDialogPanel:searchParticipantTxt_input")).sendKeys(query);
        driver.findElement(By.id("interactionTabs:ipDialogPanel:participantSearchBtn")).click();
        waitUntilElementIsVisible(By.id("interactionTabs:candidatesDialogContent:importSelected"));
    }

    private void showImportParticipantsDialog() {
        driver.findElement(By.id("interactionTabs:importParticipantBtn")).click();
        waitUntilElementIsVisible(By.id("interactionTabs:ipDialogPanel:searchParticipantTxt_input"));
    }

    private String identityForParticipantInFirstRow() {
        final By firstIdInRowElement = By.xpath("//span[@id=\"interactionTabs:participantsTable:0:participantId\"]");
        return driver.findElement(firstIdInRowElement).getText();
    }


}
