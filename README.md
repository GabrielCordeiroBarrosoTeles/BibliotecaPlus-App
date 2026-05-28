# BibliotecaPlus App

> App mobile cross-platform da plataforma BibliotecaPlus — Kotlin Multiplatform + Compose Multiplatform

---

## O que é Kotlin Multiplatform (KMP)?

**Kotlin Multiplatform** é a tecnologia do JetBrains que permite escrever código Kotlin **uma única vez** e compilá-lo para múltiplas plataformas mantendo performance nativa — sem bridge, sem WebView, sem runtime intermediário.

```
┌────────────────────────────────────────┐
│    Módulo Compartilhado (Kotlin)        │
│                                         │
│  Camada de Dados (Ktor + DTOs)          │
│  ViewModels (Koin + Coroutines)         │
│  UI (Compose Multiplatform)             │
└────────────┬───────────────┬───────────┘
             │               │
    ┌─────────▼──────┐  ┌────▼──────────┐
    │ 🤖 Android APK │  │ 🍎 iOS Framework│
    │  Android 8.0+  │  │  iPhone · iPad │
    │   (API 26+)    │  │ x64 · ARM64    │
    └────────────────┘  └───────────────┘
```

### Por que KMP foi escolhido?

| Abordagem | Trade-off |
|-----------|-----------|
| Nativo separado (Swift + Kotlin) | Máxima performance, mas duplica todo o trabalho |
| React Native / Flutter | Cross-platform, mas runtime extra e linguagem diferente |
| **KMP ← escolhido** | Código Kotlin nativo, performance real, uma linguagem |

---

## Plataformas Suportadas

| Plataforma | Target | Requisito |
|-----------|--------|-----------|
| Android | `androidTarget` | Android 8.0+ (API 26+) |
| iOS | `iosX64` | iPhone/iPad simulador x64 |
| iOS | `iosArm64` | iPhone/iPad físico |
| iOS | `iosSimulatorArm64` | Simulador Apple Silicon |

---

## Stack de Tecnologias

| Biblioteca | Versão | Função | Plataforma |
|-----------|--------|--------|-----------|
| Kotlin | 2.1.0 | Linguagem | Todas |
| Compose Multiplatform | 1.7.3 | UI declarativa compartilhada | Android + iOS |
| Ktor Client | 3.0.3 | HTTP requests + auth | Todas |
| kotlinx.serialization | 1.7.3 | JSON parsing | Todas |
| kotlinx.coroutines | 1.9.0 | Async / suspend functions | Todas |
| Koin | 4.0.0 | Injeção de dependência | Todas |
| Coil 3 | 3.0.4 | Carregamento de imagens | Todas |
| multiplatform-settings | 1.2.0 | Persistência de dados (token) | Todas |
| lifecycle-viewmodel | 2.8.4 | ViewModel + estado reativo | Todas |
| navigation-compose | 2.8.0 | Navegação entre telas | Todas |
| ktor-client-okhttp | 3.0.3 | Engine HTTP | Android |
| ktor-client-darwin | 3.0.3 | Engine HTTP (URLSession) | iOS |
| AGP (Android) | 8.7.0 | Build Android | Android |

---

## Pré-requisitos

### Android
- Android Studio Ladybug ou superior
- JDK 17
- Android SDK (compileSdk 35, minSdk 26)
- Dispositivo físico ou emulador (Android 8.0+)
- API BibliotecaPlus rodando e acessível

### iOS
- macOS com Xcode 15+
- CocoaPods
- Simulador iOS ou dispositivo físico

---

## Instalação e Build

### Android

```bash
# Clonar o projeto
git clone <repo>
cd BibliotecaPlus-App

# Configurar a URL da API
# Editar: app/src/main/res/values/strings.xml ou via build config

# Build do APK de debug
./gradlew :app:assembleDebug

# Instalar em dispositivo conectado
adb install app/build/outputs/apk/debug/app-debug.apk

# Ou rodar direto via Android Studio:
# Run → app → selecionar dispositivo
```

### iOS

```bash
# Gerar o framework compartilhado
./gradlew :shared:linkDebugFrameworkIosSimulatorArm64

# Instalar pods
cd iosApp
pod install

# Abrir no Xcode
open iosApp.xcworkspace
# → Run (⌘R)
```

---

## Estrutura do Projeto

