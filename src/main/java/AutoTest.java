
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

    /*
     * Инициализация драйвера
     */
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
    public void testSberbank() {

        // 1. Перейти на страницу "https://www.sberbank.ru/ru/person"
        goToPage(URL);

        // 2. Нажать на - Застраховать себя и имущество
        clickOn("Застраховать себя  и имущество");


        // 3. Выбрать - Страхование путешественников
        choose("Страхование путешественников");

        // 4. Проверить наличие на странице заголовка - Страхование путешественников
        checkTitleOnPage("Страхование путешественников");

        // 5. Нажать на - Оформить онлайн
        clickOn("Оформить онлайн");

        // Нажать на баннер
        clickOnBanner("zashita-traveler.jpg");

        // Переключиться на вкладку
        switchToTab();

        // 6. Нажать на - Минимальная
        clickOn("Минимальная");

        // 7. Нажать на - Оформить
        clickOn("Оформить");

        // 8. Заполнить поля
        // В секции заполнить поля

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

        // 9. Проверить заполнение полей в секции.
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

        // 10. Нажать на - Продолжить
        clickOn("Продолжить");

        // 11. Проверить сообщение об ошибке - Заполнены не все обязательные поля
        checkErrorMessage("Заполнены не все обязательные поля");

        // Закрыть вкладку.
        closeTab();
    }

    /**
     * Закрыть вкладку.
     */
    private void closeTab() {
        DRIVER.close();
    }

    /**
     * Проверить сообщение об ошибке.
     * @param message Сообщение.
     */
    private void checkErrorMessage(String message) {
        textOrImage.apply("//div[contains(@class, 'error-message')]//div[contains(text(), '" + message + "')]");
    }

    /**
     * Проверить заполнение полей в секции.
     * @param section Название секции.
     * @param fieldsAndValues Поля и значения.
     */
    private void checkFillingOfFieldsInSection(String section, Map<String, String> fieldsAndValues) {
        workingWithFields(section, fieldsAndValues, Action.CHECK_VALUE);
    }

    /**
     * В секции заполнить поля.
     * @param section Название секции.
     * @param fieldsAndValues Поля и значения, которыми мы их заполняем.
     */
    private void inSectionFillFields(String section, Map<String, String> fieldsAndValues) {
        workingWithFields(section, fieldsAndValues, Action.INPUT_VALUE);
    }

    /**
     * Действие.
     */
    enum Action {
        /**
         * Проверка.
         */
        CHECK_VALUE {
            @Override
            void toDo(WebElement field, String value) {
                assertTrue("Ошибка заполнения поля",
                        field.getAttribute("value").equalsIgnoreCase(value));
            }},
        /**
         * Ввод.
         */
        INPUT_VALUE {
            @Override
            void toDo(WebElement field, String value) {
                new Actions(DRIVER).click(field).sendKeys(value).build().perform();
            }};

        abstract void toDo(WebElement field, String value);
    }

    /**
     * Работать с полями.
     * @param section Секция.
     * @param fieldsAndValues Поля и Значения.
     * @param action Действие.
     */
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

    /**
     * Переключиться на вкладку.
     */
    private void switchToTab() {
        List<String> tabs = new ArrayList<>(DRIVER.getWindowHandles());
        tabs.remove(DRIVER.getWindowHandle());
        DRIVER.switchTo().window(tabs.get(0));
    }

    /**
     * Нажать на баннер.
     * @param banner Баннер.
     */
    private void clickOnBanner(String banner) {
        textOrImage.apply(".//img[contains(@src, '" + banner + "')]").click();
    }

    /**
     * Проверить заголовок на странице.
     * @param title Заголовок.
     */
    private void checkTitleOnPage(String title) {
        textOrImage.apply("//h1[contains(text(), '" + title + "')]");
    }

    /**
     * Перейти на страницу.
     * @param url URL-адрес.
     */
    private void goToPage(String url) {
        DRIVER.get(url);
    }

    /**
     * Выбрать.
     * @param name Название.
     */
    private void choose(String name) {
        clickOn(name);
    }

    /**
     * Нажать на.
     * @param name Название.
     */
    private void clickOn(String name) {
        buttonOrLink.apply(".//*[contains(@aria-label, '" + name + "') or contains(text(), '" + name + "')]").click();
    }

    /**
     * Кнопка / ссылка
     */
    private Function<String, WebElement> buttonOrLink =
            locator -> WAIT.until(ExpectedConditions.elementToBeClickable(By.xpath(locator)));

    /**
     * Текст / картинка
     */
    private Function<String, WebElement> textOrImage =
            locator -> WAIT.until(ExpectedConditions.visibilityOfElementLocated(By.xpath(locator)));
}
