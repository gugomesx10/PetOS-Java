package br.com.petos.project.security;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;

import java.net.CookieManager;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.regex.Pattern;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, properties = {
        "spring.datasource.url=jdbc:h2:mem:petos-console-test;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE",
        "spring.h2.console.enabled=true",
        "petos.dev.seed.tutor-email=console-tutor@petos.test",
        "petos.dev.seed.password=console-test-only"
})
@ActiveProfiles({"dev", "test"})
class H2ConsoleSecurityIntegrationTest {

    @LocalServerPort
    private int port;

    @ParameterizedTest
    @ValueSource(strings = {"/h2-console", "/h2-console/", "/h2-console/login.jsp"})
    void consoleDoesNotRedirectToPetosLogin(String path) throws Exception {
        try (var client = HttpClient.newBuilder().followRedirects(HttpClient.Redirect.NORMAL).build()) {
            var response = client.send(request(path).GET().build(), HttpResponse.BodyHandlers.ofString());
            assertThat(response.statusCode()).isEqualTo(200);
            assertThat(response.uri().getPath()).startsWith("/h2-console");
            assertThat(response.body()).containsAnyOf("H2 Console", "login.jsp").doesNotContain("PetOS");
            assertThat(response.headers().firstValue("X-Frame-Options")).contains("SAMEORIGIN");
        }
    }

    @Test
    void consoleExceptionDoesNotRelaxWebOrApiSecurity() throws Exception {
        try (var client = HttpClient.newBuilder().cookieHandler(new CookieManager()).build()) {
            var web = client.send(request("/web").GET().build(), HttpResponse.BodyHandlers.ofString());
            assertThat(web.statusCode()).isEqualTo(302);
            assertThat(web.headers().firstValue("Location").orElseThrow()).endsWith("/login");

            var api = client.send(request("/pets").GET().build(), HttpResponse.BodyHandlers.ofString());
            assertThat(api.statusCode()).isEqualTo(401);
            assertThat(api.headers().firstValue("X-Frame-Options")).contains("DENY");

            var login = client.send(request("/login").GET().build(), HttpResponse.BodyHandlers.ofString());
            assertThat(login.body()).contains("name=\"_csrf\"");
            assertThat(login.headers().firstValue("X-Frame-Options")).contains("DENY");
            var token = Pattern.compile("name=\"_csrf\"[^>]*value=\"([^\"]+)\"").matcher(login.body());
            assertThat(token.find()).isTrue();
            String credentials = "email=console-tutor%40petos.test&password=console-test-only&_csrf="
                    + URLEncoder.encode(token.group(1), StandardCharsets.UTF_8);
            var signedIn = client.send(request("/login")
                    .header("Content-Type", "application/x-www-form-urlencoded")
                    .POST(HttpRequest.BodyPublishers.ofString(credentials)).build(),
                    HttpResponse.BodyHandlers.ofString());
            assertThat(signedIn.statusCode()).isEqualTo(302);
            var home = client.send(request("/web").GET().build(), HttpResponse.BodyHandlers.ofString());
            assertThat(home.statusCode()).isEqualTo(200);

            var missingCsrf = client.send(request("/logout").POST(HttpRequest.BodyPublishers.noBody()).build(),
                    HttpResponse.BodyHandlers.ofString());
            assertThat(missingCsrf.statusCode()).isBetween(400, 499);
            var stillSignedIn = client.send(request("/web").GET().build(), HttpResponse.BodyHandlers.ofString());
            assertThat(stillSignedIn.statusCode()).isEqualTo(200);
        }
    }

    private HttpRequest.Builder request(String path) {
        return HttpRequest.newBuilder(URI.create("http://localhost:" + port + path))
                .timeout(Duration.ofSeconds(10));
    }
}
