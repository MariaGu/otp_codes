package ru.otp_codes.service;

import ru.otp_codes.dao.OTPDao;
import ru.otp_codes.dao.UserDao;
import ru.otp_codes.dto.UserDto;
import ru.otp_codes.dto.UserRegDto;
import ru.otp_codes.model.OTPCode;
import ru.otp_codes.model.User;
import ru.otp_codes.utils.OTPSender;
import ru.otp_codes.utils.PasswordEncoder;
import ru.otp_codes.utils.UserMapper;

import javax.crypto.spec.SecretKeySpec;
import javax.xml.bind.DatatypeConverter;
import java.security.Key;

import io.jsonwebtoken.*;

import java.util.*;

import io.jsonwebtoken.Jwts;

public class AuthService {
    private UserDao userDao = new UserDao();
    private static String SECRET_KEY = "oeRaYdd9jow55FfXMiINEdt1XR85VprC9jow55FfXMiINEdt1XR85RK93w";


    public String register(UserRegDto userRegDto) throws Exception {
        if (userDao.findByUsername(userRegDto.getUsername()) != null)
            throw new Exception("Username already taken");

        if (userRegDto.isAdmin() && userDao.isAdminExists())
            throw new Exception("Admin already exists");

        User user = UserMapper.fromDto(userRegDto);
        userDao.saveUser(user);

        return "Registered successfully.";
    }

    public String login(UserDto userDto) throws Exception {
        User user = userDao.findByUsername(userDto.getUsername());
        if (user == null) return null;
        if (PasswordEncoder.verify(userDto.getPassword(), user.getPasswordHash())) {
            return generateJWT(user);
        } else {
            return null;
        }
    }

    public String generateJWT(User user) {

        int ttlMillis = 30000;
        long nowMillis = System.currentTimeMillis();
        Date now = new Date(nowMillis);

        byte[] apiKeySecretBytes = DatatypeConverter.parseBase64Binary(SECRET_KEY);
        Key signingKey = new SecretKeySpec(apiKeySecretBytes, SignatureAlgorithm.HS256.getJcaName());

        Map<String, Object> claims = new HashMap<>();
        claims.put("name", user.getUsername());
        claims.put("role", user.getRole());

        JwtBuilder builder = Jwts.builder()
                .setIssuedAt(now)
                .setClaims(claims)
                .setSubject(String.valueOf(user.getId()))
                .setIssuer("otp_codes")
                .signWith(SignatureAlgorithm.HS256, signingKey);

        if (ttlMillis >= 0) {
            long expMillis = nowMillis + ttlMillis;
            Date exp = new Date(expMillis);
            builder.setExpiration(exp);
        }

        return builder.compact();
    }

    public static Map<String, String> decodeJWT(String jwt) {
        try {
            Claims claims = Jwts.parser()
                    .setSigningKey(DatatypeConverter.parseBase64Binary(SECRET_KEY))
                    .parseClaimsJws(jwt)
                    .getBody();

            Map<String, String> result = new HashMap<>();
            result.put("role", (String) claims.get("role"));
            result.put("id", claims.getSubject());
          return result;

        } catch (ExpiredJwtException e) {
            System.out.println("Token expired: " + e.getMessage());
            return null;
        } catch (SignatureException e) {
            System.out.println("Invalid signature: " + e.getMessage());
            return null;
        } catch (Exception e) {
            System.out.println("Invalid token: " + e.getMessage());
            return null;
        }
    }


}

