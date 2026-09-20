# MIXIN_TARGETS — Alvos extraídos do mappings 1.21.11

Gerado em: 2026-09-20 22:04:01 UTC
Mappings: /sdcard/MG/client_mojang_Mappings_official_1.21.11.txt

---

# TIER 1 — Mandatory

## net.minecraft.client.Camera

**Tier:** 1  
**Uso:** Posição/rotação da câmera (sun ray, frustum de sombra)

**Nome obfuscado:** `ger`

```
net.minecraft.client.Camera -> ger:
    float DEFAULT_CAMERA_DISTANCE -> a
    org.joml.Vector3f FORWARDS -> b
    org.joml.Vector3f UP -> c
    org.joml.Vector3f LEFT -> d
    boolean initialized -> e
    net.minecraft.world.level.Level level -> f
    net.minecraft.world.entity.Entity entity -> g
    net.minecraft.world.phys.Vec3 position -> h
    net.minecraft.core.BlockPos$MutableBlockPos blockPosition -> i
    org.joml.Vector3f forwards -> j
    org.joml.Vector3f up -> k
    org.joml.Vector3f left -> l
    float xRot -> m
    float yRot -> n
    org.joml.Quaternionf rotation -> o
    boolean detached -> p
    float eyeHeight -> q
    float eyeHeightOld -> r
    float partialTickTime -> s
    net.minecraft.world.attribute.EnvironmentAttributeProbe attributeProbe -> t
    30:53:void <init>() -> <init>
    56:100:void setup(net.minecraft.world.level.Level,net.minecraft.world.entity.Entity,boolean,boolean,float) -> a
    103:108:void tick() -> c
    111:126:float getMaxZoom(float) -> a
    130:132:void move(float,float,float) -> a
    135:147:void setRotation(float,float) -> a
    150:151:void setPosition(double,double,double) -> a
    154:156:void setPosition(net.minecraft.world.phys.Vec3) -> a
    160:160:net.minecraft.world.phys.Vec3 position() -> b
    164:164:net.minecraft.core.BlockPos blockPosition() -> d
    168:168:float xRot() -> e
    172:172:float yRot() -> f
    177:177:float yaw() -> a
    181:181:org.joml.Quaternionf rotation() -> g
    185:185:net.minecraft.world.entity.Entity entity() -> h
    189:189:boolean isInitialized() -> i
    193:193:boolean isDetached() -> j
    197:197:net.minecraft.world.attribute.EnvironmentAttributeProbe attributeProbe() -> k
    201:210:net.minecraft.client.Camera$NearPlane getNearPlane() -> l
    214:245:net.minecraft.world.level.material.FogType getFluidInCamera() -> m
    249:249:org.joml.Vector3fc forwardVector() -> n
    253:253:org.joml.Vector3fc upVector() -> o
    257:257:org.joml.Vector3fc leftVector() -> p
    261:265:void reset() -> q
    268:268:float getPartialTickTime() -> r
    33:35:void <clinit>() -> <clinit>
```

---

## net.minecraft.client.multiplayer.ClientLevel

**Tier:** 1  
**Uso:** Hora do mundo (sun position over time)

**Nome obfuscado:** `hif`

```
net.minecraft.client.multiplayer.ClientLevel -> hif:
    org.slf4j.Logger LOGGER -> b
    net.minecraft.network.chat.Component DEFAULT_QUIT_MESSAGE -> a
    double FLUID_PARTICLE_SPAWN_OFFSET -> c
    int NORMAL_LIGHT_UPDATES_PER_FRAME -> d
    int LIGHT_UPDATE_QUEUE_SIZE_THRESHOLD -> e
    net.minecraft.world.level.entity.EntityTickList tickingEntities -> f
    net.minecraft.world.level.entity.TransientEntitySectionManager entityStorage -> B
    net.minecraft.client.multiplayer.ClientPacketListener connection -> C
    net.minecraft.client.renderer.LevelRenderer levelRenderer -> D
    net.minecraft.client.renderer.LevelEventHandler levelEventHandler -> E
    net.minecraft.client.multiplayer.ClientLevel$ClientLevelData clientLevelData -> F
    net.minecraft.world.TickRateManager tickRateManager -> G
    net.minecraft.client.renderer.EndFlashState endFlashState -> H
    net.minecraft.client.Minecraft minecraft -> I
    java.util.List players -> J
    java.util.List dragonParts -> K
    java.util.Map mapData -> L
    int skyFlashTime -> M
    it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap tintCaches -> N
    net.minecraft.client.multiplayer.ClientChunkCache chunkSource -> O
    java.util.Deque lightUpdateQueue -> P
    int serverSimulationDistance -> Q
    net.minecraft.client.multiplayer.prediction.BlockStatePredictionHandler blockStatePredictionHandler -> R
    java.util.Set globallyRenderedBlockEntities -> S
    net.minecraft.client.multiplayer.ClientExplosionTracker explosionTracker -> T
    net.minecraft.world.level.border.WorldBorder worldBorder -> U
    net.minecraft.world.attribute.EnvironmentAttributeSystem environmentAttributes -> V
    int seaLevel -> W
    boolean tickDayTime -> X
    java.util.Set MARKER_PARTICLE_ITEMS -> Y
    162:167:void handleBlockChangedAck(int) -> b
    171:175:void onBlockEntityAdded(net.minecraft.world.level.block.entity.BlockEntity) -> a
    178:178:java.util.Set getGloballyRenderedBlockEntities() -> a
    182:185:void setServerVerifiedBlockState(net.minecraft.core.BlockPos,net.minecraft.world.level.block.state.BlockState,int) -> b
    188:198:void syncBlockState(net.minecraft.core.BlockPos,net.minecraft.world.level.block.state.BlockState,net.minecraft.world.phys.Vec3) -> a
    201:201:net.minecraft.client.multiplayer.prediction.BlockStatePredictionHandler getBlockStatePredictionHandler() -> b
    206:214:boolean setBlock(net.minecraft.core.BlockPos,net.minecraft.world.level.block.state.BlockState,int,int) -> a
    128:239:void <init>(net.minecraft.client.multiplayer.ClientPacketListener,net.minecraft.client.multiplayer.ClientLevel$ClientLevelData,net.minecraft.resources.ResourceKey,net.minecraft.core.Holder,int,int,net.minecraft.client.renderer.LevelRenderer,boolean,long,int) -> <init>
    242:255:net.minecraft.world.attribute.EnvironmentAttributeSystem$Builder addEnvironmentAttributeLayers(net.minecraft.world.attribute.EnvironmentAttributeSystem$Builder) -> a
    259:260:void queueLightUpdate(java.lang.Runnable) -> a
    263:273:void pollLightUpdates() -> d
    276:276:net.minecraft.client.renderer.EndFlashState endFlashState() -> e
    280:305:void tick(java.util.function.BooleanSupplier) -> a
    308:312:void tickTime() -> o
    315:318:void setTimeFromServer(long,long,boolean) -> a
    321:321:java.lang.Iterable entitiesForRendering() -> f
    325:331:void tickEntities() -> g
    334:334:boolean isTickingEntity(net.minecraft.world.entity.Entity) -> a
    339:339:boolean shouldTickDeath(net.minecraft.world.entity.Entity) -> h
    343:353:void tickNonPassenger(net.minecraft.world.entity.Entity) -> c
    356:373:void tickPassenger(net.minecraft.world.entity.Entity,net.minecraft.world.entity.Entity) -> a
    376:379:void unload(net.minecraft.world.level.chunk.LevelChunk) -> a
    382:384:void onChunkLoaded(net.minecraft.world.level.ChunkPos) -> a
    387:388:void onSectionBecomingNonEmpty(long) -> b
    391:392:void clearTintCaches() -> h
    396:396:boolean hasChunk(int,int) -> b
    400:400:int getEntityCount() -> i
    404:406:void addEntity(net.minecraft.world.entity.Entity) -> d
    409:414:void removeEntity(int,net.minecraft.world.entity.Entity$RemovalReason) -> a
    419:427:java.util.List getPushableEntities(net.minecraft.world.entity.Entity,net.minecraft.world.phys.AABB) -> k
    432:432:net.minecraft.world.entity.Entity getEntity(int) -> a
    436:437:void disconnect(net.minecraft.network.chat.Component) -> a
    440:449:void animateTick(int,int,int) -> b
    452:460:net.minecraft.world.level.block.Block getMarkerParticleTarget() -> p
    464:496:void doAnimateTick(int,int,int,int,net.minecraft.util.RandomSource,net.minecraft.world.level.block.Block,net.minecraft.core.BlockPos$MutableBlockPos) -> a
    499:523:void trySpawnDripParticles(net.minecraft.core.BlockPos,net.minecraft.world.level.block.state.BlockState,net.minecraft.core.particles.ParticleOptions,boolean) -> a
    526:532:void spawnParticle(net.minecraft.core.BlockPos,net.minecraft.core.particles.ParticleOptions,net.minecraft.world.phys.shapes.VoxelShape,double) -> a
    535:536:void spawnFluidParticle(double,double,double,double,double,net.minecraft.core.particles.ParticleOptions) -> a
    540:546:net.minecraft.CrashReportCategory fillReportDetails(net.minecraft.CrashReport) -> a
    551:554:void playSeededSound(net.minecraft.world.entity.Entity,double,double,double,net.minecraft.core.Holder,net.minecraft.sounds.SoundSource,float,float,long) -> a
    558:561:void playSeededSound(net.minecraft.world.entity.Entity,net.minecraft.world.entity.Entity,net.minecraft.core.Holder,net.minecraft.sounds.SoundSource,float,float,long) -> a
    565:566:void playLocalSound(net.minecraft.world.entity.Entity,net.minecraft.sounds.SoundEvent,net.minecraft.sounds.SoundSource,float,float) -> a
    570:573:void playPlayerSound(net.minecraft.sounds.SoundEvent,net.minecraft.sounds.SoundSource,float,float) -> a
    577:578:void playLocalSound(double,double,double,net.minecraft.sounds.SoundEvent,net.minecraft.sounds.SoundSource,float,float,boolean) -> a
    581:591:void playSound(double,double,double,net.minecraft.sounds.SoundEvent,net.minecraft.sounds.SoundSource,float,float,boolean,long) -> a
    595:603:void createFireworks(double,double,double,double,double,double,java.util.List) -> a
    607:608:void sendPacketToServer(net.minecraft.network.protocol.Packet) -> a
    612:612:net.minecraft.world.level.border.WorldBorder getWorldBorder() -> w
    617:617:net.minecraft.world.item.crafting.RecipeAccess recipeAccess() -> aa
    622:622:net.minecraft.world.TickRateManager tickRateManager() -> y
    627:627:net.minecraft.world.attribute.EnvironmentAttributeSystem environmentAttributes() -> c
    632:632:net.minecraft.world.ticks.LevelTickAccess getBlockTicks() -> af
    637:637:net.minecraft.world.ticks.LevelTickAccess getFluidTicks() -> ae
    642:642:net.minecraft.client.multiplayer.ClientChunkCache getChunkSource() -> j
    647:647:net.minecraft.world.level.saveddata.maps.MapItemSavedData getMapData(net.minecraft.world.level.saveddata.maps.MapId) -> a
    651:652:void overrideMapData(net.minecraft.world.level.saveddata.maps.MapId,net.minecraft.world.level.saveddata.maps.MapItemSavedData) -> a
    656:656:net.minecraft.world.scores.Scoreboard getScoreboard() -> ab
    661:662:void sendBlockUpdated(net.minecraft.core.BlockPos,net.minecraft.world.level.block.state.BlockState,net.minecraft.world.level.block.state.BlockState,int) -> a
    666:667:void setBlocksDirty(net.minecraft.core.BlockPos,net.minecraft.world.level.block.state.BlockState,net.minecraft.world.level.block.state.BlockState) -> b
    670:671:void setSectionDirtyWithNeighbors(int,int,int) -> c
    674:675:void setSectionRangeDirty(int,int,int,int,int,int) -> b
    679:680:void destroyBlockProgress(int,net.minecraft.core.BlockPos,int) -> a
    684:685:void globalLevelEvent(int,net.minecraft.core.BlockPos,int) -> b
    690:702:void levelEvent(net.minecraft.world.entity.Entity,int,net.minecraft.core.BlockPos,int) -> a
    706:707:void addParticle(net.minecraft.core.particles.ParticleOptions,double,double,double,double,double,double) -> a
    711:712:void addParticle(net.minecraft.core.particles.ParticleOptions,boolean,boolean,double,double,double,double,double,double) -> a
    716:717:void addAlwaysVisibleParticle(net.minecraft.core.particles.ParticleOptions,double,double,double,double,double,double) -> b
    721:722:void addAlwaysVisibleParticle(net.minecraft.core.particles.ParticleOptions,boolean,double,double,double,double,double,double) -> a
    726:754:void doAddParticle(net.minecraft.core.particles.ParticleOptions,boolean,boolean,double,double,double,double,double,double) -> b
    757:773:net.minecraft.server.level.ParticleStatus calculateParticleLevel(boolean) -> a
    778:778:java.util.List players() -> E
    783:783:java.util.List dragonParts() -> k
    788:788:net.minecraft.core.Holder getUncachedNoiseBiome(int,int,int) -> a
    792:792:int getSkyFlashTime() -> q
    797:798:void setSkyFlashTime(int) -> c
    802:812:float getShade(net.minecraft.core.Direction,boolean) -> a
    818:819:int getBlockTint(net.minecraft.core.BlockPos,net.minecraft.world.level.ColorResolver) -> a
    823:844:int calculateBlockTint(net.minecraft.core.BlockPos,net.minecraft.world.level.ColorResolver) -> b
    849:850:void setRespawnData(net.minecraft.world.level.storage.LevelData$RespawnData) -> a
    854:854:net.minecraft.world.level.storage.LevelData$RespawnData getRespawnData() -> C
    859:859:java.lang.String toString() -> toString
    864:864:net.minecraft.client.multiplayer.ClientLevel$ClientLevelData getLevelData() -> l
    870:870:void gameEvent(net.minecraft.core.Holder,net.minecraft.world.phys.Vec3,net.minecraft.world.level.gameevent.GameEvent$Context) -> a
    873:873:java.util.Map getAllMapData() -> m
    877:878:void addMapData(java.util.Map) -> a
    988:988:net.minecraft.world.level.entity.LevelEntityGetter getEntities() -> M
    1044:1044:java.lang.String gatherChunkSourceStats() -> P
    1049:1083:void addDestroyBlockEffect(net.minecraft.core.BlockPos,net.minecraft.world.level.block.state.BlockState) -> b
    1086:1123:void addBreakingBlockEffect(net.minecraft.core.BlockPos,net.minecraft.core.Direction) -> d
    1126:1127:void setServerSimulationDistance(int) -> i
    1130:1130:int getServerSimulationDistance() -> n
    1135:1135:net.minecraft.world.flag.FeatureFlagSet enabledFeatures() -> Q
    1140:1140:net.minecraft.world.item.alchemy.PotionBrewing potionBrewing() -> R
    1145:1145:net.minecraft.world.level.block.entity.FuelValues fuelValues() -> S
    1150:1150:void explode(net.minecraft.world.entity.Entity,net.minecraft.world.damagesource.DamageSource,net.minecraft.world.level.ExplosionDamageCalculator,double,double,double,float,boolean,net.minecraft.world.level.Level$ExplosionInteraction,net.minecraft.core.particles.ParticleOptions,net.minecraft.core.particles.ParticleOptions,net.minecraft.util.random.WeightedList,net.minecraft.core.Holder) -> a
    1154:1154:int getSeaLevel() -> V
    1159:1159:int getClientLeafTintColor(net.minecraft.core.BlockPos) -> x
    1164:1165:void registerForCleaning(net.minecraft.client.multiplayer.CacheSlot) -> registerForCleaning
    1168:1169:void trackExplosionEffects(net.minecraft.world.phys.Vec3,float,int,net.minecraft.util.random.WeightedList) -> a
    120:120:net.minecraft.world.level.storage.LevelData getLevelData() -> D_
    120:120:java.util.Collection dragonParts() -> v
    120:120:net.minecraft.world.level.chunk.ChunkSource getChunkSource() -> ac
    120:120:net.minecraft.world.attribute.EnvironmentAttributeReader environmentAttributes() -> ad
    1059:1082:void lambda$addDestroyBlockEffect$16(net.minecraft.core.BlockPos,net.minecraft.world.level.block.state.BlockState,double,double,double,double,double,double) -> a
    750:750:java.lang.String lambda$doAddParticle$15(double,double,double) -> a
    749:749:java.lang.String lambda$doAddParticle$14(net.minecraft.core.particles.ParticleOptions) -> a
    544:544:java.lang.String lambda$fillReportDetails$13() -> r
    543:543:java.lang.String lambda$fillReportDetails$12() -> t
    542:542:java.lang.String lambda$fillReportDetails$11() -> u
    391:391:void lambda$clearTintCaches$10(net.minecraft.world.level.ColorResolver,net.minecraft.client.color.block.BlockTintCache) -> a
    382:382:void lambda$onChunkLoaded$9(net.minecraft.world.level.ChunkPos,net.minecraft.world.level.ColorResolver,net.minecraft.client.color.block.BlockTintCache) -> a
    346:346:java.lang.String lambda$tickNonPassenger$8(net.minecraft.world.entity.Entity) -> e
    326:330:void lambda$tickEntities$7(net.minecraft.world.entity.Entity) -> i
    252:252:java.lang.Float lambda$addEnvironmentAttributeLayers$6(java.lang.Float,int) -> a
    246:249:java.lang.Integer lambda$addEnvironmentAttributeLayers$5(int,java.lang.Integer,int) -> a
    144:148:void lambda$new$4(it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap) -> a
    147:147:int lambda$new$3(net.minecraft.core.BlockPos) -> a
    146:146:int lambda$new$2(net.minecraft.core.BlockPos) -> b
    145:145:int lambda$new$1(net.minecraft.core.BlockPos) -> c
```

