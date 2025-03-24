#!/usr/bin/env groovy

def call(appImage, connectionParams, missedTopicsFile) {
    docker.image(appImage).inside('--entrypoint=""') {
        sh(script: """
        ${connectionParams}
        cmd=\$(php /var/www/artisan list | grep 'kafka:find-not-created-topics')
        if [ "\${cmd}" != "" ]; then
            if [ -n "${missedTopicsFile}" ]; then
                if [ ! -f "${missedTopicsFile}" ]; then
                    touch "${missedTopicsFile}"
                fi
                php /var/www/artisan kafka:find-not-created-topics --file="${missedTopicsFile}" > "path_to_missed_topics.txt"
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
