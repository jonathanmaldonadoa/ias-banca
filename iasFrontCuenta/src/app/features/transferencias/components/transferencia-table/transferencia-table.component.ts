import { CommonModule } from '@angular/common';
import { Component, Input } from '@angular/core';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatTableModule } from '@angular/material/table';
import { TransferenciaResponseDto } from '../../models/transferencia.model';

@Component({
  selector: 'app-transferencia-table',
  standalone: true,
  imports: [CommonModule, MatTableModule, MatProgressSpinnerModule],
  templateUrl: './transferencia-table.component.html',
  styleUrl: './transferencia-table.component.scss',
})
export class TransferenciaTableComponent {
  @Input() dataSource: TransferenciaResponseDto[] = [];
  @Input() loading = false;
  @Input() displayedColumns: string[] = ['requestReference', 'sourceAccountId', 'destinationAccountId', 'amount', 'currency', 'estado'];
}
