package ru.otp_codes.controller;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import ru.otp_codes.dto.OTPConfigDto;
import ru.otp_codes.dto.UsersDelDto;
import ru.otp_codes.service.AdminService;
import ru.otp_codes.utils.JWTValidator;
import ru.otp_codes.utils.JsonParser;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

public class AdminController implements HttpHandler {

    private final AdminService adminService = new AdminService();

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String path = exchange.getRequestURI().getPath();
        String method = exchange.getRequestMethod();
        if (path.startsWith("/admin/")){
          String id = JWTValidator.checkJWT(exchange, List.of("admin"));
          if (id==null){
              sendResponse(exchange, "Missing or invalid Authorization header", 401);
              return;
          }
        }
        if (method.equalsIgnoreCase("POST") && path.equals("/admin/otp_config_edit")) {
            handleOtpConfigEdit(exchange);
        } else if (method.equalsIgnoreCase("GET") && path.equals("/admin/users")) {
            handleUsers(exchange);
        } else if (method.equalsIgnoreCase("DELETE") && path.equals("/admin/delete_users")) {
            handleDeleteUsers(exchange);
        }
    }


    private void handleOtpConfigEdit(HttpExchange exchange) throws IOException {
        OTPConfigDto otpConfigDto = JsonParser.parseBody(exchange, OTPConfigDto.class);
        try {
            adminService.editConfig(otpConfigDto);
            sendResponse(exchange, "Configs were changed successfully", 200);
        } catch (Exception ex) {
            ex.printStackTrace();
            sendResponse(exchange, ex.getMessage(), 400);
        }
    }

    private void handleUsers(HttpExchange exchange) throws IOException {
        try {
            List<String> usernames = adminService.getUsers();
            sendResponse(exchange, "Usernames: : " + usernames, 200);
        } catch (Exception ex) {
            sendResponse(exchange, ex.getMessage(), 500);
        }
    }

    private void handleDeleteUsers(HttpExchange exchange) throws IOException {

        UsersDelDto usersDelDto = JsonParser.parseBody(exchange, UsersDelDto.class);
        try {
            adminService.deleteUsers(usersDelDto);
            sendResponse(exchange, "Users deleted successfully", 200);
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

