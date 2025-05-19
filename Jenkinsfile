pipeline {
  agent any

  environment {
    DOCKERHUB_CREDENTIALS = 'dockerhub-cred'
    IMAGE_NAME = 'visionn7111/sketch-quiz-server'
    SERVER_IP = '10.0.2.179' 
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
            ssh -o StrictHostKeyChecking=no ubuntu@$SERVER_IP 'mkdir -p ~/sketch-quiz-server/build/libs'
            scp -o StrictHostKeyChecking=no build/libs/*.jar ubuntu@$SERVER_IP:~/sketch-quiz-server/build/libs/
          """
        }
      }
    }

    stage('Deploy to WAS Server') {
      steps {
        sshagent(credentials: ['webserver-ssh-key']) {
          sh """
            ssh -o StrictHostKeyChecking=no ubuntu@$SERVER_IP '
              if [ ! -d ~/sketch-quiz-server/.git ]; then
                rm -rf ~/sketch-quiz-server
                git clone https://github.com/itcen-project-2team/sketch-quiz-server.git ~/sketch-quiz-server
              fi

              cd ~/sketch-quiz-server
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
