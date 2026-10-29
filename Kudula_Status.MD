KUDULA — STATUS OFICIAL DO PROJECTO

Última actualização: 21 de Outubro de 2026
Hora de referência: 19h58, Africa/Luanda
Empresa-mãe: Nulongi
Produto: Plataforma de aprendizagem e orientação de carreira com Inteligência Artificial
Slogan: Inteligência para Aprender, Orientação para Crescer.

---

1. Estado actual do projecto

A Kudula encontra-se na fase de desenvolvimento e integração das funcionalidades essenciais de Inteligência Artificial para preparar o primeiro lançamento do produto.

A aplicação Android possui uma estrutura funcional, com navegação entre módulos, gestão de estado, perfil de aprendizagem e integração de serviços de IA.

O estado da integração de IA é o seguinte:

- Tutor Kudu: integração de IA implementada e funcional para conversação e geração de respostas em streaming.
- Biblioteca Inteligente Multimodal: integração de IA em desenvolvimento. É a prioridade técnica actual.
- Restantes módulos com necessidade de IA: integração e validação ainda por realizar, conforme os requisitos de cada módulo.

A prioridade é transformar a estrutura actual num MVP funcional e operacional, com as funcionalidades essenciais implementadas, integradas, testadas e prontas para utilização pelos primeiros utilizadores.

2. Estado da integração de Inteligência Artificial

2.1. Tutor Kudu — integração implementada

O Tutor Kudu é, actualmente, a área onde a integração de IA se encontra implementada e funcional.

Componentes técnicos conhecidos:

- "KudulaAIService.kt"
- "KudulaViewModel.kt"
- Firebase AI SDK.
- Backend Google AI.
- Geração de texto em streaming.
- Contextualização das respostas através do perfil de aprendizagem e do histórico recente de conversação.

O tutor foi concebido para adaptar as explicações ao contexto e às necessidades do utilizador.

Estado: integração implementada; a validação contínua da qualidade das respostas, persistência e comportamento em diferentes cenários continua a ser necessária.

2.2. Biblioteca Inteligente Multimodal — prioridade actual

A Biblioteca Inteligente é a área que está actualmente a receber trabalho de integração correcta da IA.

O objectivo é permitir que os utilizadores forneçam documentos e outros conteúdos suportados e obtenham análises e experiências de aprendizagem fundamentadas no conteúdo real.

O fluxo funcional pretendido inclui:

1. Seleccionar e carregar um ficheiro.
2. Ler ou extrair o conteúdo real, respeitando o formato.
3. Processar o conteúdo através do serviço de IA.
4. Gerar resumos, explicações, conceitos, perguntas e outros resultados suportados.
5. Guardar os resultados e associá-los ao documento correcto.
6. Permitir que o Tutor Kudu utilize o conteúdo validado para responder a perguntas sobre o documento.
7. Tratar erros, limites de ficheiros, estados de carregamento e falhas de processamento.

A abordagem multimodal deverá ser implementada de acordo com os formatos efectivamente suportados pela arquitectura escolhida.

Estado: integração em desenvolvimento. A conclusão depende de testes reais de ponta a ponta; o carregamento de um ficheiro, por si só, não comprova que o seu conteúdo foi correctamente processado pela IA.

2.3. Restantes módulos que necessitam de IA

Depois de estabilizar a Biblioteca Inteligente, será necessário implementar ou completar as integrações de IA nos restantes módulos que dependam dessa capacidade.

Entre as áreas a avaliar estão:

- YouTube Intelligence.
- Espaço de Leitura.
- Revisão Adaptativa.
- Índice IKC.
- Hub de Carreiras (MICK).
- Eu do Futuro.
- Diário Reflexivo.
- Kudu Voice Notes.
- Outras funcionalidades de aprendizagem personalizada, orientação e análise de conteúdos.

Esta lista identifica áreas a avaliar e não significa que todas tenham exactamente os mesmos requisitos ou que todas dependam directamente de um modelo generativo.

Cada integração deverá ter objectivos, dados de entrada, resultados esperados e critérios de validação próprios.

Estado: levantamento, implementação e validação pendentes conforme o progresso real de cada módulo.

---

3. Arquitectura técnica actualmente conhecida

- Aplicação Android desenvolvida em Kotlin.
- "KudulaAIService.kt": serviço de IA com geração de texto em streaming.
- "KudulaViewModel.kt": gestão de estado e navegação.
- Firebase Authentication: autenticação.
- FirestoreRepository: persistência remota parcialmente implementada.
- Room: código de persistência local de conversas.
- KudulaSecurePreferences: preferências do processo de integração inicial.
- EventLogger: registo de eventos.

A arquitectura deverá permitir evoluir de forma sustentável para Web, Desktop e iOS, sem comprometer o foco no MVP Android.

As decisões de arquitectura devem favorecer a reutilização futura de serviços, dados e lógica sempre que isso seja viável, sem introduzir complexidade desnecessária nesta fase.

---

4. Estado da aplicação

A aplicação possui 21 destinos de navegação definidos:

