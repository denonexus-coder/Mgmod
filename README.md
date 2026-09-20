# MGShaders

Mod Fabric que implementa um pipeline custom de rendering para devices
PowerVR (GE8320) via MobileGlues.

## Requisitos

- Minecraft 1.21.11
- Fabric Loader >= 0.19.5
- Fabric API
- MobileGlues (com suporte a ESSL nativo)

## Arquitetura

- Java 21, Fabric Loom 1.17, official Mojang mappings
- Split source sets (main + client)
- Mixins client-only

## Decisão arquitetural

MC 1.21.11 usa FrameGraphBuilder. Optamos por **bypass** (pipeline GL
paralelo + blit) para o MVP. Integração nativa ao framegraph considerada
para fase futura.

## Roadmap

- [x] Fase 0 — Extração de alvos (docs/MIXIN_TARGETS.md)
- [x] Fase 1 — Renomear template + primeiro mixin
- [ ] Fase 2 — Config + leitura de /sdcard/MG/config.json
- [ ] Fase 3 — Tone mapping (cor vibrante)
- [ ] Fase 4 — Sol + hora do dia
- [ ] Fase 5 — Shadow map
- [ ] Fase 6 — RGB lights (SSBO)
- [ ] Fase 7 — Bloom + SSAO
- [ ] Fase 8 — ASTC redirect

## Licença

LGPL-2.1-only
