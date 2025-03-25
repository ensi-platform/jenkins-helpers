#!/usr/bin/env groovy

def call(appImage, connectionParams, missedTopicsFile) {
    docker.image(appImage).inside('--entrypoint=""') {
        sh(script: """
        ${connectionParams}
        cmd=\$(php /var/www/artisan list | grep 'kafka:find-not-created-topics')
        if [ "\${cmd}" != "" ]; then
            php -d error_reporting="E_ALL & ~E_DEPRECATED" /var/www/artisan kafka:find-not-created-topics --file=${missedTopicsFile} > "script-output.txt"
            cat ${missedTopicsFile}
        else
            echo 'Service does not have command kafka:find-not-created-topics'
        fi
        """, returnStatus: true)
    }
}
