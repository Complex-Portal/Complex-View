package uk.ac.ebi.intact.editor.it;

import org.junit.Test;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static uk.ac.ebi.intact.editor.Constants.BASE_URL;

public class LoginIT extends EditorIT {

    @Test
    public void successfulLoginBringsMeToTheDashboard() throws Exception {
        // Given: I want to use the editor

        // When: I go to the main page and I authenticate correctly as admin
        driver.get(BASE_URL);

        loginAs("admin");

        // Then: the page displayed should be the Dashboard
        assertThat(titleForCurrentPage(), is(equalTo("Dashboard")));
    }

    @Test
    public void unsuccessfulLoginBringsMeToTheLoginAgain() throws Exception {
        // Given: I want to use the editor

        // When: I go to the main page and I authenticate as wrong user "lala"
        driver.get(BASE_URL);

        loginAs("lala");

        // Then: the page displayed should be the Login
        assertThat(titleForCurrentPage(), is(equalTo("Editor Login")));
    }


}
