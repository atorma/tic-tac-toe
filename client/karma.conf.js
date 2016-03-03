"use strict";

var projectPaths = require('./project-paths');

module.exports = function (config) {
    config.set({
        basePath: '../public',
        files: [
            projectPaths.libDestName,
            projectPaths.appDestName,
            projectPaths.testDestName,
            {pattern: '**/*.js.map', included: false, served: true}
        ],
        exclude: [],
        frameworks: ['jasmine'],
        reporters: ['mocha'],
        browsers: ['PhantomJS'],
        preprocessors: {
            '**/*.js': ['sourcemap']
        },
        port: 9876,
        colors: true,
        logLevel: config.LOG_DEBUG, // possible values: config.LOG_DISABLE || config.LOG_ERROR || config.LOG_WARN || config.LOG_INFO || config.LOG_DEBUG
        singleRun: true,
        autoWatch: false
    });
};

