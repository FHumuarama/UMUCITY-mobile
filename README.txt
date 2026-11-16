UMUCITY MOBILE 7.5.4 - PROJETO ANDROID

Conteúdo: jogo HTML original integrado a app Android em tela cheia.

GERAR APK PELO GITHUB (SEM ANDROID STUDIO):
1. Crie um repositório no GitHub e adicione TODOS os arquivos e pastas deste ZIP na raiz.
   Importante: inclua a pasta oculta .github/workflows.
2. Abra a aba Actions e escolha Gerar APK UMUCITY.
3. Clique Run workflow (ou aguarde a execução automatica após um push).
4. Ao finalizar, abra a execução e baixe o artifact UMUCITY-Mobile-7.5.4-APK.
5. Extraia o arquivo ZIP do artifact e instale app-debug.apk no Android.

GERAR APK PELO ANDROID STUDIO:
1. Abra esta pasta como um projeto no Android Studio.
2. Deixe o Gradle sincronizar usando JDK 17 e SDK Android 35.
3. Build > Build Bundle(s) / APK(s) > Build APK(s).
4. O APK de teste estará em app/build/outputs/apk/debug/app-debug.apk.

NOTAS:
- APK debug é instalável para teste, não destinado à publicação na Play Store.
- Progresso usa localStorage persistente do WebView.
- Backup Android 10+: pasta Downloads/UMUCITY. Android 8-9: arquivos privados do app.
- O APK não foi compilado/testado neste ambiente; precisa de Android SDK/Gradle.
- O jogo original continua acessível em app/src/main/assets/index.html.
- Importação de backups usa o seletor de arquivos do Android.
- A versão HTML foi preservada, com uma única adaptação para exportação nativa.
- Android WebView precisa estar atualizado no aparelho para WebGL funcionar bem.