---

## net.minecraft.client.renderer.LevelRenderer

**Tier:** 1  
**Uso:** Pipeline principal — injeção antes/depois do render

**Nome obfuscado:** `hoh`

```
net.minecraft.client.renderer.LevelRenderer -> hoh:
    net.minecraft.resources.Identifier TRANSPARENCY_POST_CHAIN_ID -> f
    net.minecraft.resources.Identifier ENTITY_OUTLINE_POST_CHAIN_ID -> g
    int SECTION_SIZE -> a
    int HALF_SECTION_SIZE -> b
    int NEARBY_SECTION_DISTANCE_IN_BLOCKS -> c
    int MINIMUM_TRANSPARENT_SORT_COUNT -> h
    float CHUNK_VISIBILITY_THRESHOLD -> i
    net.minecraft.client.Minecraft minecraft -> j
    net.minecraft.client.renderer.entity.EntityRenderDispatcher entityRenderDispatcher -> k
    net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher blockEntityRenderDispatcher -> l
    net.minecraft.client.renderer.RenderBuffers renderBuffers -> m
    net.minecraft.client.renderer.SkyRenderer skyRenderer -> n
    net.minecraft.client.renderer.CloudRenderer cloudRenderer -> o
    net.minecraft.client.renderer.WorldBorderRenderer worldBorderRenderer -> p
    net.minecraft.client.renderer.WeatherEffectRenderer weatherEffectRenderer -> q
    net.minecraft.client.renderer.state.ParticlesRenderState particlesRenderState -> r
    net.minecraft.client.renderer.debug.DebugRenderer debugRenderer -> d
    net.minecraft.client.renderer.debug.GameTestBlockHighlightRenderer gameTestBlockHighlightRenderer -> e
    net.minecraft.client.multiplayer.ClientLevel level -> s
    net.minecraft.client.renderer.SectionOcclusionGraph sectionOcclusionGraph -> t
    it.unimi.dsi.fastutil.objects.ObjectArrayList visibleSections -> u
    it.unimi.dsi.fastutil.objects.ObjectArrayList nearbyVisibleSections -> v
    net.minecraft.client.renderer.ViewArea viewArea -> w
    int ticks -> x
    it.unimi.dsi.fastutil.ints.Int2ObjectMap destroyingBlocks -> y
    it.unimi.dsi.fastutil.longs.Long2ObjectMap destructionProgress -> z
    com.mojang.blaze3d.pipeline.RenderTarget entityOutlineTarget -> A
    net.minecraft.client.renderer.LevelTargetBundle targets -> B
    int lastCameraSectionX -> C
    int lastCameraSectionY -> D
    int lastCameraSectionZ -> E
    double prevCamX -> F
    double prevCamY -> G
    double prevCamZ -> H
    double prevCamRotX -> I
    double prevCamRotY -> J
    net.minecraft.client.renderer.chunk.SectionRenderDispatcher sectionRenderDispatcher -> K
    int lastViewDistance -> L
    boolean captureFrustum -> M
    net.minecraft.client.renderer.culling.Frustum capturedFrustum -> N
    net.minecraft.core.BlockPos lastTranslucentSortBlockPos -> O
    int translucencyResortIterationIndex -> P
    net.minecraft.client.renderer.state.LevelRenderState levelRenderState -> Q
    net.minecraft.client.renderer.SubmitNodeStorage submitNodeStorage -> R
    net.minecraft.client.renderer.feature.FeatureRenderDispatcher featureRenderDispatcher -> S
    com.mojang.blaze3d.textures.GpuSampler chunkLayerSampler -> T
    net.minecraft.gizmos.SimpleGizmoCollector collectedGizmos -> U
    net.minecraft.client.renderer.LevelRenderer$FinalizedGizmos finalizedGizmos -> V
    143:208:void <init>(net.minecraft.client.Minecraft,net.minecraft.client.renderer.entity.EntityRenderDispatcher,net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher,net.minecraft.client.renderer.RenderBuffers,net.minecraft.client.renderer.state.LevelRenderState,net.minecraft.client.renderer.feature.FeatureRenderDispatcher) -> <init>
    213:224:void close() -> close
    228:233:void onResourceManagerReload(net.minecraft.server.packs.resources.ResourceManager) -> a
    236:240:void initOutline() -> a
    243:254:net.minecraft.client.renderer.PostChain getTransparencyChain() -> B
    258:261:void doEntityOutline() -> b
    264:264:boolean shouldShowEntityOutlines() -> c
    268:289:void setLevel(net.minecraft.client.multiplayer.ClientLevel) -> a
    292:294:void clearVisibleSections() -> C
    297:328:void allChanged() -> d
    331:335:void resize(int,int) -> a
    338:349:java.lang.String getSectionStatistics() -> e
    354:354:net.minecraft.client.renderer.chunk.SectionRenderDispatcher getSectionRenderDispatcher() -> f
    358:358:double getTotalSections() -> g
    362:362:double getLastViewDistance() -> h
    366:372:int countRenderedSections() -> i
    376:380:void resetSampler() -> j
    383:386:java.lang.String getEntityStatistics() -> k
    390:445:void cullTerrain(net.minecraft.client.Camera,net.minecraft.client.renderer.culling.Frustum,boolean) -> a
    448:448:net.minecraft.client.renderer.culling.Frustum offsetFrustum(net.minecraft.client.renderer.culling.Frustum) -> a
    452:457:void applyFrustum(net.minecraft.client.renderer.culling.Frustum) -> b
    460:461:void addRecentlyCompiledSection(net.minecraft.client.renderer.chunk.SectionRenderDispatcher$RenderSection) -> a
    465:475:net.minecraft.client.renderer.culling.Frustum prepareCullFrustum(org.joml.Matrix4f,org.joml.Matrix4f,net.minecraft.world.phys.Vec3) -> a
    479:616:void renderLevel(com.mojang.blaze3d.resource.GraphicsResourceAllocator,net.minecraft.client.DeltaTracker,boolean,net.minecraft.client.Camera,org.joml.Matrix4f,org.joml.Matrix4f,org.joml.Matrix4f,com.mojang.blaze3d.buffers.GpuBufferSlice,org.joml.Vector4f,boolean) -> a
    619:747:void addMainPass(com.mojang.blaze3d.framegraph.FrameGraphBuilder,net.minecraft.client.renderer.culling.Frustum,org.joml.Matrix4f,com.mojang.blaze3d.buffers.GpuBufferSlice,boolean,net.minecraft.client.renderer.state.LevelRenderState,net.minecraft.client.DeltaTracker,net.minecraft.util.profiling.ProfilerFiller) -> a
    750:769:void addParticlesPass(com.mojang.blaze3d.framegraph.FrameGraphBuilder,com.mojang.blaze3d.buffers.GpuBufferSlice) -> a
    772:779:void addCloudsPass(com.mojang.blaze3d.framegraph.FrameGraphBuilder,net.minecraft.client.CloudStatus,net.minecraft.world.phys.Vec3,long,float,int,float) -> a
    782:799:void addWeatherPass(com.mojang.blaze3d.framegraph.FrameGraphBuilder,com.mojang.blaze3d.buffers.GpuBufferSlice) -> b
    802:830:void addLateDebugPass(com.mojang.blaze3d.framegraph.FrameGraphBuilder,net.minecraft.client.renderer.state.CameraRenderState,com.mojang.blaze3d.buffers.GpuBufferSlice,org.joml.Matrix4f) -> a
    833:869:void extractVisibleEntities(net.minecraft.client.Camera,net.minecraft.client.renderer.culling.Frustum,net.minecraft.client.DeltaTracker,net.minecraft.client.renderer.state.LevelRenderState) -> a
    872:882:void submitEntities(com.mojang.blaze3d.vertex.PoseStack,net.minecraft.client.renderer.state.LevelRenderState,net.minecraft.client.renderer.SubmitNodeCollector) -> a
    885:929:void extractVisibleBlockEntities(net.minecraft.client.Camera,float,net.minecraft.client.renderer.state.LevelRenderState) -> a
    932:943:void submitBlockEntities(com.mojang.blaze3d.vertex.PoseStack,net.minecraft.client.renderer.state.LevelRenderState,net.minecraft.client.renderer.SubmitNodeStorage) -> a
    946:965:void extractBlockDestroyAnimation(net.minecraft.client.Camera,net.minecraft.client.renderer.state.LevelRenderState) -> a
    971:985:void renderBlockDestroyAnimation(com.mojang.blaze3d.vertex.PoseStack,net.minecraft.client.renderer.MultiBufferSource$BufferSource,net.minecraft.client.renderer.state.LevelRenderState) -> a
    988:1012:void extractBlockOutline(net.minecraft.client.Camera,net.minecraft.client.renderer.state.LevelRenderState) -> b
    1015:1031:void renderBlockOutline(net.minecraft.client.renderer.MultiBufferSource$BufferSource,com.mojang.blaze3d.vertex.PoseStack,boolean,net.minecraft.client.renderer.state.LevelRenderState) -> a
    1034:1037:void checkPoseStack(com.mojang.blaze3d.vertex.PoseStack) -> a
    1040:1040:net.minecraft.client.renderer.entity.state.EntityRenderState extractEntity(net.minecraft.world.entity.Entity,float) -> a
    1044:1060:void scheduleTranslucentSectionResort(net.minecraft.world.phys.Vec3) -> a
    1063:1072:void scheduleResort(net.minecraft.client.renderer.chunk.SectionRenderDispatcher$RenderSection,net.minecraft.client.renderer.chunk.TranslucencyPointOfView,net.minecraft.world.phys.Vec3,boolean,boolean) -> a
    1075:1129:net.minecraft.client.renderer.chunk.ChunkSectionsToRender prepareChunkRenders(org.joml.Matrix4fc,double,double,double) -> a
    1133:1134:void endFrame() -> l
    1137:1138:void captureFrustum() -> m
    1141:1142:void killFrustum() -> n
    1145:1151:void tick(net.minecraft.client.Camera) -> a
    1154:1169:void removeBlockBreakingProgress() -> D
    1172:1178:void removeProgress(net.minecraft.server.level.BlockDestructionProgress) -> a
    1181:1223:void addSkyPass(com.mojang.blaze3d.framegraph.FrameGraphBuilder,net.minecraft.client.Camera,com.mojang.blaze3d.buffers.GpuBufferSlice) -> a
    1226:1229:boolean doesMobEffectBlockSky(net.minecraft.client.Camera) -> b
    1233:1281:void compileSections(net.minecraft.client.Camera) -> c
    1284:1299:void renderHitOutline(com.mojang.blaze3d.vertex.PoseStack,com.mojang.blaze3d.vertex.VertexConsumer,double,double,double,net.minecraft.client.renderer.state.BlockOutlineRenderState,int,float) -> a
    1302:1303:void blockChanged(net.minecraft.world.level.BlockGetter,net.minecraft.core.BlockPos,net.minecraft.world.level.block.state.BlockState,net.minecraft.world.level.block.state.BlockState,int) -> a
    1306:1313:void setBlockDirty(net.minecraft.core.BlockPos,boolean) -> a
    1316:1323:void setBlocksDirty(int,int,int,int,int,int) -> a
    1326:1329:void setBlockDirty(net.minecraft.core.BlockPos,net.minecraft.world.level.block.state.BlockState,net.minecraft.world.level.block.state.BlockState) -> a
    1332:1333:void setSectionDirtyWithNeighbors(int,int,int) -> a
    1336:1343:void setSectionRangeDirty(int,int,int,int,int,int) -> b
    1346:1347:void setSectionDirty(int,int,int) -> b
    1350:1351:void setSectionDirty(int,int,int,boolean) -> a
    1354:1359:void onSectionBecomingNonEmpty(long) -> a
    1362:1382:void destroyBlockProgress(int,net.minecraft.core.BlockPos,int) -> a
    1385:1385:boolean hasRenderedAllSections() -> o
    1389:1390:void onChunkReadyToRender(net.minecraft.world.level.ChunkPos) -> a
    1393:1395:void needsUpdate() -> p
    1398:1398:int getLightColor(net.minecraft.world.level.BlockAndTintGetter,net.minecraft.core.BlockPos) -> a
    1406:1421:int getLightColor(net.minecraft.client.renderer.LevelRenderer$BrightnessGetter,net.minecraft.world.level.BlockAndTintGetter,net.minecraft.world.level.block.state.BlockState,net.minecraft.core.BlockPos) -> a
    1425:1429:boolean isSectionCompiledAndVisible(net.minecraft.core.BlockPos) -> a
    1433:1433:com.mojang.blaze3d.pipeline.RenderTarget entityOutlineTarget() -> q
    1437:1437:com.mojang.blaze3d.pipeline.RenderTarget getTranslucentTarget() -> r
    1441:1441:com.mojang.blaze3d.pipeline.RenderTarget getItemEntityTarget() -> s
    1445:1445:com.mojang.blaze3d.pipeline.RenderTarget getParticlesTarget() -> t
    1449:1449:com.mojang.blaze3d.pipeline.RenderTarget getWeatherTarget() -> u
    1453:1453:com.mojang.blaze3d.pipeline.RenderTarget getCloudsTarget() -> v
    1458:1458:it.unimi.dsi.fastutil.objects.ObjectArrayList getVisibleSections() -> w
    1463:1463:net.minecraft.client.renderer.SectionOcclusionGraph getSectionOcclusionGraph() -> x
    1467:1467:net.minecraft.client.renderer.culling.Frustum getCapturedFrustum() -> y
    1471:1471:net.minecraft.client.renderer.CloudRenderer getCloudRenderer() -> z
    1486:1486:net.minecraft.gizmos.Gizmos$TemporaryCollection collectPerFrameGizmos() -> A
    1490:1502:void finalizeGizmoCollection() -> E
    1380:1380:java.util.SortedSet lambda$destroyBlockProgress$8(long) -> b
    1200:1222:void lambda$addSkyPass$7(com.mojang.blaze3d.buffers.GpuBufferSlice,net.minecraft.client.renderer.state.SkyRenderState,net.minecraft.client.renderer.SkyRenderer) -> a
    1124:1124:void lambda$prepareChunkRenders$6(int,com.mojang.blaze3d.buffers.GpuBufferSlice[],com.mojang.blaze3d.systems.RenderPass$UniformUploader) -> a
    810:829:void lambda$addLateDebugPass$5(com.mojang.blaze3d.buffers.GpuBufferSlice,com.mojang.blaze3d.resource.ResourceHandle,net.minecraft.client.renderer.state.CameraRenderState,org.joml.Matrix4f) -> a
    792:798:void lambda$addWeatherPass$4(com.mojang.blaze3d.buffers.GpuBufferSlice,int,float) -> a
    778:778:void lambda$addCloudsPass$3(int,net.minecraft.client.CloudStatus,float,net.minecraft.world.phys.Vec3,long,float) -> a
    761:768:void lambda$addParticlesPass$2(com.mojang.blaze3d.buffers.GpuBufferSlice,com.mojang.blaze3d.resource.ResourceHandle,com.mojang.blaze3d.resource.ResourceHandle) -> a
    640:746:void lambda$addMainPass$1(com.mojang.blaze3d.buffers.GpuBufferSlice,net.minecraft.client.renderer.state.LevelRenderState,net.minecraft.util.profiling.ProfilerFiller,org.joml.Matrix4f,com.mojang.blaze3d.resource.ResourceHandle,com.mojang.blaze3d.resource.ResourceHandle,boolean,com.mojang.blaze3d.resource.ResourceHandle,com.mojang.blaze3d.resource.ResourceHandle) -> a
    562:564:void lambda$renderLevel$0(org.joml.Vector4f) -> a
    123:124:void <clinit>() -> <clinit>
```

