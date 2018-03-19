package com.anna.homework5;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.ie.InternetExplorerDriver;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.events.EventFiringWebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.annotations.*;

import java.io.File;
import java.util.List;
import java.util.Random;

import static java.lang.Integer.parseInt;
import static java.util.UUID.randomUUID;
import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertTrue;

public class Homework5 {
    private WebDriver driver;

    @BeforeMethod
    @Parameters({ "browser" })
    public void setUp(String browser) {
        driver = getWebDriver(browser);
    }

    @AfterMethod
    public void tearDown() {
        driver.quit();
    }

    @Test
    public void testB() {
        driver.get("http://prestashop-automation.qatestlab.com.ua/");

        openProductsPage();

        WebElement product = findRandomProduct();

        String productPriceExpected = product.findElement(
                By.xpath(".//div[@class = 'product-price-and-shipping']/span[@class = 'price']")).getText();
        String productNameExpected = product.findElement(By.xpath(".//a")).getText();

        addToCart(product);
        assertCart(productPriceExpected, productNameExpected);
        fillName();
        fillAddress();
        fillDelivery();
        fillPayment();
        assertOrder(productPriceExpected, productNameExpected);
    }

    private void assertOrder(String productPriceExpected, String productNameExpected) {
        WebElement h3 = waitForElement(By.tagName("h3"));
        assertTrue(h3.getText().contains("ВАШ ЗАКАЗ ПОДТВЕРЖДЁН"));
        int quantity = parseInt(driver.findElement(By.className("col-xs-2")).getText());
        assertEquals(1, quantity);
        String productName = driver.findElement(By.xpath("//div[contains(@class, 'details')]")).getText();
        assertTrue(productName.contains(productNameExpected));
        String productPrice = driver.findElement(By.xpath("//div[contains(@class, 'col-xs-5')]")).getText();
        assertEquals(productPriceExpected, productPrice);
    }

    private void fillPayment() {
        waitForElement(By.xpath("//label[@for = 'payment-option-1']")).click();
        driver.findElement(By.id("conditions_to_approve[terms-and-conditions]")).click();
        driver.findElement(By.xpath("//div[@id = 'payment-confirmation']//button")).click();
    }

    private void fillDelivery() {
        waitForElement(By.xpath("//button[@name = 'confirmDeliveryOption']")).click();
    }

    private void fillAddress() {
        WebElement continueButton = waitForElement(By.xpath("//button[@name = 'confirm-addresses']"));
        driver.findElement(By.xpath("//input[@name = 'address1']")).sendKeys("Sumska 3");
        driver.findElement(By.xpath("//input[@name = 'postcode']")).sendKeys("91057");
        driver.findElement(By.xpath("//input[@name = 'city']")).sendKeys("Kharkiv");
        continueButton.click();
    }

    private void fillName() {
        driver.findElement(By.xpath("//div[contains(@class, 'checkout')]//a")).click();
        WebElement continueButton = waitForElement(By.xpath("//button[@name = 'continue']"));
        driver.findElement(By.xpath("//input[@name = 'firstname']")).sendKeys("Ganna");
        driver.findElement(By.xpath("//input[@name = 'lastname']")).sendKeys("Tertychna");
        driver.findElement(By.xpath("//input[@name = 'email']")).sendKeys(randomUUID().toString() + "@qatestlab.com.ua");
        continueButton.click();
    }

    private void assertCart(String productPriceExpected, String productNameExpected) {
        String productNameActual = driver.findElement(By.xpath("//div[@class = 'product-line-info']/a")).getText();
        String productPriceActual = driver.findElement(By.className("product-price")).getText();
        int quantity = parseInt(driver.findElement(By.className("js-cart-line-product-quantity")).getAttribute("value"));

        assertEquals(1, quantity);
        assertEquals(productNameExpected, productNameActual);
        assertEquals(productPriceExpected, productPriceActual);
    }

    private void addToCart(WebElement product) {
        new Actions(driver).moveToElement(product).perform();
        WebElement quickView = waitForElement(driver, product.findElement(By.xpath("..//a[@class = 'quick-view']")));
        new Actions(driver).moveToElement(quickView).perform();
        //new Actions(driver).moveToElement(quickView).perform();
        //new WebDriverWait(driver, 10).until(ExpectedConditions.elementToBeClickable(quickView));
        quickView.click();
        waitForElement(By.className("add-to-cart")).click();
        waitForElement(By.xpath("//div[@class = 'cart-content']/a")).click();
    }

    private WebElement findRandomProduct() {
        List<WebElement> products = driver.findElements(By.xpath("//div[@class = 'product-description']"));
        return products.get(new Random().nextInt(products.size()));
    }

    private void openProductsPage() {
        driver.findElement(By.className("all-product-link")).click();
        waitForElement(By.tagName("h1"));
    }

    private WebElement waitForElement(By by) {
        return new WebDriverWait(driver, 10).until(ExpectedConditions.visibilityOfElementLocated(by));
    }

    private WebElement waitForElement(WebDriver driver, WebElement webElement) {
        return new WebDriverWait(driver, 10).until(ExpectedConditions.visibilityOf(webElement));
    }

    private WebDriver getWebDriver(String browser) {
        RemoteWebDriver browserDriver = null;
        if (browser.toLowerCase().contains("chrome")) {
            System.setProperty("webdriver.chrome.driver", getDriverPath("chromedriver-2.35.exe"));
            browserDriver = new ChromeDriver();
        } else if (browser.toLowerCase().contains("firefox")) {
            System.setProperty("webdriver.gecko.driver", getDriverPath("geckodriver-0.20.0.exe"));
            browserDriver = new FirefoxDriver();
        } else if (browser.toLowerCase().contains("explorer")) {
            System.setProperty("webdriver.ie.driver", getDriverPath("IEDriverServer-3.8.exe"));
            browserDriver = new InternetExplorerDriver();
        }
        if (browserDriver != null) {
            EventFiringWebDriver eventFiringWebDriver = new EventFiringWebDriver(browserDriver);
            eventFiringWebDriver.register(new MyWebDriverEventListener());
            return eventFiringWebDriver;
        }
        throw new RuntimeException("Browser" + browser + " is not supported");
    }

    private String getDriverPath(String driverExec) {
        return new File(Homework5.class.getResource("/" + getOsDirectory() + "/" + driverExec)
                .getFile()).getPath();
    }

    private String getOsDirectory() {
        String osName = System.getProperty("os.name");
        if (osName.toLowerCase().contains("win")) {
            return "windows";
        }
        throw new RuntimeException(osName + " is not supported");
    }
}