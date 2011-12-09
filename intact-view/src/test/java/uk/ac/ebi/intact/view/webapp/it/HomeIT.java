package uk.ac.ebi.intact.view.webapp.it;

import org.junit.Assert;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static uk.ac.ebi.intact.view.webapp.Constants.BASE_URL;

public class HomeIT extends IntactViewIT {

	@Test
	public void mainPageAllInteractionsLoadedByDefault() throws Exception {
        // Given: I want to feel informed
        driver.get(BASE_URL);


        // When: I go to the home page for the first time
        driver.get(BASE_URL);

        // Then: I expect the Interactions tab to be active and display the total number of interactions in the database
        WebElement interactionsTabLink = driver.findElement(By.linkText("Interactions (14)"));
        Assert.assertThat(interactionsTabLink, is(notNullValue()));

	}
}
