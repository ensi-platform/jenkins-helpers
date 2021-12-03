#!/usr/bin/env groovy

def dropDatabasesByPrefix(host, username, password, dbName) {
    def dropDbScript = [
            """export PGPASSWORD=${password}""",
            """databases=\$(psql --username=${username} --dbname=postgres --host=${host} -t --csv --command '\\l' | grep ${dbName} | awk 'BEGIN {FS=","}; {print \$1}') """,
            """for i in \$databases; do """,
            """    psql --username=${username} --dbname=postgres --host=${host} --command="DROP DATABASE IF EXISTS \$i;" """,
            """done"""
    ].join("\n")

    sh(script: dropDbScript)
}

def createDatabase(host, username, password, dbName) {
    def createDbScript = [
            """export PGPASSWORD=${password}""",
            """psql --username=${username} --dbname=postgres --host=${host} --command='CREATE DATABASE ${dbName} template ensi_tpl_v1;'"""
    ].join("\n")

    sh(script: createDbScript)
}

def call(psqlImage, host, username, password, dbName, body) {
    docker.image(psqlImage).inside('--entrypoint=""') {
        dropDatabasesByPrefix(host, username, password, dbName)
        createDatabase(host, username, password, dbName)
    }

    body()

    docker.image(psqlImage).inside('--entrypoint=""') {
        dropDatabasesByPrefix(host, username, password, dbName)
    }
}