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
        driver.findElement(By.id("addFieldBtn")).click();
        waitUntilElementIsVisible(By.id("newQuerytxt"));
        changeSelectToValue(By.id("newQueryField"), "pubid");
        waitUntilLoadingIsComplete();
        driver.findElement(By.id("newQuerytxt")).sendKeys("11554746");
        driver.findElement(By.id("addAndSearchBtn")).click();

        // Then: I expect 4 interactions in total
        assertThat(numberOfResultsDisplayed(), is(equalTo(4)));
	}

    private void changeSelectToValue(By element, String value) {
        Select select = new Select(driver.findElement(element));
        select.selectByValue(value);
    }
}
