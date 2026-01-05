package core;

import com.thoughtworks.gauge.AfterScenario;
import com.thoughtworks.gauge.BeforeScenario;
import driver.DriverFactory;

public class Hooks {
    @BeforeScenario
    public void before() {
        DriverFactory.initDriver();
    }

    @AfterScenario
    public void after() {
        DriverFactory.quitDriver();
    }
}
