
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import static org.junit.Assert.assertTrue;

public class AutoTest {
    private final static WebDriver DRIVER;
    private final static WebDriverWait WAIT;
    private Map<String, String> locators;

    static {
        System.setProperty("webdriver.chrome.driver", "drv/chromedriver.exe");
        DRIVER = new ChromeDriver();
        WAIT = new WebDriverWait(DRIVER, 30);
    }

    private final static String URL = "https://www.sberbank.ru/ru/person";


    @Before
    public void setUp() {
        DRIVER.manage().timeouts().implicitlyWait(30, TimeUnit.SECONDS);
        DRIVER.manage().window().maximize();
        locators = new HashMap<String, String>() {{
            put("Застрахованные", ".//h4[contains(@class, 'ng-hide') and text()='");
            put("Страхователь", ".//h4[text()='");
            put("Данные паспорта РФ", ".//h4[text()='");
        }};
    }

    @Test()
    public void testAuto() {

        goToPage(URL);

        clickOn("Застраховать себя  и имущество");

        choose("Страхование путешественников");

        checkTitleOnPage("Страхование путешественников");

        clickOn("Оформить онлайн");

        clickOnBanner("zashita-traveler.jpg");

        switchToTab();

        clickOn("Минимальная");

        clickOn("Оформить");

        inSectionFillFields("Страхователь", new HashMap<String, String>() {{
                                                        put("Фамилия", "Иванов");
                                                        put("Отчество", "Иванович");
                                                        put("Дата рождения", "24041987");
                                                        put("Имя", "Александр");
                                                    }}
        );

        inSectionFillFields("Застрахованные", new HashMap<String, String>() {{
                                                        put("Фамилия", "Semenov");
                                                        put("Имя", "Semen");
                                                        put("Дата рождения", "10021995");
                                                    }}
        );

        inSectionFillFields("Данные паспорта РФ", new HashMap<String, String>() {{
                                                        put("Серия и номер паспорта", "5522#141517");
                                                        put("Дата выдачи", "17072014");
                                                        put("Кем выдан", "Новосибирским УФМС");
                                                    }}
        );

        checkFillingOfFieldsInSection("Страхователь", new HashMap<String, String>() {{
                                                        put("Фамилия", "Иванов");
                                                        put("Отчество", "Иванович");
                                                        put("Дата рождения", "24.04.1987");
                                                        put("Имя", "Александр");
                                                    }}
        );

        checkFillingOfFieldsInSection("Застрахованные", new HashMap<String, String>() {{
                                                        put("Фамилия", "Semenov");
                                                        put("Имя", "Semen");
                                                        put("Дата рождения", "10.02.1995");
                                                    }}
        );

        checkFillingOfFieldsInSection("Данные паспорта РФ", new HashMap<String, String>() {{
                                                        put("Серия и номер паспорта", "5522#141517");
                                                        put("Дата выдачи", "17.07.2014");
                                                        put("Кем выдан", "Новосибирским УФМС");
                                                    }}
        );

        clickOn("Продолжить");

        checkErrorMessage("Заполнены не все обязательные поля");

        closeTab();
    }

    private void closeTab() {
        DRIVER.close();
    }

    private void checkErrorMessage(String message) {
        textOrImage.apply("//div[contains(@class, 'error-message')]//div[contains(text(), '" + message + "')]");
    }

    private void checkFillingOfFieldsInSection(String section, Map<String, String> fieldsAndValues) {
        workingWithFields(section, fieldsAndValues, Action.CHECK_VALUE);
    }

    private void inSectionFillFields(String section, Map<String, String> fieldsAndValues) {
        workingWithFields(section, fieldsAndValues, Action.INPUT_VALUE);
    }

    enum Action {

        CHECK_VALUE {
            @Override
            void toDo(WebElement field, String value) {
                assertTrue("Ошибка заполнения поля",
                        field.getAttribute("value").equalsIgnoreCase(value));
            }},

        INPUT_VALUE {
            @Override
            void toDo(WebElement field, String value) {
                new Actions(DRIVER).click(field).sendKeys(value).build().perform();
            }};

        abstract void toDo(WebElement field, String value);
    }

    private void workingWithFields(String section, Map<String, String> fieldsAndValues, Action action) {
        WebElement root = textOrImage.apply("//h3[contains(text(), '" + section + "')]/parent::section[1]");

        for (Map.Entry<String, String> fieldAndValue : fieldsAndValues.entrySet()) {
            List<WebElement> inputs = root.findElements(
                    By.xpath(locators.get(section) +
                            fieldAndValue.getKey() +
                            "']/parent::fieldset//*[@type='text']"));
            List<String> values = Arrays.asList(fieldAndValue.getValue().split("#"));

            for (int i = 0; i < inputs.size(); i++) {
                action.toDo(inputs.get(i), values.get(i));
            }
        }
    }

    private void switchToTab() {
        List<String> tabs = new ArrayList<>(DRIVER.getWindowHandles());
        tabs.remove(DRIVER.getWindowHandle());
        DRIVER.switchTo().window(tabs.get(0));
    }

    private void clickOnBanner(String banner) {
        textOrImage.apply(".//img[contains(@src, '" + banner + "')]").click();
    }

    private void checkTitleOnPage(String title) {
        textOrImage.apply("//h1[contains(text(), '" + title + "')]");
    }

    private void goToPage(String url) {
        DRIVER.get(url);
    }

    private void choose(String name) {
        clickOn(name);
    }

    private void clickOn(String name) {
        buttonOrLink.apply(".//*[contains(@aria-label, '" + name + "') or contains(text(), '" + name + "')]").click();
    }

    private Function<String, WebElement> buttonOrLink =
            locator -> WAIT.until(ExpectedConditions.elementToBeClickable(By.xpath(locator)));

    private Function<String, WebElement> textOrImage =
            locator -> WAIT.until(ExpectedConditions.visibilityOfElementLocated(By.xpath(locator)));
}
