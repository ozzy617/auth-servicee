package com.project.authservice.service;

import com.project.authservice.dto.response.CheckTokenAuthenticationResponse;
import com.project.authservice.entity.UserEntity;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.io.Decoders;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class JwtService {

    private final UserService userService;
    private final Logger logger = LoggerFactory.getLogger(JwtService.class);

    @Value("${token.signing.key}")
    private String jwtSigningKey;

    @Value("${token.signing.key}")
    private String jwtRefreshKey;

    public String extractUserName(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public UserDetails extractUserDetails(String token) {
        String username = extractUserName(token);
        return userService.userDetailsService().loadUserByUsername(username);
    }

    public String generateToken(UserDetails userDetails) {
        Map<String, Object> claims = new HashMap<>();
        if (userDetails instanceof UserEntity) {
            UserEntity user = (UserEntity) userDetails;
            claims.put("id", user.getId());
            claims.put("email", user.getEmail());
            claims.put("role", user.getRole());
        }
        return generateToken(claims, userDetails, jwtSigningKey, 1000 * 60 * 24); // 24 minutes
    }

    public String generateRefreshToken(UserDetails userDetails) {
        Map<String, Object> claims = new HashMap<>();
        return generateToken(claims, userDetails, jwtRefreshKey, 1000 * 60 * 60 * 24 * 7); // 7 days
    }

    public boolean isTokenValid(String token, UserDetails userDetails) {
        String userName = extractUserName(token);
        return (userName.equals(userDetails.getUsername())) && !isTokenExpired(token, jwtSigningKey);
    }

    public boolean isRefreshTokenValid(String token) {
        return !isTokenExpired(token, jwtRefreshKey);
    }

    public <T> T extractClaim(String token, java.util.function.Function<Claims, T> claimsResolver) {
        Claims claims = extractAllClaims(token, jwtSigningKey);
        return claimsResolver.apply(claims);
    }

    public <T> T extractClaim(String token, String key, java.util.function.Function<Claims, T> claimsResolver) {
        Claims claims = extractAllClaims(token, key);
        return claimsResolver.apply(claims);
    }

    private String generateToken(Map<String, Object> extraClaims, UserDetails userDetails, String key, long expiration) {
        return Jwts.builder()
                .setClaims(extraClaims)
                .setSubject(userDetails.getUsername())
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(getSigningKey(key), SignatureAlgorithm.HS256)
                .compact();
    }

    private boolean isTokenExpired(String token, String key) {
        return extractExpiration(token, key).before(new Date());
    }

    private Date extractExpiration(String token, String key) {
        return extractClaim(token, key, Claims::getExpiration);
    }

    private Claims extractAllClaims(String token, String key) {
        return Jwts.parser()
                .setSigningKey(getSigningKey(key))
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    private Key getSigningKey(String key) {
        byte[] keyBytes = Decoders.BASE64.decode(key);
        return Keys.hmacShaKeyFor(keyBytes);
    }
    public CheckTokenAuthenticationResponse validateToken(String token) {
        try {
            String userName = extractUserName(token);
            UserDetails userDetails = userService.userDetailsService().loadUserByUsername(userName);
            boolean isAuthenticated = (userName.equals(userDetails.getUsername())) && !isTokenExpired(token, jwtSigningKey);
            return new CheckTokenAuthenticationResponse(userDetails, isAuthenticated);
        } catch (Exception e) {
            return new CheckTokenAuthenticationResponse(null, false);
        }
    }
}







//@Service
//@RequiredArgsConstructor
//public class JwtService {
//
//    private final UserService userService;
//    //private final Logger logger = LoggerFactory.getLogger(JwtService.class);
//
//    @Value("${token.signing.key}")
//    private String jwtSigningKey;
//
//    /**
//     * Extract username from token
//     *
//     * @param token token
//     * @return username
//     */
//    public String extractUserName(String token) {
//        return extractClaim(token, Claims::getSubject);
//    }
//
//    /**
//     * Generate token
//     *
//     * @param userDetails user details
//     * @return token
//     */
//    public String generateToken(UserDetails userDetails) {
//        Map<String, Object> claims = new HashMap<>();
//        if (userDetails instanceof UserEntity) {
//            UserEntity user = (UserEntity) userDetails;
//            claims.put("id", user.getId());
//            claims.put("email", user.getEmail());
//            claims.put("role", user.getRole());
//        }
//        return generateToken(claims, userDetails);
//    }
//
//    /**
//     * Validate token
//     *
//     * @param token token
//     * @param userDetails user details
//     * @return true if token is valid
//     */
//    public boolean isTokenValid(String token, UserDetails userDetails) {
//        String userName = extractUserName(token);
//        return (userName.equals(userDetails.getUsername())) && !isTokenExpired(token);
//    }
//
//    public CheckTokenAuthenticationResponse validateToken(String token) {
//        try {
//            String userName = extractUserName(token);
//            UserDetails userDetails = userService.userDetailsService().loadUserByUsername(userName);
//           // logger.info("UserDetails obtained for username = {}", userName);
//            boolean isAuthenticated = (userName.equals(userDetails.getUsername())) && !isTokenExpired(token);
//            //logger.info("User authentication check");
//            return new CheckTokenAuthenticationResponse(userDetails,isAuthenticated);
//        } catch (Exception e) {
//            return new CheckTokenAuthenticationResponse(null,false);
//        }
//    }
//
//    /**
//     * Extract claim from token
//     *
//     * @param token token
//     * @param claimsResolver function to extract claim
//     * @param <T> type of claim
//     * @return claim
//     */
//    public <T> T extractClaim(String token, java.util.function.Function<Claims, T> claimsResolver) {
//        Claims claims = extractAllClaims(token);
//        return claimsResolver.apply(claims);
//    }
//
//    /**
//     * Generate token
//     *
//     * @param extraClaims extra claims
//     * @param userDetails user details
//     * @return token
//     */
//    private String generateToken(Map<String, Object> extraClaims, UserDetails userDetails) {
//        return Jwts.builder()
//                .setClaims(extraClaims)
//                .setSubject(userDetails.getUsername())
//                .setIssuedAt(new Date(System.currentTimeMillis()))
//                .setExpiration(new Date(System.currentTimeMillis() + 1000 * 60 * 24))
//                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
//                .compact();
//    }
//
//    /**
//     * Check if token is expired
//     *
//     * @param token token
//     * @return true if token is expired
//     */
//    private boolean isTokenExpired(String token) {
//        return extractExpiration(token).before(new Date());
//    }
//
//    /**
//     * Extract expiration date from token
//     *
//     * @param token token
//     * @return expiration date
//     */
//    private Date extractExpiration(String token) {
//        return extractClaim(token, Claims::getExpiration);
//    }
//
//    /**
//     * Extract all claims from token
//     *
//     * @param token token
//     * @return claims
//     */
//    private Claims extractAllClaims(String token) {
//        return Jwts.parser()
//                .setSigningKey(getSigningKey())
//                .build()
//                .parseClaimsJws(token)
//                .getBody();
//    }
//
//    /**
//     * Get signing key
//     *
//     * @return key
//     */
//    private Key getSigningKey() {
//        byte[] keyBytes = Decoders.BASE64.decode(jwtSigningKey);
//        return Keys.hmacShaKeyFor(keyBytes);
//    }
//}