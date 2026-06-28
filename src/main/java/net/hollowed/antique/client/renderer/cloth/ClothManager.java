package net.hollowed.antique.client.renderer.cloth;

import java.lang.Math;
import java.util.*;

import net.hollowed.antique.Antiquities;
import net.hollowed.antique.AntiquitiesClient;
import net.hollowed.antique.client.cloth.ClothOwner;
import net.hollowed.antique.client.sound.cloth.AmbientClothSoundInstance;
import net.hollowed.antique.entities.parts.MyriadShovelPart;
import net.hollowed.antique.index.AntiqueBlockTags;
import net.hollowed.antique.index.AntiqueParticles;
import net.hollowed.antique.util.FastNoiseLite;
import net.hollowed.antique.util.resources.*;
import net.hollowed.antique.mixin.accessors.SpriteContentsAnimationStateAccessor;
import net.hollowed.antique.particles.TyphoSparkParticle;
import net.hollowed.antique.util.resources.client.ClothModelData;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.entity.decoration.BlockAttachedEntity;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LayeredCauldronBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;
import org.joml.*;

import com.mojang.blaze3d.vertex.PoseStack;

import net.hollowed.antique.util.interfaces.duck.ClothAccess;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.NonNull;

import static org.lwjgl.glfw.GLFW.glfwGetTime;

public class ClothManager {

    public static final FastNoiseLite WIND_DIR_NOISE = new FastNoiseLite();
    public static final FastNoiseLite WIND_NOISE = new FastNoiseLite();

    static {
        WIND_DIR_NOISE.SetNoiseType(FastNoiseLite.NoiseType.Perlin);
        WIND_DIR_NOISE.SetFrequency(0.005f);

        WIND_NOISE.SetNoiseType(FastNoiseLite.NoiseType.Perlin);
        WIND_NOISE.SetFrequency(0.05f);
    }

    public @Nullable AmbientClothSoundInstance ambientSound;

    public final Vector3f pos = new Vector3f();
    public List<ClothBody> bodies = new ArrayList<>();
    private int bodyCountCooldown = 0;
    public ClothOwner owner;
    public ClothSkinData data;
    public boolean rendered = false;

    private List<Entity> collisionEntities = List.of();
    private long prevTime;

    public ClothManager(Vector3f pos, int bodyCount, ClothSkinData data) {
        this.pos.set(pos);
        reset(bodyCount, data);
    }

    public void reset(int bodyCount, ClothSkinData data) {
        bodies.clear();
        this.data = data;
        for (int i = 0; i < bodyCount + 1; i++) {
            bodies.add(new ClothBody(pos));
        }
    }

    public void setBodyCount(int count) {
        if (count != bodies.size() + 1) {
            reset(count, this.data);
        }
    }

    public static boolean isWater(Level level, Vector3f pos) {
        BlockPos blockPos = BlockPos.containing(pos.x, pos.y, pos.z);
        float fract = pos.y - blockPos.getY();
        FluidState fluid = level.getFluidState(blockPos);

        if (fluid.is(FluidTags.WATER)) {
            return fract <= fluid.getHeight(level, blockPos);
        }

        BlockState block = level.getBlockState(blockPos);

        if (block.is(Blocks.WATER_CAULDRON)) {
            switch (block.getValue(LayeredCauldronBlock.LEVEL)) {
                case 1 -> {
                    return fract <= 10f / 16f;
                }
                case 2 -> {
                    return fract <= 13f / 16f;
                }
                case 3 -> {
                    return true;
                }
            }
        }

        return false;
    }

    public void tickSound() {
        ClientLevel level = Minecraft.getInstance().level;

        if (level != null) {
            data.ambientSound().ifPresent(soundData -> {
                Optional<Identifier> sound = soundData.sound();

                if (bodies.stream().anyMatch(body -> level.isRainingAt(body.blockPos()) || isWater(level, body.pos))) {
                    sound = soundData.waterSound().or(soundData::sound);
                }

                sound.ifPresentOrElse(id -> {
                    if (ambientSound == null) {
                        ambientSound = new AmbientClothSoundInstance(
                                new SoundEvent(id, Optional.of(8f)),
                                owner.getSoundSource(),
                                owner
                        );
                        Minecraft.getInstance().getSoundManager().play(ambientSound);
                    } else if (!ambientSound.getIdentifier().equals(id)) {
                        ambientSound.publicStop();
                        ambientSound = new AmbientClothSoundInstance(
                                new SoundEvent(id, Optional.of(8f)),
                                owner.getSoundSource(),
                                owner
                        );
                        Minecraft.getInstance().getSoundManager().play(ambientSound);
                    }
                }, () -> {
                    if (ambientSound != null) {
                        ambientSound.publicStop();
                        ambientSound = null;
                    }
                });
            });
        }
    }

