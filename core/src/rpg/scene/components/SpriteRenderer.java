package rpg.scene.components;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector2;
import rpg.scene.RenderItem;

public class SpriteRenderer extends Component implements Renderable {

    private Texture texture;
    private static ShaderProgram shader = null;
    private static Mesh mesh = null;

    private Vector2 dimensions = new Vector2(1, 1);
    private Vector2 offset = new Vector2();

    @Override
    public RenderItem render() {
        if (shader == null) {
            shader = new ShaderProgram(Gdx.files.internal("shaders/default-sprite.vsh"), Gdx.files.internal("shaders/default-sprite.fsh"));
            String log = shader.getLog();
            if (log != null && log.length() > 0) {
                System.err.println(log);
            }
        }
        if (mesh == null) {
            mesh = new Mesh(Mesh.VertexDataType.VertexBufferObject, true, 4, 0,
                    new VertexAttribute(VertexAttributes.Usage.Position, 3, ShaderProgram.POSITION_ATTRIBUTE),
                    new VertexAttribute(VertexAttributes.Usage.TextureCoordinates, 2, ShaderProgram.TEXCOORD_ATTRIBUTE + "0"));
            mesh.setVertices(new float[]{
                    -.5f, -.5f, 0, 0, 1,
                    .5f, -.5f, 0, 1, 1,
                    .5f, .5f, 0, 1, 0,
                    -.5f, .5f, 0, 0, 0,
            });
        }

        Matrix4 model = new Matrix4().mulLeft(new Matrix4().setToTranslation(offset.x, offset.y, 0)).mulLeft(new Matrix4().setToScaling(dimensions.x, dimensions.y, 1));
        return new RenderItem(shader, texture, mesh, model, GL20.GL_TRIANGLE_FAN);
    }

    public Texture getTexture() {
        return texture;
    }

    public void setTexture(Texture texture) {
        this.texture = texture;
    }

    public Vector2 getDimensions() {
        return dimensions;
    }

    public void setDimensions(Vector2 dimensions) {
        this.dimensions = new Vector2(dimensions);
    }

    public Vector2 getOffset() {
        return offset;
    }

    public void setOffset(Vector2 offset) {
        this.offset = new Vector2(offset);
    }
}
