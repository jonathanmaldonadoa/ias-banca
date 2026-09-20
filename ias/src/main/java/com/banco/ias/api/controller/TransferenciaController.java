package com.banco.ias.api.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.banco.ias.business.service.TransferenciaService;
import com.banco.ias.domain.dto.TransferenciaRequest;
import com.banco.ias.domain.dto.TransferenciaResponseDTO;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import org.springframework.http.codec.ServerSentEvent;

@RestController
@RequestMapping("/api/transferencias")
@Tag(name = "Transferencias", description = "Operaciones de consulta y registro de transferencias bancarias")
public class TransferenciaController {

    private final TransferenciaService transferenciaService;

    public TransferenciaController(TransferenciaService transferenciaService) {
        this.transferenciaService = transferenciaService;
    }

    @GetMapping
    @Operation(
        summary = "Listar transferencias",
        description = "Devuelve todas las transferencias registradas en el sistema, en orden de actualización reciente.",
        responses = {
            @ApiResponse(responseCode = "200", description = "Listado obtenido correctamente",
                content = @Content(mediaType = "application/json", schema = @Schema(implementation = TransferenciaResponseDTO.class)))
        }
    )
    public Flux<TransferenciaResponseDTO> listar() {
        return transferenciaService.listar();
    }

    @GetMapping("/request/{requestReference}")
    @Operation(
        summary = "Consultar transferencia por requestReference",
        description = "Busca una transferencia usando la referencia de solicitud enviada por el cliente. Sirve para controlar referencias duplicadas.",
        responses = {
            @ApiResponse(responseCode = "200", description = "Transferencia encontrada"),
            @ApiResponse(responseCode = "404", description = "No existe una transferencia con esa referencia")
        }
    )
    public Mono<ResponseEntity<TransferenciaResponseDTO>> consultarPorRequestReference(@PathVariable String requestReference) {
        return transferenciaService.consultarPorRequestReference(requestReference)
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @GetMapping("/cuenta/{numeroCuenta}")
    @Operation(
        summary = "Consultar historial de movimientos por cuenta",
        description = "Devuelve todos los movimientos asociados a una cuenta específica, tanto como origen como destino.",
        responses = {
            @ApiResponse(responseCode = "200", description = "Historial de la cuenta obtenido correctamente",
                content = @Content(mediaType = "application/json", schema = @Schema(implementation = TransferenciaResponseDTO.class)))
        }
    )
    public Flux<TransferenciaResponseDTO> consultarPorCuenta(@PathVariable String numeroCuenta) {
        return transferenciaService.consultarPorCuenta(numeroCuenta);
    }

    @GetMapping(value = "/request/{requestReference}/eventos",
            produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ServerSentEvent<TransferenciaResponseDTO>> eventos(
            @PathVariable String requestReference) {
        return transferenciaService.eventos(requestReference)
                .map(data -> ServerSentEvent.builder(data).build());
    }

    @PostMapping
    @Operation(
        summary = "Registrar transferencia",
        description = "Procesa una transferencia nueva validando reglas de negocio, límites diarios y referencias repetidas.",
        responses = {
            @ApiResponse(responseCode = "202", description = "Transferencia recibida y pendiente de procesamiento",
                content = @Content(mediaType = "application/json", schema = @Schema(implementation = TransferenciaResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Validación de negocio fallida")
        }
    )
    public Mono<ResponseEntity<TransferenciaResponseDTO>> registrar(@RequestBody TransferenciaRequest request) {
        return transferenciaService.registrar(request)
            .map(t -> ResponseEntity.status(HttpStatus.ACCEPTED).body(t));
    }
}
