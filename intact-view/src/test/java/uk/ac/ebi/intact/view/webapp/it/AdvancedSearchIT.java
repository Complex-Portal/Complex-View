package uk.ac.ebi.intact.view.webapp.it;

import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.support.ui.Select;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class AdvancedSearchIT extends IntactViewIT {

	@Test
	public void whenIClickOnAddAndSearchIShouldHaveCorrectResults() throws Exception {
        // Given: I want to do a query using the advanced search from the home page
        goToTheStartPage();

        // When: I query 11554746 using Publication ID and click on the Add & Search button
        showAdvancedFields();
        selectAdvancedFieldByLabel("Publication id (Ex: 10837477)");
        typeAdvancedQuery("11554746");
        clickOnAddAndSearch();

        // Then: I expect 4 interactions in total
        assertThat(numberOfResultsDisplayed(), is(equalTo(3)));
	}

    @Test
    public void whenISelectDetectionMethodUsingTreeIShouldHaveCorrectResults() throws Exception {
        // Given: I want to do a query using the advanced search from the home page
        goToTheStartPage();

        // When: I choose Detection Method and browse the tree selecting "imaging technique"
        showAdvancedFields();
        selectAdvancedFieldByLabel("Interaction detection method (Ex: pull down)");
        clickOnBrowseIcon();
        selectImagingTechniqueInDialog();
        clickOnAddAndSearch();

        // Then: I expect 2 interactions in total
        assertThat(numberOfResultsDisplayed(), is(equalTo(2)));
    }



    private void typeAdvancedQuery(String search) {
        driver.findElement(By.id("newQuerytxt")).sendKeys(search);
    }

    private void selectAdvancedFieldByLabel(String fieldValue) {
        changeSelectToLabel(By.id("newQueryField"), fieldValue);
        waitUntilLoadingIsComplete();
    }

    private void showAdvancedFields() {
        driver.findElement(By.id("addFieldBtn")).click();
        waitUntilElementIsVisible(By.id("newQuerytxt"));
    }

    private void changeSelectToLabel(By element, String label) {
        Select select = new Select(driver.findElement(element));
        select.selectByVisibleText(label);
    }

    private void clickOnAddAndSearch() {
        driver.findElement(By.id("addAndSearchBtn")).click();
    }

    private void selectImagingTechniqueInDialog()  {
        //We need wait to avoid  "Element is not currently visible exception"
        sleep(100);
        driver.findElement(By.xpath("//li[@id='ontologyTree:0']/div/span/span")).click();
        waitUntilElementIsVisible(By.id("ontologyTree:0_1:termTxt"));
        driver.findElement(By.id("ontologyTree:0_1:termTxt")).click();
        waitUntilElementHasValue(By.id("newQuerytxt"), "MI:0428");
    }

    private void clickOnBrowseIcon() {
        waitUntilElementIsVisible(By.id("browseOntologyImg"));
        driver.findElement(By.id("browseOntology")).click();
    }
}
