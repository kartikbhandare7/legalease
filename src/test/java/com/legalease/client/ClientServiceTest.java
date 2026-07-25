package com.legalease.client;

import com.legalease.cases.model.Case;
import com.legalease.cases.repository.CaseRepository;
import com.legalease.client.dto.ClientIntakeRequest;
import com.legalease.client.dto.ClientUpdateRequest;
import com.legalease.client.model.Client;
import com.legalease.client.repository.ClientRepository;
import com.legalease.client.service.ClientService;
import com.legalease.common.enums.CaseStatus;
import com.legalease.common.enums.CaseType;
import com.legalease.common.enums.UserRole;
import com.legalease.common.exception.BadRequestException;
import com.legalease.common.exception.ResourceNotFoundException;
import com.legalease.user.model.User;
import com.legalease.user.repository.UserRepository;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;

import java.time.LocalDateTime;
import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ClientService Tests")
class ClientServiceTest {

    @Mock private ClientRepository clientRepository;
    @Mock private CaseRepository caseRepository;
    @Mock private UserRepository userRepository;

    @InjectMocks
    private ClientService clientService;

    private UUID lawyerId;
    private UUID caseId;
    private UUID clientId;
    private User lawyer;
    private Case linkedCase;
    private Client existingClient;
    private ClientIntakeRequest validIntakeRequest;

    @BeforeEach
    void setUp() {
        lawyerId = UUID.randomUUID();
        caseId   = UUID.randomUUID();
        clientId = UUID.randomUUID();

        lawyer = User.builder()
                .id(lawyerId)
                .fullName("Adv. Rahul Sharma")
                .email("rahul@legalease.com")
                .role(UserRole.ROLE_LAWYER)
                .build();

        linkedCase = Case.builder()
                .id(caseId)
                .lawyer(lawyer)
                .caseTitle("Sharma vs State")
                .caseType(CaseType.CRIMINAL)
                .caseStatus(CaseStatus.ACTIVE)
                .build();

        existingClient = Client.builder()
                .id(clientId)
                .case_(linkedCase)
                .lawyer(lawyer)
                .clientName("John Doe")
                .phone("9876543210")
                .email("john@example.com")
                .opposingParty("Ravi Kumar")
                .caseBackground("Accused of theft in 2022")
                .rawIntakeNote("John theft case Ravi 2022")
                .aiAssisted(true)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        validIntakeRequest = new ClientIntakeRequest();
        validIntakeRequest.setCaseId(caseId);
        validIntakeRequest.setClientName("John Doe");
        validIntakeRequest.setPhone("9876543210");
        validIntakeRequest.setEmail("john@example.com");
        validIntakeRequest.setOpposingParty("Ravi Kumar");
        validIntakeRequest.setCaseBackground("Accused of theft in 2022");
        validIntakeRequest.setRawIntakeNote("John theft case Ravi 2022");
        validIntakeRequest.setAiAssisted(true);
    }

    // ── CREATE TESTS ──────────────────────────────────────────────────────────

    @Nested
    @DisplayName("Create client — intake")
    class CreateClientTests {

        @Test
        @DisplayName("Should create client successfully with AI assisted flag")
        void shouldCreateClientSuccessfully() {
            when(userRepository.findById(lawyerId))
                    .thenReturn(Optional.of(lawyer));
            when(caseRepository.findByIdAndLawyerId(caseId, lawyerId))
                    .thenReturn(Optional.of(linkedCase));
            when(clientRepository.existsByCaseIdAndClientNameIgnoreCase(
                    caseId, "John Doe"))
                    .thenReturn(false);
            when(clientRepository.save(any(Client.class)))
                    .thenReturn(existingClient);

            var response = clientService.createClient(
                    validIntakeRequest, lawyerId);

            assertThat(response.getClientName()).isEqualTo("John Doe");
            assertThat(response.isAiAssisted()).isTrue();
            assertThat(response.getOpposingParty()).isEqualTo("Ravi Kumar");
            assertThat(response.getCaseId()).isEqualTo(caseId);

            verify(clientRepository).save(any(Client.class));
        }

