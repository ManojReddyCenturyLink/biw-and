def NAME = "Centurylink-android"

pipeline {
    agent { label 'android' }
    options {
        timeout(time: 1, unit: 'HOURS')
    }
    stages {
        stage('Test') {
            steps {
                gradlew(args: ['clean', 'test', 'lint'])
                androidLint(canComputeNew: false, defaultEncoding: '', healthy: '', pattern: 'app/build/reports/lint-results*.xml', unHealthy: '')
		        junit(allowEmptyResults: true, testResults: 'app/build/test-results/**/*.xml')
            }
            post {
                always {
                    danger()
                    sonar(projectVersion: env.BUILD_NUMBER)
                }
            }
        }

        stage('Build') {
            when {
                not { changeRequest() }
            }
            steps {
                gradlew(args: ['clean', 'assemble'], name: NAME)
            }
            post {
                success {
                    script { ota.publishAPK(name: NAME) }
                }
            }
        }

        stage('Documentation') {
            when {
                not { changeRequest() }
            }
            steps {
                gradlew(args: ['dokka'])
            }
            post {
                success {
                    publishHTML([
                        allowMissing: true, 
                        alwaysLinkToLastBuild: true, 
                        keepAll: false,
                        reportDir: 'app/build/dokka',
                        reportFiles: 'app/index.html',
                        reportName: 'Documentation'
                    ])
                }
            }
        }
    }
    post {
        always {
            slack(channels: ['#centurylink-alerts'], alertPullRequests: false, alertFailures: true, includeChanges: true)
        }
        failure {
            slack(channels: ['#centurylink-dev'], alertPullRequests: false, alertFailures: true, includeChanges: false)
        }
    }
}