package com.freelms.integration.security;

import com.freelms.integration.token.ApiToken;

import java.security.Principal;
import java.util.Set;

/**
 * Smartup LMS - API Token Principal
 *
 * Represents an authenticated API token in the security context.
 */
public class ApiTokenPrincipal implements Principal {

    private final Long tokenId;
    private final String tokenName;
    private final Long organizationId;
    private final ApiToken.TokenType tokenType;
    private final Set<String> scopes;

    public ApiTokenPrincipal(ApiToken apiToken) {
        this.tokenId = apiToken.getId();
        this.tokenName = apiToken.getName();
        this.organizationId = apiToken.getOrganizationId();
        this.tokenType = apiToken.getType();
        this.scopes = apiToken.getScopes();
    }

    @Override
    public String getName() {
        return tokenName;
    }

    public Long getTokenId() {
        return tokenId;
    }

    public String getTokenName() {
        return tokenName;
    }

    public Long getOrganizationId() {
        return organizationId;
    }

    public ApiToken.TokenType getTokenType() {
        return tokenType;
    }

    public Set<String> getScopes() {
        return scopes;
    }

    public boolean hasScope(String scope) {
        return scopes.contains(scope) || scopes.contains("*");
    }

    public boolean isFullAccess() {
        return tokenType == ApiToken.TokenType.FULL_ACCESS;
    }

    public boolean canRead() {
        return tokenType != ApiToken.TokenType.WEBHOOK;
    }

    public boolean canWrite() {
        return tokenType == ApiToken.TokenType.READ_WRITE || tokenType == ApiToken.TokenType.FULL_ACCESS;
    }

    @Override
    public String toString() {
        return "ApiTokenPrincipal{" +
                "tokenId=" + tokenId +
                ", tokenName='" + tokenName + '\'' +
                ", organizationId=" + organizationId +
                ", tokenType=" + tokenType +
                '}';
    }
}
