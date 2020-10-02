package hu.elte.issuetrackerrest;

import hu.elte.issuetrackerrest.entities.Issue;
import java.time.LocalDateTime;
import java.util.List;
import static org.assertj.core.api.Java6Assertions.assertThat;
import org.junit.jupiter.api.Test;
import org.skyscreamer.jsonassert.JSONAssert;
import org.skyscreamer.jsonassert.JSONCompareMode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class IssueControllerRestTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    public void greetingShouldReturnDefaultMessage() throws Exception {
        assertThat(
            this.restTemplate.getForObject("http://localhost:" + port + "/hello",
                    String.class)).contains("world");
    }
    
    @Test
    public void shouldReturnAllIssues() throws Exception {
        ResponseEntity<List<Issue>> response = restTemplate.exchange("http://localhost:" + port + "/issues", HttpMethod.GET, null, new ParameterizedTypeReference<List<Issue>>() {});
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().size()).isEqualTo(4);
    }
    
    @Test
    public void shouldReturnTheFirstIssue() throws Exception {
        ResponseEntity<Issue> response = restTemplate.getForEntity("http://localhost:" + port + "/issues/1", Issue.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        Issue issue = response.getBody();
        assertThat(issue.getTitle()).isEqualTo("issue1");
    }
    
    @Test
    public void shouldReturnTheFirstIssueAsString() throws Exception {
        ResponseEntity<String> response = restTemplate.getForEntity("http://localhost:" + port + "/issues/1", String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        JSONAssert.assertEquals("{title:issue1,description:description1}", response.getBody(), false);
    }
    
    @Test
    public void shouldSaveAPostedIssue() throws Exception {
        ResponseEntity<List<Issue>> response = restTemplate.exchange("http://localhost:" + port + "/issues", HttpMethod.GET, null, new ParameterizedTypeReference<List<Issue>>() {});
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().size()).isEqualTo(4);
        
        Issue issue = new Issue();
        issue.setTitle("new title");
        issue.setDescription("new description");
        issue.setPlace("new place");
        issue.setStatus(Issue.Status.NEW);
        
        ResponseEntity<Issue> responsePost = restTemplate.postForEntity("http://localhost:" + port + "/issues", issue, Issue.class);
        assertThat(responsePost.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(responsePost.getBody().getId()).isNotNull();
        assertThat(responsePost.getBody().getId()).isEqualTo(5);
        assertThat(responsePost.getBody().getTitle()).isEqualTo("new title");
        assertThat(responsePost.getBody().getDescription()).isEqualTo("new description");
        assertThat(responsePost.getBody().getPlace()).isEqualTo("new place");
        assertThat(responsePost.getBody().getStatus()).isEqualTo(Issue.Status.NEW);
        assertThat(responsePost.getBody().getCreated_at()).isNotNull();
        assertThat(responsePost.getBody().getUpdated_at()).isNotNull();
        
        ResponseEntity<List<Issue>> response2 = restTemplate.exchange("http://localhost:" + port + "/issues", HttpMethod.GET, null, new ParameterizedTypeReference<List<Issue>>() {});
        assertThat(response2.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response2.getBody().size()).isEqualTo(5);
    }
    
    @Test
    public void shouldReturnAllIssues2() throws Exception {
        ResponseEntity<List<Issue>> response = restTemplate.exchange("http://localhost:" + port + "/issues", HttpMethod.GET, null, new ParameterizedTypeReference<List<Issue>>() {});
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().size()).isEqualTo(4);
    }
    
}
