export interface Organization {
  id: number;
  name: string;
  slug: string;
  description: string | null;
  logo: string | null;
  domain: string | null;
  owner: {
    id: number;
    firstName: string;
    lastName: string;
    email: string;
  };
  seats: {
    used: number;
    max: number | null;
  };
  courses: {
    used: number;
    max: number | null;
  };
  features: {
    whiteLabel: boolean;
    customDomain: boolean;
    apiAccess: boolean;
    scorm: boolean;
    liveSessions: boolean;
    sso: boolean;
  };
  branding: {
    primaryColor?: string;
    secondaryColor?: string;
    logo?: string;
    logoLight?: string;
    favicon?: string;
    customCss?: string;
    customJs?: string;
    headerHtml?: string;
    footerHtml?: string;
  } | null;
  settings: any;
  isActive: boolean;
  isVerified: boolean;
  createdAt: Date;
}

export interface OrganizationMember {
  id: number;
  user: {
    id: number;
    firstName: string;
    lastName: string;
    email: string;
  };
  role: 'owner' | 'admin' | 'manager' | 'instructor' | 'member';
  permissions: string[];
  isActive: boolean;
  joinedAt: Date;
}

export interface CreateOrganizationRequest {
  name: string;
  slug: string;
  description?: string;
  logo?: string;
}

export interface UpdateOrganizationRequest {
  name?: string;
  description?: string;
  logo?: string;
  domain?: string;
  branding?: any;
  settings?: any;
}

export interface InviteMemberRequest {
  email: string;
  role: 'admin' | 'manager' | 'instructor' | 'member';
  permissions?: string[];
}

export interface SsoConfig {
  provider: 'saml' | 'oauth2' | 'oidc' | 'ldap';
  config: {
    // SAML
    entityId?: string;
    ssoUrl?: string;
    certificate?: string;
    // OAuth2/OIDC
    clientId?: string;
    clientSecret?: string;
    authorizationUrl?: string;
    tokenUrl?: string;
    // LDAP
    ldapUrl?: string;
    ldapBaseDn?: string;
    ldapBindDn?: string;
    ldapSearchFilter?: string;
  };
}

export interface ApiKeys {
  apiKey: string;
  apiSecret: string;
}
