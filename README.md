# BIP Teste Integrado

Solução fullstack em camadas para o desafio descrito em `docs/README.md`.

## Arquitetura

- `db/`: scripts SQL de schema e carga inicial.
- `ejb-module/`: EJB responsável pela regra crítica de transferência, com validação de saldo, transação e lock pessimista.
- `backend-module/`: API Spring Boot com CRUD de benefícios e integração explícita com `BeneficioEjbService`.
- `frontend/`: aplicação Angular standalone consumindo a API.
- `.github/workflows/`: CI validando EJB, backend e build frontend.

## Executando Localmente

Pré-requisitos: Java 17+, Maven 3.9+, Node.js 22+ e npm.

```powershell
mvn verify
cd frontend
npm ci
npm run build
```

Para subir a API:

```powershell
mvn -pl backend-module -am spring-boot:run
```

Para subir o frontend:

```powershell
cd frontend
npm start
```

A API usa H2 em memória e carrega automaticamente `backend-module/src/main/resources/schema.sql` e `data.sql`. Os scripts equivalentes para execução manual ficam em `db/schema.sql` e `db/seed.sql`.

## API

Base URL: `http://localhost:8080/api/v1/beneficios`

- `GET /api/v1/beneficios`: lista benefícios ativos.
- `GET /api/v1/beneficios/{id}`: busca por ID.
- `POST /api/v1/beneficios`: cria benefício.
- `PUT /api/v1/beneficios/{id}`: atualiza benefício.
- `DELETE /api/v1/beneficios/{id}`: inativa benefício.
- `POST /api/v1/beneficios/transfer`: transfere saldo entre benefícios.

Swagger UI: `http://localhost:8080/swagger-ui.html`

## Decisões Técnicas

- A transferência é delegada pelo backend ao `BeneficioEjbService`, evitando duplicação da regra crítica.
- O EJB aplica validações antes de bloquear registros, ordena os locks por ID para reduzir risco de deadlock e usa `PESSIMISTIC_WRITE`.
- O backend mantém CRUD em Spring Data JPA e usa soft delete por meio do campo `ativo`.
- A coluna `VERSION` habilita controle otimista nas entidades JPA, enquanto a transferência usa lock pessimista por ser operação financeira crítica.

## Testes

```powershell
mvn -f ejb-module test
mvn -f ejb-module install
mvn -f backend-module verify
cd frontend
npm run build
```

O comando `mvn verify` na raiz executa EJB e backend no reactor Maven e é o caminho usado pelo CI.
