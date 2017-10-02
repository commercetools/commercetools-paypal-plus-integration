package com.commercetools.controller;

import com.commercetools.Application;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.embedded.LocalServerPort;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.test.context.junit4.SpringRunner;

import java.time.Instant;
import java.util.Map;

import static com.commercetools.testUtil.TestConstants.MAIN_TEST_TENANT_NAME;
import static java.lang.String.format;
import static org.assertj.core.api.Assertions.assertThat;
import static org.asynchttpclient.util.HttpConstants.Methods.GET;
import static org.asynchttpclient.util.HttpConstants.Methods.POST;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = Application.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
public class CommercetoolsCustomErrorControllerIT {

    @Autowired
    private TestRestTemplate testRestTemplate;

    @LocalServerPort
    private int port;

    @Test
    public void error() throws Exception {
        // GET to wrong URL
        assertResponse(testRestTemplate.getForObject(format("http://localhost:%s/woot", port), Map.class), "/woot", GET);
        assertResponse(testRestTemplate.getForObject(format("http://localhost:%s/woot/", port), Map.class), "/woot/", GET);

        // GET request to the page which supports only POST requests
        assertResponse(testRestTemplate.getForObject(format("http://localhost:%s/%s/commercetools/create/payments/11111111-1111-1111-1111-111111111111/",
                port, MAIN_TEST_TENANT_NAME), Map.class),
                format("/%s/commercetools/create/payments/11111111-1111-1111-1111-111111111111/", MAIN_TEST_TENANT_NAME), GET);

        // POST to wrong URL
        assertResponse(testRestTemplate.postForObject(format("http://localhost:%s/woot", port), null, Map.class),
                "/woot", POST);
        assertResponse(testRestTemplate.postForObject(format("http://localhost:%s/woot/", port), null, Map.class),
                "/woot/", POST);

        // POST request to the page which supports only GET requests
        assertResponse(testRestTemplate.postForObject(format("http://localhost:%s/%s/payments/11111111-1111-1111-1111-111111111111/",
                port, MAIN_TEST_TENANT_NAME), null, Map.class),
                format("/%s/payments/11111111-1111-1111-1111-111111111111/", MAIN_TEST_TENANT_NAME), POST);
    }

    @SuppressWarnings("unchecked")
    private void assertResponse(Map result, String path, String requestMethod) throws Exception {
        assertThat(result.get("requestMethod")).isEqualTo(requestMethod);
        assertThat(result.get("path")).isEqualTo(path);

        // assert that responsed timestamp is not more that 10 seconds ago (expecting some delays in the tests)
        Instant timestamp = Instant.parse((String) result.get("timestamp"));
        Instant now = Instant.now();
        assertThat(now.minusMillis(timestamp.toEpochMilli()).toEpochMilli())
                .isLessThan(10_000);

        Map appInfo = (Map) result.get("applicationInfo");
        assertThat(appInfo.get("version")).isEqualTo("undefined");
        assertThat(appInfo.get("title")).isEqualTo("undefined");

    }

}