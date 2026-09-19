import { CommonModule } from '@angular/common';
import { Component, EventEmitter, Input, Output } from '@angular/core';
import { FormGroup, ReactiveFormsModule } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { TransferenciaRequestDto } from '../../models/transferencia.model';

@Component({
  selector: 'app-transferencia-form',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, MatFormFieldModule, MatInputModule, MatButtonModule],
  templateUrl: './transferencia-form.component.html',
  styleUrl: './transferencia-form.component.scss',
})
export class TransferenciaFormComponent {
  @Input({ required: true }) transferForm!: FormGroup;
  @Input() isLoading = false;

  @Output() submit = new EventEmitter<TransferenciaRequestDto>();

  submitTransfer(): void {
    if (this.transferForm.invalid) {
      this.transferForm.markAllAsTouched();
      return;
    }

    const payload: TransferenciaRequestDto = this.transferForm.getRawValue();
    this.submit.emit(payload);
  }
}
