# AutoChamada Android (projeto inicial)

Aplicativo Android nativo em Kotlin com:
- Tela simples com **Chamar**, **Desligar / Parar** e engrenagem.
- Número de destino configurável na área administrativa.
- Senha administrativa inicial: **1234** (altere ao primeiro uso).
- Permissões de chamada e estado telefônico solicitadas em tempo de execução.
- Tentativa de nova chamada 3 segundos após o sistema reportar que a chamada terminou.

## Abrir e compilar
1. Instale o Android Studio.
2. Abra a pasta `AutoChamadaAndroid` no Android Studio.
3. Aguarde a sincronização do Gradle.
4. Conecte um telefone Android físico, aceite as permissões e execute **Run**.
5. Para gerar APK: **Build > Build APK(s)**.

## Limitações importantes
Este é um projeto inicial, não um APK pré-compilado. O Android e alguns fabricantes limitam o início de chamadas e a observação do estado telefônico em segundo plano. O comportamento varia por versão, permissões, SIM e fabricante; teste em aparelho real. Em particular, a API antiga de estado de chamada pode ter diferenças nas versões atuais do Android. A implementação não consegue garantir rediscagem em todos os aparelhos, nem interromper uma chamada já conectada ao tocar em “Desligar / Parar”; esse botão cancela as novas tentativas. Para uma versão de produção, recomenda-se testar e adaptar o serviço em primeiro plano e o tratamento de chamadas à versão Android alvo.
