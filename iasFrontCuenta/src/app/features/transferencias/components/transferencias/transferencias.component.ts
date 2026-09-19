import { CommonModule } from '@angular/common';
import { Component, OnInit, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { MatTableModule } from '@angular/material/table';

import {
  TransferenciaRequestDto,
  TransferenciaResponseDto,
} from '../../models/transferencia.model';
import { TransferenciaService } from '../../services/transferencia.service';
import { TransferenciaFormComponent } from '../transferencia-form/transferencia-form.component';
import { TransferenciaReferenceFormComponent } from '../transferencia-reference-form/transferencia-reference-form.component';
import { TransferenciaTableComponent } from '../transferencia-table/transferencia-table.component';

@Component({
  selector: 'app-transferencias',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    MatCardModule,
    MatTableModule,
    MatFormFieldModule,
    MatInputModule,
    MatButtonModule,
    MatProgressSpinnerModule,
    MatSnackBarModule,
    TransferenciaFormComponent,
    TransferenciaReferenceFormComponent,
    TransferenciaTableComponent,
  ],
  templateUrl: './transferencias.component.html',
  styleUrl: './transferencias.component.scss',
})
export class TransferenciasComponent implements OnInit {
  readonly transferencias = signal<TransferenciaResponseDto[]>([]);
  readonly historialCuenta = signal<TransferenciaResponseDto[]>([]);
  readonly transferenciaEncontrada = signal<TransferenciaResponseDto | null>(null);
  readonly isLoading = signal<boolean>(false);
  readonly transferColumns = ['requestReference', 'sourceAccountId', 'destinationAccountId', 'amount', 'currency', 'estado'];

  private readonly transferenciaService = inject(TransferenciaService);
  private readonly formBuilder = inject(FormBuilder);
  private readonly snackBar = inject(MatSnackBar);

  readonly transferForm = this.formBuilder.nonNullable.group({
    requestReference: ['', Validators.required],
    sourceAccountId: ['', Validators.required],
    destinationAccountId: ['', Validators.required],
    amount: [0, [Validators.required, Validators.min(0.01)]],
    currency: ['COP', Validators.required],
  });

  readonly accountHistoryForm = this.formBuilder.nonNullable.group({
    numeroCuenta: ['', Validators.required],
  });

  readonly referenceForm = this.formBuilder.nonNullable.group({
    requestReference: ['', Validators.required],
  });

  ngOnInit(): void {
    this.cargarTransferencias();
  }

  cargarTransferencias(): void {
    this.isLoading.set(true);

    this.transferenciaService.listar().subscribe({
      next: (transferencias: TransferenciaResponseDto[]) => {
        this.transferencias.set(transferencias);
        this.isLoading.set(false);
      },
      error: () => {
        this.isLoading.set(false);
        this.snackBar.open('No se pudo cargar el historial de transferencias.', 'Cerrar', {
          duration: 4000,
        });
      },
    });
  }

  registrarTransferencia(payload?: TransferenciaRequestDto): void {
    const transferenciaRequest: TransferenciaRequestDto = payload ?? {
      requestReference: this.transferForm.controls.requestReference.value.trim(),
      sourceAccountId: this.transferForm.controls.sourceAccountId.value.trim(),
      destinationAccountId: this.transferForm.controls.destinationAccountId.value.trim(),
      amount: Number(this.transferForm.controls.amount.value),
      currency: this.transferForm.controls.currency.value.trim(),
    };

    if (this.transferForm.invalid || !transferenciaRequest.requestReference || !transferenciaRequest.sourceAccountId || !transferenciaRequest.destinationAccountId || !transferenciaRequest.currency) {
      this.transferForm.markAllAsTouched();
      this.snackBar.open('Completa todos los campos requeridos.', 'Cerrar', { duration: 3000 });
      return;
    }

    this.isLoading.set(true);

    this.transferenciaService.registrar(transferenciaRequest).subscribe({
      next: (transferencia: TransferenciaResponseDto) => {
        this.transferenciaEncontrada.set(transferencia);
        this.isLoading.set(false);
        this.transferForm.reset({
          requestReference: '',
          sourceAccountId: '',
          destinationAccountId: '',
          amount: 0,
          currency: 'COP',
        });
        this.cargarTransferencias();
        this.snackBar.open(`Transferencia ${transferencia.requestReference} registrada.`, 'Cerrar', {
          duration: 3000,
        });
      },
      error: () => {
        this.isLoading.set(false);
        this.snackBar.open('La transferencia no pudo procesarse. Revisa reglas de negocio.', 'Cerrar', {
          duration: 4000,
        });
      },
    });
  }

  consultarPorReferencia(reference?: string): void {
    const requestReference = (reference ?? this.referenceForm.controls.requestReference.value).trim();

    if (!requestReference) {
      this.snackBar.open('Debes indicar la referencia de solicitud.', 'Cerrar', { duration: 3000 });
      return;
    }

    this.isLoading.set(true);

    this.transferenciaService.consultarPorRequestReference(requestReference).subscribe({
      next: (transferencia: TransferenciaResponseDto) => {
        this.transferenciaEncontrada.set(transferencia);
        this.isLoading.set(false);
        this.snackBar.open(`Referencia encontrada: ${transferencia.requestReference}`, 'Cerrar', {
          duration: 3000,
        });
      },
      error: () => {
        this.transferenciaEncontrada.set(null);
        this.isLoading.set(false);
        this.snackBar.open('No existe una transferencia con esa referencia.', 'Cerrar', {
          duration: 4000,
        });
      },
    });
  }

  consultarPorCuenta(): void {
    const numeroCuenta = this.accountHistoryForm.controls.numeroCuenta.value.trim();

    if (!numeroCuenta) {
      this.snackBar.open('Debes indicar el número de cuenta.', 'Cerrar', { duration: 3000 });
      return;
    }

    this.isLoading.set(true);

    this.transferenciaService.consultarPorCuenta(numeroCuenta).subscribe({
      next: (transferencias: TransferenciaResponseDto[]) => {
        this.historialCuenta.set(transferencias);
        this.isLoading.set(false);
      },
      error: () => {
        this.historialCuenta.set([]);
        this.isLoading.set(false);
        this.snackBar.open('No se pudo consultar el historial de la cuenta.', 'Cerrar', {
          duration: 4000,
        });
      },
    });
  }
}
