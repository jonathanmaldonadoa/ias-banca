import { CommonModule } from '@angular/common';
import { Component, Input } from '@angular/core';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatTableModule } from '@angular/material/table';
import { Cuenta } from '../../models/cuenta.model';

@Component({
  selector: 'app-cuenta-table',
  standalone: true,
  imports: [CommonModule, MatTableModule, MatProgressSpinnerModule],
  templateUrl: './cuenta-table.component.html',
  styleUrl: './cuenta-table.component.scss',
})
export class CuentaTableComponent {
  @Input() dataSource: Cuenta[] = [];
  @Input() loading = false;
  @Input() displayedColumns: string[] = ['numero', 'condicion', 'saldo', 'limiteDiario', 'moneda'];
}
