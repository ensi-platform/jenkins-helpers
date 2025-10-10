#!/usr/bin/env groovy

def call(appImage, connectionParams, missedTopicsFile) {
    docker.image(appImage).inside('--entrypoint=""') {
        sh(script: """
        ${connectionParams}
        cmd=\$(php /var/www/artisan list | grep 'kafka:find-not-created-topics')
        if [ "\${cmd}" != "" ]; then
            if php /var/www/artisan help kafka:find-not-created-topics 2>/dev/null | grep -q "\-\-file"; then
                php /var/www/artisan kafka:find-not-created-topics --file=${missedTopicsFile}
            else
                php /var/www/artisan kafka:find-not-created-topics > ${missedTopicsFile}
            fi
            cat ${missedTopicsFile}
        else
            echo 'Service does not have command kafka:find-not-created-topics'
        fi
        """, returnStatus: true)
    }
}
