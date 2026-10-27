package com.udaiyan.hybridthreat.controller;

import com.udaiyan.hybridthreat.model.Threat;
import com.udaiyan.hybridthreat.model.ThreatType;
import com.udaiyan.hybridthreat.repository.ThreatRepository;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/threats")
public class ThreatController {

    private final ThreatRepository repository;

    public ThreatController(ThreatRepository repository) {
        this.repository = repository;
    }

    @GetMapping
    public List<Threat> list(@RequestParam(required = false) ThreatType type) {
        return type == null ? repository.findAll() : repository.findByType(type);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Threat> get(@PathVariable long id) {
        return repository.findById(id)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<Threat> create(@Valid @RequestBody Threat threat) {
        long id = repository.insert(threat);
        Threat created = repository.findById(id).orElseThrow();
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
            .path("/{id}").buildAndExpand(id).toUri();
        return ResponseEntity.created(location).body(created);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Threat> update(@PathVariable long id,
                                          @Valid @RequestBody Threat threat) {
        if (repository.update(id, threat) == 0) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(repository.findById(id).orElseThrow());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable long id) {
        if (repository.delete(id) == 0) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.noContent().build();
    }
}
