package rpg.scene.containers;

import com.badlogic.gdx.assets.AssetManager;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.KryoSerializable;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import rpg.scene.systems.GdxAssetManagerSystem;

public abstract class AssetContainer<T> implements KryoSerializable {

    protected String path;

    protected T ref;

    public T getAsset() {
        if (ref != null) {
            return ref;
        }
        loadAsset();
        return ref;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
        AssetManager am = GdxAssetManagerSystem.getSingleton().getAssetManager();
        if (ref != null && am.containsAsset(ref)) {
            am.unload(am.getAssetFileName(ref));
        }
        ref = null;
    }

    protected abstract void loadAsset();

    @Override
    public void write(Kryo kryo, Output output) {
        output.writeString(path);
    }

    @Override
    public void read(Kryo kryo, Input input) {
        path = input.readString();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof AssetContainer)) {
            return false;
        }
        AssetContainer<?> t = (AssetContainer<?>) obj;
        if (!path.equals(t.path)) {
            return false;
        }
        return true;
    }
}
