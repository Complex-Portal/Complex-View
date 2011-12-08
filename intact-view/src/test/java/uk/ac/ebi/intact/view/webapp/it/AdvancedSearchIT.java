package uk.ac.ebi.intact.view.webapp.it;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.By;

import static org.hamcrest.CoreMatchers.*;
import static uk.ac.ebi.intact.view.webapp.Constants.BASE_URL;

public class AdvancedSearchIT extends IntactViewIT {

//    private WebDriver driver;

	
    @Before
	public void setUp() throws Exception {
//		driver = newDriver();
	}

	@Test
	public void whenClickingOnAddAndSearchIShouldSeeResults() throws Exception {
        // given I want to do a query using the advanced search
        driver.get(BASE_URL);

        // when I use the query pubid 11554746 and click on the Add & Search button
        driver.findElement(By.id("addFieldBtn")).click();
        wait.until(elementIsVisible(By.id("newQuerytxt")));
        driver.findElement(By.id("newQuerytxt")).clear();
        driver.findElement(By.id("newQuerytxt")).sendKeys("pubid:11554746");
        driver.findElement(By.id("addAndSearchBtn")).click();

        // then I expect 4 interactions in total
        Assert.assertThat(numberOfResults(), is(equalTo(4)));

        takeScreenshot("/tmp/lala4.png", driver);
	}

    private int numberOfResults() {
        return Integer.parseInt(driver.findElement(By.id("mainPanels:totalResultsOut")).getText());
    }

    @After
	public void tearDown() throws Exception {
		driver.quit();
	}
}
