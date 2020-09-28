def NAME = "Centurylink-android"
def SLACK_ALWAYS_CHANNEL = "#centurylink-alerts"
def SLACK_FAIL_CHANNEL = "#centurylink-dev"

pipeline {
    agent { label 'android' }
    options {
        timeout(time: 1, unit: 'HOURS')
    }
    stages {
        stage('Test') {
            steps {
                gradlew(args: ['clean', 'test', 'lintVitalDevRelease'])
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
                gradlew(args: ['clean', 'assemble'],
                        localProperties: "9273c873-f51f-492a-b902-8e775375a56b",
                        keystore: "54fa0f17-1c30-450b-b9fc-f3856241f596",
                        keystoreName: "release.keystore",
                        name: NAME)
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
            script {
                def slackMessage = slack.defaultMessage()
                slackMessage += " - <${env.JENKINS_URL}/job/Centurylink/job/centurylink-android/job/master/Documentation|Documentation>"
                slack(channels: [SLACK_ALWAYS_CHANNEL], alertPullRequests: true, alertFailures: true, includeChanges: true, message: slackMessage)
            }
        }
        failure {
            slack(channels: [SLACK_FAIL_CHANNEL], alertPullRequests: true, alertFailures: true, includeChanges: false)
        }
    }
}