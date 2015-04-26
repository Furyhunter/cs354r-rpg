package rpg.scene.components;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.VertexAttribute;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.MathUtils;
import rpg.scene.RenderItem;
import rpg.scene.replication.Replicated;

public class TilemapRendererComponent extends Component implements Renderable {
    @Replicated
    protected int width;

    @Replicated
    protected int height;

    @Replicated
    protected byte[] values;

    private Mesh mesh;

    private boolean reconstructMesh = true;

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
            float[] verts = new float[(width + 1) * (height + 1) * 3];
            for (int iy = 0; iy <= height; iy++) {
                for (int ix = 0; ix <= width; ix++) {
                    verts[((iy * (width + 1)) + ix) * 3] = (float) (ix) - ((float) width / 2);
                    verts[((iy * (width + 1)) + ix) * 3 + 1] = (float) (iy) - ((float) height / 2);
                    verts[((iy * (width + 1)) + ix) * 3 + 2] = (float) ((int) values[(iy * width) + ix] & 0xFF) / 255.f;
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

            mesh = new Mesh(true, (width + 1) * (height + 1), getSize() * 6, VertexAttribute.Position());
            mesh.setVertices(verts);
            mesh.setIndices(indices);

            reconstructMesh = false;
        }
        if (shader == null) {
            shader = new ShaderProgram(Gdx.files.internal("shaders/tilemaprenderer.vsh"), Gdx.files.internal("shaders/tilemaprenderer.fsh"));
        }
        RenderItem item = new RenderItem();
        item.setMesh(mesh);
        item.setShader(shader);
        item.setPrimitiveType(GL20.GL_TRIANGLES);
        return item;
    }

    public void setPointValue(int x, int y, byte value) {
        if (x < 0 || x > width) {
            throw new IllegalArgumentException("x index out of bounds");
        }
        if (y < 0 || y > height) {
            throw new IllegalArgumentException("y index out of bounds");
        }
        values[(y * width) + x] = value;
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
}
