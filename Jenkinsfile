pipeline {
    agent {
        docker { image 'docker:24-dind' }
    }

    environment {
        SERVER_IP = "${SERVER_IP}"  // 서버 IP 환경변수로 설정
        IMAGE_NAME = 'visionn7111/server-app'  // 서버용 이미지명
        IMAGE_TAG = "${BUILD_NUMBER}"
        ECR_REGISTRY = '010686621060.dkr.ecr.ap-northeast-2.amazonaws.com'
        ECR_REPOSITORY = '2team/server-app'
        AWS_REGION = 'ap-northeast-2'
    }

    stages {
        stage('Clone Source') {
            steps {
                git url: 'https://github.com/itcen-project-2team/server-app', branch: 'infra/cicd'
            }
        }

        stage('Login to ECR') {
            steps {
                withCredentials([usernamePassword(credentialsId: 'AWS_ECR_CREDENTIAL', usernameVariable: 'AWS_ACCESS_KEY_ID', passwordVariable: 'AWS_SECRET_ACCESS_KEY')]) {
                    sh '''
                        aws --region $AWS_REGION ecr get-login-password | docker login --username AWS --password-stdin $ECR_REGISTRY
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
