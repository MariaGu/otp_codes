package ru.otp_codes.controller;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import ru.otp_codes.dto.UserDto;
import ru.otp_codes.dto.UserRegDto;
import ru.otp_codes.service.AuthService;
import ru.otp_codes.utils.JsonParser;

import java.io.*;


public class AuthController implements HttpHandler {
    private final AuthService authService = new AuthService();

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String path = exchange.getRequestURI().getPath();
        if ("POST".equalsIgnoreCase(exchange.getRequestMethod())) {
            if (path.equals("/register")) handleRegister(exchange);
            else if (path.equals("/login")) handleLogin(exchange);
        }
    }

    private void handleRegister(HttpExchange exchange) throws IOException {
        UserRegDto userRegDto = JsonParser.parseBody(exchange, UserRegDto.class);
        try {
            String res = authService.register(userRegDto);
            sendResponse(exchange, res, 200);
        } catch (Exception ex) {
            ex.printStackTrace();
            sendResponse(exchange, ex.getMessage(), 400);
        }
    }

    private void handleLogin(HttpExchange exchange) throws IOException {
        UserDto userDto = JsonParser.parseBody(exchange, UserDto.class);
        try {
            String token = authService.login(userDto);
            if (token != null)
                sendResponse(exchange, "Login successful, token: " + token, 200);
            else
                sendResponse(exchange, "Invalid credentials", 401);
        } catch (Exception ex) {
            sendResponse(exchange, ex.getMessage(), 500);
        }
    }

    private void sendResponse(HttpExchange exchange, String response, int code) throws IOException {
        exchange.sendResponseHeaders(code, response.length());
        OutputStream os = exchange.getResponseBody();
        os.write(response.getBytes());
        os.close();
    }
}