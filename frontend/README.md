# Frontend Angular

Aplicação Angular 21 standalone para o CRUD de benefícios e a feature de transferência. Consome a API do `backend-module`.

## Stack

- Angular 21 (standalone components, lazy-loaded routes)
- Reactive Forms para validação
- RxJS para fluxo assíncrono
- Karma + Jasmine para testes unitários

## Pré-requisitos

- Node.js 22+
- npm 10+
- Backend rodando em `http://localhost:8080` (`mvn -pl backend-module -am spring-boot:run` na raiz)

## Comandos

```powershell
npm ci                    # instala dependências
npm start                 # dev server em http://localhost:4200 com proxy
npm run build             # build de produção em dist/frontend
npm test                  # roda testes em watch mode (Karma + Chrome)
npm run test:ci           # roda testes uma única vez em ChromeHeadless
```

## Configuração de ambiente

A URL base da API fica em `src/environments/`:

- `environment.ts` (produção): `apiUrl: 'http://localhost:8080'` — ajustar para a URL real do backend.
- `environment.development.ts` (dev): `apiUrl: ''` — usa caminho relativo para o `proxy.conf.json` redirecionar `/api` ao backend.

O `angular.json` aplica `fileReplacements` no build de desenvolvimento (`ng serve` / `ng build --configuration=development`). O build padrão (produção) usa `environment.ts`.

## Arquitetura

```text
src/app/
├── app.config.ts              # providers (HttpClient + Router)
├── app.routes.ts              # rotas lazy-loaded
├── components/
│   ├── beneficio-list/        # tela de listagem com inativação (soft delete)
│   ├── beneficio-form/        # formulário de criar/editar
│   └── transfer/              # tela de transferência entre benefícios
├── models/
│   └── beneficio.model.ts     # DTOs (BeneficioRequest, BeneficioResponse, TransferRequest)
└── services/
    └── beneficio.service.ts   # cliente HTTP da API
```

## API consumida

Base: `${environment.apiUrl}/api/v1/beneficios`

| Método | Endpoint           | Uso                                              |
|--------|--------------------|--------------------------------------------------|
| GET    | `/`                | Lista benefícios ativos                          |
| GET    | `/{id}`            | Busca por ID                                     |
| POST   | `/`                | Cria benefício                                   |
| PUT    | `/{id}`            | Atualiza benefício                               |
| DELETE | `/{id}`            | Soft delete (inativa)                            |
| POST   | `/transfer`        | Transfere saldo (`fromId`, `toId`, `amount`)     |

Os DTOs do frontend (`models/beneficio.model.ts`) espelham os DTOs do backend (`backend-module/src/main/java/com/example/backend/dto/`).

## Fluxo da Transferência

1. Usuário acessa `/transferir` (botão **Transferir** na listagem).
2. Componente carrega benefícios via `getAll()` para popular os selects de origem/destino.
3. Validações no formulário: campos obrigatórios + `amount >= 0.01`.
4. Validação no submit: `fromId !== toId`.
5. Erros do backend (ex.: saldo insuficiente — `IllegalArgumentException` retornando 400) são exibidos no alerta `.alert-error` via `err.error.error`.
6. Em sucesso, os saldos são recarregados e o formulário é resetado.

## Testes

Os testes ficam em `*.spec.ts` ao lado de cada arquivo testado:

- `services/beneficio.service.spec.ts` — cobre todos os 6 endpoints com `HttpTestingController`.
- `components/beneficio-list/beneficio-list.component.spec.ts` — render, fluxos de erro, inativar, link "Transferir".
- `components/beneficio-form/beneficio-form.component.spec.ts` — modos create/edit, validações, fluxo de erro.
- `components/transfer/transfer.component.spec.ts` — validações, regras de submit, mensagens.

Para rodar:

```powershell
npm run test:ci
```

Requer Chrome instalado para o `ChromeHeadlessNoSandbox`.

## Observações

- A listagem usa `confirm()` nativo antes de inativar — adequado ao escopo, em produção troque por modal.
- `beneficio-list` e `transfer` usam `BehaviorSubject` para refresh + `timeout(8000)` para falha rápida.
- Soft delete: o backend marca `ativo=false`; o método é exposto como `delete()` no service por convenção REST, mas é rotulado como "Inativar" na UI.

Por Maurício Miranda
