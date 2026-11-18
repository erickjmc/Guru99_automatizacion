package tests;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.Assert;
import org.testng.annotations.*;

import java.time.Duration;

public class LoginTest {

    WebDriver driver;

    @BeforeMethod
    public void setup(){
        driver = new ChromeDriver();
        driver.manage().window().maximize();
        driver.get("https://demo.guru99.com/V1/index.php");
    }
    @Test
    public void testLoginValido() {
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(15));

        // 1. Ingreso de credenciales
        WebElement userInput = wait.until(ExpectedConditions.visibilityOfElementLocated(By.name("uid")));
        userInput.sendKeys("mngr669009");
        driver.findElement(By.name("password")).sendKeys("hYvedYd");
        driver.findElement(By.name("btnLogin")).click();

        // Validar que entró a la pagina
        String titulo = driver.getTitle();
        Assert.assertFalse(titulo.contains("https://demo.guru99.com/V1/html/Managerhomepage.php"));

    }

    @AfterMethod
    public void cerrar(){
        if (driver != null) {
            driver.quit();
        }
    }
}