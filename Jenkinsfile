pipeline {
  agent any

  environment {
    SERVER_IP = '10.0.2.179'
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
            ssh -o StrictHostKeyChecking=no ubuntu@$SERVER_IP '
              rm -rf ~/whiteboard-server
              mkdir -p ~/whiteboard-server
            '

            scp -o StrictHostKeyChecking=no -r * .[^.]* ubuntu@$SERVER_IP:~/whiteboard-server
          """
        }
      }
    }

    stage('Deploy to WAS') {
      steps {
        sshagent(credentials: ['webserver-ssh-key']) {
          sh """
            ssh -o StrictHostKeyChecking=no ubuntu@$SERVER_IP '
              cd ~/whiteboard-server

              if [ ! -f docker-compose.yml ]; then
                echo "docker-compose.yml 파일이 없습니다. 배포 중단." && exit 1
              fi

              docker-compose down || true
              docker-compose up -d --build
            '
          """
        }
      } //1234
    }
  }
}
