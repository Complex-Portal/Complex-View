/**
 * Copyright 2011 The European Bioinformatics Institute, and others.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package uk.ac.ebi.intact.editor.it;

import org.apache.commons.io.FileUtils;
import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.openqa.selenium.*;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.test.context.ContextConfiguration;
import uk.ac.ebi.intact.core.unit.IntactBasicTestCase;
import uk.ac.ebi.intact.editor.it.util.ScreenShotOnFailureRule;
import uk.ac.ebi.intact.model.user.User;

import java.io.File;
import java.io.IOException;

import static uk.ac.ebi.intact.editor.Constants.BASE_URL;

/**
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 *
 *
 */
public abstract class EditorIT extends BaseIT {

    protected WebDriver driver;
    protected WebDriverWait wait;

    @Rule
    public ScreenShotOnFailureRule screenshotOnFailureRule = new ScreenShotOnFailureRule();

    protected EditorIT() {
    }

    @Before
    public void setUp() throws Exception {
        this.driver = new FirefoxDriver();
        wait = new WebDriverWait(driver, 30, 500);

        screenshotOnFailureRule.setDriver(driver);
    }

    @After
    public void tearDown() throws Exception {
        driver.quit();
    }

    protected void waitUntilElementIsVisible(final By by) {
        wait.until(new ExpectedCondition<Boolean>() {
            public Boolean apply(WebDriver webDriver) {
                return driver.findElement(by) != null;
            }
        });
    }

    protected void waitUntilLoadingIsComplete() {
        wait.until(new ExpectedCondition<Boolean>() {
            public Boolean apply(WebDriver webDriver) {
                System.out.println("Searching ...");
                return "status-normal".equals(webDriver.findElement(By.id("statusIndicator")).getAttribute("class"));
            }
        });
    }

    protected void takeScreenshot(String filename) throws IOException {
        File scrFile = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
        FileUtils.copyFile(scrFile, new File(filename));
    }

    protected void loginAs(String user) {
        driver.findElement(By.id("j_username")).sendKeys(user);
        driver.findElement(By.id("j_password_clear")).sendKeys(user);
        driver.findElement(By.id("login")).click();
    }

    protected User getUserByLogin(String login){

        return getDaoFactory().getUserDao().getByLogin(login);
    }
    
    protected void goToPageInContext(String path) {
        driver.get(BASE_URL+path);
    }

    protected void goToExperimentPage(String ac) {
        goToPageInContext("/experiment/"+ac);
    }

    protected String titleForCurrentPage() {
        return driver.getTitle();
    }

    protected boolean isElementPresent(By by) {
        try {
            driver.findElement(by);
            return true;
        } catch (NoSuchElementException e) {
            return false;
        }
    }

    protected String infoMessageSummary() {
        final By infoMessage = By.xpath("//span[@class='ui-messages-info-summary']");
        waitUntilElementIsVisible(infoMessage);
        final WebElement info = driver.findElement(infoMessage);
        return info.getText();
    }

    protected WebDriver getDriver() {
        return driver;
    }


    protected Matcher<String> contains(final String substring) {
        return new BaseMatcher<String>() {
            @Override
            public boolean matches(Object o) {
                return o.toString().contains(substring);
            }

            @Override
            public void describeTo(Description description) {
                description.appendText("contains: ").appendValue(substring);
            }
        };
    }

    protected Matcher<String> startsWith(final String substring) {
        return new BaseMatcher<String>() {
            @Override
            public boolean matches(Object o) {
                return o.toString().startsWith(substring);
            }

            @Override
            public void describeTo(Description description) {
                description.appendText("starts with: ").appendValue(substring);
            }
        };
    }
}
