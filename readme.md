# Jenkins shared lib #

Библиотека для Jenkins scripted pipelines.

## Установка ##

Перед использованием библиотеки её необходимо зарегистрировать в дженинсе.  
Для этого переходим в раздел *Настроить Jenkins > Конфигурация системы > Global Pipeline Libraries*
и добавляем там библиотеку.  
Имя, которое вы дадите библиотеке при её подключении будет использоваться при подключении библиотеки в коде пайплайна.

## Подключение библиотеки в пайплайне ##

Сначала необходимо подключить библиотеку
```groovy
@Library('ru.greensight')_
```

После этого вам станут доступны функции и классы, определённые в библиотеке.  
Классы необходимо дополнительно импортировать:
```groovy
import example.HelmParams
import example.Options
```

Функции, которые определены в папке vars явного подлючения не требуют.

## Доступные возможности ##

### cloneToFolder( folderName, repoUrl, branch, credentialsId ) ###

Клонировать репозиторий в указанную папку.  
Пример использования:
```groovy
cloneToFolder("src", "https://gitlab.com/path/to/project.git", "master", "gitlab-credentials-id")
```

### imageExistsInRegistry( registryCredsId, registryAddress, imageName, imageTag ) ###

Проверить наличие docker образа в реджистри.  
Пример использования:
```groovy
mageExistsInRegistry("registry-credentials-id", "https://hub.docker.com", "bitnami/php", "8.0-alpine")
```

### withPostgresDB( psqlImage, host, username, password, dbName, body ) ###

Создать временную БД на указанном сервере postgres, а после того как тело функции будет выполенно, удалить её.  
Пример использования:
```groovy
withPostgresDB("bitnami/psql", "127.0.0.1", "postgres", "example", "autotest_task_12345") {
    sh """
        export DB_NAME=autotest_task_12345
        composer test
    """
}
```

### ru.greensight.Options

Класс для получения опций из:

- ConfigFiles со списоком переменных
- Глобальных env переменных дженкинса
- Глобальных env переменных дженкинса с учётом текущей папки джоба

Jenkins ConfigFiles это плагин для дженинса, который позволяет прикреплять к папке или джобу файлы, доступные внутри пайплайна.  
В нашем случае используется файл типа Custom, внутри которого заданы переменные по одной на строке, вот так:
```
BASE_IMAGE=bintami/php:8.0-cli
SITE_URL=https://example.org
LIST_VARIABLE=one,two,three
```
Такой файл можно прочитать только когда начал выпоняться один из stages пайплайна.
Если нужно использовать какую-то переменную до выполнения stages, можно воспользоватья env перменными.

С глобальными env переменными всё просто - их можно задать в настройках дженинса. Их можно получить прямо по имени.

Env переменные с учётом текущей папки, это тоже глобальные env переменные, но в их имени есть путь до текущего джоба,
который позволяет задавать переменную отдельно для каждой папки, а так же наследовать переменную от родительской папки.  
Допустим наш job находится в папке `prod/oms`. Мы хотим определить для него env переменную `STAGES` именно для этой папки.
Для этого нам нужно создать глобальную env переменную с именем `PROP_prod_oms_STAGES`.  
Здесь PROP это обязательный префикс таких переменных, prod_oms это путь до джоба с заменой слешей на подчёркивание.
Ну а STAGES это уже название переменной.

Такие переменные стоит использовать только тогда, когда параметр нам нужен вне конкретного stage, например при формировании parameters пайплайна.

Пример использования:
```groovy
import ru.greensight.Options

def options = new Options(script:this)

node {
    stage("Run") {
        options.loadConfigFile("env-folder")
        options.loadConfigFile("env-job") // если в этом файле будут уже загруженные переменные, они будут затёрты новыми значениями
        
        print(options.get("VARIABLE_FROM_FILE"))
        print(options.get("VARIABLE_FROM_ENV"))
        print(options.get("VARIABLE_FROM_NAMESPACED_ENV"))
        
        def stages = options.getAsList("LIST_VARIABLE")
        stages.each {
            print(it)
        }
    }
}
```

### ru.greensight.HelmParams ###

Этот класс позволяет
- найти первый сущетсвующий файл из списка и запомнить его
- расширофать sops файлы
- задать значения переменных для helm upgrade (--set=image=bitnami/php:8.0)
- сформировать строку параметров для helm, содержащую --values и --set параметры
  
Пример использования:
```groovy
import ru.greensight.HelmParams

def helm = new HelmParams(script:this)

node {
    stage("Run") {
        helm.addFirstExisting(["override.values.yaml", "master.values.yaml", "default.values.yaml"])
        helm.addFirstExisting(["master.sops.yaml"])
        helm.setValue("app.image", "bitnami/php:8.0")
        
        def helmParamsStr = helm.buildParams("greensight/sops:v1", "127.0.0.1:1234")
        // --values=override.values.yaml --values=master.secret.yaml --set=app.image=bitnami/php:8.0
    }
}
```

Обратите внимание на то, что вместо файла `master.sops.yaml` в итоге используется `master.secret.yaml`.
Файлы с названием оканчивающимся на `sops.yaml` автоматически расшифровываются и результат записывается 
в файлы с названием `secret.yaml`.