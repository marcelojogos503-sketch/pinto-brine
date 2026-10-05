# PINTObrine — Forge 1.20.1

## O que esta versão já faz
- Entidade Pintobrine própria.
- Consciência persistente de 0 a 100.
- Estados: Observando, Seguindo, Invadindo, Perseguindo e Caçando.
- Aparições aleatórias principalmente à noite.
- Pode desaparecer quando o jogador olha diretamente.
- Guarda o alvo e uma posição de "base" aproximada quando o jogador fica parado.
- Vai até a base lembrada.
- Abre portas próximas.
- Pode quebrar vidro ao invadir.
- Persegue e ataca quando a consciência sobe.
- Sons vanilla usados como placeholders para teleporte, porta e vidro.

## Como compilar
1. Instale Java 17.
2. Baixe o Forge MDK 1.20.1 (47.4.x) e coloque estes arquivos dentro da pasta do MDK, substituindo os arquivos do projeto.
3. No terminal da pasta, rode `gradlew build` (Windows) ou `./gradlew build` (Linux/macOS).
4. O jar aparecerá em `build/libs/`.
5. Coloque o jar em `.minecraft/mods` junto com Forge 1.20.1.

## Teste rápido
Depois de entrar no mundo, à noite espere a entidade aparecer. Para forçar um teste pelo chat:
`/summon pintobrine:pintobrine ~ ~ ~`

A versão 1.0 prioriza uma base funcional. Texturas/animações/sistema de sangue e sons próprios podem ser trocados pelos assets finais depois.
