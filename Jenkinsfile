pipeline {
  agent any

  environment {
    DOCKERHUB_CREDENTIALS = 'dockerhub-cred'
    IMAGE_NAME = 'visionn7111/sketch-quiz-server'
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
            # 1. 디렉토리 제거 후 새로 생성
            ssh -o StrictHostKeyChecking=no ubuntu@$SERVER_IP '
              rm -rf ~/sketch-quiz-server
              mkdir -p ~/sketch-quiz-server
            '

            # 2. 프로젝트 전체 전송
            scp -o StrictHostKeyChecking=no -r Jenkinsfile README.md build build.gradle docker-compose.yml dockerfile gradle gradlew gradlew.bat run-mongodb.sh settings.gradle src .git .gitattributes .gitignore .gradle ubuntu@$SERVER_IP:~/sketch-quiz-server
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
