package com.banco.ias.business.mapper;

import java.text.Normalizer;
import java.util.UUID;

import org.springframework.stereotype.Component;

import com.banco.ias.domain.model.EstadoTransferencia;
import com.banco.ias.domain.model.Transferencia;
import com.banco.ias.persistence.entity.TransferenciaEntity;
import com.banco.ias.domain.dto.TransferenciaRequest;
import com.banco.ias.domain.dto.TransferenciaResponseDTO;

@Component
public class TransferenciaMapper {

    public Transferencia toDomain(TransferenciaEntity entity) {
        if (entity == null) {
            return null;
        }
        return new Transferencia(
                entity.getId(),
                entity.getRequestReference(),
                entity.getSourceAccountId(),
                entity.getDestinationAccountId(),
                entity.getAmount(),
                entity.getCurrency(),
                EstadoTransferencia.valueOf(entity.getEstado()),
                entity.getResultado(),
                entity.getObservacion(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }

    public TransferenciaEntity toEntity(Transferencia transferencia) {
        if (transferencia == null) {
            return null;
        }

        return new TransferenciaEntity(
                transferencia.id(),
                transferencia.requestReference(),
                transferencia.sourceAccountId(),
                transferencia.destinationAccountId(),
                transferencia.amount(),
                transferencia.currency(),
                transferencia.estado().name(),
                transferencia.resultado(),
                transferencia.observacion(),
                transferencia.createdAt(),
                transferencia.updatedAt()
        );
    }

    public Transferencia toDomain(TransferenciaRequest request) {
        if (request == null) {
            return null;
        }

        String requestReference = request.requestReference() == null || request.requestReference().isBlank()
                ? UUID.randomUUID().toString()
                : request.requestReference();
        return Transferencia.crear(
                requestReference,
                request.sourceAccountId(),
                request.destinationAccountId(),
                request.amount(),
                request.currency() == null ? "COP" : normalizarMoneda(request.currency())
        );
    }

    public TransferenciaResponseDTO toResponse(Transferencia transferencia) {
        if (transferencia == null) {
            return null;
        }

        return new TransferenciaResponseDTO(
                transferencia.id(),
                transferencia.requestReference(),
                transferencia.sourceAccountId(),
                transferencia.destinationAccountId(),
                transferencia.amount(),
                transferencia.currency(),
                transferencia.estado(),
                transferencia.resultado(),
                transferencia.observacion(),
                transferencia.createdAt(),
                transferencia.updatedAt()
        );
    }

    private String normalizarMoneda(String moneda) {
        String normalized = Normalizer.normalize(moneda, Normalizer.Form.NFD)
                .replaceAll("[^\\p{ASCII}]", "");
        return normalized == null || normalized.isBlank() ? "PEN" : normalized.trim().toUpperCase();
    }
}
