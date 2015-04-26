attribute vec3 a_position;

uniform mat4 u_pvmMatrix;

varying float v_index;

void main() {
    gl_Position = u_pvmMatrix * vec4(a_position.xy, 0, 1);
    v_index = a_position.z;
}
