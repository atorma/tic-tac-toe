"use strict";

var paths = {
    appSourceMain: 'src/main/tic-tac-toe.js',
    appSourceAll: 'src/main/**/*.js',
    html: ['src/main/**/*.html', '!src/main/**/*.spec.html'],
    resources: ['src/main/resources/**'],
    libResources: ['node_modules/angular-material/angular-material.css'],
    tests: 'src/test/**/*.spec.js',
    build: '../public',
    appDestName: 'tic-tac-toe.js',
    libDestName: 'libs.js',
    testDestName: 'tic-tac-toe.spec.js'
};

module.exports = paths;
