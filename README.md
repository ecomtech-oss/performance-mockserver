# Performance-Mockserver

## сервис-заглушка на базе [mockserver](https://www.mock-server.com/)

### Описание

Для реализации собственных заглушек нужно добавить в
директорию `org.samokat.performance.mockserver.mocks` класс с ожиданиями по примеру
`Croissant.java` или `BananaBread.java`. Один класс представляет полную конфигурацию заглушки. Также
стоит добавить в `org.samokat.performance.mockserver.core.initializer.CommandSwitcher` инициализацию
вашей конфигурации по примеру.

### Функционал

Метрики mockserver - http://localhost:1080/mockserver/metrics  
Дашборд для отладки - http://localhost:1080/mockserver/dashboard  
Помимо конфигураций описываемых в классах директории `org.samokat.performance.mockserver.mocks` релизовано:  

- заглушка SMTP по порту 587
- заглушка graphql для экстернал полей (пример использования в `BananaBread.java`)
- метрики micrometer - http://localhost:8080/prometheus

### HowToRun

#### Docker

Замените значение для team : croissant, bananabread или индентификатор вашей заглушки

```
docker build -t mock .
docker run --name mock -p 587:587 -p 1080:1080 -p 8080:8080 -it mock:latest java -jar -Dteam=<your_value> -Dloglevel=ERROR run.jar
```