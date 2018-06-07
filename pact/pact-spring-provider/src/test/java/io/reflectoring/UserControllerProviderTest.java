package io.reflectoring;

import au.com.dius.pact.provider.junit.Provider;
import au.com.dius.pact.provider.junit.State;
import au.com.dius.pact.provider.junit.loader.PactBroker;
import au.com.dius.pact.provider.junit.loader.PactBrokerAuth;
import au.com.dius.pact.provider.junit.loader.PactFolder;
import au.com.dius.pact.provider.junit.target.HttpTarget;
import au.com.dius.pact.provider.junit.target.Target;
import au.com.dius.pact.provider.junit.target.TestTarget;
import au.com.dius.pact.provider.spring.SpringRestPactRunner;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.when;

//The second step of our contract verification is creating a test for the provider using a mock client based on the contract

@RunWith(SpringRestPactRunner.class)
@Provider("userservice")// tells pact that we’re testing the provider called “userservice”. Pact will automatically filter out all interactions from the loaded pact files that are not addressed at the provider “userservice”

//With @PactFolder we tell Pact where to look for pact files that serve as the base for our contract test. Note that there are other options for loading pact files such as the @PactBroker annotation.
//@PactFolder("/home/dani/workspace/pact-code-examples-master/pact/pact-feign-consumer/build/pacts")
@PactBroker(host = "localhost", port = "80")
@PactBrokerAuth(scheme = "Basic",username = "pactbrokeruser" , password = "TheUserPassword")
@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT,
        properties = {"server.port=8080"}
)
public class UserControllerProviderTest {

  @MockBean
  private UserRepository userRepository;

  //we’ll define the target to be used for verifying the interactions in the contract and start up the Spring Boot app before running the tests
//  Since Pact creates a mock consumer for us that “replays” all requests from the pact files, it needs to know where to send those requests.
//  With @TestTarget we mark a field of type Target that provides exactly this information. In our case, we tell Pact to send the requests via HTTP to localhost:8080,
//  since we started the Spring Boot application on the same port.
  @TestTarget
  public final Target target = new HttpTarget(8080);

  //Finally, we create a method that puts our Spring Boot application into a defined state that is suitable to respond to the mock consumer’s requests
// In our case, the pact file defines a single providerState named provider accepts a new person.
// In this method, we set up our mock repository so that it returns a suitable User object that fits the object expected in the contract.
  @State({"provider accepts a new person","person 42 exists"})
  public void toCreatePersonState() {

    User user = new User();
    user.setId(42L);
    user.setFirstName("Daniel");
    user.setLastName("de la Torre");
    when(userRepository.findOne(eq(42L))).thenReturn(user);
    when(userRepository.save(any(User.class))).thenReturn(user);
  }

}