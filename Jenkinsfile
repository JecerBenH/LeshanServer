pipeline{
     environment {
        registry = "jecer1997/myrepo"
        registryCredential = 'docker_id'
        dockerImage = ''
    }
    agent any
    stages{

        stage('Clone Git Repo'){
            steps{
                echo 'pulling from git ... ';
                git branch:'master',
                url:'https://github.com/JecerBenH/LeshanServer.git';
            }
        }


        stage('Maven compile'){
            steps{
                echo 'Maven compile';
                sh "mvn compiler:compile";
                sh "mvn clean";            }
        }

        stage('Maven Package') {
            steps {
                sh 'mvn package'
            }
        }

        stage('Build Docker Image'){
            steps {
                script{
                    sh 'docker image build  -t jecer1997/leshan:latest .  '
                }
            }
        }

        stage('Run Docker Image'){
            steps {
                script{
                    sh 'docker run -d --rm --name LeshanServerContainer -p 5683:5683/udp -p 8081:8080/tcp jecer1997:latest  '
                }
            }
        }


    }
    }
