package id.ac.ui.cs.advprog.eventspherre.command;

import id.ac.ui.cs.advprog.eventspherre.model.PaymentRequest;
import id.ac.ui.cs.advprog.eventspherre.model.PaymentTransaction;
import id.ac.ui.cs.advprog.eventspherre.model.Ticket;
import id.ac.ui.cs.advprog.eventspherre.model.TicketType;
import id.ac.ui.cs.advprog.eventspherre.model.User;
import id.ac.ui.cs.advprog.eventspherre.repository.PaymentRequestRepository;
import id.ac.ui.cs.advprog.eventspherre.repository.PaymentTransactionRepository;
import id.ac.ui.cs.advprog.eventspherre.repository.UserRepository;
import id.ac.ui.cs.advprog.eventspherre.repository.TicketRepository;
import id.ac.ui.cs.advprog.eventspherre.repository.TicketTypeRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = org.mockito.quality.Strictness.LENIENT)
class AuditCommandTests {

    @Mock private PaymentTransactionRepository txRepo;
    @Mock private PaymentRequestRepository     reqRepo;
    @Mock private UserRepository               usrRepo;
    @Mock private TicketRepository             ticketRepo;
    @Mock private TicketTypeRepository         ticketTypeRepo;
    @Mock private PaymentTransaction           tx;
    @Mock private PaymentRequest               req;
    @Mock private User                         usr;

    private final UUID txId  = UUID.randomUUID();
    private final UUID reqId = UUID.randomUUID();


    private void commonStubbing(double amount,
                                int userId,
                                PaymentRequest.PaymentType type)
    {
        when(tx.getRequestId()).thenReturn(reqId);
        when(tx.getUserId()).thenReturn(userId);
        when(tx.getAmount()).thenReturn(amount);
        when(tx.getType()).thenReturn(type);
        when(txRepo.findById(txId)).thenReturn(Optional.of(tx));
        when(reqRepo.findById(reqId)).thenReturn(Optional.of(req));
        when(usrRepo.findById(userId)).thenReturn(Optional.of(usr));
    }


    @Nested
    @DisplayName("FlagFailedCommand")
    class FlagFailed {

        @Test
        @DisplayName("sets FAILED, rolls back request & deducts user (TOPUP)")
        void shouldFlagFailedAndRevertTopup() {
            commonStubbing(150.0, 1, PaymentRequest.PaymentType.TOPUP);

            new FlagFailedCommand(txId, txRepo, reqRepo, usrRepo, ticketRepo, ticketTypeRepo).execute();

            verify(tx).setStatus("FAILED");
            req.setAmount(-tx.getAmount());
            verify(req).setProcessed(false);
            verify(req).setMessage("ADMIN-FLAG: FAILED");
            verify(reqRepo).save(req);
            verify(usr).deduct(150.0);
        }

