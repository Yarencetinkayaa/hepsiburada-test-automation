package steps;

import com.thoughtworks.gauge.Step;
import core.Delay;
import core.ElementReader;
import driver.DriverFactory;
import org.openqa.selenium.*;
import org.openqa.selenium.interactions.Actions;

import java.util.*;

public class SearchSteps {

    private final WebDriver driver = DriverFactory.getDriver();

    // LoginSteps içindeki cookie step'ini reuse ediyoruz
    private final LoginSteps common = new LoginSteps();

    // İkinci satırdaki ilk ürün kartını burada tutacağız
    private WebElement selectedCard;

    // ===================== SEARCH =====================

    @Step("Search bar'a tıklanır")
    public void clickSearchBar() {
        common.acceptCookiesIfPresent();

        By searchBy = ElementReader.by("input_SearchBar");
        WebElement el = firstInteractable(searchBy, 12000);
        scrollCenter(el);

        // 1) normal click
        try {
            el.click();
            Delay.xs();
            return;
        } catch (ElementClickInterceptedException ignored) {}

        // 2) actions click
        try {
            new Actions(driver).moveToElement(el)
                    .pause(java.time.Duration.ofMillis(150))
                    .click().perform();
            Delay.xs();
            return;
        } catch (Exception ignored) {}

        // 3) js click
        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", el);
        Delay.xs();
    }

    @Step("Search bar'da <keyword> aranır")
    public void searchInSearchBar(String keyword) {
        common.acceptCookiesIfPresent();

        By searchBy = ElementReader.by("input_SearchBar");
        WebElement el = firstInteractable(searchBy, 12000);
        scrollCenter(el);

        try { el.click(); } catch (Exception ignored) {}

        // temizle
        try {
            el.sendKeys(Keys.chord(Keys.CONTROL, "a"));
            el.sendKeys(Keys.BACK_SPACE);
        } catch (Exception ignored) {}

        el.sendKeys(keyword);
        el.sendKeys(Keys.ENTER);

        Delay.l();
    }

    // ===================== LIST / GRID / CART ACTIONS =====================

    @Step("Ürün listesi satır satır değerlendirilir")
    public void evaluateProductListRowByRow() {
        common.acceptCookiesIfPresent();

        By cardsBy = ElementReader.by("cards_Product");
        List<WebElement> cards = driver.findElements(cardsBy);

        if (cards.isEmpty()) {
            throw new RuntimeException("Ürün listesi bulunamadı (cards_Product).");
        }

        int limit = Math.min(12, cards.size());
        List<Rectangle> rects = new ArrayList<>();

        for (int i = 0; i < limit; i++) {
            try {
                rects.add(cards.get(i).getRect());
            } catch (StaleElementReferenceException ignored) {}
        }

        rects.sort(Comparator.comparingInt(Rectangle::getY));
        int rowCount = countRowsByY(rects, 25);

        System.out.println("Ürün kartı sayısı (toplam): " + cards.size());
        System.out.println("İlk " + limit + " karttan tahmini satır sayısı: " + rowCount);
    }

    @Step("Liste/grid view kontrolü yapılır")
    public void checkListGridView() {
        common.acceptCookiesIfPresent();

        By cardsBy = ElementReader.by("cards_Product");
        List<WebElement> cards = driver.findElements(cardsBy);

        if (cards.size() < 2) {
            throw new RuntimeException("Grid/List kontrolü için yeterli kart yok: " + cards.size());
        }

        int limit = Math.min(10, cards.size());
        Set<Integer> distinctX = new HashSet<>();

        for (int i = 0; i < limit; i++) {
            try {
                distinctX.add(cards.get(i).getRect().getX());
            } catch (StaleElementReferenceException ignored) {}
        }

        if (distinctX.size() >= 2) {
            System.out.println("Görünüm: GRID (farklı X sayısı = " + distinctX.size() + ")");
        } else {
            System.out.println("Görünüm: LIST (farklı X sayısı = " + distinctX.size() + ")");
        }
    }

