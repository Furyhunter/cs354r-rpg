package rpg.scene.components;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.*;
import rpg.scene.RenderItem;
import rpg.scene.containers.TextureContainer;
import rpg.scene.replication.Replicated;

public class SpriteRenderer extends Component implements Renderable, Spatial2D {

    @Replicated
    protected TextureContainer texture;

    private static ShaderProgram shader = null;
    private static Mesh mesh = null;

    @Replicated
    protected Vector2 dimensions = new Vector2(1, 1);
    @Replicated
    protected Vector2 offset = new Vector2();

    @Replicated
    protected float rotation;

    @Replicated
    protected boolean billboard = true;

    public Vector2 texCoordTranslation = new Vector2();
    public Vector2 texCoordScale = new Vector2(1, 1);

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

        Matrix4 model = new Matrix4();
        model.scale(dimensions.x, dimensions.y, 1);
        if (!billboard) model.translate(offset.x, offset.y, 0);
        model.rotate(Vector3.Z, rotation);

        RenderItem renderItem = new RenderItem(shader, new Texture[]{texture.getAsset()}, mesh, model, GL20.GL_TRIANGLE_FAN);
        renderItem.setUniformSetFunction(this::setUniforms);
        renderItem.setTransparent(true);
        return renderItem;
    }

    public TextureContainer getTexture() {
        return texture;
    }

    public void setTexture(String path) {
        if (texture == null) {
            texture = new TextureContainer();
        }
        texture.setPath(path);
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

    public float getRotation() {
        return rotation;
    }

    public void setRotation(float rotation) {
        this.rotation = rotation;
    }

    public boolean isBillboard() {
        return billboard;
    }

    public void setBillboard(boolean billboard) {
        this.billboard = billboard;
    }

    @Override
    public Rectangle getRectangle() {
        return new Rectangle(offset.x, offset.y, dimensions.x, dimensions.y);
    }

    private void setUniforms(ShaderProgram p) {
        Matrix3 m = new Matrix3().translate(texCoordTranslation).scale(texCoordScale);
        p.setUniformMatrix("u_texCoord0Transform", m);
        p.setUniformf("u_billboard", billboard ? 1.0f : 0.0f);
        p.setUniformMatrix("u_spriteRotScale", new Matrix4().rotate(Vector3.Z, rotation).scale(dimensions.x, dimensions.y, 1));
        p.setUniformf("u_spriteOffset", offset);
    }
}
