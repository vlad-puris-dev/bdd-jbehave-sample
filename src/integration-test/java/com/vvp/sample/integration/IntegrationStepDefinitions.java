package com.vvp.sample.integration;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import org.apache.commons.io.IOUtils;
import org.jbehave.core.annotations.AfterScenario;
import org.jbehave.core.annotations.BeforeScenario;
import org.jbehave.core.annotations.Given;
import org.jbehave.core.annotations.Named;
import org.jbehave.core.annotations.Then;
import org.jbehave.core.annotations.When;
import org.jbehave.core.model.ExamplesTable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vvp.sample.model.Account;
import com.vvp.sample.model.AccountResponse;

@Component
public class IntegrationStepDefinitions {
    /**
     * Path URL.
     */
    private String urlPath;
    /**
     * Response content.
     */
    private ResponseEntity<String> response;
    /**
     * Server URL.
     */
    private static final String BASE_URL = "http://localhost:6001";
    /**
     * Account response.
     */
    private AccountResponse accountResponse;
    /**
     * Test rest template.
     */
    @Autowired
    private TestRestTemplate restTemplate;

    /**
     * Steps that will be executed before each scenario.
     */
    @BeforeScenario
    public void start() {
        urlPath = null;
    }

    /**
     * Steps that will be executed after each scenario.
     */
    @AfterScenario
    public void end() {
    }

    /**
     * Step to setup path.
     * @param path url path
     */
    @Given("path '$path'")
    public void setPath(@Named("path") final String path) throws Exception {
        urlPath = "/" + path;
    }

    /**
     * Step to execute method.
     * @param method action method
     */
    @When("method $method")
    public void runMethod(@Named("method") final String method) throws Exception {
         response = restTemplate.getForEntity(BASE_URL + urlPath, String.class);
         if (!response.getStatusCode().equals(HttpStatus.NOT_FOUND)) {
             accountResponse = new ObjectMapper().readValue(response.getBody(), AccountResponse.class);
         }
    }

    /**
     * Step to check response status.
     * @param responseStatus expected response status
     */
    @Then("status $responseStatus")
    public void checkResponseStatus(@Named("responseStatus") final String responseStatus) {
            assertEquals(responseStatus, String.valueOf(response.getStatusCodeValue()));
    }

    /**
     * Step to check response path.
     * @param expectedParamName expected param name
     * @param expectedParamValue expected param value
     */
    @Then("match response.$expectedParamName == $expectedParamValue")
    public void checkResponseContent(@Named("expectedParamName") final String expectedParamName,
            @Named("expectedParamValue") final String expectedParamValue) throws Exception {
        String paramValue = "";
        switch (expectedParamName) {
            case "accountId" :
                paramValue = accountResponse.getAccountId();
                break;
            case "status" :
                paramValue = accountResponse.getStatus();
                break;
            case "errorMessage" :
                paramValue = accountResponse.getErrorMessage();
                break;
            default :
                break;
        }
        assertEquals(expectedParamValue.replace("'", ""), String.valueOf(paramValue));
    }

    /**
     * Step to check response path.
     * @param responsePath expected response path
     */
    @Then("match response '$responsePath'")
    public void checkResponseContent(@Named("responsePath") final String responsePath) throws Exception {
        assertEquals(accountResponse.toString(), readFile(responsePath).replaceAll("\\s", ""));
    }

    /**
     * Step to check accounts.
     * @param expectedAccounts expected accounts
     */
    @Then("match response.accounts contains expected $expectedAccounts")
    public void checkAccounts(@Named("expectedAccounts") final ExamplesTable expectedAccounts) throws Exception {
        assertEquals(Arrays.asList(new ObjectMapper().convertValue(expectedAccounts.getRows(),
                Account[].class)).toString().replace("\\u0027", ""),
                accountResponse.getAccounts().toString());
    }

    /**
     * Return file content.
     * @param filePath path to file
     * @return file content
     * @throws IOException exception thrown during file read
     */
    private static String readFile(final String filePath) throws IOException {
        return IOUtils.toString(new ClassPathResource(filePath).getInputStream(), StandardCharsets.UTF_8);
    }
}
