package net.hollowed.antique.util.shockwave;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector3fc;

import java.util.*;

public class ShockwaveManager {
    public static final Map<Block, Float> RESISTANCES = new HashMap<>();

    static {
        RESISTANCES.put(Blocks.AIR, 0.9f);
        RESISTANCES.put(Blocks.WATER, 0.95f);
        RESISTANCES.put(Blocks.LAVA, 0.8f);
    }

    public static final int MAX_PATH_LENGTH = 4;

    protected @NotNull List<Node> nodes = new ArrayList<>();
    public final float strength;
    public final int delay;
    protected int delayCounter = 0;

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
                nodeImpact *= 0.75f; // TODO
            }

            return allowance / denominator;
        }
    }

    public ShockwaveManager(@NotNull BlockPos pos, @NotNull Direction dir, float strength, int delay) {
        nodes.add(new Node(pos, strength, dir));
        this.strength = strength;
        this.delay = delay;
    }

    /**
     * @return If the shockwave is finished
     */
    public boolean tick(@NotNull ServerLevel level) {
        if (delayCounter++ == delay) {
            delayCounter = 0;

            List<Node> nextNodes = new ArrayList<>();

            for (Node node : nodes) {
                for (LivingEntity entity : level.getEntitiesOfClass(LivingEntity.class, new AABB(node.pos))) {
                    double speed = node.charge / strength * 4;
                    entity.push(node.path[node.path.length - 1].getUnitVec3().multiply(speed + ((Math.random() - 0.5) / 10), speed + ((Math.random() - 0.5) / 10), speed + ((Math.random() - 0.5) / 10)));
                    entity.hurtMarked = true;
                }

                level.sendParticles(ParticleTypes.WAX_ON, node.pos.getX() + 0.5, node.pos.getY() + 0.5, node.pos.getZ() + 0.5, 1, 0, 0, 0, 0);

                float totalAllowance = 0;
                float[] allowances = new float[6];
                Direction dontGoThatWay = node.path[node.path.length - 1].getOpposite();

                for (Direction dir : Direction.values()) {
                    if (dir != dontGoThatWay) {
                        BlockPos otherPos = node.pos.relative(dir);
                        BlockState otherState = level.getBlockState(otherPos);

                        if (RESISTANCES.containsKey(otherState.getBlock())) {
                            float allowance = node.calculateDirectionAllowance(dir);
                            allowance = (float) Math.pow(allowance, 2);

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

                            if (RESISTANCES.containsKey(otherState.getBlock())) {
                                float allowance = allowances[dir.ordinal()] / totalAllowance;
                                float charge = (node.charge) * allowance * RESISTANCES.get(otherState.getBlock());

                                if (charge <= 1e-2) {
                                    continue;
                                }

                                Direction[] path;

                                if (node.path.length == MAX_PATH_LENGTH) {
                                    path = new Direction[MAX_PATH_LENGTH];
                                    System.arraycopy(node.path, 1, path, 0, MAX_PATH_LENGTH - 1);
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

        return nodes.isEmpty();
    }
}