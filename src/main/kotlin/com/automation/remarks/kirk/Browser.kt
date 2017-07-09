package com.automation.remarks.kirk

import com.automation.remarks.kirk.core.*
import org.openqa.selenium.By
import org.openqa.selenium.Dimension
import org.openqa.selenium.WebDriver
import org.openqa.selenium.chrome.ChromeDriver

class Browser(val driver: WebDriver=ChromeDriver()) : SearchContext, Navigable {

    var config: Configuration = loadConfig(Configuration::class)

    var baseUrl: String by baseUrl()

    var timeout: Int by timeout()

    var autoClosable: Boolean? by autoClosable()

    var poolingInterval: Double by poolingInterval()

    var startMaximized: Boolean? by startMaximized()

    var screenSize: List<Int> by screenSize()

    fun with(block: Browser.() -> Unit): Browser {
        return this.apply(block)
    }

    val currentUrl: String by lazy {
        driver.currentUrl
    }

    val js: JsExecutor = JsExecutor(driver)

    override fun open(url: String) {
        driver.autoClose(autoClosable)
        if (screenSize.isNotEmpty()) {
            driver.manage().window().size = Dimension(screenSize[0], screenSize[1])
        } else if (startMaximized!!) {
            driver.manage().window().maximize()
        }

        if (isAbsoluteUrl(url)) {
            driver.navigate().to(url)
        } else {
            driver.navigate().to(baseUrl + url)
        }
    }

    private fun isAbsoluteUrl(url: String): Boolean {
        return (url.startsWith("http://") || url.startsWith("https://"))
    }

    override fun <T : Page> to(pageClass: (Browser) -> T): T {
        val page = pageClass(this)
        page.url?.let { open(it) }
        return page
    }

    override fun <T : Page> at(pageClass: (Browser) -> T, closure: T.() -> Unit) {
        val page = pageClass(this)
        page.closure()
    }

    override fun element(by: By): KElement {
        return KElement(by, driver).apply {
            waitTimeout = timeout
            waitPoolingInterval = poolingInterval
        }
    }

    override fun all(by: By): KElementCollection {
        return KElementCollection(by, driver).apply {
            waitTimeout = timeout
            waitPoolingInterval = poolingInterval
        }
    }

    fun takeScreenshot(saveTo: String = "${System.getProperty("user.dir")}/build/screen_${System.currentTimeMillis()}.png") {
        ScreenshotContainer(driver, saveTo).takeScreenshotAsFile()
    }

    override fun back(): Browser {
        driver.navigate().back()
        return this
    }

    override fun forward(): Browser {
        driver.navigate().forward()
        return this
    }

    override fun refresh(): Browser {
        driver.navigate().refresh()
        return this
    }

    override fun quit() {
        driver.quit()
    }

    private fun addAutoCloseHook() {
        Runtime.getRuntime().addShutdownHook(object : Thread() {
            override fun run() = quit()
        })
    }
}

fun WebDriver.autoClose(enabled: Boolean? = true) {
    if (enabled!!) {
        Runtime.getRuntime().addShutdownHook(object : Thread() {
            override fun run() = quit()
        })
    }
}