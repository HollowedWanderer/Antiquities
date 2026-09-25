package net.hollowed.antique.util.math.generic;

import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.joml.Quaterniond;
import org.joml.Vector3d;

import static java.lang.Math.cos;
import static java.lang.Math.sin;

public class LMath {

    /**
     * Converts Euler angles in radians to Quaternions
     **/
    public static Quaterniond EulerToQuaternion(@NotNull Vec3 vec) { return EulerToQuaternion(new Vector3d(vec.x, vec.y, vec.z), 0.0); }

    /**
     * Converts Euler angles in radians to Quaternions
     **/
    public static Quaterniond EulerToQuaternion(@NotNull Vec3 vec, double angle) { return EulerToQuaternion(new Vector3d(vec.x, vec.y, vec.z), angle); }

    /**
     * Converts Euler angles in radians to Quaternions
     **/
    public static Quaterniond EulerToQuaternion(double x, double y, double z) { return EulerToQuaternion(new Vector3d(x,y,z), 0.0); }

    /**
     * Converts Euler angles in radians to Quaternions
     **/
    public static Quaterniond EulerToQuaternion(double x, double y, double z, double angle) { return EulerToQuaternion(new Vector3d(x,y,z), angle); }

    /**
     * Converts Euler angles in radians to Quaternions
     **/
    public static Quaterniond EulerToQuaternion(Vector3d vec) { return EulerToQuaternion(vec, 0.0); }

    /**
     * Converts Euler angles in radians to Quaternions
     **/
    public static Quaterniond EulerToQuaternion(@NotNull Vector3d vec, double angle) {
        Vector3d normal = new Vector3d(vec).normalize();
        Vector3d up = new Vector3d(0, 1, 0);
        Vector3d rotAxis = up.cross(normal, new Vector3d());

        if(rotAxis.lengthSquared() == 0) { return normal.y <= 0 ? new Quaterniond().identity() : new Quaterniond().rotateX(Math.PI); }

        double rot = Math.acos(up.dot(normal));

        Quaterniond quaternion = new Quaterniond().fromAxisAngleRad(rotAxis.normalize(), rot);

        Vector3d rollAxis = new Vector3d(
                cos(vec.y)*cos(vec.x),
                sin(vec.y)*cos(vec.x),
                sin(vec.x)
        );

        quaternion.rotateAxis(angle, rollAxis);

        return quaternion;
    }

}