```
BibliotecaPlus-App/
├── shared/                            # Módulo compartilhado (KMP)
│   ├── src/
│   │   ├── commonMain/kotlin/         # Código 100% compartilhado
│   │   │   └── com/bibliotecaplus/
│   │   │       ├── data/
│   │   │       │   └── api/
│   │   │       │       ├── BibliotecaApiClient.kt  # Ktor + endpoints
│   │   │       │       └── Dtos.kt                 # Todos os DTOs
│   │   │       └── presentation/
│   │   │           ├── auth/
│   │   │           │   ├── LoginScreen.kt
│   │   │           │   └── LoginViewModel.kt
│   │   │           ├── home/
│   │   │           │   ├── HomeScreen.kt
│   │   │           │   └── HomeViewModel.kt
│   │   │           ├── books/
│   │   │           │   ├── BookListScreen.kt
│   │   │           │   ├── BookDetailScreen.kt
│   │   │           │   └── BookViewModel.kt
│   │   │           ├── loans/
│   │   │           │   ├── LoansScreen.kt
│   │   │           │   └── LoansViewModel.kt
│   │   │           ├── profile/
│   │   │           │   ├── ProfileScreen.kt
│   │   │           │   ├── EditProfileScreen.kt
│   │   │           │   └── ProfileViewModel.kt
│   │   │           └── notifications/
│   │   │               ├── NotificationsScreen.kt
│   │   │               └── NotificationsViewModel.kt
│   │   ├── androidMain/               # Código específico Android
│   │   └── iosMain/                   # Código específico iOS
│   └── build.gradle.kts
├── app/                               # Android App shell
│   ├── src/main/kotlin/
│   │   └── MainActivity.kt            # ComponentActivity → SharedApp()
│   └── build.gradle.kts
├── iosApp/                            # iOS App shell
│   ├── iOSApp.swift                   # SwiftUI entry point → SharedApp
│   └── iosApp.xcworkspace
└── build.gradle.kts
```

---

## Screens e Funcionalidades

### LoginScreen
- Login com email e senha
- Checkbox "Lembrar por 30 dias"
- Logo da BibliotecaPlus
- Armazena `accessToken` + `refreshToken` via `multiplatform-settings`

### HomeScreen (Bottom Navigation — 4 abas)

**Aba Home:**
- Saudação personalizada com nome do usuário
- 3 mini-cards de resumo: Empréstimos Ativos · Vencendo em Breve · Em Atraso
- Alertas visuais: banner amarelo (vencimento próximo) e vermelho (atrasado)
- Lista de empréstimos ativos com card detalhado
- Carrossel horizontal **"Em Alta"** — livros mais emprestados com capa e contagem

**Aba Livros (BookListScreen):**
- Busca em tempo real por título, ISBN ou autor
- Chips de filtro: Todos · Disponíveis · Indisponíveis
- Grid 2 colunas com capas carregadas via Coil 3
- Tap no livro → BookDetailScreen

**Aba Empréstimos (LoansScreen):**
- Chips de status: Todos · Ativos · Em Atraso · Devolvidos
- Card por empréstimo: capa, título, autor, datas, status colorido
- Botão **"Renovar"** por empréstimo ativo (máx 2 renovações)

**Aba Perfil (ProfileScreen):**
- Exibe: nome, email, cargo, matrícula, telefone, avatar
- Botão editar perfil → EditProfileScreen
- Botão logout (limpa tokens)

### BookDetailScreen
- Detalhes completos: título, autor, editora, ISBN, sinopse, idioma, ano
- Badge de disponibilidade
- Botão **"Solicitar Reserva"** (cria reserva na API)

### EditProfileScreen
- Formulário para atualizar nome, telefone, matrícula
- `PATCH /users/me`

### NotificationsScreen
- Lista de notificações com tipo, título, corpo e tempo relativo
- Marcar como lida (individual) e "marcar todas"
- Ícone do sino no TopAppBar com badge de não lidas

---

## API Client (`BibliotecaApiClient.kt`)

Todos os requests passam por `safeCall { }` que retorna `Result<T>`:

```kotlin
// Exemplo de uso
val result = api.getBooks(page = 1, search = "kotlin")
result
    .onSuccess { paginated -> /* paginated.data, paginated.meta */ }
    .onFailure { error -> /* error.message */ }
```

**Endpoints consumidos:**

```
POST auth/login                          → AuthResponse
POST auth/logout
GET  books?page&limit&search&available   → PaginatedData<BookDto>
GET  books/{id}                          → BookDto
GET  books/trending?limit=6              → List<TrendingBookDto>
GET  loans?page&limit&status             → PaginatedData<LoanDto>
POST loans/{id}/renew                    → LoanDto
GET  users/me                            → UserDto
PATCH users/me                           → UserDto
POST reservations { bookId, notes? }     → ReservationDto
DELETE reservations/{id}
GET  notifications                       → List<NotificationDto>
POST notifications/{id}/read
POST notifications/read-all
```

---

## Credenciais de Teste (seed:dev)

| Usuário | Email | Senha | Role |
|---------|-------|-------|------|
| Cordeiro (Admin) | `cordeiro@adm.com` | `123456` | ADMIN |
| Cordeiro (Aluno) | `cordeiro@aluno.com` | `123456` | STUDENT |
| Cordeiro (Prof) | `cordeiro@prof.com` | `123456` | PROFESSOR |

> O aluno tem 2 empréstimos ativos, 1 atrasado e 1 devolvido para demonstração.
