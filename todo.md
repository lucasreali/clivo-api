# TODO — Clivo API

Backlog de construção em fases. Cada tarefa é pequena o bastante para ser entregue ao agente isoladamente e validada antes da próxima.

Pacote raiz: `com.example.clivoapi`
Build: Gradle Kotlin DSL · Java 21 · Spring Boot 4.1.x · Spring Modulith 2.1.x · PostgreSQL 16

---

## Checklist geral

**Fase 0 — Fundação**
- [X] 0.1 Configurar o build
- [X] 0.2 Aplicar o schema físico
- [X] 0.3 Contexto de inquilino
- [X] 0.4 Auditoria
- [X] 0.5 Tratamento de erro
- [X] 0.6 Módulos do Spring Modulith

**Fase 1 — Configuração**
- [X] 1.1 Ativação de módulos (mecanismo A)
- [X] 1.2 Porta de consulta de módulo ativo
- [X] 1.3 Guarda de rota por módulo
- [X] 1.4 Recurso de capacidades
- [X] 1.5 Parâmetros da clínica (mecanismo C)
- [X] 1.6 Modelo de ficha (mecanismo B) — só a estrutura

**Fase 2 — Núcleo: cadastros**
- [ ] 2.1 Acesso
- [ ] 2.2 Cliente
- [ ] 2.3 Profissional e disponibilidade
- [ ] 2.4 Catálogo de serviços

**Fase 3 — Agenda e a corrente**
- [ ] 3.1 Bloqueio de agenda
- [ ] 3.2 Agendamento
- [ ] 3.3 **Chain** — validação de agendamento
- [ ] 3.4 **Chain** — validação de ativação de módulo
- [ ] 3.5 Painel do dia

**Fase 4 — Atendimento e ficha**
- [ ] 4.1 Atendimento
- [ ] 4.2 **Abstract Factory** — montagem da ficha
- [ ] 4.3 Componentes especiais
- [ ] 4.4 Histórico do cliente

**Fase 5 — Financeiro**
- [ ] 5.1 Cobrança
- [ ] 5.2 Pagamento
- [ ] 5.3 Ponto de extensão do atendimento

**Fase 6 — Módulos**
- [ ] 6.1 Dependentes
- [ ] 6.2 Estoque
- [ ] 6.3 Lote e validade *(exige 6.2)*
- [ ] 6.4 **Strategy** — política de lote vencido
- [ ] 6.5 **Strategy** — política de acesso por perfil
- [ ] 6.6 Pacotes de sessões
- [ ] 6.7 Convênios
- [ ] 6.8 Notificações
- [ ] 6.9 Comissionamento

**Fase 7 — Verificação**
- [ ] 7.1 Prova de reuso (`ReuseTest`)
- [ ] 7.2 Testes de arquitetura
- [ ] 7.3 Cenários

**Entrega da atividade de padrões**
- [ ] 3 padrões codificados: Chain, Abstract Factory, Strategy
- [ ] 2 exemplos de cada, todos funcionais e ligados ao núcleo
- [ ] 14 classes de domínio com métodos de negócio
- [ ] `ReuseTest` verde nos 3 métodos
- [ ] `ModularityTest` e `ArchitectureTest` verdes

---

## Princípios — valem para todas as tarefas

**Legibilidade acima de performance.** Sempre. Se houver escolha entre uma consulta esperta e um método que se lê de primeira, escolha o segundo. Otimização só entra com número medido mostrando que precisa.

**Nenhum comentário no código.** Se um trecho precisa de comentário para ser entendido, ele precisa de um nome melhor ou de ser quebrado em dois métodos. A única exceção é `package-info.java`, que carrega anotação e não comentário.

**Código, pacotes, classes e métodos em inglês.** Sem exceção.

**Métodos curtos e nomes que dizem a intenção.** `appointment.overlaps(other)` no lugar de `appointment.getStart().isBefore(other.getEnd()) && ...` espalhado pelo serviço.

**Regra de negócio mora na entidade, não no serviço.** O serviço orquestra, busca, salva e coordena transação. Quem sabe se um lote está vencido é o lote.

**O núcleo não conhece os módulos.** `core` nunca importa `modules`. Quando o núcleo precisa acionar comportamento de módulo, declara uma interface em `common/extension` e injeta `List<Interface>`. Módulo inativo não registra bean e simplesmente não está na lista.

**Módulo desativado não deixa vestígio.** Endpoint responde 404, nunca 403. O recurso de capacidades não lista o módulo. Nada de campo nulo ou atributo vazio na resposta.

