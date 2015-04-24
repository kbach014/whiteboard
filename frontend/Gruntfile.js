module.exports = function(grunt) {
	'use strict';

	grunt.initConfig({

		path: require('path'),

		pkg: grunt.file.readJSON('package.json'),

		clean: ['dist/'],

		concat: {
			options: {
				separator: ';\n\n'
			},
			dist: {
				src: ['js/**/*.js'],
				dest: 'whiteboard.js'
			}
		},

		less: {
			development: {
				options: {
					paths: ['css']
				},
				files: {
					'whiteboard.css': 'css/custom.less'
				}
			}
		},

		uglify: {
			options: {
				banner: '/*! <%= pkg.name %> <%= pkg.version %> <%= grunt.template.today("dd-mm-yyyy") %> */\n'
			},
			dist: {
				files: {
					'whiteboard.min.js': ['<%= concat.dist.dest %>']
				}
			}
		},

		jasmine: {
			confsys: {
				src: ['js/**/*.js'],
				options: {
					vendor: [
						'lib/lodash.min.js',
						'lib/angular.min.js',
						'lib/angular-route.min.js',
						'lib/angular-mocks.js'
					],
					specs: 'tests/**/*spec.js',
					helpers: 'tests/**/*helper.js'
				}
			}
		},

		jshint: {
			files: ['Gruntfile.js', 'js/**/*.js', 'tests/**/*.js'],
			options: {
				globals: {
					jQuery: true,
					console: true,
					module: true,
					document: true
				}
			}
		},

		connect: {
			server: {
				options: {
					port: 9000,
					hostname: 'localhost',
					livereload: true,
					open: true
				}
			}
		},

		watch: {
			server: {
				files: ['index.html', 'js/**/*.js', 'partials/**', 'css/**'],
				options: {livereload: true},
				tasks: ['reload']
			}
		},

		copy: {
			html: {
				src: ['index.html', 'partials/**', 'whiteboard.css', 'whiteboard.js', 'whiteboard.min.js'],
				dest: '../backend/src/main/webapp/',
			}
		}

	});


	grunt.loadNpmTasks('grunt-contrib-clean');
	grunt.loadNpmTasks('grunt-contrib-concat');
	grunt.loadNpmTasks('grunt-contrib-connect');
	grunt.loadNpmTasks('grunt-contrib-copy');
	grunt.loadNpmTasks('grunt-contrib-jshint');
	grunt.loadNpmTasks('grunt-contrib-jasmine');
	grunt.loadNpmTasks('grunt-contrib-less');
	grunt.loadNpmTasks('grunt-contrib-uglify');
	grunt.loadNpmTasks('grunt-contrib-watch');

	grunt.registerTask('test', [
		'jshint',
		'jasmine'
	]);

	grunt.registerTask('reload', [
		'jshint',
		'concat',
		'less'
	]);

	grunt.registerTask('default', [
		'clean',
		'jshint',
		'concat',
		'uglify',
		'less'
	]);

	grunt.registerTask('serve',[
		'reload',
		'connect',
		'watch'
	]);

	grunt.registerTask('deploy', [
		'default',
		'copy'
	]);

};