        @Test
        @DisplayName("refunds user when purchase failed")
        void shouldRefundPurchase() {
            commonStubbing(80.0, 2, PaymentRequest.PaymentType.PURCHASE);
            when(tx.getId()).thenReturn(txId);
            when(ticketRepo.findByTransactionId(txId)).thenReturn(Collections.emptyList());

            new FlagFailedCommand(txId, txRepo, reqRepo, usrRepo, ticketRepo, ticketTypeRepo).execute();

            verify(tx).setStatus("FAILED");
            req.setAmount(-tx.getAmount());
            verify(reqRepo).save(req);
            verify(usr).topUp(80.0);
        }
        @Test
        @DisplayName("restores ticket quota and deletes tickets when purchase failed")
        void shouldRestoreTicketQuotaOnFailedPurchase() {
            // Setup
            commonStubbing(100.0, 3, PaymentRequest.PaymentType.PURCHASE);
            when(tx.getId()).thenReturn(txId);
            
            // Create mock tickets and ticket types
            Ticket ticket1 = mock(Ticket.class);
            Ticket ticket2 = mock(Ticket.class);
            TicketType ticketType1 = mock(TicketType.class);
            TicketType ticketType2 = mock(TicketType.class);
            
            UUID ticketTypeId1 = UUID.randomUUID();
            UUID ticketTypeId2 = UUID.randomUUID();
            
            // Setup ticket type relationships
            when(ticket1.getTicketType()).thenReturn(ticketType1);
            when(ticket2.getTicketType()).thenReturn(ticketType2);
            when(ticketType1.getId()).thenReturn(ticketTypeId1);
            when(ticketType2.getId()).thenReturn(ticketTypeId2);
            
            // Setup current quotas
            when(ticketType1.getQuota()).thenReturn(10);
            when(ticketType2.getQuota()).thenReturn(20);
            
            // Setup repository responses
            when(ticketRepo.findByTransactionId(txId)).thenReturn(List.of(ticket1, ticket2));
            when(ticketTypeRepo.findById(ticketTypeId1)).thenReturn(Optional.of(ticketType1));
            when(ticketTypeRepo.findById(ticketTypeId2)).thenReturn(Optional.of(ticketType2));

            // Execute
            new FlagFailedCommand(txId, txRepo, reqRepo, usrRepo, ticketRepo, ticketTypeRepo).execute();

            // Verify ticket quota restoration
            verify(ticketType1).setQuota(11); // 10 + 1
            verify(ticketType2).setQuota(21); // 20 + 1
            verify(ticketTypeRepo).save(ticketType1);
            verify(ticketTypeRepo).save(ticketType2);
            
            // Verify tickets are deleted
            verify(ticketRepo).deleteByTransactionId(txId);
            
            // Verify user refund
            verify(usr).topUp(100.0);
        }

        @Test
        @DisplayName("handles multiple tickets of same type correctly")
        void shouldHandleMultipleTicketsOfSameType() {
            // Setup
            commonStubbing(200.0, 4, PaymentRequest.PaymentType.PURCHASE);
            when(tx.getId()).thenReturn(txId);
            
            // Create mock tickets of same type
            Ticket ticket1 = mock(Ticket.class);
            Ticket ticket2 = mock(Ticket.class);
            Ticket ticket3 = mock(Ticket.class);
            TicketType ticketType = mock(TicketType.class);
            
            UUID ticketTypeId = UUID.randomUUID();
            
            // All tickets belong to same type
            when(ticket1.getTicketType()).thenReturn(ticketType);
            when(ticket2.getTicketType()).thenReturn(ticketType);
            when(ticket3.getTicketType()).thenReturn(ticketType);
            when(ticketType.getId()).thenReturn(ticketTypeId);
            when(ticketType.getQuota()).thenReturn(5);
            
            // Setup repository responses
            when(ticketRepo.findByTransactionId(txId)).thenReturn(List.of(ticket1, ticket2, ticket3));
            when(ticketTypeRepo.findById(ticketTypeId)).thenReturn(Optional.of(ticketType));

            // Execute
            new FlagFailedCommand(txId, txRepo, reqRepo, usrRepo, ticketRepo, ticketTypeRepo).execute();

            // Verify quota increased by 3 (count of tickets)
            verify(ticketType).setQuota(8); // 5 + 3
            verify(ticketTypeRepo).save(ticketType);
            verify(ticketRepo).deleteByTransactionId(txId);
        }

