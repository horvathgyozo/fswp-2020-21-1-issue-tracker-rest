package hu.elte.issuetrackerrest.security;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.fasterxml.jackson.databind.ObjectMapper;
import hu.elte.issuetrackerrest.entities.User;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.stream.Collectors;
import javax.servlet.FilterChain;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

public class JWTAuthenticationFilter extends UsernamePasswordAuthenticationFilter {

    private AuthenticationManager authenticationManager;
    
    private static final String COOKIE_NAME = "token";

    private static final int EXPIRATION =  30 * 60 * 1000;
    
    private static final String SECRET =  "alma";

    public JWTAuthenticationFilter(AuthenticationManager authenticationManager) {
        this.authenticationManager = authenticationManager;
        setFilterProcessesUrl("/api/auth"); 
    }

    @Override
    public Authentication attemptAuthentication(HttpServletRequest req,
                                                HttpServletResponse res) throws AuthenticationException {
        try {
            User creds = new ObjectMapper()
                    .readValue(req.getInputStream(), User.class);

            return authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            creds.getUsername(),
                            creds.getPassword(),
                            new ArrayList<>())
            );
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    protected void successfulAuthentication(HttpServletRequest req,
                                            HttpServletResponse res,
                                            FilterChain chain,
                                            Authentication auth) throws IOException {
        long now = System.currentTimeMillis();
        String token = JWT.create()
                .withSubject(auth.getName())
                .withExpiresAt(new Date(now + EXPIRATION))
                .sign(Algorithm.HMAC512(SECRET.getBytes()));
//        String token = Jwts.builder()
//                    .setSubject(auth.getName())
//                    .claim("authorities", auth.getAuthorities().stream()
//                            .map(GrantedAuthority::getAuthority).collect(Collectors.toList()))
//                    .setIssuedAt(new Date(now))
//                    .setExpiration(new Date(now + EXPIRATION))
//                    .signWith(SignatureAlgorithm.HS512, SECRET)
//                    .compact();

        res.getWriter().write(token);
        res.getWriter().flush();
    }
}