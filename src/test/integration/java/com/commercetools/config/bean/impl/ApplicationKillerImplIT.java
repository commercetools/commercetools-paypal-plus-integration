package com.commercetools.config.bean.impl;

import com.commercetools.config.ApplicationConfiguration;
import com.commercetools.config.bean.ApplicationKiller;
import org.junit.Rule;
import org.junit.Test;
import org.junit.contrib.java.lang.system.ExpectedSystemExit;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = ApplicationConfiguration.class)
public class ApplicationKillerImplIT {

    @Rule
    public final ExpectedSystemExit exit = ExpectedSystemExit.none();

    @Autowired
    private ApplicationKiller applicationKiller;

    @Test
    public void killApplication_withMessage() throws Exception {
        exit.expectSystemExitWithStatus(22);
        applicationKiller.killApplication(22, "finita la commedia");
    }

    @Test
    public void killApplication_withException() throws Exception {
        exit.expectSystemExitWithStatus(42);
        applicationKiller.killApplication(42, new RuntimeException("hasta la vista baby"));
    }

    @Test
    public void killApplication_withMessageAndException() throws Exception {
        exit.expectSystemExitWithStatus(88);
        applicationKiller.killApplication(88, "finita la commedia", new RuntimeException("hasta la vista baby"));
    }

}