**O Hibernate nunca gera schema.** `ddl-auto: validate` e Flyway. O banco usa `EXCLUDE USING gist`, índices parciais e extensões que o Hibernate não sabe criar.

**Repositórios e implementações ficam em `internal`.** Só o pacote raiz de cada módulo é API pública.

---

# FASE 0 — Fundação

Objetivo: a aplicação sobe, conecta no banco, aplica o schema e isola inquilinos automaticamente. Nada de domínio ainda.

### [X] 0.1 · Configurar o build

**Objetivo:** dependências resolvidas e build verde.

Adicionar ao `build.gradle.kts`: web, data-jpa, validation, security, flyway-core, flyway-database-postgresql, postgresql, lombok, spring-modulith-starter-core, spring-modulith-starter-jpa, spring-modulith-docs, spring-boot-starter-test, spring-security-test, spring-boot-testcontainers, testcontainers-postgresql, spring-modulith-starter-test.

**Como:** o Spring Boot não gerencia a versão do Modulith. Declarar `extra["springModulithVersion"]` e importar o BOM em `dependencyManagement`.

**Não use H2.** O schema depende de recursos exclusivos do PostgreSQL.

**Pronto quando:** `./gradlew build` passa.

### [X] 0.2 · Aplicar o schema físico

**Objetivo:** o banco existe e o Hibernate valida contra ele.

Colar o SQL físico do projeto em `src/main/resources/db/migration/V1__schema.sql`. Configurar `application.yaml` com `ddl-auto: validate` e Flyway habilitado. Criar perfis `dev` e `test`.

**Pronto quando:** a aplicação sobe contra um Postgres local e o Flyway registra a migração.

### [X] 0.3 · Contexto de inquilino

**Objetivo:** toda consulta filtra por clínica sem ninguém escrever `where tenant_id = ?`.

Criar em `common/tenant`: `Tenant` (entidade, tabela `tenant`), `TenantContext` (guarda o inquilino da requisição), `TenantResolutionFilter` (lê da sessão e popula o contexto), e a configuração do Hibernate para multi-tenancy por discriminador.

**Como:** o Hibernate tem a anotação `@TenantId`. Aplicada no campo `tenant` de uma entidade, ele injeta o filtro em toda consulta e preenche o valor ao inserir. Não escreva o filtro à mão.

**Pronto quando:** existe um teste que grava duas entidades em inquilinos diferentes e comprova que a consulta de um não alcança a do outro.

### [X] 0.4 · Auditoria

**Objetivo:** toda entidade registra quem criou e quem alterou, sem repetir código.

Criar em `common/audit`: `AuditableEntity` como `@MappedSuperclass` com `createdAt`, `createdBy`, `updatedAt`, `updatedBy`, e a configuração de auditoria do Spring Data.

**Como:** `@EnableJpaAuditing` mais um `AuditorAware` que lê o usuário autenticado.

**Pronto quando:** uma entidade qualquer que estenda `AuditableEntity` grava os quatro campos sozinha.

### [X] 0.5 · Tratamento de erro

**Objetivo:** erro de negócio vira resposta HTTP coerente, em um lugar só.

Criar em `common/exception`: `BusinessException`, `ResourceNotFoundException`, `ErrorResponse` e um `@RestControllerAdvice`.

**Pronto quando:** lançar `BusinessException` em qualquer serviço produz 422 com corpo padronizado, e `ResourceNotFoundException` produz 404.

### [X] 0.6 · Módulos do Spring Modulith

**Objetivo:** as fronteiras entre pacotes viram regra verificável.

Criar a árvore de pacotes vazia com `package-info.java` anotado em cada módulo. `common` é `@ApplicationModule(type = OPEN)`. Os demais declaram `allowedDependencies` explicitamente.

Criar `ModularityTest` chamando `ApplicationModules.of(...).verify()` e gerando documentação com `Documenter`.

**Pronto quando:** `./gradlew test --tests "*ModularityTest"` passa.

---

# FASE 1 — Configuração: os três mecanismos

Objetivo: o sistema já sabe o que cada clínica contratou e como ela está configurada. Vem antes do domínio porque quase tudo depende disso.

### [X] 1.1 · Ativação de módulos (mecanismo A)

**Objetivo:** ligar e desligar funcionalidade por clínica, validando dependências.

Criar em `configuration/modules`: `ModuleActivation` (entidade), `ModuleActivationService`, repositório e controller em `internal`.

