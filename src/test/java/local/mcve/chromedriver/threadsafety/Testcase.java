package local.mcve.chromedriver.threadsafety;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

import org.openqa.selenium.By;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.Platform;
import org.openqa.selenium.Point;
import org.openqa.selenium.WebDriver.Window;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.logging.LogEntry;
import org.openqa.selenium.logging.LogType;
import org.openqa.selenium.logging.LoggingPreferences;
import org.openqa.selenium.remote.CapabilityType;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class Testcase {
	private static final String SELENIUM_URL = "http://localhost:4444/wd/hub";
	private static final String TESTPAGE = "http://localhost:8080/chromedriverthreadsafetymcve.html";
	private static final By INPUT = By.cssSelector("input");

	private static final int TEST_COUNT = 7;

	static int finishCount = 0;

	ThreadLocal<RemoteWebDriver> driver = new ThreadLocal<>();

	int driverCounter = 0;

	@BeforeMethod
	private void beforeEach() {
		int id;

		synchronized (this) {
			id = driverCounter++;
		}

		DesiredCapabilities dc = new DesiredCapabilities();

		dc.setPlatform(Platform.fromString("linux"));
		dc.setBrowserName(DesiredCapabilities.chrome().getBrowserName());
		Map<String, Object> prefs = new HashMap<>();
		prefs.put("intl.accept_languages", "de-DE, de");
		prefs.put("profile.default_content_settings.popups", 0);

		ChromeOptions options = new ChromeOptions();
		options.setExperimentalOption("prefs", prefs);
		options.addArguments("--test-type");
		options.addArguments("--lang=de");
		dc.setCapability(ChromeOptions.CAPABILITY, options);

		LoggingPreferences loggingprefs = new LoggingPreferences();
		loggingprefs.enable(LogType.BROWSER, Level.ALL);
		dc.setCapability(CapabilityType.LOGGING_PREFS, loggingprefs);

		try {
			RemoteWebDriver driver = new RemoteWebDriver(new URL(SELENIUM_URL), dc);

			Window window = driver.manage().window();

			window.setPosition(new Point(500 * id, 0));
			window.setSize(new Dimension(500, 500));

			System.out.println(String.format("Created driver %d with Session ID %s", id, driver.getSessionId()));

			this.driver.set(driver);
		} catch (MalformedURLException ex) {
			throw new RuntimeException("Unexpected MalformedURLException", ex);
		}
	}

	@AfterMethod
	public void afterEach() {
		synchronized (this) {
			finishCount++;
		}

		List<LogEntry> entries = this.driver.get().manage().logs().get(LogType.BROWSER).filter(Level.INFO);
		for (LogEntry entry : entries) {
			System.out.println(String.format("%d [%s] %s", entry.getTimestamp(), driver.get().getSessionId(), entry.getMessage()));
		}

		this.driver.get().quit();
	}

	@Test(priority=0)
	public void testANoise() {
		driver.get().get(TESTPAGE);

		System.out.println("Constantly typing while other tests are running and trying to type too (to steal the focus) ...");

		WebElement el = driver.get().findElement(INPUT);

		// Type as long as the other tests are not finished
		while (true) {
			int count;

			synchronized (this) {
				count = finishCount;
			}

			if (count >= TEST_COUNT) {
				break;
			}

			System.out.println(String.format("Finished tests: %d", count));

			el.click();

			el.sendKeys("aaaaaaaaaaaaaaaaaa");

			try {
				Thread.sleep(100);
			} catch (InterruptedException ex) { /* *shrug* */ }
		}

		System.out.println("All tests done.");
	}

	@Test(priority=1)
	public void testA() {
		String input = "D1E1M1O1S1T1R1I1N1G1";
		driver.get().get(TESTPAGE);
		typeAndVerify(driver.get(), INPUT, input);
	}

	@Test(priority=1)
	public void testB() {
		String input = "D2E2M2O2S2T2R2I2N2G2";
		driver.get().get(TESTPAGE);
		typeAndVerify(driver.get(), INPUT, input);
	}

	@Test(priority=1)
	public void testC() {
		String input = "D3E3M3O3S3T3R3I3N3G3";
		driver.get().get(TESTPAGE);
		typeAndVerify(driver.get(), INPUT, input);
	}

	@Test(priority=1)
	public void testD() {
		String input = "D4E4M4O4S4T4R4I4N4G4";
		driver.get().get(TESTPAGE);
		typeAndVerify(driver.get(), INPUT, input);
	}

	@Test(priority=1)
	public void testE() {
		String input = "D5E5M5O5S5T5R5I5N5G5";
		driver.get().get(TESTPAGE);
		typeAndVerify(driver.get(), INPUT, input);
	}

	@Test(priority=1)
	public void testF() {
		String input = "D6E6M6O6S6T6R6I6N6G6";
		driver.get().get(TESTPAGE);
		typeAndVerify(driver.get(), INPUT, input);
	}

	@Test(priority=1)
	public void testG() {
		String input = "D7E7M7O7S7T7R7I7N7G7";
		driver.get().get(TESTPAGE);
		typeAndVerify(driver.get(), INPUT, input);
	}

	private void typeAndVerify(RemoteWebDriver driver, By selector, String input) {
		new WebDriverWait(driver, 3000)
			.until(
				ExpectedConditions.presenceOfElementLocated(selector)
			);

		WebElement el = driver.findElement(selector);

		// Ensure we explicitly force the focus
		// Note: this improves the ruggedness
		el.click();

		// Wait a little bit for async Javascript to complete
		// Note: this improves the ruggedness
		try {
			Thread.sleep(500);
		} catch (InterruptedException ex) { /* *shrug* */ }

		el.clear();

		el.sendKeys(input);

		Assert.assertEquals(el.getAttribute("value"), input);
	}
}
