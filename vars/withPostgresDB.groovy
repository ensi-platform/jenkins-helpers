#!/usr/bin/env groovy

def dropDatabasesByPrefix(host, username, password, dbName, port = "5432") {
    def dropDbScript = [
            """export PGPASSWORD=${password}""",
            """databases=\$(psql --username=${username} --dbname=postgres --host=${host} --port=${port} -t --csv --command '\\l' | grep ${dbName} | awk 'BEGIN {FS=","}; {print \$1}') """,
            """for i in \$databases; do """,
            """    psql --username=${username} --dbname=postgres --host=${host} --port=${port} --command="DROP DATABASE IF EXISTS \$i;" """,
            """done"""
    ].join("\n")

    sh(script: dropDbScript)
}

def createDatabase(host, username, password, dbName, port = "5432") {
    def createDbScript = [
            """export PGPASSWORD=${password}""",
            """for i in 1 2 3 4 5 6 7 8 9 10; do""",
            """    connections=\$(psql --username=${username} --dbname=postgres --host=${host} --port=${port} -t --command=\"SELECT COUNT(*) FROM pg_stat_activity WHERE datname = 'ensi_tpl_v1' AND pid <> pg_backend_pid();\")""",
            """    if [ \"\$connections\" -eq \"0\" ]; then""",
            """        break""",
            """    fi""",
            """    echo \"Waiting for template database to be free... (\$i/10)\" """,
            """    sleep 2""",
            """done""",
            """psql --username=${username} --dbname=postgres --host=${host} --port=${port} --command=\"SELECT pg_terminate_backend(pid) FROM pg_stat_activity WHERE datname = 'ensi_tpl_v1' AND pid <> pg_backend_pid();\" """,
            """psql --username=${username} --dbname=postgres --host=${host} --port=${port} --command='CREATE DATABASE ${dbName} TEMPLATE ensi_tpl_v1;'"""
    ].join("\n")

    sh(script: createDbScript)
}

def call(psqlImage, host, username, password, dbName, port = "5432", body) {
    docker.image(psqlImage).inside('--entrypoint=""') {
        dropDatabasesByPrefix(host, username, password, dbName, port )
        createDatabase(host, username, password, dbName, port)
    }

    body()

    docker.image(psqlImage).inside('--entrypoint=""') {
        dropDatabasesByPrefix(host, username, password, dbName, port)
    }
}
