#!/usr/bin/env groovy

def call(kafkaToolsImage, bootstrapServer, kafkaLogin, kafkaPassword, topicSettingsFile, missedTopicsFile) {
    docker.image(kafkaToolsImage).inside('--entrypoint=""') {
        sh """
        /app/main.py --bootstrap-server=${kafkaHost} \
                     --kafka-login=${kafkaLogin} --kafka-password=${kafkaPassword} \
                     --topics-file=${topicSettingsFile} \
                     --topic-names-file=${missedTopicsFile}
        """
    }
}