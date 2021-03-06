package com.bettercloud.vault.vault.api;

import com.bettercloud.vault.Vault;
import com.bettercloud.vault.VaultConfig;
import com.bettercloud.vault.VaultException;
import com.bettercloud.vault.json.JsonObject;
import com.bettercloud.vault.vault.VaultTestUtils;
import com.bettercloud.vault.vault.mock.AuthRequestValidatingMockVault;
import org.eclipse.jetty.server.Server;
import org.junit.Test;

import javax.servlet.http.HttpServletRequest;
import java.util.function.Predicate;

import static com.bettercloud.vault.vault.VaultTestUtils.readRequestBody;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class AuthBackendAwsTests {

    @Test
    public void testLoginByAwsEc2Id() throws Exception {
        final Predicate<HttpServletRequest> isValidEc2IdRequest = (request) -> {
            try {
                JsonObject requestBody = readRequestBody(request).orElse(null);
                return requestBody != null && request.getRequestURI().endsWith("/auth/aws/login") &&
                        requestBody.getString("identity", "").equals("identity") &&
                        requestBody.getString("signature", "").equals("signature");
            } catch (Exception e) {
                return false;
            }
        };
        final AuthRequestValidatingMockVault mockVault =  new AuthRequestValidatingMockVault(isValidEc2IdRequest);

        final Server server = VaultTestUtils.initHttpMockVault(mockVault);
        server.start();

        final VaultConfig vaultConfig = new VaultConfig()
                .address("http://127.0.0.1:8999")
                .build();
        final Vault vault = new Vault(vaultConfig);

        String token = null;
        try {
            token = vault.auth()
                    .loginByAwsEc2("role","identity","signature", null, null)
                    .getAuthClientToken();
        } catch(VaultException e) { }

        server.stop();

        assertNotNull(token);
        assertEquals("c9368254-3f21-aded-8a6f-7c818e81b17a", token.trim());

    }

    @Test
    public void testLoginByAwsEc2Pkcs7() throws Exception {
        final Predicate<HttpServletRequest> isValidEc2pkcs7Request = (request) -> {
            try {
                JsonObject requestBody = readRequestBody(request).orElse(null);
                return requestBody != null && request.getRequestURI().endsWith("/auth/aws/login") &&
                      requestBody.getString("pkcs7", "").equals("pkcs7");
            } catch (Exception e) {
                e.printStackTrace(System.out);
                return false;
            }
        };
        final AuthRequestValidatingMockVault mockVault =  new AuthRequestValidatingMockVault(isValidEc2pkcs7Request);

        final Server server = VaultTestUtils.initHttpMockVault(mockVault);
        server.start();

        final VaultConfig vaultConfig = new VaultConfig()
                .address("http://127.0.0.1:8999")
                .build();
        final Vault vault = new Vault(vaultConfig);

        System.out.println("Running Aws EC2 test");

        String token = null;
        try {
            token = vault.auth()
                    .loginByAwsEc2("role","pkcs7",null,null)
                    .getAuthClientToken();
        } catch(VaultException e) { }

        server.stop();

        assertNotNull(token);
        assertEquals("c9368254-3f21-aded-8a6f-7c818e81b17a", token.trim());
    }

    @Test
    public void testLoginByAwsIam() throws Exception {
        final Predicate<HttpServletRequest> isValidEc2IamRequest = (request) -> {
            JsonObject requestBody = readRequestBody(request).orElse(null);
            return requestBody != null && request.getRequestURI().endsWith("/auth/aws/login") &&
                    requestBody.getString("iam_http_request_method", "").equals("POST") &&
                    requestBody.getString("iam_request_url", "").equals("url") &&
                    requestBody.getString("iam_request_body", "").equals("body") &&
                    requestBody.getString("iam_request_headers", "").equals("headers");
        };

        final AuthRequestValidatingMockVault mockVault =  new AuthRequestValidatingMockVault(isValidEc2IamRequest);

        final Server server = VaultTestUtils.initHttpMockVault(mockVault);
        server.start();

        final VaultConfig vaultConfig = new VaultConfig()
                .address("http://127.0.0.1:8999")
                .build();
        final Vault vault = new Vault(vaultConfig);

        final String token = vault.auth()
                .loginByAwsIam("role","url","body","headers",null)
                .getAuthClientToken();

        server.stop();

        assertNotNull(token);
        assertEquals("c9368254-3f21-aded-8a6f-7c818e81b17a", token.trim());
    }

}
