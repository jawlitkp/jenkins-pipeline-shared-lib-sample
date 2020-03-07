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
  ) 
        stage('do some Docker work') {

        }
}
}
