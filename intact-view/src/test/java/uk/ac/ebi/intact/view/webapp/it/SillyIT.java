package uk.ac.ebi.intact.view.webapp.it;

import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.support.ui.Select;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static uk.ac.ebi.intact.view.webapp.Constants.BASE_URL;

public class SillyIT extends IntactViewIT {

	@Test
	public void whenIClickOnAddAndSearchIShouldHaveCorrectResults() throws Exception {
        // Given: I want to do a query using the advanced search from the home page
        goToTheStartPage();

        // When: I query 11554746 using Publication ID and click on the Add & Search button
        takeScreenshot(System.getProperty("basedir", "lala.png"));

        // Then: I expect 4 interactions in total
        assertThat(numberOfResultsDisplayed(), is(equalTo(4)));
	}

    @Test
    public void whenISelectDetectionMethodUsingTreeIShouldHaveCorrectResults() throws Exception {
        // Given: I want to do a query using the advanced search from the home page
        driver.get(BASE_URL);

        takeScreenshot(System.getProperty("basedir", "lala2.png"));

                // Then: I expect 4 interactions in total
                assertThat(numberOfResultsDisplayed(), is(equalTo(4)));
    }


}
