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

    def addFirstExistingOptional(List possiblePaths) {
        for (possiblePath in possiblePaths) {
            if (script.fileExists(possiblePath)) {
                files.add(possiblePath)
                return
            }
        }
    }

    def setValue(name, value) {
        params.put(name, value)
        return this
    }

    def findValue(jqPath, defaultValue = null) {
        def plainFiles = files.findAll {
            !it.endsWith(".sops.yaml")
        }

        def value = defaultValue

        for (file in plainFiles) {
            valueFromFile = script.sh(script: "yq -r '${jqPath}' ${file}", returnStdout:true)
            if (valueFromFile != "null") {
                value = valueFromFile
            }
        }

        return value
    }

    def buildParams(sopsImage, sopsUrl, gpgKeyFile = null) {
        def encryptedFiles = files.findAll {
            it.endsWith(".sops.yaml")
        }
        def decryptedFiles = [:]

        if (encryptedFiles) {
            script.docker.image(sopsImage).inside('--entrypoint=""') {
                if (gpgKeyFile != null) {
                    script.sh "gpg --import ${gpgKeyFile}"
                }
                for (def i = 0; i < encryptedFiles.size(); i++) {
                    def encryptedFile = encryptedFiles.getAt(i)
                    def decryptedFile = encryptedFile.replaceAll(".sops.yaml",".secret.yaml")
                    script.sh "sops --keyservice tcp://${sopsUrl} --verbose -d ${encryptedFile} > ${decryptedFile}"
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
