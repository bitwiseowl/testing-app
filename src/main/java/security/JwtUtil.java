package security;

import java.nio.charset.StandardCharsets;
import java.util.Date;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.provisioning.UserDetailsManager;
import org.springframework.stereotype.Component;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

import org.springframework.security.authentication.BadCredentialsException;

@Component
public class JwtUtil {
	


	public JwtUtil(@Value("${jwt.secretKey}") String secretKey,
			@Value("${jwt.expirationOffset}") int expirationOffset, @Value("${jwt.refreshOffset}") long refreshOffset, UserDetailsManager userDetailsManager) {
		super();
		this.secretKey = secretKey;
		this.expirationOffset = expirationOffset;
		this.refreshOffset = refreshOffset;
		this.userDetailsManager = userDetailsManager;
	}

	private final String secretKey;
	private final int expirationOffset;
	private final long refreshOffset;
	private final UserDetailsManager userDetailsManager;
	
	
	//Use this to convert your secret key to SHA256 
	public SecretKey convertKeyToSHA256() {
		return Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8));
		
	}
	
	public String generateToken(UserDetails user) {
		return Jwts.builder()
			.subject(user.getUsername())
			.issuedAt(new Date())
			.expiration(new Date(System.currentTimeMillis() + expirationOffset))
			.claim("type", "access")
			.signWith(convertKeyToSHA256()) 
			.compact();
	}
	
	public String generateRefreshToken(UserDetails user) {
		return Jwts.builder()
			.subject(user.getUsername())
			.issuedAt(new Date())
			.expiration(new Date(System.currentTimeMillis() + refreshOffset))
			.claim("type", "refresh")
			.signWith(convertKeyToSHA256()) 
			.compact();
	}
	
	public String getUsernameFromToken(String token) {
		return Jwts.parser().verifyWith(convertKeyToSHA256()).build().parseSignedClaims(token).getPayload().getSubject();
		
	}
	
	public boolean validateAccessToken(String token, UserDetails userDetails) {
	    try {
	        Claims claims = Jwts.parser()
	                .verifyWith(convertKeyToSHA256())
	                .build()
	                .parseSignedClaims(token)
	                .getPayload();

	        String username = claims.getSubject();
	        Date expiration = claims.getExpiration();
	        String type = claims.get("type", String.class);
	        return ("access".equals(type) && userDetailsManager.userExists(username) &&  !expiration.before(new Date()));
	    } catch (Exception e) {
	        throw new BadCredentialsException("Token does not match Signature"); 
	    }
	}
	
	public boolean validateRefreshToken(String token) {
	    try {
	        Claims claims = Jwts.parser()
	                .verifyWith(convertKeyToSHA256())
	                .build()
	                .parseSignedClaims(token)
	                .getPayload();

	        String type = claims.get("type", String.class);
	        return ("refresh".equals(type) && !claims.getExpiration().before(new Date()));
	    } catch (Exception e) {
	        return false; 
	    }
	}
	


}
