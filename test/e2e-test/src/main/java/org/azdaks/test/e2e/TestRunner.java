package org.azdaks.test.e2e;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.azdaks.test.e2e.api.ApiClient;
import org.azdaks.test.e2e.contract.response.AccountResponse;
import org.azdaks.test.e2e.contract.response.CreateAccountResponse;
import org.azdaks.test.e2e.contract.response.HomeResponse;
import org.azdaks.test.e2e.contract.response.TransferResponse;
import org.azdaks.test.e2e.endpoint.*;
import org.azdaks.test.e2e.util.Assert;
import org.azdaks.test.e2e.util.Print;

import java.net.http.HttpClient;
import java.time.Duration;

public class TestRunner {

    private final HttpClient _httpClient;
    private final ObjectMapper _objectMapper;
    private final TestSettings _settings;

    public TestRunner(TestSettings settings) {

        _settings = settings;

        _httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(settings.getTimeoutSeconds()))
                .build();

        _objectMapper = new ObjectMapper();
    }

    public void run() throws Exception {
        Print.section("0. Application Running");
        testApplicationIsRunning();

        Print.section("1. Test Create Account");
        testCreateAccount();

        Print.section("2. Test Create Money Transfer");
        testCreateMoneyTransfer();

        Print.section("3. Test Money Transfer Completed");
        testMoneyTransferCompletion();

        Print.section("4. Test Account Balance");
        testAccountBalanceAfterTransfer();

        Print.section("5. Test Fraud Money Transfer");
        testFraudMoneyTransfer();

        Print.section("6. Test Insufficient Funds Money Transfer");
        testInsufficientFundsMoneyTransfer();
    }

    public void testApplicationIsRunning() throws Exception {

        Print.message("👀 Test Application is Running");

        var result = ApiClient.<HomeResponse>builder()
                .settings(_settings)
                .httpClient(_httpClient)
                .objectMapper(_objectMapper)
                .endpoint(new HomeEndpoint())
                .build()
                .send(HomeResponse.class);

        Assert.matchesStatusCode(200, result.getResponse().statusCode(), "✅ Application is Running", "🛑 Application is Not Running");
        Assert.contentContains("Public API Service Started", result.getBody().getMessage(), "✅ Application is Running Correctly", "🛑 Application is Not Running Correctly");
    }

    public void testCreateAccount() throws Exception {

        Print.message("👀 Test Account Creation");

        var result = ApiClient.<CreateAccountResponse>builder()
                .settings(_settings)
                .httpClient(_httpClient)
                .objectMapper(_objectMapper)
                .endpoint(new CreateAccountEndpoint())
                .build()
                .send(CreateAccountResponse.class);

        Assert.matchesStatusCode(200, result.getResponse().statusCode(), "✅ Account Created", "🛑 Account Creation Failed");
        Assert.contentMatches(_settings.getOwner(), result.getBody().getAccount().getOwner(), "✅ Account Owner is Correct", "🛑 Account Owner is Not Correct");
        Assert.contentMatches(_settings.getAmount(), result.getBody().getAccount().getAmount(), "✅ Account Amount is Correct", "🛑 Account Amount is Not Correct");
    }

    public void testCreateMoneyTransfer() throws Exception {

        Print.message("👀 Test Money Transfer Creation");

        var result = ApiClient.<TransferResponse>builder()
                .settings(_settings)
                .httpClient(_httpClient)
                .objectMapper(_objectMapper)
                .endpoint(new CreateMoneyTransferEndpoint())
                .build()
                .send(TransferResponse.class);

        _settings.setTransferId(result.getBody().getTransferId());

        Assert.matchesStatusCode(202, result.getResponse().statusCode(), "✅ Money Transfer Created", "🛑 Money Transfer Creation Failed");
        Assert.contentMatches("ACCEPTED", result.getBody().getStatus(), "✅ Money Transfer Status is Correct", "🛑 Money Transfer Status is Not Correct");
    }

    public void testMoneyTransferCompletion() throws Exception {

        Print.message("👀 Test Money Transfer Status");

        Print.message("⏳ Waiting 5 seconds for Money Transfer to Complete");
        Thread.sleep(Duration.ofSeconds(5).toMillis());

        var result = ApiClient.<TransferResponse>builder()
                .settings(_settings)
                .httpClient(_httpClient)
                .objectMapper(_objectMapper)
                .endpoint(new GetMoneyTransferEndpoint())
                .build()
                .send(TransferResponse.class);


        Assert.matchesStatusCode(200, result.getResponse().statusCode(), "✅ Money Transfer Completed", "🛑 Money Transfer Completion Failed");
        Assert.contentMatches("COMPLETED", result.getBody().getStatus(), "✅ Money Transfer Status is Correct", "🛑 Money Transfer Status is Not Correct");
        Assert.contentMatches(_settings.getTransferAmount(), result.getBody().getAmount(), "✅ Money Transfer Amount is Correct", "🛑 Money Transfer Amount is Not Correct");
        Assert.contentMatches(_settings.getOwner(), result.getBody().getSender(), "✅ Money Transfer Sender is Correct", "🛑 Money Transfer Sender is Not Correct");
        Assert.contentMatches("Receiver", result.getBody().getReceiver(), "✅ Money Transfer Receiver is Correct", "🛑 Money Transfer Receiver is Not Correct");
        Assert.contentMatches(_settings.getTransferId(), result.getBody().getTransferId(), "✅ Money Transfer Id is Correct", "🛑 Money Transfer Id is Not Correct");
    }

    public void testAccountBalanceAfterTransfer() throws Exception {

        Print.message("👀 Test Account Balance");

        Print.message("⏳ Waiting 5 seconds for Money Transfer to Reflect in Account Balance");
        Thread.sleep(Duration.ofSeconds(5).toMillis());

        var result = ApiClient.<AccountResponse>builder()
                .settings(_settings)
                .httpClient(_httpClient)
                .objectMapper(_objectMapper)
                .endpoint(new GetAccountEndpoint())
                .build()
                .send(AccountResponse.class);

        Assert.matchesStatusCode(200, result.getResponse().statusCode(), "✅ Account Balance Checked", "🛑 Account Balance Check Failed");
        Assert.contentMatches(_settings.getAmount() - _settings.getTransferAmount(), result.getBody().getAmount(), "✅ Account Balance is Correct", "🛑 Account Balance is Not Correct");
    }

    public void testFraudMoneyTransfer() throws Exception {
        Print.message("👀 Test Fraud Money Transfer");

        var createResult = ApiClient.<TransferResponse>builder()
                .settings(_settings)
                .httpClient(_httpClient)
                .objectMapper(_objectMapper)
                .endpoint(new CreateFraudMoneyTransferEndpoint())
                .build()
                .send(TransferResponse.class);

        _settings.setTransferId(createResult.getBody().getTransferId());

        Assert.matchesStatusCode(202, createResult.getResponse().statusCode(), "✅ Fraud Money Transfer Created", "🛑 Fraud Money Transfer Creation Failed");
        Assert.contentMatches("ACCEPTED", createResult.getBody().getStatus(), "✅ Fraud Money Transfer Status is Correct", "🛑 Fraud Money Transfer Status is Not Correct");


        Print.message("⏳ Waiting 5 seconds for Money Transfer to be Checked");
        Thread.sleep(Duration.ofSeconds(5).toMillis());

        var result = ApiClient.<TransferResponse>builder()
                .settings(_settings)
                .httpClient(_httpClient)
                .objectMapper(_objectMapper)
                .endpoint(new GetMoneyTransferEndpoint())
                .build()
                .send(TransferResponse.class);

        Assert.matchesStatusCode(200, result.getResponse().statusCode(), "✅ Fraud Check Completed", "🛑 Fraud Check Failed");
        Assert.contentMatches("REJECTED", result.getBody().getStatus(), "✅ Fraud Check Status is Correct", "🛑 Fraud Check Status is Not Correct");
    }

    private void testInsufficientFundsMoneyTransfer() throws Exception {
        _settings.setOwner("InsufficientFunds");
        _settings.setAmount(10);

        testCreateAccount();

        var getAccount = ApiClient.<AccountResponse>builder()
                .settings(_settings)
                .httpClient(_httpClient)
                .objectMapper(_objectMapper)
                .endpoint(new GetAccountEndpoint())
                .build()
                .send(AccountResponse.class);

        var insufficientFundsAmount = getAccount.getBody().getAmount() + 1;

        _settings.setTransferAmount(insufficientFundsAmount);

        testCreateMoneyTransfer();

        Print.message("👀 Test Insufficient Funds Money Transfer");

        Print.message("⏳ Waiting 5 seconds for Money Transfer to be Checked");
        Thread.sleep(Duration.ofSeconds(5).toMillis());

        var result = ApiClient.<TransferResponse>builder()
                .settings(_settings)
                .httpClient(_httpClient)
                .objectMapper(_objectMapper)
                .endpoint(new GetMoneyTransferEndpoint())
                .build()
                .send(TransferResponse.class);


        Assert.matchesStatusCode(200, result.getResponse().statusCode(), "✅ Money Transfer Completed", "🛑 Money Transfer Completion Failed");
        Assert.contentMatches("INSUFFICIENT_FUNDS", result.getBody().getStatus(), "✅ Money Transfer Status is Correct", "🛑 Money Transfer Status is Not Correct");
        Assert.contentMatches(_settings.getTransferAmount(), result.getBody().getAmount(), "✅ Money Transfer Amount is Correct", "🛑 Money Transfer Amount is Not Correct");
        Assert.contentMatches(_settings.getOwner(), result.getBody().getSender(), "✅ Money Transfer Sender is Correct", "🛑 Money Transfer Sender is Not Correct");
        Assert.contentMatches("Receiver", result.getBody().getReceiver(), "✅ Money Transfer Receiver is Correct", "🛑 Money Transfer Receiver is Not Correct");
        Assert.contentMatches(_settings.getTransferId(), result.getBody().getTransferId(), "✅ Money Transfer Id is Correct", "🛑 Money Transfer Id is Not Correct");
    }
}
