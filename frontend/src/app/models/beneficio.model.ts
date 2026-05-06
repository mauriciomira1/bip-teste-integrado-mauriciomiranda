export interface BeneficioResponse {
  id: number;
  nome: string;
  descricao: string;
  valor: number;
  ativo: boolean;
}

export interface BeneficioRequest {
  nome: string;
  descricao?: string;
  valor: number;
  ativo?: boolean;
}

export interface TransferRequest {
  fromId: number;
  toId: number;
  amount: number;
}
