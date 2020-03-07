def call(body) {

    // evaluate the body block, and collect configuration into the object
    def pipelineParams= [:]
    body.resolveStrategy = Closure.DELEGATE_FIRST
    body.delegate = pipelineParams
    body()
    
pipeline {
podTemplate(yaml: """
kind: Pod
spec:
  containers:
  - name: maven
    image: us.gcr.io/sharedinfra-svc-corp00/devopsjenkins/devops-jenkins-slave-maven:0.0.5
    command:
    - cat
    tty: true
  - name: kaniko
    image: us.gcr.io/sharedinfra-svc-corp00/devopsjenkins/kaniko-executor:debug
    imagePullPolicy: Always
    command:
    - /busybox/cat
    tty: true
    volumeMounts:
      - name: jenkinsgcp
        mountPath: /var/run/secrets/jenkinsgcp
    env:
      - name: GOOGLE_APPLICATION_CREDENTIALS
        value: /var/run/secrets/jenkinsgcp/credentials.json
  volumes:
    - name: jenkinsgcp
      secret:
        secretName: jenkinsgcp
"""
  ) {

  node(POD_LABEL) {

    stage ('Checkout Code') {
        git credentialsId: 'a6086650-deb3-4b91-85b4-d442f88300e7', branch: 'master', poll: false, url: 'https://github.bedbath.com/bbbydevopscicd/docker-hello-world-spring-boot.git'
    }

    stage('Build Project') {
      container('maven') {
        sh "'mvn' -Dmaven.test.failure.ignore clean package"
      }
    }

    stage('Build with Kaniko') {
      container('kaniko') {
        sh '/kaniko/executor -c `pwd` --cache=true --destination=us.gcr.io/sharedinfra-svc-corp00/devopsjenkins/docker-hello-world-spring-boot'
      }
    }
  }
}
}