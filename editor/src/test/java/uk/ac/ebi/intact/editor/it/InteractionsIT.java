package uk.ac.ebi.intact.editor.it;

import org.junit.Test;
import org.openqa.selenium.By;
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
    
    @Test
    public void updateFigLegendShouldUpdateAnnotations() throws Exception {
        goToInteractionPageShortLabel("prp17-sap61");
        loginAs("curator");

        clickOnAnnotationsTab();
        typeFigureLegend(" - lala");

        assertThat(firstRowAnnotationText(), is(equalTo("Fig 3 - lala")));
    }

    @Test
    public void updateAnnotationShouldUpdateFigLegend() throws Exception {
        goToInteractionPageShortLabel("prp17-sap61");
        loginAs("curator");

        clickOnAnnotationsTab();
        typeFirstRowAnnotationText(" - lolo");

        assertThat(figureLegendText(), is(equalTo("Fig 3 - lolo")));
    }


    private void clickOnAnnotationsTab() {
        driver.findElement(By.linkText("Annotations (2)")).click();
        waitUntilLoadingIsComplete();
    }

    private void typeFigureLegend(String s) {
        driver.findElement(By.id("figLegendTxt")).sendKeys(s);
    }

    private String firstRowAnnotationText() {
        return valueForElement(By.id("interactionTabs:annotationsTable:0:annotationTxt"));
    }

    private void typeFirstRowAnnotationText(String s) {
        driver.findElement(By.id("interactionTabs:annotationsTable:0:annotationTxt")).sendKeys(s);
    }

    private String figureLegendText() {
        return valueForElement(By.id("figLegendTxt"));
    }

    private void importSelectedByDefault() {
        driver.findElement(By.id("interactionTabs:candidatesDialogContent:importSelected")).click();
        waitUntilElementIsVisible(By.linkText("Participants (3)"));
    }

    private void searchParticipantsUsing(String query) {
        driver.findElement(By.id("interactionTabs:ipDialogPanel:searchParticipantTxt")).sendKeys(query);
        driver.findElement(By.id("interactionTabs:ipDialogPanel:participantSearchBtn")).click();
        waitUntilElementIsVisible(By.id("interactionTabs:candidatesDialogContent:importSelected"));
    }

    private void showImportParticipantsDialog() {
        waitUntilElementIsVisible(By.id("interactionTabs:importParticipantBtn"));
        driver.findElement(By.id("interactionTabs:importParticipantBtn")).click();
        waitUntilElementIsVisible(By.id("interactionTabs:ipDialogPanel:searchParticipantTxt"));
    }

    private String identityForParticipantInFirstRow() {
        final By firstIdInRowElement = By.xpath("//span[@id=\"interactionTabs:participantsTable:0:participantId\"]");
        return driver.findElement(firstIdInRowElement).getText();
    }


}
