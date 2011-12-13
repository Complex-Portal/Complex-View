package uk.ac.ebi.intact.view.webapp.it;

import org.junit.Test;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class SearchIT extends IntactViewIT {

	@Test
	public void searchUsingCvSynonymS() throws Exception {
        // Given: I want to do a search using a cv synonym
        goToTheStartPage();

        // When: I do the search using "2 hybrid" (synonym of "two hybrid");
        search("detmethod:\"2 hybrid\"");

        // Then: I expect to obtain the 2 interactions that display "two hybrid" as detection method
        assertThat(numberOfResultsDisplayed(), is(equalTo(2)));
	}

}