---

## com.mojang.blaze3d.pipeline.RenderTarget

**Tier:** 1  
**Uso:** Post-process final antes do blit

**Nome obfuscado:** `fxt`

```
com.mojang.blaze3d.pipeline.RenderTarget -> fxt:
    int UNNAMED_RENDER_TARGETS -> a
    int width -> c
    int height -> d
    java.lang.String label -> e
    boolean useDepth -> f
    com.mojang.blaze3d.textures.GpuTexture colorTexture -> g
    com.mojang.blaze3d.textures.GpuTextureView colorTextureView -> h
    com.mojang.blaze3d.textures.GpuTexture depthTexture -> i
    com.mojang.blaze3d.textures.GpuTextureView depthTextureView -> j
    28:31:void <init>(java.lang.String,boolean) -> <init>
    34:38:void resize(int,int) -> a
    41:59:void destroyBuffers() -> a
    62:71:void copyDepthFrom(com.mojang.blaze3d.pipeline.RenderTarget) -> a
    74:92:void createBuffers(int,int) -> b
    95:99:void blitToScreen() -> b
    102:109:void blitAndBlendToTexture(com.mojang.blaze3d.textures.GpuTextureView) -> a
    112:112:com.mojang.blaze3d.textures.GpuTexture getColorTexture() -> c
    116:116:com.mojang.blaze3d.textures.GpuTextureView getColorTextureView() -> d
    120:120:com.mojang.blaze3d.textures.GpuTexture getDepthTexture() -> e
    124:124:com.mojang.blaze3d.textures.GpuTextureView getDepthTextureView() -> f
    103:103:java.lang.String lambda$blitAndBlendToTexture$2() -> g
    90:90:java.lang.String lambda$createBuffers$1() -> h
    86:86:java.lang.String lambda$createBuffers$0() -> i
    16:16:void <clinit>() -> <clinit>
```

---

## com.mojang.blaze3d.platform.Window

**Tier:** 1  
**Uso:** Tamanho do framebuffer (uTexel dos shaders)

**Nome obfuscado:** `fyk`

```
com.mojang.blaze3d.platform.Window -> fyk:
    org.slf4j.Logger LOGGER -> c
    int BASE_WIDTH -> a
    int BASE_HEIGHT -> b
    org.lwjgl.glfw.GLFWErrorCallback defaultErrorCallback -> d
    com.mojang.blaze3d.platform.WindowEventHandler eventHandler -> e
    com.mojang.blaze3d.platform.ScreenManager screenManager -> f
    long handle -> g
    int windowedX -> h
    int windowedY -> i
    int windowedWidth -> j
    int windowedHeight -> k
    java.util.Optional preferredFullscreenVideoMode -> l
    boolean fullscreen -> m
    boolean actuallyFullscreen -> n
    int x -> o
    int y -> p
    int width -> q
    int height -> r
    int framebufferWidth -> s
    int framebufferHeight -> t
    int guiScaledWidth -> u
    int guiScaledHeight -> v
    int guiScale -> w
    java.lang.String errorSection -> x
    boolean dirty -> y
    boolean vsync -> z
    boolean iconified -> A
    boolean minimized -> B
    boolean allowCursorChanges -> C
    com.mojang.blaze3d.platform.cursor.CursorType currentCursor -> D
    41:131:void <init>(com.mojang.blaze3d.platform.WindowEventHandler,com.mojang.blaze3d.platform.ScreenManager,com.mojang.blaze3d.platform.DisplayData,java.lang.String,java.lang.String) -> <init>
    134:142:java.lang.String getPlatform() -> a
    147:148:int getRefreshRate() -> b
    153:153:boolean shouldClose() -> c
    157:166:void checkGlfwError(java.util.function.BiConsumer) -> a
    170:201:void setIcon(net.minecraft.server.packs.PackResources,com.mojang.blaze3d.platform.IconSet) -> a
    204:205:void setErrorSection(java.lang.String) -> a
    210:211:void setBootErrorCallback() -> w
    214:216:void bootCrash(int,long) -> b
    220:225:void defaultErrorCallback(int,long) -> a
    228:232:void setDefaultErrorCallback() -> d
    235:238:void updateVsync(boolean) -> a
    242:247:void close() -> close
    250:252:void onMove(long,int,int) -> a
    255:280:void onFramebufferResize(long,int,int) -> b
    283:289:void refreshFramebufferSize() -> x
    292:294:void onResize(long,int,int) -> c
    297:300:void onFocus(long,boolean) -> a
    303:306:void onEnter(long,boolean) -> b
    309:310:void onIconify(long,boolean) -> c
    313:318:void updateDisplay(com.mojang.blaze3d.TracyFrameCapture) -> a
    321:321:java.util.Optional getPreferredFullscreenVideoMode() -> e
    325:330:void setPreferredFullscreenVideoMode(java.util.Optional) -> a
    333:338:void changeFullscreenVideoMode() -> f
    343:378:void setMode() -> y
    381:382:void toggleFullScreen() -> g
    385:389:void setWindowed(int,int) -> a
    392:401:void updateFullscreen(boolean,com.mojang.blaze3d.TracyFrameCapture) -> a
    404:411:int calculateScale(int,boolean) -> a
    415:421:void setGuiScale(int) -> a
    424:425:void setTitle(java.lang.String) -> b
    428:428:long handle() -> h
    432:432:boolean isFullscreen() -> i
    436:436:boolean isIconified() -> j
    440:440:int getWidth() -> k
    444:444:int getHeight() -> l
    448:449:void setWidth(int) -> b
    452:453:void setHeight(int) -> c
    456:456:int getScreenWidth() -> m
    460:460:int getScreenHeight() -> n
    464:464:int getGuiScaledWidth() -> o
    468:468:int getGuiScaledHeight() -> p
    472:472:int getX() -> q
    476:476:int getY() -> r
    480:480:int getGuiScale() -> s
    484:484:com.mojang.blaze3d.platform.Monitor findBestMonitor() -> t
    488:489:void updateRawMouseInput(boolean) -> b
    492:496:void setWindowCloseCallback(java.lang.Runnable) -> a
    505:505:boolean isMinimized() -> u
    509:510:void setAllowCursorChanges(boolean) -> c
    513:519:void selectCursor(com.mojang.blaze3d.platform.cursor.CursorType) -> a
    522:522:float getAppropriateLineWidth() -> v
    492:492:void lambda$setWindowCloseCallback$0(java.lang.Runnable,long) -> a
    36:36:void <clinit>() -> <clinit>
```

