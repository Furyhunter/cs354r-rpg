varying vec2 v_texCoord0;

uniform mat4 u_pMatrix;
uniform mat4 u_vmMatrix;

uniform sampler2D u_texture1;

uniform mat3 u_texCoord0Transform;
uniform float u_billboard;
uniform mat4 u_spriteRotScale;

void main() {
    vec4 sample = texture2D(u_texture1, v_texCoord0);
    if (sample.a < 0.4) discard;
    gl_FragColor = sample;
    //gl_FragColor = vec4(1.0, 0.0, 0.0, 1.0);
}