        @Test
        @DisplayName("handles missing ticket type gracefully")
        void shouldHandleMissingTicketTypeGracefully() {
            // Setup
            commonStubbing(50.0, 5, PaymentRequest.PaymentType.PURCHASE);
            when(tx.getId()).thenReturn(txId);
            
            Ticket ticket = mock(Ticket.class);
            TicketType ticketType = mock(TicketType.class);
            UUID ticketTypeId = UUID.randomUUID();
            
            when(ticket.getTicketType()).thenReturn(ticketType);
            when(ticketType.getId()).thenReturn(ticketTypeId);
            when(ticketRepo.findByTransactionId(txId)).thenReturn(List.of(ticket));
            when(ticketTypeRepo.findById(ticketTypeId)).thenReturn(Optional.empty()); // Missing ticket type

            // Execute
            new FlagFailedCommand(txId, txRepo, reqRepo, usrRepo, ticketRepo, ticketTypeRepo).execute();

            // Verify tickets are still deleted even if ticket type not found
            verify(ticketRepo).deleteByTransactionId(txId);
            verify(usr).topUp(50.0);
            
            // Verify no save operations on missing ticket type
            verify(ticketTypeRepo, never()).save(any(TicketType.class));
        }

        @Test
        @DisplayName("handles transaction not found")
        void shouldHandleTransactionNotFound() {
            when(txRepo.findById(txId)).thenReturn(Optional.empty());

            new FlagFailedCommand(txId, txRepo, reqRepo, usrRepo, ticketRepo, ticketTypeRepo).execute();

            // Should not interact with other repositories if transaction not found
            verify(reqRepo, never()).findById(any());
            verify(usrRepo, never()).findById(any());
            verify(ticketRepo, never()).findByTransactionId(any());
        }


    }


    @Nested
    @DisplayName("HardDeleteCommand")
    class HardDelete {

        @Test
        @DisplayName("deletes tx & request, reverses user balance")
        void shouldDeleteBothEntities() {
            commonStubbing(50.0, 3, PaymentRequest.PaymentType.TOPUP);
            when(tx.getStatus()).thenReturn("SUCCESS"); // Not already failed
            when(tx.getId()).thenReturn(txId);
            when(ticketRepo.findByTransactionId(txId)).thenReturn(Collections.emptyList());

            new HardDeleteCommand(txId, txRepo, reqRepo, usrRepo, ticketRepo, ticketTypeRepo).execute();

            verify(usr).deduct(50.0);
            verify(txRepo).delete(tx);
            verify(reqRepo).delete(req);
        }
        @Test
        @DisplayName("handles already failed transaction - skips balance processing")
        void shouldSkipBalanceProcessingForFailedTransaction() {
            // Test isTransactionFinal method
            commonStubbing(100.0, 4, PaymentRequest.PaymentType.TOPUP);
            when(tx.getStatus()).thenReturn("FAILED"); // Already failed
            when(tx.getId()).thenReturn(txId);

            new HardDeleteCommand(txId, txRepo, reqRepo, usrRepo, ticketRepo, ticketTypeRepo).execute();

            // Should update request message but not process user balance
            verify(req).setMessage("ADMIN-DELETE: HARD");
            verify(reqRepo).save(req);
            verify(usr, never()).deduct(anyDouble());
            verify(usr, never()).topUp(anyDouble());
            verify(txRepo).delete(tx);
            verify(reqRepo).delete(req);
        }

        @Test
        @DisplayName("handles soft deleted transaction - skips balance processing")
        void shouldSkipBalanceProcessingForSoftDeletedTransaction() {
            // Test isTransactionFinal method
            commonStubbing(75.0, 5, PaymentRequest.PaymentType.PURCHASE);
            when(tx.getStatus()).thenReturn("SOFT_DELETED"); // Already soft deleted
            when(tx.getId()).thenReturn(txId);

            new HardDeleteCommand(txId, txRepo, reqRepo, usrRepo, ticketRepo, ticketTypeRepo).execute();

            // Should update request message but not process user balance
            verify(req).setMessage("ADMIN-DELETE: HARD");
            verify(reqRepo).save(req);
            verify(usr, never()).deduct(anyDouble());
            verify(usr, never()).topUp(anyDouble());
            verify(txRepo).delete(tx);
        }