---

# TIER 2 — Rendering (efeitos visuais)

## net.minecraft.client.renderer.SkyRenderer

**Tier:** 2  
**Uso:** Atmosfera do céu (cor por hora do dia)

**Nome obfuscado:** `hpk`

```
net.minecraft.client.renderer.SkyRenderer -> hpk:
    net.minecraft.resources.Identifier SUN_SPRITE -> a
    net.minecraft.resources.Identifier END_FLASH_SPRITE -> b
    net.minecraft.resources.Identifier END_SKY_LOCATION -> c
    float SKY_DISC_RADIUS -> d
    int SKY_VERTICES -> e
    int STAR_COUNT -> f
    float SUN_SIZE -> g
    float SUN_HEIGHT -> h
    float MOON_SIZE -> i
    float MOON_HEIGHT -> j
    int SUNRISE_STEPS -> k
    int END_SKY_QUAD_COUNT -> l
    float END_FLASH_HEIGHT -> m
    float END_FLASH_SCALE -> n
    net.minecraft.client.renderer.texture.TextureAtlas celestialsAtlas -> o
    com.mojang.blaze3d.buffers.GpuBuffer starBuffer -> p
    com.mojang.blaze3d.buffers.GpuBuffer topSkyBuffer -> q
    com.mojang.blaze3d.buffers.GpuBuffer bottomSkyBuffer -> r
    com.mojang.blaze3d.buffers.GpuBuffer endSkyBuffer -> s
    com.mojang.blaze3d.buffers.GpuBuffer sunBuffer -> t
    com.mojang.blaze3d.buffers.GpuBuffer moonBuffer -> u
    com.mojang.blaze3d.buffers.GpuBuffer sunriseBuffer -> v
    com.mojang.blaze3d.buffers.GpuBuffer endFlashBuffer -> w
    com.mojang.blaze3d.systems.RenderSystem$AutoStorageIndexBuffer quadIndices -> x
    net.minecraft.client.renderer.texture.AbstractTexture endSkyTexture -> y
    int starIndexCount -> z
    71:100:void <init>(net.minecraft.client.renderer.texture.TextureManager,net.minecraft.client.resources.model.AtlasManager) -> <init>
    103:103:net.minecraft.client.renderer.texture.AbstractTexture getTexture(net.minecraft.client.renderer.texture.TextureManager,net.minecraft.resources.Identifier) -> a
    107:123:com.mojang.blaze3d.buffers.GpuBuffer buildSunriseFan() -> c
    127:127:com.mojang.blaze3d.buffers.GpuBuffer buildSunQuad(net.minecraft.client.renderer.texture.TextureAtlas) -> a
    131:131:com.mojang.blaze3d.buffers.GpuBuffer buildEndFlashQuad(net.minecraft.client.renderer.texture.TextureAtlas) -> b
    135:145:com.mojang.blaze3d.buffers.GpuBuffer buildCelestialQuad(java.lang.String,net.minecraft.client.renderer.texture.TextureAtlasSprite) -> a
    149:163:com.mojang.blaze3d.buffers.GpuBuffer buildMoonPhases(net.minecraft.client.renderer.texture.TextureAtlas) -> c
    167:202:com.mojang.blaze3d.buffers.GpuBuffer buildStars() -> d
    206:212:void buildSkyDisc(com.mojang.blaze3d.vertex.VertexConsumer,float) -> a
    215:236:com.mojang.blaze3d.buffers.GpuBuffer buildEndSky() -> e
    240:250:void renderSkyDisc(int) -> a
    253:278:void extractRenderState(net.minecraft.client.multiplayer.ClientLevel,float,net.minecraft.client.Camera,net.minecraft.client.renderer.state.SkyRenderState) -> a
    281:281:boolean shouldRenderDarkDisc(float,net.minecraft.client.multiplayer.ClientLevel) -> a
    285:301:void renderDarkDisc() -> a
    304:325:void renderSunMoonAndStars(com.mojang.blaze3d.vertex.PoseStack,float,float,float,net.minecraft.world.level.MoonPhase,float,float) -> a
    328:357:void renderSun(float,com.mojang.blaze3d.vertex.PoseStack) -> a
    360:391:void renderMoon(net.minecraft.world.level.MoonPhase,float,com.mojang.blaze3d.vertex.PoseStack) -> a
    394:413:void renderStars(float,com.mojang.blaze3d.vertex.PoseStack) -> b
    416:452:void renderSunriseAndSunset(com.mojang.blaze3d.vertex.PoseStack,float,int) -> a
    455:471:void renderEndSky() -> b
    474:506:void renderEndFlash(com.mojang.blaze3d.vertex.PoseStack,float,float,float) -> a
    510:518:void close() -> close
    495:495:java.lang.String lambda$renderEndFlash$14() -> f
    462:462:java.lang.String lambda$renderEndSky$13() -> g
    442:442:java.lang.String lambda$renderSunriseAndSunset$12() -> h
    404:404:java.lang.String lambda$renderStars$11() -> i
    380:380:java.lang.String lambda$renderMoon$10() -> j
    346:346:java.lang.String lambda$renderSun$9() -> k
    292:292:java.lang.String lambda$renderDarkDisc$8() -> l
    243:243:java.lang.String lambda$renderSkyDisc$7() -> m
    234:234:java.lang.String lambda$buildEndSky$6() -> n
    200:200:java.lang.String lambda$buildStars$5() -> o
    161:161:java.lang.String lambda$buildMoonPhases$4() -> p
    143:143:java.lang.String lambda$buildCelestialQuad$3(java.lang.String) -> a
    121:121:java.lang.String lambda$buildSunriseFan$2() -> q
    97:97:java.lang.String lambda$new$1() -> r
    92:92:java.lang.String lambda$new$0() -> s
    45:47:void <clinit>() -> <clinit>
```

---

## net.minecraft.client.renderer.CloudRenderer

**Tier:** 2  
**Uso:** Iluminação de nuvens

**Nome obfuscado:** `hnv`

```
net.minecraft.client.renderer.CloudRenderer -> hnv:
    int FLAG_INSIDE_FACE -> a
    int FLAG_USE_TOP_COLOR -> b
    float CELL_SIZE_IN_BLOCKS -> c
    int TICKS_PER_CELL -> d
    float BLOCKS_PER_SECOND -> e
    int UBO_SIZE -> f
    org.slf4j.Logger LOGGER -> g
    net.minecraft.resources.Identifier TEXTURE_LOCATION -> h
    long EMPTY_CELL -> i
    int COLOR_OFFSET -> j
    int NORTH_OFFSET -> k
    int EAST_OFFSET -> l
    int SOUTH_OFFSET -> m
    int WEST_OFFSET -> n
    boolean needsRebuild -> o
    int prevCellX -> p
    int prevCellZ -> q
    net.minecraft.client.renderer.CloudRenderer$RelativeCameraPos prevRelativeCameraPos -> r
    net.minecraft.client.CloudStatus prevType -> s
    net.minecraft.client.renderer.CloudRenderer$TextureData texture -> t
    int quadCount -> u
    net.minecraft.client.renderer.MappableRingBuffer ubo -> v
    net.minecraft.client.renderer.MappableRingBuffer utb -> w
    41:73:void <init>() -> <init>
    78:102:java.util.Optional prepare(net.minecraft.server.packs.resources.ResourceManager,net.minecraft.util.profiling.ProfilerFiller) -> a
    111:114:int getSizeForCloudDistance(int) -> a
    119:121:void apply(java.util.Optional,net.minecraft.server.packs.resources.ResourceManager,net.minecraft.util.profiling.ProfilerFiller) -> a
    124:124:boolean isCellEmpty(int) -> b
    128:128:long packCellData(int,boolean,boolean,boolean,boolean) -> a
    132:132:boolean isNorthEmpty(long) -> a
    136:136:boolean isEastEmpty(long) -> b
    140:140:boolean isSouthEmpty(long) -> c
    144:144:boolean isWestEmpty(long) -> d
    148:245:void render(int,net.minecraft.client.CloudStatus,float,net.minecraft.world.phys.Vec3,long,float) -> a
    248:273:void buildMesh(net.minecraft.client.renderer.CloudRenderer$RelativeCameraPos,java.nio.ByteBuffer,int,int,boolean,int) -> a
    276:287:void tryBuildCell(net.minecraft.client.renderer.CloudRenderer$RelativeCameraPos,java.nio.ByteBuffer,int,int,boolean,int,int,int,int,long[]) -> a
    290:291:void buildFlatCell(java.nio.ByteBuffer,int,int) -> a
    294:298:void encodeFace(java.nio.ByteBuffer,int,int,net.minecraft.core.Direction,int) -> a
    302:334:void buildExtrudedCell(net.minecraft.client.renderer.CloudRenderer$RelativeCameraPos,java.nio.ByteBuffer,int,int,long) -> a
    337:338:void markForRebuild() -> a
    341:342:void endFrame() -> b
    346:350:void close() -> close
    41:41:void apply(java.lang.Object,net.minecraft.server.packs.resources.ResourceManager,net.minecraft.util.profiling.ProfilerFiller) -> a
    41:41:java.lang.Object prepare(net.minecraft.server.packs.resources.ResourceManager,net.minecraft.util.profiling.ProfilerFiller) -> b
    236:236:java.lang.String lambda$render$2() -> c
    158:158:java.lang.String lambda$render$1() -> d
    73:73:java.lang.String lambda$new$0() -> e
    48:55:void <clinit>() -> <clinit>
```

---

## net.minecraft.client.renderer.WeatherEffectRenderer

**Tier:** 2  
**Uso:** Chuva/neve com iluminação correta

**Nome obfuscado:** `hpt`

```
net.minecraft.client.renderer.WeatherEffectRenderer -> hpt:
    float RAIN_PARTICLES_PER_BLOCK -> a
    int RAIN_RADIUS -> b
    net.minecraft.resources.Identifier RAIN_LOCATION -> c
    net.minecraft.resources.Identifier SNOW_LOCATION -> d
    int RAIN_TABLE_SIZE -> e
    int HALF_RAIN_TABLE_SIZE -> f
    int rainSoundTime -> g
    float[] columnSizeX -> h
    float[] columnSizeZ -> i
    44:57:void <init>() -> <init>
    60:101:void extractRenderState(net.minecraft.world.level.Level,int,float,net.minecraft.world.phys.Vec3,net.minecraft.client.renderer.state.WeatherRenderState) -> a
    104:112:void render(net.minecraft.client.renderer.MultiBufferSource,net.minecraft.world.phys.Vec3,net.minecraft.client.renderer.state.WeatherRenderState) -> a
    116:122:net.minecraft.client.renderer.WeatherEffectRenderer$ColumnInstance createRainColumnInstance(net.minecraft.util.RandomSource,int,int,int,int,int,int,float) -> a
    126:137:net.minecraft.client.renderer.WeatherEffectRenderer$ColumnInstance createSnowColumnInstance(net.minecraft.util.RandomSource,int,int,int,int,int,int,float) -> b
    144:174:void renderInstances(com.mojang.blaze3d.vertex.VertexConsumer,java.util.List,net.minecraft.world.phys.Vec3,float,int,float) -> a
    177:233:void tickRainParticles(net.minecraft.client.multiplayer.ClientLevel,net.minecraft.client.Camera,int,net.minecraft.server.level.ParticleStatus,int) -> a
    236:240:net.minecraft.world.level.biome.Biome$Precipitation getPrecipitationAt(net.minecraft.world.level.Level,net.minecraft.core.BlockPos) -> a
    38:39:void <clinit>() -> <clinit>
```

