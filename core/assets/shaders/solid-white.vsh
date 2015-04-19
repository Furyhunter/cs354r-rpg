attribute vec3 a_position;

uniform mat4 u_pvmMatrix;

void main() {
    gl_Position = u_pvmMatrix * vec4(a_position, 1);
}
