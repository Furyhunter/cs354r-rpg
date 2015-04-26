uniform mat4 u_pvmMatrix;

uniform float u_time;

varying float v_index;

void main() {
    // Color ranges
    // 0.0 - 0.3 -- blue (water)
    // 0.3 - 0.4 -- yellow (sand)
    // 0.4 - 0.7 -- green (grass)
    // 0.7 - 0.8 -- brown (dirt)
    // 0.8 - 1.0 -- white (snow)

    vec3 color = vec3(0, 0, 0);

    if (v_index < 0.3) {
        float a = v_index * (1.0 / 0.3);
        color = mix(vec3(0, 0, 0.1), vec3(0.2, 0.2, 0.7), a);
    } else if (v_index < 0.4) {
        float a = (v_index - 0.3) * (1.0 / 0.1);
        color = mix(vec3(0.7, 0.7, 0), vec3(0.4, 0.4, 0), a);
    } else if (v_index < 0.7) {
        float a = (v_index - 0.4) * (1.0 / 0.3);
        color = mix(vec3(0, 0.5, 0), vec3(0.3, 0.7, 0), a);
    } else if (v_index < 0.8) {
        float a = (v_index - 0.7) * (1.0 / 0.1);
        color = mix(vec3(0.3, 0.3, 0), vec3(0.6, 0.6, 0.4), a);
    } else {
        float a = (v_index - 0.8) * (1.0 / 0.2);
        color = mix(vec3(0.6, 0.6, 0.6), vec3(0.9, 0.9, 0.9), a);
    }
    gl_FragColor = vec4(color, 1);
}
