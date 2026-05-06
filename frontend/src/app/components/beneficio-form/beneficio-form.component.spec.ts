import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ActivatedRoute, provideRouter, Router } from '@angular/router';
import { of, throwError } from 'rxjs';
import { BeneficioFormComponent } from './beneficio-form.component';
import { BeneficioService } from '../../services/beneficio.service';
import { BeneficioResponse } from '../../models/beneficio.model';

function createFixture(idParam: string | null): {
  fixture: ComponentFixture<BeneficioFormComponent>;
  component: BeneficioFormComponent;
  serviceSpy: jasmine.SpyObj<BeneficioService>;
} {
  const serviceSpy = jasmine.createSpyObj<BeneficioService>('BeneficioService', [
    'getById',
    'create',
    'update',
  ]);
  const sample: BeneficioResponse = {
    id: 7,
    nome: 'Edit',
    descricao: 'd',
    valor: 50,
    ativo: true,
  };
  serviceSpy.getById.and.returnValue(of(sample));

  TestBed.configureTestingModule({
    imports: [BeneficioFormComponent],
    providers: [
      provideRouter([]),
      { provide: BeneficioService, useValue: serviceSpy },
      {
        provide: ActivatedRoute,
        useValue: { snapshot: { paramMap: { get: () => idParam } } },
      },
    ],
  });

  const fixture = TestBed.createComponent(BeneficioFormComponent);
  const component = fixture.componentInstance;
  fixture.detectChanges();
  return { fixture, component, serviceSpy };
}

describe('BeneficioFormComponent', () => {
  describe('modo create', () => {
    it('isEdit=false e form começa inválido', () => {
      const { component } = createFixture(null);
      expect(component.isEdit).toBeFalse();
      expect(component.form.valid).toBeFalse();
    });

    it('valida nome required e valor min 0.01', () => {
      const { component } = createFixture(null);

      component.form.patchValue({ nome: '', valor: 0 });
      expect(component.form.valid).toBeFalse();

      component.form.patchValue({ nome: 'Novo', valor: 0.01 });
      expect(component.form.valid).toBeTrue();
    });

    it('submit chama create com ativo=true e navega para /beneficios', () => {
      const { component, serviceSpy } = createFixture(null);
      const router = TestBed.inject(Router);
      const navSpy = spyOn(router, 'navigate');
      serviceSpy.create.and.returnValue(
        of({ id: 1, nome: 'Novo', descricao: '', valor: 1, ativo: true })
      );

      component.form.patchValue({ nome: 'Novo', valor: 1, descricao: '' });
      component.submit();

      expect(serviceSpy.create).toHaveBeenCalled();
      const arg = serviceSpy.create.calls.mostRecent().args[0];
      expect(arg.ativo).toBeTrue();
      expect(navSpy).toHaveBeenCalledWith(['/beneficios']);
    });
  });

  describe('modo edit', () => {
    it('isEdit=true e carrega valores via getById', () => {
      const { component, serviceSpy } = createFixture('7');
      expect(component.isEdit).toBeTrue();
      expect(serviceSpy.getById).toHaveBeenCalledWith(7);
      expect(component.form.value.nome).toBe('Edit');
      expect(component.form.value.valor).toBe(50);
    });

    it('submit chama update com o id', () => {
      const { component, serviceSpy } = createFixture('7');
      serviceSpy.update.and.returnValue(
        of({ id: 7, nome: 'Edit', descricao: 'd', valor: 50, ativo: true })
      );

      component.submit();

      expect(serviceSpy.update).toHaveBeenCalledWith(7, jasmine.any(Object));
    });

    it('exibe erro quando getById falha', () => {
      const serviceSpy = jasmine.createSpyObj<BeneficioService>(
        'BeneficioService',
        ['getById', 'create', 'update']
      );
      serviceSpy.getById.and.returnValue(throwError(() => new Error('not found')));

      TestBed.configureTestingModule({
        imports: [BeneficioFormComponent],
        providers: [
          provideRouter([]),
          { provide: BeneficioService, useValue: serviceSpy },
          {
            provide: ActivatedRoute,
            useValue: { snapshot: { paramMap: { get: () => '99' } } },
          },
        ],
      });

      const fixture = TestBed.createComponent(BeneficioFormComponent);
      fixture.detectChanges();

      expect(fixture.componentInstance.error).toContain('não encontrado');
    });
  });
});
