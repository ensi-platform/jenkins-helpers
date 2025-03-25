#!/usr/bin/env groovy

def call(appImage, connectionParams, missedTopicsFile) {
    docker.image(appImage).inside('--entrypoint=""') {
        sh(script: """
        ${connectionParams}
        cmd=\$(php /var/www/artisan list | grep 'kafka:find-not-created-topics')
        if [ "\${cmd}" != "" ]; then
            php /var/www/artisan kafka:find-not-created-topics --file=${missedTopicsFile}
            cat ${missedTopicsFile}
        else
            echo 'Service does not have command kafka:find-not-created-topics'
        fi
        """, returnStatus: true)
    }
}
