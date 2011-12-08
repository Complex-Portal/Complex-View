package uk.ac.ebi.intact.view.webapp;

import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.support.ui.Wait;
import org.openqa.selenium.support.ui.WebDriverWait;
import uk.ac.ebi.intact.model.Publication;
import uk.ac.ebi.intact.view.webapp.selenium.VisibilityOfElementLocated;

import java.io.File;
import java.io.IOException;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static uk.ac.ebi.intact.view.webapp.Constants.BASE_URL;

public class AdvancedSearchIT extends IntactViewIT {

    private WebDriver driver;
    private Wait wait;
	
    @Before
	public void setUp() throws Exception {
		driver = new FirefoxDriver();
        wait = new WebDriverWait(driver, 15);
	}

	@Test
	public void whenClickingOnAddAndSearchIShouldSeeResults() throws Exception {
        // given
        driver.get(BASE_URL);

        // when
        driver.findElement(By.id("addFieldBtn")).click();
        screenshot("/tmp/lala1.png");
        wait.until(new VisibilityOfElementLocated(By.id("newQuerytxt")));
        driver.findElement(By.id("newQuerytxt")).clear();
        driver.findElement(By.id("newQuerytxt")).sendKeys("brca2");
        driver.findElement(By.id("j_id656747709_6ee41004")).click();

        // then
        //assertThat(driver.getTitle(), is(equalTo("Dashboard")));

        screenshot("/tmp/lala4.png");
	}

    private void screenshot(String filename) throws IOException {
        File scrFile = ((TakesScreenshot)driver).getScreenshotAs(OutputType.FILE);
        FileUtils.copyFile(scrFile, new File(filename));
        System.out.println(scrFile);
    }


    @After
	public void tearDown() throws Exception {
		driver.quit();
	}
}
