package render;

/**
 * Simple 3D vector class to replace Processing's PVector.
 * Provides the same API so migration is straightforward.
 */
public class Vec3 {

    public float x;
    public float y;
    public float z;

    /**
     * Create a zero vector.
     */
    public Vec3() {
        this.x = 0;
        this.y = 0;
        this.z = 0;
    }

    /**
     * Create a 2D vector (z = 0).
     */
    public Vec3(float x, float y) {
        this.x = x;
        this.y = y;
        this.z = 0;
    }

    /**
     * Create a 3D vector.
     */
    public Vec3(float x, float y, float z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    /**
     * Set the vector components.
     */
    public Vec3 set(float x, float y, float z) {
        this.x = x;
        this.y = y;
        this.z = z;
        return this;
    }

    /**
     * Set from another vector.
     */
    public Vec3 set(Vec3 v) {
        this.x = v.x;
        this.y = v.y;
        this.z = v.z;
        return this;
    }

    /**
     * Create a copy of this vector.
     */
    public Vec3 copy() {
        return new Vec3(x, y, z);
    }

    /**
     * Add another vector to this one.
     */
    public Vec3 add(Vec3 v) {
        this.x += v.x;
        this.y += v.y;
        this.z += v.z;
        return this;
    }

    /**
     * Add components to this vector.
     */
    public Vec3 add(float x, float y, float z) {
        this.x += x;
        this.y += y;
        this.z += z;
        return this;
    }

    /**
     * Subtract another vector from this one.
     */
    public Vec3 sub(Vec3 v) {
        this.x -= v.x;
        this.y -= v.y;
        this.z -= v.z;
        return this;
    }

    /**
     * Subtract components from this vector.
     */
    public Vec3 sub(float x, float y, float z) {
        this.x -= x;
        this.y -= y;
        this.z -= z;
        return this;
    }

    /**
     * Multiply this vector by a scalar.
     */
    public Vec3 mult(float scalar) {
        this.x *= scalar;
        this.y *= scalar;
        this.z *= scalar;
        return this;
    }

    /**
     * Divide this vector by a scalar.
     */
    public Vec3 div(float scalar) {
        if (scalar != 0) {
            this.x /= scalar;
            this.y /= scalar;
            this.z /= scalar;
        }
        return this;
    }

    /**
     * Calculate the magnitude (length) of this vector.
     */
    public float mag() {
        return (float) Math.sqrt(x * x + y * y + z * z);
    }

    /**
     * Calculate the squared magnitude (avoids sqrt for comparisons).
     */
    public float magSq() {
        return x * x + y * y + z * z;
    }

    /**
     * Normalize this vector (make it unit length).
     */
    public Vec3 normalize() {
        float m = mag();
        if (m > 0) {
            div(m);
        }
        return this;
    }

    /**
     * Limit the magnitude of this vector.
     */
    public Vec3 limit(float max) {
        if (magSq() > max * max) {
            normalize();
            mult(max);
        }
        return this;
    }

    /**
     * Set the magnitude of this vector.
     */
    public Vec3 setMag(float mag) {
        normalize();
        mult(mag);
        return this;
    }

    /**
     * Calculate the dot product with another vector.
     */
    public float dot(Vec3 v) {
        return x * v.x + y * v.y + z * v.z;
    }

    /**
     * Calculate the cross product with another vector.
     */
    public Vec3 cross(Vec3 v) {
        float crossX = y * v.z - z * v.y;
        float crossY = z * v.x - x * v.z;
        float crossZ = x * v.y - y * v.x;
        return new Vec3(crossX, crossY, crossZ);
    }

    /**
     * Calculate the distance to another vector.
     */
    public float dist(Vec3 v) {
        float dx = x - v.x;
        float dy = y - v.y;
        float dz = z - v.z;
        return (float) Math.sqrt(dx * dx + dy * dy + dz * dz);
    }

    /**
     * Calculate the angle between this vector and another (in radians).
     */
    public float angleBetween(Vec3 v) {
        float dot = dot(v);
        float m1 = mag();
        float m2 = v.mag();
        if (m1 == 0 || m2 == 0) return 0;
        return (float) Math.acos(dot / (m1 * m2));
    }

    /**
     * Linearly interpolate towards another vector.
     */
    public Vec3 lerp(Vec3 v, float amt) {
        this.x = x + (v.x - x) * amt;
        this.y = y + (v.y - y) * amt;
        this.z = z + (v.z - z) * amt;
        return this;
    }

    /**
     * Rotate this vector around the X axis.
     */
    public Vec3 rotateX(float angle) {
        float cos = (float) Math.cos(angle);
        float sin = (float) Math.sin(angle);
        float newY = y * cos - z * sin;
        float newZ = y * sin + z * cos;
        this.y = newY;
        this.z = newZ;
        return this;
    }

    /**
     * Rotate this vector around the Y axis.
     */
    public Vec3 rotateY(float angle) {
        float cos = (float) Math.cos(angle);
        float sin = (float) Math.sin(angle);
        float newX = x * cos + z * sin;
        float newZ = -x * sin + z * cos;
        this.x = newX;
        this.z = newZ;
        return this;
    }

    /**
     * Rotate this vector around the Z axis.
     */
    public Vec3 rotateZ(float angle) {
        float cos = (float) Math.cos(angle);
        float sin = (float) Math.sin(angle);
        float newX = x * cos - y * sin;
        float newY = x * sin + y * cos;
        this.x = newX;
        this.y = newY;
        return this;
    }

    /**
     * Get the heading angle (2D angle from origin).
     */
    public float heading() {
        return (float) Math.atan2(y, x);
    }

    /**
     * Convert to float array.
     */
    public float[] array() {
        return new float[]{x, y, z};
    }

    @Override
    public String toString() {
        return "[ " + x + ", " + y + ", " + z + " ]";
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Vec3)) return false;
        Vec3 v = (Vec3) obj;
        return x == v.x && y == v.y && z == v.z;
    }

    @Override
    public int hashCode() {
        int result = Float.floatToIntBits(x);
        result = 31 * result + Float.floatToIntBits(y);
        result = 31 * result + Float.floatToIntBits(z);
        return result;
    }

    // ============ Static factory methods ============

    /**
     * Create a vector from an angle (2D, in radians).
     */
    public static Vec3 fromAngle(float angle) {
        return new Vec3((float) Math.cos(angle), (float) Math.sin(angle), 0);
    }

    /**
     * Create a random 2D unit vector.
     */
    public static Vec3 random2D() {
        float angle = (float) (Math.random() * Math.PI * 2);
        return fromAngle(angle);
    }

    /**
     * Create a random 3D unit vector.
     */
    public static Vec3 random3D() {
        float angle = (float) (Math.random() * Math.PI * 2);
        float vz = (float) (Math.random() * 2 - 1);
        float vzSq = vz * vz;
        float vx = (float) (Math.sqrt(1 - vzSq) * Math.cos(angle));
        float vy = (float) (Math.sqrt(1 - vzSq) * Math.sin(angle));
        return new Vec3(vx, vy, vz);
    }

    /**
     * Add two vectors and return the result.
     */
    public static Vec3 add(Vec3 a, Vec3 b) {
        return new Vec3(a.x + b.x, a.y + b.y, a.z + b.z);
    }

    /**
     * Subtract two vectors and return the result.
     */
    public static Vec3 sub(Vec3 a, Vec3 b) {
        return new Vec3(a.x - b.x, a.y - b.y, a.z - b.z);
    }

    /**
     * Multiply a vector by a scalar and return the result.
     */
    public static Vec3 mult(Vec3 v, float scalar) {
        return new Vec3(v.x * scalar, v.y * scalar, v.z * scalar);
    }

    /**
     * Divide a vector by a scalar and return the result.
     */
    public static Vec3 div(Vec3 v, float scalar) {
        if (scalar == 0) return new Vec3(v.x, v.y, v.z);
        return new Vec3(v.x / scalar, v.y / scalar, v.z / scalar);
    }

    /**
     * Calculate dot product of two vectors.
     */
    public static float dot(Vec3 a, Vec3 b) {
        return a.x * b.x + a.y * b.y + a.z * b.z;
    }

    /**
     * Calculate cross product of two vectors.
     */
    public static Vec3 cross(Vec3 a, Vec3 b) {
        return a.cross(b);
    }

    /**
     * Calculate distance between two vectors.
     */
    public static float dist(Vec3 a, Vec3 b) {
        return a.dist(b);
    }

    /**
     * Linearly interpolate between two vectors.
     */
    public static Vec3 lerp(Vec3 a, Vec3 b, float amt) {
        return new Vec3(
            a.x + (b.x - a.x) * amt,
            a.y + (b.y - a.y) * amt,
            a.z + (b.z - a.z) * amt
        );
    }

    /**
     * Calculate angle between two vectors.
     */
    public static float angleBetween(Vec3 a, Vec3 b) {
        return a.angleBetween(b);
    }
}
