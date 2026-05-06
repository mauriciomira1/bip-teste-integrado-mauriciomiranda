import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideRouter, Router } from '@angular/router';
import { of, throwError } from 'rxjs';
import { TransferComponent } from './transfer.component';
import { BeneficioService } from '../../services/beneficio.service';
import { BeneficioResponse } from '../../models/beneficio.model';

describe('TransferComponent', () => {
  let fixture: ComponentFixture<TransferComponent>;
  let component: TransferComponent;
  let serviceSpy: jasmine.SpyObj<BeneficioService>;

  const sample: BeneficioResponse[] = [
    { id: 1, nome: 'A', descricao: '', valor: 1000, ativo: true },
    { id: 2, nome: 'B', descricao: '', valor: 500, ativo: true },
  ];

  beforeEach(async () => {
    serviceSpy = jasmine.createSpyObj<BeneficioService>('BeneficioService', [
      'getAll',
      'transfer',
    ]);
    serviceSpy.getAll.and.returnValue(of(sample));

    await TestBed.configureTestingModule({
      imports: [TransferComponent],
      providers: [
        provideRouter([]),
        { provide: BeneficioService, useValue: serviceSpy },
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(TransferComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('formulário começa inválido (campos obrigatórios)', () => {
    expect(component.form.valid).toBeFalse();
  });

  it('valida amount min 0.01', () => {
    component.form.patchValue({ fromId: 1, toId: 2, amount: 0 });
    expect(component.form.valid).toBeFalse();

    component.form.patchValue({ amount: 0.01 });
    expect(component.form.valid).toBeTrue();
  });

  it('bloqueia origem == destino mesmo com form válido', () => {
    component.form.patchValue({ fromId: 1, toId: 1, amount: 10 });
    component.submit();

    expect(component.error).toContain('diferentes');
    expect(serviceSpy.transfer).not.toHaveBeenCalled();
  });

  it('submit feliz envia fromId/toId/amount e exibe sucesso', () => {
    serviceSpy.transfer.and.returnValue(of(void 0));
    component.form.patchValue({ fromId: 1, toId: 2, amount: 100 });

    component.submit();

    expect(serviceSpy.transfer).toHaveBeenCalledWith({
      fromId: 1,
      toId: 2,
      amount: 100,
    });
    expect(component.success).toContain('sucesso');
    expect(component.loading).toBeFalse();
  });

  it('submit erro do backend é exibido', () => {
    serviceSpy.transfer.and.returnValue(
      throwError(() => ({ error: { error: 'Saldo insuficiente' } }))
    );
    component.form.patchValue({ fromId: 1, toId: 2, amount: 100 });

    component.submit();

    expect(component.error).toBe('Saldo insuficiente');
    expect(component.loading).toBeFalse();
  });

  it('exibe mensagem amigável quando getAll falha (sem URL)', () => {
    serviceSpy.getAll.and.returnValue(
      throwError(() => ({ status: 500, url: 'http://localhost:8080/api/v1/beneficios' }))
    );

    const fresh = TestBed.createComponent(TransferComponent);
    fresh.componentInstance.beneficios$.subscribe();

    expect(fresh.componentInstance.error).toContain('Não foi possível carregar');
    expect(fresh.componentInstance.error).not.toContain('localhost');
  });

  it('voltar navega para /beneficios', () => {
    const router = TestBed.inject(Router);
    const navSpy = spyOn(router, 'navigate');

    component.voltar();

    expect(navSpy).toHaveBeenCalledWith(['/beneficios']);
  });
});
