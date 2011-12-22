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
package uk.ac.ebi.intact.view.webapp.it;

import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.openqa.selenium.By;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.test.context.ContextConfiguration;
import uk.ac.ebi.intact.core.unit.IntactBasicTestCase;

import java.io.File;
import java.io.IOException;

import static uk.ac.ebi.intact.view.webapp.Constants.BASE_URL;

/**
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 *
 *
 */
@ContextConfiguration(locations = {
        "classpath*:/META-INF/intact.spring.xml",
        "classpath*:/META-INF/intact-view-test.spring.xml",
        "classpath*:/META-INF/intact-view.jpa-test.spring.xml"}, inheritLocations = false)
public abstract class IntactViewIT extends IntactBasicTestCase {

    protected WebDriver driver;
    protected WebDriverWait wait;

    @Before
	public void setUp() throws Exception {
        this.driver = new FirefoxDriver();
        wait = new WebDriverWait(driver, 30, 500);
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
                return "status-normal".equals(webDriver.findElement(By.id("statusIndicator")).getAttribute("class"));
            }
        });
    }

    protected void takeScreenshot(String filename) throws IOException {
        File scrFile = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
        FileUtils.copyFile(scrFile, new File(filename));
    }

    protected void goToInteractionsTab() {
        driver.findElement(By.linkText("Interactions (14)")).click();
        waitUntilLoadingIsComplete();
    }

    protected void search(String query) {
        driver.findElement(By.id("queryTxt")).clear();
        driver.findElement(By.id("queryTxt")).sendKeys(query);
        driver.findElement(By.id("quickSearchBtn")).click();
        waitUntilLoadingIsComplete();
    }

    protected int numberOfResultsDisplayed() {
        return Integer.parseInt(driver.findElement(By.id("mainPanels:totalResultsOut")).getText());
    }

    protected void goToTheStartPage() {
        driver.get(BASE_URL);
    }

    protected void goToInteractionDetailsPage(String interactionAc) {
        driver.get(BASE_URL+"/interaction/"+interactionAc);
    }

    protected int rowCountForDataTableWithId(String id) {
        return driver.findElements(By.xpath("//div[@id=\""+id+"\"]/table/tbody/tr")).size();
    }
}
