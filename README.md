# AutoChamada (com.example.autochamada)

Aplicativo Android nativo em Kotlin para automação e rediscagem programada de chamadas telefônicas utilizando `CallService` (Foreground Service) e `MainActivity`.

## Como gerar o arquivo `.apk` (Sem instalar nada no PC)

Este pacote já inclui o workflow **GitHub Actions** (`.github/workflows/build-apk.yml`) pré-configurado:

1. Crie um repositório gratuito no [GitHub](https://github.com/new).
2. Clique em **uploading an existing file** e arraste todos os arquivos deste pacote (incluindo a pasta `.github`).
3. Abra a aba **Actions** do seu repositório no GitHub.
4. Aguarde ~2 minutos até o workflow **Gerar APK Android (AutoChamada)** ficar verde.
5. Role até a seção **Artifacts** e clique em **AutoChamada-debug-apk** para baixar o seu `.apk` pronto para instalar no celular!

## Como gerar no Android Studio ou Terminal

- **Android Studio**: Abra a pasta do projeto, aguarde o Gradle Sync e clique em **Build > Build Bundle(s) / APK(s) > Build APK(s)**.
- **Terminal**: Execute `gradle assembleDebug`. O arquivo será salvo em `app/build/outputs/apk/debug/app-debug.apk`.