Módulos do catálogo: `dependent`, `sessionpackage`, `inventory`, `batch` (exige `inventory`), `insurance`, `notification`, `commission`.

**Como:** a dependência entre módulos fica declarada em dados, não em código. Ativar `batch` sem `inventory` é recusado na gravação.

**Pronto quando:** existe endpoint para ativar e desativar, e tentar ativar `batch` sem `inventory` retorna 422.

### [X] 1.2 · Porta de consulta de módulo ativo

**Objetivo:** qualquer parte do sistema pergunta "este módulo está ativo?" sem depender do pacote de configuração.

Criar `ModuleActivationState` em `common/extension` com um método que responde se um módulo está ativo na clínica corrente. Implementar em `configuration/modules/internal`.

**Pronto quando:** um serviço qualquer consegue perguntar sem importar `configuration`.

### [X] 1.3 · Guarda de rota por módulo

**Objetivo:** cumprir a regra de "sem vestígio" nos endpoints.

Criar a anotação `@RequiresModule` em `common/extension` e um interceptor que a lê.

**Como:** o interceptor consulta `ModuleActivationState`. Módulo inativo → **404**, nunca 403. Um 403 revelaria que a funcionalidade existe.

**Pronto quando:** um controller anotado responde 404 numa clínica sem o módulo e 200 numa clínica com ele.

### [X] 1.4 · Recurso de capacidades

**Objetivo:** o cliente da API descobre o que aquela clínica tem, sem adivinhar.

Criar um endpoint que devolve a lista de módulos ativos e os parâmetros vigentes da clínica corrente.

**Como:** módulo inativo simplesmente não aparece na lista. Nada de `"inventory": false`.

**Pronto quando:** duas clínicas com configurações diferentes recebem respostas diferentes.

### [X] 1.5 · Parâmetros da clínica (mecanismo C)

**Objetivo:** ajustar o valor de uma regra sem tocar em código.

Criar em `configuration/parameter`: `ClinicParameter` (entidade com chave, valor, tipo e faixa válida), `ClinicParameterService`, repositório e controller em `internal`. Criar a porta `ClinicParameters` em `common/extension`.

Catálogo mínimo: `role_model` (SEGREGATED ou SINGLE), `reschedule_window_hours` (1 a 168), `reminder_lead_hours` (1 a 72), `block_expired_batch` (booleano), `expiry_alert_days` (7 a 180).

**Como:** valor fora da faixa é recusado na gravação, não descoberto em produção. O valor vigente vale para as operações seguintes sem exigir novo login.

**Pronto quando:** gravar 200 em `reminder_lead_hours` retorna 422 informando os limites, e o valor anterior permanece.

### [X] 1.6 · Modelo de ficha (mecanismo B) — só a estrutura

**Objetivo:** guardar a definição da ficha clínica como dado.

Criar em `configuration/template`: `RecordTemplate`, `TemplateSection`, `TemplateField`, com serviço, repositórios e controller. Versionamento: publicar gera versão nova e preserva as anteriores.

**Como:** `TemplateField` tem `code`, `label`, `fieldType` (string, não enum), `required`, `validationRules` em JSON e ordem. O tipo ser string é o que permite acrescentar tipo novo sem recompilar.

**Não implemente a renderização agora.** Isso é a Fase 4.

**Pronto quando:** é possível montar e publicar um modelo com seções e campos, e alterar um publicado gera versão 2 sem apagar a 1.

---

# FASE 2 — Núcleo: cadastros

Objetivo: as entidades que todo o resto referencia.

### [ ] 2.1 · Acesso

Criar em `core/access`: `AppUser`, `AccessService`, repositório e configuração de segurança em `internal`.

Métodos de negócio: `isActive()`, `hasRole(Role)`.

**Como:** senha com hash. A sessão carrega identidade, clínica e perfil. Perfis: `RECEPTION`, `PRACTITIONER`, `ASSISTANT`, `MANAGER`, `PLATFORM_ADMIN`. Administrador de plataforma não pertence a clínica nenhuma.

**Pronto quando:** login funciona e a sessão resolve o inquilino do filtro da Fase 0.3.

### [ ] 2.2 · Cliente

Criar em `core/customer`: `Customer`, `ConsentRecord`, serviço, repositório e controller.

Métodos: `deactivate(reason)`, `hasValidConsent()`.

**Como:** cliente com atendimento é **inativado com motivo obrigatório**, nunca removido. Documento é único por clínica.

