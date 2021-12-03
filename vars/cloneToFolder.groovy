#!/usr/bin/env groovy

def call(folderName, repoUrl, branch, credentialsId) {
    if (!fileExists(folderName)){
        new File(folderName).mkdir()
    }
    dir (folderName) {
        git([url: repoUrl, branch: branch, credentialsId: credentialsId])
    }
}
