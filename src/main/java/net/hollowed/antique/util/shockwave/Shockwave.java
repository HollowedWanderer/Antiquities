package net.hollowed.antique.util.shockwave;

import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.hollowed.antique.index.AntiqueBlockTags;
import net.hollowed.antique.networking.ShockwaveParticlesPayload;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector3fc;

import java.util.ArrayList;
import java.util.List;

public class Shockwave {

    protected @NotNull List<Node> nodes = new ArrayList<>();
    public final float strength;
    public final float charge;
    public final int delay;
    protected int delayCounter = 0;

    public Shockwave(@NotNull BlockPos pos, @NotNull Direction dir, float strength, float charge, int delay) {
        nodes.add(new Node(pos, charge, dir));
        this.strength = strength;
        this.charge = charge;
        this.delay = delay;
    }

    public boolean tick(@NotNull ServerLevel level) {
        List<Node> nextNodes = new ArrayList<>();
        if (delayCounter++ == delay) {
            delayCounter = 0;

            for (Node node : nodes) {
                double speed = node.charge / charge * strength;
                List<Entity> entities = level.getEntities(null, new AABB(node.pos));

                for (Entity entity : entities) {
                    this.pushEntity(node, entity, speed);
                }

                this.spawnTravelParticles(level, node);

                float totalAllowance = 0;
                float[] allowances = new float[6];
                Direction dontGoThatWay = node.path[node.path.length - 1].getOpposite();

                for (Direction dir : Direction.values()) {
                    if (dir != dontGoThatWay) {
                        BlockPos otherPos = node.pos.relative(dir);
                        BlockState otherState = level.getBlockState(otherPos);

                        if (ShockwaveManager.RESISTANCES.containsKey(otherState.getBlock()) || otherState.is(AntiqueBlockTags.SHOCKWAVE_PASSABLE)) {
                            float allowance = node.calculateDirectionAllowance(dir);
                            allowance = (float) Math.pow(allowance, 1.5);

                            allowances[dir.ordinal()] = allowance;
                            totalAllowance += allowance;
                        }
                    }
                }

                if (totalAllowance > 0) {
                    for (Direction dir : Direction.values()) {
                        if (dir != dontGoThatWay) {
                            BlockPos otherPos = node.pos.relative(dir);
                            BlockState otherState = level.getBlockState(otherPos);

                            if (ShockwaveManager.RESISTANCES.containsKey(otherState.getBlock()) || otherState.is(AntiqueBlockTags.SHOCKWAVE_PASSABLE)) {
                                float allowance = allowances[dir.ordinal()] / totalAllowance;
                                float charge = (node.charge) * allowance *
                                        (
                                                ShockwaveManager.RESISTANCES.get(otherState.getBlock()) != null ?
                                                        ShockwaveManager.RESISTANCES.get(otherState.getBlock())
                                                        : level.getFluidState(otherPos).is(FluidTags.WATER) ? ShockwaveManager.RESISTANCES.get(Blocks.WATER) : ShockwaveManager.RESISTANCES.get(Blocks.AIR)
                                        );

                                if (charge <= 1e-2) {
                                    continue;
                                }

                                Direction[] path;

                                if (node.path.length == ShockwaveManager.MAX_PATH_LENGTH) {
                                    path = new Direction[ShockwaveManager.MAX_PATH_LENGTH];
                                    System.arraycopy(node.path, 1, path, 0, ShockwaveManager.MAX_PATH_LENGTH - 1);
                                } else {
                                    path = new Direction[node.path.length + 1];
                                    System.arraycopy(node.path, 0, path, 0, node.path.length);
                                }

                                path[path.length - 1] = dir;
                                nextNodes.add(new Node(otherPos, charge, path));
                            }
                        }
                    }
                }
            }

            nodes = nextNodes;
        }

        return nextNodes.isEmpty() && nodes.isEmpty();
    }

    public void pushEntity(Node node, Entity entity, double speed) {
        entity.push(node.path[node.path.length - 1].getUnitVec3().multiply(speed, speed, speed));
        entity.hurtMarked = true;
    }

    public void spawnTravelParticles(ServerLevel level, Node node) {
        double particleVel = 0.75;
        if (level.getFluidState(node.pos).is(FluidTags.WATER)) {
            for (ServerPlayer serverPlayer : level.players().stream().toList()) {
                ServerPlayNetworking.send(serverPlayer,
                        new ShockwaveParticlesPayload(
                                node.pos.getX() + 0.5F,
                                node.pos.getY() + 0.5F,
                                node.pos.getZ() + 0.5F,
                                node.path[node.path.length - 1].getUnitVec3().multiply(particleVel, particleVel, particleVel)
                        )
                );
            }
        }
    }

    public record Node(
            BlockPos pos,
            float charge,
            Direction... path
    ) {
        public float calculateDirectionAllowance(@NotNull Direction dir) {
            if (path.length == 0) {
                return 1;
            }

            float allowance = 0;
            float nodeImpact = 1;
            float denominator = 0;
            Vector3fc dirVec = dir.getUnitVec3f();

            for (Direction pathNode : path) {
                allowance += (pathNode.getUnitVec3f().dot(dirVec) / 2 + 0.5f) * nodeImpact;
                denominator += nodeImpact;
                nodeImpact *= 0.75f;
            }

            return allowance / denominator;
        }
    }
}
