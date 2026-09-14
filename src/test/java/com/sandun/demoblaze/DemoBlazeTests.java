package com.sandun.demoblaze;

import org.openqa.selenium.Alert;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.testng.Assert;
import org.testng.annotations.Test;
import java.util.List;

public class DemoBlazeTests extends BaseTest {

    @Test
    public void tc01_homePageSmokeTest() {
        String title = driver.getTitle();
        Assert.assertFalse(title.isEmpty(), "Page title should not be empty");

        WebElement storeHeading = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.xpath("//a[contains(text(),'PRODUCT STORE')]")));
        Assert.assertTrue(storeHeading.isDisplayed(), "PRODUCT STORE heading should be displayed");
    }

    @Test
    public void tc02_productSelection() {
        openProduct(PRODUCT_SAMSUNG);

        WebElement productName = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.xpath("//h2[@class='name']")));
        Assert.assertEquals(productName.getText(), PRODUCT_SAMSUNG, "Product heading should match selected item");

        WebElement priceElement = driver.findElement(By.xpath("//h3[@class='price-container']"));
        String price = priceElement.getText();
        System.out.println("Samsung galaxy s6 price: " + price);
        Assert.assertTrue(price.contains("$"), "Price should contain a currency symbol");
    }

    @Test
    public void tc03_addToCart() {
        openProduct(PRODUCT_SAMSUNG);
        String alertText = addCurrentProductToCart();
        Assert.assertEquals(alertText, "Product added", "Alert should confirm the product was added");
    }

    @Test
    public void tc04_cartManagement() {
        openProduct(PRODUCT_SAMSUNG);
        addCurrentProductToCart();

        driver.navigate().to(BASE_URL);
        openProduct(PRODUCT_NOKIA);
        addCurrentProductToCart();

        goToCart();

        List<WebElement> cartRows = wait.until(ExpectedConditions.numberOfElementsToBeMoreThan(
                By.cssSelector("#tbodyid tr"), 1));
        System.out.println("Cart rows before removal: " + cartRows.size());
        Assert.assertEquals(cartRows.size(), 2, "Cart should contain two products");

        for (WebElement row : cartRows) {
            List<WebElement> cells = row.findElements(By.tagName("td"));
            String name = cells.get(1).getText();
            String price = cells.get(2).getText();
            System.out.println("Product: " + name + " | Price: $" + price);
        }

        removeProductFromCart(PRODUCT_NOKIA);

        List<WebElement> remainingRows = driver.findElements(By.cssSelector("#tbodyid tr"));
        Assert.assertEquals(remainingRows.size(), 1, "Only one product should remain after removal");

        String remainingProduct = remainingRows.get(0).findElements(By.tagName("td")).get(1).getText();
        Assert.assertEquals(remainingProduct, PRODUCT_SAMSUNG, "Samsung galaxy s6 should remain in the cart");

        String total = driver.findElement(By.id("totalp")).getText();
        System.out.println("Cart total: $" + total);
        Assert.assertFalse(total.isEmpty(), "Cart total should not be empty");
    }

    @Test
    public void tc05_checkoutValidation() {
        openProduct(PRODUCT_SAMSUNG);
        addCurrentProductToCart();
        goToCart();

        wait.until(d -> ((JavascriptExecutor) d).executeScript("return document.readyState").equals("complete"));

        clickWithRetry(By.xpath("//button[normalize-space()='Place Order']"), 3);

        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("orderModal")));

        clickWithRetry(By.xpath("//button[text()='Purchase']"), 3);

        wait.until(ExpectedConditions.alertIsPresent());
        Alert invalidAlert = driver.switchTo().alert();
        String invalidMessage = invalidAlert.getText();
        System.out.println("Invalid checkout alert: " + invalidMessage);
        Assert.assertTrue(invalidMessage.contains("Please fill out"), "Alert should report missing Name and Creditcard");
        invalidAlert.accept();

        driver.findElement(By.id("name")).sendKeys(CHECKOUT_NAME);
        driver.findElement(By.id("country")).sendKeys(CHECKOUT_COUNTRY);
        driver.findElement(By.id("city")).sendKeys(CHECKOUT_CITY);

        driver.findElement(By.id("card")).sendKeys(CHECKOUT_CARD);
        driver.findElement(By.id("month")).sendKeys(CHECKOUT_MONTH);
        driver.findElement(By.id("year")).sendKeys(CHECKOUT_YEAR);

        clickWithRetry(By.xpath("//button[text()='Purchase']"), 3);

        WebElement successMessage = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.xpath("//h2[text()='Thank you for your purchase!']")));
        Assert.assertTrue(successMessage.isDisplayed(), "Purchase success message should be displayed");
    }

    private void openProduct(String productName) {
        clickWithRetry(By.linkText("Phones"), 3);

        wait.until(ExpectedConditions.numberOfElementsToBeMoreThan(
                By.cssSelector("#tbodyid .card"), 0));

        clickWithRetry(By.linkText(productName), 3);
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//h2[@class='name']")));
    }

    private String addCurrentProductToCart() {
        clickWithRetry(By.linkText("Add to cart"), 3);
        wait.until(ExpectedConditions.alertIsPresent());
        Alert alert = driver.switchTo().alert();
        String alertText = alert.getText();
        System.out.println("Add to cart alert: " + alertText);
        alert.accept();
        return alertText;
    }

    private void goToCart() {
        clickWithRetry(By.id("cartur"), 3);
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("tbodyid")));
    }

    private void removeProductFromCart(String productName) {
        List<WebElement> rows = driver.findElements(By.cssSelector("#tbodyid tr"));
        int rowCountBefore = rows.size();

        for (WebElement row : rows) {
            String name = row.findElements(By.tagName("td")).get(1).getText();
            if (name.equals(productName)) {
                row.findElement(By.linkText("Delete")).click();
                break;
            }
        }
        wait.until(ExpectedConditions.numberOfElementsToBe(By.cssSelector("#tbodyid tr"), rowCountBefore - 1));
    }

    private void clickWithRetry(By locator, int attempts) {
        RuntimeException lastException = null;

        for (int i = 0; i < attempts; i++) {
            try {
                WebElement el = wait.until(ExpectedConditions.presenceOfElementLocated(locator));
                ((JavascriptExecutor) driver).executeScript(
                        "arguments[0].scrollIntoView({block: 'center'});", el);

                try {
                    wait.until(ExpectedConditions.elementToBeClickable(locator)).click();
                    return;
                } catch (TimeoutException notClickable) {

                    System.out.println("elementToBeClickable timed out for " + locator
                            + ", falling back to JS click.");
                    ((JavascriptExecutor) driver).executeScript("arguments[0].click();", el);
                    return;
                }
            } catch (StaleElementReferenceException e) {
                lastException = e;
            } catch (TimeoutException e) {

                lastException = e;
            }
        }
        throw lastException;
    }
}
