import { Component, inject } from "@angular/core";
import { CommonModule, CurrencyPipe } from "@angular/common";
import { RouterLink } from "@angular/router";
import {
  BehaviorSubject,
  Observable,
  catchError,
  of,
  switchMap,
  tap,
  timeout,
} from "rxjs";
import { BeneficioService } from "../../services/beneficio.service";
import { BeneficioResponse } from "../../models/beneficio.model";

@Component({
  selector: "app-beneficio-list",
  standalone: true,
  imports: [CommonModule, CurrencyPipe, RouterLink],
  template: `
    <div class="header">
      <h1>Benefícios</h1>
      <div class="header-actions">
        <a routerLink="/transferir" class="btn btn-secondary">Transferir</a>
        <a routerLink="/beneficios/novo" class="btn btn-primary">+ Novo</a>
      </div>
    </div>

    <div *ngIf="error" class="alert alert-error">{{ error }}</div>
    <div *ngIf="success" class="alert alert-success">{{ success }}</div>

    <ng-container *ngIf="beneficios$ | async as beneficios; else loadingTpl">
      <table *ngIf="beneficios.length > 0; else emptyTpl">
        <thead>
          <tr>
            <th>ID</th>
            <th>Nome</th>
            <th>Descrição</th>
            <th>Valor</th>
            <th>Ativo</th>
            <th>Ações</th>
          </tr>
        </thead>
        <tbody>
          <tr *ngFor="let b of beneficios">
            <td>{{ b.id }}</td>
            <td>{{ b.nome }}</td>
            <td>{{ b.descricao || "-" }}</td>
            <td>{{ b.valor | currency: "BRL" }}</td>
            <td>
              <span [class]="b.ativo ? 'badge badge-ok' : 'badge badge-off'">
                {{ b.ativo ? "Sim" : "Não" }}
              </span>
            </td>
            <td class="actions">
              <a
                [routerLink]="['/beneficios/editar', b.id]"
                class="btn btn-sm btn-secondary"
                >Editar</a
              >
              <button
                (click)="inativar(b.id)"
                class="btn btn-sm btn-danger"
                [disabled]="!b.ativo"
              >
                Inativar
              </button>
            </td>
          </tr>
        </tbody>
      </table>
    </ng-container>

    <ng-template #loadingTpl>
      <p class="empty">Carregando benefícios...</p>
    </ng-template>

    <ng-template #emptyTpl>
      <p class="empty">Nenhum benefício encontrado.</p>
    </ng-template>
  `,
  styles: [
    `
      .header {
        display: flex;
        justify-content: space-between;
        align-items: center;
        margin-bottom: 1.5rem;
      }
      .header-actions {
        display: flex;
        gap: 0.5rem;
      }
      h1 {
        margin: 0;
      }
      table {
        width: 100%;
        border-collapse: collapse;
        background: white;
        border-radius: 6px;
        overflow: hidden;
        box-shadow: 0 1px 4px rgba(0, 0, 0, 0.1);
      }
      th,
      td {
        padding: 0.75rem 1rem;
        text-align: left;
        border-bottom: 1px solid #eee;
      }
      th {
        background: #f8f8f8;
        font-weight: 600;
        color: #444;
        font-size: 0.85rem;
        text-transform: uppercase;
      }
      tbody tr:hover {
        background: #fafafa;
      }
      .actions {
        display: flex;
        gap: 0.5rem;
      }
      .empty {
        color: #999;
        text-align: center;
        padding: 3rem;
      }
      .badge {
        padding: 0.2rem 0.6rem;
        font-size: 0.8rem;
        font-weight: 600;
      }
      .badge-ok {
        color: #2e7d32;
      }
      .badge-off {
        color: #c62828;
      }
      .alert {
        padding: 0.75rem 1rem;
        border-radius: 4px;
        margin-bottom: 1rem;
      }
      .alert-error {
        background: #fce8e6;
        color: #c62828;
        border: 1px solid #f5c6c4;
      }
      .alert-success {
        background: #e6f4ea;
        color: #2e7d32;
        border: 1px solid #c3e6cb;
      }
      .btn {
        padding: 0.5rem 1rem;
        border: none;
        border-radius: 4px;
        cursor: pointer;
        text-decoration: none;
        display: inline-block;
        font-size: 0.875rem;
        font-weight: 500;
      }
      .btn-primary {
        background: #1a1a2e;
        color: white;
      }
      .btn-secondary {
        background: #555;
        color: white;
      }
      .btn-danger {
        background: #c62828;
        color: white;
      }
      .btn-sm {
        padding: 0.3rem 0.7rem;
        font-size: 0.8rem;
      }
      .btn:disabled {
        opacity: 0.5;
        cursor: not-allowed;
      }
    `,
  ],
})
export class BeneficioListComponent {
  private readonly service = inject(BeneficioService);
  private readonly refresh$ = new BehaviorSubject<void>(undefined);

  error = "";
  success = "";

  beneficios$: Observable<BeneficioResponse[]> = this.refresh$.pipe(
    tap(() => (this.error = "")),
    switchMap(() =>
      this.service.getAll().pipe(
        timeout(8000),
        catchError((err) => {
          console.error("Erro ao carregar benefícios:", err);
          this.error = "Não foi possível carregar os benefícios. Tente novamente.";
          return of([]);
        }),
      ),
    ),
  );

  load(): void {
    this.refresh$.next();
  }

  inativar(id: number): void {
    if (!confirm("Confirma a inativação deste benefício?")) return;
    this.success = "";
    this.service.delete(id).subscribe({
      next: () => {
        this.success = "Benefício inativado com sucesso.";
        this.refresh$.next();
      },
      error: () => (this.error = "Erro ao inativar benefício."),
    });
  }
}
