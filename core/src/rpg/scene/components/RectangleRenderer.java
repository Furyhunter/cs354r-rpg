package rpg.scene.components;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector2;
import rpg.scene.RenderItem;
import rpg.scene.replication.Replicated;

public class RectangleRenderer extends Component implements Renderable {
    @Replicated
    protected Vector2 size = new Vector2(1, 1);

    @Replicated
    protected Color color = new Color(1, 1, 1, 1);

    @Replicated
    protected boolean transparent = false;

    private static Mesh mesh;
    private static ShaderProgram shader;

    @Override
    public RenderItem render() {
        if (mesh == null) {
            mesh = new Mesh(true, 4, 0,
                    new VertexAttribute(VertexAttributes.Usage.Position, 3, ShaderProgram.POSITION_ATTRIBUTE));
            mesh.setVertices(new float[]{
                    -.5f, -.5f, 0,
                    .5f, -.5f, 0,
                    .5f, .5f, 0,
                    -.5f, .5f, 0,
            });
        }
        if (shader == null) {
            shader = new ShaderProgram(Gdx.files.internal("shaders/solid-color.vsh"), Gdx.files.internal("shaders/solid-color.fsh"));
        }
        Matrix4 model = new Matrix4().mulLeft(new Matrix4().setToScaling(size.x, size.y, 1));

        RenderItem r = new RenderItem();
        r.setUniformSetFunction(this::setUniforms);
        r.setPrimitiveType(GL20.GL_TRIANGLE_FAN);
        r.setModelMatrix(model);
        r.setShader(shader);
        r.setMesh(mesh);
        r.setTransparent(transparent);
        return r;
    }

    private void setUniforms(ShaderProgram shader) {
        shader.setUniformf("u_color", color);
    }

    public Vector2 getSize() {
        return size.cpy();
    }

    public void setSize(Vector2 size) {
        this.size = size.cpy();
    }

    public boolean isTransparent() {
        return transparent;
    }

    public void setTransparent(boolean transparent) {
        this.transparent = transparent;
    }

    public Color getColor() {
        return color;
    }

    public void setColor(Color color) {
        this.color = color;
    }
}
