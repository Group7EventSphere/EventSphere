package id.ac.ui.cs.advprog.eventspherre.mapper;

import id.ac.ui.cs.advprog.eventspherre.dto.PaymentRequestDTO;
import id.ac.ui.cs.advprog.eventspherre.model.PaymentRequest;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class PaymentRequestMapper {

    public PaymentRequestDTO toDto(PaymentRequest entity) {
        return new PaymentRequestDTO(
            entity.getId(),
            entity.getUserId(),
            entity.getAmount(),
            entity.getType().name(),
            entity.isProcessed(),
            entity.getMessage(),
            entity.getCreatedAt()
        );
    }

    public List<PaymentRequestDTO> toDtoList(List<PaymentRequest> list) {
        return list.stream()
                   .map(this::toDto)
                   .toList();
    }

    public PaymentRequest toEntity(PaymentRequestDTO dto) {
        return PaymentRequest.builder()
            .id(dto.id())
            .userId(dto.userId())
            .amount(dto.amount())
            .type(PaymentRequest.PaymentType.valueOf(dto.type()))
            .processed(dto.processed())
            .message(dto.message())
            .createdAt(dto.createdAt())
            .build();
    }
}