**Pronto quando:** cadastrar, buscar, alterar e inativar funcionam, e tentar inativar sem motivo retorna 422.

### [ ] 2.3 · Profissional e disponibilidade

Criar em `core/practitioner`: `Practitioner`, `AvailabilitySlot`, serviço, repositórios e controller.

Métodos: `worksAt(DayOfWeek, LocalTime)`, `deactivate()`.

**Como:** `AvailabilitySlot` guarda dia da semana e faixa de horário. Faixas do mesmo profissional não podem se sobrepor.

**Pronto quando:** é possível definir a agenda semanal de um profissional e consultar se ele atende num horário.

### [ ] 2.4 · Catálogo de serviços

Criar em `core/catalog`: `Service` (tabela `service`), `CatalogService`, repositório e controller.

Método: `endTimeFrom(LocalDateTime)`.

**Como:** a duração do serviço define o intervalo ocupado na agenda. A classe Spring chama-se `CatalogService` para não virar `ServiceService`.

**Pronto quando:** cadastrar serviço com duração e valor funciona, e a duração calcula o horário final corretamente.

---

# FASE 3 — Agenda e a corrente de validação

Objetivo: o coração do sistema. É aqui que entra o primeiro padrão.

### [ ] 3.1 · Bloqueio de agenda

Criar em `core/scheduling`: `ScheduleBlock`, repositório e endpoints.

Métodos: `covers(LocalDateTime)`, `appliesTo(Practitioner)`.

**Como:** profissional nulo significa bloqueio da clínica inteira.

**Pronto quando:** é possível registrar um bloqueio com motivo e consultá-lo por período.

### [ ] 3.2 · Agendamento

Criar em `core/scheduling`: `Appointment`, `AppointmentStatus`, `SchedulingService`, repositório e controller.

Métodos: `overlaps(Appointment)`, `canBeRescheduled(int windowHours)`, `cancel(reason)`, `checkIn()`.

**Como:** a sobreposição também é garantida por constraint no banco. O método na entidade existe para dar mensagem boa ao usuário antes de tentar gravar.

**Pronto quando:** criar, reagendar e cancelar funcionam.

### [ ] 3.3 · Chain of Responsibility — validação de agendamento

**Objetivo do padrão:** acrescentar uma regra de validação nova deve exigir **criar uma classe e nada mais**. Nenhum arquivo existente pode ser editado.

Criar em `patterns/chain`: a interface `AppointmentValidator` com um método que recebe o agendamento e ou lança exceção de recusa, ou retorna. Elos: `AvailabilityValidator`, `ScheduleBlockValidator`, `OverlapValidator`, `RescheduleWindowValidator`.

**Como:** o serviço injeta `List<AppointmentValidator>` e o Spring monta a corrente sozinho, na ordem de `@Order`. Nada de cada elo guardar referência ao próximo. Nada de lista construída à mão.

**Importante:** `SchedulingService` **delega** à corrente. Ele não pode ter validação inline nenhuma dessas quatro regras. Se houver duplicata, apague a do serviço.

**Pronto quando:** as quatro recusas retornam mensagens distintas, e existe um teste que acrescenta um elo novo definido dentro do próprio teste e prova que ele entra na corrente sem alterar classe de produção.

### [ ] 3.4 · Chain — validação de ativação de módulo

Criar em `patterns/chain`: `ModuleActivationValidator` com os elos `ModuleDependencyValidator` e `ExistingDataValidator`.

**Como:** mesma mecânica do 3.3. `ModuleActivationService` passa a delegar.

**Pronto quando:** ativar `batch` sem `inventory` e desativar `inventory` com `batch` ativo são ambos recusados pela corrente.

### [ ] 3.5 · Painel do dia

Criar em `core/scheduling`: endpoint que lista os agendamentos do dia com situação, e registro de chegada e de falta.

**Como:** ordenar por horário. Chegada muda a situação e coloca o cliente na fila de espera.

**Pronto quando:** o painel devolve os agendamentos do dia e a chegada atualiza a situação.

---

# FASE 4 — Atendimento e a ficha configurável

Objetivo: o motor que faz uma clínica odontológica e uma veterinária usarem a mesma tela.

### [ ] 4.1 · Atendimento

Criar em `core/encounter`: `Encounter`, `EncounterStatus`, serviço, repositório e controller.

Métodos: `complete()`, `isCompleted()`.

