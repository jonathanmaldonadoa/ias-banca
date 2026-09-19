import { CommonModule } from '@angular/common';
import { Component, EventEmitter, Input, Output } from '@angular/core';
import { FormGroup, ReactiveFormsModule } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';

@Component({
  selector: 'app-transferencia-reference-form',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, MatFormFieldModule, MatInputModule, MatButtonModule],
  templateUrl: './transferencia-reference-form.component.html',
  styleUrl: './transferencia-reference-form.component.scss',
})
export class TransferenciaReferenceFormComponent {
  @Input({ required: true }) referenceForm!: FormGroup;
  @Input() isLoading = false;

  @Output() search = new EventEmitter<string>();

  submitQuery(): void {
    if (this.referenceForm.invalid) {
      this.referenceForm.markAllAsTouched();
      return;
    }

    const reference = this.referenceForm.get('requestReference')?.value?.trim();
    this.search.emit(reference);
  }
}
