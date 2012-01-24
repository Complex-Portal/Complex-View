package uk.ac.ebi.intact.editor.it;

import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import uk.ac.ebi.intact.model.Experiment;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static uk.ac.ebi.intact.editor.Constants.BASE_URL;

public class SearchIT extends EditorIT {

	@Test
	public void importProteinFromUniprotSuccessfully() throws Exception {
        driver.get(BASE_URL);

        loginAs("curator");

        searchFor("curation started");

        // Then: there should be one controlled vocabulary
        assertTrue(isElementPresent(By.linkText("Controlled Vocabularies (1)")));
        
        // and "curation started" should be a clickable result
        driver.findElement(By.linkText("curation started")).click();

        assertThat(titleForCurrentPage(), contains("CvLifecycleEvent: curation started"));
	}

    private void searchFor(String query) {
        final WebElement queryElement = driver.findElement(By.id("queryTxt"));
        queryElement.clear();
        queryElement.sendKeys(query);
        
        driver.findElement(By.id("topSearchBtn")).click();

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

    private void importSelectedByDefault() {
        driver.findElement(By.id("candidatesDialogContent:importSelected")).click();
        waitUntilLoadingIsComplete();
    }


}
