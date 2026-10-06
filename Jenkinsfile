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

        stage('Build and Push Docker Image') {
    steps {
        withCredentials([
            usernamePassword(
                credentialsId: 'dockerhub-credentials',
                usernameVariable: 'DOCKER_USERNAME',
                passwordVariable: 'DOCKER_TOKEN'
            )
        ]) {
            powershell '''
                $image = "goldy12/thesisflow:build-$env:BUILD_NUMBER"

                Write-Host "Building versioned image: $image"

                docker build -t $image .

                if ($LASTEXITCODE -ne 0) {
                    throw "Docker image build failed."
                }

                $env:DOCKER_TOKEN | docker login `
                    -u $env:DOCKER_USERNAME `
                    --password-stdin

                if ($LASTEXITCODE -ne 0) {
                    throw "Docker Hub login failed."
                }

                docker push $image

                if ($LASTEXITCODE -ne 0) {
                    throw "Docker Hub push failed."
                }

                Write-Host "Successfully pushed $image"
            '''
        }
    }
}
stage('Deploy with Docker') {
    steps {
        withCredentials([
            string(
                credentialsId: 'thesisflow-db-password',
                variable: 'DB_PASSWORD'
            )
        ]) {
            powershell '''
                $env:DOCKER_IMAGE = "goldy12/thesisflow:build-$env:BUILD_NUMBER"

                Write-Host "Deploying image: $env:DOCKER_IMAGE"

                .\\scripts\\deploy-docker.ps1
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