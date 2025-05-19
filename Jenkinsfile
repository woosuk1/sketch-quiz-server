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
            # WAS 서버에서 기존 프로젝트 삭제 및 새 폴더 생성
            ssh -o StrictHostKeyChecking=no ubuntu@$SERVER_IP '
              rm -rf ~/sketch-quiz-server
              mkdir -p ~/sketch-quiz-server
            '

            # 현재 Jenkins 작업 디렉토리 전체 복사 (숨김파일 포함)
            scp -o StrictHostKeyChecking=no -r * .[^.]* ubuntu@$SERVER_IP:~/sketch-quiz-server
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

              # docker-compose.yml 존재 확인
              if [ ! -f docker-compose.yml ]; then
                echo "docker-compose.yml 파일이 없습니다. 배포 중단." && exit 1
              fi

              # 충돌 방지: 이미 존재하는 mongodb 컨테이너 강제 제거
              docker rm -f mongodb || true

              # 기존 컨테이너 및 네트워크 정리 후 재배포
              docker-compose down || true
              docker-compose up -d --build
            '
          """
        }
      }
    }
  }
}
