package uk.ac.ebi.intact.view.webapp.it;

import org.junit.Assert;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.support.ui.Select;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static uk.ac.ebi.intact.view.webapp.Constants.BASE_URL;

public class AdvancedSearchIT extends IntactViewIT {

	@Test
	public void whenIClickOnAddAndSearchIShouldHaveCorrectResults() throws Exception {
        // Given: I want to do a query using the advanced search
        driver.get(BASE_URL);


        // When: I query 11554746 using Publication ID and click on the Add & Search button
        driver.findElement(By.id("addFieldBtn")).click();
        waitUntilElementIsVisible(By.id("newQuerytxt"));
        changeSelectToValue(By.id("newQueryField"), "pubid");
        waitUntilLoadingIsComplete();
        driver.findElement(By.id("newQuerytxt")).sendKeys("11554746");
        driver.findElement(By.id("addAndSearchBtn")).click();

        takeScreenshot("/tmp/lala4.png", driver);

        // Then: I expect 4 interactions in total
        Assert.assertThat(numberOfResultsDisplayed(), is(equalTo(4)));
	}

    private void changeSelectToValue(By element, String value) {
        Select select = new Select(driver.findElement(element));
        select.selectByValue(value);
    }

    private int numberOfResultsDisplayed() {
        return Integer.parseInt(driver.findElement(By.id("mainPanels:totalResultsOut")).getText());
    }
}
