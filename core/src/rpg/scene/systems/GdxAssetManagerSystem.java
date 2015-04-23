package rpg.scene.systems;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.assets.loaders.resolvers.InternalFileHandleResolver;

public final class GdxAssetManagerSystem extends AbstractSceneSystem {

    private static GdxAssetManagerSystem singleton;

    public static GdxAssetManagerSystem getSingleton() {
        if (singleton == null) {
            singleton = new GdxAssetManagerSystem();
        }
        return singleton;
    }

    private AssetManager assetManager;
    private boolean loading = false;

    private GdxAssetManagerSystem() {
        assetManager = new AssetManager(new InternalFileHandleResolver());
    }

    public AssetManager getAssetManager() {
        return assetManager;
    }

    @Override
    public void beginProcessing() {
        loading = !assetManager.update(10); // more than enough time in 60fps
    }

    @Override
    public boolean doesProcessNodes() {
        return false;
    }

    public boolean isLoading() {
        return loading;
    }
}
