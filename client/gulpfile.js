"use strict";

var gulp = require('gulp');
var gutil = require('gulp-util');
var del = require('del');
var runSequence = require('run-sequence');
var sourcemaps = require('gulp-sourcemaps');
var source = require('vinyl-source-stream');
var buffer = require('vinyl-buffer');
var watchify = require('watchify');
var browserify = require('browserify');
var karma = require('karma').server;
var _ = require('lodash');
var jshint = require('gulp-jshint');
var uglify = require('gulp-uglify');
var ngAnnotate = require('gulp-ng-annotate');
var preprocess = require('gulp-preprocess');

var paths = {
    app: 'src/app/tic-tac-toe.js',
    html: ['src/app/**/*.html', '!src/app/**/*.spec.html'],
    resources: ['src/resources/**'],
    lib: ['src/lib/**/*.*'],
    libResources: ['node_modules/angular-material/angular-material.css'],
    tests: 'src/app/**/*.spec.js',
    build: 'build'
};

var bundles = require('./browserify-bundles');
var packageJson = require('./package.json');

var materialDesignSprites = ['action', 'alert', 'content', 'navigation'];

var DEVELOPMENT = "development";
var PRODUCTION = "production";
var context = {
    environment: DEVELOPMENT,
    version: packageJson.version
};

function ifEnv(env, fun) {
    if (context.environment == env) {
        return fun();
    } else {
        return gutil.noop();
    }
}


gulp.task('build-dev', function (cb) {
    context.environment = DEVELOPMENT;
    runSequence(
        'clean',
        [
            'js-libs',
            'js-app',
            'html',
            'resources',
            'lib',
            'lib-resources'
        ],
        cb);
});

gulp.task('build-prod', function (cb) {
    context.environment = PRODUCTION;
    runSequence(
        'clean',
        [
            'js-libs',
            'js-app',
            'html',
            'resources',
            'lib',
            'lib-resources'
        ],
        cb);
});


gulp.task('clean', function (cb) {
    del(paths.build + '/**/*.*', cb);
});

gulp.task('js-libs', function () {
    return bundles.libBundle
        .bundle()
        .on('error', gutil.log.bind(gutil, 'Browserify Error'))
        .pipe(source('libs.js'))
        .pipe(buffer())
        .pipe(sourcemaps.init({loadMaps: true}))
        .pipe(ifEnv(PRODUCTION, uglify))
        .pipe(sourcemaps.write('./'))
        .pipe(gulp.dest(paths.build));
});


gulp.task('jshint', function () {
    gulp.src('src/app/**/*.js')
        .pipe(jshint())
        .pipe(jshint.reporter('default'));
});


function bundleApp(bundler) {
    return bundler.bundle()
        .on('error', gutil.log.bind(gutil, 'Browserify Error'))
        .pipe(source('tic-tac-toe.js'))
        .pipe(buffer())
        .pipe(ngAnnotate())
        .pipe(sourcemaps.init({loadMaps: true}))
        .pipe(ifEnv(PRODUCTION, uglify))
        .pipe(sourcemaps.write('./'))
        .pipe(gulp.dest(paths.build));
}

gulp.task('js-app', function () {
    return bundleApp(bundles.appBundle);
});

gulp.task('watch:js-app', function () {
    var bundler = watchify(bundles.appBundle)
        .on('update', function () {
            return bundleApp(bundler);
        });
});

gulp.task('html', function () {
    return gulp.src(paths.html)
        .pipe(preprocess({context: context}))
        .pipe(gulp.dest(paths.build));
});

gulp.task('watch:html', function () {
    return gulp.watch(paths.html, ['html']);
});

gulp.task('resources', function () {
    return gulp.src(paths.resources)
        .pipe(gulp.dest(paths.build + '/resources'));
});

gulp.task('watch:resources', function () {
    return gulp.watch(paths.resources, ['resources']);
});

gulp.task('lib', function () {
    return gulp.src(paths.lib)
        .pipe(gulp.dest(paths.build));
});

gulp.task('lib-resources', function (done) {
    return gulp.src(paths.libResources)
        .pipe(gulp.dest(paths.build + '/resources'));
});


gulp.task('watch', function (cb) {
    runSequence(['watch:js-app', 'watch:html', 'watch:resources'], cb);
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
