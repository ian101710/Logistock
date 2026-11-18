package com.logistock.app.dto;

/**
 * Respuesta del endpoint POST /api/auth/login con el token JWT generado.
 */
public class LoginResponse {

    private String token;
    private String tipo;
    private long expiraEnMs;

    public LoginResponse(String token, String tipo, long expiraEnMs) {
        this.token = token;
        this.tipo = tipo;
        this.expiraEnMs = expiraEnMs;
    }

    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }
    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { this.tipo = tipo; }
    public long getExpiraEnMs() { return expiraEnMs; }
    public void setExpiraEnMs(long expiraEnMs) { this.expiraEnMs = expiraEnMs; }
}
