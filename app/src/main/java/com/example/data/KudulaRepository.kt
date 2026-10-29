package com.example.data

import com.example.model.*

object KudulaRepository {

    fun getDefaultProfile(): UserCognitiveProfile {
        return UserCognitiveProfile()
    }

    fun getLibraryItems(): List<LibraryItem> {
        return emptyList()
    }

    fun getInitialChatMessages(
        profile: UserCognitiveProfile = UserCognitiveProfile(),
        libraryItems: List<LibraryItem> = emptyList()
    ): List<ChatMessage> {
        val topic = if (libraryItems.isNotEmpty()) {
            libraryItems.first().title
        } else if (profile.academicArea.isNotBlank()) {
            profile.academicArea
        } else if (profile.professionalArea.isNotBlank()) {
            profile.professionalArea
        } else {
            ""
        }

        val prompts = if (libraryItems.isNotEmpty()) {
            listOf(
                "Explica os conceitos essenciais de '$topic'",
                "Como aplicar os temas de '$topic' na prática real?",
                "Gera um quiz socrático para testar o meu domínio em '$topic'",
                "Dá-me uma analogia simples do dia a dia sobre '$topic'"
            )
        } else if (topic.isNotBlank()) {
            listOf(
                "Explica um conceito fundamental de $topic",
                "Como resolver os desafios práticos mais comuns em $topic?",
                "Gera 2 perguntas de desafio para testar a minha retenção",
                "Ajuda-me a criar um resumo estruturado para estudar $topic"
            )
        } else {
            listOf(
                "Ajuda-me a dominar um conceito difícil passo a passo",
                "Dá-me um exemplo prático com analogia do quotidiano",
                "Gera um quiz socrático para testar o meu raciocínio",
                "Como estruturar um plano de estudos eficiente para a minha matéria?"
            )
        }

        return listOf(
            ChatMessage(
                id = "msg-1",
                isUser = false,
                text = "Olá! Eu sou o Kudu, o teu tutor cognitivo. Estou pronto para te acompanhar em cada passo da tua jornada de aprendizagem. Em que conceito ou desafio gostarias de focar hoje?",
                timestamp = "Agora",
                suggestedPrompt = prompts
            )
        )
    }

    fun getSampleYouTubeAnalyses(): Map<String, YouTubeAnalysis> {
        return emptyMap()
    }

    fun getFlashcards(): List<FlashcardItem> {
        return emptyList()
    }

