package com.twisted;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.twisted.logic.entities.Entity;
import com.twisted.logic.entities.Faction;

import java.util.HashMap;

/**
 * Handles asset access. Asset access through this class should be static.
 */
public class Asset {

    /* Storage */

    //main
    private static final HashMap<TextureAsset, TextureRegionDrawable> textureAssets = new HashMap<>();
    private static final HashMap<FontAsset, BitmapFont> fontAssets = new HashMap<>();

    //entities and factions
    private static final HashMap<Entity.Model, TextureRegionDrawable> entityIconAssets = new HashMap<>();
    private static final HashMap<Entity.Tier, TextureRegionDrawable> entityTierIconAssets = new HashMap<>();
    private static final HashMap<Entity.Model, String> entityBlurbAssets = new HashMap<>();
    private static final HashMap<Faction, TextureRegionDrawable> factionIconAssets = new HashMap<>();

    //empty
    public static final TextureRegionDrawable permaAsset = retrieve(Pixel.WHITE);


    /* Accessing */

    //basic retrieving
    public static TextureRegionDrawable retrieve(TextureAsset key){
        TextureRegionDrawable asset = textureAssets.get(key);
        if(asset == null){
            asset = new TextureRegionDrawable(new Texture(Gdx.files.internal(key.getPath())));
            textureAssets.put(key, asset);
        }
        return asset;
    }
    public static BitmapFont retrieve(FontAsset key){
        BitmapFont asset = fontAssets.get(key);
        if(asset == null){
            asset = new BitmapFont(Gdx.files.internal(key.getPath()));
            fontAssets.put(key, asset);
        }
        return asset;
    }

    //retrieving entity and faction specific
    public static TextureRegionDrawable retrieveEntityIcon(Entity.Model model){
        TextureRegionDrawable asset = entityIconAssets.get(model);
        if(asset == null){
            asset = new TextureRegionDrawable(new Texture(Gdx.files.internal("images/entities/"
                    + model.getFilename() + "-icon.png")));
            entityIconAssets.put(model, asset);
        }
        return asset;
    }
    public static TextureRegionDrawable retrieveEntityIcon(Entity.Tier tier){
        TextureRegionDrawable asset = entityTierIconAssets.get(tier);
        if(asset == null){
            asset = new TextureRegionDrawable(new Texture(Gdx.files.internal("images/entities/"
                    + tier.getFilename() + "-icon.png")));
            entityTierIconAssets.put(tier, asset);
        }
        return asset;
    }
    public static String retrieveEntityBlurb(Entity.Model model){
        String asset = entityBlurbAssets.get(model);
        if(asset == null){
            asset = Gdx.files.internal("text/blurbs/" + model.getFilename() + ".txt").readString();
            entityBlurbAssets.put(model, asset);
        }
        return asset;
    }
    public static TextureRegionDrawable retrieveFactionIcon(Faction faction){
        TextureRegionDrawable asset = factionIconAssets.get(faction);
        if(asset == null){
            asset = new TextureRegionDrawable(new Texture(Gdx.files.internal("images/factions/"
                    + faction.getFilename() + "-icon.png")));
            factionIconAssets.put(faction, asset);
        }
        return asset;
    }

    //remove
    public static void clear(){
        textureAssets.clear();
    }


    /* Utility */

    public static Label.LabelStyle labelStyle(Avenir font){
        return new Label.LabelStyle(Asset.retrieve(font), Color.WHITE);
    }


    /* Asset Enums */

    public interface BaseAsset {
        String getPath();
    }

    public interface TextureAsset extends BaseAsset {}
    public enum Gem implements TextureAsset {
        CALCITE("calcite"),
        CRYSTAL("crystal"),
        KERNITE("kernite"),
        PYRENE("pyrene");

        private final String path;
        public String getPath(){
            return path;
        }

        Gem(String string){
            this.path = "images/gems/" + string + ".png";
        }

    }

