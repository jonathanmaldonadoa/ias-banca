import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';

import { Cuenta } from '../models/cuenta.model';

@Injectable({
  providedIn: 'root',
})
export class CuentaService {
  private readonly apiUrl = 'http://localhost:8080/api/cuentas';

  constructor(private readonly httpClient: HttpClient) {}

  listar(): Observable<Cuenta[]> {
    return this.httpClient.get<Cuenta[]>(this.apiUrl);
  }

  obtenerPorNumero(numero: string): Observable<Cuenta> {
    return this.httpClient.get<Cuenta>(`${this.apiUrl}/${encodeURIComponent(numero)}`);
  }
}
