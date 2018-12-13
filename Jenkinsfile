pipeline {
    agent any

    tools {
        maven 'M3.3.9'
    }

    triggers {
        pollSCM('H * * * *')
    }

    stages {
        stage('build') {
            steps {
                withMaven(options: [junitPublisher(ignoreAttachments: false), artifactsPublisher()]) {
//                    TODO remove -DskipTests when java problem is solved
                    sh 'mvn -DskipTests -DincludeSrcJavadocs clean source:jar install'
                }
            }
        }

        stage('Docker Build on DockerHub') {
            when {
                branch 'master'
            }
            steps {
                sh 'curl --data "build=true" -X POST https://registry.hub.docker.com/u/dmadk/ais-track/trigger/d73b4561-f94f-4279-aec9-b8a2ed7c238d/'
            }
        }

    }


    post {
        failure {
            // notify users when the Pipeline fails
            mail to: 'steen@lundogbendsen.dk',
                    subject: "Failed Pipeline: ${currentBuild.fullDisplayName}",
                    body: "Something is wrong with ${env.BUILD_URL}"
        }
    }
}