**Como:** o conteúdo preenchido vai em JSON, com `@JdbcTypeCode(SqlTypes.JSON)` sobre `Map<String, Object>`. Não adicione biblioteca externa; o Hibernate 6.2+ faz isso nativamente. O atendimento guarda a **versão do modelo** usada, para a ficha antiga continuar legível depois de o modelo mudar.

**Pronto quando:** registrar e concluir um atendimento funciona, e reabrir um concluído devolve a ficha na versão em que foi preenchida.

### [ ] 4.2 · Abstract Factory — montagem da ficha

**Objetivo do padrão:** um tipo de campo novo deve exigir **criar uma classe e nada mais**.

Criar em `patterns/factory`: interface `Field`, `FieldDefinition` (montada a partir de `TemplateField`), `FieldFactory`, e as fábricas de `TextField`, `NumberField`, `DateField`, `SelectField`. Mais `RecordEngine`, que percorre o modelo e monta a ficha.

**Como:** cada fábrica é um bean nomeado pelo tipo que produz. O `RecordEngine` resolve pelo `fieldType` declarado no template. **Nada de enum de tipos de campo** — enum obrigaria a editar código para acrescentar tipo, quebrando o critério de reuso.

**Pronto quando:** duas clínicas com modelos diferentes produzem fichas diferentes pelo mesmo endpoint, e existe teste que acrescenta um tipo de campo dentro do próprio teste e vê a ficha renderizá-lo.

### [ ] 4.3 · Componentes especiais

Criar em `patterns/factory`: `SpecialComponentFactory` produzindo `Odontogram` e `BodyMap`.

**Como:** componente especial só aparece quando o módulo exigido está ativo. Consultar `ModuleActivationState`.

**Pronto quando:** publicar um modelo com odontograma numa clínica sem o módulo correspondente é recusado.

### [ ] 4.4 · Histórico do cliente

Criar em `core/encounter`: endpoint de histórico por cliente, em ordem cronológica reversa.

**Como:** o escopo depende do parâmetro `role_model`. Em modo segregado, recepção vê data, profissional e serviço; o conteúdo clínico é restrito ao profissional. Em modo único, todos veem tudo.

**Pronto quando:** o mesmo usuário de recepção recebe respostas diferentes em duas clínicas com `role_model` diferente.

---

# FASE 5 — Financeiro

### [ ] 5.1 · Cobrança

Criar em `core/billing`: `Invoice`, `InvoiceStatus`, serviço, repositório e controller.

Métodos: `calculateNetAmount()`, `applyDiscount(amount, reason)`, `outstandingBalance()`, `isOverdue()`.

**Como:** todo atendimento concluído gera cobrança. Quando coberta por pacote, o valor líquido é zero e a origem fica declarada — a cobrança existe de qualquer forma, para não haver atendimento sem rastro financeiro.

**Pronto quando:** concluir atendimento gera cobrança em aberto com o valor do serviço.

### [ ] 5.2 · Pagamento

Criar em `core/billing`: `Payment`, `PaymentMethod`, serviço, repositório e controller.

Método: `refund(reason)`.

**Como:** aceita recebimento parcial. Valor acima do saldo em aberto é recusado.

**Pronto quando:** pagamento total e parcial funcionam, e estorno recompõe o saldo.

### [ ] 5.3 · Ponto de extensão do atendimento

**Objetivo:** o núcleo aciona comportamento de módulo sem conhecê-lo.

Criar `EncounterCompletionListener` em `common/extension`. `EncounterService` injeta `List<EncounterCompletionListener>` e chama todos ao concluir.

**Pronto quando:** o núcleo compila sem nenhuma implementação existir, e uma implementação nova é chamada só por existir.

---

# FASE 6 — Módulos

Cada módulo é uma fatia independente. Faça um de cada vez. Todos seguem a mesma forma: entidade, serviço, repositório em `internal`, controller anotado com `@RequiresModule`.

### [ ] 6.1 · Dependentes

`Dependent` com `ageInYears()`. Atributos específicos do tipo em JSON, para servir a animal sob tutor, menor sob responsável e assistido sob cuidador sem fechar a estrutura.

### [ ] 6.2 · Estoque

`Product` com `isBelowMinimum()` e `decreaseStock(quantity)`. Movimentação de entrada, saída e ajuste, sempre com autoria.

Implementar `StockDispenser` em `common/extension` para o atendimento acionar a baixa.

### [ ] 6.3 · Lote e validade

`Batch` com `isExpired(LocalDate)`, `expiresWithin(int days)`, `hasAvailable(quantity)`.

