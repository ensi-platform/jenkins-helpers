#!/usr/bin/env groovy

def dropDatabasesByPrefix(host, username, password, dbName) {
    def dropDbScript = [
            """databases=\$(mysql -u ${username} -p'${password}' -h ${host} -e 'show databases;' | grep ${dbName}) """,
            """for i in \$databases; do """,
            """    mysql -u ${username} -p'${password}' -h ${host} -e "drop database \$i;" """,
            """done"""
    ].join("\n")

    sh(script: dropDbScript, returnStatus: true)
}

def createDatabase(host, username, password, dbName) {
    def createDbScript = [
            """mysql -u ${username} -p'${password}' -h ${host} -e 'CREATE DATABASE ${dbName};'"""
    ].join("\n")

    sh(script: createDbScript)
}

def call(mysqlImage, host, username, password, dbName, body) {
    docker.image(mysqlImage).inside('--entrypoint=""') {
        dropDatabasesByPrefix(host, username, password, dbName)
        createDatabase(host, username, password, dbName)
    }

    body()

    docker.image(mysqlImage).inside('--entrypoint=""') {
        dropDatabasesByPrefix(host, username, password, dbName)
    }
}