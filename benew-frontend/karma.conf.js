// Karma configuration file for Angular 20 project
// Used for running tests with headless Chrome in CI/CD pipeline

module.exports = function(config) {
  config.set({
    basePath: '',
    frameworks: ['jasmine'],
    plugins: [
      require('karma-jasmine'),
      require('karma-chrome-launcher'),
      require('karma-jasmine-html-reporter'),
      require('karma-coverage')
    ],
    client: {
      clearContext: false, // leave Jasmine Spec Runner output visible in browser
      jasmine: {
        random: false, // disable random test execution order
        seed: null,
        stopSpecOnExpectationFailure: false
      }
    },
    jasmineHtmlReporter: {
      suppressAll: true // removes the duplicated traces
    },
    coverageReporter: {
      dir: require('path').join(__dirname, './coverage/benew'),
      subdir: '.',
      reporters: [
        { type: 'html' },
        { type: 'text-summary' },
        { type: 'lcovonly' }
      ],
      check: {
        global: {
          statements: 0,
          branches: 0,
          functions: 0,
          lines: 0
        }
      }
    },
    reporters: ['progress', 'kjhtml'],
    browsers: ['Chrome'],
    restartOnFileChange: true,
    
    // Custom launcher for headless Chrome (used in CI/CD)
    customLaunchers: {
      ChromeHeadlessCI: {
        base: 'ChromeHeadless',
        flags: [
          '--no-sandbox',
          '--disable-gpu',
          '--disable-dev-shm-usage',
          '--disable-software-rasterizer',
          '--disable-extensions',
          '--remote-debugging-port=9222'
        ]
      }
    },
    
    // Timeouts
    browserNoActivityTimeout: 30000,
    browserDisconnectTimeout: 10000,
    browserDisconnectTolerance: 3,
    captureTimeout: 60000,
    
    // Single run mode for CI/CD
    singleRun: false,
    
    // Port configuration
    port: 9876,
    colors: true,
    logLevel: config.LOG_INFO,
    autoWatch: true
  });
};