    public static Vector3f getViewVector(float x) {
        return new Vector3f(
                (float) -Math.sin(Math.toRadians(x)),
                0,
                (float) -Math.cos(Math.toRadians(x))
        );
    }

    public static Vector3f getViewVector(float x, float y) {
        return new Vector3f(
                (float) (-Math.sin(Math.toRadians(x)) * Math.cos(Math.toRadians(y))),
                (float) -Math.sin(Math.toRadians(y)),
                (float) (-Math.cos(Math.toRadians(x)) * Math.cos(Math.toRadians(y)))
        );
    }

    public void tick() {
        Level level = owner.getLevel();

        for (ClothBody body : bodies) {
            body.prevPos = new Vector3f(body.pos);
        }

        bodies.getFirst().pos.set(pos);

        // Update pass
        for (ClothBody body : bodies) {
            boolean isWater = isWater(level, body.pos);

            // Apply gravity
            float gravity = 0.1f * data.gravity();
            if (isWater) {
                gravity *= data.waterGravity();
            }

            body.velocity.add(0, -gravity, 0);

            float dir = WIND_DIR_NOISE.GetNoise((float) glfwGetTime() * 20, 0) * 45; // 90 degree slice going negative Z

            float thunder = level.getThunderLevel(0);
            float wind = Math.max(0, WIND_NOISE.GetNoise(body.pos.x, body.pos.z - (float) glfwGetTime() * 20) / 2 + 0.25f) + thunder * 0.75f;

            int worldHeight = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, (int) body.pos.x, (int) body.pos.z);
            float mountainScale = Mth.clamp((float) (worldHeight - 80) / 400, 0, 0.1f);

            Vector3f totalWind = getViewVector(dir)
                    .mul(wind * 0.2f + mountainScale)
                    .mul(Mth.clamp((body.pos.y - (worldHeight - 10)) / 10, 0, 2.5f));

            Vector2d[] offsets = {
                    new Vector2d(),
                    new Vector2d(-15, 0),
                    new Vector2d(15, 0),
                    new Vector2d(0, -15),
                    new Vector2d(0, 15)
            };
            float average = 0;
            float maximum = 0;

            BlockPos excludePos;

            if (owner.asEntity() instanceof BlockAttachedEntity block) {
                excludePos = block.getPos();
            } else {
                excludePos = null;
            }

            for (Vector2d offset : offsets) {
                BlockHitResult ray = level.clip(new ClipContext(
                        new Vec3(new Vector3f(body.pos)),
                        new Vec3(new Vector3f(body.pos).add(getViewVector((float) (dir + offset.x), (float) offset.y).mul(-32))),
                        ClipContext.Block.COLLIDER,
                        ClipContext.Fluid.SOURCE_ONLY,
                        CollisionContext.emptyWithFluidCollisions()
                ) {
                    @Override
                    public @NonNull VoxelShape getBlockShape(@NonNull BlockState blockState, @NonNull BlockGetter blockGetter, @NonNull BlockPos blockPos) {
                        if (Objects.equals(blockPos, excludePos) || blockState.is(AntiqueBlockTags.WIND_PASSABLE)) {
                            return Shapes.empty();
                        }

                        return super.getBlockShape(blockState, blockGetter, blockPos);
                    }
                });

                if (ray.getType() == HitResult.Type.BLOCK) {
                    float distance = Mth.clamp(1 - (float) ray.getLocation().distanceTo(new Vec3(new Vector3f(body.pos))) / 32, 0, 1);
                    float v = 1 - distance * distance;
                    average += v;

                    if (maximum < v) {
                        maximum = v;
                    }
                } else {
                    average += 1;

                    if (maximum < 1) {
                        maximum = 1;
                    }
                }
            }

            body.velocity.add(totalWind.mul((average / offsets.length + maximum) / 2));

            body.update();
        }

        // Collision pass

        if (level.getGameTime() > (prevTime + 10)) {
            prevTime = level.getGameTime();
            Vec3 pos = new Vec3(new Vector3f(bodies.getFirst().pos));
            collisionEntities = level.getEntities(owner.asEntity(), new AABB(pos.subtract(5), pos.add(5)), entity -> !(entity instanceof MyriadShovelPart) && !entity.isSpectator());
        }

        for (ClothBody body : bodies) {
            body.slideOutOfBlocks(level, owner, data);
            body.slideOutOfEntities(collisionEntities, owner.asEntity(), data);
        }

        for (int i = 1; i < bodies.size(); i++) {
            bodies.get(i).containDistance(bodies.get(i - 1).pos, data.length() / (bodies.size() - 1));
        }

