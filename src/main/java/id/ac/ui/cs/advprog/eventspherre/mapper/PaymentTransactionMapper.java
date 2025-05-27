package id.ac.ui.cs.advprog.eventspherre.mapper;

import id.ac.ui.cs.advprog.eventspherre.dto.PaymentTransactionDTO;
import id.ac.ui.cs.advprog.eventspherre.model.PaymentTransaction;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class PaymentTransactionMapper {
    public PaymentTransactionDTO toDto(PaymentTransaction tx) {
        return new PaymentTransactionDTO(
                tx.getId(),
                tx.getUserId(),
                tx.getAmount(),
                tx.getType().name(),
                tx.getStatus(),
                tx.getCreatedAt()
        );
    }

    public List<PaymentTransactionDTO> toDtoList(List<PaymentTransaction> list) {
        return list.stream().map(this::toDto).toList();
    }
}