---

## net.minecraft.client.renderer.fog.FogRenderer

**Tier:** 2  
**Uso:** Cor de fog que acompanha a atmosfera

**Nome obfuscado:** `igq`

```
net.minecraft.client.renderer.fog.FogRenderer -> igq:
    int FOG_UBO_SIZE -> a
    java.util.List FOG_ENVIRONMENTS -> b
    boolean fogEnabled -> c
    com.mojang.blaze3d.buffers.GpuBuffer emptyBuffer -> d
    net.minecraft.client.renderer.MappableRingBuffer regularBuffer -> e
    65:77:void <init>() -> <init>
    81:83:void close() -> close
    86:87:void endFrame() -> a
    90:95:com.mojang.blaze3d.buffers.GpuBufferSlice getBuffer(net.minecraft.client.renderer.fog.FogRenderer$FogMode) -> a
    100:167:org.joml.Vector4f computeFogColor(net.minecraft.client.Camera,float,net.minecraft.client.multiplayer.ClientLevel,int,float) -> a
    171:171:boolean toggleFog() -> b
    175:197:org.joml.Vector4f setupFog(net.minecraft.client.Camera,int,net.minecraft.client.DeltaTracker,float,net.minecraft.client.multiplayer.ClientLevel) -> a
    201:205:net.minecraft.world.level.material.FogType getFogType(net.minecraft.client.Camera) -> a
    209:218:void updateBuffer(java.nio.ByteBuffer,int,org.joml.Vector4f,float,float,float,float,float,float) -> a
    72:72:java.lang.String lambda$new$1() -> c
    67:67:java.lang.String lambda$new$0() -> d
    37:61:void <clinit>() -> <clinit>
```

---

## net.minecraft.client.renderer.fog.FogData

**Tier:** 2  
**Uso:** Dados de fog passados ao shader

**Nome obfuscado:** `igp`

```
net.minecraft.client.renderer.fog.FogData -> igp:
    float environmentalStart -> a
    float renderDistanceStart -> b
    float environmentalEnd -> c
    float renderDistanceEnd -> d
    float skyEnd -> e
    float cloudEnd -> f
    3:3:void <init>() -> <init>
```

---

## net.minecraft.client.renderer.LightTexture

**Tier:** 2  
**Uso:** LUT de luz — candidato a luzes RGB (via SSBO)

**Nome obfuscado:** `hoj`

```
net.minecraft.client.renderer.LightTexture -> hoj:
    int FULL_BRIGHT -> a
    int FULL_SKY -> b
    int FULL_BLOCK -> c
    int TEXTURE_SIZE -> d
    int LIGHTMAP_UBO_SIZE -> e
    com.mojang.blaze3d.textures.GpuTexture texture -> f
    com.mojang.blaze3d.textures.GpuTextureView textureView -> g
    boolean updateLightTexture -> h
    float blockLightRedFlicker -> i
    net.minecraft.client.renderer.GameRenderer renderer -> j
    net.minecraft.client.Minecraft minecraft -> k
    net.minecraft.client.renderer.MappableRingBuffer ubo -> l
    net.minecraft.util.RandomSource randomSource -> m
    60:71:void <init>(net.minecraft.client.renderer.GameRenderer,net.minecraft.client.Minecraft) -> <init>
    74:74:com.mojang.blaze3d.textures.GpuTextureView getTextureView() -> a
    79:82:void close() -> close
    85:88:void tick() -> b
    91:92:float calculateDarknessScale(net.minecraft.world.entity.LivingEntity,float,float) -> a
    96:169:void updateLightTexture(float) -> a
    172:172:float getBrightness(net.minecraft.world.level.dimension.DimensionType,int) -> a
    179:182:float getBrightness(float,int) -> a
    189:189:int pack(int,int) -> a
    196:196:int block(int) -> a
    203:203:int sky(int) -> b
    207:212:int lightCoordsWithEmission(int,int) -> b
    161:161:java.lang.String lambda$updateLightTexture$1() -> c
    70:70:java.lang.String lambda$new$0() -> d
    37:47:void <clinit>() -> <clinit>
```

---

## net.minecraft.client.renderer.entity.EntityRenderDispatcher

**Tier:** 2  
**Uso:** Sombras dinâmicas de entidades

**Nome obfuscado:** `hwo`

```
net.minecraft.client.renderer.entity.EntityRenderDispatcher -> hwo:
    java.util.Map renderers -> e
    java.util.Map playerRenderers -> f
    java.util.Map mannequinRenderers -> g
    net.minecraft.client.renderer.texture.TextureManager textureManager -> a
    net.minecraft.client.Camera camera -> b
    net.minecraft.world.entity.Entity crosshairPickEntity -> c
    net.minecraft.client.renderer.item.ItemModelResolver itemModelResolver -> h
    net.minecraft.client.renderer.MapRenderer mapRenderer -> i
    net.minecraft.client.renderer.block.BlockRenderDispatcher blockRenderDispatcher -> j
    net.minecraft.client.renderer.ItemInHandRenderer itemInHandRenderer -> k
    net.minecraft.client.resources.model.AtlasManager atlasManager -> l
    net.minecraft.client.gui.Font font -> m
    net.minecraft.client.Options options -> d
    java.util.function.Supplier entityModels -> n
    net.minecraft.client.resources.model.EquipmentAssetManager equipmentAssets -> o
    net.minecraft.client.renderer.PlayerSkinRenderCache playerSkinRenderCache -> p
    64:64:int getPackedLightCoords(net.minecraft.world.entity.Entity,float) -> a
    45:79:void <init>(net.minecraft.client.Minecraft,net.minecraft.client.renderer.texture.TextureManager,net.minecraft.client.renderer.item.ItemModelResolver,net.minecraft.client.renderer.MapRenderer,net.minecraft.client.renderer.block.BlockRenderDispatcher,net.minecraft.client.resources.model.AtlasManager,net.minecraft.client.gui.Font,net.minecraft.client.Options,java.util.function.Supplier,net.minecraft.client.resources.model.EquipmentAssetManager,net.minecraft.client.renderer.PlayerSkinRenderCache) -> <init>
    83:86:net.minecraft.client.renderer.entity.EntityRenderer getRenderer(net.minecraft.world.entity.Entity) -> a
    91:91:net.minecraft.client.renderer.entity.player.AvatarRenderer getPlayerRenderer(net.minecraft.client.player.AbstractClientPlayer) -> a
    95:100:net.minecraft.client.renderer.entity.player.AvatarRenderer getAvatarRenderer(java.util.Map,net.minecraft.world.entity.Avatar) -> a
    105:113:net.minecraft.client.renderer.entity.EntityRenderer getRenderer(net.minecraft.client.renderer.entity.state.EntityRenderState) -> a
    117:119:void prepare(net.minecraft.client.Camera,net.minecraft.world.entity.Entity) -> a
    122:123:boolean shouldRender(net.minecraft.world.entity.Entity,net.minecraft.client.renderer.culling.Frustum,double,double,double) -> a
    127:139:net.minecraft.client.renderer.entity.state.EntityRenderState extractEntity(net.minecraft.world.entity.Entity,float) -> b
    144:182:void submit(net.minecraft.client.renderer.entity.state.EntityRenderState,net.minecraft.client.renderer.state.CameraRenderState,double,double,double,com.mojang.blaze3d.vertex.PoseStack,net.minecraft.client.renderer.SubmitNodeCollector) -> a
    185:187:net.minecraft.CrashReportCategory fillRendererDetails(net.minecraft.client.renderer.entity.EntityRenderer,net.minecraft.CrashReport) -> a
    191:192:void resetCamera() -> a
    195:195:double distanceToSqr(net.minecraft.world.entity.Entity) -> b
    199:199:net.minecraft.client.renderer.ItemInHandRenderer getItemInHandRenderer() -> b
    204:208:void onResourceManagerReload(net.minecraft.server.packs.resources.ResourceManager) -> a
```

---

## net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher

**Tier:** 2  
**Uso:** Sombras de block entities (baús, placas)

**Nome obfuscado:** `hrh`

```
net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher -> hrh:
    java.util.Map renderers -> a
    net.minecraft.client.gui.Font font -> b
    java.util.function.Supplier entityModelSet -> c
    net.minecraft.world.phys.Vec3 cameraPos -> d
    net.minecraft.client.renderer.block.BlockRenderDispatcher blockRenderDispatcher -> e
    net.minecraft.client.renderer.item.ItemModelResolver itemModelResolver -> f
    net.minecraft.client.renderer.entity.ItemRenderer itemRenderer -> g
    net.minecraft.client.renderer.entity.EntityRenderDispatcher entityRenderer -> h
    net.minecraft.client.resources.model.MaterialSet materials -> i
    net.minecraft.client.renderer.PlayerSkinRenderCache playerSkinRenderCache -> j
    32:54:void <init>(net.minecraft.client.gui.Font,java.util.function.Supplier,net.minecraft.client.renderer.block.BlockRenderDispatcher,net.minecraft.client.renderer.item.ItemModelResolver,net.minecraft.client.renderer.entity.ItemRenderer,net.minecraft.client.renderer.entity.EntityRenderDispatcher,net.minecraft.client.resources.model.MaterialSet,net.minecraft.client.renderer.PlayerSkinRenderCache) -> <init>
    58:58:net.minecraft.client.renderer.blockentity.BlockEntityRenderer getRenderer(net.minecraft.world.level.block.entity.BlockEntity) -> a
    63:63:net.minecraft.client.renderer.blockentity.BlockEntityRenderer getRenderer(net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState) -> a
    67:68:void prepare(net.minecraft.client.Camera) -> a
    71:87:net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState tryExtractRenderState(net.minecraft.world.level.block.entity.BlockEntity,float,net.minecraft.client.renderer.feature.ModelFeatureRenderer$CrumblingOverlay) -> a
    91:106:void submit(net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState,com.mojang.blaze3d.vertex.PoseStack,net.minecraft.client.renderer.SubmitNodeCollector,net.minecraft.client.renderer.state.CameraRenderState) -> a
    110:112:void onResourceManagerReload(net.minecraft.server.packs.resources.ResourceManager) -> a
```

---

# TIER 3 — Advanced (framegraph/pipeline interno)

## net.minecraft.client.renderer.GameRenderer

**Tier:** 3  
**Uso:** Orquestrador de render — candidato para injetar shadow pass

**Nome obfuscado:** `hob`

