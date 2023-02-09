#!/usr/bin/env groovy

def call(postgresImage, database, body) {
    try {
        sh(script: """docker run -d -e POSTGRES_HOST_AUTH_METHOD=trust -e POSTGRES_DB=${database} --name \$BUILD_TAG ${postgresImage}""")
        sh(script: "sleep 5")
        def db_ip = sh(returnStdout: true, script: "docker inspect --format='{{.NetworkSettings.Networks.bridge.IPAddress}}' \$BUILD_TAG").trim()

        body(db_ip)
    } finally {
        sh(script: "docker rm -f -v \$BUILD_TAG")
    }
}