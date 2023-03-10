#!/usr/bin/env groovy

def call(appImage, connectionParams, missedTopicsFile) {
    docker.image(appImage).inside('--entrypoint=""') {
        sh """
        ${connectionParams}
        php /var/www/artisan kafka:find-not-created-topics > ${missedTopicsFile}
        """
    }
}