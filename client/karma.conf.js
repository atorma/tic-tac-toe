"use strict";

var toPath = require('join-path');

var browserified = require('./browserified');
var projectPaths = require('./project-paths');

var preprocessors = {};
preprocessors[projectPaths.tests] = ['browserify'];

var libsPath = toPath(projectPaths.build, projectPaths.libDestName);
var libsMapPath = libsPath + '.map';

module.exports = function (config) {
    config.set({
        basePath: '',
        files: [
            libsPath,
            {pattern: libsMapPath, included: false},
            projectPaths.tests
        ],
        exclude: [],
        frameworks: ['browserify', 'jasmine'],
        reporters: ['mocha'],
        preprocessors: preprocessors,
        browserify: {
            debug: true,
            configure: function (bundler) {
                bundler.on('prebundle', function () {
                    bundler.external(browserified.appDependencies);
                });
            }
        },
        port: 9876,
        colors: true,
        logLevel: config.LOG_INFO, // possible values: config.LOG_DISABLE || config.LOG_ERROR || config.LOG_WARN || config.LOG_INFO || config.LOG_DEBUG
        autoWatch: true,
        browsers: ['PhantomJS'],
        singleRun: false
    });
};

