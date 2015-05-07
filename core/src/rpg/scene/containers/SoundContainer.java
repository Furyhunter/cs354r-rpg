package rpg.scene.containers;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.audio.Sound;
import rpg.scene.systems.GdxAssetManagerSystem;

public class SoundContainer extends AssetContainer<Sound> {
    @Override
    protected void loadAsset() {
        AssetManager am = GdxAssetManagerSystem.getSingleton().getAssetManager();
        am.load(path, Sound.class);
        am.finishLoadingAsset(path);
        ref = am.get(path, Sound.class);
    }
}
