pipeline {
    agent any
    
    environment {
        MAVEN_VERSION = '3.9.9'
        MAVEN_HOME = "${WORKSPACE}/apache-maven-${MAVEN_VERSION}"
        PATH = "${MAVEN_HOME}/bin:${env.PATH}"
    }
    
    stages {
        stage('Checkout') {
            steps {
                checkout scm
            }
        }
        
        stage('Setup Maven') {
            steps {
                sh '''
                    if ! command -v mvn &> /dev/null; then
                        echo "Maven not found. Downloading Maven ${MAVEN_VERSION}..."
                        cd ${WORKSPACE}
                        if [ "$(uname)" = "Darwin" ]; then
                            # macOS
                            curl -L "https://archive.apache.org/dist/maven/maven-3/${MAVEN_VERSION}/binaries/apache-maven-${MAVEN_VERSION}-bin.tar.gz" -o maven.tar.gz
                        else
                            # Linux
                            wget "https://archive.apache.org/dist/maven/maven-3/${MAVEN_VERSION}/binaries/apache-maven-${MAVEN_VERSION}-bin.tar.gz" -O maven.tar.gz
                        fi
                        tar -xzf maven.tar.gz
                        rm maven.tar.gz
                        chmod +x ${MAVEN_HOME}/bin/mvn
                        echo "Maven installed at ${MAVEN_HOME}"
                    else
                        echo "Maven found in PATH"
                    fi
                    echo "Java version:"
                    java -version
                    echo "Maven version:"
                    mvn -version
                '''
            }
        }
        
        stage('Build') {
            steps {
                sh 'mvn clean compile -DskipTests'
            }
        }
        
        stage('Test') {
            steps {
                sh 'mvn test'
            }
            post {
                always {
                    junit '**/target/surefire-reports/*.xml'
                }
            }
        }
        
        stage('Package') {
            steps {
                sh 'mvn package -DskipTests'
            }
            post {
                success {
                    archiveArtifacts artifacts: '**/target/*.jar', fingerprint: true
                }
            }
        }
    }
    
    post {
        success {
            echo 'Pipeline completed successfully'
        }
        failure {
            echo 'Pipeline failed'
        }
    }
}