```
net.minecraft.client.renderer.GameRenderer -> hob:
    net.minecraft.resources.Identifier BLUR_POST_CHAIN_ID -> g
    int MAX_BLUR_RADIUS -> a
    org.slf4j.Logger LOGGER -> h
    float PROJECTION_Z_NEAR -> b
    float PROJECTION_3D_HUD_Z_FAR -> c
    float PORTAL_SPINNING_SPEED -> i
    float NAUSEA_SPINNING_SPEED -> j
    net.minecraft.client.Minecraft minecraft -> k
    net.minecraft.util.RandomSource random -> l
    float renderDistance -> m
    net.minecraft.client.renderer.ItemInHandRenderer itemInHandRenderer -> d
    net.minecraft.client.renderer.ScreenEffectRenderer screenEffectRenderer -> n
    net.minecraft.client.renderer.RenderBuffers renderBuffers -> o
    float spinningEffectTime -> p
    float spinningEffectSpeed -> q
    float fovModifier -> r
    float oldFovModifier -> s
    float darkenWorldAmount -> t
    float darkenWorldAmountO -> u
    boolean renderBlockOutline -> v
    long lastScreenshotAttempt -> w
    boolean hasWorldScreenshot -> x
    long lastActiveTime -> y
    net.minecraft.client.renderer.LightTexture lightTexture -> z
    net.minecraft.client.renderer.texture.OverlayTexture overlayTexture -> A
    net.minecraft.client.renderer.PanoramicScreenshotParameters panoramicScreenshotParameters -> B
    net.minecraft.client.renderer.CubeMap cubeMap -> e
    net.minecraft.client.renderer.PanoramaRenderer panorama -> f
    com.mojang.blaze3d.resource.CrossFrameResourcePool resourcePool -> C
    net.minecraft.client.renderer.fog.FogRenderer fogRenderer -> D
    net.minecraft.client.gui.render.GuiRenderer guiRenderer -> E
    net.minecraft.client.gui.render.state.GuiRenderState guiRenderState -> F
    net.minecraft.client.renderer.state.LevelRenderState levelRenderState -> G
    net.minecraft.client.renderer.SubmitNodeStorage submitNodeStorage -> H
    net.minecraft.client.renderer.feature.FeatureRenderDispatcher featureRenderDispatcher -> I
    net.minecraft.resources.Identifier postEffectId -> J
    boolean effectActive -> K
    net.minecraft.client.Camera mainCamera -> L
    com.mojang.blaze3d.platform.Lighting lighting -> M
    net.minecraft.client.renderer.GlobalSettingsUniform globalSettingsUniform -> N
    net.minecraft.client.renderer.PerspectiveProjectionMatrixBuffer levelProjectionMatrixBuffer -> O
    net.minecraft.client.renderer.CachedPerspectiveProjectionMatrixBuffer hud3dProjectionMatrixBuffer -> P
    118:185:void <init>(net.minecraft.client.Minecraft,net.minecraft.client.renderer.ItemInHandRenderer,net.minecraft.client.renderer.RenderBuffers,net.minecraft.client.renderer.block.BlockRenderDispatcher) -> <init>
    189:200:void close() -> close
    203:203:net.minecraft.client.renderer.SubmitNodeStorage getSubmitNodeStorage() -> b
    207:207:net.minecraft.client.renderer.feature.FeatureRenderDispatcher getFeatureRenderDispatcher() -> c
    211:211:net.minecraft.client.renderer.state.LevelRenderState getLevelRenderState() -> d
    215:216:void setRenderBlockOutline(boolean) -> a
    219:220:void setPanoramicScreenshotParameters(net.minecraft.client.renderer.PanoramicScreenshotParameters) -> a
    223:223:net.minecraft.client.renderer.PanoramicScreenshotParameters getPanoramicScreenshotParameters() -> e
    227:227:boolean isPanoramicMode() -> f
    231:233:void clearPostEffect() -> g
    236:237:void togglePostEffect() -> h
    240:246:void checkEntityPostEffect(net.minecraft.world.entity.Entity) -> a
    249:251:void setPostEffect(net.minecraft.resources.Identifier) -> a
    254:258:void processBlurEffect() -> i
    261:276:void preloadUiShader(net.minecraft.server.packs.resources.ResourceProvider) -> a
    279:318:void tick() -> j
    321:321:net.minecraft.resources.Identifier currentPostEffect() -> k
    325:327:void resize(int,int) -> a
    330:342:void pick(float) -> a
    346:358:void tickFov() -> v
    361:382:float getFov(net.minecraft.client.Camera,float,boolean) -> a
    386:409:void bobHurt(com.mojang.blaze3d.vertex.PoseStack,float) -> a
    412:422:void bobView(com.mojang.blaze3d.vertex.PoseStack,float) -> b
    425:449:void renderItemInHand(float,boolean,org.joml.Matrix4f) -> a
    452:453:org.joml.Matrix4f getProjectionMatrix(float) -> b
    457:457:float getDepthFar() -> l
    461:465:float getNightVisionScale(net.minecraft.world.entity.LivingEntity,float) -> a
    470:594:void render(net.minecraft.client.DeltaTracker,boolean) -> a
    597:636:void renderActiveTextDebug() -> w
    639:662:void tryTakeScreenshotIfNeeded() -> x
    665:689:void takeAutoScreenshot(java.nio.file.Path) -> a
    692:714:boolean shouldRenderBlockOutline() -> y
    718:730:void updateCamera(net.minecraft.client.DeltaTracker) -> a
    733:805:void renderLevel(net.minecraft.client.DeltaTracker) -> b
    808:814:void extractCamera(float) -> d
    818:819:org.joml.Matrix4f getProjectionMatrixForCulling(float) -> e
    823:827:void resetData() -> m
    830:831:void displayItemActivation(net.minecraft.world.item.ItemStack) -> a
    834:834:net.minecraft.client.Minecraft getMinecraft() -> n
    838:838:float getDarkenWorldAmount(float) -> c
    842:842:float getRenderDistance() -> o
    846:846:net.minecraft.client.Camera getMainCamera() -> p
    850:850:net.minecraft.client.renderer.LightTexture lightTexture() -> q
    854:854:net.minecraft.client.renderer.texture.OverlayTexture overlayTexture() -> r
    859:870:net.minecraft.world.phys.Vec3 projectPointToScreen(net.minecraft.world.phys.Vec3) -> a
    875:882:double projectHorizonToScreen() -> a
    887:887:net.minecraft.client.renderer.GlobalSettingsUniform getGlobalSettingsUniform() -> s
    891:891:com.mojang.blaze3d.platform.Lighting getLighting() -> t
    895:898:void setLevel(net.minecraft.client.multiplayer.ClientLevel) -> a
    901:901:net.minecraft.client.renderer.PanoramaRenderer getPanorama() -> u
    666:666:void lambda$takeAutoScreenshot$7(java.nio.file.Path,com.mojang.blaze3d.platform.NativeImage) -> a
    667:687:void lambda$takeAutoScreenshot$6(com.mojang.blaze3d.platform.NativeImage,java.nio.file.Path) -> a
    655:661:void lambda$tryTakeScreenshotIfNeeded$5(java.nio.file.Path) -> b
    599:635:void lambda$renderActiveTextDebug$4(net.minecraft.client.gui.render.state.GuiTextRenderState) -> a
    559:559:java.lang.String lambda$render$3() -> z
    542:542:java.lang.String lambda$render$2() -> A
    531:531:java.lang.String lambda$render$1() -> B
    263:268:java.lang.String lambda$preloadUiShader$0(net.minecraft.server.packs.resources.ResourceProvider,net.minecraft.resources.Identifier,com.mojang.blaze3d.shaders.ShaderType) -> a
    104:108:void <clinit>() -> <clinit>
```

---

## net.minecraft.client.renderer.PostChain

**Tier:** 3  
**Uso:** Post-processing pipeline do Minecraft

**Nome obfuscado:** `hov`

```
net.minecraft.client.renderer.PostChain -> hov:
    net.minecraft.resources.Identifier MAIN_TARGET_ID -> a
    java.util.List passes -> b
    java.util.Map internalTargets -> c
    java.util.Set externalTargets -> d
    java.util.Map persistentTargets -> e
    net.minecraft.client.renderer.CachedOrthoProjectionMatrixBuffer projectionMatrixBuffer -> f
    32:40:void <init>(java.util.List,java.util.Map,java.util.Set,net.minecraft.client.renderer.CachedOrthoProjectionMatrixBuffer) -> <init>
    43:60:net.minecraft.client.renderer.PostChain load(net.minecraft.client.renderer.PostChainConfig,net.minecraft.client.renderer.texture.TextureManager,java.util.Set,net.minecraft.resources.Identifier,net.minecraft.client.renderer.CachedOrthoProjectionMatrixBuffer) -> a
    64:95:net.minecraft.client.renderer.PostPass createPass(net.minecraft.client.renderer.texture.TextureManager,net.minecraft.client.renderer.PostChainConfig$Pass,net.minecraft.resources.Identifier) -> a
    99:130:void addToFrame(com.mojang.blaze3d.framegraph.FrameGraphBuilder,int,int,net.minecraft.client.renderer.PostChain$TargetBundle) -> a
    135:139:void process(com.mojang.blaze3d.pipeline.RenderTarget,com.mojang.blaze3d.resource.GraphicsResourceAllocator) -> a
    142:151:com.mojang.blaze3d.pipeline.RenderTarget getOrCreatePersistentTarget(net.minecraft.resources.Identifier,com.mojang.blaze3d.resource.RenderTargetDescriptor) -> a
    156:161:void close() -> close
    86:86:java.lang.String lambda$createPass$1(java.lang.String) -> a
    46:46:boolean lambda$load$0(net.minecraft.client.renderer.PostChainConfig,net.minecraft.resources.Identifier) -> a
    27:27:void <clinit>() -> <clinit>
```

---

## net.minecraft.client.renderer.chunk.SectionRenderDispatcher

**Tier:** 3  
**Uso:** Compilação de chunks (VBO) — hook para bufferUpload

**Nome obfuscado:** `hts`

```
net.minecraft.client.renderer.chunk.SectionRenderDispatcher -> hts:
    net.minecraft.client.renderer.chunk.CompileTaskDynamicQueue compileQueue -> a
    java.util.Queue toUpload -> b
    java.util.concurrent.Executor mainThreadUploadExecutor -> c
    java.util.Queue toClose -> d
    net.minecraft.client.renderer.SectionBufferBuilderPack fixedBuffers -> e
    net.minecraft.client.renderer.SectionBufferBuilderPool bufferPool -> f
    boolean closed -> g
    net.minecraft.util.thread.ConsecutiveExecutor consecutiveExecutor -> h
    net.minecraft.TracingExecutor executor -> i
    net.minecraft.client.multiplayer.ClientLevel level -> j
    net.minecraft.client.renderer.LevelRenderer renderer -> k
    net.minecraft.world.phys.Vec3 cameraPosition -> l
    net.minecraft.client.renderer.chunk.SectionCompiler sectionCompiler -> m
    42:73:void <init>(net.minecraft.client.multiplayer.ClientLevel,net.minecraft.client.renderer.LevelRenderer,net.minecraft.TracingExecutor,net.minecraft.client.renderer.RenderBuffers,net.minecraft.client.renderer.block.BlockRenderDispatcher,net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher) -> <init>
    76:77:void setLevel(net.minecraft.client.multiplayer.ClientLevel) -> a
    81:110:void runTask() -> i
    113:114:void setCameraPosition(net.minecraft.world.phys.Vec3) -> a
    118:125:void uploadAllPendingUploads() -> a
    128:129:void rebuildSectionSync(net.minecraft.client.renderer.chunk.SectionRenderDispatcher$RenderSection,net.minecraft.client.renderer.chunk.RenderRegionCache) -> a
    132:142:void schedule(net.minecraft.client.renderer.chunk.SectionRenderDispatcher$RenderSection$CompileTask) -> a
    145:146:void clearCompileQueue() -> b
    150:150:boolean isQueueEmpty() -> c
    154:158:void dispose() -> d
    162:162:java.lang.String getStats() -> e
    167:167:int getCompileQueueSize() -> f
    172:172:int getToUpload() -> g
    177:177:int getFreeBufferCount() -> h
    136:141:void lambda$schedule$4(net.minecraft.client.renderer.chunk.SectionRenderDispatcher$RenderSection$CompileTask) -> b
    95:109:void lambda$runTask$3(net.minecraft.client.renderer.chunk.SectionRenderDispatcher$RenderSection$CompileTask,net.minecraft.client.renderer.SectionBufferBuilderPack,net.minecraft.client.renderer.chunk.SectionRenderDispatcher$SectionTaskResult,java.lang.Throwable) -> a
    101:108:void lambda$runTask$2(net.minecraft.client.renderer.chunk.SectionRenderDispatcher$SectionTaskResult,net.minecraft.client.renderer.SectionBufferBuilderPack) -> a
    93:93:java.util.concurrent.CompletionStage lambda$runTask$1(java.util.concurrent.CompletableFuture) -> a
    92:92:java.util.concurrent.CompletableFuture lambda$runTask$0(net.minecraft.client.renderer.chunk.SectionRenderDispatcher$RenderSection$CompileTask,net.minecraft.client.renderer.SectionBufferBuilderPack) -> a
```

---

## net.minecraft.client.renderer.chunk.SectionCompiler

**Tier:** 3  
**Uso:** Compilador de seção — hook para geometria custom

**Nome obfuscado:** `htp`

```
net.minecraft.client.renderer.chunk.SectionCompiler -> htp:
    net.minecraft.client.renderer.block.BlockRenderDispatcher blockRenderer -> a
    net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher blockEntityRenderer -> b
    36:39:void <init>(net.minecraft.client.renderer.block.BlockRenderDispatcher,net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher) -> <init>
    42:108:net.minecraft.client.renderer.chunk.SectionCompiler$Results compile(net.minecraft.core.SectionPos,net.minecraft.client.renderer.chunk.RenderSectionRegion,com.mojang.blaze3d.vertex.VertexSorting,net.minecraft.client.renderer.SectionBufferBuilderPack) -> a
    112:118:com.mojang.blaze3d.vertex.BufferBuilder getOrBeginLayer(java.util.Map,net.minecraft.client.renderer.SectionBufferBuilderPack,net.minecraft.client.renderer.chunk.ChunkSectionLayer) -> a
    122:126:void handleBlockEntity(net.minecraft.client.renderer.chunk.SectionCompiler$Results,net.minecraft.world.level.block.entity.BlockEntity) -> a
```

---

## com.mojang.blaze3d.systems.RenderSystem

**Tier:** 3  
**Uso:** Sistema de estado GL — hook global (cuidado com performance)

**Nome obfuscado:** `com.mojang.blaze3d.systems.RenderSystem`

