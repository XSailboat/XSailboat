module.exports = function (grunt) {

    // Project configuration.
    grunt.initConfig({
        pkg: grunt.file.readJSON('package.json'),
        meta: {
            src1: './src/main/resources/templates',
            src2: './src/main/resources/static'
        },
        sass: {
            dist: {
                files:
                {
                    '<%= meta.src2 %>': '<%= meta.src1 %>',
                    './target/classes/templates': '<%= meta.src1 %>',
                    './target/classes/static': '<%= meta.src2 %>'
                },
                options: {
                    loadPath: ['src/main/resources/static/','src/main/resources/templates/'],
                    style: 'expanded'
                }
            }
        },
        // cssmin: {
        //     min: {
        //         files: {
        //             // '<%= meta.src1 %>/decorators/**.min.css': '<%= meta.src1 %>/decorators/main.css'
        //         }
        //     }
        // },
        watch: {
            scripts: {
                files: [
                    '<%= meta.src2 %>/**/*.scss',
                    '<%= meta.src1 %>/**/*.scss'
                ],
                tasks: ['sass']
            }
        }
    });

    // 加载包含 "uglify" 任务的插件。
    grunt.loadNpmTasks('grunt-contrib-sass');
    grunt.loadNpmTasks('grunt-contrib-cssmin');
    grunt.loadNpmTasks('grunt-contrib-watch');

    // 默认被执行的任务列表。
    grunt.registerTask('default', ['watch']);

};