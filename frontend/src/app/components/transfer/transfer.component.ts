import { Component, inject } from '@angular/core';
import { CommonModule, CurrencyPipe } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { BehaviorSubject, Observable, catchError, of, switchMap, tap, timeout } from 'rxjs';
import { BeneficioService } from '../../services/beneficio.service';
import { BeneficioResponse } from '../../models/beneficio.model';

@Component({
  selector: 'app-transfer',
  standalone: true,
  imports: [CommonModule, CurrencyPipe, ReactiveFormsModule],
  template: `
    <h1>Transferir entre Benefícios</h1>

    <div *ngIf="error" class="alert alert-error">{{ error }}</div>
    <div *ngIf="success" class="alert alert-success">{{ success }}</div>

    <ng-container *ngIf="beneficios$ | async as beneficios; else loadingBeneficios">
    <form [formGroup]="form" (ngSubmit)="submit()" class="form">
      <div class="field">
        <label for="fromId">Origem *</label>
        <select id="fromId" formControlName="fromId">
          <option [ngValue]="null">Selecione o benefício de origem...</option>
          <option *ngFor="let b of beneficios" [ngValue]="b.id">
            {{ b.nome }} — {{ b.valor | currency:'BRL' }}
          </option>
        </select>
      </div>

      <div class="field">
        <label for="toId">Destino *</label>
        <select id="toId" formControlName="toId">
          <option [ngValue]="null">Selecione o benefício de destino...</option>
          <option *ngFor="let b of beneficios" [ngValue]="b.id">
            {{ b.nome }} — {{ b.valor | currency:'BRL' }}
          </option>
        </select>
      </div>

      <div class="field">
        <label for="amount">Valor *</label>
        <input id="amount" formControlName="amount" type="number" step="0.01" min="0.01" placeholder="0,00" />
        <span class="hint" *ngIf="form.get('amount')?.touched && form.get('amount')?.invalid">
          Informe um valor maior que R$ 0,01.
        </span>
      </div>

      <div class="actions">
        <button type="submit" class="btn btn-primary" [disabled]="form.invalid || loading">
          {{ loading ? 'Transferindo...' : 'Transferir' }}
        </button>
        <button type="button" class="btn btn-secondary" (click)="voltar()">Cancelar</button>
      </div>
    </form>
    </ng-container>

    <ng-template #loadingBeneficios>
      <p class="empty">Carregando benefícios...</p>
    </ng-template>
  `,
  styles: [`
    h1 { margin-bottom: 1.5rem; }
    .form { max-width: 480px; }
    .field { display: flex; flex-direction: column; margin-bottom: 1.25rem; }
    label { font-weight: 600; margin-bottom: 0.35rem; color: #333; font-size: 0.9rem; }
    select, input[type=number] {
      padding: 0.6rem 0.75rem;
      border: 1px solid #ccc;
      border-radius: 4px;
      font-size: 1rem;
      background: white;
      transition: border-color 0.2s;
    }
    select:focus, input:focus { outline: none; border-color: #1a1a2e; }
    .hint { color: #c62828; font-size: 0.8rem; margin-top: 0.25rem; }
    .actions { display: flex; gap: 1rem; margin-top: 1.5rem; }
    .btn { padding: 0.6rem 1.5rem; border: none; border-radius: 4px; cursor: pointer; font-size: 0.95rem; font-weight: 500; }
    .btn-primary { background: #1a1a2e; color: white; }
    .btn-secondary { background: #888; color: white; }
    .btn:disabled { opacity: 0.55; cursor: not-allowed; }
    .alert { padding: 0.75rem 1rem; border-radius: 4px; margin-bottom: 1rem; }
    .alert-error { background: #fce8e6; color: #c62828; border: 1px solid #f5c6c4; }
    .alert-success { background: #e6f4ea; color: #2e7d32; border: 1px solid #c3e6cb; }
    .empty { color: #999; text-align: center; padding: 3rem; }
  `]
})
export class TransferComponent {
  private readonly fb = inject(FormBuilder);
  private readonly service = inject(BeneficioService);
  private readonly router = inject(Router);
  private readonly refresh$ = new BehaviorSubject<void>(undefined);

  form: FormGroup;
  loading = false;
  error = '';
  success = '';

  beneficios$: Observable<BeneficioResponse[]> = this.refresh$.pipe(
    tap(() => this.error = ''),
    switchMap(() =>
      this.service.getAll().pipe(
        timeout(8000),
        catchError(err => {
          console.error('Erro ao carregar benefícios para transferência:', err);
          this.error = 'Não foi possível carregar os benefícios. Tente novamente.';
          return of([]);
        })
      )
    )
  );

  constructor() {
    this.form = this.fb.group({
      fromId: [null, Validators.required],
      toId: [null, Validators.required],
      amount: [null, [Validators.required, Validators.min(0.01)]]
    });
  }

  submit(): void {
    if (this.form.invalid) return;
    const { fromId, toId, amount } = this.form.value;
    if (+fromId === +toId) {
      this.error = 'Origem e destino devem ser benefícios diferentes.';
      return;
    }
    this.loading = true;
    this.error = '';
    this.success = '';
    this.service.transfer({ fromId: +fromId, toId: +toId, amount: +amount }).subscribe({
      next: () => {
        this.success = 'Transferência realizada com sucesso!';
        this.loading = false;
        this.form.reset();
        this.refresh$.next();
      },
      error: (err) => {
        this.error = err?.error?.error || 'Erro ao realizar transferência.';
        this.loading = false;
      }
    });
  }

  voltar(): void {
    this.router.navigate(['/beneficios']);
  }
}
