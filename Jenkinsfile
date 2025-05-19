pipeline {
  agent any

  environment {
    DOCKERHUB_CREDENTIALS = 'dockerhub-cred'
    IMAGE_NAME = 'visionn7111/sketch-quiz-server'
    SERVER_IP = '10.0.2.179' // 프라이빗 WAS 서버 IP
  }

  stages {

    stage('Clone Source') {
      steps {
        git url: 'https://github.com/itcen-project-2team/sketch-quiz-server', branch: 'main'
      }
    }

    stage('Build JAR') {
      steps {
        sh './gradlew clean build'
      }
    }

    stage('Copy Entire Project to WAS') {
      steps {
        sshagent(credentials: ['webserver-ssh-key']) {
          sh """
            ssh -o StrictHostKeyChecking=no ubuntu@$SERVER_IP 'rm -rf ~/sketch-quiz-server'
            scp -o StrictHostKeyChecking=no -r . ubuntu@$SERVER_IP:~/sketch-quiz-server
          """
        }
      }
    }

    stage('Deploy to WAS') {
      steps {
        sshagent(credentials: ['webserver-ssh-key']) {
          sh """
            ssh -o StrictHostKeyChecking=no ubuntu@$SERVER_IP '
              cd ~/sketch-quiz-server

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
