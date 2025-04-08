package ru.otp_codes.dao;

import ru.otp_codes.dto.TransactionDto;
import ru.otp_codes.utils.DB;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.UUID;

public class TransactionDao {

    public void createTransactions() throws SQLException {
        String sql = """
                CREATE TABLE IF NOT EXISTS transactions (
                    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                    user_id UUID REFERENCES users(id) ON DELETE CASCADE,
                    purchase VARCHAR(20) NOT NULL,
                    amount INTEGER NOT NULL,
                    created_at TIMESTAMP NOT NULL DEFAULT now()
                );""";
        try (Connection conn = DB.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.executeUpdate();
        }
    }

    public void saveTransaction(TransactionDto transactionDto, UUID userId) throws SQLException {
        String sql = "INSERT INTO transactions (user_id, purchase, amount) VALUES (?, ?, ?)";
        try (Connection conn = DB.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setObject(1, userId);
            ps.setString(2, transactionDto.getPurchase());
            ps.setInt(3, transactionDto.getAmount());
            ps.executeUpdate();
        }
    }
}
