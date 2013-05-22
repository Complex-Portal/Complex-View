package uk.ac.ebi.intact.view.webapp.it;

import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;

public class HomeIT extends IntactViewIT {

	@Test
	public void mainPageAllInteractionsLoadedByDefault() throws Exception {
        // Given: I want to feel informed

        // When: I go to the home page for the first time
        goToTheStartPage();

        // Then: I expect the Interactions tab to be active and display the total number of interactions in the database
        WebElement interactionsTabLink = driver.findElement(By.linkText("Interactions (98)"));
        assertThat(interactionsTabLink, is(notNullValue()));

	}
}
