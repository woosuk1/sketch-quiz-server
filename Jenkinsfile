pipeline {
  agent any

  environment {
    DOCKERHUB_CREDENTIALS = 'dockerhub-cred'
    IMAGE_NAME = 'visionn7111/sketch-quiz-server'
    SERVER_IP = '10.0.2.179' // 프라이빗 WAS 서버 IP
  }

  stages {

    stage('Clone') {
      steps {
        git url: 'https://github.com/itcen-project-2team/sketch-quiz-server', branch: 'main'
      }
    }

    stage('Build JAR') {
      steps {
        sh './gradlew clean build'
      }
    }

    stage('Copy JAR to WAS Server') {
      steps {
        sshagent(credentials: ['webserver-ssh-key']) {
          sh """
            ssh -o StrictHostKeyChecking=no ubuntu@$SERVER_IP 'mkdir -p ~/whiteboard-backend/build/libs'
            scp -o StrictHostKeyChecking=no build/libs/*.jar ubuntu@$SERVER_IP:~/whiteboard-backend/build/libs/
          """
        }
      }
    }

    stage('Deploy to WAS Server') {
      steps {
        sshagent(credentials: ['webserver-ssh-key']) {
          sh """
            ssh -o StrictHostKeyChecking=no ubuntu@$SERVER_IP '
              if [ ! -d ~/whiteboard-backend ]; then
                git clone https://github.com/itcen-project-2team/realtime-sharing-notebook-server-Test.git ~/whiteboard-backend
              fi

              cd ~/whiteboard-backend
              git pull

              if [ ! -f docker-compose.yml ]; then
                echo "docker-compose.yml 파일이 없습니다. 배포 중단." && exit 1
              fi

              docker-compose down || true
              docker-compose up -d --build
            '
          """
        }
      }
    }
  }
}
