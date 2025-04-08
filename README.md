# otp_codes

представлен пример реализации простой аутентификации и отправки OTP-кода в текстовый файл средствами Java и JDBC с использованием PostgreSQL. Этот пример демонстрирует основные принципы, включая регистрацию пользователя, логин, генерацию OTP, сохранение в БД и отправку в файл. Всё выполнено в терминах трёхслойной архитектуры: DAO → Service → Controller.

⛳️ Предположения:

- Используем PostgreSQL 17
- Используем таблицы users и otp_codes согласно описанию
- Простая реализация HTTP-сервера на com.sun.net.httpserver.HttpServer

1. Структура проекта (минимально)

- Main.java — запускает HTTP-сервер
- controller/
    - AuthController.java
- service/
    - AuthService.java
- dao/
    - UserDao.java
    - OTPDao.java
- model/
    - User.java
    - OTPCode.java
- util/
    - DB.java — соединение к БД
    - PasswordEncoder.java
    - OTPSender.java