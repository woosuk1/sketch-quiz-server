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
        AWS_ECR_CREDENTIAL_ID = 'AWS_ECR_CREDENTIAL'  // 반드시 'AWS Credentials' 타입으로 Jenkins에 등록돼 있어야 함
        AWS_REGION = 'ap-northeast-2'
        ECR_REGISTRY = '010686621060.dkr.ecr.ap-northeast-2.amazonaws.com'
        ECR_REPOSITORY = "${ECR_REGISTRY}/2team/back-ecr"
        IMAGE_TAG = "${BUILD_NUMBER}"

        // Deployment target
        SERVER_IP = "${SERVER_IP}"
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

        stage('Login to ECR') {
            steps {
                withAWS(credentials: "${AWS_ECR_CREDENTIAL_ID}", region: "${AWS_REGION}") {
                    sh '''
                        aws ecr get-login-password --region $AWS_REGION | docker login --username AWS --password-stdin $ECR_REGISTRY
                    '''
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
                    scp -o StrictHostKeyChecking=no docker-compose.yml ubuntu@${SERVER_IP}:~/server-app/
                    ssh -o StrictHostKeyChecking=no ubuntu@${SERVER_IP} '
                        cd ~/server-app
                        docker-compose down || true
                        IMAGE_TAG=$IMAGE_TAG docker-compose up -d --build
                    '
                    """
                }
            }
        }
    }
}
