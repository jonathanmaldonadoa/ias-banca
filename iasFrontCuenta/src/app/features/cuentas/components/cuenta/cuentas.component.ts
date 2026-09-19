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

import { Cuenta } from '../../models/cuenta.model';
import { CuentaService } from '../../services/cuenta.service';
import { CuentaSearchFormComponent } from '../cuenta-search-form/cuenta-search-form.component';
import { CuentaTableComponent } from '../cuenta-table/cuenta-table.component';

@Component({
  selector: 'app-cuentas',
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
    CuentaSearchFormComponent,
    CuentaTableComponent,
  ],
  templateUrl: './cuentas.component.html',
  styleUrl: './cuentas.component.scss',
})
export class CuentasComponent implements OnInit {
  readonly cuentas = signal<Cuenta[]>([]);
  readonly cuentaSeleccionada = signal<Cuenta | null>(null);
  readonly isLoading = signal<boolean>(false);
  readonly displayedColumns = ['numero', 'condicion', 'saldo', 'limiteDiario', 'moneda'];

  private readonly cuentaService = inject(CuentaService);
  private readonly formBuilder = inject(FormBuilder);
  private readonly snackBar = inject(MatSnackBar);

  readonly searchForm = this.formBuilder.nonNullable.group({
    numero: ['', [Validators.required, Validators.minLength(3)]],
  });

  ngOnInit(): void {
    this.cargarCuentas();
  }

  cargarCuentas(): void {
    this.isLoading.set(true);

    this.cuentaService.listar().subscribe({
      next: (cuentas: Cuenta[]) => {
        this.cuentas.set(cuentas);
        this.isLoading.set(false);
      },
      error: () => {
        this.isLoading.set(false);
        this.snackBar.open('No se pudo cargar el listado de cuentas.', 'Cerrar', {
          duration: 4000,
        });
      },
    });
  }

  buscarCuenta(): void {
    const numero = this.searchForm.controls.numero.value.trim();

    if (!numero) {
      this.snackBar.open('Debes indicar el número de cuenta.', 'Cerrar', { duration: 3000 });
      return;
    }

    this.isLoading.set(true);

    this.cuentaService.obtenerPorNumero(numero).subscribe({
      next: (cuenta: Cuenta) => {
        this.cuentaSeleccionada.set(cuenta);
        this.isLoading.set(false);
        this.snackBar.open(`Cuenta ${cuenta.numero} consultada correctamente.`, 'Cerrar', {
          duration: 3000,
        });
      },
      error: () => {
        this.cuentaSeleccionada.set(null);
        this.isLoading.set(false);
        this.snackBar.open('La cuenta no existe o no se pudo consultar.', 'Cerrar', {
          duration: 4000,
        });
      },
    });
  }
}
