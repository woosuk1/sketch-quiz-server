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
        ECR_REGISTRY = '010686621060.dkr.ecr.ap-northeast-2.amazonaws.com'
        ECR_REPOSITORY = "${ECR_REGISTRY}/2team/back-ecr"
        IMAGE_TAG = "${BUILD_NUMBER}"

        // Deployment target
        SERVER_IP = "${SERVER_IP}"

        // Mongo environment variables (필요 시 추가)
        MONGO_INITDB_ROOT_USERNAME = credentials('mongo-root-username')
        MONGO_INITDB_ROOT_PASSWORD = credentials('mongo-root-password')
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
                                aws ecr get-login-password --region $AWS_REGION | docker login --username AWS --password-stdin $ECR_REGISTRY
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
                    scp -o StrictHostKeyChecking=no docker-compose.yml ubuntu@${SERVER_IP}:~/server-app/
                    ssh -o StrictHostKeyChecking=no ubuntu@${SERVER_IP} "
                        export MONGO_INITDB_ROOT_USERNAME=${MONGO_INITDB_ROOT_USERNAME} &&
                        export MONGO_INITDB_ROOT_PASSWORD=${MONGO_INITDB_ROOT_PASSWORD} &&
                        cd ~/server-app &&
                        docker-compose down || true &&
                        IMAGE_TAG=${IMAGE_TAG} docker-compose up -d --build
                    "
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