    fun getCareerRoles(profile: UserCognitiveProfile = UserCognitiveProfile()): List<CareerRole> {
        if (!profile.onboardingCompleted) return emptyList()
        val professionalRole = profile.professionalArea.ifBlank { profile.academicArea.ifBlank { "Especialista" } }
        val academicField = profile.academicArea.ifBlank { profile.professionalArea.ifBlank { "Geral" } }
        val aspiration = profile.careerDirection.ifBlank { professionalRole }

        val combinedKey = "$academicField $professionalRole $aspiration".lowercase()

        val roles = when {
            combinedKey.contains("saúde") || combinedKey.contains("médic") || combinedKey.contains("biolog") || combinedKey.contains("clínic") -> listOf(
                CareerRole(
                    id = "car-health-1",
                    title = if (professionalRole.contains("Médic", ignoreCase = true)) "Médico Especialista" else "Especialista em Ciências da Saúde",
                    description = "Trilha clínica e investigativa baseada na integração de evidências, diagnóstico de precisão e cuidados continuados.",
                    matchScore = 15,
                    salaryGlobal = "Referência de Mercado em Saúde",
                    marketTrend = "+32% procura especializada",
                    topSkillsNeeded = listOf("Diagnóstico Diferencial", "Fisiologia & Farmacologia", "Comunicação Clínica"),
                    missingSkills = listOf("Consolidação de Casos Clínicos Complexos")
                ),
                CareerRole(
                    id = "car-health-2",
                    title = "Investigador Biomédico & Saúde Digital",
                    description = "Pesquisa translacional acoplada à análise de bioestatística e novas terapias de precisão.",
                    matchScore = 12,
                    salaryGlobal = "Carreira Global de Pesquisa",
                    marketTrend = "+28% crescimento anual",
                    topSkillsNeeded = listOf("Bioestatística", "Metodologia Científica", "Leitura Crítica de Ensaios"),
                    missingSkills = listOf("Publicação Científica e Análise de Dados")
                )
            )

            combinedKey.contains("gestão") || combinedKey.contains("negócios") || combinedKey.contains("administra") || combinedKey.contains("empreend") || combinedKey.contains("líder") -> listOf(
                CareerRole(
                    id = "car-biz-1",
                    title = if (professionalRole.contains("Empreend", ignoreCase = true)) "Fundador & Líder de Inovação" else "Consultor Estratégico de Negócios",
                    description = "Tomada de decisão em ambientes de alta incerteza, modelação financeira e escalabilidade de produtos.",
                    matchScore = 15,
                    salaryGlobal = "Padrão Internacional de Liderança",
                    marketTrend = "+30% procura no mercado",
                    topSkillsNeeded = listOf("Pensamento Estratégico", "Finanças Corporativas", "Liderança de Equipas"),
                    missingSkills = listOf("Metodologias Ágeis de Governança")
                ),
                CareerRole(
                    id = "car-biz-2",
                    title = "Gestor de Produto & Crescimento",
                    description = "Alinhamento entre necessidades de mercado, proposta de valor e otimização de métricas chave.",
                    matchScore = 10,
                    salaryGlobal = "Referência em Gestão e Produto",
                    marketTrend = "+35% procura global",
                    topSkillsNeeded = listOf("Product Discovery", "Métricas de Impacto", "Negociação"),
                    missingSkills = listOf("Frameworks de Experimentação Rápida")
                )
            )

            combinedKey.contains("direito") || combinedKey.contains("humanidades") || combinedKey.contains("jurídic") || combinedKey.contains("social") || combinedKey.contains("filosof") -> listOf(
                CareerRole(
                    id = "car-law-1",
                    title = if (professionalRole.contains("Advogad") || professionalRole.contains("Direito")) "Especialista Jurídico & Regulatório" else "Consultor em Ciências Humanas & Sociais",
                    description = "Análise hermenêutica, argumentação estruturada e resolução de conflitos complexos.",
                    matchScore = 15,
                    salaryGlobal = "Referência em Direito e Consultoria",
                    marketTrend = "+24% demanda em conformidade",
                    topSkillsNeeded = listOf("Interpretação Normativa", "Retórica & Redação Jurídica", "Mediação Estratégica"),
                    missingSkills = listOf("Direito Digital e Proteção de Dados")
                ),
                CareerRole(
                    id = "car-law-2",
                    title = "Analista de Políticas Públicas e Estratégia",
                    description = "Desenvolvimento de pareceres institucionais e avaliação de impacto socioeconómico.",
                    matchScore = 10,
                    salaryGlobal = "Setor Governamental e Internacional",
                    marketTrend = "+20% procura sustentável",
                    topSkillsNeeded = listOf("Análise Regulatória", "Ética Aplicada", "Comunicação Institucional"),
                    missingSkills = listOf("Governança Corporativa e Compliance")
                )
            )

            combinedKey.contains("design") || combinedKey.contains("artes") || combinedKey.contains("visual") || combinedKey.contains("ux") -> listOf(
                CareerRole(
                    id = "car-design-1",
                    title = "Designer Estratégico de Experiência (UI/UX)",
                    description = "Arquitetura da informação centrada no ser humano, prototipagem avançada e sistemas visuais.",
                    matchScore = 15,
                    salaryGlobal = "Referência Global em Design",
                    marketTrend = "+33% procura internacional",
                    topSkillsNeeded = listOf("Arquitetura da Informação", "Design de Interação", "Pesquisa com Utilizadores"),
                    missingSkills = listOf("Design Systems Escaláveis")
                ),
                CareerRole(
                    id = "car-design-2",
                    title = "Diretor Criativo & Design de Serviços",
                    description = "Visão holística de produto, identidade de marca e jornadas de valor completas.",
                    matchScore = 10,
                    salaryGlobal = "Padrão Criativo Internacional",
                    marketTrend = "+25% demanda em estúdios",
                    topSkillsNeeded = listOf("Storytelling Visual", "Direção de Arte", "Estratégia de Marca"),
                    missingSkills = listOf("Liderança de Projetos Criativos")
                )
            )

            combinedKey.contains("engenhar") || combinedKey.contains("exatas") || combinedKey.contains("física") || combinedKey.contains("matemát") -> listOf(
                CareerRole(
                    id = "car-eng-1",
                    title = "Engenheiro Especialista & Soluções Técnicas",
                    description = "Cálculo avançado, modelação de processos e garantia de eficiência em infraestruturas.",
                    matchScore = 15,
                    salaryGlobal = "Referência Internacional de Engenharia",
                    marketTrend = "+34% demanda industrial",
                    topSkillsNeeded = listOf("Modelação Matemática", "Gestão de Projetos Técnicos", "Simulação Física"),
                    missingSkills = listOf("Automação e Sensores Inteligentes")
                ),
                CareerRole(
                    id = "car-eng-2",
                    title = "Cientista de Dados & Engenharia Quantitativa",
                    description = "Análise preditiva, estatística multivariada e otimização de sistemas complexos.",
                    matchScore = 10,
                    salaryGlobal = "Faixa de Alta Demanda Global",
                    marketTrend = "+38% procura anual",
                    topSkillsNeeded = listOf("Estatística Avançada", "Modelos Preditivos", "Otimização"),
                    missingSkills = listOf("Pipelines de Dados Escaláveis")
                )
            )

            combinedKey.contains("tecnolog") || combinedKey.contains("software") || combinedKey.contains("program") || combinedKey.contains("comput") -> listOf(
                CareerRole(
                    id = "car-tech-1",
                    title = "Arquiteto de Software & Engenharia de Sistemas",
                    description = "Desenvolvimento de plataformas resilientes, arquitetura distribuída e boas práticas de engenharia.",
                    matchScore = 15,
                    salaryGlobal = "Referência Global de Tecnologia",
                    marketTrend = "+36% demanda contínua",
                    topSkillsNeeded = listOf("Algoritmos & Estruturas de Dados", "Arquitetura Distribuída", "Clean Code"),
                    missingSkills = listOf("Engenharia de Confiabilidade (SRE)")
                ),
                CareerRole(
                    id = "car-tech-2",
                    title = "Engenheiro de Inteligência Artificial & LLMs",
                    description = "Construção de sistemas cognitivos adaptativos, RAG e orquestração de modelos de linguagem.",
                    matchScore = 10,
                    salaryGlobal = "Vanguarda Tecnológica Mundial",
                    marketTrend = "+45% procura anual",
                    topSkillsNeeded = listOf("Sistemas RAG & Embeddings", "Prompt Engineering Avançado", "Pipelines MLOps"),
                    missingSkills = listOf("Afinação Fina de Modelos e Avaliação de Latência")
                )
            )

            else -> listOf(
                CareerRole(
                    id = "car-custom-1",
                    title = aspiration.ifBlank { "Especialista em $professionalRole" },
                    description = "Trilha personalizada gerada com base nas tuas aptidões de $academicField e interesses declarados.",
                    matchScore = 15,
                    salaryGlobal = "Referência de Mercado para $professionalRole",
                    marketTrend = "+28% crescimento médio",
                    topSkillsNeeded = listOf(academicField, "Raciocínio Crítico", "Autonomia Cognitiva"),
                    missingSkills = listOf("Aprofundamento Prático na Área")
                )
            )
        }

        return roles
    }

