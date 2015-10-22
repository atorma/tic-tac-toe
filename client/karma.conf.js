"use strict";

var toPath = require('join-path');

var projectPaths = require('./project-paths');

var libPath = toPath(projectPaths.build, projectPaths.libDestName);
var appPath = toPath(projectPaths.build, projectPaths.appDestName);
var testPath = toPath(projectPaths.build, projectPaths.testDestName);

module.exports = function (config) {
    config.set({
        basePath: '',
        files: [libPath, appPath, testPath],
        exclude: [],
        frameworks: ['browserify', 'jasmine'],
        reporters: ['mocha'],
        port: 9876,
        colors: true,
        logLevel: config.LOG_DEBUG, // possible values: config.LOG_DISABLE || config.LOG_ERROR || config.LOG_WARN || config.LOG_INFO || config.LOG_DEBUG
        autoWatch: true,
        browsers: ['PhantomJS'],
        singleRun: false
    });
};

