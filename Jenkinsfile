pipeline {
  agent any

  environment {
    DOCKERHUB_CREDENTIALS = 'dockerhub-cred'
    IMAGE_NAME = 'visionn7111/whiteboard-server'
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
            # 1. 기존 프로젝트 디렉토리 삭제 후 생성
            ssh -o StrictHostKeyChecking=no ubuntu@$SERVER_IP '
              rm -rf ~/whiteboard-server
              mkdir -p ~/whiteboard-server
            '

            # 2. Jenkins 워크스페이스 전체 복사 (숨김 파일 포함)
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

              # 필수 파일 확인
              if [ ! -f docker-compose.yml ]; then
                echo "docker-compose.yml 파일이 없습니다. 배포 중단." && exit 1
              fi

              # 기존 mongodb 컨테이너 제거 (충돌 방지)
              docker rm -f mongodb || true

              # 기존 컨테이너 종료 및 재시작
              docker-compose down || true
              docker-compose up -d --build
            '
          """
        }
      }
    }
  }
}