```
com.mojang.blaze3d.systems.RenderSystem -> com.mojang.blaze3d.systems.RenderSystem:
    org.slf4j.Logger LOGGER -> LOGGER
    int MINIMUM_ATLAS_TEXTURE_SIZE -> MINIMUM_ATLAS_TEXTURE_SIZE
    int PROJECTION_MATRIX_UBO_SIZE -> PROJECTION_MATRIX_UBO_SIZE
    java.lang.Thread renderThread -> renderThread
    com.mojang.blaze3d.systems.GpuDevice DEVICE -> DEVICE
    double lastDrawTime -> lastDrawTime
    com.mojang.blaze3d.systems.RenderSystem$AutoStorageIndexBuffer sharedSequential -> sharedSequential
    com.mojang.blaze3d.systems.RenderSystem$AutoStorageIndexBuffer sharedSequentialQuad -> sharedSequentialQuad
    com.mojang.blaze3d.systems.RenderSystem$AutoStorageIndexBuffer sharedSequentialLines -> sharedSequentialLines
    com.mojang.blaze3d.ProjectionType projectionType -> projectionType
    com.mojang.blaze3d.ProjectionType savedProjectionType -> savedProjectionType
    org.joml.Matrix4fStack modelViewStack -> modelViewStack
    com.mojang.blaze3d.buffers.GpuBufferSlice shaderFog -> shaderFog
    com.mojang.blaze3d.buffers.GpuBufferSlice shaderLightDirections -> shaderLightDirections
    com.mojang.blaze3d.buffers.GpuBufferSlice projectionMatrixBuffer -> projectionMatrixBuffer
    com.mojang.blaze3d.buffers.GpuBufferSlice savedProjectionMatrixBuffer -> savedProjectionMatrixBuffer
    java.lang.String apiDescription -> apiDescription
    java.util.concurrent.atomic.AtomicLong pollEventsWaitStart -> pollEventsWaitStart
    java.util.concurrent.atomic.AtomicBoolean pollingEvents -> pollingEvents
    net.minecraft.util.ArrayListDeque PENDING_FENCES -> PENDING_FENCES
    com.mojang.blaze3d.textures.GpuTextureView outputColorTextureOverride -> outputColorTextureOverride
    com.mojang.blaze3d.textures.GpuTextureView outputDepthTextureOverride -> outputDepthTextureOverride
    com.mojang.blaze3d.buffers.GpuBuffer globalSettingsUniform -> globalSettingsUniform
    net.minecraft.client.renderer.DynamicUniforms dynamicUniforms -> dynamicUniforms
    com.mojang.blaze3d.systems.ScissorState scissorStateForRenderTypeDraws -> scissorStateForRenderTypeDraws
    com.mojang.blaze3d.systems.SamplerCache samplerCache -> samplerCache
    39:39:void <init>() -> <init>
    98:98:com.mojang.blaze3d.systems.SamplerCache getSamplerCache() -> getSamplerCache
    102:106:void initRenderThread() -> initRenderThread
    109:109:boolean isOnRenderThread() -> isOnRenderThread
    113:116:void assertOnRenderThread() -> assertOnRenderThread
    119:119:java.lang.IllegalStateException constructThreadException() -> constructThreadException
    123:128:void pollEvents() -> pollEvents
    131:131:boolean isFrozenAtPollEvents() -> isFrozenAtPollEvents
    136:148:void flipFrame(com.mojang.blaze3d.platform.Window,com.mojang.blaze3d.TracyFrameCapture) -> flipFrame
    153:160:void limitDisplayFPS(int) -> limitDisplayFPS
    165:166:void setShaderFog(com.mojang.blaze3d.buffers.GpuBufferSlice) -> setShaderFog
    169:169:com.mojang.blaze3d.buffers.GpuBufferSlice getShaderFog() -> getShaderFog
    173:174:void setShaderLights(com.mojang.blaze3d.buffers.GpuBufferSlice) -> setShaderLights
    177:177:com.mojang.blaze3d.buffers.GpuBufferSlice getShaderLights() -> getShaderLights
    181:182:void enableScissorForRenderTypeDraws(int,int,int,int) -> enableScissorForRenderTypeDraws
    185:186:void disableScissorForRenderTypeDraws() -> disableScissorForRenderTypeDraws
    189:189:com.mojang.blaze3d.systems.ScissorState getScissorStateForRenderTypeDraws() -> getScissorStateForRenderTypeDraws
    193:193:java.lang.String getBackendDescription() -> getBackendDescription
    197:197:java.lang.String getApiDescription() -> getApiDescription
    201:201:net.minecraft.util.TimeSource$NanoTimeSource initBackendSystem() -> initBackendSystem
    205:209:void initRenderer(long,int,boolean,com.mojang.blaze3d.shaders.ShaderSource,boolean) -> initRenderer
    212:213:void setErrorCallback(org.lwjgl.glfw.GLFWErrorCallbackI) -> setErrorCallback
    216:217:void setupDefaultState() -> setupDefaultState
    220:223:void setProjectionMatrix(com.mojang.blaze3d.buffers.GpuBufferSlice,com.mojang.blaze3d.ProjectionType) -> setProjectionMatrix
    226:229:void backupProjectionMatrix() -> backupProjectionMatrix
    232:235:void restoreProjectionMatrix() -> restoreProjectionMatrix
    238:239:com.mojang.blaze3d.buffers.GpuBufferSlice getProjectionMatrixBuffer() -> getProjectionMatrixBuffer
    243:244:org.joml.Matrix4f getModelViewMatrix() -> getModelViewMatrix
    248:249:org.joml.Matrix4fStack getModelViewStack() -> getModelViewStack
    253:257:com.mojang.blaze3d.systems.RenderSystem$AutoStorageIndexBuffer getSequentialBuffer(com.mojang.blaze3d.vertex.VertexFormat$Mode) -> getSequentialBuffer
    262:263:void setGlobalSettingsUniform(com.mojang.blaze3d.buffers.GpuBuffer) -> setGlobalSettingsUniform
    266:266:com.mojang.blaze3d.buffers.GpuBuffer getGlobalSettingsUniform() -> getGlobalSettingsUniform
    270:271:com.mojang.blaze3d.ProjectionType getProjectionType() -> getProjectionType
    275:276:void queueFencedTask(java.lang.Runnable) -> queueFencedTask
    280:295:void executePendingTasks() -> executePendingTasks
    298:301:com.mojang.blaze3d.systems.GpuDevice getDevice() -> getDevice
    305:305:com.mojang.blaze3d.systems.GpuDevice tryGetDevice() -> tryGetDevice
    309:312:net.minecraft.client.renderer.DynamicUniforms getDynamicUniforms() -> getDynamicUniforms
    317:333:void bindDefaultUniforms(com.mojang.blaze3d.systems.RenderPass) -> bindDefaultUniforms
    61:67:void lambda$static$1(it.unimi.dsi.fastutil.ints.IntConsumer,int) -> lambda$static$1
    53:59:void lambda$static$0(it.unimi.dsi.fastutil.ints.IntConsumer,int) -> lambda$static$0
    40:95:void <clinit>() -> <clinit>
```

---

## com.mojang.blaze3d.opengl.GlDevice

**Tier:** 3  
**Uso:** Device GL — candidato para marker ESSL

**Nome obfuscado:** `fxe`

```
com.mojang.blaze3d.opengl.GlDevice -> fxe:
    org.slf4j.Logger LOGGER -> g
    boolean USE_GL_ARB_vertex_attrib_binding -> a
    boolean USE_GL_KHR_debug -> b
    boolean USE_GL_EXT_debug_label -> c
    boolean USE_GL_ARB_debug_output -> d
    boolean USE_GL_ARB_direct_state_access -> e
    boolean USE_GL_ARB_buffer_storage -> f
    com.mojang.blaze3d.systems.CommandEncoder encoder -> h
    com.mojang.blaze3d.opengl.GlDebug debugLog -> i
    com.mojang.blaze3d.opengl.GlDebugLabel debugLabels -> j
    int maxSupportedTextureSize -> k
    com.mojang.blaze3d.opengl.DirectStateAccess directStateAccess -> l
    com.mojang.blaze3d.shaders.ShaderSource defaultShaderSource -> m
    java.util.Map pipelineCache -> n
    java.util.Map shaderCache -> o
    com.mojang.blaze3d.opengl.VertexArrayCache vertexArrayCache -> p
    com.mojang.blaze3d.opengl.BufferStorage bufferStorage -> q
    java.util.Set enabledExtensions -> r
    int uniformOffsetAlignment -> s
    int maxSupportedAnisotropy -> t
    67:107:void <init>(long,int,boolean,com.mojang.blaze3d.shaders.ShaderSource,boolean) -> <init>
    110:110:com.mojang.blaze3d.opengl.GlDebugLabel debugLabels() -> a
    116:116:com.mojang.blaze3d.systems.CommandEncoder createCommandEncoder() -> createCommandEncoder
    121:121:int getMaxSupportedAnisotropy() -> getMaxSupportedAnisotropy
    126:129:com.mojang.blaze3d.textures.GpuSampler createSampler(com.mojang.blaze3d.textures.AddressMode,com.mojang.blaze3d.textures.AddressMode,com.mojang.blaze3d.textures.FilterMode,com.mojang.blaze3d.textures.FilterMode,int,java.util.OptionalDouble) -> createSampler
    134:134:com.mojang.blaze3d.textures.GpuTexture createTexture(java.util.function.Supplier,int,com.mojang.blaze3d.textures.TextureFormat,int,int,int,int) -> createTexture
    139:203:com.mojang.blaze3d.textures.GpuTexture createTexture(java.lang.String,int,com.mojang.blaze3d.textures.TextureFormat,int,int,int,int) -> createTexture
    208:208:com.mojang.blaze3d.textures.GpuTextureView createTextureView(com.mojang.blaze3d.textures.GpuTexture) -> createTextureView
    213:221:com.mojang.blaze3d.textures.GpuTextureView createTextureView(com.mojang.blaze3d.textures.GpuTexture,int,int) -> createTextureView
    226:239:com.mojang.blaze3d.buffers.GpuBuffer createBuffer(java.util.function.Supplier,int,long) -> createBuffer
    244:258:com.mojang.blaze3d.buffers.GpuBuffer createBuffer(java.util.function.Supplier,int,java.nio.ByteBuffer) -> createBuffer
    263:266:java.lang.String getImplementationInformation() -> getImplementationInformation
    271:271:java.util.List getLastDebugMessages() -> getLastDebugMessages
    276:276:boolean isDebuggingEnabled() -> isDebuggingEnabled
    281:281:java.lang.String getRenderer() -> getRenderer
    286:286:java.lang.String getVendor() -> getVendor
    291:291:java.lang.String getBackendName() -> getBackendName
    296:296:java.lang.String getVersion() -> getVersion
    300:310:int getMaxSupportedTextureSize() -> e
    315:315:int getMaxTextureSize() -> getMaxTextureSize
    320:320:int getUniformOffsetAlignment() -> getUniformOffsetAlignment
    325:342:void clearPipelineCache() -> clearPipelineCache
    347:352:void sacrificeShaderToOpenGlAndAmd() -> f
    356:356:java.util.List getEnabledExtensions() -> getEnabledExtensions
    361:362:void close() -> close
    365:365:com.mojang.blaze3d.opengl.DirectStateAccess directStateAccess() -> b
    369:369:com.mojang.blaze3d.opengl.GlRenderPipeline getOrCompilePipeline(com.mojang.blaze3d.pipeline.RenderPipeline) -> a
    373:374:com.mojang.blaze3d.opengl.GlShaderModule getOrCompileShader(net.minecraft.resources.Identifier,com.mojang.blaze3d.shaders.ShaderType,net.minecraft.client.renderer.ShaderDefines,com.mojang.blaze3d.shaders.ShaderSource) -> a
    379:380:com.mojang.blaze3d.opengl.GlRenderPipeline precompilePipeline(com.mojang.blaze3d.pipeline.RenderPipeline,com.mojang.blaze3d.shaders.ShaderSource) -> a
    384:402:com.mojang.blaze3d.opengl.GlShaderModule compileShader(com.mojang.blaze3d.opengl.GlDevice$ShaderCompilationKey,com.mojang.blaze3d.shaders.ShaderSource) -> a
    406:423:com.mojang.blaze3d.opengl.GlProgram compileProgram(com.mojang.blaze3d.pipeline.RenderPipeline,com.mojang.blaze3d.shaders.ShaderSource) -> b
    428:428:com.mojang.blaze3d.opengl.GlRenderPipeline compilePipeline(com.mojang.blaze3d.pipeline.RenderPipeline,com.mojang.blaze3d.shaders.ShaderSource) -> c
    432:432:com.mojang.blaze3d.opengl.VertexArrayCache vertexArrayCache() -> c
    436:436:com.mojang.blaze3d.opengl.BufferStorage getBufferStorage() -> d
    50:50:com.mojang.blaze3d.pipeline.CompiledRenderPipeline precompilePipeline(com.mojang.blaze3d.pipeline.RenderPipeline,com.mojang.blaze3d.shaders.ShaderSource) -> precompilePipeline
    380:380:com.mojang.blaze3d.opengl.GlRenderPipeline lambda$precompilePipeline$2(com.mojang.blaze3d.shaders.ShaderSource,com.mojang.blaze3d.pipeline.RenderPipeline) -> a
    374:374:com.mojang.blaze3d.opengl.GlShaderModule lambda$getOrCompileShader$1(com.mojang.blaze3d.shaders.ShaderSource,com.mojang.blaze3d.opengl.GlDevice$ShaderCompilationKey) -> a
    369:369:com.mojang.blaze3d.opengl.GlRenderPipeline lambda$getOrCompilePipeline$0(com.mojang.blaze3d.pipeline.RenderPipeline) -> b
    51:59:void <clinit>() -> <clinit>
```

