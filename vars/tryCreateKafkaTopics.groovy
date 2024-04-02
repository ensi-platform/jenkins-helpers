#!/usr/bin/env groovy

def call(kafkaToolsImage, bootstrapServer, kafkaLogin, kafkaPassword, topicSettingsFile, missedTopicsFile) {
    if (!fileExists(missedTopicsFile)) {
        echo "Debug: файл с отсутствующими топиками не найден"
        return
    }

    def fileContent = readFile(missedTopicsFile)

    if (fileContent.length() < 1) {
        echo "Debug: файл с отсутствующими топиками пуст"
        return
    }

    docker.image(kafkaToolsImage).inside('--entrypoint=""') {
        sh """
        /app/main.py --bootstrap-server=${bootstrapServer} \
                     --kafka-login=${kafkaLogin} --kafka-password=${kafkaPassword} \
                     --topics-file=${topicSettingsFile} \
                     --topic-names-file=${missedTopicsFile}
        """
    }
}