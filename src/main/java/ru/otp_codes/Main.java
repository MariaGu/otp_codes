package ru.otp_codes;

import com.sun.net.httpserver.HttpServer;
import ru.otp_codes.controller.AdminController;
import ru.otp_codes.controller.AuthController;
import ru.otp_codes.controller.UserController;
import ru.otp_codes.dao.OTPConfigDao;
import ru.otp_codes.dao.OTPDao;
import ru.otp_codes.dao.TransactionDao;
import ru.otp_codes.dao.UserDao;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.sql.SQLException;
import java.util.Properties;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class Main {

     private static int PERIOD_SCHEDULER;
     private static int OTP_LIFETIME;
     private static int OTP_LENGTH;
    private static int PORT;


    public Main() {
        Properties config = mainConfig();
        PERIOD_SCHEDULER = Integer.parseInt(config.getProperty("app.period.scheduler.min"));
        OTP_LIFETIME = Integer.parseInt(config.getProperty("app.otp.lifetime.sec"));
        OTP_LENGTH = Integer.parseInt(config.getProperty("app.otp.length"));
        PORT = Integer.parseInt(config.getProperty("app.port"));
    }

    private Properties mainConfig() {
        try {
            Properties props = new Properties();
            props.load(Main.class.getClassLoader()
                    .getResourceAsStream("app.properties"));
            return props;
        } catch (Exception e) {
            throw new RuntimeException("Failed to load Main configuration", e);
        }
    }

    public static void main(String[] args) throws Exception {
        Main main = new Main();
        createDB();
        createServer();
        initScheduler();
    }

    private static void createServer() throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress(PORT), 0);
        server.createContext("/register", new AuthController());
        server.createContext("/login", new AuthController());
        server.createContext("/admin/users", new AdminController());
        server.createContext("/admin/delete_users", new AdminController());
        server.createContext("/admin/otp_config_edit", new AdminController());
        server.createContext("/user/generate_otp", new UserController());
        server.createContext("/user/make_transaction", new UserController());
        server.createContext("/user/validate_otp", new UserController());
        server.setExecutor(null);
        server.start();
        System.out.println("Server started on port 8080...");
    }

    private static void createDB() throws SQLException {
        UserDao userDao = new UserDao();
        userDao.createUser();
        TransactionDao transactionDao = new TransactionDao();
        transactionDao.createTransactions();
        OTPDao otpDao = new OTPDao();
        otpDao.createOTP();
        OTPConfigDao otpConfigDao = new OTPConfigDao();
        otpConfigDao.createOTPConfig(OTP_LIFETIME, OTP_LENGTH);
        System.out.println("DB created...");
    }

    private static void initScheduler() {
        ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
        scheduler.scheduleAtFixedRate(
                () -> {
                    OTPDao otpDao = new OTPDao();
                    try {
                        otpDao.editStatusWhenExpired("EXPIRED");
                    } catch (SQLException e) {
                        throw new RuntimeException(e);
                    }
                },
                0,
                PERIOD_SCHEDULER,
                TimeUnit.MINUTES
        );

        System.out.println("OTP-scheduler created...");
    }
}
