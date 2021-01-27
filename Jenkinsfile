def NAME = "BIWF-android"


pipeline {
    agent any
     tools {
            gradle "gradle-6.5"
        }
    options {
        timeout(time: 1, unit: 'HOURS')
    }
    stages {
        stage ('StaticCodeAnalyzer-KTLint') {
            steps {
               sh 'gradle clean ktlintCheck'
            }
            post {
                failure {
                    archiveArtifacts artifacts: 'app/build/reports/ktlint/*.*', fingerprint: true
                }
            }
            }



        stage('Run Sonar Scanner') {
                    steps {
                        withSonarQubeEnv('') {
                            sh "gradle sonar -Dsonar.projectKey=SFC-BIWF-mobile_scan -Dsonar.projectName=SFC-BIWF-mobile -Dsonar.host.url=https://sonar.foss.corp.intranet "
                        }
                    }
                }


        stage('Test') {
            steps {
                sh 'gradle  test'
                junit(allowEmptyResults: true, testResults: 'app/build/test-results/**/*.xml')
            }
        }

        stage('Build') {
            when {
                not { changeRequest() }
            }
            steps {
                sh 'gradle clean assemble'
            }
            }
         stage("Archive Artifacts") {
                              steps {
                                script {
                                  archiveArtifacts allowEmptyArchive: true,
                                      artifacts: '**/*.apk'
                                  cleanWs()
                                }
                              }
                               post {
                                      success {
                                                sh "java -jar $DIM_JAR UpdateFile /dbname=IT3000 /host=ne1itcprhas50.ne1.savvis.net /dsn=PCMS /user=$DIM_UID /pass=$DIM_PASSWD /directory=. /USER_DIRECTORY=$WORKSPACE /filename=$APK_PATH/apk-debug.apk /project=BIWFMOBILE:RELEASE /request=$DIM_SMR"
                                              }
                                          }
                            }
        }
    }