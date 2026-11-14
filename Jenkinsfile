// Pipeline CI/CD: Build & Test -> Jacoco 100% -> Trivy Scan -> Package -> Publish -> Deploy
// Sesuaikan nilai di blok environment / credentials dengan server Jenkins Anda.
pipeline {
    agent any

    options {
        timestamps()
        disableConcurrentBuilds()
        buildDiscarder(logRotator(numToKeepStr: '10'))
    }

    environment {
        APP_NAME    = 'ifs25030-p5-pbo2026-springboot-cashflow'
        DEPLOY_HOST = credentials('cashflow-deploy-host')   // mis. user@server
        DEPLOY_DIR  = '/opt/cashflow'
    }

    stages {
        stage('Build & Test') {
            steps {
                sh 'chmod +x mvnw'
                sh './mvnw -B -ntp clean test'
            }
            post {
                always {
                    junit allowEmptyResults: true, testResults: 'target/surefire-reports/*.xml'
                }
            }
        }

        stage('Jacoco 100%') {
            steps {
                // Gagal bila coverage (selain framework/view dan framework/auth) < 100%.
                sh './mvnw -B -ntp jacoco:report@report jacoco:check@jacoco-check'
            }
            post {
                always {
                    archiveArtifacts artifacts: 'target/site/jacoco/**', allowEmptyArchive: true
                }
            }
        }

        stage('Trivy Scan') {
            steps {
                sh '''
                    trivy fs --exit-code 1 --severity HIGH,CRITICAL \
                        --scanners vuln,secret --skip-dirs target,node_modules,.git .
                '''
            }
        }

        stage('Package') {
            steps {
                sh './mvnw -B -ntp -DskipTests -Pproduction clean package'
            }
        }

        stage('Publish') {
            steps {
                archiveArtifacts artifacts: 'target/*.jar', fingerprint: true
            }
        }

        stage('Deploy') {
            when { branch 'main' }
            steps {
                sshagent(credentials: ['cashflow-deploy-ssh']) {
                    sh '''
                        scp -o StrictHostKeyChecking=no target/${APP_NAME}-*.jar ${DEPLOY_HOST}:${DEPLOY_DIR}/app.jar
                        ssh -o StrictHostKeyChecking=no ${DEPLOY_HOST} "sudo systemctl restart cashflow"
                    '''
                }
            }
        }
    }

    post {
        success { echo 'Pipeline sukses.' }
        failure { echo 'Pipeline gagal, periksa log stage di atas.' }
    }
}
