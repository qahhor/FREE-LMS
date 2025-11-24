export const environment = {
  production: false,
  apiUrl: 'http://localhost:3000/api/v1',

  // Phase 3: Payment Gateways
  stripe: {
    publicKey: 'pk_test_your_stripe_public_key',
  },
  payme: {
    merchantId: 'your_payme_merchant_id',
  },
  click: {
    merchantId: 'your_click_merchant_id',
  },

  // Phase 3: Webinar Providers
  zoom: {
    apiKey: 'your_zoom_api_key',
    apiSecret: 'your_zoom_api_secret',
  },
  jitsi: {
    domain: 'meet.jit.si',
  },

  // Phase 3: SCORM
  scorm: {
    storageUrl: 'http://localhost:3000/scorm-content',
  },

  // Phase 3: Features
  features: {
    subscriptions: true,
    multiTenancy: true,
    scormSupport: true,
    webinars: true,
    whiteLabel: true,
    sso: true,
    api: true,
  },
};
