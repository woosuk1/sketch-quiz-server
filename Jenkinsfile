pipeline {
    agent any
    options {
        timeout(time: 1, unit: 'HOURS')
    }

    environment {
        TIME_ZONE = 'Asia/Seoul'

        // GitHub
        GIT_TARGET_BRANCH = 'infra/cicd'
        GIT_REPOSITORY_URL = 'https://github.com/itcen-project-2team/sketch-quiz-server.git'
        GIT_CREDENTIALS_ID = 'jenkins-credential'

        // AWS ECR
        AWS_ECR_CREDENTIAL_ID = 'AWS_ECR_CREDENTIAL'
        AWS_REGION = 'ap-northeast-2'
        ECR_REPOSITORY = '010686621060.dkr.ecr.ap-northeast-2.amazonaws.com/2team/back-ecr'
        IMAGE_TAG = "${BUILD_NUMBER}"

        // Deployment target
        SERVER_IP = "${SERVER_IP}"

        // App environment variables (Jenkins에 등록한 변수들)
        DB_HOST = "${DB_HOST}"
        DB_USER = "${DB_USER}"
        DB_PASS = "${DB_PASS}"
    }

    stages {
        stage('Init') {
            steps {
                deleteDir()
            }
        }

        stage('Clone Source') {
            steps {
                git branch: "${GIT_TARGET_BRANCH}",
                    credentialsId: "${GIT_CREDENTIALS_ID}",
                    url: "${GIT_REPOSITORY_URL}"
            }
        }

        stage('Gradle Build') {
            steps {
                sh './gradlew clean build -x test'
            }
        }

        stage('Login to ECR') {
            steps {
                withCredentials([[$class: 'AmazonWebServicesCredentialsBinding', credentialsId: AWS_ECR_CREDENTIAL_ID]]) {
                    script {
                        def status = sh(
                            script: '''
                                set -eux
                                aws ecr get-login-password --region $AWS_REGION | docker login --username AWS --password-stdin $ECR_REPOSITORY
                            ''',
                            returnStatus: true
                        )
                        if (status != 0) {
                            error "ECR 로그인 실패, 상태 코드: ${status}"
                        }
                    }
                }
            }
        }

        stage('Build & Push Docker Image') {
            steps {
                sh """
                    docker build -t $ECR_REPOSITORY:$IMAGE_TAG .
                    docker push $ECR_REPOSITORY:$IMAGE_TAG
                """
            }
        }

        stage('Deploy with docker-compose') {
            steps {
                sshagent(credentials: ['webserver-ssh-key']) {
                    sh """
                        ssh -o StrictHostKeyChecking=no ubuntu@${SERVER_IP} 'mkdir -p ~/server-app'
                        scp -o StrictHostKeyChecking=no build/libs/*.jar docker-compose.yml ubuntu@${SERVER_IP}:~/server-app/
                        ssh -o StrictHostKeyChecking=no ubuntu@${SERVER_IP} '
                            cd ~/server-app
                            docker-compose down || true
                            IMAGE_TAG=$IMAGE_TAG DB_HOST=$DB_HOST DB_USER=$DB_USER DB_PASS=$DB_PASS docker-compose up -d --build
                        '
                    """
                }
            }
        }

        stage('Cleanup') {
            steps {
                sh "docker image prune -f --all"
            }
        }
    }

    post {
        success {
            echo 'Pipeline succeeded'
        }
        failure {
            echo 'Pipeline failed'
        }
    }
}
