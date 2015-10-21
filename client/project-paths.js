"use strict";

var paths = {
    appSourceMain: 'src/app/tic-tac-toe.js',
    appSourceAll: ['src/app/**/*.js', '!src/app/**/*.spec.js'],
    html: ['src/app/**/*.html', '!src/app/**/*.spec.html'],
    resources: ['src/resources/**'],
    libResources: ['node_modules/angular-material/angular-material.css'],
    tests: 'src/app/**/*.spec.js',
    build: '../public',
    appDestName: 'tic-tac-toe.js',
    libDestName: 'libs.js'
};

module.exports = paths;
