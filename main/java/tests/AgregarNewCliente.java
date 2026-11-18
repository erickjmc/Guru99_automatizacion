package tests;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.Assert;
import org.testng.annotations.*;
import java.time.Duration;



public class AgregarNewCliente {

    WebDriver driver;
    WebDriverWait wait;
    JavascriptExecutor js;



    @BeforeMethod
    public void setup() {

        WebDriverManager.chromedriver().setup();
        driver = new ChromeDriver();
        driver.manage().window().maximize();
        wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        js = (JavascriptExecutor) driver;

        driver.get("https://demo.guru99.com/V1/index.php");
        driver.findElement(By.name("uid")).sendKeys("mngr669009");
        driver.findElement(By.name("password")).sendKeys("hYvedYd");
        driver.findElement(By.name("btnLogin")).click();

        try { wait.until(ExpectedConditions.alertIsPresent()).accept(); } catch (Exception e){}
        wait.until(ExpectedConditions.elementToBeClickable(By.linkText("New Customer"))).click();
    }

    @Test

    public void BUG01_NombreNoPermiteEspaciosYTildes() {

        WebElement nombre = driver.findElement(By.name("name"));
        nombre.sendKeys("José Martínez");
        driver.findElement(By.name("city")).click();
        String valor = nombre.getAttribute("value");
        String error = driver.findElement(By.id("message")).getText();


        if(!valor.equals("José Martínez") || error.contains("Numbers")) {
            Assert.fail("BUG-V1-01 CONFIRMADO: Nombre 'José Martínez' -> Valor real:'"+valor+"' Mensaje:'"+error+"'. No permite espacios ni tildes.");
        }

    }

    @Test
    public void BUG02_CiudadEstadoNoPermiteTildes() {

        WebElement city = driver.findElement(By.name("city"));
        city.sendKeys("Córdoba");
        String valor = city.getAttribute("value");

        if(!valor.equals("Córdoba")) {
            Assert.fail("BUG-V1-02 CONFIRMADO: Ciudad 'Córdoba' no permite tilde. Quedó: '"+valor+"'");
        }
    }

    @Test
    public void BUG03_DireccionObligatoriaPermiteBlanco() {

        WebElement addr = driver.findElement(By.name("addr"));
        addr.clear(); // Dejar en blanco a propósito
        driver.findElement(By.name("city")).click();
        String error = "";

        try { error = driver.findElement(By.id("message3")).getText(); } catch (Exception e){ error = "SIN MENSAJE"; }

        if(error.equals("SIN MENSAJE") || error.isEmpty()) {
            Assert.fail("BUG-V1-03 CONFIRMADO: Dirección es campo obligatorio * pero permite dejarlo en blanco sin advertencia.");
        }
    }

    @Test
    public void BUG04_PINPermitePuntoYEspacio() {

        WebElement pin = driver.findElement(By.name("pinno"));
       // Prueba 1: punto al final como en tu foto 12547.
        js.executeScript("arguments[0].value='12547.'", pin);

        // Prueba 2: espacio al inicio
        String val1 = pin.getAttribute("value");

       // Prueba con espacio al inicio usando JS porque con sendKeys a veces lo trimea
        js.executeScript("arguments[0].value=' 123456'", pin);
        String val2 = pin.getAttribute("value");

        if(val1.contains(".") || val2.startsWith(" ")) {
            Assert.fail("BUG-V1-04 CONFIRMADO: PIN permite caracteres inválidos. Con punto: '"+val1+"' Con espacio al inicio: '"+val2+"'. Debería ser solo 6 dígitos [0-9]{6}");
        }

    }

    @Test
    public void BUG05_TelefonoPermiteNegativo() {

        WebElement tel = driver.findElement(By.name("telephoneno"));
        js.executeScript("arguments[0].value='-3135834691'", tel);
        String val = tel.getAttribute("value");

        if(val.startsWith("-")) {
            Assert.fail("BUG-V1-05 CONFIRMADO (REGRESIÓN): Teléfono permite guión negativo como primer carácter: '"+val+"'. Ver evidencia foto V1.");
        }

    }

    @Test
    public void BUG06_EmailPermiteEspacios() {

        WebElement email = driver.findElement(By.name("emailid"));
        js.executeScript("arguments[0].value='erick07martinez07 @ gmail.'", email);
        driver.findElement(By.name("telephoneno")).click();
        String error = "";

        try { error = driver.findElement(By.id("message9")).getText(); } catch (Exception e){ error = "SIN VALIDACIÓN"; }

        if(error.equals("SIN VALIDACIÓN") || error.isEmpty()) {
            Assert.fail("BUG-V1-06 CONFIRMADO: Email 'erick07martinez07 @ gmail.' con espacios antes/después del @ y al final es aceptado sin mensaje. Debería ser inválido.");
        }

    }

    @AfterMethod
    public void cerrar() { if(driver!=null) driver.quit(); }

}