"use strict";

var browserify = require('browserify');
var watchify = require('watchify');
var _ = require('lodash');

/*
 * Dependencies
 * material-design-icons: 
 *  Breaks browserify. This is an icon package and does not have package.json main or index.js
 */
var packageJson = require('./package.json');
var dependencies = _(packageJson && packageJson.dependencies || {})
    .keys()
    .without('material-design-icons')
    .value();


var libBundle = browserify()
    .require(dependencies);

var appDependencies = dependencies; // Put manually defined dependencies here

var appBundle = browserify(watchify.args)
    .add('./src/app/tic-tac-toe.js')
    .external(appDependencies);


module.exports = {
    libBundle: libBundle,
    appBundle: appBundle,
    appDependencies: appDependencies
};