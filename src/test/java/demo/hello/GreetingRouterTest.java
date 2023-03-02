package demo.hello;

import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.reactive.server.WebTestClient;
import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class GreetingRouterTest {
  @Autowired
  private WebTestClient webTestClient;

//  @Test
//  public void testHello() {
//    webTestClient.get().uri("/com.hello")
//        .accept(APPLICATION_JSON)
//        .exchange()
//        .expectStatus().isOk()
//        .expectBody(Greeting.class)
//        .value(pGreeting -> {
//          assertThat(pGreeting.getMessage()).isEqualTo("Hello from Spring!");
//        });
//  }
}
