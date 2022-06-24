//file:noinspection GroovyAssignabilityCheck
package ru.greensight

class Options {
    Script script
    public vars = [:]

    def get(name) {
        def result

        result = getNamespacedEnv(name)
        if (result != null) {
            return result
        }

        result = script.env[name]
        if (result != null) {
            return result
        }

        if (name in vars) {
            return vars[name]
        }
        return result
    }

    def getAsList(name) {
        def valueStr = get(name)
        if (valueStr instanceof String || valueStr instanceof GString) {
            return valueStr.split(',').collect({it.trim()})
        }
        return []
    }

    def getNamespacedEnv(name) {
        def pathParts = script.env["JOB_NAME"].split("/") as List
        while (pathParts.size() > 0) {
            def path = pathParts.join("_")
            def key = "PROP_${path}_${name}"
            def value = script.env[key]
            if (value) {
                return value
            } else {
                pathParts.remove(pathParts.size() - 1)
            }
        }

        return null
    }

    def loadConfigFile(configFileCode) {
        try {
            script.configFileProvider([script.configFile(fileId: configFileCode, targetLocation: "./${configFileCode}.txt")]) {
                def propsFromFile = script.readProperties(file: "./${configFileCode}.txt")
                for (prop in propsFromFile) {
                    vars."${prop.key}" = "${prop.value}"
                }
            }
        } catch (Exception e) {}
    }

    def checkDefined(requiredKeys) {
        def missingKeys = []
        for (key in requiredKeys) {
            if (!vars.containsKey(key)) {
                missingKeys.add(key)
            }
        }
        if (missingKeys.size() > 0) {
            def missingKeysStr = missingKeys.join(", ")
            script.error("These variables are undefined: ${missingKeysStr}")
        }
    }

}