        @Test
        @DisplayName("Should throw ResourceNotFoundException when lawyer not found")
        void shouldThrowWhenLawyerNotFound() {
            when(userRepository.findById(lawyerId))
                    .thenReturn(Optional.empty());

            assertThatThrownBy(() ->
                    clientService.createClient(validIntakeRequest, lawyerId))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Lawyer not found");

            verify(clientRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should throw ResourceNotFoundException when case not owned")
        void shouldThrowWhenCaseNotOwnedByLawyer() {
            when(userRepository.findById(lawyerId))
                    .thenReturn(Optional.of(lawyer));
            when(caseRepository.findByIdAndLawyerId(caseId, lawyerId))
                    .thenReturn(Optional.empty());

            assertThatThrownBy(() ->
                    clientService.createClient(validIntakeRequest, lawyerId))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Case not found or access denied");

            verify(clientRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should throw BadRequestException on duplicate client name in same case")
        void shouldThrowOnDuplicateClientInSameCase() {
            when(userRepository.findById(lawyerId))
                    .thenReturn(Optional.of(lawyer));
            when(caseRepository.findByIdAndLawyerId(caseId, lawyerId))
                    .thenReturn(Optional.of(linkedCase));
            when(clientRepository.existsByCaseIdAndClientNameIgnoreCase(
                    caseId, "John Doe"))
                    .thenReturn(true);

            assertThatThrownBy(() ->
                    clientService.createClient(validIntakeRequest, lawyerId))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessageContaining("already exists for this case");

            verify(clientRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should save rawIntakeNote even when AI was not used")
        void shouldSaveRawNoteWhenManualEntry() {
            validIntakeRequest.setAiAssisted(false);
            validIntakeRequest.setRawIntakeNote("Manual note by lawyer");

            Client manualClient = Client.builder()
                    .id(UUID.randomUUID())
                    .case_(linkedCase)
                    .lawyer(lawyer)
                    .clientName("John Doe")
                    .rawIntakeNote("Manual note by lawyer")
                    .aiAssisted(false)
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .build();

            when(userRepository.findById(lawyerId))
                    .thenReturn(Optional.of(lawyer));
            when(caseRepository.findByIdAndLawyerId(caseId, lawyerId))
                    .thenReturn(Optional.of(linkedCase));
            when(clientRepository.existsByLegalCase_IdAndClientNameIgnoreCase(
                    any(), any()))
                    .thenReturn(false);
            when(clientRepository.save(any(Client.class)))
                    .thenReturn(manualClient);

            var response = clientService.createClient(
                    validIntakeRequest, lawyerId);

            assertThat(response.isAiAssisted()).isFalse();
            assertThat(response.getRawIntakeNote())
                    .isEqualTo("Manual note by lawyer");
        }
    }

    // ── READ TESTS ────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("Read clients")
    class ReadClientTests {

        @Test
        @DisplayName("Should return paginated clients for a case")
        void shouldReturnClientsByCase() {
            Page<Client> page = new PageImpl<>(List.of(existingClient));

            when(caseRepository.findByIdAndLawyerId(caseId, lawyerId))
                    .thenReturn(Optional.of(linkedCase));
            when(clientRepository.findByLegalCase_IdOrderByCreatedAtDesc(
                    eq(caseId), any(Pageable.class)))
                    .thenReturn(page);

            var result = clientService.getClientsByCase(
                    caseId, lawyerId, 0, 10);

            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getContent().get(0).getClientName())
                    .isEqualTo("John Doe");
        }

        @Test
        @DisplayName("Should search clients by name keyword")
        void shouldSearchClientsByKeyword() {
            Page<Client> page = new PageImpl<>(List.of(existingClient));

            when(clientRepository.searchByClientName(
                    eq(lawyerId), eq("john"), any(Pageable.class)))
                    .thenReturn(page);

            var result = clientService.getAllClients(
                    lawyerId, "john", 0, 10);

            assertThat(result.getContent()).hasSize(1);
            verify(clientRepository).searchByClientName(
                    eq(lawyerId), eq("john"), any());
        }

        @Test
        @DisplayName("Should throw ResourceNotFoundException for unknown clientId")
        void shouldThrowForUnknownClient() {
            when(clientRepository.findByIdAndLawyerId(any(), eq(lawyerId)))
                    .thenReturn(Optional.empty());

            assertThatThrownBy(() ->
                    clientService.getClientById(UUID.randomUUID(), lawyerId))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Client not found or access denied");
        }
    }

    // ── UPDATE TESTS ──────────────────────────────────────────────────────────

    @Nested
    @DisplayName("Update client")
    class UpdateClientTests {

        @Test
        @DisplayName("Should update client fields successfully")
        void shouldUpdateClientSuccessfully() {
            var request = new ClientUpdateRequest();
            request.setClientName("John Doe Updated");
            request.setPhone("9999999999");
            request.setOpposingParty("New Opposing Party");
            request.setCaseBackground("Updated background");

            when(clientRepository.findByIdAndLawyerId(clientId, lawyerId))
                    .thenReturn(Optional.of(existingClient));
            when(clientRepository.save(any(Client.class)))
                    .thenReturn(existingClient);

            clientService.updateClient(clientId, request, lawyerId);

            // Verify fields were mutated before save
            verify(clientRepository).save(argThat(c ->
                    c.getClientName().equals("John Doe Updated") &&
                            c.getPhone().equals("9999999999")
            ));
        }

        @Test
        @DisplayName("Should not change aiAssisted flag on update")
        void shouldNotChangeAiAssistedFlagOnUpdate() {
            var request = new ClientUpdateRequest();
            request.setClientName("John");
            request.setPhone("1234567890");

            when(clientRepository.findByIdAndLawyerId(clientId, lawyerId))
                    .thenReturn(Optional.of(existingClient));
            when(clientRepository.save(any(Client.class)))
                    .thenReturn(existingClient);

            clientService.updateClient(clientId, request, lawyerId);

            // aiAssisted was true on creation — must remain true after update
            verify(clientRepository).save(
                    argThat(c -> c.isAiAssisted()));
        }
    }

    // ── DELETE TESTS ──────────────────────────────────────────────────────────

    @Nested
    @DisplayName("Delete client")
    class DeleteClientTests {

        @Test
        @DisplayName("Should delete client when lawyer owns it")
        void shouldDeleteOwnedClient() {
            when(clientRepository.findByIdAndLawyerId(clientId, lawyerId))
                    .thenReturn(Optional.of(existingClient));

            clientService.deleteClient(clientId, lawyerId);

            verify(clientRepository).delete(existingClient);
        }

        @Test
        @DisplayName("Should throw ResourceNotFoundException when client not owned")
        void shouldThrowWhenClientNotOwned() {
            UUID anotherLawyer = UUID.randomUUID();
            when(clientRepository.findByIdAndLawyerId(clientId, anotherLawyer))
                    .thenReturn(Optional.empty());

            assertThatThrownBy(() ->
                    clientService.deleteClient(clientId, anotherLawyer))
                    .isInstanceOf(ResourceNotFoundException.class);

            verify(clientRepository, never()).delete(any());
        }
    }

    // ── STATS TESTS ───────────────────────────────────────────────────────────

    @Nested
    @DisplayName("AI stats")
    class StatsTests {

        @Test
        @DisplayName("Should return correct AI assisted intake count")
        void shouldReturnAiAssistedCount() {
            when(clientRepository.countByLawyerIdAndAiAssistedTrue(lawyerId))
                    .thenReturn(7L);

            long count = clientService.getAiAssistedCount(lawyerId);

            assertThat(count).isEqualTo(7L);
        }
    }
}