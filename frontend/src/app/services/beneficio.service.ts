import { Injectable } from "@angular/core";
import { HttpClient } from "@angular/common/http";
import { Observable } from "rxjs";
import {
  BeneficioRequest,
  BeneficioResponse,
  TransferRequest,
} from "../models/beneficio.model";
import { environment } from "../../environments/environment";

@Injectable({ providedIn: "root" })
export class BeneficioService {
  private readonly apiUrl = `${environment.apiUrl}/api/v1/beneficios`;

  constructor(private http: HttpClient) {}

  getAll(): Observable<BeneficioResponse[]> {
    return this.http.get<BeneficioResponse[]>(this.apiUrl);
  }

  getById(id: number): Observable<BeneficioResponse> {
    return this.http.get<BeneficioResponse>(`${this.apiUrl}/${id}`);
  }

  create(request: BeneficioRequest): Observable<BeneficioResponse> {
    return this.http.post<BeneficioResponse>(this.apiUrl, request);
  }

  update(id: number, request: BeneficioRequest): Observable<BeneficioResponse> {
    return this.http.put<BeneficioResponse>(`${this.apiUrl}/${id}`, request);
  }

  /* Criei como delete para atender ao CRUD solicitado, mas na prática seria mais viável arquivar pra não perder dados */
  delete(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }

  transfer(request: TransferRequest): Observable<void> {
    return this.http.post<void>(`${this.apiUrl}/transfer`, request);
  }
}