        @Test
        @DisplayName("processes purchase refund with ticket quota restoration")
        void shouldHandlePurchaseRefundWithTicketRestore() {
            // Test handlePurchaseRefund and restoreTicketQuota methods
            commonStubbing(150.0, 6, PaymentRequest.PaymentType.PURCHASE);
            when(tx.getStatus()).thenReturn("SUCCESS");
            when(tx.getId()).thenReturn(txId);
            
            // Create mock tickets and ticket types
            Ticket ticket1 = mock(Ticket.class);
            Ticket ticket2 = mock(Ticket.class);
            TicketType ticketType1 = mock(TicketType.class);
            TicketType ticketType2 = mock(TicketType.class);
            
            UUID ticketTypeId1 = UUID.randomUUID();
            UUID ticketTypeId2 = UUID.randomUUID();
            
            // Setup ticket type relationships (tests lambda$restoreTicketQuota$3)
            when(ticket1.getTicketType()).thenReturn(ticketType1);
            when(ticket2.getTicketType()).thenReturn(ticketType2);
            when(ticketType1.getId()).thenReturn(ticketTypeId1);
            when(ticketType2.getId()).thenReturn(ticketTypeId2);
            
            // Setup current quotas
            when(ticketType1.getQuota()).thenReturn(12);
            when(ticketType2.getQuota()).thenReturn(18);
            
            // Setup repository responses
            when(ticketRepo.findByTransactionId(txId)).thenReturn(List.of(ticket1, ticket2));
            when(ticketTypeRepo.findById(ticketTypeId1)).thenReturn(Optional.of(ticketType1));
            when(ticketTypeRepo.findById(ticketTypeId2)).thenReturn(Optional.of(ticketType2));

            new HardDeleteCommand(txId, txRepo, reqRepo, usrRepo, ticketRepo, ticketTypeRepo).execute();

            // Verify ticket quota restoration (tests lambda$restoreTicketQuota$4)
            verify(ticketType1).setQuota(13); // 12 + 1
            verify(ticketType2).setQuota(19); // 18 + 1
            verify(ticketTypeRepo).save(ticketType1);
            verify(ticketTypeRepo).save(ticketType2);
            
            // Verify tickets are deleted
            verify(ticketRepo).deleteByTransactionId(txId);
            
            // Verify user refund
            verify(usr).topUp(150.0);
            
            // Verify deletion
            verify(txRepo).delete(tx);
            verify(reqRepo).delete(req);
        }

        @Test
        @DisplayName("handles multiple tickets of same type in hard delete")
        void shouldHandleMultipleTicketsOfSameTypeInHardDelete() {
            // Test lambda$restoreTicketQuota$5 (grouping and counting)
            commonStubbing(200.0, 7, PaymentRequest.PaymentType.PURCHASE);
            when(tx.getStatus()).thenReturn("SUCCESS");
            when(tx.getId()).thenReturn(txId);
            
            // Create multiple tickets of same type
            Ticket ticket1 = mock(Ticket.class);
            Ticket ticket2 = mock(Ticket.class);
            Ticket ticket3 = mock(Ticket.class);
            Ticket ticket4 = mock(Ticket.class);
            TicketType ticketType = mock(TicketType.class);
            
            UUID ticketTypeId = UUID.randomUUID();
            
            // All tickets belong to same type
            when(ticket1.getTicketType()).thenReturn(ticketType);
            when(ticket2.getTicketType()).thenReturn(ticketType);
            when(ticket3.getTicketType()).thenReturn(ticketType);
            when(ticket4.getTicketType()).thenReturn(ticketType);
            when(ticketType.getId()).thenReturn(ticketTypeId);
            when(ticketType.getQuota()).thenReturn(7);
            
            // Setup repository responses
            when(ticketRepo.findByTransactionId(txId)).thenReturn(List.of(ticket1, ticket2, ticket3, ticket4));
            when(ticketTypeRepo.findById(ticketTypeId)).thenReturn(Optional.of(ticketType));

            new HardDeleteCommand(txId, txRepo, reqRepo, usrRepo, ticketRepo, ticketTypeRepo).execute();

            // Verify quota increased by 4 (count of tickets)
            verify(ticketType).setQuota(11); // 7 + 4
            verify(ticketTypeRepo).save(ticketType);
            verify(ticketRepo).deleteByTransactionId(txId);
            verify(usr).topUp(200.0);
        }

