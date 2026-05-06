import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { Router, ActivatedRoute } from '@angular/router';
import { BeneficioService } from '../../services/beneficio.service';

@Component({
  selector: 'app-beneficio-form',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  template: `
    <h1>{{ isEdit ? 'Editar' : 'Novo' }} Benefício</h1>

    <div *ngIf="error" class="alert alert-error">{{ error }}</div>

    <form [formGroup]="form" (ngSubmit)="submit()" class="form">
      <div class="field">
        <label for="nome">Nome *</label>
        <input id="nome" formControlName="nome" type="text" maxlength="100" placeholder="Nome do benefício" />
        <span class="hint" *ngIf="form.get('nome')?.touched && form.get('nome')?.invalid">
          Nome é obrigatório.
        </span>
      </div>

      <div class="field">
        <label for="descricao">Descrição</label>
        <input id="descricao" formControlName="descricao" type="text" maxlength="255" placeholder="Descrição (opcional)" />
      </div>

      <div class="field">
        <label for="valor">Valor *</label>
        <input id="valor" formControlName="valor" type="number" step="0.01" min="0.01" placeholder="0,00" />
        <span class="hint" *ngIf="form.get('valor')?.touched && form.get('valor')?.invalid">
          Informe um valor maior que R$ 0,01.
        </span>
      </div>

      <div class="field field-check" *ngIf="isEdit">
        <label>
          <input formControlName="ativo" type="checkbox" />
          Ativo
        </label>
      </div>

      <div class="actions">
        <button type="submit" class="btn btn-primary" [disabled]="form.invalid || loading">
          {{ loading ? 'Salvando...' : 'Salvar' }}
        </button>
        <button type="button" class="btn btn-secondary" (click)="voltar()">Cancelar</button>
      </div>
    </form>
  `,
  styles: [`
    h1 { margin-bottom: 1.5rem; }
    .form { max-width: 480px; }
    .field { display: flex; flex-direction: column; margin-bottom: 1.25rem; }
    label { font-weight: 600; margin-bottom: 0.35rem; color: #333; font-size: 0.9rem; }
    input[type=text], input[type=number] {
      padding: 0.6rem 0.75rem;
      border: 1px solid #ccc;
      border-radius: 4px;
      font-size: 1rem;
      transition: border-color 0.2s;
    }
    input:focus { outline: none; border-color: #1a1a2e; }
    .field-check { flex-direction: row; align-items: center; gap: 0.5rem; }
    .field-check label { margin: 0; font-weight: normal; display: flex; align-items: center; gap: 0.4rem; cursor: pointer; }
    .hint { color: #c62828; font-size: 0.8rem; margin-top: 0.25rem; }
    .actions { display: flex; gap: 1rem; margin-top: 1.5rem; }
    .btn { padding: 0.6rem 1.5rem; border: none; border-radius: 4px; cursor: pointer; font-size: 0.95rem; font-weight: 500; }
    .btn-primary { background: #1a1a2e; color: white; }
    .btn-secondary { background: #888; color: white; }
    .btn:disabled { opacity: 0.55; cursor: not-allowed; }
    .alert { padding: 0.75rem 1rem; border-radius: 4px; margin-bottom: 1rem; background: #fce8e6; color: #c62828; border: 1px solid #f5c6c4; }
  `]
})
export class BeneficioFormComponent implements OnInit {
  form: FormGroup;
  isEdit = false;
  loading = false;
  error = '';
  private id?: number;

  constructor(
    private fb: FormBuilder,
    private service: BeneficioService,
    private router: Router,
    private route: ActivatedRoute
  ) {
    this.form = this.fb.group({
      nome: ['', [Validators.required, Validators.maxLength(100)]],
      descricao: ['', Validators.maxLength(255)],
      valor: [null, [Validators.required, Validators.min(0.01)]],
      ativo: [true]
    });
  }

  ngOnInit(): void {
    const idParam = this.route.snapshot.paramMap.get('id');
    if (idParam) {
      this.isEdit = true;
      this.id = +idParam;
      this.service.getById(this.id).subscribe({
        next: b => this.form.patchValue(b),
        error: () => this.error = 'Benefício não encontrado.'
      });
    }
  }

  submit(): void {
    if (this.form.invalid) return;
    this.loading = true;
    this.error = '';

    const request = {
      ...this.form.value,
      ativo: this.isEdit ? this.form.value.ativo : true
    };

    const op = this.isEdit
      ? this.service.update(this.id!, request)
      : this.service.create(request);

    op.subscribe({
      next: () => this.router.navigate(['/beneficios']),
      error: (err) => {
        this.error = err?.error?.error || 'Erro ao salvar benefício.';
        this.loading = false;
      }
    });
  }

  voltar(): void {
    this.router.navigate(['/beneficios']);
  }
}
