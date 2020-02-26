def NAME = "Centurylink-android"

pipeline {
    agent { label 'android' }
    options {
        timeout(time: 1, unit: 'HOURS')
    }
    stages {
        stage('Test') {
            steps {
                gradlew(args: ['clean', 'test'], name: NAME)
                androidLint(canComputeNew: false, defaultEncoding: '', healthy: '', pattern: '**/lint-results*.xml', unHealthy: '')
		        junit(allowEmptyResults: true, testResults: '**/build/reports/**/*.xml')
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