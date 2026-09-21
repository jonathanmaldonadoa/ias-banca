package com.banco.ias.business.service;

import com.banco.ias.domain.dto.TransferenciaRequest;
import com.banco.ias.domain.dto.TransferenciaResponseDTO;
import com.banco.ias.domain.model.Transferencia;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface TransferenciaService {

    Mono<TransferenciaResponseDTO> registrar(TransferenciaRequest request);

    Mono<TransferenciaResponseDTO> consultarPorRequestReference(String requestReference);

    Flux<TransferenciaResponseDTO> listar();

    Flux<TransferenciaResponseDTO> consultarPorCuenta(String accountNumber);

    Flux<TransferenciaResponseDTO> eventos(String requestReference);

    Transferencia toDomain(TransferenciaRequest request);
}
