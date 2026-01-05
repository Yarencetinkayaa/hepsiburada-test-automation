package steps;

import com.thoughtworks.gauge.Step;
import core.Delay;
import core.ElementReader;
import driver.DriverFactory;
import org.openqa.selenium.*;

import java.util.List;

public class CartSteps {

    private final WebDriver driver = DriverFactory.getDriver();

    @Step("Sepette ürünün olduğunu kontrol et")
    public void verifyProductExistsInCart() {

        // Sepetim'e tıkla
        By sepetimBy = ElementReader.by("btn_Sepetim_Header");
        WebElement sepetim = firstVisible(sepetimBy, 12000);

        try {
            sepetim.click();
        } catch (Exception e) {
            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", sepetim);
        }

        Delay.l();

        // URL kontrol
        String url = driver.getCurrentUrl();
        if (url == null || !url.contains("sepetim")) {
            throw new RuntimeException("Sepet sayfasına gidilemedi. CurrentUrl: " + url);
        }

        // Basit doğrulama: boş sepet yazısı var mı?
        String bodyText = "";
        try {
            bodyText = driver.findElement(By.tagName("body")).getText();
        } catch (Exception ignored) {}

        if (bodyText == null) bodyText = "";
        String lower = bodyText.toLowerCase();

        if (lower.contains("sepetin boş") || lower.contains("sepetiniz boş")) {
            throw new RuntimeException("Sepet boş görünüyor. Ürün eklenmemiş olabilir.");
        }

        // Sepette ürün olduğuna dair sinyal
        boolean hasSignal = lower.contains("adet") || lower.contains("tl") || lower.contains("₺");
        if (!hasSignal) {
            throw new RuntimeException("Sepette ürün olduğuna dair sinyal bulunamadı (adet/fiyat yok).");
        }

        System.out.println("Sepet kontrolü başarılı: Sepette ürün var görünüyor.");
    }

    private WebElement firstVisible(By by, long timeoutMs) {
        long end = System.currentTimeMillis() + timeoutMs;

        while (System.currentTimeMillis() < end) {
            try {
                List<WebElement> els = driver.findElements(by);
                for (WebElement el : els) {
                    try {
                        if (el.isDisplayed()) return el;
                    } catch (StaleElementReferenceException ignored) {}
                }
            } catch (Exception ignored) {}

            Delay.xs();
        }

        throw new RuntimeException("Element bulunamadı/görünür değil: " + by);
    }
}
