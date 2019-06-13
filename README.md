# Задание 3
## Условия задачи
Нужно спроектировать и реализовать бэкенд (REST API) для сервиса аналогичного pastebin.com — сервис позволяет заливать куски текста/кода ("пасту") и получать на них короткую ссылку, которую можно отправить другим людям.

При загрузке "пасты" пользователь указывает:

Срок в течение которого "паста" будет доступна по ссылке (expiration time) 10 мин., 1 час, 3 часа, 1 день, 1 неделя, 1 месяц, без ограничения после окончания срока получить доступ к "пасте" нельзя, в том числе и автору

Ограничение доступа:

public — доступна всем

unlisted — доступна только по ссылке

## Зависимости
Требуется redis-server, он используется как база данных для данного проекта.

## Запуск
```bash
git clone https://github.com/DiamondDen/THIS_Incubator_task3.git
cd ./THIS_Incubator_task3
mvn install
cd ./target
```
Ниже указаны данные по умолчанию
```bash
export redis_host=127.0.0.1
export redis_port=6379

java -jar ./THIS_Incubator_task3-0.0.1-SNAPSHOT.jar
```
Так же можно указать параметр noNeedToCompressData, который отключить сжатие данных
```bash
java -DnoNeedToCompressData=true -jar ...
```

WebServer запускается по умолчанию на порту 8080.

## Новые пасты
Добавить новую пасту
```bash
curl -X POST "http://127.0.0.1:8080/document" -d "Hi world! It is PUBLIC!" -H "Content-Type: application/plaintext"
```
Ответ:
```json
{"response":{"type":"PUBLIC","key":"oiU8W6BU"}}
```
Это мы добавили публичную пасту, теперь добавим только по ссылке
```bash
curl -X POST "http://127.0.0.1:8080/document?typeAccess=1" -d "Hi world! It is UNLISTED!" -H "Content-Type: application/json"
```
Ответ:
```json
{"response":{"type":"UNLISTED","key":"9WMx7Pwf5laXe20pvjdHqJ2Q"}}
```
у **typeAccess** есть два состояния:

    1. PUBLIC = 0 (default)
    2. UNLISTED = 1
  
Так же есть и **expiry**
  
    1. Навсегда = -1 (default)
    2. 10 минут =  0
    3. 1 день   =  1
    4. 7 дней   =  2
    5. 14 дней  =  3
    6. 31 день  =  4
  
Пример PUBLIC пасты на 10 минут
```bash
curl -X POST "http://127.0.0.1:8080/document?typeAccess=0&expiry=0" -d "Hi world! It is PUBLIC!" -H "Content-Type: application/plaintext"
```
## Получить пасты
Получить PUBLIC пасту по ключу
```bash
curl http://127.0.0.1:8080/pub/oiU8W6BU
```
Ответ:

```json
{"response":{"data":"Hi world! It is PUBLIC!"}}
```
Получить UNLISTED пасту по ключу
```bash
curl http://127.0.0.1:8080/unl/9WMx7Pwf5laXe20pvjdHqJ2Q
```
Ответ:
```json
{"response":{"data":"Hi world! It is UNLISTED!"}}
```
Список последних 10 публичных паст
```bash
curl http://127.0.0.1:8080/list
```
В случаи нахождение бага, просьба написать в telegram - @diamondden
