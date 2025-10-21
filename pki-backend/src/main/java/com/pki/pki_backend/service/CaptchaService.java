package com.pki.pki_backend.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Service
public class CaptchaService {
    
    @Value("${recaptcha.secret-key:6LeIxAcTAAAAAGG-vFI1TnRWxMZNFuojJ4WifJWe}") // Test secret key
    private String secretKey;
    
    private final RestTemplate restTemplate = new RestTemplate();
    
    public boolean verifyCaptcha(String captchaToken) {
        if (captchaToken == null || captchaToken.isEmpty()) {
            return false;
        }
        
        try {
            String url = "https://www.google.com/recaptcha/api/siteverify";
            
            MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
            params.add("secret", secretKey);
            params.add("response", captchaToken);
            
            HttpHeaders headers = new HttpHeaders();
            headers.add("Content-Type", "application/x-www-form-urlencoded");
            
            HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(params, headers);
            
            ResponseEntity<Map> response = restTemplate.postForEntity(url, request, Map.class);
            
            Map<String, Object> body = response.getBody();
            return body != null && Boolean.TRUE.equals(body.get("success"));
            
        } catch (Exception e) {
            System.err.println("Error verifying captcha: " + e.getMessage());
            return false;
        }
    }
}