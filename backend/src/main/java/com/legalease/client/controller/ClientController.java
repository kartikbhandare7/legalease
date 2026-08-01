package com.legalease.client.controller;

import com.legalease.client.dto.ClientIntakeRequest;
import com.legalease.client.dto.ClientResponse;
import com.legalease.client.dto.ClientUpdateRequest;
import com.legalease.client.service.ClientService;
import com.legalease.common.security.CurrentUser;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/clients")
@RequiredArgsConstructor
public class ClientController {
    private final ClientService clientService;

    @PostMapping("/intake")
    @PreAuthorize("hasRole('LAWYER')")
    public ResponseEntity<ClientResponse> createClient(
            @Valid @RequestBody ClientIntakeRequest request,
            @CurrentUser UUID lawyerId){
        return ResponseEntity.ok(clientService.createClient(request, lawyerId));
    }

    @GetMapping("/case/{caseId}")
    @PreAuthorize("hasRole('LAWYER')")
    public ResponseEntity<Page<ClientResponse>> getClientsByCaseId(
            @PathVariable UUID caseId,
            @CurrentUser UUID lawyerId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(
                clientService.getClientsByCase(caseId, lawyerId, page, size));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('LAWYER','CLERK')")
    public ResponseEntity<Page<ClientResponse>> getAllClients(
            @CurrentUser UUID lawyerId,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(clientService.getAllClients(lawyerId, keyword, page, size));
    }

    @GetMapping("/{clientId}")
    @PreAuthorize("hasAnyRole('LAWYER','CLERK')")
    public ResponseEntity<ClientResponse> getClientById(
            @PathVariable UUID clientId,
            @CurrentUser UUID lawyerId){

        return ResponseEntity.ok(clientService.getClientById(clientId, lawyerId));
    }

    @PutMapping("/{clientId}")
    @PreAuthorize("hasRole('LAWYER')")
    public ResponseEntity<ClientResponse> updateClient(
            @PathVariable UUID clientId,
            @Valid @RequestBody ClientUpdateRequest request,
            @CurrentUser UUID lawyerId) {
        return ResponseEntity.ok(clientService.updateClient(clientId, request, lawyerId));
    }
    @DeleteMapping("/{clientId}")
    @PreAuthorize("hasRole('LAWYER')")
    public ResponseEntity<Void> deleteClient(
            @PathVariable UUID clientId,
            @CurrentUser UUID lawyerId) {
        clientService.deleteClient(clientId, lawyerId);
        return ResponseEntity.noContent().build();
    }

    // Dashboard stat — AI adoption metric
    @GetMapping("/stats/ai-assisted")
    @PreAuthorize("hasRole('LAWYER')")
    public ResponseEntity<Long> getAiAssistedCount(
            @CurrentUser UUID lawyerId) {
        return ResponseEntity.ok(
                clientService.getAiAssistedCount(lawyerId));
    }
}
