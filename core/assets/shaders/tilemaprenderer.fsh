uniform mat4 u_pvmMatrix;

uniform float u_time;

varying float v_index;

void main() {
    gl_FragColor = vec4(v_index, v_index, v_index, 1);
}
