package com.soohea.wxshop.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.soohea.wxshop.entity.LoginResponse;
import com.soohea.wxshop.generate.User;
import org.apache.http.client.methods.*;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.cookie.Cookie;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicHeader;
import org.apache.http.util.EntityUtils;
import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.configuration.ClassicConfiguration;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

import static com.soohea.wxshop.service.TelVerificationServiceTest.VALID_PARAMETER_CODE;
import static java.net.HttpURLConnection.HTTP_OK;

public class AbstractIntegrationTest {
    @Autowired
    Environment environment;

    private CloseableHttpClient httpClient = HttpClientBuilder.create().build();

    HttpClientContext context = HttpClientContext.create();


    @Value("${spring.datasource.url}")
    private String databaseUrl;
    @Value("${spring.datasource.username}")
    private String databaseUsername;
    @Value("${spring.datasource.password}")
    private String databasePassword;

    @BeforeEach
    public void initDatabase() {
        //在每个测试开始前，执行一次flyway：clean flyway:migrate
        ClassicConfiguration conf = new ClassicConfiguration();
        conf.setDataSource(databaseUrl, databaseUsername, databasePassword);
        Flyway flyway = new Flyway(conf);
        flyway.clean();
        flyway.migrate();
    }

    public static ObjectMapper objectMapper = new ObjectMapper();

    public UserLoginResponse loginAndGetCookie() throws IOException, URISyntaxException {
        //最开始默认状态下，访问/api/status处于未登录状态
        String statusResponse = doHttpRequest("/api/v1/status", "GET", null, null).body;
        LoginResponse statusResponseData = objectMapper.readValue(statusResponse, LoginResponse.class);
        Assertions.assertFalse(statusResponseData.isLogin());

        //发送验证码
        int responseCode = doHttpRequest("/api/v1/code", "POST", VALID_PARAMETER_CODE, null).code;
        Assertions.assertEquals(HTTP_OK, responseCode);


        //带着验证码进行登录，得到Cookie
        HttpResponse loginResponse = doHttpRequest("/api/v1/login", "POST", VALID_PARAMETER_CODE, null);
        List<Cookie> cookies = loginResponse.cookies;
        String cookie = cookies.stream().filter(c -> c.getName().contains("JSESSIONID")).findFirst().get().getValue();

        statusResponse = doHttpRequest("/api/v1/status", "GET", null, cookie).body;
        statusResponseData = objectMapper.readValue(statusResponse, LoginResponse.class);

        return new UserLoginResponse(cookie, statusResponseData.getUser());
    }

    public String getUrl(String apiName) {
        // 获取集成测试的端口号
        return "http://localhost:" + environment.getProperty("local.server.port") + apiName;
    }


    public HttpResponse doHttpRequest(String apiName, String httpMethod, Object requestBody, String cookie) throws IOException, URISyntaxException {
        HttpRequestBase requestBase;
        switch (httpMethod) {
            case "GET":
                requestBase = new HttpGet();
                break;
            case "POST":
                requestBase = new HttpPost();
                break;
            case "DELETE":
                requestBase = new HttpDelete();
                break;
            case "PATCH":
                requestBase = new HttpPatch();
                break;
            case "PUT":
                requestBase = new HttpPut();
                break;
            default:
                throw new IllegalStateException("Unexpected value: " + httpMethod);
        }
        requestBase.setURI(new URI(getUrl(apiName)));
        requestBase.setHeader("Content-Type", "application/json;charset=UTF-8");
        requestBase.setHeader("Accept", "application/json;charset=UTF-8");
        if (cookie != null) {
            requestBase.addHeader(new BasicHeader("Cookie", cookie));
        }
        if (requestBody != null) {
            StringEntity entity = new StringEntity(objectMapper.writeValueAsString(requestBody), "UTF-8");
            ((HttpEntityEnclosingRequestBase) requestBase).setEntity(entity);
        }
        org.apache.http.HttpResponse response;

        response = httpClient.execute(requestBase, context);


        List<Cookie> cookies = context.getCookieStore().getCookies();

        String body = EntityUtils.toString(response.getEntity(), "UTF-8");

        return new HttpResponse(response.getStatusLine().getStatusCode(), body, cookies);
    }

    public static class UserLoginResponse {
        String cookie;
        User user;

        public UserLoginResponse(String cookie, User user) {
            this.cookie = cookie;
            this.user = user;
        }
    }


    public static class HttpResponse {
        int code;
        String body;
        List<Cookie> cookies;

        HttpResponse(int code, String body, List<Cookie> cookies) {
            this.code = code;
            this.body = body;
            this.cookies = cookies;
        }

        public <T> T asJsonObject(TypeReference<T> typeReference) throws JsonProcessingException {
            return objectMapper.readValue(body, typeReference);

        }
    }

}
