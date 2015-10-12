var bundles = require('./browserify-bundles');

module.exports = function (config) {
    config.set({
        basePath: '',
        files: [
            'build/libs.js',
            {pattern: 'build/libs.js.map', included: false},
            'src/app/**/*.spec.js'
        ],
        exclude: [],
        frameworks: ['browserify', 'jasmine'],
        reporters: ['mocha'],
        preprocessors: {
            'src/app/**/*.spec.js': ['browserify']
        },
        browserify: {
            debug: true,
            configure: function (bundle) {
                bundle.on('prebundle', function () {
                    bundle.external(bundles.appDependencies);
                });
            }
        },
        port: 9876,
        colors: true,
        logLevel: config.LOG_INFO, // possible values: config.LOG_DISABLE || config.LOG_ERROR || config.LOG_WARN || config.LOG_INFO || config.LOG_DEBUG
        autoWatch: true,
        browsers: ['Chrome'],
        singleRun: false
    });
};

