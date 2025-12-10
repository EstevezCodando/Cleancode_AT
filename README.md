# Relatório Técnico da Refatoração do Módulo de Pedidos de Entrega

## 1. Contexto e Objetivo da Refatoração

Ao ingressar na equipe técnica da empresa de logística, o cenário inicial era um módulo de pedidos de entrega com forte carga de dívida técnica: código difícil de manter, regras de negócio inconsistentes, ausência de validações e baixa clareza estrutural. A classe `Pedido` concentrava múltiplas responsabilidades ao mesmo tempo em que misturava domínio, lógica de cálculo de frete, regras promocionais e formatação textual de etiquetas.

O objetivo desta refatoração foi:

- Separar responsabilidades por camadas de abstração (domínio, serviços de domínio, aplicação/apresentação);
- Introduzir entidades de domínio com invariantes explícitos;
- Definir contratos claros para cálculo de frete e geração de etiquetas;
- Implementar tratamento de erros previsível, com exceções específicas;
- Preparar o sistema para extensão (novos fretes, promoções, formatos de etiqueta) sem alterar código já validado;
- Atender às rubricas propostas de Clean Code, modularidade, baixo acoplamento e reutilização.

Toda a solução foi implementada em um projeto **Spring Boot + Maven**, com organização em pacotes sob `empresa.logistica` e testes automatizados (unitários e property-based).

---

## 2. Análise Crítica do Código Legado

### 2.1. Ausência de encapsulamento

Na classe original:

```java
public class Pedido {
    public String endereco;
    public double peso;
    public String tipoFrete;
    public String destinatario;
    // ...
}
```

Todos os atributos eram públicos, o que permite que qualquer parte do sistema altere livremente o estado interno do objeto, inclusive para estados inválidos (por exemplo, `peso` negativo, `tipoFrete` nulo ou vazio). Não havia mecanismos para garantir invariantes nem restrições de uso.

### 2.2. Acoplamento entre lógica de negócio e apresentação

Os métodos `gerarEtiqueta()` e `gerarResumoPedido()` misturavam cálculo de frete com formatação textual:

```java
public String gerarEtiqueta() {
    return "Destinatário: " + destinatario + "\nEndereço: " + endereco + "\nValor do Frete: R$" + calcularFrete();
}
```

Isso acoplava diretamente o domínio (endereços, pesos e fretes) ao formato textual de saída, dificultando qualquer alteração de formato (por exemplo, para JSON, PDF ou outros meios), além de tornar a classe responsável por mais de uma preocupação.

### 2.3. Duplicação de lógica e responsabilidades misturadas

A mesma classe:

- Calculava o frete;
- Aplicava promoção (`aplicarFretePromocional`);
- Decidia se o frete era grátis (`isFreteGratis`);
- Formatava etiqueta e resumo;
- Guardava dados do pedido.

Isso viola diretamente o princípio da responsabilidade única (SRP) e torna o código frágil: qualquer ajuste de regra de negócio ou de apresentação exige editar a mesma classe, aumentando o risco de regressões.

### 2.4. Uso de valores mágicos

A lógica de frete utilizava valores literais sem significado explícito:

```java
if (tipoFrete.equals("EXP")) {
    return peso * 1.5 + 10;
} else if (tipoFrete.equals("PAD")) {
    return peso * 1.2;
} else if (tipoFrete.equals("ECO")) {
    return peso * 1.1 - 5;
}
```

Strings como `"EXP"`, `"PAD"`, `"ECO"` e constantes numéricas (`1.5`, `1.2`, `1.1`, `10`, `-5`, `2`) não possuíam nomes ou agrupamento conceitual. Isso degrada a legibilidade e torna a manutenção suscetível a erros.

### 2.5. Falta de validações e falhas silenciosas

Não havia verificação para:

- Peso menor ou igual a zero;
- Campos obrigatórios nulos ou em branco;
- Tipos de frete desconhecidos.

Quando o tipo de frete não correspondia a nenhum dos reconhecidos, o método retornava 0:

```java
} else {
    return 0;
}
```

Esse retorno silencioso mascara erros de configuração ou dados inválidos, dando a impressão de que o frete é válido quando, na verdade, houve falha de interpretação das regras.

### 2.6. Baixa coesão e ausência de abstrações