---

## com.mojang.blaze3d.opengl.GlProgram

**Tier:** 3  
**Uso:** Wrapper de programa GL — candidato para cache de binário

**Nome obfuscado:** `fxg`

```
com.mojang.blaze3d.opengl.GlProgram -> fxg:
    org.slf4j.Logger LOGGER -> c
    java.util.Set BUILT_IN_UNIFORMS -> a
    com.mojang.blaze3d.opengl.GlProgram INVALID_PROGRAM -> b
    java.util.Map uniformsByName -> d
    int programId -> e
    java.lang.String debugLabel -> f
    33:42:void <init>(int,java.lang.String) -> <init>
    45:69:com.mojang.blaze3d.opengl.GlProgram link(com.mojang.blaze3d.opengl.GlShaderModule,com.mojang.blaze3d.opengl.GlShaderModule,com.mojang.blaze3d.vertex.VertexFormat,java.lang.String) -> a
    73:127:void setupUniforms(java.util.List,java.util.List) -> a
    131:133:void close() -> close
    136:137:com.mojang.blaze3d.opengl.Uniform getUniform(java.lang.String) -> a
    142:142:int getProgramId() -> a
    147:147:java.lang.String toString() -> toString
    151:151:java.lang.String getDebugLabel() -> b
    155:155:java.util.Map getUniforms() -> c
    21:31:void <clinit>() -> <clinit>
```

---

## com.mojang.blaze3d.opengl.GlShaderModule

**Tier:** 3  
**Uso:** Wrapper de shader GL — detecção de ESSL nativo

**Nome obfuscado:** `fxk`

```
com.mojang.blaze3d.opengl.GlShaderModule -> fxk:
    int NOT_ALLOCATED -> b
    com.mojang.blaze3d.opengl.GlShaderModule INVALID_SHADER -> a
    net.minecraft.resources.Identifier id -> c
    int shaderId -> d
    com.mojang.blaze3d.shaders.ShaderType type -> e
    15:19:void <init>(int,net.minecraft.resources.Identifier,com.mojang.blaze3d.shaders.ShaderType) -> <init>
    23:29:void close() -> close
    32:32:net.minecraft.resources.Identifier getId() -> a
    36:36:int getShaderId() -> b
    40:40:java.lang.String getDebugLabel() -> c
    9:9:void <clinit>() -> <clinit>
```

---

# TIER 4 — Textura/Atlas (ASTC redirect)

## net.minecraft.client.renderer.texture.TextureAtlas

**Tier:** 4  
**Uso:** Atlas de texturas (novo nome em 1.21.11)

**Nome obfuscado:** `ilo`

```
net.minecraft.client.renderer.texture.TextureAtlas -> ilo:
    org.slf4j.Logger LOGGER -> g
    net.minecraft.resources.Identifier LOCATION_BLOCKS -> d
    net.minecraft.resources.Identifier LOCATION_ITEMS -> e
    net.minecraft.resources.Identifier LOCATION_PARTICLES -> f
    java.util.List sprites -> h
    java.util.List animatedTexturesStates -> i
    java.util.Map texturesByName -> j
    net.minecraft.client.renderer.texture.TextureAtlasSprite missingSprite -> k
    net.minecraft.resources.Identifier location -> l
    int maxSupportedTextureSize -> m
    int width -> n
    int height -> o
    int maxMipLevel -> p
    int mipLevelCount -> q
    com.mojang.blaze3d.textures.GpuTextureView[] mipViews -> r
    com.mojang.blaze3d.buffers.GpuBuffer spriteUbos -> s
    53:70:void <init>(net.minecraft.resources.Identifier) -> <init>
    74:88:void createTexture(int,int,int) -> a
    91:146:void upload(net.minecraft.client.renderer.texture.SpriteLoader$Preparations) -> a
    149:197:void uploadInitialContents() -> l
    201:204:void dumpContents(net.minecraft.resources.Identifier,java.nio.file.Path) -> a
    207:216:void dumpSpriteNames(java.nio.file.Path,java.lang.String,java.util.Map) -> a
    219:228:void cycleAnimationFrames() -> d
    231:242:void uploadAnimationFrames() -> m
    246:247:void tick() -> e
    250:254:net.minecraft.client.renderer.texture.TextureAtlasSprite getSprite(net.minecraft.resources.Identifier) -> a
    258:258:net.minecraft.client.renderer.texture.TextureAtlasSprite missingSprite() -> f
    262:268:void clearTextureData() -> g
    272:283:void close() -> close
    286:286:net.minecraft.resources.Identifier location() -> h
    290:290:int maxSupportedTextureSize() -> i
    294:294:int getWidth() -> j
    298:298:int getHeight() -> k
    233:233:java.lang.String lambda$uploadAnimationFrames$6() -> n
    202:202:int lambda$dumpContents$5(int) -> a
    175:175:java.lang.String lambda$uploadInitialContents$4() -> o
    173:173:java.lang.String lambda$uploadInitialContents$3() -> p
    163:163:java.lang.String lambda$uploadInitialContents$2(net.minecraft.client.renderer.texture.TextureAtlasSprite) -> a
    155:155:boolean lambda$uploadInitialContents$1(net.minecraft.client.renderer.texture.TextureAtlasSprite) -> b
    116:116:java.lang.String lambda$upload$0() -> q
    35:50:void <clinit>() -> <clinit>
```

---

## net.minecraft.client.renderer.texture.SpriteLoader

**Tier:** 4  
**Uso:** Carregador de sprites — ponto de ASTC redirect

**Nome obfuscado:** `ill`

```
net.minecraft.client.renderer.texture.SpriteLoader -> ill:
    org.slf4j.Logger LOGGER -> a
    net.minecraft.resources.Identifier location -> b
    int maxSupportedTextureSize -> c
    39:42:void <init>(net.minecraft.resources.Identifier,int) -> <init>
    45:45:net.minecraft.client.renderer.texture.SpriteLoader create(net.minecraft.client.renderer.texture.TextureAtlas) -> a
    49:102:net.minecraft.client.renderer.texture.SpriteLoader$Preparations stitch(java.util.List,int,java.util.concurrent.Executor) -> a
    106:107:java.util.concurrent.CompletableFuture runSpriteSuppliers(net.minecraft.client.renderer.texture.atlas.SpriteResourceLoader,java.util.List,java.util.concurrent.Executor) -> a
    111:114:java.util.concurrent.CompletableFuture loadAndStitch(net.minecraft.server.packs.resources.ResourceManager,net.minecraft.resources.Identifier,int,java.util.concurrent.Executor,java.util.Set) -> a
    118:124:java.util.Map getStitchedSprites(net.minecraft.client.renderer.texture.Stitcher,int,int) -> a
    121:121:void lambda$getStitchedSprites$10(java.util.Map,int,int,net.minecraft.client.renderer.texture.SpriteContents,int,int,int) -> a
    114:114:net.minecraft.client.renderer.texture.SpriteLoader$Preparations lambda$loadAndStitch$9(int,java.util.concurrent.Executor,java.util.List) -> a
    113:113:java.util.concurrent.CompletionStage lambda$loadAndStitch$8(net.minecraft.client.renderer.texture.atlas.SpriteResourceLoader,java.util.concurrent.Executor,java.util.List) -> a
    112:112:java.util.List lambda$loadAndStitch$7(net.minecraft.server.packs.resources.ResourceManager,net.minecraft.resources.Identifier) -> a
    107:107:java.util.List lambda$runSpriteSuppliers$6(java.util.List) -> a
    106:106:java.util.concurrent.CompletableFuture lambda$runSpriteSuppliers$5(net.minecraft.client.renderer.texture.atlas.SpriteResourceLoader,java.util.concurrent.Executor,net.minecraft.client.renderer.texture.atlas.SpriteSource$Loader) -> a
    106:106:net.minecraft.client.renderer.texture.SpriteContents lambda$runSpriteSuppliers$4(net.minecraft.client.renderer.texture.atlas.SpriteSource$Loader,net.minecraft.client.renderer.texture.atlas.SpriteResourceLoader) -> a
    99:99:void lambda$stitch$3(java.util.Map,int) -> a
    99:99:void lambda$stitch$2(int,net.minecraft.client.renderer.texture.TextureAtlasSprite) -> a
    88:88:java.lang.String lambda$stitch$1(net.minecraft.client.renderer.texture.Stitcher$Entry) -> a
    49:49:java.lang.String lambda$stitch$0() -> a
    34:34:void <clinit>() -> <clinit>
```

---

## net.minecraft.client.renderer.texture.SpriteContents

**Tier:** 4  
**Uso:** Conteúdo de sprite — candidato a interceptar upload

**Nome obfuscado:** `ilk`

```
net.minecraft.client.renderer.texture.SpriteContents -> ilk:
    org.slf4j.Logger LOGGER -> b
    int UBO_SIZE -> a
    net.minecraft.resources.Identifier name -> c
    int width -> d
    int height -> e
    com.mojang.blaze3d.platform.NativeImage originalImage -> f
    com.mojang.blaze3d.platform.NativeImage[] byMipLevel -> g
    net.minecraft.client.renderer.texture.SpriteContents$AnimatedTexture animatedTexture -> h
    java.util.List additionalMetadata -> i
    net.minecraft.client.renderer.texture.MipmapStrategy mipmapStrategy -> j
    float alphaCutoffBias -> k
    64:65:void <init>(net.minecraft.resources.Identifier,net.minecraft.client.resources.metadata.animation.FrameSize,com.mojang.blaze3d.platform.NativeImage) -> <init>
    67:80:void <init>(net.minecraft.resources.Identifier,net.minecraft.client.resources.metadata.animation.FrameSize,com.mojang.blaze3d.platform.NativeImage,java.util.Optional,java.util.List,java.util.Optional) -> <init>
    84:97:void increaseMipLevel(int) -> a
    100:100:int getFrameCount() -> f
    104:104:boolean isAnimated() -> a
    108:163:net.minecraft.client.renderer.texture.SpriteContents$AnimatedTexture createAnimatedTexture(net.minecraft.client.resources.metadata.animation.FrameSize,int,int,net.minecraft.client.resources.metadata.animation.AnimationMetadataSection) -> a
    168:168:int width() -> b
    173:173:int height() -> c
    178:178:net.minecraft.resources.Identifier name() -> d
    182:182:java.util.stream.IntStream getUniqueFrames() -> e
    186:186:net.minecraft.client.renderer.texture.SpriteContents$AnimationState createAnimationState(com.mojang.blaze3d.buffers.GpuBufferSlice,int) -> a
    190:196:java.util.Optional getAdditionalMetadata(net.minecraft.server.packs.metadata.MetadataSectionType) -> a
    201:204:void close() -> close
    208:208:java.lang.String toString() -> toString
    212:218:boolean isTransparent(int,int,int) -> a
    222:223:void uploadFirstFrame(com.mojang.blaze3d.textures.GpuTexture,int) -> a
    153:153:boolean lambda$createAnimatedTexture$4(it.unimi.dsi.fastutil.ints.IntSet,int) -> a
    93:93:java.lang.String lambda$increaseMipLevel$3() -> g
    91:91:java.lang.String lambda$increaseMipLevel$2() -> h
    90:90:java.lang.String lambda$increaseMipLevel$1() -> i
    74:74:net.minecraft.client.renderer.texture.SpriteContents$AnimatedTexture lambda$new$0(net.minecraft.client.resources.metadata.animation.FrameSize,com.mojang.blaze3d.platform.NativeImage,net.minecraft.client.resources.metadata.animation.AnimationMetadataSection) -> a
    42:50:void <clinit>() -> <clinit>
```

---

# TIER 5 — Não usar (referência)

As classes abaixo foram consideradas e descartadas:

- Todos os entity renderers específicos (CowRenderer, PigRenderer, etc.) — usam o pipeline genérico
- Debug renderers — apenas em modo F3 do próprio Minecraft
- GUI/item renderers — fora do escopo (mundo apenas)
- Bibliotecas de terceiros (netty, fastutil) — não interessa
