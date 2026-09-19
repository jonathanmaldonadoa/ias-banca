import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';

import {
  TransferenciaRequestDto,
  TransferenciaResponseDto,
} from '../models/transferencia.model';

@Injectable({
  providedIn: 'root',
})
export class TransferenciaService {
  private readonly apiUrl = 'http://localhost:8080/api/transferencias';

  constructor(private readonly httpClient: HttpClient) {}

  listar(): Observable<TransferenciaResponseDto[]> {
    return this.httpClient.get<TransferenciaResponseDto[]>(this.apiUrl);
  }

  consultarPorRequestReference(requestReference: string): Observable<TransferenciaResponseDto> {
    return this.httpClient.get<TransferenciaResponseDto>(
      `${this.apiUrl}/request/${encodeURIComponent(requestReference)}`,
    );
  }

  consultarPorCuenta(numeroCuenta: string): Observable<TransferenciaResponseDto[]> {
    return this.httpClient.get<TransferenciaResponseDto[]>(
      `${this.apiUrl}/cuenta/${encodeURIComponent(numeroCuenta)}`,
    );
  }

  registrar(request: TransferenciaRequestDto): Observable<TransferenciaResponseDto> {
    return this.httpClient.post<TransferenciaResponseDto>(this.apiUrl, request);
  }
}
