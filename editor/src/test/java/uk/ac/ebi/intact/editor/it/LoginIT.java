package uk.ac.ebi.intact.editor.it;

import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import uk.ac.ebi.intact.core.context.IntactContext;
import uk.ac.ebi.intact.core.unit.IntactMockBuilder;
import uk.ac.ebi.intact.dataexchange.enricher.standard.ExperimentEnricher;
import uk.ac.ebi.intact.model.Publication;

import java.io.File;
import java.util.concurrent.TimeUnit;

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

        loginAs("admin", driver);

        // Then: the page displayed should be the Dashboard
        assertThat(titleForCurrentPage(), is(equalTo("Dashboard")));
    }

    @Test
    public void unsuccessfulLoginBringsMeToTheLoginAgain() throws Exception {
        // Given: I want to use the editor

        // When: I go to the main page and I authenticate as wrong user "lala"
        driver.get(BASE_URL);

        loginAs("lala", driver);

        // Then: the page displayed should be the Login
        assertThat(titleForCurrentPage(), is(equalTo("Editor Login")));
    }


}
