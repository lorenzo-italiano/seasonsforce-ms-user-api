package fr.polytech.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

@Service
public class ApiService {

    private static final Logger logger = LoggerFactory.getLogger(ApiService.class);

    @Autowired
    private RestTemplate restTemplate;

    /**
     * Make an API call.
     *
     * @param uri          URI of the API
     * @param method       HTTP method
     * @param responseType Class of the response
     * @param token        String - Access token from the user who adds the review
     * @param <T>          Type of the response
     * @return Response
     * @throws HttpClientErrorException if an error occurs while calling the API
     */
    public <T> T makeApiCall(String uri, HttpMethod method, Class<T> responseType, String token, T body) throws HttpClientErrorException {
        logger.info("Making API call to {}", uri);
        HttpHeaders headers = createHeaders(token);
        HttpEntity<?> entity = new HttpEntity<>(body, headers);
        ResponseEntity<T> response = restTemplate.exchange(uri, method, entity, responseType);
        if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
            return response.getBody();
        } else {
            throw new HttpClientErrorException(response.getStatusCode());
        }
    }

    /**
     * Create headers for API calls.
     *
     * @param token String - Access token from the user who adds the review
     * @return HttpHeaders
     * @throws HttpClientErrorException if the token is not valid
     */
    private HttpHeaders createHeaders(String token) throws HttpClientErrorException {
        if (token == null || !token.startsWith("Bearer ")) {
            throw new HttpClientErrorException(HttpStatus.UNAUTHORIZED);
        }
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token.replace("Bearer ", ""));
        return headers;
    }
}
