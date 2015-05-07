package rpg.scene.containers;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.audio.Music;
import rpg.scene.systems.GdxAssetManagerSystem;

public class MusicContainer extends AssetContainer<Music> {
    @Override
    protected void loadAsset() {
        AssetManager am = GdxAssetManagerSystem.getSingleton().getAssetManager();
        am.load(path, Music.class);
        am.finishLoadingAsset(path);
        ref = am.get(path, Music.class);
    }
}
