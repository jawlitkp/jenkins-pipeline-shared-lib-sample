#!/usr/bin/env groovy

def call(body) {
    def config = [:]
    body.resolveStrategy = Closure.DELEGATE_FIRST
    body.delegate = config
    body()

    echo config.name
    echo "Param1 is: ${env.param1}"
    echo "Param2 is: ${env.param2}"
    if (env.param1 == 'One default') {
        echo "Param1 is default"
    }
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
    }
}
