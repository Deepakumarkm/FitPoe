package revenue;

import java.awt.AWTException;
import java.awt.Robot;
import java.awt.event.KeyEvent;
import java.time.Duration;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.Assert;

import io.github.bonigarcia.wdm.WebDriverManager;

public class RevenueCalculatorTest {
	 private WebDriver driver;
	    private WebDriverWait wait;
	    private Robot robot;
	    private Actions actions;
	    private JavascriptExecutor executor;

	    public void setUp() throws AWTException {
	        // Set up WebDriverManager and initialize WebDriver
	        WebDriverManager.chromedriver().setup();
	        driver = new ChromeDriver();
	        wait = new WebDriverWait(driver, Duration.ofSeconds(10));
	        actions = new Actions(driver);
	        executor = (JavascriptExecutor) driver;
	        robot = new Robot();

	        // Maximize browser window and set timeouts
	        driver.manage().window().maximize();
	        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(10));
	        driver.manage().timeouts().pageLoadTimeout(Duration.ofSeconds(120));
	    }

	    public void loadPage(String url) {
	        driver.get(url);
	    }

	    public WebElement waitForElement(By locator) {
	        return wait.until(ExpectedConditions.visibilityOfElementLocated(locator));
	    }

	    public void clickElementUsingJS(WebElement element) {
	        executor.executeScript("arguments[0].click()", element);
	    }

	    public void scrollToElement(WebElement element) throws InterruptedException {
	    	Thread.sleep(2000);//to slow down to record the video
	        executor.executeScript("arguments[0].scrollIntoView(false)", element);
	    }

	    public int getIntFromText(WebElement element) {
	        String text = element.getText().replaceAll("[^0-9]", ""); // Extract digits
	        return Integer.valueOf(text);
	    }

	    public void moveSliderToValue(int currentValue, int targetValue) throws InterruptedException {
	        for (int i = 0; i < targetValue - currentValue; i++) {
	            Thread.sleep(20); // Slight delay for smooth slider movement
	            robot.keyPress(KeyEvent.VK_RIGHT);
	            robot.keyRelease(KeyEvent.VK_RIGHT);
	        }
	    }

	    public void verifyValue(int actual, int expected, String message) {
	    
	        Assert.assertEquals(actual, expected, message);
	    }

	    public void Quit() {
	        if (driver != null) {
	            driver.quit();
	        }
	    }

	    public void testRevenueCalculator() throws InterruptedException, AWTException {
	        setUp();
	        try {
	            loadPage("https://www.fitpeo.com/");

	            // Navigate to Revenue Calculator
	            waitForElement(By.xpath("//a[contains(@href,'revenue-calculator')]")).click();

	            // Adjust slider
	            WebElement slider = waitForElement(By.xpath("//input[@type='range' and contains(@style,'border')]"));
	            actions.click(slider).perform();
	            int currentValue = Integer.valueOf(slider.getAttribute("value"));
	            int targetValue = 820;
	            moveSliderToValue(currentValue, targetValue);
	            verifyValue(Integer.valueOf(slider.getAttribute("value")), targetValue, "Slider did not move to the target value.");

	            // Update and verify patient count
	            WebElement patientInput = waitForElement(By.xpath("//input[contains(@class,'MuiInputBase') and @type='number']"));
	            String patientCount = patientInput.getAttribute("value");
	            verifyValue(Integer.valueOf(patientCount), targetValue, "Patient count mismatch with slider value.");
	            int newPatientCount = 560;
	            robot.keyPress(KeyEvent.VK_TAB);
	    		robot.keyRelease(KeyEvent.VK_TAB);
	            patientInput.sendKeys(String.valueOf(newPatientCount));
	            verifyValue(Integer.valueOf(slider.getAttribute("value")), newPatientCount, "Slider value mismatch with updated patient count.");

	            // Check CPT options and validate reimbursement
	            int cpt99091Price = selectCPTOption("CPT-99091");
	            int cpt99453Price = selectCPTOption("CPT-99453");
	            int cpt99454Price = selectCPTOption("CPT-99454");
	            int cpt99474Price = selectCPTOption("CPT-99474");

	            // Verify total reimbursement
	            WebElement totalReimbursementElement = waitForElement(By.xpath("//p[contains(text(),'Total Recurring Reimbursement')]//child::p"));
	            int expectedTotal = (cpt99091Price + cpt99454Price + cpt99474Price) * newPatientCount;
	            int actualTotal = getIntFromText(totalReimbursementElement);
	            verifyValue(actualTotal, expectedTotal, "Total reimbursement amount mismatch.");

	            System.out.println("Test passed! Total reimbursement: " + actualTotal);
	        } 
	        finally {
	        	Quit();
	        }
	    }

	    private int selectCPTOption(String cptCode) throws InterruptedException {
	        WebElement cptCheckbox = driver.findElement(By.xpath("//p[text()='" + cptCode + "']//following-sibling::label//input"));
	        scrollToElement(cptCheckbox);
	        clickElementUsingJS(cptCheckbox);

	        WebElement cptPriceElement = driver.findElement(By.xpath("//p[text()='" + cptCode + "']//following-sibling::label//span[contains(@class,'MuiTypography')]"));
	        return getIntFromText(cptPriceElement);
	    }

	    public static void main(String[] args) throws InterruptedException, AWTException {
	        RevenueCalculatorTest test = new RevenueCalculatorTest();
	        test.testRevenueCalculator();
	    }
}
