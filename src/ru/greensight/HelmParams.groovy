package ru.greensight

class HelmParams {
    Script script
    public files = []
    public params = [:]

    def addFirstExisting(List possiblePaths) {
        for (possiblePath in possiblePaths) {
            if (script.fileExists(possiblePath)) {
                files.add(possiblePath)
                return
            }
        }
        script.error("Values file is not found in all possible places")
    }

    def setValue(name, value) {
        params.put(name, value)
        return this
    }

    def buildParams(sopsImage, sopsUrl) {
        def encryptedFiles = files.findAll {
            it.endsWith(".sops.yaml")
        }
        def decryptedFiles = [:]

        if (encryptedFiles) {
            script.docker.image(sopsImage).inside('--entrypoint=""') {
                for (def i = 0; i < encryptedFiles.size(); i++) {
                    def encryptedFile = encryptedFiles.getAt(i)
                    def decryptedFile = encryptedFile.replaceAll(".sops.yaml",".secret.yaml")
                    script.sh "sops --enable-local-keyservice=false --keyservice tcp://${sopsUrl} --verbose -d ${encryptedFile} > ${decryptedFile}"
                    decryptedFiles.put(encryptedFile, decryptedFile)
                }
            }
        }

        def fileOptions = files.collect({
            if (decryptedFiles.containsKey(it)) {
                return "--values=${decryptedFiles.get(it)}"
            } else {
                return "--values=${it}"
            }
        }).join(" ")

        def setOptions = params.entrySet().collect({entry ->
            return "--set=${entry.key}=${entry.value}"
        }).join(" ")

        return "${fileOptions} ${setOptions}"
    }
}
