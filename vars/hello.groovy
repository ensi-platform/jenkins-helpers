#!/usr/bin/env groovy

def hello() {
    echo "Start hello"

    new Options(script:this).run()

    return this
}