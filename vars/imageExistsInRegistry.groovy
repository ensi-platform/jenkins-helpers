#!/usr/bin/env groovy

def call(registryCredsId, registryAddress, imageName, imageTag) {
    def result = false
    try {
        withCredentials([usernamePassword(credentialsId: registryCredsId, usernameVariable: 'username', passwordVariable: 'password')]) {
            def statusCode = sh(returnStdout:true, script: """
                set +x
                curl -s -k \
                    -u '${username}:${password}' \
                    '${registryAddress}/service/token?service=harbor-registry&scope=repository:${imageName}:pull' | \
                    jq -r '.token' > harbor-token.txt
                curl -s -k \
                    -H 'Content-Type: application/json' \
                    -H "Authorization:  Bearer \$(cat harbor-token.txt)" \
                    -X GET \
                    -o /dev/null -w '%{http_code}' \
                    '${registryAddress}/v2/${imageName}/manifests/${imageTag}'
            """).trim()

            result = ("200" == statusCode)
        }
    } catch (Exception e) {}

    if (result) {
        echo "Debug: образ в registry существует"
    } else {
        echo "Debug: образ в registry не наден"
    }

    return result
}