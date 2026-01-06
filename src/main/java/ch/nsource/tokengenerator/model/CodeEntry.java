package ch.nsource.tokengenerator.model;

import java.time.Instant;

public class CodeEntry {
    private Long id;
    private String code;
    private Instant createdAt;
    private Instant expiresAt;

    public CodeEntry() {
    }

    public CodeEntry(Long id, String code, Instant createdAt, Instant expiresAt) {
        this.id = id;
        this.code = code;
        this.createdAt = createdAt;
        this.expiresAt = expiresAt;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(Instant expiresAt) {
        this.expiresAt = expiresAt;
    }
}
