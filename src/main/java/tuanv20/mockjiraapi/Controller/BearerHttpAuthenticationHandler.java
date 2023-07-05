package tuanv20.mockjiraapi.Controller;

import com.atlassian.httpclient.api.Request.Builder;
import com.atlassian.jira.rest.client.api.AuthenticationHandler;

public class BearerHttpAuthenticationHandler implements AuthenticationHandler {
    private static final String AUTHORIZATION_HEADER = "Authorization";
    private final String token = "OTY5Njk2Mzg2NzA4Oh0amfT2mfNUjmNrlL/zR0uRroQr";

    @Override
    public void configure(Builder builder) {
        builder.setHeader(AUTHORIZATION_HEADER, "Bearer " + token);
    }
}
