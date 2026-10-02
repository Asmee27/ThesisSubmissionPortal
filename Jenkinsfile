pipeline {
    agent any

    stages {

        stage('Checkout') {
            steps {
                checkout scm
            }
        }

        stage('Build and Test') {
            steps {
                bat 'mvn clean test'
            }
        }

        stage('Package') {
            steps {
                bat 'mvn package -DskipTests'
            }
        }

        stage('Deploy') {
            steps {
                withCredentials([
                    string(
                        credentialsId: 'thesisflow-db-password',
                        variable: 'DB_PASSWORD'
                    )
                ]) {
                    powershell '''
                        .\\scripts\\deploy.ps1
                    '''
                }
            }
        }
    }

    post {
        success {
            echo 'ThesisFlow CI/CD pipeline completed successfully.'
        }

        failure {
            echo 'ThesisFlow pipeline failed.'
        }
    }
}