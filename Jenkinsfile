pipeline {
    agent any

    tools {
        maven 'Maven 3.10.0'
    }

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
                    '''

                    powershell '''
                        $ok = $false
                        for ($i = 1; $i -le 5; $i++) {
                            Write-Host "Docker login attempt $i..."
                            docker login -u $env:DOCKER_USERNAME -p $env:DOCKER_TOKEN
                            if ($LASTEXITCODE -eq 0) { $ok = $true; break }
                            Start-Sleep -Seconds 15
                        }
                        if (-not $ok) {
                            throw "Docker login failed after 5 attempts."
                        }
                    '''

                    powershell '''
                        $image = "goldy12/thesisflow:build-$env:BUILD_NUMBER"
                        $ok = $false
                        for ($i = 1; $i -le 5; $i++) {
                            Write-Host "Docker push attempt $i..."
                            docker push $image
                            if ($LASTEXITCODE -eq 0) { $ok = $true; break }
                            Start-Sleep -Seconds 15
                        }
                        if (-not $ok) {
                            throw "Docker Hub push failed after 5 attempts."
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