    public enum Pixel implements TextureAsset {
        BLACK("black"),
        BLUE("blue"),
        DARKGRAY("darkgray"),
        DARKPURLE("darkpurple"),
        GRAY("gray"),
        GREEN("green"),
        LIGHTGRAY("lightgray"),
        MAGENTA("magenta"),
        MENU_A1("menu-a1"),
        MENU_A2("menu-a2"),
        NAVY("navy"),
        SPACE("space"),
        WHITE("white");

        private final String path;
        public String getPath(){
            return path;
        }

        Pixel(String string){
            this.path = "images/pixels/" + string + ".png";
        }
    }
    public enum Circle implements TextureAsset {
        CIRCLE_BLACK("black"),
        CIRCLE_BLUE("blue"),
        CIRCLE_GRAY("gray"),
        CIRCLE_ORANGE("orange");

        private final String path;
        public String getPath(){
            return path;
        }

        Circle(String string){
            this.path = "images/circles/" + string + ".png";
        }
    }
    public enum UiBasic implements TextureAsset {
        CURSOR_1("cursor1"),
        ARROW_1("arrow-1"),
        ARROW_2("arrow-2"),
        ARROW_3("arrow-3"),
        WHITE_ARROW("white-arrow"),
        WHITE_SQUARE_1("white-square-1");

        private final String path;
        public String getPath(){
            return path;
        }

        UiBasic(String string){
            this.path = "images/ui/" + string + ".png";
        }
    }
    public enum UiIcon implements TextureAsset {
        DOCK("dock"),
        UNDOCK("undock"),
        STATION_ARMORED("station-armored"),
        STATION_DEPLOYMENT("station-deployment"),
        STATION_SHIELDED("station-shielded"),
        STATION_VULNERABLE("station-vulnerable");

        private final String path;
        public String getPath(){
            return path;
        }

        UiIcon(String string){
            this.path = "images/ui/icons/" + string + ".png";
        }
    }
    public enum UiButton implements TextureAsset {
        ALIGN("align"),
        BLASTER_OFF("blaster-off"),
        BLASTER_ON("blaster-on"),
        DEFAULT("default"),
        DOCK("dock"),
        UNDOCK("undock"),
        EXTRACTOR_OFF("extractor-off"),
        EXTRACTOR_ON("extractor-on"),
        HARVESTER_ON("harvester-off"),
        HARVESTER_OFF("harvester-on"),
        LASER_OFF("laser-off"),
        LASER_ON("laser-on"),
        LIQUIDATOR_OFF("liquidator-off"),
        LIQUIDATOR_ON("liquidator-on"),
        MOVE("move"),
        ORBIT("orbit"),
        STOP("stop"),
        TARGET_OFF("target-off"),
        TARGET_ON("target-on"),
        TRANSFER_LEFT("transfer-left"),
        TRANSFER_NONE("transfer-none"),
        TRANSFER_RIGHT("transfer-right"),
        WARP("warp");

        private final String path;
        public String getPath(){
            return path;
        }

        UiButton(String string){
            this.path = "images/ui/buttons/" + string + ".png";
        }
    }
    public enum Misc implements TextureAsset {
        STATION_BASE("entities/station-icon");

        private final String path;
        public String getPath(){
            return path;
        }

        Misc(String string){
            this.path = "images/" + string + ".png";
        }
    }

    public interface FontAsset extends BaseAsset{}
    public enum Avenir implements FontAsset {
        LIGHT_12("light_12"),
        LIGHT_16("light_16"),

        MEDIUM_12("medium_12"),
        MEDIUM_12_ITALIC("medium_12_italic"),
        MEDIUM_14("medium_14"),
        MEDIUM_16("medium_16"),

        HEAVY_16("heavy_16"),
        HEAVY_20("heavy_20"),

        BLACK_24("black_24"),
        BLACK_32("black_32"),
        BLACK_48("black_48");

        private final String path;
        public String getPath(){
            return path;
        }

        Avenir(String string){
            this.path = "fonts/avenir/" + string + ".fnt";
        }
    }


}