A classe `Pedido` funcionava como um “objeto Deus” para o módulo, concentrando lógica, dados e apresentação. Não havia abstrações claras para:

- Entidade de domínio (Entrega);
- Estrategias de frete;
- Serviço de formatação de etiqueta ou resumo;
- Tratamento de erros de domínio.

O uso de `if-else` encadeados representava um código rigidamente acoplado aos tipos de frete existentes, violando o princípio Aberto-Fechado (OCP).

---

## 3. Descrição Detalhada das Etapas de Refatoração

### 3.1. Separação das responsabilidades de domínio: criação da entidade `Entrega`

O primeiro passo foi isolar o conceito de domínio fundamental: a **entrega**. A classe `Entrega` foi criada como uma entidade imutável, representando um pedido de entrega com dados consistentes.

Decisões de projeto:

- Atributos privados e finais;
- Validações no construtor;
- Normalização do tipo de frete (upper case);
- Imutabilidade para evitar alterações inesperadas após a construção.

Benefícios:

- Garante que qualquer `Entrega` existente no sistema está em um estado válido;
- Reduz a superfície de erro, já que estados inválidos são barrados na criação;
- Facilita o raciocínio sobre o domínio.

### 3.2. Extração das estratégias de frete para a interface `CalculadoraFrete`

Em seguida, a lógica de cálculo de frete foi extraída da classe original e modelada como uma interface de alto nível:

- `CalculadoraFrete` define o contrato para qualquer tipo de frete;
- Cada tipo de frete (`FreteExpresso`, `FretePadrao`, `FreteEconomico`) implementa suas regras específicas;
- Uma política de promoção por peso é disponibilizada como método `default`, reaproveitável por múltiplas estratégias.

Métodos aplicados:

- **Open/Closed Principle (OCP)**: novos tipos de frete podem ser adicionados sem alterar código existente, apenas criando novas implementações da interface;
- **Strategy Pattern**: cada frete é uma estratégia de cálculo distinta;
- **Single Responsibility Principle (SRP)**: cada classe de frete cuida apenas de uma fórmula específica e da sua semântica.

### 3.3. Criação do `CalculadoraFreteProvider`: substituição do `if-else` rígido

O bloco `if-else` baseado em `tipoFrete` foi substituído por um provider:

- `CalculadoraFreteProvider` recebe, via injeção de dependências do Spring, todas as implementações de `CalculadoraFrete`;
- Constrói um mapa `código → calculadora`;
- Expõe métodos `calcularFrete` e `ehFreteGratis` que delegam para a estratégia correta;
- Lança `TipoFreteNaoSuportadoException` para códigos inválidos.

Métodos aplicados:

- **Inversão de Controle (IoC)** e **Injeção de Dependências (DI)**: o Spring injeta automaticamente as estratégias anotadas com `@Component`;
- **Open/Closed Principle**: adicionar `FreteNoturno`, por exemplo, requer apenas uma nova classe; o provider continua intacto;
- Eliminação de acoplamento rígido à lista fixa de tipos de frete em uma única classe.

### 3.4. Extração da apresentação: criação do `EtiquetaService` e `FormatadorEtiqueta`

A lógica de geração de etiqueta e resumo foi isolada em um serviço de aplicação:

- `EtiquetaService` recebe `CalculadoraFreteProvider` e `FormatadorEtiqueta` por injeção;
- Calcula o frete a partir de uma `Entrega` e delega a formatação ao `FormatadorEtiqueta`;
- `FormatadorEtiquetaTextoSimples` implementa o formato de texto próximo ao legado, mas agora isolado.

Métodos aplicados:

- **Separation of Concerns**: domínio (regra de frete e estado da entrega) foi separado de apresentação (texto de etiqueta);
- **Dependency Inversion Principle (DIP)**: `EtiquetaService` depende de interfaces (`CalculadoraFreteProvider`, `FormatadorEtiqueta`), não de implementações concretas;
- Facilita a evolução da camada de apresentação (outros formatos) sem tocar no domínio.

### 3.5. Tratamento de erros com exceções específicas

Para evitar falhas silenciosas, foram introduzidas as seguintes exceções de domínio:

- `ValidacaoEntidadeException`: lançada ao tentar criar `Entrega` com dados inválidos (peso ≤ 0, destinatário/endereço vazios, tipo de frete ausente);
- `TipoFreteNaoSuportadoException`: lançada quando um código de frete não existe no registry de estratégias.

Métodos aplicados:

- Falhas passam a ser **explícitas e previsíveis**;
- Erros de dados de entrada são detectados no limite de criação do objeto, e não propagados silenciosamente pelo sistema;
- A API do domínio comunica de forma clara o que deu errado, facilitando tratamento em camadas superiores (por exemplo, REST).

### 3.6. Testes automatizados e property-based

Para garantir a robustez do comportamento, foram criados:

- Testes unitários da entidade `Entrega` (garantindo invariantes e comportamento básico);
- Testes de `EtiquetaService` para verificar a integração entre cálculo de frete e formatação;
- Testes property-based com jqwik para `FreteEconomico`:
  - Verificando que para pesos abaixo de certo limite o frete é sempre zero;
  - Assegurando que o valor do frete nunca se torna negativo, mesmo em faixas amplas de pesos.

Métodos aplicados:

- **Test-Driven Mindset**: testes como especificação de comportamento;
- Property-based testing para explorar múltiplas combinações de entrada e detectar casos de borda de maneira mais sistemática.

---

## 4. Respostas às Questões Analíticas

### 4.1. Abstrações definidas e distribuição em camadas

**Domínio**:

- `Entrega`: entidade imutável representando uma entrega, com invariantes garantidos;
- `CalculadoraFrete`: contrato para qualquer algoritmo de cálculo de frete.

**Serviços de domínio**:

- `FreteExpresso`, `FretePadrao`, `FreteEconomico`: implementações concretas de estratégias de frete;
- `CalculadoraFreteProvider`: resolve qual estratégia utilizar com base no código do tipo de frete e expõe métodos de alto nível para cálculo e verificação de frete grátis.

**Camada de aplicação/apresentação**:

- `EtiquetaService`: orquestra o cálculo de frete e delega a formatação;
- `FormatadorEtiqueta`: abstração para qualquer forma de geração de etiqueta/resumo;
- `FormatadorEtiquetaTextoSimples`: implementação textual para fins de compatibilidade e simplicidade.

**Como isso favorece entendimento, manutenção e testabilidade**:

- Um desenvolvedor que deseja alterar apenas a regra de cálculo de frete econômico vai diretamente para `FreteEconomico`, sem tocar em formatação nem em outras estratégias;
- Quem deseja alterar o texto da etiqueta atua em `FormatadorEtiquetaTextoSimples`, sem interferir na regra de cálculo;
- `EtiquetaService` pode ser testado com dublês de `CalculadoraFreteProvider` e `FormatadorEtiqueta`, isolando cenários e facilitando testes unitários.

**Substituição de estruturas rígidas `if-else` e favorecimento da extensão**:

- No código legado, o cálculo de frete usava:

  ```java
  if (tipoFrete.equals("EXP")) { ... }
  else if (tipoFrete.equals("PAD")) { ... }
  else if (tipoFrete.equals("ECO")) { ... }
  else { return 0; }
  ```

- Na arquitetura refatorada, a escolha da estratégia é feita por meio de um mapa de `CalculadoraFrete` mantido pelo `CalculadoraFreteProvider`. Adicionar um novo tipo de frete significa criar uma nova classe que implementa `CalculadoraFrete` (por exemplo, `FreteNoturno`) e marcar com `@Component`. O Spring injeta a nova estratégia automaticamente, e o provider passa a reconhecê-la sem alteração de código existente.

### 4.2. Contratos de integridade dos objetos de domínio

O principal contrato de integridade é o da classe `Entrega`:

- Destinatário, endereço e tipo de frete não podem ser nulos nem vazios;
- Peso deve ser maior que zero;
- Tipo de frete é normalizado para maiúsculas para evitar inconsistências por capitalização.

Essas regras são verificadas no construtor, e qualquer violação resulta em `ValidacaoEntidadeException`. Com isso:

- Objetos inválidos não são criados;
- Outros componentes podem assumir que `Entrega` sempre representa um estado coerente do domínio;
- A detecção de erros é antecipada, reduzindo surpresas em pontos distantes da execução.

Complementarmente, `CalculadoraFreteProvider` define o contrato de que:

