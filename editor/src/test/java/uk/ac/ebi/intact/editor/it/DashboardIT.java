package uk.ac.ebi.intact.editor.it;

import org.junit.Assert;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static uk.ac.ebi.intact.editor.Constants.BASE_URL;

public class DashboardIT extends EditorIT {

	@Test
	public void theDashboardDisplaysThePublicationsIOwn() throws Exception {
        // Given: I want check my publications in the dashboard


        // When: I connect to the dashboard as admin
        driver.get(BASE_URL);
        loginAs("admin");

        // Then: the I should own 1 publication
        WebElement interactionsTabLink = driver.findElement(By.linkText("Publications owned by you (1)"));
        Assert.assertThat(interactionsTabLink, is(notNullValue()));
	}


}
