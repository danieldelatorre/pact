package io.reflectoring;

import au.com.dius.pact.consumer.Pact;
import au.com.dius.pact.consumer.PactProviderRuleMk2;
import au.com.dius.pact.consumer.PactVerification;
import au.com.dius.pact.consumer.dsl.PactDslJsonBody;
import au.com.dius.pact.consumer.dsl.PactDslWithProvider;
import au.com.dius.pact.model.RequestResponsePact;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringRunner.class)
@SpringBootTest(properties = {
        // overriding provider address
        "userservice.ribbon.listOfServers: localhost:8888"
})
public class UserServiceConsumerTest {

  //When we want to create a test using Pact, first we need to define a @Rule that will be used in our test:
  //We’re passing the provider name, host, and port on which the server mock (which is created from the contract) will be started.
  @Rule
  public PactProviderRuleMk2 stubProvider = new PactProviderRuleMk2("userservice", "localhost", 8888, this);

//  Let’s say that service has defined the contract for two HTTP methods that it can handle.
//  The first method is a POST request that sends a body with 1 field. When the request succeeds, it returns a 201 HTTP response code and the Content-Type header for JSON
//  Let’s define such a contract using Pact.
//  We need to use the @Pact annotation and pass the consumer name for which the contract is defined. Inside of the annotated method, we can define our POST contract
  @Pact(state = "provider accepts a new person", provider = "userservice", consumer = "userclient")
  public RequestResponsePact createPersonPact(PactDslWithProvider builder) {

    Map<String, String> headers = new HashMap<>();
    headers.put("Content-Type", "application/json");

    return builder
            .given("provider accepts a new person")
            .uponReceiving("a request to POST a person")
            .path("/user-service/users")
            .method("POST")
            .headers(headers)
            .body("{\"firstName\": \"Daniel\",\"lastName\": \"de la Torre\"}")
            .willRespondWith()
            .status(201)
            .headers(headers)
            .body(new PactDslJsonBody()
                    .numberValue("id", 42))
            .toPact();
  }

  @Pact(state = "person 42 exists", provider = "userservice", consumer = "userclient")
  public RequestResponsePact updatePersonPact(PactDslWithProvider builder) {

    Map<String, String> headers = new HashMap<>();
    headers.put("Content-Type", "application/json");

    return builder
            .given("person 42 exists")
            .uponReceiving("a request to PUT a person")
            .path("/user-service/users/42")
            .method("PUT")
            .headers(headers)
            .body("{\"firstName\": \"Zaphod\",\"lastName\": \"Beeblebrox\"}")
            .willRespondWith()
            .status(200)
            .headers(headers)
            .body("{\"firstName\": \"Zaphod\",\"lastName\": \"Beeblebrox\"}")
            .toPact();
  }


  @Test
  //which tells Pact that in this method we want to verify that our client works against the pact defined in the method createPersonPact
  //Pact starts a stub HTTP server on port 8888 which responds to the request defined in the pact with the response defined in the pact
  @PactVerification(fragment = "createPersonPact")
  public void verifyCreatePersonPact() throws Exception{

    HttpHeaders httpHeaders = new HttpHeaders();
    httpHeaders.setContentType(MediaType.APPLICATION_JSON);
    String jsonBody = "{\"firstName\": \"Daniel\",\"lastName\": \"de la Torre\"}";

    ResponseEntity<IdObject> postResponse = new RestTemplate()
            .exchange(
                    stubProvider.getUrl() + "/user-service/users",
                    HttpMethod.POST,
                    new HttpEntity<>(jsonBody, httpHeaders),
                    IdObject.class
            );

    assertThat(postResponse.getStatusCode().value()).isEqualTo(201);
    assertThat(postResponse.getHeaders().get("Content-Type").contains("application/json")).isTrue();

    assertThat(postResponse.getBody().getId()).isEqualTo(42);
  }

  @Test
  @PactVerification(fragment = "updatePersonPact")
  public void verifyUpdatePersonPact() {

    HttpHeaders httpHeaders = new HttpHeaders();
    httpHeaders.setContentType(MediaType.APPLICATION_JSON);
    String jsonBody = "{\"firstName\": \"Zaphod\",\"lastName\": \"Beeblebrox\"}";

    ResponseEntity<User> postResponse = new RestTemplate()
            .exchange(
                    stubProvider.getUrl() + "/user-service/users/42",
                    HttpMethod.PUT,
                    new HttpEntity<>(jsonBody, httpHeaders),
                    User.class
            );

    assertThat(postResponse.getStatusCode().value()).isEqualTo(200);
    assertThat(postResponse.getHeaders().get("Content-Type").contains("application/json")).isTrue();
    assertThat(postResponse.getBody().getFirstName()).isEqualTo("Zaphod");
    assertThat(postResponse.getBody().getLastName()).isEqualTo("Beeblebrox");
  }

//  Now that we have our contract, we can use to create tests against it for both the client and the provider.
//
//  Each of these tests will use a mock of its counterpart which is based on the contract, meaning:
//
//  the client will use a mock provider
//  the provider will use a mock client
//  Effectively, the tests are done against the contract.
}
