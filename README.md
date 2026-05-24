# 📱 Biblioteca+ Mobile

Aplicativo Android da plataforma Biblioteca+.

## Stack

- **Linguagem**: Kotlin
- **UI**: Jetpack Compose + Material3
- **Arquitetura**: MVVM + Clean Architecture
- **DI**: Hilt/Dagger
- **HTTP**: Retrofit + OkHttp
- **DB Local**: Room
- **Persistência**: DataStore
- **Imagens**: Coil
- **Navegação**: Navigation Compose

## Início Rápido

1. Abrir `mobile/` no **Android Studio**
2. Aguardar sync do Gradle
3. Criar `local.properties` com caminho do SDK
4. Rodar em emulador (Android 8+) ou dispositivo físico

## URL da API (Emulador)

```
http://10.0.2.2:3333/api/v1/
```

O IP `10.0.2.2` aponta para `localhost` do host quando rodando no emulador Android.

## Estrutura

```
app/src/main/java/com/bibliotecaplus/
├── di/               → Hilt modules
├── data/
│   ├── api/          → Retrofit service + DTOs
│   ├── local/        → Room DAOs + entities
│   └── repository/   → Repositories
├── domain/
│   ├── model/        → Domain models
│   └── usecase/      → Use cases
└── presentation/
    ├── navigation/   → NavGraph
    ├── auth/         → Login screens
    ├── home/         → Home screen
    ├── books/        → Book list + detail
    ├── loans/        → Loans screen
    └── profile/      → Profile screen
```

## Funcionalidades

- ✅ Login com Remember Me
- ✅ Catálogo de livros (busca + filtro)
- ✅ Detalhes do livro com reserva
- ✅ Empréstimos ativos + renovação
- ✅ Perfil do usuário
- ✅ Offline-first (Room cache)