- Todos os tipos de frete válidos devem ter uma estratégia correspondente;
- Códigos desconhecidos são tratados com `TipoFreteNaoSuportadoException`, o que impede que cálculos de frete retornem zero de forma silenciosa.

Essa abordagem aumenta a robustez e previsibilidade porque claramente diferencia **fluxo normal** (frete calculado) de **fluxo de erro** (entrada inválida ou tipo de frete não suportado).

### 4.3. Boas práticas de nomenclatura e estrutura de código

As escolhas de nomes foram orientadas pelo domínio e pela intenção dos componentes:

- `Entrega` em vez de `Pedido` enfatiza o foco na entrega física;
- `CalculadoraFrete` explicita sua intenção como responsável por calcular frete;
- `FreteExpresso`, `FretePadrao`, `FreteEconomico` seguem nomenclatura coerente, refletindo o tipo de serviço;
- `EtiquetaService` comunica que seu propósito é gerar etiquetas e resumos;
- `ValidacaoEntidadeException` e `TipoFreteNaoSuportadoException` informam claramente o tipo de problema tratado.

Essa nomenclatura autoexplicativa reduz a necessidade de comentários descritivos. O código torna-se legível por leitura das assinaturas e dos pacotes:

- Pacote `dominio` concentra entidades centrais;
- Pacote `frete` concentra lógica de cálculo relacionada ao frete;
- Pacote `etiqueta` concentra lógica de geração de representação textual;
- Pacote `excecao` agrupa erros de domínio.

A estrutura de código mantém métodos pequenos, coesos e com uma única responsabilidade. Cada classe tem um propósito bem definido, o que reduz a complexidade cognitiva ao navegar pelo projeto.

### 4.4. Organização de arquivos e pacotes para coesão e navegabilidade

A distribuição dos arquivos em pacotes por responsabilidade foi intencional para facilitar a navegação:

- Quem busca entender o **modelo do domínio** começa pelo pacote `dominio`;
- Quem precisa alterar a **regra de frete** vai ao pacote `frete`;
- Quem quer mudar a **apresentação de etiquetas** acessa `etiqueta`;
- Tratamentos de erro específicos são encontrados em `excecao`;
- A configuração Spring Boot reside em `config` e na classe raiz `LogisticaApplication`.

Essa organização cria “zonas de coesão lógica” dentro do projeto. O desenvolvedor não precisa procurar em múltiplos locais misturados para entender uma funcionalidade. Isso traz dois efeitos importantes:

1. **Redução de necessidade de comentários**: o próprio nome do pacote e da classe já comunica o papel do código;
2. **Facilidade de onboarding**: novos membros da equipe conseguem se localizar mais rapidamente, reduzindo o tempo para compreensão do sistema.

---

## Estrutura implementada

```text
logistica/
  pom.xml

  src/main/java/empresa/logistica/
    LogisticaApplication.java

    dominio/
      Entrega.java

    excecao/
      ValidacaoEntidadeException.java
      TipoFreteNaoSuportadoException.java

    frete/
      CalculadoraFrete.java
      FreteExpresso.java
      FretePadrao.java
      FreteEconomico.java
      CalculadoraFreteProvider.java

    etiqueta/
      FormatadorEtiqueta.java
      FormatadorEtiquetaTextoSimples.java
      EtiquetaService.java


  src/test/java/empresa/logistica/
    LogisticaApplicationTests.java

    dominio/
      EntregaTest.java

    frete/
      FreteEconomicoPropertyTest.java

    etiqueta/
      EtiquetaServiceTest.java
```

Responsabilidades por pacote:

- `dominio`: entidades de negócio (imutáveis, válidas).
- `excecao`: exceções específicas de domínio/validação.
- `frete`: estratégias de cálculo de frete + provider (“registry”).
- `etiqueta`: serviço de geração de etiqueta e resumo, desacoplado de domínio.
- `config`: configuração Spring para registrar beans e integrar o domínio com a infraestrutura.

O objetivo é:

- Separar responsabilidades em camadas coesas.
- Eliminar `if-else` rígidos para cálculo de frete.
- Garantir invariantes de domínio (objetos sempre válidos).
- Tratar erros de forma explícita e previsível.
- Preparar a arquitetura para extensão (novos tipos de frete, promoções, formatos de etiqueta).
