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
import uk.ac.ebi.intact.editor.it.util.ScreenShotOnFailureRule;
import uk.ac.ebi.intact.model.Experiment;
import uk.ac.ebi.intact.model.Interaction;
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
        if (driver != null) {
            driver.quit();
        }
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
        driver.findElement(By.id("j_username")).clear();
        driver.findElement(By.id("j_username")).sendKeys(user);
        sleep(500);
        driver.findElement(By.id("j_password_clear")).clear();
        driver.findElement(By.id("j_password_clear")).sendKeys(user);
        sleep(500);
        driver.findElement(By.id("login")).click();
    }

    protected void sleep(int i) {
        try {
            Thread.sleep(i);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    protected User getUserByLogin(String login){
        return getDaoFactory().getUserDao().getByLogin(login);
    }
    
    protected void goToPageInContext(String path) {
        driver.get(BASE_URL+path);
    }

    protected void goToPublicationWithId(String id) {
        goToPageInContext("/publication/"+id);
    }

    protected void goToExperimentPageByLabel(String expLabel) {
        Experiment experiment = getDaoFactory().getExperimentDao().getByShortLabel(expLabel);
        goToPageInContext("/experiment/"+experiment.getAc());
    }

    protected void goToExperimentPage(String ac) {
        goToPageInContext("/experiment/" + ac);
    }

    protected void goToInteractionPage(String ac) {
        goToPageInContext("/interaction/"+ac);
    }
    
    protected void goToInteractionPageShortLabel(String shortLabel) {
        final Interaction interaction = getDaoFactory().getInteractionDao().getByShortLabel("prp17-sap61");
        final String interactionAc = interaction.getAc();
        goToPageInContext("/interaction/"+interactionAc);
    }

    protected void goToParticipantPage(String ac) {
        goToPageInContext("/participant/"+ac);
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

    protected boolean infoMessageSummaryExists(String summary) {
        final By byInfoMessage = By.xpath("//span[@class='ui-messages-info-summary']");
        return messageSummaryExists(summary, byInfoMessage);
    }

    protected boolean errorMessageSummaryExists(String summary) {
        final By byInfoMessage = By.xpath("//span[@class='ui-messages-error-summary']");
        return messageSummaryExists(summary, byInfoMessage);
    }

    private boolean messageSummaryExists(String summary, By byInfoMessage) {
        waitUntilElementIsVisible(byInfoMessage);

        for (WebElement element : driver.findElements(byInfoMessage)) {
            if (summary.equals(element.getText())) {
                return true;
            }
        }

        return false;
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

    protected String valueForElement(By id) {
        return driver.findElement(id).getAttribute("value");
    }

    protected void waitUntilElementHasText(final By by, final String text) {
        wait.until(new ExpectedCondition<Boolean>() {
            public Boolean apply(WebDriver webDriver) {
                final WebElement element = driver.findElement(by);
                return element != null && element.getText().equals(text);
            }
        });
    }

    protected void waitUntilElementHasValue(final By by, final String value) {
        wait.until(new ExpectedCondition<Boolean>() {
            public Boolean apply(WebDriver webDriver) {
                final WebElement element = driver.findElement(by);
                return element != null && (element.getAttribute("value")).equals(value);
            }
        });
    }
}
