package net.hollowed.antique.util.math.conversion;

import net.minecraft.world.phys.Vec3;
import org.joml.Vector3d;

public class JomlToMC {

    public static Vec3 fromVector3d(Vector3d vec) { return new Vec3(vec.x, vec.y, vec.z); }

}
