"use strict";

var gulp = require('gulp');
var gutil = require('gulp-util');
var del = require('del');
var runSequence = require('run-sequence');
var sourcemaps = require('gulp-sourcemaps');
var source = require('vinyl-source-stream');
var buffer = require('vinyl-buffer');
var watchify = require('watchify');
var karma = require('karma').server;
var _ = require('lodash');
var jshint = require('gulp-jshint');
var uglify = require('gulp-uglify');
var ngAnnotate = require('gulp-ng-annotate');
var preprocess = require('gulp-preprocess');
var gulpIf = require('gulp-if');

var projectPaths = require('./project-paths');
var browserified = require('./browserified');
var packageJson = require('./package.json');


var DEV = "development";
var PROD = "production";
var context = {
    env: DEV,
    version: packageJson.version
};


gulp.task('build-dev', function (cb) {
    context.env = DEV;
    runSequence('build', cb);
});

gulp.task('build-prod', function (cb) {
    context.env = PROD;
    runSequence('build', cb);
});

gulp.task('watch', function (cb) {
    context.env = DEV;
    runSequence(['watch:js-app', 'watch:html', 'watch:resources'], cb);
});

gulp.task('clean', function () {
    return del(projectPaths.build + '/**', {force: true});
});


gulp.task('build', function (cb) {
    runSequence(
        'clean',
        ['js-libs', 'js-app', 'html', 'resources', 'lib-resources'],
        cb);
});


function browserifyBuild(params) {
    return params.browserified.bundle()
        .on('error', gutil.log.bind(gutil.log, "Browserify error:"))
        .pipe(source(params.outputFileName))
        .pipe(buffer())
        .pipe(gulpIf(params.ngAnnotate, ngAnnotate()))
        .pipe(sourcemaps.init({loadMaps: true}))
        .pipe(gulpIf(context.env == PROD, uglify()))
        .pipe(sourcemaps.write('./'))
        .pipe(gulp.dest(projectPaths.build));
}

gulp.task('js-libs', function () {
    return browserifyBuild({
        browserified: browserified.lib,
        ngAnnotate: false,
        outputFileName: projectPaths.libDestName
    });
});

gulp.task('js-app', function() {
    return browserifyBuild({
        browserified: browserified.app,
        ngAnnotate: true,
        outputFileName: projectPaths.appDestName
    });
});

gulp.task('watch:js-app', function () {
    var watchified = watchify(browserified.app)
        .on('log', gutil.log.bind(gutil.log, "Watchify:"))
        .on('update', build);

    return build();

    function build() {
        return browserifyBuild({
            browserified: watchified,
            ngAnnotate: true,
            outputFileName: projectPaths.appDestName
        });
    }
});


gulp.task('jshint', function () {
    return gulp.src(projectPaths.appSourceAll)
        .pipe(jshint())
        .pipe(jshint.reporter('default'));
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

gulp.task('test', function () {
    return karma.start({
        configFile: __dirname + '/karma.conf.js',
        singleRun: true
    });
});

gulp.task('tdd', function (done) {
    karma.start({
        configFile: __dirname + '/karma.conf.js'
    }, done);
});

gulp.task('develop', function (cb) {
    runSequence(['build-dev', 'watch'], cb);
});
