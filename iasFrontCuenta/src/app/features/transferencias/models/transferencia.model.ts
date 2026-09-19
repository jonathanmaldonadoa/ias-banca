export type TransferenciaEstado = 'PENDIENTE' | 'APROBADA' | 'RECHAZADA' | 'DUPLICADA';

export interface TransferenciaRequestDto {
  requestReference: string;
  sourceAccountId: string;
  destinationAccountId: string;
  amount: number;
  currency: string;
}

export interface TransferenciaResponseDto {
  id: string;
  requestReference: string;
  sourceAccountId: string;
  destinationAccountId: string;
  amount: number;
  currency: string;
  estado: TransferenciaEstado;
  resultado: string;
  observacion: string;
  createdAt: string;
  updatedAt: string;
}
