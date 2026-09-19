import { CommonModule } from '@angular/common';
import { Component, EventEmitter, Input, Output } from '@angular/core';
import { FormGroup, ReactiveFormsModule } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';

@Component({
  selector: 'app-cuenta-search-form',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, MatFormFieldModule, MatInputModule, MatButtonModule],
  templateUrl: './cuenta-search-form.component.html',
  styleUrl: './cuenta-search-form.component.scss',
})
export class CuentaSearchFormComponent {
  @Input({ required: true }) searchForm!: FormGroup;
  @Input() isLoading = false;

  @Output() search = new EventEmitter<void>();
}
