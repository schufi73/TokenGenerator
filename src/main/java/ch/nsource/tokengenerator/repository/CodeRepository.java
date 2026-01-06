package ch.nsource.tokengenerator.repository;

import ch.nsource.tokengenerator.model.CodeEntry;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.time.Instant;
import java.util.Optional;

@Repository
public class CodeRepository {
    private final JdbcTemplate jdbcTemplate;

    private static final RowMapper<CodeEntry> ROW_MAPPER = (rs, rowNum) -> {
        CodeEntry e = new CodeEntry();
        e.setId(rs.getLong("id"));
        e.setCode(rs.getString("code"));
        long created = rs.getLong("created_at");
        long expires = rs.getLong("expires_at");
        e.setCreatedAt(Instant.ofEpochSecond(created));
        e.setExpiresAt(Instant.ofEpochSecond(expires));
        return e;
    };

    public CodeRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public CodeEntry insert(CodeEntry entry) throws DataAccessException {
        String sql = "INSERT INTO codes(code, created_at, expires_at) VALUES(?, ?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(con -> {
            PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, entry.getCode());
            ps.setLong(2, entry.getCreatedAt().getEpochSecond());
            ps.setLong(3, entry.getExpiresAt().getEpochSecond());
            return ps;
        }, keyHolder);
        Number key = keyHolder.getKey();
        if (key != null) {
            entry.setId(key.longValue());
        }
        return entry;
    }

    public Optional<CodeEntry> findByCode(String code) {
        String sql = "SELECT id, code, created_at, expires_at FROM codes WHERE code = ?";
        return jdbcTemplate.query(sql, ROW_MAPPER, code).stream().findFirst();
    }

    public boolean deleteByCode(String code) {
        String sql = "DELETE FROM codes WHERE code = ?";
        int updated = jdbcTemplate.update(sql, code);
        return updated > 0;
    }
}
