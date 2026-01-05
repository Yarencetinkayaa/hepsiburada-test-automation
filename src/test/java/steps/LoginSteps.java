package steps;

import com.thoughtworks.gauge.Step;
import core.Delay;
import core.ElementReader;
import core.ValueReader;
import driver.DriverFactory;
import org.openqa.selenium.*;
import org.openqa.selenium.interactions.Actions;

import java.util.List;

public class LoginSteps {

    private final WebDriver driver = DriverFactory.getDriver();

    @Step("Hepsiburada ana sayfası açılır")
    public void openHomePage() {
        driver.get(ValueReader.get("baseUrl"));
        Delay.l();

        acceptCookiesIfPresent();

        if (!driver.getCurrentUrl().contains("hepsiburada.com")) {
            throw new RuntimeException("Ana sayfa açılamadı. CurrentUrl: " + driver.getCurrentUrl());
        }
    }

    @Step("Çerez bildirimi varsa kabul edilir")
    public void acceptCookiesIfPresent() {
        By cookieBy = ElementReader.by("btn_CerezKabul");

        try { driver.switchTo().defaultContent(); } catch (Exception ignored) {}

        long end = System.currentTimeMillis() + 8000;
        while (System.currentTimeMillis() < end) {
            try {
                List<WebElement> els = driver.findElements(cookieBy);
                if (els.isEmpty()) return;

                WebElement btn = els.get(0);
                if (!isDisplayedSafe(btn)) { Delay.xs(); continue; }

                scrollCenter(btn);

                // 1) normal click
                try {
                    btn.click();
                    Delay.s();
                    return;
                } catch (ElementClickInterceptedException ignored) {}

                // 2) actions click
                try {
                    new Actions(driver).moveToElement(btn)
                            .pause(java.time.Duration.ofMillis(150))
                            .click().perform();
                    Delay.s();
                    return;
                } catch (Exception ignored) {}

                // 3) js click
                try {
                    ((JavascriptExecutor) driver).executeScript("arguments[0].click();", btn);
                    Delay.s();
                    return;
                } catch (Exception ignored) {}

            } catch (Exception ignored) {}

            Delay.xs();
        }
    }

    // ---------------- Helpers ----------------

    private void scrollCenter(WebElement el) {
        try {
            ((JavascriptExecutor) driver).executeScript(
                    "arguments[0].scrollIntoView({block:'center', inline:'center'});", el
            );
        } catch (Exception ignored) {}
    }

    private boolean isDisplayedSafe(WebElement el) {
        try { return el.isDisplayed(); } catch (Exception e) { return false; }
    }
}