        @Test
        @DisplayName("handles empty ticket list in hard delete")
        void shouldHandleEmptyTicketListInHardDelete() {
            // Test restoreTicketQuota with empty list
            commonStubbing(80.0, 8, PaymentRequest.PaymentType.PURCHASE);
            when(tx.getStatus()).thenReturn("SUCCESS");
            when(tx.getId()).thenReturn(txId);
            when(ticketRepo.findByTransactionId(txId)).thenReturn(Collections.emptyList());

            new HardDeleteCommand(txId, txRepo, reqRepo, usrRepo, ticketRepo, ticketTypeRepo).execute();

            // Should still refund user but not try to restore quota
            verify(usr).topUp(80.0);
            verify(ticketTypeRepo, never()).save(any(TicketType.class));
            verify(ticketRepo, never()).deleteByTransactionId(txId);
            verify(txRepo).delete(tx);
            verify(reqRepo).delete(req);
        }

        @Test
        @DisplayName("handles missing ticket type in hard delete")
        void shouldHandleMissingTicketTypeInHardDelete() {
            commonStubbing(90.0, 9, PaymentRequest.PaymentType.PURCHASE);
            when(tx.getStatus()).thenReturn("SUCCESS");
            when(tx.getId()).thenReturn(txId);
            
            Ticket ticket = mock(Ticket.class);
            TicketType ticketType = mock(TicketType.class);
            UUID ticketTypeId = UUID.randomUUID();
            
            when(ticket.getTicketType()).thenReturn(ticketType);
            when(ticketType.getId()).thenReturn(ticketTypeId);
            when(ticketRepo.findByTransactionId(txId)).thenReturn(List.of(ticket));
            when(ticketTypeRepo.findById(ticketTypeId)).thenReturn(Optional.empty()); // Missing ticket type

            new HardDeleteCommand(txId, txRepo, reqRepo, usrRepo, ticketRepo, ticketTypeRepo).execute();

            // Should still refund user and delete tickets even if ticket type not found
            verify(usr).topUp(90.0);
            verify(ticketRepo).deleteByTransactionId(txId);
            verify(ticketTypeRepo, never()).save(any(TicketType.class));
            verify(txRepo).delete(tx);
        }

        @Test
        @DisplayName("handles topup type in hard delete")
        void shouldHandleTopupTypeInHardDelete() {
            // Test processUserBalanceAndTickets with TOPUP type
            commonStubbing(120.0, 11, PaymentRequest.PaymentType.TOPUP);
            when(tx.getStatus()).thenReturn("SUCCESS");
            when(tx.getId()).thenReturn(txId);

            new HardDeleteCommand(txId, txRepo, reqRepo, usrRepo, ticketRepo, ticketTypeRepo).execute();

            // For TOPUP type, should deduct (reverse the topup)
            verify(usr).deduct(120.0);
            verify(txRepo).delete(tx);
            verify(reqRepo).delete(req);
        }

        @Test
        @DisplayName("handles transaction not found in hard delete")
        void shouldHandleTransactionNotFoundInHardDelete() {
            when(txRepo.findById(txId)).thenReturn(Optional.empty());

            new HardDeleteCommand(txId, txRepo, reqRepo, usrRepo, ticketRepo, ticketTypeRepo).execute();

            // Should not interact with other repositories if transaction not found
            verify(reqRepo, never()).findById(any());
            verify(usrRepo, never()).findById(any());
            verify(ticketRepo, never()).findByTransactionId(any());
            verify(txRepo, never()).delete(any());
        }

