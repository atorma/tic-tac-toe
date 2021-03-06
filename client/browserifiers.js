"use strict";

var browserify = require('browserify');
var watchify = require('watchify');
var _ = require('lodash');
var glob = require('glob');

var projectPaths = require('./project-paths');

var packageJson = require('./package.json');
var dependencies = _(packageJson && packageJson.dependencies || {})
    .keys()
    .without('material-design-icons') // This is an icon package and does not have package.json main or index.js
    .value();

var appDependencies = dependencies; // Add manually defined dependencies to this array

var testFiles = glob.sync(projectPaths.tests);

module.exports = {
    forLibs: forLibs,
    forApp: forApp,
    forTests: forTests,
    appDependencies: appDependencies
};

function forLibs(browserifyOpts) {
    return getInstance(browserifyOpts)
        .require(dependencies);
}

function forApp(browserifyOpts) {
    return getInstance(browserifyOpts)
        .add(projectPaths.appSourceMain)
        .external(appDependencies);
}


function forTests(browserifyOpts) {
    return getInstance(browserifyOpts)
        .add(testFiles)
        .external(appDependencies)
        .external(projectPaths.appSourceAll);
}

function getInstance(additionalOpts) {
    var opts = _.extend({}, additionalOpts, watchify.args);
    return browserify(opts);
}

