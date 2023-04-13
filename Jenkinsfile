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
                  	          sh 'docker image build  -t jecer1997:latest .  '
                           }
                     }
        }

        stage('Run Docker Image'){
                             steps {
                                   script{
                          	          sh 'docker run jecer1997:latest'
                                   }
                             }
                }


    }
    }
