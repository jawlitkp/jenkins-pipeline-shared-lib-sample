def call(body) {

    // evaluate the body block, and collect configuration into the object
    def pipelineParams= [:]
    body.resolveStrategy = Closure.DELEGATE_FIRST
    body.delegate = pipelineParams
    body()

    pipeline {
        podTemplate(label: 'mypod', containers: [
            containerTemplate(name: 'docker', image: 'docker', ttyEnabled: true, command: 'cat'),
            containerTemplate(name: 'kubectl', image: 'lachlanevenson/k8s-kubectl:v1.8.0', command: 'cat', ttyEnabled: true),
            containerTemplate(name: 'helm', image: 'lachlanevenson/k8s-helm:latest', command: 'cat', ttyEnabled: true)
        ],
        volumes: [
            hostPathVolume(mountPath: '/var/run/docker.sock', hostPath: '/var/run/docker.sock'),
        ]) 
        node('mypod') {
            stage('do some Docker work') {
                container('docker') {                    
                        sh """
                            docker pull ubuntu
                            docker tag ubuntu ${env.DOCKER_HUB_USER}/ubuntu:${env.BUILD_NUMBER}
                            """
                        sh "docker login -u ${env.DOCKER_HUB_USER} -p ${env.DOCKER_HUB_PASSWORD} "
                        sh "docker push ${env.DOCKER_HUB_USER}/ubuntu:${env.BUILD_NUMBER} "
                }
            }
    }
}