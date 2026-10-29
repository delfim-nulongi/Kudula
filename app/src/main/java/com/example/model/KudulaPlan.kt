package com.example.model

/**
 * Kudula Plans Ecosystem (F0.6 Architecture).
 *
 * Preparado para futura sincronização com Firebase Auth, RevenueCat/Google Play Billing
 * e Backend NestJS, garantindo zero acoplamento direto de lógica com a interface.
 */
enum class KudulaPlan(
    val id: String,
    val displayName: String,
    val priceMonthly: String,
    val priceAnnual: String,
    val badge: String,
    val description: String,
    val isCustomQuote: Boolean = false
) {
    FREE(
        id = "free",
        displayName = "Free",
        priceMonthly = "$0",
        priceAnnual = "$0",
        badge = "Ativo",
        description = "O teu plano inicial com todas as fundações de IA cognitiva."
    ),
    STUDENT(
        id = "student",
        displayName = "Student",
        priceMonthly = "$5",
        priceAnnual = "$48/ano",
        badge = "Mais Popular",
        description = "Ideal para estudantes universitários e pré-vestibulares que precisam de ritmo acelerado."
    ),
    PLUS(
        id = "plus",
        displayName = "Plus",
        priceMonthly = "$10",
        priceAnnual = "$96/ano",
        badge = "Aceleração",
        description = "Potência total com Career Hub, simulação do Eu do Futuro e mapeamento avançado."
    ),
    PRO(
        id = "pro",
        displayName = "Pro",
        priceMonthly = "$20",
        priceAnnual = "$190/ano",
        badge = "Profissional",
        description = "Para profissionais e pesquisadores com Career Twin, Proof of Learning e Skills DNA."
    ),
    BUSINESS(
        id = "business",
        displayName = "Business",
        priceMonthly = "Sob consulta",
        priceAnnual = "Sob consulta",
        badge = "Equipas & PMEs",
        description = "Gestão de competências de equipas, dashboards corporativos e analytics de upskilling.",
        isCustomQuote = true
    ),
    INSTITUTION(
        id = "institution",
        displayName = "Institution",
        priceMonthly = "Sob consulta",
        priceAnnual = "Sob consulta",
        badge = "Universidades & Escolas",
        description = "Integração institucional em larga escala, relatórios pedagógicos e acreditação oficial.",
        isCustomQuote = true
    );

    val threeKeyBenefits: List<String>
        get() = when (this) {
            FREE -> listOf(
                "Tutor Socrático Kudu essencial",
                "Até 5 uploads de biblioteca por mês",
                "Índice IKC e prontidão IPA básicos"
            )
            STUDENT -> listOf(
                "IA ampliada com respostas ilimitadas",
                "Uploads alargados de PDFs e YouTube",
                "Podcasts IA e Voice Notes expandidos"
            )
            PLUS -> listOf(
                "Acesso completo ao Career Hub (MICK)",
                "Simulação temporal do Eu do Futuro",
                "Treino avançado de competências e soft skills"
            )
            PRO -> listOf(
                "Gêmeo Cognitivo de Carreira (Career Twin)",
                "Certificados criptográficos Proof of Learning",
                "Skills DNA e telemetria de precisão"
            )
            BUSINESS -> listOf(
                "Painel executivo de upskilling da equipa",
                "Rotas cognitivas corporativas personalizadas",
                "Suporte prioritário e SLA dedicado"
            )
            INSTITUTION -> listOf(
                "Integração com LMS e turmas ilimitadas",
                "Acreditação oficial de horas formativas",
                "Treinamento docente e relatórios ministeriais"
            )
        }

    val fullDetails: List<String>
        get() = when (this) {
            FREE -> listOf(
                "Tutor Kudu com modo texto e socrático básico",
                "5 documentos PDF ou sínteses YouTube por mês",
                "Voice Notes de até 30 segundos",
                "Radar IKC com 7 dimensões essenciais",
                "Diário reflexivo guiado offline (KLSE)"
            )
            STUDENT -> listOf(
                "Tudo do plano Free",
                "IA ampliada sem latência e sem fila",
                "Upload de até 30 PDFs e vídeos YouTube/mês",
                "Kudu Voice Notes de até 60 segundos com áudio",
                "Geração automática de Podcasts de estudo IA",
                "Sincronização em nuvem e multi-dispositivo"
            )
            PLUS -> listOf(
                "Tudo do plano Student",
                "Acesso irrestrito ao Hub de Carreiras (MICK)",
                "Simulador temporal do Eu do Futuro (5 a 10 anos)",
                "Mapeamento dinâmico de lacunas de competências",
                "Smart Revisão ilimitada com repetição espaçada",
                "Exportação de relatórios cognitivos em PDF"
            )
            PRO -> listOf(
                "Tudo do plano Plus",
                "Career Twin: modelo de IA treinado com o teu estilo",
                "Emissão de selos Proof of Learning com SHA-256",
                "Skills DNA com benchmark de mercado global",
                "Sessões de mentoria com especialistas Kudula",
                "Acesso antecipado a novos módulos do KCLS"
            )
            BUSINESS -> listOf(
                "Todas as ferramentas Pro para cada colaborador",
                "Dashboard de liderança com métricas de equipa",
                "Gestão centralizada de licenças corporativas",
                "Planos de desenvolvimento individual (PDI)",
                "Treinamento e onboarding personalizado para a empresa",
                "Segurança corporativa com criptografia de ponta a ponta"
            )
            INSTITUTION -> listOf(
                "Implantação à escala de campus universitário",
                "Painel do docente com monitorização de retenção",
                "APIs de integração com Canvas, Moodle e Blackboard",
                "Certificações digitais institucionais",
                "Auditoria pedagógica e relatórios de conformidade",
                "Gestor de conta e engenheiro de soluções dedicado"
            )
        }
}
