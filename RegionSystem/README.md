# Region System

Minecraft Java Edition 1.21.11 / Fabric / Official Mojang mappings.

## Region

1 Region = 4x4 chunks = 16 chunk columns.

## Pipeline

PLAYER
  |
  v
RegionServerManager
  |
  v
4x4 Region
  |
  v
ServerChunkCache
  |
  v
ChunkStatus.FULL
  |
  +--> terrain generation
  +--> structures
  +--> lighting
  +--> heightmaps
  |
  v
ChunkMap
  |
  v
Vanilla chunk network
  |
  v
CLIENT
  |
  v
ClientLevel
  |
  v
SectionRenderDispatcher
  |
  v
Section mesh rebuild
  |
  v
GPU upload
  |
  v
Render

## Design

The Region System uses a 4x4 chunk Region as the scheduling and cache unit.

The first implementation intentionally preserves Minecraft's normal
chunk generation, networking and rendering path.

This makes the system playable while allowing future optimization of:

- Region generation
- Region preloading
- Region cache
- chunk I/O
- batch generation
- mesh generation
- mesh upload
- prediction
- persistent Region storage

## Build

./gradlew build --no-daemon

## Tests

./gradlew test --no-daemon

## Development server

./gradlew runServer

## Output

build/libs/regionsystem-0.1.0.jar
