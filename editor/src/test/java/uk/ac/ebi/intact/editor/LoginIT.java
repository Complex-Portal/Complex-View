package uk.ac.ebi.intact.editor;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.firefox.FirefoxDriver;

import java.util.concurrent.TimeUnit;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static uk.ac.ebi.intact.editor.Constants.BASE_URL;

public class LoginIT {
    private WebDriver driver;
	
    @Before
	public void setUp() throws Exception {
		driver = new FirefoxDriver();
		driver.manage().timeouts().implicitlyWait(30, TimeUnit.SECONDS);
	}

	@Test
	public void logginAsAdminIGoToDashboard() throws Exception {
        // given
		driver.get(BASE_URL);

        // when
		driver.findElement(By.id("j_username")).sendKeys("admin");
		driver.findElement(By.id("j_password_clear")).sendKeys("admin");
		driver.findElement(By.id("login")).click();

        // then
        assertThat(driver.getTitle(), is(equalTo("Dashboard")));
	}

	@After
	public void tearDown() throws Exception {
		driver.quit();
	}
}
