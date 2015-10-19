"use strict";

var browserify = require('browserify');
var watchify = require('watchify');
var _ = require('lodash');

var projectPaths = require('./project-paths');
var packageJson = require('./package.json');
var dependencies = _(packageJson && packageJson.dependencies || {})
    .keys()
    .without('material-design-icons') // This is an icon package and does not have package.json main or index.js
    .value();


var libs = browserify(watchify.args)
    .require(dependencies);

var appDependencies = dependencies; // Add manually defined dependencies to this array

var app = browserify(watchify.args)
    .add(projectPaths.appSourceMain)
    .external(appDependencies);


module.exports = {
    lib: libs,
    app: app,
    appDependencies: appDependencies
};