        @Test
        @DisplayName("handles user not found in hard delete")
        void shouldHandleUserNotFoundInHardDelete() {
            commonStubbing(85.0, 12, PaymentRequest.PaymentType.TOPUP);
            when(tx.getStatus()).thenReturn("SUCCESS");
            when(usrRepo.findById(12)).thenReturn(Optional.empty());

            new HardDeleteCommand(txId, txRepo, reqRepo, usrRepo, ticketRepo, ticketTypeRepo).execute();

            // Should still update request and delete even if user not found
            verify(req).setMessage("ADMIN-DELETE: HARD");
            verify(reqRepo).save(req);
            verify(txRepo).delete(tx);
            verify(reqRepo).delete(req);
        }

        @Test
        @DisplayName("handles request not found in hard delete")
        void shouldHandleRequestNotFoundInHardDelete() {
            commonStubbing(95.0, 13, PaymentRequest.PaymentType.TOPUP);
            when(tx.getStatus()).thenReturn("SUCCESS");
            when(reqRepo.findById(reqId)).thenReturn(Optional.empty());

            new HardDeleteCommand(txId, txRepo, reqRepo, usrRepo, ticketRepo, ticketTypeRepo).execute();

            // Should still process user balance and delete transaction
            verify(usr).deduct(95.0);
            verify(txRepo).delete(tx);
            // Should not try to delete non-existent request
            verify(reqRepo, never()).delete(any(PaymentRequest.class));
        }

        @Test
        @DisplayName("mixed ticket types with different quantities")
        void shouldHandleMixedTicketTypesWithDifferentQuantities() {
            commonStubbing(300.0, 14, PaymentRequest.PaymentType.PURCHASE);
            when(tx.getStatus()).thenReturn("SUCCESS");
            when(tx.getId()).thenReturn(txId);
            
            // Create tickets with mixed types
            Ticket ticket1 = mock(Ticket.class);
            Ticket ticket2 = mock(Ticket.class);
            Ticket ticket3 = mock(Ticket.class);
            Ticket ticket4 = mock(Ticket.class);
            Ticket ticket5 = mock(Ticket.class);
            
            TicketType typeA = mock(TicketType.class);
            TicketType typeB = mock(TicketType.class);
            
            UUID typeAId = UUID.randomUUID();
            UUID typeBId = UUID.randomUUID();
            
            // 3 tickets of type A, 2 tickets of type B
            when(ticket1.getTicketType()).thenReturn(typeA);
            when(ticket2.getTicketType()).thenReturn(typeA);
            when(ticket3.getTicketType()).thenReturn(typeA);
            when(ticket4.getTicketType()).thenReturn(typeB);
            when(ticket5.getTicketType()).thenReturn(typeB);
            
            when(typeA.getId()).thenReturn(typeAId);
            when(typeB.getId()).thenReturn(typeBId);
            when(typeA.getQuota()).thenReturn(10);
            when(typeB.getQuota()).thenReturn(5);
            
            when(ticketRepo.findByTransactionId(txId)).thenReturn(List.of(ticket1, ticket2, ticket3, ticket4, ticket5));
            when(ticketTypeRepo.findById(typeAId)).thenReturn(Optional.of(typeA));
            when(ticketTypeRepo.findById(typeBId)).thenReturn(Optional.of(typeB));

            new HardDeleteCommand(txId, txRepo, reqRepo, usrRepo, ticketRepo, ticketTypeRepo).execute();

            // Verify correct quota restoration
            verify(typeA).setQuota(13); // 10 + 3
            verify(typeB).setQuota(7);  // 5 + 2
            verify(ticketTypeRepo).save(typeA);
            verify(ticketTypeRepo).save(typeB);
            verify(ticketRepo).deleteByTransactionId(txId);
            verify(usr).topUp(300.0);
        }
    }


    @Nested
    @DisplayName("SoftDeleteCommand")
    class SoftDelete {

