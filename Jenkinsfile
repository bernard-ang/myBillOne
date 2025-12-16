import groovy.json.JsonOutput

def npmInstallDone = false

pipeline {
    parameters {
        booleanParam(name: "SKIP_DOCKER_BUILD", defaultValue: false, description: "Skip building Docker images")
        booleanParam(name: "SKIP_DOCKER_PUSH", defaultValue: false, description: "Skip pushing Docker images to registry")
        booleanParam(name: "SKIP_DEPLOY", defaultValue: false, description: "Skip deploying to UAT/Production environment")
        booleanParam(name: "SKIP_CHANGESET_OPTIMIZATION", defaultValue: false, description: "Ignore job optimizations based on changeset")
    }
    agent any
    environment {
        DO_TOKEN = credentials('grabbill-do-token')
        DO_OP_TOKEN = credentials('grabbill-op-do-token')

        UAT_BRANCH = 'deploy/uat'
        PROD_BRANCH = 'deploy/prod'
        OP_UAT_BRANCH = 'deploy/op-uat'
        OP_PROD_BRANCH = 'deploy/op-prod'
        OP_GENERALI_BRANCH = 'deploy/op-generali'

        SLACK_TOKEN_CRED_ID = 'slack-grabbill'
        SLACK_TEAM_DOMAIN = 'grabbill'
    }
    options {
        skipDefaultCheckout()

        buildDiscarder(logRotator(numToKeepStr: '10'))
    }
    tools {
        jdk 'java-11-openjdk'
        maven 'maven-3.8.6'
        nodejs 'nodejs-20.13.1'
    }
    stages {
        stage('init') {
            steps {
                cleanWs()
                checkout scm
                nodejs(nodeJSInstallationName: 'nodejs-20.13.1', configId: 'config-npm.evos.tech') {
                    sh 'node --version'
                    sh 'npm --version'
                    sh 'npm install -g @evos-tech/sidekick@next'
                }
            }
        }
        stage('build-backend') {
            when {
                anyOf {
                    expression {
                        params.SKIP_CHANGESET_OPTIMIZATION == true
                    }
                    changeset 'grabbill-backend/**'
                }
            }
            steps {
                dir('grabbill-backend') {
                    sh 'mvn clean install'
                }
            }
        }
        stage('overwrite-build-info') {
            steps {
                script {
                    def now = new Date();
                    def buildInfo = [
                            git: [
                                    hash     : sh(script: 'git describe --always', returnStdout: true).trim(),
                                    createdAt: now.format("yyyyMMdd")
                            ]
                    ]

                    writeFile file: 'grabbill-ui/buildinfo.json', text: JsonOutput.toJson(buildInfo)
                }
            }
        }

        stage('build-ui-client') {
            when {
                anyOf {
                    expression {
                        params.SKIP_CHANGESET_OPTIMIZATION == true
                    }
                    changeset 'grabbill-ui/*'
                    changeset 'grabbill-ui/projects/client/**'
                }
            }
            steps {
                dir('grabbill-ui') {
                    nodejs(nodeJSInstallationName: 'nodejs-20.13.1') {
                        script {
                            if (!npmInstallDone) {
                                sh 'npm ci'
                                npmInstallDone = true
                            }
                        }
                        sh 'npm run build:client'
                    }
                }
            }
        }
        stage('build-ui-admin') {
            when {
                anyOf {
                    expression {
                        params.SKIP_CHANGESET_OPTIMIZATION == true
                    }
                    changeset 'grabbill-ui/*'
                    changeset 'grabbill-ui/projects/admin/**'
                }
            }
            steps {
                dir('grabbill-ui') {
                    nodejs(nodeJSInstallationName: 'nodejs-20.13.1') {
                        script {
                            if (!npmInstallDone) {
                                sh 'npm ci'
                                npmInstallDone = true
                            }
                        }
                        sh 'npm run build:admin'
                    }
                }
            }
        }
        stage('build-docker-images') {
            when {
                expression {
                    params.SKIP_DOCKER_BUILD == false
                }
            }
            stages {
                stage('build-backend-images') {
                    when {
                        anyOf {
                            expression {
                                params.SKIP_CHANGESET_OPTIMIZATION == true
                            }
                            changeset 'grabbill-backend/**'
                        }
                    }
                    steps {
                        nodejs(nodeJSInstallationName: 'nodejs-20.13.1') {
                            sh 'sk dev build-image --name grabbill/grabbill-engine --dockerFile grabbill-backend/engine.Dockerfile'
                            sh 'sk dev build-image --name grabbill/grabbill-server --dockerFile grabbill-backend/server.Dockerfile'
                        }
                    }
                }
                stage('build-ui-uat-images') {
                    when {
                        branch "${UAT_BRANCH}"
                    }
                    stages {
                        stage('build-ui-admin-uat-image') {
                            when {
                                anyOf {
                                    changeset 'grabbill-ui/*'
                                    changeset 'grabbill-ui/config/**'
                                    changeset 'grabbill-ui/projects/admin/**'
                                    expression {
                                        params.SKIP_CHANGESET_OPTIMIZATION == true
                                    }
                                }
                            }
                            steps {
                                nodejs(nodeJSInstallationName: 'nodejs-20.13.1') {
                                    sh 'sk dev build-image --name grabbill/grabbill-admin-uat --dockerFile grabbill-ui/admin-uat.Dockerfile'
                                }
                            }
                        }
                        stage('build-ui-client-uat-image') {
                            when {
                                anyOf {
                                    changeset 'grabbill-ui/*'
                                    changeset 'grabbill-ui/config/**'
                                    changeset 'grabbill-ui/projects/client/**'
                                    expression {
                                        params.SKIP_CHANGESET_OPTIMIZATION == true
                                    }
                                }
                            }
                            steps {
                                nodejs(nodeJSInstallationName: 'nodejs-20.13.1') {
                                    sh 'sk dev build-image --name grabbill/grabbill-client-uat --dockerFile grabbill-ui/client-uat.Dockerfile'
                                }

                            }
                        }
                    }
                }
                stage('build-ui-prod-images') {
                    when {
                        branch "${PROD_BRANCH}"
                    }
                    stages {
                        stage('build-ui-admin-prod-images') {
                            when {
                                anyOf {
                                    changeset 'grabbill-ui/*'
                                    changeset 'grabbill-ui/config/**'
                                    changeset 'grabbill-ui/projects/admin/**'
                                    expression {
                                        params.SKIP_CHANGESET_OPTIMIZATION == true
                                    }
                                }
                            }
                            steps {
                                nodejs(nodeJSInstallationName: 'nodejs-20.13.1') {
                                    sh 'sk dev build-image --name grabbill/grabbill-admin --dockerFile grabbill-ui/admin.Dockerfile'
                                }
                            }
                        }
                        stage('build-ui-client-prod-images') {
                            when {
                                anyOf {
                                    changeset 'grabbill-ui/*'
                                    changeset 'grabbill-ui/config/**'
                                    changeset 'grabbill-ui/projects/client/**'
                                    expression {
                                        params.SKIP_CHANGESET_OPTIMIZATION == true
                                    }
                                }
                            }
                            steps {
                                nodejs(nodeJSInstallationName: 'nodejs-20.13.1') {
                                    sh 'sk dev build-image --name grabbill/grabbill-client --dockerFile grabbill-ui/client.Dockerfile'
                                }
                            }
                        }
                    }
                }
                stage('build-ui-op-uat-images') {
                    when {
                        branch "${OP_UAT_BRANCH}"
                    }
                    stages {
                        stage('build-ui-client-op-uat-image') {
                            when {
                                anyOf {
                                    changeset 'grabbill-ui/*'
                                    changeset 'grabbill-ui/config/**'
                                    changeset 'grabbill-ui/projects/client/**'
                                    expression {
                                        params.SKIP_CHANGESET_OPTIMIZATION == true
                                    }
                                }
                            }
                            steps {
                                nodejs(nodeJSInstallationName: 'nodejs-20.13.1') {
                                    sh 'sk dev build-image --name grabbill/grabbill-client-op-uat --dockerFile grabbill-ui/client-op-uat.Dockerfile'
                                }
                            }
                        }
                    }
                }
                stage('build-ui-op-prod-images') {
                    when {
                        branch "${OP_PROD_BRANCH}"
                    }
                    stages {
                        stage('build-ui-client-op-prod-image') {
                            when {
                                anyOf {
                                    changeset 'grabbill-ui/*'
                                    changeset 'grabbill-ui/config/**'
                                    changeset 'grabbill-ui/projects/client/**'
                                    expression {
                                        params.SKIP_CHANGESET_OPTIMIZATION == true
                                    }
                                }
                            }
                            steps {
                                nodejs(nodeJSInstallationName: 'nodejs-20.13.1') {
                                    sh 'sk dev build-image --name grabbill/grabbill-client-op --dockerFile grabbill-ui/client-op-prod-sunlife.Dockerfile'
                                }
                            }
                        }
                    }
                }
                stage('build-ui-op-generali-images') {
                    when {
                        branch "${OP_GENERALI_BRANCH}"
                    }
                    stages {
                        stage('build-ui-client-op-generali-image') {
                            when {
                                anyOf {
                                    changeset 'grabbill-ui/*'
                                    changeset 'grabbill-ui/config/**'
                                    changeset 'grabbill-ui/projects/client/**'
                                    expression {
                                        params.SKIP_CHANGESET_OPTIMIZATION == true
                                    }
                                }
                            }
                            steps {
                                nodejs(nodeJSInstallationName: 'nodejs-20.13.1') {
                                    sh 'sk dev build-image --name grabbill/grabbill-client-generali-op --dockerFile grabbill-ui/client-op-prod-generali.Dockerfile'
                                }
                            }
                        }
                    }
                }
            }
        }
        stage('reset-build-info') {
            steps {
                script {
                    sh 'git reset --hard'
                }
            }
        }
        stage('push-docker-images') {
            when {
                expression {
                    params.SKIP_DOCKER_PUSH == false
                }
            }
            stages {
                stage('push-backend-images') {
                    when {
                        anyOf {
                            expression {
                                params.SKIP_CHANGESET_OPTIMIZATION == true
                            }
                            changeset 'grabbill-backend/**'
                        }
                    }
                    steps {
                        withDockerRegistry([credentialsId: 'kctang-docker.evos.tech', url: 'https://docker.evos.tech']) {
                            nodejs(nodeJSInstallationName: 'nodejs-20.13.1') {
                                sh 'sk dev push-image --name grabbill/grabbill-engine --registry docker.evos.tech --tag jenkins'
                                sh 'sk dev push-image --name grabbill/grabbill-server --registry docker.evos.tech --tag jenkins'
                            }
                        }
                    }
                }
                stage('push-ui-uat-images') {
                    when {
                        branch "${UAT_BRANCH}"
                    }
                    stages {
                        stage('push-ui-admin-uat-images') {
                            when {
                                anyOf {
                                    changeset 'grabbill-ui/*'
                                    changeset 'grabbill-ui/config/**'
                                    changeset 'grabbill-ui/projects/admin/**'
                                    expression {
                                        params.SKIP_CHANGESET_OPTIMIZATION == true
                                    }
                                }
                            }
                            steps {
                                withDockerRegistry([credentialsId: 'kctang-docker.evos.tech', url: 'https://docker.evos.tech']) {
                                    nodejs(nodeJSInstallationName: 'nodejs-20.13.1') {
                                        sh 'sk dev push-image --name grabbill/grabbill-admin-uat --registry docker.evos.tech --tag jenkins'
                                    }
                                }
                            }
                        }
                        stage('push-ui-client-uat-images') {
                            when {
                                anyOf {
                                    changeset 'grabbill-ui/*'
                                    changeset 'grabbill-ui/config/**'
                                    changeset 'grabbill-ui/projects/client/**'
                                    expression {
                                        params.SKIP_CHANGESET_OPTIMIZATION == true
                                    }
                                }
                            }
                            steps {
                                withDockerRegistry([credentialsId: 'kctang-docker.evos.tech', url: 'https://docker.evos.tech']) {
                                    nodejs(nodeJSInstallationName: 'nodejs-20.13.1') {
                                        sh 'sk dev push-image --name grabbill/grabbill-client-uat --registry docker.evos.tech --tag jenkins'
                                    }
                                }
                            }
                        }
                    }
                }
                stage('push-ui-prod-images') {
                    when {
                        branch "${PROD_BRANCH}"
                    }
                    stages {
                        stage('push-ui-admin-prod-images') {
                            when {
                                anyOf {
                                    changeset 'grabbill-ui/*'
                                    changeset 'grabbill-ui/config/**'
                                    changeset 'grabbill-ui/projects/admin/**'
                                    expression {
                                        params.SKIP_CHANGESET_OPTIMIZATION == true
                                    }
                                }
                            }
                            steps {
                                withDockerRegistry([credentialsId: 'kctang-docker.evos.tech', url: 'https://docker.evos.tech']) {
                                    nodejs(nodeJSInstallationName: 'nodejs-20.13.1') {
                                        sh 'sk dev push-image --name grabbill/grabbill-admin --registry docker.evos.tech --tag jenkins'
                                    }
                                }
                            }
                        }
                        stage('push-ui-client-prod-images') {
                            when {
                                anyOf {
                                    changeset 'grabbill-ui/*'
                                    changeset 'grabbill-ui/config/**'
                                    changeset 'grabbill-ui/projects/client/**'
                                    expression {
                                        params.SKIP_CHANGESET_OPTIMIZATION == true
                                    }
                                }
                            }
                            steps {
                                withDockerRegistry([credentialsId: 'kctang-docker.evos.tech', url: 'https://docker.evos.tech']) {
                                    nodejs(nodeJSInstallationName: 'nodejs-20.13.1') {
                                        sh 'sk dev push-image --name grabbill/grabbill-client --registry docker.evos.tech --tag jenkins'
                                    }
                                }
                            }
                        }
                    }
                }
                stage('push-ui-op-uat-images') {
                    when {
                        branch "${OP_UAT_BRANCH}"
                    }
                    stages {
                        stage('push-ui-client-op-uat-images') {
                            when {
                                anyOf {
                                    changeset 'grabbill-ui/*'
                                    changeset 'grabbill-ui/config/**'
                                    changeset 'grabbill-ui/projects/client/**'
                                    expression {
                                        params.SKIP_CHANGESET_OPTIMIZATION == true
                                    }
                                }
                            }
                            steps {
                                withDockerRegistry([credentialsId: 'kctang-docker.evos.tech', url: 'https://docker.evos.tech']) {
                                    nodejs(nodeJSInstallationName: 'nodejs-20.13.1') {
                                        sh 'sk dev push-image --name grabbill/grabbill-client-op-uat --registry docker.evos.tech --tag jenkins'
                                    }
                                }
                            }
                        }
                    }
                }
                stage('push-ui-op-prod-images') {
                    when {
                        branch "${OP_PROD_BRANCH}"
                    }
                    stages {
                        stage('push-ui-client-op-prod-images') {
                            when {
                                anyOf {
                                    changeset 'grabbill-ui/*'
                                    changeset 'grabbill-ui/config/**'
                                    changeset 'grabbill-ui/projects/client/**'
                                    expression {
                                        params.SKIP_CHANGESET_OPTIMIZATION == true
                                    }
                                }
                            }
                            steps {
                                withDockerRegistry([credentialsId: 'kctang-docker.evos.tech', url: 'https://docker.evos.tech']) {
                                    nodejs(nodeJSInstallationName: 'nodejs-20.13.1') {
                                        sh 'sk dev push-image --name grabbill/grabbill-client-op --registry docker.evos.tech --tag jenkins'
                                    }
                                }
                            }
                        }
                    }
                }
                stage('push-ui-op-generali-images') {
                    when {
                        branch "${OP_GENERALI_BRANCH}"
                    }
                    stages {
                        stage('push-ui-client-op-generali-images') {
                            when {
                                anyOf {
                                    changeset 'grabbill-ui/*'
                                    changeset 'grabbill-ui/config/**'
                                    changeset 'grabbill-ui/projects/client/**'
                                    expression {
                                        params.SKIP_CHANGESET_OPTIMIZATION == true
                                    }
                                }
                            }
                            steps {
                                withDockerRegistry([credentialsId: 'kctang-docker.evos.tech', url: 'https://docker.evos.tech']) {
                                    nodejs(nodeJSInstallationName: 'nodejs-20.13.1') {
                                        sh 'sk dev push-image --name grabbill/grabbill-client-generali-op --registry docker.evos.tech --tag jenkins'
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        stage('deploy') {
            when {
                expression {
                    params.SKIP_DEPLOY == false
                }
            }
            stages {
                stage('deploy-uat') {
                    when {
                        branch "${UAT_BRANCH}"
                    }
                    stages {
                        stage('deploy-uat-backend') {
                            when {
                                anyOf {
                                    expression {
                                        params.SKIP_CHANGESET_OPTIMIZATION == true
                                    }
                                    changeset 'grabbill-backend/**'
                                    changeset 'environments/uat/grabbill-backend/**'
                                }
                            }
                            steps {
                                slackSend(tokenCredentialId: SLACK_TOKEN_CRED_ID, teamDomain: SLACK_TEAM_DOMAIN, channel: "#git", botUser: true, color: "warning", message: "Redeploying UAT environment - backend")
                                withDockerRegistry([credentialsId: 'kctang-docker.evos.tech', url: 'https://docker.evos.tech']) {
                                    nodejs(nodeJSInstallationName: 'nodejs-20.13.1') {
                                        sh 'sk ops pull -e uat -g grabbill-backend'
                                        sh 'sk ops up -e uat -g grabbill-backend'
                                    }
                                }
                            }
                        }
                        stage('deploy-uat-frontend') {
                            when {
                                anyOf {
                                    expression {
                                        params.SKIP_CHANGESET_OPTIMIZATION == true
                                    }
                                    changeset 'grabbill-ui/**'
                                    changeset 'environments/uat/grabbill-ui/**'
                                }
                            }
                            steps {
                                slackSend(tokenCredentialId: SLACK_TOKEN_CRED_ID, teamDomain: SLACK_TEAM_DOMAIN, channel: "#git", botUser: true, color: "warning", message: "Redeploying UAT environment - frontend")
                                withDockerRegistry([credentialsId: 'kctang-docker.evos.tech', url: 'https://docker.evos.tech']) {
                                    nodejs(nodeJSInstallationName: 'nodejs-20.13.1') {
                                        sh 'sk ops pull -e uat -g grabbill-frontend'
                                        sh 'sk ops up -e uat -g grabbill-frontend'
                                    }
                                }
                            }
                        }
                    }
                }
                stage('deploy-prod') {
                    when {
                        branch "${PROD_BRANCH}"
                    }
                    stages {
                        stage('deploy-prod-backend') {
                            when {
                                anyOf {
                                    expression {
                                        params.SKIP_CHANGESET_OPTIMIZATION == true
                                    }
                                    changeset 'grabbill-backend/**'
                                    changeset 'environments/prod/grabbill-backend/**'
                                }
                            }
                            steps {
                                slackSend(tokenCredentialId: SLACK_TOKEN_CRED_ID, teamDomain: SLACK_TEAM_DOMAIN, channel: "#git", botUser: true, color: "warning", message: "Redeploying PROD environment - backend")
                                withDockerRegistry([credentialsId: 'kctang-docker.evos.tech', url: 'https://docker.evos.tech']) {
                                    nodejs(nodeJSInstallationName: 'nodejs-20.13.1') {
                                        sh 'sk ops pull -e prod -g grabbill-backend'
                                        sh 'sk ops up -e prod -g grabbill-backend'
                                    }
                                }
                            }
                        }
                        stage('deploy-prod-frontend') {
                            when {
                                anyOf {
                                    expression {
                                        params.SKIP_CHANGESET_OPTIMIZATION == true
                                    }
                                    changeset 'grabbill-ui/**'
                                    changeset 'environments/prod/grabbill-ui/**'
                                }
                            }
                            steps {
                                slackSend(tokenCredentialId: SLACK_TOKEN_CRED_ID, teamDomain: SLACK_TEAM_DOMAIN, channel: "#git", botUser: true, color: "warning", message: "Redeploying PROD environment - frontend")
                                withDockerRegistry([credentialsId: 'kctang-docker.evos.tech', url: 'https://docker.evos.tech']) {
                                    nodejs(nodeJSInstallationName: 'nodejs-20.13.1') {
                                        sh 'sk ops pull -e prod -g grabbill-frontend'
                                        sh 'sk ops up -e prod -g grabbill-frontend'
                                    }
                                }
                            }
                        }
                    }
                }
                stage('deploy-op-uat') {
                    when {
                        branch "${OP_UAT_BRANCH}"
                    }
                    stages {
                        stage('deploy-op-uat-backend') {
                            when {
                                anyOf {
                                    expression {
                                        params.SKIP_CHANGESET_OPTIMIZATION == true
                                    }
                                    changeset 'grabbill-backend/**'
                                    changeset 'environments/op-uat/backend/**'
                                }
                            }
                            steps {
                                slackSend(tokenCredentialId: SLACK_TOKEN_CRED_ID, teamDomain: SLACK_TEAM_DOMAIN, channel: "#git", botUser: true, color: "warning", message: "Redeploying OP UAT environment - backend")
                                withDockerRegistry([credentialsId: 'kctang-docker.evos.tech', url: 'https://docker.evos.tech']) {
                                    nodejs(nodeJSInstallationName: 'nodejs-20.13.1') {
                                        withEnv(["DO_TOKEN=${DO_OP_TOKEN}"]) {
                                            sh 'sk ops pull -e op-uat -g backend'
                                            sh 'sk ops up -e op-uat -g backend'
                                        }
                                    }
                                }
                            }
                        }
                        stage('deploy-op-uat-frontend') {
                            when {
                                anyOf {
                                    expression {
                                        params.SKIP_CHANGESET_OPTIMIZATION == true
                                    }
                                    changeset 'grabbill-ui/**'
                                    changeset 'environments/op-uat/frontend/**'
                                }
                            }
                            steps {
                                slackSend(tokenCredentialId: SLACK_TOKEN_CRED_ID, teamDomain: SLACK_TEAM_DOMAIN, channel: "#git", botUser: true, color: "warning", message: "Redeploying OP UAT environment - frontend")
                                withDockerRegistry([credentialsId: 'kctang-docker.evos.tech', url: 'https://docker.evos.tech']) {
                                    nodejs(nodeJSInstallationName: 'nodejs-20.13.1') {
                                        withEnv(["DO_TOKEN=${DO_OP_TOKEN}"]) {
                                            sh 'sk ops pull -e op-uat -g frontend'
                                            sh 'sk ops up -e op-uat -g frontend'
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                stage('deploy-op-prod') {
                    when {
                        branch "${OP_PROD_BRANCH}"
                    }
                    stages {
                        stage('deploy-op-prod-backend') {
                            when {
                                anyOf {
                                    expression {
                                        params.SKIP_CHANGESET_OPTIMIZATION == true
                                    }
                                    changeset 'grabbill-backend/**'
                                    changeset 'environments/op-prod/backend/**'
                                }
                            }
                            steps {
                                slackSend(tokenCredentialId: SLACK_TOKEN_CRED_ID, teamDomain: SLACK_TEAM_DOMAIN, channel: "#git", botUser: true, color: "warning", message: "Redeploying OP Production environment - backend")
                                withDockerRegistry([credentialsId: 'kctang-docker.evos.tech', url: 'https://docker.evos.tech']) {
                                    nodejs(nodeJSInstallationName: 'nodejs-20.13.1') {
                                        withEnv(["DO_TOKEN=${DO_OP_TOKEN}"]) {
                                            sh 'sk ops pull -e op-prod -g backend'
                                            sh 'sk ops up -e op-prod -g backend'
                                        }
                                    }
                                }
                            }
                        }
                        stage('deploy-op-prod-frontend') {
                            when {
                                anyOf {
                                    expression {
                                        params.SKIP_CHANGESET_OPTIMIZATION == true
                                    }
                                    changeset 'grabbill-ui/**'
                                    changeset 'environments/op-prod/frontend/**'
                                }
                            }
                            steps {
                                slackSend(tokenCredentialId: SLACK_TOKEN_CRED_ID, teamDomain: SLACK_TEAM_DOMAIN, channel: "#git", botUser: true, color: "warning", message: "Redeploying OP Production environment - frontend")
                                withDockerRegistry([credentialsId: 'kctang-docker.evos.tech', url: 'https://docker.evos.tech']) {
                                    nodejs(nodeJSInstallationName: 'nodejs-20.13.1') {
                                        withEnv(["DO_TOKEN=${DO_OP_TOKEN}"]) {
                                            sh 'sk ops pull -e op-prod -g frontend'
                                            sh 'sk ops up -e op-prod -g frontend'
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                stage('deploy-op-generali') {
                    when {
                        branch "${OP_GENERALI_BRANCH}"
                    }
                    stages {
                        stage('deploy-op-generali-backend') {
                            when {
                                anyOf {
                                    expression {
                                        params.SKIP_CHANGESET_OPTIMIZATION == true
                                    }
                                    changeset 'grabbill-backend/**'
                                    changeset 'environments/op-generali/backend/**'
                                }
                            }
                            steps {
                                slackSend(tokenCredentialId: SLACK_TOKEN_CRED_ID, teamDomain: SLACK_TEAM_DOMAIN, channel: "#git", botUser: true, color: "warning", message: "Redeploying OP Generali environment - backend")
                                withDockerRegistry([credentialsId: 'kctang-docker.evos.tech', url: 'https://docker.evos.tech']) {
                                    nodejs(nodeJSInstallationName: 'nodejs-20.13.1') {
                                        withEnv(["DO_TOKEN=${DO_OP_TOKEN}"]) {
                                            sh 'sk ops pull -e op-generali -g backend'
                                            sh 'sk ops up -e op-generali -g backend'
                                        }
                                    }
                                }
                            }
                        }
                        stage('deploy-op-generali-frontend') {
                            when {
                                anyOf {
                                    expression {
                                        params.SKIP_CHANGESET_OPTIMIZATION == true
                                    }
                                    changeset 'grabbill-ui/**'
                                    changeset 'environments/op-generali/frontend/**'
                                }
                            }
                            steps {
                                slackSend(tokenCredentialId: SLACK_TOKEN_CRED_ID, teamDomain: SLACK_TEAM_DOMAIN, channel: "#git", botUser: true, color: "warning", message: "Redeploying OP Generali environment - frontend")
                                withDockerRegistry([credentialsId: 'kctang-docker.evos.tech', url: 'https://docker.evos.tech']) {
                                    nodejs(nodeJSInstallationName: 'nodejs-20.13.1') {
                                        withEnv(["DO_TOKEN=${DO_OP_TOKEN}"]) {
                                            sh 'sk ops pull -e op-generali -g frontend'
                                            sh 'sk ops up -e op-generali -g frontend'
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
    post {
        success { slackSend(tokenCredentialId: SLACK_TOKEN_CRED_ID, teamDomain: SLACK_TEAM_DOMAIN, channel: "#git", botUser: true, color: "good", message: "Success - ${env.JOB_NAME} ${env.BUILD_NUMBER} (<${env.BUILD_URL}|Open>)") }
        unstable { slackSend(tokenCredentialId: SLACK_TOKEN_CRED_ID, teamDomain: SLACK_TEAM_DOMAIN, channel: "#git", botUser: true, color: "warning", message: "Unstable - ${env.JOB_NAME} ${env.BUILD_NUMBER} (<${env.BUILD_URL}|Open>)") }
        failure { slackSend(tokenCredentialId: SLACK_TOKEN_CRED_ID, teamDomain: SLACK_TEAM_DOMAIN, channel: "#git", botUser: true, color: "danger", message: "Failure - ${env.JOB_NAME} ${env.BUILD_NUMBER} (<${env.BUILD_URL}|Open>)") }
        changed { slackSend(tokenCredentialId: SLACK_TOKEN_CRED_ID, teamDomain: SLACK_TEAM_DOMAIN, channel: "#git", botUser: true, color: "warning", message: "Changed - ${env.JOB_NAME} ${env.BUILD_NUMBER} (<${env.BUILD_URL}|Open>)") }
    }
}
