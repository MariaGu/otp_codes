package ru.otp_codes.utils;

import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;

public class OTPSender {
    public static void sendToFile(String username, String code) {
        try (FileWriter writer = new FileWriter("OTP_" + username + ".txt")) {
            writer.write("Hello, " + username + "\nYour OTP code is: " + code +
                    "\nGenerated at: " + LocalDateTime.now());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
