import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';

import { environment } from '../../../../environments/environment';
import {
  TransferenciaRequestDto,
  TransferenciaResponseDto,
} from '../models/transferencia.model';

@Injectable({
  providedIn: 'root',
})
export class TransferenciaService {
  private readonly apiUrl = `${environment.apiUrl}/transferencias`;

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

  escucharEstado(requestReference: string): Observable<TransferenciaResponseDto> {
    return new Observable<TransferenciaResponseDto>((subscriber) => {
      const source = new EventSource(
        `${this.apiUrl}/request/${encodeURIComponent(requestReference)}/eventos`,
      );

      source.onmessage = (event) => subscriber.next(JSON.parse(event.data) as TransferenciaResponseDto);
      source.onerror = () => {
        source.close();
        subscriber.complete();
      };

      return () => source.close();
    });
  }
}
