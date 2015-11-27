"use strict";

var gulp = require('gulp');
var gutil = require('gulp-util');
var del = require('del');
var runSequence = require('run-sequence');
var sourcemaps = require('gulp-sourcemaps');
var source = require('vinyl-source-stream');
var buffer = require('vinyl-buffer');
var browserify = require('browserify');
var watchify = require('watchify');
var karma = require('karma');
var _ = require('lodash');
var uglify = require('gulp-uglify');
var ngAnnotate = require('gulp-ng-annotate');
var preprocess = require('gulp-preprocess');
var gulpIf = require('gulp-if');



var projectPaths = require('./project-paths');
var browserifiers = require('./browserifiers');
var packageJson = require('./package.json');


var DEV = "development";
var PROD = "production";
var context = {
    env: DEV,
    version: packageJson.version
};

var karmaConf = {
    configFile: __dirname + '/karma.conf.js',
    logLevel: "INFO",
    singleRun: true,
    autoWatch: false
};



gulp.task('clean', function () {
    return del(projectPaths.build + '/**', {force: true});
});

gulp.task('build-dev', function (cb) {
    context.env = DEV;
    runSequence('build', cb);
});

gulp.task('test-dev', function (cb) {
    context.env = DEV;
    karmaConf.singleRun = true;
    karmaConf.autoWatch = false;
    karmaConf.colors = true;
    karmaConf.logLevel = "DEBUG";
    runSequence('js-tests', 'start-karma', cb);
});

gulp.task('develop', function (cb) {
    runSequence(['build-dev', 'watch'], cb);
});

gulp.task('tdd', ['develop'], function (cb) {
    karmaConf.singleRun = false;
    karmaConf.autoWatch = true;
    runSequence('develop', 'start-karma', cb);
});

gulp.task('build-prod', function (cb) {
    context.env = PROD;
    runSequence('build', cb);
});

gulp.task('test-prod', function (cb) {
    context.env = PROD;
    karmaConf.singleRun = true;
    karmaConf.autoWatch = false;
    karmaConf.colors = false;
    runSequence('js-tests', 'start-karma', cb);
});




gulp.task('watch', function (cb) {
    context.env = DEV;
    runSequence(['watch:js-app', 'watch:js-tests', 'watch:html', 'watch:resources'], cb);
});

gulp.task('build', function (cb) {
    runSequence(
        'clean',
        ['js-libs', 'js-app', 'html', 'resources', 'lib-resources'],
        cb);
});

function browserifyBuild(buildOpts) {
    var browserifyOpts = {
        debug: context.env === DEV
    };
    var browserified = buildOpts.browserifier(browserifyOpts);

    return browserified.bundle()
        .on('error', gutil.log.bind(gutil.log, "Browserify error:"))
        .pipe(source(buildOpts.outputFileName))
        .pipe(buffer())
        .pipe(gulpIf(buildOpts.ngAnnotate, ngAnnotate()))
        .pipe(sourcemaps.init({loadMaps: true}))
        .pipe(gulpIf(context.env === PROD, uglify()))
        .pipe(sourcemaps.write('./'))
        .pipe(gulp.dest(projectPaths.build));
}

gulp.task('js-libs', function () {
    return browserifyBuild({
        browserifier: browserifiers.forLibs,
        ngAnnotate: false,
        outputFileName: projectPaths.libDestName
    });
});

gulp.task('js-app', function() {
    return browserifyBuild({
        browserifier: browserifiers.forApp,
        ngAnnotate: true,
        outputFileName: projectPaths.appDestName
    });
});

gulp.task('watch:js-app', function () {
    var watchifier = function(opts) {
        return watchify(browserifiers.forApp(opts))
            .on('log', gutil.log.bind(gutil.log, "Watchify (app):"))
            .on('update', build);
    };

    return build();

    function build() {
        return browserifyBuild({
            browserifier: watchifier,
            ngAnnotate: true,
            outputFileName: projectPaths.appDestName
        });
    }
});

gulp.task('js-tests', function() {
    context.env = DEV;
    return browserifyBuild({
        browserifier: browserifiers.forTests,
        ngAnnotate: true,
        outputFileName: projectPaths.testDestName
    });
});

gulp.task('watch:js-tests', function () {
    var watchifier = function(opts) {
        return watchify(browserifiers.forTests(opts))
            .on('log', gutil.log.bind(gutil.log, "Watchify (tests):"))
            .on('update', build);
    };

    return build();

    function build() {
        return browserifyBuild({
            browserifier: watchifier,
            ngAnnotate: true,
            outputFileName: projectPaths.testDestName
        });
    }
});

gulp.task('html', function () {
    return gulp.src(projectPaths.html)
        .pipe(preprocess({context: context}))
        .pipe(gulp.dest(projectPaths.build));
});

gulp.task('watch:html', function () {
    return gulp.watch(projectPaths.html, ['html']);
});


gulp.task('resources', function () {
    return gulp.src(projectPaths.resources)
        .pipe(gulp.dest(projectPaths.build + '/resources'));
});

gulp.task('watch:resources', function () {
    return gulp.watch(projectPaths.resources, ['resources']);
});


gulp.task('lib-resources', function () {
    return gulp.src(projectPaths.libResources)
        .pipe(gulp.dest(projectPaths.build + '/resources'));
});

gulp.task('start-karma', function(cb) {
    var server = new karma.Server(karmaConf, cb);
    server.start();
});

