import { ComponentFixture, TestBed, fakeAsync, tick } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { of, throwError } from 'rxjs';
import { BeneficioListComponent } from './beneficio-list.component';
import { BeneficioService } from '../../services/beneficio.service';
import { BeneficioResponse } from '../../models/beneficio.model';

describe('BeneficioListComponent', () => {
  let fixture: ComponentFixture<BeneficioListComponent>;
  let component: BeneficioListComponent;
  let serviceSpy: jasmine.SpyObj<BeneficioService>;

  const sample: BeneficioResponse[] = [
    { id: 1, nome: 'A', descricao: 'desc A', valor: 100, ativo: true },
    { id: 2, nome: 'B', descricao: 'desc B', valor: 200, ativo: false },
  ];

  beforeEach(async () => {
    serviceSpy = jasmine.createSpyObj<BeneficioService>('BeneficioService', [
      'getAll',
      'delete',
    ]);
    serviceSpy.getAll.and.returnValue(of(sample));

    await TestBed.configureTestingModule({
      imports: [BeneficioListComponent],
      providers: [
        provideRouter([]),
        { provide: BeneficioService, useValue: serviceSpy },
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(BeneficioListComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('renderiza lista de benefícios', () => {
    const rows = fixture.nativeElement.querySelectorAll('tbody tr');
    expect(rows.length).toBe(2);
    expect(rows[0].textContent).toContain('A');
  });

  it('exibe mensagem amigável quando getAll falha (sem URL)', fakeAsync(() => {
    serviceSpy.getAll.and.returnValue(
      throwError(() => ({ status: 500, url: 'http://localhost:8080/api/v1/beneficios' }))
    );

    component.load();
    tick();
    fixture.detectChanges();

    const alert = fixture.nativeElement.querySelector('.alert-error');
    expect(alert).toBeTruthy();
    expect(alert.textContent).toContain('Não foi possível carregar');
    expect(alert.textContent).not.toContain('localhost');
    expect(alert.textContent).not.toContain('http');
  }));

  it('inativar chama delete e recarrega quando confirmado', () => {
    spyOn(window, 'confirm').and.returnValue(true);
    serviceSpy.delete.and.returnValue(of(void 0));

    component.inativar(1);

    expect(serviceSpy.delete).toHaveBeenCalledWith(1);
    expect(serviceSpy.getAll).toHaveBeenCalledTimes(2);
  });

  it('inativar não chama delete quando cancelado', () => {
    spyOn(window, 'confirm').and.returnValue(false);

    component.inativar(1);

    expect(serviceSpy.delete).not.toHaveBeenCalled();
  });

  it('renderiza link "Transferir" para /transferir', () => {
    const link: HTMLAnchorElement | null = fixture.nativeElement.querySelector(
      'a[href="/transferir"]'
    );
    expect(link).toBeTruthy();
    expect(link!.textContent).toContain('Transferir');
  });
});
