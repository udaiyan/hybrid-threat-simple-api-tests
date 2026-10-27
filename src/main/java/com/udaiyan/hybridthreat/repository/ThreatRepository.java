package com.udaiyan.hybridthreat.repository;

import com.udaiyan.hybridthreat.model.Threat;
import com.udaiyan.hybridthreat.model.ThreatType;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;

@Repository
public class ThreatRepository {

    private static final String SELECT_COLS =
        "SELECT id, name, type, severity, source, detected_at, status FROM threats";

    private final JdbcClient jdbcClient;

    public ThreatRepository(JdbcClient jdbcClient) {
        this.jdbcClient = jdbcClient;
    }

    public List<Threat> findAll() {
        return jdbcClient.sql(SELECT_COLS + " ORDER BY id")
            .query((rs, n) -> map(rs))
            .list();
    }

    public List<Threat> findByType(ThreatType type) {
        return jdbcClient.sql(SELECT_COLS + " WHERE type = :type ORDER BY id")
            .param("type", type.name())
            .query((rs, n) -> map(rs))
            .list();
    }

    public Optional<Threat> findById(long id) {
        return jdbcClient.sql(SELECT_COLS + " WHERE id = :id")
            .param("id", id)
            .query((rs, n) -> map(rs))
            .optional();
    }

    public long insert(Threat threat) {
        return jdbcClient.sql("""
                INSERT INTO threats (name, type, severity, source, detected_at, status)
                VALUES (:name, :type, :severity, :source, :detectedAt, :status)
                RETURNING id
                """)
            .param("name", threat.getName())
            .param("type", threat.getType().name())
            .param("severity", threat.getSeverity())
            .param("source", threat.getSource())
            .param("detectedAt", Timestamp.from(threat.getDetectedAt()))
            .param("status", threat.getStatus())
            .query(Long.class)
            .single();
    }

    public int update(long id, Threat threat) {
        return jdbcClient.sql("""
                UPDATE threats
                   SET name = :name,
                       type = :type,
                       severity = :severity,
                       source = :source,
                       detected_at = :detectedAt,
                       status = :status
                 WHERE id = :id
                """)
            .param("name", threat.getName())
            .param("type", threat.getType().name())
            .param("severity", threat.getSeverity())
            .param("source", threat.getSource())
            .param("detectedAt", Timestamp.from(threat.getDetectedAt()))
            .param("status", threat.getStatus())
            .param("id", id)
            .update();
    }

    public int delete(long id) {
        return jdbcClient.sql("DELETE FROM threats WHERE id = :id")
            .param("id", id)
            .update();
    }

    private Threat map(ResultSet rs) throws SQLException {
        return new Threat(
            rs.getLong("id"),
            rs.getString("name"),
            ThreatType.valueOf(rs.getString("type")),
            rs.getString("severity"),
            rs.getString("source"),
            rs.getTimestamp("detected_at").toInstant(),
            rs.getString("status")
        );
    }
}
