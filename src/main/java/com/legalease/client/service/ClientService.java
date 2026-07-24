package com.legalease.client.service;

import com.legalease.cases.model.Case;
import com.legalease.cases.repository.CaseRepository;
import com.legalease.client.dto.ClientIntakeRequest;
import com.legalease.client.dto.ClientResponse;
import com.legalease.client.dto.ClientUpdateRequest;
import com.legalease.client.model.Client;
import com.legalease.client.repository.ClientRepository;
import com.legalease.common.exception.BadRequestException;
import com.legalease.common.exception.ResourceNotFoundException;
import com.legalease.user.model.User;
import com.legalease.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ClientService {

    private final ClientRepository clientRepository;
    private final CaseRepository caseRepository;
    private final UserRepository userRepository;

    // ── CREATE ────────────────────────────────────────────────────────────────

    @Transactional
    public ClientResponse createClient(ClientIntakeRequest request,
                                       UUID lawyerId) {
        // 1. Verify lawyer exists
        User lawyer = userRepository.findById(lawyerId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Lawyer not found"));

        // 2. Verify case exists and belongs to this lawyer
        Case linkedCase = caseRepository.findByIdAndLawyerId(
                        request.getCaseId(), lawyerId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Case not found or access denied"));

        // 3. Prevent duplicate client entry for same case
        if (clientRepository.existsByCaseIdAndClientNameIgnoreCase(
                request.getCaseId(), request.getClientName())) {
            throw new BadRequestException(
                    "Client '" + request.getClientName() +
                            "' already exists for this case");
        }

        Client client = Client.builder()
                .case_(linkedCase)
                .lawyer(lawyer)
                .clientName(request.getClientName())
                .phone(request.getPhone())
                .email(request.getEmail())
                .opposingParty(request.getOpposingParty())
                .caseBackground(request.getCaseBackground())
                .rawIntakeNote(request.getRawIntakeNote())
                .aiAssisted(request.isAiAssisted())
                .build();

        return mapToResponse(clientRepository.save(client));
    }

    // ── READ — all clients under a case ──────────────────────────────────────

    public Page<ClientResponse> getClientsByCase(UUID caseId,
                                                 UUID lawyerId,
                                                 int page,
                                                 int size) {
        // Verify case ownership before returning clients
        caseRepository.findByIdAndLawyerId(caseId, lawyerId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Case not found or access denied"));

        Pageable pageable = PageRequest.of(page, size);
        return clientRepository
                .findByCaseIdOrderByCreatedAtDesc(caseId, pageable)
                .map(this::mapToResponse);
    }

    // ── READ — all clients for lawyer (cross-case) ────────────────────────────

    public Page<ClientResponse> getAllClients(UUID lawyerId,
                                              String keyword,
                                              int page,
                                              int size) {
        Pageable pageable = PageRequest.of(page, size);

        if (keyword != null && !keyword.isBlank()) {
            return clientRepository
                    .searchByClientName(lawyerId, keyword.trim(), pageable)
                    .map(this::mapToResponse);
        }

        return clientRepository
                .findByLawyerIdOrderByCreatedAtDesc(lawyerId, pageable)
                .map(this::mapToResponse);
    }

    // ── READ ONE ──────────────────────────────────────────────────────────────

    public ClientResponse getClientById(UUID clientId, UUID lawyerId) {
        return mapToResponse(findOwnedClient(clientId, lawyerId));
    }

    // ── UPDATE ────────────────────────────────────────────────────────────────

    @Transactional
    public ClientResponse updateClient(UUID clientId,
                                       ClientUpdateRequest request,
                                       UUID lawyerId) {
        Client existing = findOwnedClient(clientId, lawyerId);

        existing.setClientName(request.getClientName());
        existing.setPhone(request.getPhone());
        existing.setEmail(request.getEmail());
        existing.setOpposingParty(request.getOpposingParty());
        existing.setCaseBackground(request.getCaseBackground());

        // aiAssisted stays as originally set — don't change after creation
        return mapToResponse(clientRepository.save(existing));
    }

    // ── DELETE ────────────────────────────────────────────────────────────────

    @Transactional
    public void deleteClient(UUID clientId, UUID lawyerId) {
        Client existing = findOwnedClient(clientId, lawyerId);
        clientRepository.delete(existing);
    }

    // ── DASHBOARD STATS ───────────────────────────────────────────────────────

    // Shows how often AI log is being used — good portfolio metric
    public long getAiAssistedCount(UUID lawyerId) {
        return clientRepository.countByLawyerIdAndAiAssistedTrue(lawyerId);
    }

    // ── PRIVATE HELPERS ───────────────────────────────────────────────────────

    private Client findOwnedClient(UUID clientId, UUID lawyerId) {
        return clientRepository.findByIdAndLawyerId(clientId, lawyerId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Client not found or access denied"));
    }

    private ClientResponse mapToResponse(Client c) {
        return ClientResponse.builder()
                .id(c.getId())
                .caseId(c.getCase_().getId())
                .caseTitle(c.getCase_().getCaseTitle())
                .lawyerId(c.getLawyer().getId())
                .lawyerName(c.getLawyer().getFullName())
                .clientName(c.getClientName())
                .phone(c.getPhone())
                .email(c.getEmail())
                .opposingParty(c.getOpposingParty())
                .caseBackground(c.getCaseBackground())
                .rawIntakeNote(c.getRawIntakeNote())
                .aiAssisted(c.isAiAssisted())
                .createdAt(c.getCreatedAt())
                .updatedAt(c.getUpdatedAt())
                .build();
    }
}