1. Splash.
2. Como Funciona.
3. Login.
4. Boas-vindas Cognitivas.
5. Início.
6. Biblioteca Inteligente.
7. Tutor Kudu.
8. YouTube Intelligence.
9. Espaço de Leitura.
10. Revisão Adaptativa.
11. Índice IKC.
12. Hub de Carreiras (MICK).
13. Eu do Futuro.
14. Proof of Learning.
15. Comunidade.
16. Definições.
17. Learning Timeline.
18. Diário Reflexivo.
19. Perfil.
20. Kudu Voice Notes.
21. Planos.

A existência de navegação para estes módulos não significa que todas as funcionalidades estejam implementadas, ligadas à IA ou validadas.

O estado de cada módulo deverá ser documentado individualmente.

---

5. Plano de trabalho até ao primeiro lançamento

P0 — Biblioteca Inteligente Multimodal

- Inspeccionar a implementação actual.
- Corrigir ou concluir a leitura e o processamento dos ficheiros.
- Integrar correctamente o conteúdo real com o serviço de IA.
- Validar a geração de resultados.
- Guardar os resultados e associá-los aos documentos.
- Testar a utilização desses conteúdos pelo Tutor Kudu.

P1 — Integração de IA nos módulos essenciais

- Identificar quais os módulos indispensáveis ao primeiro lançamento.
- Definir os requisitos de IA de cada módulo.
- Implementar as integrações necessárias.
- Reutilizar os serviços existentes sempre que adequado.
- Validar individualmente cada funcionalidade.
- Evitar considerar uma funcionalidade concluída apenas por apresentar uma interface funcional.

P2 — Integração e estabilidade do produto

- Testar a comunicação entre os módulos.
- Validar a persistência de documentos, resultados e conversas.
- Verificar autenticação, permissões e tratamento de erros.
- Testar diferentes formatos de conteúdo suportados.
- Avaliar desempenho, segurança e custos de IA.
- Confirmar que os principais percursos do utilizador funcionam de ponta a ponta.

P3 — Preparação para o primeiro lançamento

- Definir o âmbito final do MVP.
- Corrigir erros críticos.
- Realizar testes internos e com utilizadores convidados.
- Preparar monitorização, recolha de feedback e suporte.
- Estabelecer limites de utilização compatíveis com os custos operacionais.
- Confirmar que as funcionalidades anunciadas correspondem às funcionalidades realmente disponíveis.

---

6. Estado do Product-Market Fit

Com base na informação actualmente documentada:

- Protótipo Android funcional: sim.
- Integração de IA no Tutor Kudu: implementada.
- Integração de IA na Biblioteca Inteligente Multimodal: em desenvolvimento.
- Integração de IA nas restantes áreas: por completar ou validar conforme cada módulo.
- Utilizadores reais nos produtos: nenhum confirmado.
- Pagamentos: nenhum confirmado.
- Product-Market Fit: ainda não demonstrado.

O progresso técnico não constitui, por si só, evidência de Product-Market Fit. A validação de mercado dependerá da utilização real, do valor percebido, da retenção e da procura pelo produto.

---

7. Critérios para considerar o MVP pronto

O MVP não deverá ser considerado pronto apenas porque a aplicação abre ou porque o Tutor Kudu responde.

Antes do primeiro lançamento, deverá existir evidência de que:

- Os percursos essenciais da aplicação funcionam.
- A Biblioteca Inteligente processa correctamente os formatos de conteúdo incluídos no MVP.
- A IA produz resultados fundamentados nos conteúdos fornecidos.
- Os módulos essenciais que necessitam de IA estão efectivamente integrados.
- Os dados e resultados relevantes são guardados e recuperados correctamente.
- Os módulos comunicam entre si conforme previsto.
- Os erros mais importantes são tratados adequadamente.
- A segurança, os custos e os limites de utilização são aceitáveis.
- Os primeiros utilizadores conseguem executar as tarefas principais sem assistência técnica constante.

O âmbito exacto do MVP deverá ser definido com base na prioridade para o utilizador, na viabilidade técnica e nos recursos disponíveis.

---

8. Próxima acção imediata

Continuar o desenvolvimento da integração de IA na Biblioteca Inteligente Multimodal.

O próximo passo é inspeccionar o código actual, identificar o que já está implementado e determinar a alteração técnica seguinte, sem reconstruir componentes que já funcionam.

Depois de validar a Biblioteca Inteligente, avançar progressivamente para as integrações de IA dos restantes módulos essenciais, até alcançar um MVP funcional, operacional e preparado para o primeiro lançamento.

---

9. Regras de actualização deste ficheiro

Este documento é a referência central para acompanhar o estado de desenvolvimento da Kudula.

Sempre que houver uma alteração relevante, actualizar:

- O estado real de cada integração de IA.
- As funcionalidades concluídas e efectivamente testadas.
- Os problemas técnicos identificados.
- As tarefas pendentes.
- O próximo passo concreto.
- A data e a hora da actualização.

Distinguir sempre entre planeado, em desenvolvimento, implementado, testado e validado.

Não declarar uma funcionalidade concluída sem evidências verificáveis no código ou através de testes.

Estado de referência: desenvolvimento do protótipo funcional, com o Tutor Kudu como integração de IA já implementada e a Biblioteca Inteligente Multimodal como prioridade técnica actual.

Objectivo imediato: concluir e validar as integrações de IA essenciais para disponibilizar o primeiro MVP funcional e operacional da Kudula.