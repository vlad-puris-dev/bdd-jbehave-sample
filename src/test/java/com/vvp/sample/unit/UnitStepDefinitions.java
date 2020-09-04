package com.vvp.sample.unit;

import static com.github.tomakehurst.wiremock.client.WireMock.configureFor;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;
import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.jbehave.core.annotations.AfterScenario;
import org.jbehave.core.annotations.BeforeScenario;
import org.jbehave.core.annotations.Given;
import org.jbehave.core.annotations.Named;
import org.jbehave.core.annotations.When;
import org.jbehave.core.model.ExamplesTable;
import org.jbehave.core.annotations.Then;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpStatus;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.ResponseDefinitionBuilder;
import com.vvp.sample.model.Account;
import com.vvp.sample.model.AccountResponse;

public class UnitStepDefinitions {
    /**
     * Base URL.
     */
    private String baseUrl;
    /**
     * Path URL.
     */
    private String urlPath;
    /**
     * Server URL.
     */
    private static final String SERVER_URL = "localhost";
    /**
     * Mock server.
     */
    private WireMockServer wireMockServer;
    /**
     * Mock http client.
     */
    private CloseableHttpClient httpClient;
    /**
     * Http response.
     */
    private HttpResponse response;

    /**
     * Steps that will be executed before each scenario.
     */
    @BeforeScenario
    public void start() {
        urlPath = null;
        response = null;
        httpClient = HttpClients.createDefault();
        wireMockServer = new WireMockServer(options()
                .withRootDirectory("src/test/resources")
                .dynamicPort());
        wireMockServer.start();
        configureFor(SERVER_URL, wireMockServer.port());
        baseUrl = "http://" + SERVER_URL + ":" + wireMockServer.port();
    }

    /**
     * Steps that will be executed after each scenario.
     */
    @AfterScenario
    public void end() {
        this.wireMockServer.stop();
    }

    /**
     * Step to setup path.
     * @param path url path
     */
    @Given("path '$path'")
    public void setPath(@Named("path") final String path) throws Exception {
        urlPath = "/" + path;
        ResponseDefinitionBuilder aResponse = new ResponseDefinitionBuilder();
        aResponse.withHeader("Content-Type", "application/json");
        switch (path) {
            case "error" :
                aResponse.withStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
                break;
            case "v1/accounts/0123456789" :
                aResponse.withBody(readFile("/data/unitAccountResponse.json"))
                    .withStatus(HttpStatus.OK.value());
                break;
            case "v1/accounts" :
                aResponse.withBody(readFile("/data/unitAccountsResponse.json"))
                    .withStatus(HttpStatus.OK.value());
                break;
            default :
                break;
        }
        stubFor(get(urlEqualTo(urlPath)).willReturn(aResponse));
    }

    /**
     * Step to execute method.
     * @param method action method
     */
    @When("method $method")
    public void runMethod(@Named("method") final String method) throws Exception {
        response = httpClient.execute(new HttpGet(baseUrl + urlPath));
    }

    /**
     * Step to check response status.
     * @param responseStatus expected response status
     */
    @Then("status $responseStatus")
    public void checkResponseStatus(@Named("responseStatus") final String responseStatus) {
        assertEquals(responseStatus, String.valueOf(response.getStatusLine().getStatusCode()));
    }

    /**
     * Step to check response path.
     * @param responsePath expected response path
     */
    @Then("match response '$responsePath'")
    public void checkResponseContent(@Named("responsePath") final String responsePath) throws Exception {
        assertEquals(getResponseContent(response), readFile(responsePath));
    }

    /**
     * Step to check accounts.
     * @param expectedAccounts expected accounts
     */
    @Then("match response.accounts contains expected $expectedAccounts")
    public void checkAccounts(@Named("expectedAccounts") final ExamplesTable expectedAccounts) throws Exception {
        assertEquals(Arrays.asList(new ObjectMapper().convertValue(expectedAccounts.getRows(),
                                Account[].class)).toString().replace("\\u0027", ""),
                        new ObjectMapper().readValue(getResponseContent(response),
                                AccountResponse.class).getAccounts().toString());
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

    /**
     * Return response body content.
     * @param response response
     * @return response body content
     * @throws Exception exception thrown during response body content read
     */
    private static String getResponseContent(final HttpResponse response) throws Exception {
        return IOUtils.toString(response.getEntity().getContent(), StandardCharsets.UTF_8);
    }
}
