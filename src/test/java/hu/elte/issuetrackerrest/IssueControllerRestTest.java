package hu.elte.issuetrackerrest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import hu.elte.issuetrackerrest.entities.Issue;
import hu.elte.issuetrackerrest.entities.User;
import java.util.List;
import static org.assertj.core.api.Java6Assertions.assertThat;
import org.json.JSONObject;
import org.junit.jupiter.api.Test;
import org.skyscreamer.jsonassert.JSONAssert;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
//@DirtiesContext(classMode = ClassMode.BEFORE_EACH_TEST_METHOD)
public class IssueControllerRestTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    private String getTokenForUser(String username, String password) throws Exception {
        User user = new User();
        user.setUsername(username);
        user.setPassword(password);
        
        ResponseEntity<String> responseAuth = restTemplate.postForEntity("/api/auth", user, String.class);
        String jsonString = responseAuth.getBody();
        JSONObject json2 = new JSONObject(jsonString);
        return json2.getString("token");
    }
    
    private HttpEntity getRequestEntityForUser(String username, String password) throws Exception {
        String token = getTokenForUser(username, password);
        
        HttpHeaders headers = new HttpHeaders();
        headers.add("Cookie", "token=" + token);
//        headers.add("Authorization", "Bearer " + token);
        return new HttpEntity(null, headers);
    }
    
    @Test
    public void greetingShouldReturnDefaultMessage() throws Exception {
        System.out.println("Test 1");
        HttpEntity requestEntity = getRequestEntityForUser("user", "user");
        ResponseEntity<String> response = restTemplate.exchange(
                "http://localhost:" + port + "/hello",
                HttpMethod.GET,
                requestEntity,
                String.class
        );
        
        assertThat(response.getBody()).contains("world");
    }
    
    @Test
    public void shouldReturnAllIssuesForUser() throws Exception {
        System.out.println("Test 2");
        HttpEntity requestEntity = getRequestEntityForUser("user", "user");
        ResponseEntity<List<Issue>> response = restTemplate.exchange("http://localhost:" + port + "/issues", HttpMethod.GET, requestEntity, new ParameterizedTypeReference<List<Issue>>() {});
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().size()).isEqualTo(2);
    }
    
    @Test
    public void shouldReturnAllIssuesForAdmin() throws Exception {
        System.out.println("Test 3");
        HttpEntity requestEntity = getRequestEntityForUser("admin", "admin");
        ResponseEntity<List<Issue>> response = restTemplate.exchange(
                "http://localhost:" + port + "/issues", 
                HttpMethod.GET, 
                requestEntity, 
                new ParameterizedTypeReference<List<Issue>>() {});
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().size()).isEqualTo(4);
    }
    
    @Test
    public void shouldReturnTheFirstIssue() throws Exception {
        System.out.println("Test 4");
        HttpEntity requestEntity = getRequestEntityForUser("user", "user");
        ResponseEntity<Issue> response = restTemplate.exchange(
                "http://localhost:" + port + "/issues/1",
                HttpMethod.GET,
                requestEntity,
                Issue.class
        );
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        Issue issue = response.getBody();
        assertThat(issue.getTitle()).isEqualTo("issue1");
    }

    @Test
    public void shouldReturnTheFirstIssueAsString() throws Exception {
        System.out.println("Test 5");
        HttpEntity requestEntity = getRequestEntityForUser("user", "user");
        ResponseEntity<String> response = restTemplate.exchange(
                "http://localhost:" + port + "/issues/1",
                HttpMethod.GET,
                requestEntity,
                String.class
        );
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        JSONAssert.assertEquals("{title:issue1,description:description1}", response.getBody(), false);
    }
    
    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.AFTER_METHOD)
    public void shouldSaveAPostedIssue() throws Exception {
        System.out.println("Test 6");
        HttpEntity requestEntity = getRequestEntityForUser("admin", "admin");
        ResponseEntity<List<Issue>> responseList = restTemplate.exchange("http://localhost:" + port + "/issues", HttpMethod.GET, requestEntity, new ParameterizedTypeReference<List<Issue>>() {});
        assertThat(responseList.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(responseList.getBody().size()).isEqualTo(4);
        
        Issue issue = new Issue();
        issue.setTitle("new title");
        issue.setDescription("new description");
        issue.setPlace("new place");
        issue.setStatus(Issue.Status.NEW);
        
        String token = getTokenForUser("admin", "admin");
        
        HttpHeaders headers = new HttpHeaders();
        headers.add("Cookie", "token=" + token);
        headers.add("Content-Type", "application/json");
        
        ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
        String json = ow.writeValueAsString(issue);
        HttpEntity<String> requestEntityWithBody = new HttpEntity<String>(json, headers);
   
//        System.out.println(json);
        ResponseEntity<Issue> responsePost = restTemplate.exchange(
                "http://localhost:" + port + "/issues",
                HttpMethod.POST,
                requestEntityWithBody,
                Issue.class
        );
//        System.out.println(responsePost.getBody());
        assertThat(responsePost.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(responsePost.getBody().getId()).isNotNull();
        assertThat(responsePost.getBody().getId()).isEqualTo(5);
        assertThat(responsePost.getBody().getTitle()).isEqualTo("new title");
        assertThat(responsePost.getBody().getDescription()).isEqualTo("new description");
        assertThat(responsePost.getBody().getPlace()).isEqualTo("new place");
        assertThat(responsePost.getBody().getStatus()).isEqualTo(Issue.Status.NEW);
        assertThat(responsePost.getBody().getCreated_at()).isNotNull();
        assertThat(responsePost.getBody().getUpdated_at()).isNotNull();
        
        ResponseEntity<List<Issue>> responseList2 = restTemplate.exchange("http://localhost:" + port + "/issues", HttpMethod.GET, requestEntity, new ParameterizedTypeReference<List<Issue>>() {});
        assertThat(responseList2.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(responseList2.getBody().size()).isEqualTo(5);
    }
    
    @Test
    public void shouldReturnAllIssues2() throws Exception {
        System.out.println("Test 7");
        HttpEntity requestEntity = getRequestEntityForUser("admin", "admin");
        ResponseEntity<List<Issue>> response = restTemplate.exchange(
                "http://localhost:" + port + "/issues", 
                HttpMethod.GET, 
                requestEntity, 
                new ParameterizedTypeReference<List<Issue>>() {});
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().size()).isEqualTo(4);
    }
    
}
