#!/usr/bin/env groovy

def call(appImage, connectionParams, missedTopicsFile) {
    docker.image(appImage).inside('--entrypoint=""') {
        sh """
        ${connectionParams}
        cmd=\$(php /var/www/artisan list | grep 'kafka:find-not-created-topics')
        if [ "\${cmd}" != "" ]; then
            php /var/www/artisan kafka:find-not-created-topics > ${missedTopicsFile}
            cat ${missedTopicsFile}
        else
            echo 'Сервис не имеет команды kafka:find-not-created-topics'
        fi
        """
    }
}