        if (rendered) {
            tickSound();
            tickParticles(level);
        }
    }

    public void tickParticles(Level level) {
        data.particleData().ifPresent(data -> {
            for (int i = 0; i < bodies.size(); i++) {
                ClothBody body = bodies.get(i);
                boolean water = level.isRainingAt(body.blockPos()) || isWater(level, body.pos);
                ParticleOptions particle = data.particle();
                float chance = data.chance();
                float distance = data.distance();
                float velocity = data.velocity();

                if (water) {
                    particle = data.waterParticle().orElse(particle);
                    chance = data.waterChance().orElse(chance);
                    distance = data.waterDistance().orElse(distance);
                    velocity = data.waterVelocity().orElse(velocity);
                }

                if (level.getRandom().nextFloat() < chance) {
                    Vector3d pos = new Vector3d(
                            level.getRandom().nextDouble() * 2 - 1,
                            level.getRandom().nextDouble() * 2 - 1,
                            level.getRandom().nextDouble() * 2 - 1
                    ).normalize();

                    if (particle.getType() == AntiqueParticles.TYPHO_SPARK) {
                        SpriteContentsAnimationStateAccessor accessor = ColorProvider.SpriteAnimated.findAnimationState(
                                AntiquitiesClient.CLOTHS_ATLAS,
                                Antiquities.id("cloth/typho_cloth_emissive")
                        );
                        particle = new TyphoSparkParticle.Options(
                                Optional.of((int) (((float) i / bodies.size() + (23 - accessor.antique$getFrame()) / 23f) * 8) % 8)
                        );
                    }

                    level.addParticle(
                            particle,
                            body.pos.x + pos.x * distance,
                            body.pos.y + pos.y * distance,
                            body.pos.z + pos.z * distance,
                            pos.x * velocity,
                            pos.y * velocity,
                            pos.z * velocity
                    );
                }
            }
        });
    }

    public static ClothManager getOrCreate(ClothOwner owner, Identifier id, ClothSkinData data) {
        if (Minecraft.getInstance().level instanceof ClothAccess clothAccess) {
            return clothAccess.antique$getManagers().computeIfAbsent(owner, _ -> new HashMap<>()).computeIfAbsent(id, _ -> {
                ClothManager manager = new ClothManager(new Vector3f(owner.getPosition().toVector3f()), 8, data);
                manager.owner = owner;
                return manager;
            });
        }

        return null;
    }

    public void renderCloth(Holder<ClothSkinData> data, PoseStack matrices, SubmitNodeCollector queue, int light, int color, List<SewnClothPattern> patterns, HolderLookup.Provider registryAccess, float tickDelta) {
        this.renderCloth(data, matrices, queue, light, color, patterns, registryAccess, new Matrix4f(), tickDelta);
    }

    public void renderCloth(Holder<ClothSkinData> skin, PoseStack matrices, SubmitNodeCollector queue, int light, int color, List<SewnClothPattern> patterns, HolderLookup.Provider registryAccess, Matrix4f reprojectionMatrix, float tickDelta) {
        this.rendered = true;
        this.data = skin.value();
        ClothModelData model = ClothModelListener.MODELS.computeIfAbsent(skin.value().model().orElse(skin.unwrapKey().orElseThrow().identifier()), key -> {
            Antiquities.LOGGER.error("Nonexistent cloth model {}", key);
            return ClothModelData.EMPTY;
        });

        int bodyCount = skin.value().bodyAmount();

        if (bodyCount != 0 && this.bodyCountCooldown <= 0 && bodyCount != (bodies.size() - 1)) {
            setBodyCount(bodyCount);
            this.bodyCountCooldown = 3;
        }

        if (this.bodyCountCooldown > 0) {
            this.bodyCountCooldown--;
        }

        Vec3 position = matrixToVec(matrices);

        pos.set(position.x, position.y, position.z);
        bodies.getFirst().renderPos = pos;

        for (int i = 1; i < bodies.size(); i++) {
            bodies.get(i).containDistance(bodies.get(i - 1).renderPos, tickDelta, data.length() / (bodies.size() - 1));
        }

        model.worldRenderer().render(
                this,
                skin,
                matrices,
                queue,
                light,
                color,
                patterns,
                registryAccess,
                reprojectionMatrix,
                tickDelta
        );
    }

    public static Vec3 matrixToVec(PoseStack matrixStack) {
        Matrix4f matrix = matrixStack.last().pose();
        Camera camera = Minecraft.getInstance().gameRenderer.mainCamera();
        Vector4f localPos = new Vector4f(0, 0, 0, 1);
        matrix.transform(localPos);
        Vec3 cameraPos = camera.position();
        return new Vec3(cameraPos.x + localPos.x(), cameraPos.y + localPos.y(), cameraPos.z + localPos.z());
    }

    @Override
    public String toString() {
        return "ClothManager{pos=" + pos + ", owner=" + owner + ", model=" + data.model() + "}";
    }
}