        @Test
        @DisplayName("sets SOFT_DELETED, marks request & deducts user")
        void shouldSoftDelete() {
            commonStubbing(30.0, 4, PaymentRequest.PaymentType.TOPUP);
            when(tx.getId()).thenReturn(txId);
            when(ticketRepo.findByTransactionId(txId)).thenReturn(Collections.emptyList());

            new SoftDeleteCommand(txId, txRepo, reqRepo, usrRepo, ticketRepo, ticketTypeRepo).execute();

            verify(tx).setStatus("SOFT_DELETED");
            req.setAmount(-tx.getAmount());
            verify(req).setProcessed(false);
            verify(req).setMessage("ADMIN-DELETE: SOFT_DELETED");
            verify(reqRepo).save(req);
            verify(usr).deduct(30.0);
        }

        @Test
        @DisplayName("refunds user and restores ticket quota for purchase")
        void shouldRefundPurchaseAndRestoreQuota() {
            commonStubbing(100.0, 5, PaymentRequest.PaymentType.PURCHASE);
            when(tx.getId()).thenReturn(txId);
            
            // Create mock tickets and ticket types
            Ticket ticket1 = mock(Ticket.class);
            Ticket ticket2 = mock(Ticket.class);
            TicketType ticketType1 = mock(TicketType.class);
            TicketType ticketType2 = mock(TicketType.class);
            
            UUID ticketTypeId1 = UUID.randomUUID();
            UUID ticketTypeId2 = UUID.randomUUID();
            
            // Setup ticket type relationships
            when(ticket1.getTicketType()).thenReturn(ticketType1);
            when(ticket2.getTicketType()).thenReturn(ticketType2);
            when(ticketType1.getId()).thenReturn(ticketTypeId1);
            when(ticketType2.getId()).thenReturn(ticketTypeId2);
            
            // Setup current quotas
            when(ticketType1.getQuota()).thenReturn(15);
            when(ticketType2.getQuota()).thenReturn(25);
            
            // Setup repository responses
            when(ticketRepo.findByTransactionId(txId)).thenReturn(List.of(ticket1, ticket2));
            when(ticketTypeRepo.findById(ticketTypeId1)).thenReturn(Optional.of(ticketType1));
            when(ticketTypeRepo.findById(ticketTypeId2)).thenReturn(Optional.of(ticketType2));

            new SoftDeleteCommand(txId, txRepo, reqRepo, usrRepo, ticketRepo, ticketTypeRepo).execute();

            // Verify ticket quota restoration (lambda$execute$2)
            verify(ticketType1).setQuota(16); // 15 + 1
            verify(ticketType2).setQuota(26); // 25 + 1
            verify(ticketTypeRepo).save(ticketType1);
            verify(ticketTypeRepo).save(ticketType2);
            
            // Verify tickets are deleted
            verify(ticketRepo).deleteByTransactionId(txId);
            
            // Verify user refund
            verify(usr).topUp(100.0);
            
            // Verify transaction and request updates
            verify(tx).setStatus("SOFT_DELETED");
            verify(req).setMessage("ADMIN-DELETE: SOFT_DELETED");
        }

