package uk.ac.ebi.intact.editor;

import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import uk.ac.ebi.intact.core.context.IntactContext;
import uk.ac.ebi.intact.core.unit.IntactMockBuilder;
import uk.ac.ebi.intact.model.Publication;

import java.io.File;
import java.util.concurrent.TimeUnit;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static uk.ac.ebi.intact.editor.Constants.BASE_URL;

public class LoginIT extends EditorIT {

    private WebDriver driver;
	
    @Before
	public void setUp() throws Exception {
		driver = new FirefoxDriver();
	}

	@Test
	public void logginAsAdminIGoToDashboard() throws Exception {
        // given
        willLoginAs("admin");

        final Publication publicationRandom = getMockBuilder().createPublicationRandom();
        getCorePersister().saveOrUpdate(publicationRandom);

        // when
        driver.get(BASE_URL);
        loginAs("admin", driver);

        // then
        assertThat(driver.getTitle(), is(equalTo("Dashboard")));

//        File scrFile = ((TakesScreenshot)driver).getScreenshotAs(OutputType.FILE);
//        FileUtils.copyFile(scrFile, new File("/tmp/lala.png"));
//        System.out.println(scrFile);
	}



    @After
	public void tearDown() throws Exception {
		driver.quit();
	}
}
