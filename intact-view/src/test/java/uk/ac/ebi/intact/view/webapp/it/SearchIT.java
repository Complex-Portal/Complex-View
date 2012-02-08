package uk.ac.ebi.intact.view.webapp.it;

import org.junit.Test;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public class SearchIT extends IntactViewIT {

	@Test
	public void searchUsingCvSynonyms() throws Exception {
        // Given: I want to do a search using a cv synonym
        goToTheStartPage();

        // When: I do the search using "2 hybrid" (synonym of "two hybrid");
        search("detmethod:\"2 hybrid\"");

        // Then: I expect to obtain the 2 interactions that display "two hybrid" as detection method
        assertThat(numberOfResultsDisplayed(), is(equalTo(2)));
	}

    @Test
    public void searchUsingCvSynonymsWithNonExistentTerm() throws Exception {
        // Given: I want to do a search using a cv synonym
        goToTheStartPage();

        // When: I do the search using "lala";
        search("detmethod:\"lalala\"");

        // Then: I expect to obtain 0 interactions
        assertThat(numberOfResultsDisplayed(), is(equalTo(0)));
    }

    @Test
    public void afterSearchQueryFieldsShowsQuery() throws Exception {
        goToTheStartPage();
        search("Traf5");
        assertThat(searchQuery(), is(equalTo("Traf5")));
        assertTrue(driver.getCurrentUrl().contains("intact/query/Traf5"));
    }

    @Test
    public void searchUsingAScoreRange() throws Exception {
        goToTheStartPage();
        search("intact-miscore:[0.5 TO 1]");
        assertThat(numberOfResultsDisplayed(), is(equalTo(6)));
    }

}