    fun getFutureSelfProjections(profile: UserCognitiveProfile = UserCognitiveProfile()): List<FutureSelfMilestone> {
        if (!profile.onboardingCompleted) return emptyList()
        val currentXp = profile.xp
        val currentIkc = if (profile.ikc.hasData) profile.ikc.overallAverage else 5
        val roleTitle = profile.careerDirection.ifBlank {
            profile.professionalArea.ifBlank {
                profile.academicArea.ifBlank { "Especialista" }
            }
        }

        return listOf(
            FutureSelfMilestone(
                timeframe = "Estado Atual (Hoje)",
                targetRole = profile.professionalArea.ifBlank { profile.academicArea.ifBlank { "Iniciante na Tua Jornada" } },
                projectedIKC = currentIkc,
                projectedXP = currentXp,
                unlockedAbilities = listOf(
                    "Perfil cognitivo calibrado",
                    "Acesso ao Knowledge Vault de $roleTitle",
                    "Pronto para iniciar sessões de foco guiadas"
                ),
                guidanceQuote = "O teu ponto de partida define a linha de base para o teu crescimento acelerado em $roleTitle."
            ),
            FutureSelfMilestone(
                timeframe = "Em 6 Meses",
                targetRole = "Profissional em $roleTitle",
                projectedIKC = (currentIkc + 20).coerceAtMost(88),
                projectedXP = currentXp + 3500,
                unlockedAbilities = listOf(
                    "Domínio dos conceitos fundamentais de $roleTitle",
                    "Primeiras credenciais validadas de aprendizagem",
                    "Ritmo diário de alta retenção sem sobrecarga"
                ),
                guidanceQuote = "Ao manter um ritmo consistente de estudo na tua área, consolidas conexões duradouras."
            ),
            FutureSelfMilestone(
                timeframe = "Em 1 Ano",
                targetRole = "Referência em $roleTitle",
                projectedIKC = (currentIkc + 30).coerceAtMost(94),
                projectedXP = currentXp + 9000,
                unlockedAbilities = listOf(
                    "Capacidade comprovada de resolver problemas complexos em $roleTitle",
                    "Portfólio de competências práticas consolidadas",
                    "Raciocínio autônomo e domínio prático"
                ),
                guidanceQuote = "Com 1 ano de prática contínua, a tua evolução em $roleTitle destaca-te no mercado."
            ),
            FutureSelfMilestone(
                timeframe = "Em 2 Anos",
                targetRole = "Líder e Especialista Sênior em $roleTitle",
                projectedIKC = 96,
                projectedXP = currentXp + 18000,
                unlockedAbilities = listOf(
                    "Autoridade prática e mentoria em $roleTitle",
                    "Liderança de projetos estratégicos",
                    "Autonomia intelectual completa e reconhecimento profissional"
                ),
                guidanceQuote = "O teu percurso comprova maestria irrefutável e excelência na tua área escolhida."
            )
        )
    }

    fun getProofOfLearningBadges(): List<ProofOfLearningBadge> {
        return emptyList()
    }

    fun getCommunityPosts(): List<CommunityPost> {
        return emptyList()
    }
}
