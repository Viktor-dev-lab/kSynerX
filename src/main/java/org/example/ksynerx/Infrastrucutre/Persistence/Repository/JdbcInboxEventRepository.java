package org.example.ksynerx.Infrastrucutre.Persistence.Repository;

import org.example.ksynerx.Domain.Interface.InboxEventRepository;
import org.example.ksynerx.Domain.Interface.InboxWorkerRepository;
import org.example.ksynerx.Domain.Model.ChannelType;
import org.example.ksynerx.Domain.Model.InboxEvent;
import org.example.ksynerx.Domain.Model.InboxStatus;
import org.example.ksynerx.Domain.Exception.ChannelConfigurationException;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.Objects;
import java.util.Optional;

@Repository
public class JdbcInboxEventRepository implements InboxEventRepository, InboxWorkerRepository {
    private final NamedParameterJdbcTemplate jdbc;

    public JdbcInboxEventRepository(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public long getOrCreateChannel(String sourceCode, ChannelType type) {
        var parameters = new MapSqlParameterSource("sourceCode", sourceCode).addValue("type", type.name());
        jdbc.update("""
                INSERT INTO channels (source_code, type)
                VALUES (:sourceCode, CAST(:type AS channel_type))
                ON CONFLICT (type) DO NOTHING
                """, parameters);
        return Objects.requireNonNull(jdbc.queryForObject("""
                SELECT id, source_code, enabled FROM channels
                WHERE type = CAST(:type AS channel_type) FOR SHARE
                """, parameters, (row, index) -> {
            if (!sourceCode.equals(row.getString("source_code"))) {
                throw new ChannelConfigurationException(ChannelConfigurationException.Reason.SOURCE_MISMATCH,
                        "Channel " + type + " belongs to a different sourceCode");
            }
            if (!row.getBoolean("enabled")) {
                throw new ChannelConfigurationException(ChannelConfigurationException.Reason.DISABLED,
                        "Channel " + type + " is disabled");
            }
            return row.getLong("id");
        }));
    }

    @Override
    public void lockProduct(String sourceCode, int productId) {
        // Transaction-scoped lock also covers the first event, before a product row exists.
        jdbc.query("""
                SELECT pg_advisory_xact_lock(hashtextextended(:sourceCode || ':' || CAST(:productId AS text), 0))
                """, identity(sourceCode, productId), row -> { });
    }

    @Override
    public long insert(long channelId, int productId, long revision, String contentHash, String payload) {
        var parameters = new MapSqlParameterSource("channelId", channelId)
                .addValue("productId", (long) productId).addValue("revision", revision)
                .addValue("contentHash", contentHash).addValue("payload", payload);
        return Objects.requireNonNull(jdbc.queryForObject("""
                INSERT INTO inbox_events (channel_id, product_id, revision, content_hash, payload)
                VALUES (:channelId, :productId, :revision, :contentHash, CAST(:payload AS jsonb))
                RETURNING id
                """, parameters, Long.class));
    }

    @Override
    public Optional<InboxEvent> claimNext() {
        return jdbc.query("""
                SELECT e.id, c.source_code, e.product_id, e.revision, e.content_hash,
                       e.payload::text AS payload, e.retry_count
                FROM inbox_events e JOIN channels c ON c.id = e.channel_id
                WHERE e.status IN ('PENDING', 'RETRY') AND e.next_retry_at <= CURRENT_TIMESTAMP AND c.enabled
                ORDER BY e.created_at, e.id LIMIT 1 FOR UPDATE OF e SKIP LOCKED
                """, new MapSqlParameterSource(), (row, index) -> new InboxEvent(
                row.getLong("id"), row.getString("source_code"), row.getLong("product_id"),
                row.getLong("revision"), row.getString("content_hash"), row.getString("payload"),
                row.getInt("retry_count"))).stream().findFirst();
    }

    @Override
    public void complete(long eventId, InboxStatus status, String message) {
        jdbc.update("""
                UPDATE inbox_events SET status = CAST(:status AS inbox_status),
                    processed_at = clock_timestamp(), error_message = :message
                WHERE id = :eventId
                """, new MapSqlParameterSource("eventId", eventId).addValue("status", status.name())
                .addValue("message", message, java.sql.Types.VARCHAR));
    }

    @Override
    public Optional<InboxStatus> recordFailure(InboxEvent event, boolean permanent, int maxAttempts,
                                              int retryDelaySeconds, String message) {
        return jdbc.query("""
                UPDATE inbox_events SET
                    retry_count = retry_count + 1,
                    status = CASE WHEN :permanent OR retry_count + 1 >= :maxAttempts
                                  THEN 'FAILED'::inbox_status ELSE 'RETRY'::inbox_status END,
                    next_retry_at = clock_timestamp() + :retryDelaySeconds * INTERVAL '1 second',
                    processed_at = CASE WHEN :permanent OR retry_count + 1 >= :maxAttempts
                                        THEN clock_timestamp() ELSE NULL END,
                    error_message = :message
                WHERE id = :eventId AND status IN ('PENDING', 'RETRY') AND retry_count = :expectedRetries
                RETURNING status::text
                """, new MapSqlParameterSource("eventId", event.id()).addValue("expectedRetries", event.retryCount())
                .addValue("permanent", permanent).addValue("maxAttempts", maxAttempts)
                .addValue("retryDelaySeconds", retryDelaySeconds).addValue("message", message),
                (row, index) -> InboxStatus.valueOf(row.getString(1))).stream().findFirst();
    }

    private MapSqlParameterSource identity(String sourceCode, int productId) {
        return new MapSqlParameterSource("sourceCode", sourceCode).addValue("productId", (long) productId);
    }
}
