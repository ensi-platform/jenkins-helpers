#!/usr/bin/env groovy

def call(appImage, connectionParams, missedTopicsFile) {
    docker.image(appImage).inside('--entrypoint=""') {
        sh(script: """
        ${connectionParams}
        if [ -n "${missedTopicsFile}" ] && [ ! -f "${missedTopicsFile}" ]; then
                    touch "${missedTopicsFile}"
        fi
        cmd=\$(php /var/www/artisan list | grep 'kafka:find-not-created-topics')
        if [ "\${cmd}" != "" ]; then
            if [ -n "${missedTopicsFile}" ]; then
                php /var/www/artisan kafka:find-not-created-topics --file="${missedTopicsFile}"
                cat "${missedTopicsFile}"
            else
                php /var/www/artisan kafka:find-not-created-topics
            fi
        else
            echo 'Service does not have command kafka:find-not-created-topics'
        fi
        """, returnStatus: true)
    }
}
