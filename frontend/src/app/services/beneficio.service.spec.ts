import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import {
  HttpTestingController,
  provideHttpClientTesting,
} from '@angular/common/http/testing';
import { BeneficioService } from './beneficio.service';
import {
  BeneficioRequest,
  BeneficioResponse,
  TransferRequest,
} from '../models/beneficio.model';
import { environment } from '../../environments/environment';

describe('BeneficioService', () => {
  let service: BeneficioService;
  let httpMock: HttpTestingController;
  const baseUrl = `${environment.apiUrl}/api/v1/beneficios`;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [
        BeneficioService,
        provideHttpClient(),
        provideHttpClientTesting(),
      ],
    });
    service = TestBed.inject(BeneficioService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => httpMock.verify());

  it('getAll → GET /api/v1/beneficios', () => {
    const mock: BeneficioResponse[] = [
      { id: 1, nome: 'A', descricao: 'd', valor: 10, ativo: true },
    ];

    service.getAll().subscribe((res) => expect(res).toEqual(mock));

    const req = httpMock.expectOne(baseUrl);
    expect(req.request.method).toBe('GET');
    req.flush(mock);
  });

  it('getById → GET /api/v1/beneficios/{id}', () => {
    const mock: BeneficioResponse = {
      id: 5,
      nome: 'X',
      descricao: '',
      valor: 1,
      ativo: true,
    };

    service.getById(5).subscribe((res) => expect(res).toEqual(mock));

    const req = httpMock.expectOne(`${baseUrl}/5`);
    expect(req.request.method).toBe('GET');
    req.flush(mock);
  });

  it('create → POST com body', () => {
    const body: BeneficioRequest = { nome: 'Novo', valor: 100 };
    const mock: BeneficioResponse = {
      id: 9,
      nome: 'Novo',
      descricao: '',
      valor: 100,
      ativo: true,
    };

    service.create(body).subscribe((res) => expect(res).toEqual(mock));

    const req = httpMock.expectOne(baseUrl);
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual(body);
    req.flush(mock);
  });

  it('update → PUT /api/v1/beneficios/{id} com body', () => {
    const body: BeneficioRequest = { nome: 'Edit', valor: 50 };
    const mock: BeneficioResponse = {
      id: 2,
      nome: 'Edit',
      descricao: '',
      valor: 50,
      ativo: true,
    };

    service.update(2, body).subscribe((res) => expect(res).toEqual(mock));

    const req = httpMock.expectOne(`${baseUrl}/2`);
    expect(req.request.method).toBe('PUT');
    expect(req.request.body).toEqual(body);
    req.flush(mock);
  });

  it('delete → DELETE /api/v1/beneficios/{id}', () => {
    service.delete(7).subscribe((res) => expect(res).toBeNull());

    const req = httpMock.expectOne(`${baseUrl}/7`);
    expect(req.request.method).toBe('DELETE');
    req.flush(null);
  });

  it('transfer → POST /transfer com fromId/toId/amount', () => {
    const body: TransferRequest = { fromId: 1, toId: 2, amount: 25 };

    service.transfer(body).subscribe();

    const req = httpMock.expectOne(`${baseUrl}/transfer`);
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual(body);
    req.flush(null);
  });
});