    @Step("İkinci satırdaki ilk ürün bulunur")
    public void findSecondRowFirstProduct() {
        common.acceptCookiesIfPresent();

        By cardsBy = ElementReader.by("cards_Product");
        List<WebElement> cards = driver.findElements(cardsBy);

        if (cards.size() < 3) {
            throw new RuntimeException("İkinci satır için yeterli kart yok: " + cards.size());
        }

        this.selectedCard = findSecondRowFirstCard(cards);

        if (this.selectedCard == null) {
            throw new RuntimeException("İkinci satırdaki ilk ürün bulunamadı.");
        }

        scrollCenter(this.selectedCard);
        System.out.println("İkinci satır 1. ürün seçildi.");
    }

    @Step("İkinci satırdaki ilk ürünün sepet ikonuna tıklanarak sepete eklenir")
    public void clickCartIconOnSelectedCard() {
        common.acceptCookiesIfPresent();

        if (this.selectedCard == null) {
            throw new RuntimeException("Önce 'İkinci satırdaki ilk ürün bulunur' step'i çalışmalı.");
        }

        scrollCenter(this.selectedCard);

        By iconBy = ElementReader.by("btn_AddToCart_Icon_InCard");

        WebElement icon = null;
        long end = System.currentTimeMillis() + 10000;

        while (System.currentTimeMillis() < end) {
            try {
                List<WebElement> icons = this.selectedCard.findElements(iconBy);
                if (!icons.isEmpty() && isDisplayedSafe(icons.get(0)) && icons.get(0).isEnabled()) {
                    icon = icons.get(0);
                    break;
                }
            } catch (StaleElementReferenceException ignored) {}
            Delay.xs();
        }

        if (icon == null) {
            throw new RuntimeException("Sepet ikonu bulunamadı (kart içinde).");
        }

        // click: normal -> actions -> js
        try {
            icon.click();
            Delay.s();
            return;
        } catch (ElementClickInterceptedException ignored) {}

        try {
            new Actions(driver).moveToElement(icon)
                    .pause(java.time.Duration.ofMillis(120))
                    .click().perform();
            Delay.s();
            return;
        } catch (Exception ignored) {}

        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", icon);
        Delay.s();
    }

    // ===================== HELPERS =====================

    private WebElement firstInteractable(By by, long timeoutMs) {
        long end = System.currentTimeMillis() + timeoutMs;

        while (System.currentTimeMillis() < end) {
            try {
                List<WebElement> els = driver.findElements(by);
                for (WebElement el : els) {
                    try {
                        if (el.isDisplayed() && el.isEnabled()) return el;
                    } catch (StaleElementReferenceException ignored) {}
                }
            } catch (Exception ignored) {}

            Delay.xs();
        }

        throw new RuntimeException("Interactable element bulunamadı: " + by);
    }

    private WebElement findSecondRowFirstCard(List<WebElement> cards) {
        List<CardPos> list = new ArrayList<>();
        for (WebElement c : cards) {
            try {
                Rectangle r = c.getRect();
                if (r.getHeight() > 0 && r.getWidth() > 0) {
                    list.add(new CardPos(c, r.getX(), r.getY()));
                }
            } catch (StaleElementReferenceException ignored) {}
        }

        if (list.size() < 3) return null;

        list.sort(Comparator.comparingInt(a -> a.y));

        int yTol = 25;
        List<List<CardPos>> rows = new ArrayList<>();

        for (CardPos cp : list) {
            boolean placed = false;
            for (List<CardPos> row : rows) {
                int baseY = row.get(0).y;
                if (Math.abs(cp.y - baseY) <= yTol) {
                    row.add(cp);
                    placed = true;
                    break;
                }
            }
            if (!placed) {
                List<CardPos> newRow = new ArrayList<>();
                newRow.add(cp);
                rows.add(newRow);
            }
        }

        if (rows.size() < 2) return null;

        List<CardPos> secondRow = rows.get(1);
        secondRow.sort(Comparator.comparingInt(a -> a.x));

        return secondRow.get(0).el;
    }

    private int countRowsByY(List<Rectangle> rects, int yTol) {
        List<Integer> bases = new ArrayList<>();
        for (Rectangle r : rects) {
            boolean found = false;
            for (Integer base : bases) {
                if (Math.abs(r.getY() - base) <= yTol) {
                    found = true;
                    break;
                }
            }
            if (!found) bases.add(r.getY());
        }
        return bases.size();
    }

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

    private static class CardPos {
        final WebElement el;
        final int x;
        final int y;

        CardPos(WebElement el, int x, int y) {
            this.el = el;
            this.x = x;
            this.y = y;
        }
    }
}
