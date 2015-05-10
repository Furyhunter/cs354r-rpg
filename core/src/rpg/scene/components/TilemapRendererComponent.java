package rpg.scene.components;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.VertexAttribute;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Matrix3;
import com.badlogic.gdx.math.Rectangle;
import rpg.scene.RenderItem;
import rpg.scene.replication.Replicated;
import rpg.scene.systems.GdxAssetManagerSystem;

public class TilemapRendererComponent extends Component implements Renderable, Spatial2D {
    @Replicated
    protected int width;

    @Replicated
    protected int height;

    @Replicated
    protected byte[] values;

    private Mesh mesh;

    private boolean reconstructMesh = true;

    private static Texture texture;
    private static ShaderProgram shader;

    public TilemapRendererComponent() {

    }

    public TilemapRendererComponent(int width, int height) {
        this.width = width;
        this.height = height;
        values = new byte[(this.width + 1) * (this.height + 1)];
        reconstructMesh = true;
    }

    @Override
    public RenderItem render() {
        if (reconstructMesh) {
            if (mesh != null) {
                mesh.dispose();
            }
            float[] verts = new float[(width + 1) * (height + 1) * 5];
            for (int iy = 0; iy <= height; iy++) {
                for (int ix = 0; ix <= width; ix++) {
                    verts[((iy * (width + 1)) + ix) * 5] = (float) (ix) - ((float) width / 2);
                    verts[((iy * (width + 1)) + ix) * 5 + 1] = (float) (iy) - ((float) height / 2);
                    verts[((iy * (width + 1)) + ix) * 5 + 2] = (float) ((int) values[(iy * (width + 1)) + ix] & 0xFF) / 255.f;
                    verts[((iy * (width + 1)) + ix) * 5 + 3] = (float) ix;
                    verts[((iy * (width + 1)) + ix) * 5 + 4] = (float) iy;
                }
            }
            short[] indices = new short[getSize() * 6];
            for (int iy = 0; iy < height; iy++) {
                for (int ix = 0; ix < width; ix++) {
                    indices[(((iy * width) + ix) * 6)] = (short) (((iy) * (width + 1)) + (ix));
                    indices[(((iy * width) + ix) * 6) + 1] = (short) (((iy) * (width + 1)) + (ix + 1));
                    indices[(((iy * width) + ix) * 6) + 2] = (short) (((iy + 1) * (width + 1)) + (ix + 1));
                    indices[(((iy * width) + ix) * 6) + 3] = (short) (((iy) * (width + 1)) + (ix));
                    indices[(((iy * width) + ix) * 6) + 4] = (short) (((iy + 1) * (width + 1)) + (ix + 1));
                    indices[(((iy * width) + ix) * 6) + 5] = (short) (((iy + 1) * (width + 1)) + (ix));
                }
            }

            mesh = new Mesh(true, (width + 1) * (height + 1), getSize() * 6, VertexAttribute.Position(), VertexAttribute.TexCoords(0));
            mesh.setVertices(verts);
            mesh.setIndices(indices);

            reconstructMesh = false;
        }
        if (shader == null) {
            shader = new ShaderProgram(Gdx.files.internal("shaders/tilemaprenderer.vsh"), Gdx.files.internal("shaders/tilemaprenderer.fsh"));
            System.out.println(shader.getLog());
        }
        if (texture == null) {
            GdxAssetManagerSystem.getSingleton().getAssetManager().load("sprites/terrain-noise.png", Texture.class);
            GdxAssetManagerSystem.getSingleton().getAssetManager().finishLoadingAsset("sprites/terrain-noise.png");
            texture = GdxAssetManagerSystem.getSingleton().getAssetManager().get("sprites/terrain-noise.png", Texture.class);
            texture.setWrap(Texture.TextureWrap.Repeat, Texture.TextureWrap.Repeat);
        }
        RenderItem item = new RenderItem();
        item.setMesh(mesh);
        item.setShader(shader);
        item.setPrimitiveType(GL20.GL_TRIANGLES);
        item.setTextureToBind(0, texture);
        item.setUniformSetFunction(this::setShaderUniforms);
        return item;
    }

    public void setPointValue(int x, int y, byte value) {
        if (x < 0 || x > width) {
            throw new IllegalArgumentException("x index out of bounds");
        }
        if (y < 0 || y > height) {
            throw new IllegalArgumentException("y index out of bounds");
        }
        values[(y * (width + 1)) + x] = value;
        reconstructMesh = true;
    }

    public void setPointValue(int x, int y, float value) {
        if (value < 0 || value > 1) {
            throw new IllegalArgumentException("value < 0 || value > 1");
        }
        setPointValue(x, y, (byte) MathUtils.round(value * 255));
    }

    public int getSize() {
        return width * height;
    }

    @Override
    public void onPostApplyReplicatedFields() {
        reconstructMesh = true;
    }

    @Override
    public Rectangle getRectangle() {
        return new Rectangle((float) -width / 2, (float) -height / 2, width, height);
    }

    private void setShaderUniforms(ShaderProgram p) {
        if (p.getUniformLocation("u_texCoord0Matrix") != -1) {
            p.setUniformMatrix("u_texCoord0Matrix", new Matrix3().scale(0.1f, 0.1f));
        }
    }
}
