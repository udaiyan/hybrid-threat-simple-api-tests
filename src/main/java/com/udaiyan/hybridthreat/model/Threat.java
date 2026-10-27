package com.udaiyan.hybridthreat.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.Instant;

public class Threat {

    private Long id;

    @NotBlank
    private String name;

    @NotNull
    private ThreatType type;

    @NotBlank
    private String severity;

    @NotBlank
    private String source;

    @NotNull
    private Instant detectedAt;

    @NotBlank
    private String status;

    public Threat() {
    }

    public Threat(Long id, String name, ThreatType type, String severity,
                  String source, Instant detectedAt, String status) {
        this.id = id;
        this.name = name;
        this.type = type;
        this.severity = severity;
        this.source = source;
        this.detectedAt = detectedAt;
        this.status = status;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public ThreatType getType() { return type; }
    public void setType(ThreatType type) { this.type = type; }

    public String getSeverity() { return severity; }
    public void setSeverity(String severity) { this.severity = severity; }

    public String getSource() { return source; }
    public void setSource(String source) { this.source = source; }

    public Instant getDetectedAt() { return detectedAt; }
    public void setDetectedAt(Instant detectedAt) { this.detectedAt = detectedAt; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