        @Test
        @DisplayName("handles multiple tickets of same type correctly")
        void shouldHandleMultipleTicketsOfSameType() {
            commonStubbing(150.0, 6, PaymentRequest.PaymentType.PURCHASE);
            when(tx.getId()).thenReturn(txId);
            
            // Create mock tickets of same type
            Ticket ticket1 = mock(Ticket.class);
            Ticket ticket2 = mock(Ticket.class);
            Ticket ticket3 = mock(Ticket.class);
            TicketType ticketType = mock(TicketType.class);
            
            UUID ticketTypeId = UUID.randomUUID();
            
            // All tickets belong to same type (tests lambda$execute$1)
            when(ticket1.getTicketType()).thenReturn(ticketType);
            when(ticket2.getTicketType()).thenReturn(ticketType);
            when(ticket3.getTicketType()).thenReturn(ticketType);
            when(ticketType.getId()).thenReturn(ticketTypeId);
            when(ticketType.getQuota()).thenReturn(8);
            
            // Setup repository responses
            when(ticketRepo.findByTransactionId(txId)).thenReturn(List.of(ticket1, ticket2, ticket3));
            when(ticketTypeRepo.findById(ticketTypeId)).thenReturn(Optional.of(ticketType));

            new SoftDeleteCommand(txId, txRepo, reqRepo, usrRepo, ticketRepo, ticketTypeRepo).execute();

            // Verify quota increased by 3 (count of tickets) - tests lambda$execute$3
            verify(ticketType).setQuota(11); // 8 + 3
            verify(ticketTypeRepo).save(ticketType);
            verify(ticketRepo).deleteByTransactionId(txId);
            verify(usr).topUp(150.0);
        }

        @Test
        @DisplayName("handles missing ticket type gracefully")
        void shouldHandleMissingTicketTypeGracefully() {
            commonStubbing(75.0, 7, PaymentRequest.PaymentType.PURCHASE);
            when(tx.getId()).thenReturn(txId);
            
            Ticket ticket = mock(Ticket.class);
            TicketType ticketType = mock(TicketType.class);
            UUID ticketTypeId = UUID.randomUUID();
            
            when(ticket.getTicketType()).thenReturn(ticketType);
            when(ticketType.getId()).thenReturn(ticketTypeId);
            when(ticketRepo.findByTransactionId(txId)).thenReturn(List.of(ticket));
            when(ticketTypeRepo.findById(ticketTypeId)).thenReturn(Optional.empty()); // Missing ticket type

            new SoftDeleteCommand(txId, txRepo, reqRepo, usrRepo, ticketRepo, ticketTypeRepo).execute();

            // Verify tickets are still deleted even if ticket type not found
            verify(ticketRepo).deleteByTransactionId(txId);
            verify(usr).topUp(75.0);
            
            // Verify no save operations on missing ticket type
            verify(ticketTypeRepo, never()).save(any(TicketType.class));
            
            // Verify transaction still updated
            verify(tx).setStatus("SOFT_DELETED");
        }

        @Test
        @DisplayName("handles empty ticket list for purchase")
        void shouldHandleEmptyTicketListForPurchase() {
            commonStubbing(120.0, 8, PaymentRequest.PaymentType.PURCHASE);
            when(tx.getId()).thenReturn(txId);
            when(ticketRepo.findByTransactionId(txId)).thenReturn(Collections.emptyList());

            new SoftDeleteCommand(txId, txRepo, reqRepo, usrRepo, ticketRepo, ticketTypeRepo).execute();

            // Should still refund user even with no tickets
            verify(usr).topUp(120.0);
            verify(tx).setStatus("SOFT_DELETED");
            verify(req).setMessage("ADMIN-DELETE: SOFT_DELETED");
            
            // Should not try to delete tickets if none exist
            verify(ticketRepo, never()).deleteByTransactionId(txId);
        }

        @Test
        @DisplayName("handles transaction not found")
        void shouldHandleTransactionNotFound() {
            when(txRepo.findById(txId)).thenReturn(Optional.empty());

            new SoftDeleteCommand(txId, txRepo, reqRepo, usrRepo, ticketRepo, ticketTypeRepo).execute();

            // Should not interact with other repositories if transaction not found
            verify(reqRepo, never()).findById(any());
            verify(usrRepo, never()).findById(any());
            verify(ticketRepo, never()).findByTransactionId(any());
        }
    }


    @Test
    @DisplayName("AuditCommandInvoker calls execute() exactly once")
    void invokerShouldInvoke() {
        AuditCommand mockCmd = mock(AuditCommand.class);
        AuditCommandInvoker invoker = new AuditCommandInvoker();

        invoker.invoke(mockCmd);

        verify(mockCmd, times(1)).execute();
    }
}
