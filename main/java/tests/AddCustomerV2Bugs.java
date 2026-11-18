package tests;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.time.Duration;

public class AddCustomerV2Bugs {
    WebDriver driver;
    WebDriverWait wait;

    @BeforeMethod
    public void setup() {
        ChromeOptions options = new ChromeOptions();
        options.setAcceptInsecureCerts(true);
        options.addArguments("--ignore-certificate-errors");
        options.addArguments("--remote-allow-origins=*");

        driver = new ChromeDriver(options);
        driver.manage().window().maximize();
        wait = new WebDriverWait(driver, Duration.ofSeconds(15)); // Aumentado a 15 segundos por seguridad

        // 1. Abrir la URL
        driver.get("https://demo.guru99.com/V2/");

        // 2. Esperar explícitamente a que el campo 'uid' esté presente en el DOM antes de interactuar
        WebElement txtUserId = wait.until(ExpectedConditions.presenceOfElementLocated(By.name("uid")));
        txtUserId.sendKeys("mngr669009");

        driver.findElement(By.name("password")).sendKeys("hYvedYd");
        driver.findElement(By.name("btnLogin")).click();

        try {
            wait.until(ExpectedConditions.alertIsPresent()).accept();
        } catch (Exception e) {
            // Sin alerta, continuamos
        }

        wait.until(ExpectedConditions.elementToBeClickable(By.linkText("New Customer"))).click();
    }

    @Test
    public void BUG_V2_01_NombreTildeConfundidoConNumero() {
        WebElement nombre = driver.findElement(By.name("name"));
        nombre.clear();
        nombre.sendKeys("Erick Martinez");

        // Clic en otro campo para forzar la validación (blur)
        driver.findElement(By.name("city")).click();

        WebElement errorMsg = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("message")));
        String msg = errorMsg.getText();
        System.out.println("V2 Nombre msg: " + msg);

        Assert.assertFalse(msg.contains("No se permiten números"),
                "BUG V2-01 CONFIRMADO: Nombre 'Erick Martinez' con espacio muestra 'No se permiten números'. El espacio es válido.");
    }

    @Test
    public void BUG_V2_02_CiudadEstadoPermiteEspacioDoble() {
        WebElement ciudad = driver.findElement(By.name("city"));
        ciudad.clear();
        ciudad.sendKeys("CER  ETE");

        WebElement estado = driver.findElement(By.name("state"));
        estado.clear();
        estado.sendKeys("CO  RDOBA");
        estado.click();

        String valCiudad = ciudad.getAttribute("value");
        System.out.println("V2 Ciudad val: " + valCiudad);

        Assert.assertFalse(valCiudad.contains("  "),
                "BUG V2-02 CONFIRMADO: Ciudad/Estado permiten espacios múltiples 'CER  ETE'. Estado actual: " + valCiudad);
    }

    @Test
    public void BUG_V2_03_PinMensajeIncorrecto() {
        WebElement pin = driver.findElement(By.name("pinno"));
        pin.clear();
        pin.sendKeys("12 625");
        driver.findElement(By.name("telephoneno")).click();

        String msg = "";
        try {
            msg = driver.findElement(By.id("message6")).getText();
        } catch(Exception e) {
            msg = driver.findElement(By.id("message")).getText();
        }
        System.out.println("V2 PIN msg: " + msg);

        boolean isGenericError = msg.contains("No se permiten caracteres") && !msg.contains("espacios");
        Assert.assertFalse(isGenericError,
                "BUG V2-03 CONFIRMADO: PIN '12 625' con espacio muestra mensaje genérico: '" + msg + "'.");
    }

    @Test
    public void BUG_V2_04_EmailPermiteDobleArroba() {
        WebElement email = driver.findElement(By.name("emailid"));
        email.clear();
        email.sendKeys("erick07martinez07@@gmail.com");
        driver.findElement(By.name("telephoneno")).click();

        String msg;
        try {
            msg = driver.findElement(By.id("message9")).getText();
        } catch(Exception e) {
            msg = "SIN VALIDACION";
        }

        String val = email.getAttribute("value");
        System.out.println("V2 Email: " + val + " Msg: " + msg);

        boolean isBugPresent = val.contains("@@") && (msg.equals("SIN VALIDACION") || msg.isEmpty());
        Assert.assertFalse(isBugPresent,
                "BUG V2-04 CONFIRMADO CRÍTICO: Email con doble @@ 'erick07martinez07@@gmail.com' es aceptado sin advertencia.");
    }

    @Test
    public void BUG_V2_05_TelefonoSiguePermitiendoNegativo() {
        WebElement tel = driver.findElement(By.name("telephoneno"));
        tel.clear();
        tel.sendKeys("-1035834691");

        String val = tel.getAttribute("value");
        System.out.println("V2 Telefono val: " + val);

        Assert.assertFalse(val.startsWith("-"),
                "BUG V2-05 CONFIRMADO REGRESIÓN PERSISTENTE: Teléfono sigue permitiendo '-' al inicio: '" + val + "'.");
    }

    @AfterMethod
    public void cerrar() {
        if (driver != null) {
            driver.quit();
        }
    }
}
