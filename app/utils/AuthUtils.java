package utils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleTokenResponse;
import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestFactory;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import models.User;
import play.mvc.Http;

import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Collection;

/**
 * Created by vinodvr on 21/06/15.
 */
public class AuthUtils {
    public static final String GOOGLE_CLIENT_ID = "17546691062-ccpkr1fagptio0225ceeucuehf9ehstv.apps.googleusercontent.com";
    private static final String GOOGLE_CLIENT_SECRET = "zI_65xFZfsgSDQP6bkJRZ7sj";
    public static final String OAUTH2_REDIRECT_URI = "http://localhost:9000/login";
    private static final String USER_INFO_URL = "https://www.googleapis.com/oauth2/v1/userinfo";
    private static final Collection<String> SCOPE = Arrays.asList("https://www.googleapis.com/auth/userinfo.profile;https://www.googleapis.com/auth/userinfo.email".split(";"));
    private static final JsonFactory JSON_FACTORY = new JacksonFactory();
    private static final HttpTransport HTTP_TRANSPORT = new NetHttpTransport();

    public static User getUserInfo(String code) throws Exception {
        GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
                HTTP_TRANSPORT, JSON_FACTORY, GOOGLE_CLIENT_ID, GOOGLE_CLIENT_SECRET, SCOPE).build();
        GoogleTokenResponse response = flow.newTokenRequest(code).setRedirectUri(OAUTH2_REDIRECT_URI).execute();
        Credential credential = flow.createAndStoreCredential(response, null);
        HttpRequestFactory requestFactory = HTTP_TRANSPORT.createRequestFactory(credential);

        final GenericUrl url = new GenericUrl(USER_INFO_URL);
        final HttpRequest request = requestFactory.buildGetRequest(url);
        request.getHeaders().setContentType("application/json");
        final String jsonIdentity = request.execute().parseAsString();

        JsonNode node = new ObjectMapper().readTree(jsonIdentity);
        User user= new User();
        user.email = node.get("email").asText();
        user.name = node.get("name").asText();
        user.imageUrl = node.get("picture").asText();
        return user;
    }

    public static String generateStateToken() {
        return "youlearn-state" + new SecureRandom().nextLong();
    }


    public static User getUserInfo(Http.Session session) {
        User user = new User();
        user.name = session.get("name");
        user.email = session.get("email");
        user.imageUrl = session.get("imageUrl");

        return user;
    }
}
