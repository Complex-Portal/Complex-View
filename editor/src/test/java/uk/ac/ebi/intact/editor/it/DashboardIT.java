package uk.ac.ebi.intact.editor.it;

import org.junit.Assert;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.springframework.beans.factory.annotation.Autowired;
import uk.ac.ebi.intact.core.lifecycle.LifecycleManager;
import uk.ac.ebi.intact.model.Publication;
import uk.ac.ebi.intact.model.user.User;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static uk.ac.ebi.intact.editor.Constants.BASE_URL;

public class DashboardIT extends EditorIT {

    @Autowired
    private LifecycleManager lifecycleManager;

	@Test
	public void theDashboardDisplaysThePublicationsIOwn() throws Exception {
        // Create a user "mike" with a random publication that he owns
        User mike = getMockBuilder().createCurator("mike", "Mike", "Smith", "mike@example.com");
        mike.setPassword("a17fed27eaa842282862ff7c1b9c8395a26ac320");
        getCorePersister().saveOrUpdate(mike);

        final Publication mikesPublication = getMockBuilder().createPublication("87823812");
        lifecycleManager.getNewStatus().assignToCurator(mikesPublication, mike);
        lifecycleManager.getAssignedStatus().startCuration(mikesPublication);
        getCorePersister().saveOrUpdate(mikesPublication);

        // Given: I want check my publications in the dashboard
        // When: I connect to the dashboard as admin
        driver.get(BASE_URL);
        loginAs("mike");

        // Then: the I should own 1 publication
        WebElement interactionsTabLink = driver.findElement(By.linkText("87823812"));
        Assert.assertThat(interactionsTabLink, is(notNullValue()));
	}


}
