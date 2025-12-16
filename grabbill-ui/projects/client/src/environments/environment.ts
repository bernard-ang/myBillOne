// This file can be replaced during build by using the `fileReplacements` array.
// `ng build` replaces `environment.ts` with `environment.prod.ts`.
// The list of file replacements can be found in `angular.json`.

export const environment = {
  production: false,
  config: {
    appUrl: 'http://localhost:4801',
    baseApiUrl: 'http://localhost:8080',
    websiteUrl: 'http://localhost:4800',
    shortDateFormat: 'dd/MM/yyyy',
    dateFormat: 'dd/MM/yyyy, h:mm:ss a',
    maxIndexRow: 20000,
    maxPdfFileSizeBytes: 20000000,
    maxZipFileSizeBytes: 500000000,
    maxFileSizeBytes: 10000000,
    experimentalFeature: true,
    contextSensitiveHelp: true,
    supportEmail: 'support@mybillone.com',
    smtpFromEmail: 'dev@mybillone.com',
    maxImageSizeBytes: 20000000,
    maxImageTotal: 100,
    canTopUpSms: false,
    isPaidPlanEnabled: true,
    deploymentMode: 'saas',
    firebase: {
      apiKey: 'AIzaSyBJwLNvl_jqvjeGPpUG0wY4AOzsDOPS8yQ',
      authDomain: 'grabbill-dev.firebaseapp.com',
      projectId: 'grabbill-dev',
      storageBucket: 'grabbill-dev.appspot.com',
      messagingSenderId: '645960359111',
      appId: '1:645960359111:web:dd6b86de5f7e92a69872e2',
      measurementId: 'G-8P3TXRSHFX',
    },
  },
};

/*
 * For easier debugging in development mode, you can import the following file
 * to ignore zone related error stack frames such as `zone.run`, `zoneDelegate.invokeTask`.
 *
 * This import should be commented out in production mode because it will have a negative impact
 * on performance if an error is thrown.
 */
// import 'zone.js/plugins/zone-error';  // Included with Angular CLI.
