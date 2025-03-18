#!/usr/bin/env groovy

def call(appImage, connectionParams, missedTopicsFile) {
    docker.image(appImage).inside('--entrypoint=""') {
        sh(script: """
        ${connectionParams}
        cmd=\$(php /var/www/artisan list | grep 'kafka:find-not-created-topics')
        if [ "\${cmd}" != "" ]; then
            if [ -n "${missedTopicsFile}" ]; then
                php /var/www/artisan kafka:find-not-created-topics --file=${missedTopicsFile}
                cat ${missedTopicsFile}
            else
                php /var/www/artisan kafka:find-not-created-topics
            fi
        else
            echo 'Сервис не имеет команды kafka:find-not-created-topics'
        fi
        """, returnStatus: true)
    }
}