**Como:** exige o módulo de estoque ativo. Lote vencido aparece para conferência de descarte mas não é selecionável quando o bloqueio está ligado.

### [ ] 6.4 · Strategy — política de lote vencido

**Objetivo do padrão:** uma política nova deve exigir **criar uma classe e nada mais**.

Criar em `patterns/strategy`: interface `ExpiredBatchPolicy`, implementações `BlockExpiredBatchPolicy` e `WarnExpiredBatchPolicy`.

**Como:** cada uma é um bean nomeado. O serviço injeta `Map<String, ExpiredBatchPolicy>` e resolve pela chave, usando o valor de `block_expired_batch`. **Proibido `if`, `switch` ou enum de política dentro do serviço.**

Esta é a função exclusiva do produto. Capriche.

**Pronto quando:** a mesma operação recusa numa clínica e alerta na outra, e a diferença entre as duas é uma linha na tabela de parâmetros.

### [ ] 6.5 · Strategy — política de acesso por perfil

Criar `RoleAccessPolicy` com `SegregatedRoleAccessPolicy` e `SingleRoleAccessPolicy`.

**Como:** resolvido pelo parâmetro `role_model`, mesma mecânica do 6.4. O papel do usuário é idêntico nos dois casos; o que muda é a política.

**Pronto quando:** recepção recebe 403 no relatório financeiro em modo segregado e 200 em modo único, sem nenhum papel de usuário ter sido alterado.

### [ ] 6.6 · Pacotes de sessões

`SessionPackage` com `remainingSessions()`, `isActive()`, `consumeSession()`.

**Como:** um atendimento consome no máximo uma sessão. Implementar `EncounterCompletionListener` para o abatimento acontecer sozinho.

### [ ] 6.7 · Convênios

`InsurancePlan` com `reimbursementFor(Money grossAmount)` e `CustomerInsurance` ligando cliente e plano.

**Como:** implementar um ponto de extensão de ajuste de cobrança, para o percentual entrar sem o financeiro conhecer o módulo.

### [ ] 6.8 · Notificações

`Notification` com `markAsSent()` e `registerResponse(String)`.

**Como:** o envio usa a antecedência de `reminder_lead_hours`. O gateway de mensagens é ator externo; abstraia atrás de uma interface e implemente uma versão de log por enquanto.

### [ ] 6.9 · Comissionamento

`Commission` com `calculate(Money invoiceAmount)`. Apuração por período com fechamento.

---

# FASE 7 — Verificação

### [ ] 7.1 · Prova de reuso

Criar `ReuseTest` em `src/test/java/com/example/clivoapi/patterns/`, com um método por padrão:

- acrescenta uma `ExpiredBatchPolicy` nova definida dentro do teste e verifica que o serviço a resolve
- acrescenta um `AppointmentValidator` novo definido dentro do teste e verifica que entra na corrente na posição certa
- acrescenta um tipo de campo novo definido dentro do teste e verifica que a ficha o renderiza

**Nenhum desses testes pode exigir alteração em classe de produção.** É este arquivo que demonstra o reuso; ele vale mais que qualquer diagrama.

### [ ] 7.2 · Testes de arquitetura

`ModularityTest` com `verify()` e `Documenter`. `ArchitectureTest` com ArchUnit provando que `..core..` não acessa `..modules..`.

### [ ] 7.3 · Cenários

Criar `src/test/java/com/example/clivoapi/scenarios/` com Testcontainers PostgreSQL. Nomear os métodos no formato `ct01_descricaoDoCenario()`, para cruzarem com a suíte de casos de teste do projeto.

Cobrir no mínimo:

- isolamento entre inquilinos: busca, acesso direto por identificador, relatório
- configuração inválida: dependência de módulo e parâmetro fora da faixa
- agenda: sobreposição, bloqueio, fora da disponibilidade, prazo de reagendamento
- ficha: modelo novo sem alterar schema, versão anterior preservada, campo de módulo inativo ausente
- lote: recusa com bloqueio ligado, alerta com bloqueio desligado

---

# Ordem de execução

```
Fase 0  →  Fase 1  →  Fase 2  →  Fase 3  →  Fase 4  →  Fase 5  →  Fase 6  →  Fase 7
```

Dentro de cada fase, siga a numeração. As fases 6.1 a 6.9 são independentes entre si e podem sair em qualquer ordem, exceto que 6.3 exige 6.2 pronto.

Não avance de fase com teste vermelho.