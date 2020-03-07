def call(Map pipelineParams) {

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