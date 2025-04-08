package ru.otp_codes.controller;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import ru.otp_codes.dto.*;
import ru.otp_codes.service.AuthService;
import ru.otp_codes.service.UserService;
import ru.otp_codes.utils.JWTValidator;
import ru.otp_codes.utils.JsonParser;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

public class UserController implements HttpHandler {
    private final UserService userService = new UserService();

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String path = exchange.getRequestURI().getPath();
        String id=null;
        if (path.startsWith("/user/")){
            id = JWTValidator.checkJWT(exchange, List.of("admin", "user"));
            if (id==null){
                sendResponse(exchange, "Missing or invalid Authorization header", 401);
                return;
            }
        }
        if ("POST".equalsIgnoreCase(exchange.getRequestMethod())) {
            if (path.equals("/user/validate_otp")) handleValidateOTP(exchange, id);
            if (path.equals("/user/generate_otp")) handleGenerateOTP(exchange, id);
            else if (path.equals("/user/make_transaction")) handleTransaction(exchange, id);
        }
    }

    private void handleValidateOTP(HttpExchange exchange, String id) throws IOException {
        OTPValidDto otpValidDto = JsonParser.parseBody(exchange, OTPValidDto.class);
        try {
            boolean result = userService.validateOTP(otpValidDto, id);
            if (result) {
                sendResponse(exchange, "OTP-code validated successfully", 200);
            } else {
                sendResponse(exchange, "OTP-code NOT validated successfully", 400);
            }
        } catch (Exception ex) {
            sendResponse(exchange, ex.getMessage(), 400);
        }
    }

    private void handleGenerateOTP(HttpExchange exchange, String id) throws IOException {
        OTPDto otpDto = JsonParser.parseBody(exchange, OTPDto.class);
        try {
            userService.generateOTP(otpDto, id);
            sendResponse(exchange, "OTP generated and sent", 200);
        } catch (Exception ex) {
            sendResponse(exchange, ex.getMessage(), 400);
        }
    }

    private void handleTransaction(HttpExchange exchange, String id) throws IOException {
        TransactionDto transactionDto = JsonParser.parseBody(exchange, TransactionDto.class);
        try {
            userService.makeTransaction(transactionDto, id);
            sendResponse(exchange, "Transaction made", 200);
        } catch (Exception ex) {
            sendResponse(exchange, ex.getMessage(), 400);
        }
    }

    private void sendResponse(HttpExchange exchange, String response, int code) throws IOException {
        exchange.sendResponseHeaders(code, response.length());
        OutputStream os = exchange.getResponseBody();
        os.write(response.getBytes());
        os.close();
    }
}
