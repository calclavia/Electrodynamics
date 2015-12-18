var gulp = require('gulp');
var zip = require('gulp-zip');

gulp.task('default', function() {
  return gulp.src(['src/**/*'])
    .pipe(zip('edx.zip'))
    .pipe(gulp.dest('dist'));
});

gulp.task('watch', function(cb) {
  gulp.watch('src/**/*', ['default']);
});
