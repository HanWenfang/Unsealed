/**
 * Unsealed: Whispers of Wisdom. 
 * 
 * Copyright (C) 2012 - Juan 'Nushio' Rodriguez
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License version 3 of 
 * the License as published by the Free Software Foundation
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * 
 */
package net.k3rnel.unsealed.screens;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.Application.ApplicationType;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.NinePatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureAtlas.AtlasRegion;
import com.badlogic.gdx.graphics.g2d.tiled.SimpleTileAtlas;
import com.badlogic.gdx.graphics.g2d.tiled.TileAtlas;
import com.badlogic.gdx.graphics.g2d.tiled.TileMapRenderer;
import com.badlogic.gdx.graphics.g2d.tiled.TiledLoader;
import com.badlogic.gdx.graphics.g2d.tiled.TiledMap;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.NinePatchDrawable;
import com.badlogic.gdx.scenes.scene2d.utils.PressedListener;

import net.k3rnel.unsealed.Unsealed;
import net.k3rnel.unsealed.battle.BattleEntity;
import net.k3rnel.unsealed.battle.BattleGrid;
import net.k3rnel.unsealed.battle.BattleHUD;
import net.k3rnel.unsealed.battle.BattleHero;
import net.k3rnel.unsealed.battle.magic.MagicEntity;
import net.k3rnel.unsealed.battle.skills.EarthSpikes;
import net.k3rnel.unsealed.battle.skills.FireLion;
import net.k3rnel.unsealed.battle.skills.FirePunch;
import net.k3rnel.unsealed.battle.skills.ThunderClaw;
import net.k3rnel.unsealed.battle.skills.TornadoVacuum;
import net.k3rnel.unsealed.battle.skills.IceTentacle;
import net.k3rnel.unsealed.screens.AbstractScreen;
import net.k3rnel.unsealed.services.MusicManager.UnsealedMusic;
import net.k3rnel.unsealed.services.SoundManager.UnsealedSound;
import net.k3rnel.unsealed.story.MapCharacter;
import net.k3rnel.unsealed.story.StyledTable;
import net.k3rnel.unsealed.story.TextBox;

public class BattleScreen extends AbstractScreen {


    //    private Image background;
    private Image battleoverlay;

    TiledMap tileMap;
    TileAtlas tileAtlas;
    protected TileMapRenderer tileMapRenderer;
    public OrthographicCamera camera;

    public BattleGrid grid;
    public BattleHUD hud;

    public static BattleHero hero;

    public Label roundLabel;

    protected ImageButton restartButton;

    public static int round = 0;
    public static int bonus = 1;

    public boolean scriptedBattle;
    public int act;
    public float stateTime;

    public List<MapCharacter> characters;
    MapCharacter tmpChar;

    public TextBox dialog;
    StyledTable.TableStyle textBoxStyle;

    List<MagicEntity> magics = new ArrayList<MagicEntity>();

    String mapname;

    public boolean disableInput = false;

    public BattleScreen(Unsealed game, boolean scriptedBattle, String mapname) {
        super(game);
        if(scriptedBattle){
            this.scriptedBattle = true;
            act = 0;
        }
        this.mapname = mapname;
    }
    @Override
    protected boolean isGameScreen() {
        return false;
    }
    @Override
    public void show() {
        super.show();
        long time = new Date().getTime();
        Gdx.app.log(Unsealed.LOG,"Starting... ");
        if(!scriptedBattle)
            game.getMusicManager().play( UnsealedMusic.BATTLE );
        atlas = new TextureAtlas( Gdx.files.internal( "image-atlases/pages-info.atlas" ) );

        time = new Date().getTime() - time;
        Gdx.app.log(Unsealed.LOG,"Started atlas in... "+time);
        time = new Date().getTime();
        
        // Load the tmx file into map
        tileMap = TiledLoader.createMap(Gdx.files.internal("map-atlases/"+mapname+".tmx"));

        time = new Date().getTime() - time;
        Gdx.app.log(Unsealed.LOG,"Started tiledMap in... "+time);
        time = new Date().getTime();
        
        // Load the tiles into atlas
        tileAtlas = new TileAtlas(tileMap, Gdx.files.internal("map-atlases/"));

        time = new Date().getTime() - time;
        Gdx.app.log(Unsealed.LOG,"Started tileAtlas in... "+time);
        time = new Date().getTime();
        
        // Create the renderer
        tileMapRenderer = new TileMapRenderer(tileMap, tileAtlas, tileMap.width, tileMap.height);

        time = new Date().getTime() - time;
        Gdx.app.log(Unsealed.LOG,"Started tileMapRenderer in... "+time);
        time = new Date().getTime();
        
        // Create the camera
        camera = new OrthographicCamera(MENU_VIEWPORT_WIDTH,MENU_VIEWPORT_HEIGHT);
        camera.position.set(850,1265, 0);
        camera.zoom = 0.8f;
        camera.update();


        AtlasRegion atlasRegion = atlas.findRegion( "battle/ui/field-3x3" );
        battleoverlay = new Image(atlasRegion);
        battleoverlay.setScale(1.2f);
        battleoverlay.setX( MENU_VIEWPORT_WIDTH/2 - (battleoverlay.getWidth()*battleoverlay.getScaleX())/2 );
        battleoverlay.setY( MENU_VIEWPORT_HEIGHT/2 - (battleoverlay.getHeight()*battleoverlay.getScaleY())- 65);
        stage.addActor(battleoverlay);

        stateTime = 0;

        characters = new ArrayList<MapCharacter>();
        time = new Date().getTime() - time;
        Gdx.app.log(Unsealed.LOG,"Started camera & overlay in... "+time);
        time = new Date().getTime();
        hud = new BattleHUD(getAtlas(),this.stage.getWidth(), stage.getHeight());
        hud.xButton.addListener(new PressedListener() {
            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                super.touchDown(event, x, y, pointer, button);
                buttonPress(6,true); 
                return true;
            }
            @Override
            public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
                super.touchUp(event, x, y, pointer, button);
                buttonPress(6,false); 
            }
        });
        hud.bButton.addListener(new PressedListener() {
            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                super.touchDown(event, x, y, pointer, button);
                buttonPress(7,true); 
                return true;
            }
            @Override
            public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
                super.touchUp(event, x, y, pointer, button);
                buttonPress(7,false); 
            }
        });

        hud.aButton.addListener(new PressedListener() {
            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                super.touchDown(event, x, y, pointer, button);
                buttonPress(8,true); 
                return true;
            }
            @Override
            public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
                super.touchUp(event, x, y, pointer, button);
                buttonPress(8,false); 
            }
        });
        
        hud.yButton.addListener(new PressedListener() {
            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                super.touchDown(event, x, y, pointer, button);
                buttonPress(9,true); 
                return true;
            }
            @Override
            public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
                super.touchUp(event, x, y, pointer, button);
                buttonPress(9,false); 
            }
        });
        if(Gdx.app.getType()==ApplicationType.Android||Unsealed.DEBUG==true){
            hud.leftTrigger.addListener(new PressedListener() {
                @Override
                public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                    super.touchDown(event, x, y, pointer, button);
                    buttonPress(4,true); 
                    return true;
                }

                @Override
                public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
                    super.touchUp(event, x, y, pointer, button);
                    buttonPress(4,false); 
                }
            });
            hud.rightTrigger.addListener(new PressedListener() {
                @Override
                public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                    super.touchDown(event, x, y, pointer, button);
                    buttonPress(5,true); 
                    return true;
                }
                @Override
                public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
                    super.touchUp(event, x, y, pointer, button);
                    buttonPress(5,false); 
                }
            });


        }
        time = new Date().getTime() - time;
        Gdx.app.log(Unsealed.LOG,"Started HUD in... "+time);
        time = new Date().getTime();
        grid = new BattleGrid(getAtlas(), this.stage.getWidth(), stage.getHeight(),6,3);

        time = new Date().getTime() - time;
        Gdx.app.log(Unsealed.LOG,"Started GRID in... "+time);
        time = new Date().getTime();
        hero = new BattleHero(getAtlas(),150);
        hero.setGrid(1,1);
        hero.setSkill1(new EarthSpikes(getAtlas()));
        hud.xButton.addActor(hero.getSkill1());

        hero.setSkill2(new TornadoVacuum(getAtlas()));
        hud.bButton.addActor(hero.getSkill2());

        hero.setSkill3(new FireLion(getAtlas()));
        hud.aButton.addActor(hero.getSkill3());
        grid.assignEntity(hero);     

        hero.setSkill4(new ThunderClaw(getAtlas()));
        hero.setSkill5(new IceTentacle(getAtlas()));
        hero.setSkill6(new FirePunch(getAtlas()));
        time = new Date().getTime() - time;
        Gdx.app.log(Unsealed.LOG,"Started hero in... "+time);
        time = new Date().getTime();
        if(!scriptedBattle)
            grid.spawnEnemies(bonus);
        time = new Date().getTime() - time;
        Gdx.app.log(Unsealed.LOG,"Started spawn enemies in... "+time);
        time = new Date().getTime();
        roundLabel = new Label("Round "+round,getSkin());
        roundLabel.setX(350);
        roundLabel.setY(350);
//        this.stage.addActor(roundLabel);

        atlasRegion = atlas.findRegion( "battle/ui/continue" );
        restartButton = new ImageButton(new Image(atlasRegion).getDrawable(),new Image(atlasRegion).getDrawable());
        restartButton.setY(140);
        restartButton.setX(170);
        restartButton.setWidth(426);
        restartButton.setHeight(165);
        restartButton.setVisible(false);
        restartButton.setDisabled(true);
        restartButton.addListener(new ClickListener() {

            @Override
            public void clicked(InputEvent arg0, float arg1, float arg2) {
                hero = new BattleHero(getAtlas(),150);
                hero.setGrid(1,1);
                hero.reset();
                hero.setSkill1(new EarthSpikes(getAtlas()));
                hero.setSkill2(new TornadoVacuum(getAtlas()));
                hero.setSkill3(new FireLion(getAtlas()));
                grid.reset();
                grid.assignEntity(hero);     
                bonus = 1;
                round = 1;

                restartButton.setVisible(false);
                restartButton.setDisabled(true);
                grid.spawnEnemies(bonus);

            }
        });
        this.stage.addActor(restartButton);
        
        NinePatch patch = getAtlas().createPatch("maps/dialog-box");

        textBoxStyle = new StyledTable.TableStyle();
        textBoxStyle.background = new NinePatchDrawable(patch);
        textBoxStyle.font = new BitmapFont();
        textBoxStyle.padX = 8;
        textBoxStyle.padY = 4;


        dialog = new TextBox("", textBoxStyle);
        dialog.setWidth(Gdx.graphics.getWidth());
        dialog.setHeight(Gdx.graphics.getHeight() / 8);
        dialog.setVisible(false);
        time = new Date().getTime() - time;
        Gdx.app.log(Unsealed.LOG,"Started the rest in... "+time);
        time = new Date().getTime();
        hud.addActor(dialog);
        Gdx.input.setInputProcessor(new InputMultiplexer(this,stage,hud));
    } 

    public void checkScene(float delta){

    }

    @Override
    public void render(float delta) {
        // (1) process the game logic
        // update the actors
        stage.act( delta );
        
        // (2) draw the result
         
        
        if(scriptedBattle)
            checkScene(delta);

        // clear the screen with the given RGB color (black)
        Gdx.gl.glClearColor( 0f, 0f, 0f, 1f );
        Gdx.gl.glClear( GL20.GL_COLOR_BUFFER_BIT );

        // draw the actors
        tileMapRenderer.render(camera);

        stage.draw();


        if(!scriptedBattle)
            if(BattleGrid.checkState() == BattleGrid.battleWon){
                bonus++;
                hero.reset();
                grid.reset();
                grid.assignEntity(hero);
                grid.spawnEnemies(bonus);
            }else if(BattleGrid.checkState() == BattleGrid.battleLost){
                roundLabel.setX(330);
                roundLabel.setText("You Lost at Round "+round+"!");
                restartButton.setVisible(true);
                restartButton.setDisabled(false);

            }

        grid.act(delta);
        hud.act(delta);
        hud.fillMana(hero);

        
        
        grid.draw();
        stage.getSpriteBatch().begin();
        for(MapCharacter character : characters){
            if(character.isVisible()){
                character.act(delta);
                character.draw(stage.getSpriteBatch(), 1);
            }
        }
        
        hud.draw();
        
        stage.getSpriteBatch().end();
        if(restartButton.isVisible()){
            this.getBatch().begin();
            restartButton.draw(this.getBatch(), 1);
            this.getBatch().end();
        }
        
        
    }

    @Override
    public boolean keyDown(int keycode) {
        if(!disableInput)
            switch(keycode) {
                case Input.Keys.RIGHT:
                    buttonPress(3,true);
                    return true;
                case Input.Keys.D:
                    buttonPress(3,true);
                    return true;
                case Input.Keys.LEFT:
                    buttonPress(2,true);
                    return true;
                case Input.Keys.A:
                    buttonPress(2,true);
                    return true;
                case Input.Keys.UP:
                    buttonPress(0,true);
                    return true;
                case Input.Keys.W:
                    buttonPress(0,true);
                    return true;
                case Input.Keys.DOWN:
                    buttonPress(1,true);
                    return true;
                case Input.Keys.S:
                    buttonPress(1,true);
                    return true;
                case Input.Keys.U:
                    buttonPress(4,true);
                    return true;
                case Input.Keys.O:
                    buttonPress(5,true);
                    return true;
                case Input.Keys.J:
                    buttonPress(6,true);
                    return true;
                case Input.Keys.K:
                    buttonPress(7,true);
                    return true;
                case Input.Keys.L:
                    buttonPress(8,true);
                    return true;
                case Input.Keys.I:
                    buttonPress(9,true);
                    return true;
            }
        return false;
    }
    @Override
    public boolean keyUp(int keycode) {
        if(!disableInput)
            switch(keycode) {
                case Input.Keys.U:
                    buttonPress(4,false);
                    return true;
                case Input.Keys.O:
                    buttonPress(5,false);
                    return true;
                case Input.Keys.J:
                    buttonPress(6,false);
                    return true;
                case Input.Keys.K:
                    buttonPress(7,false);
                    return true;
                case Input.Keys.L:
                    buttonPress(8,false);
                    return true;
                case Input.Keys.I:
                    buttonPress(9,false);
                    return true;
                case Input.Keys.BACK:
                    game.setScreen(new MenuScreen(game));
                    return true;
                case Input.Keys.ESCAPE:
                    game.setScreen(new MenuScreen(game));
                    return true;
            }
        return false;
    }

    /**
     * 0 = up, 1 = down, 2 = left, 3 = right
     * 4 = shield, 5 = attack
     * 6 = Skill1. 7 = Skill2. 8 = Skill3.
     * 9 = next-skill-page
     * @param direction
     */
    public void buttonPress(int button,boolean pressed){
        if(!disableInput)
            switch(button){
                //TODO: Hero moves at a fixed pixel rate (A.k.a. magic numbers). It should instead move based on screen width.
                case 0:{ // Up
                    if(hero.getState()!=BattleEntity.stateBlocking){
                        if((hero.getGridYInt()-1>-1)){
                            BattleGrid.moveEntity(hero, hero.getGridXInt(), hero.getGridYInt()-1);

                        }
                    }
                    break;
                }
                case 1:{ // Down
                    if(hero.getState()!=BattleEntity.stateBlocking){
                        if((hero.getGridYInt()+1<3)){
                            BattleGrid.moveEntity(hero, hero.getGridXInt(), hero.getGridYInt()+1);

                        }
                    }
                    break;
                }
                case 2:{ // Left
                    if(hero.getState()!=BattleEntity.stateBlocking){
                        if((hero.getGridXInt()-1>-1)){
                            BattleGrid.moveEntity(hero, hero.getGridXInt()-1, hero.getGridYInt());

                        }
                    }
                    break;
                }
                case 3:{ // Right
                    if(hero.getState()!=BattleEntity.stateBlocking){
                        if((hero.getGridXInt()+1<3)){
                            BattleGrid.moveEntity(hero, hero.getGridXInt()+1, hero.getGridYInt());
                        }
                    }
                    break;
                }
                case 4:{ // Shield
                    if(pressed){
                        if(hero.getState()!=BattleEntity.stateBlocking){
                            hero.setState(BattleEntity.stateBlocking);

                            hero.setMana(hero.getMana()-1);
                        }
                    }else{
                        hero.setState(BattleEntity.stateIdle);
                    }
                    break;
                }
                case 5:{ // Attack
                    if(pressed){

                    }else{
                        if(hero.getState()==BattleEntity.stateIdle){
                            if(hero.getMana()>=1){
                                game.getSoundManager().play(UnsealedSound.CLICK);
                                hero.magicType=0;
                                hero.setMana(hero.getMana()-1);
                                hero.setState(BattleEntity.stateAttacking);
                            }
                        }
                    }
                    break;
                }
                case 6:{ // Skill1
                    if(pressed){
                        if(hero.getState()==BattleEntity.stateIdle){
                            if(hero.getSkill1()!=null)
                                if(hero.getMana()>=hero.getSkill1().manaCost){
                                    game.getSoundManager().play(UnsealedSound.CLICK);
                                    hero.setMana(hero.getMana()-hero.getSkill1().manaCost);
                                    hero.setState(hero.getSkill1().stance);
                                    hero.magicType = hero.getSkill1().id;
                                }
                        }
                    }else{

                    }
                    break;
                }
                case 7:{ // Skill2
                    if(pressed){
                        if(hero.getState()==BattleEntity.stateIdle){
                            if(hero.getSkill2()!=null)
                                if(hero.getMana()>=hero.getSkill2().manaCost){
                                    game.getSoundManager().play(UnsealedSound.CLICK);
                                    hero.setMana(hero.getMana()-hero.getSkill2().manaCost);
                                    hero.magicType = hero.getSkill2().id;
                                    hero.setState(hero.getSkill2().stance);
                                }
                        }
                    }else{

                    }
                    break;
                }
                case 8:{ // Skill3
                    if(pressed){
                        if(hero.getState()==BattleEntity.stateIdle){
                            if(hero.getSkill3()!=null)
                                if(hero.getMana()>=hero.getSkill3().manaCost){
                                    game.getSoundManager().play(UnsealedSound.CLICK);
                                    hero.setMana(hero.getMana()-hero.getSkill3().manaCost);
                                    hero.magicType = hero.getSkill3().id;
                                    hero.setState(hero.getSkill3().stance);
                                }
                        }
                    }else{

                    }
                    break;
                }
                case 9:{ // SkillSwitch
                    if(pressed){
                        hero.skillSwitch();
                        
                        //Reload y,b,a
                        if(hero.getSkill1()!=null){
                            hud.xButton.clear();
                            hud.xButton = new ImageButton(new Image(hud.blankButton[0][0]).getDrawable(),new Image(hud.blankButton[1][0]).getDrawable());
                            hud.xButton.setSize(83,92);
                            hud.xButton.setX( hud.getWidth() - (hud.xButton.getWidth()*hud.xButton.getScaleX()) -160 );
                            hud.xButton.setY( hud.getHeight() -(hud.xButton.getHeight()*hud.xButton.getScaleY()+ 100) );
                            hud.xButton.addActor(hero.getSkill1());
                            hud.xButton.addListener(new PressedListener() {
                                @Override
                                public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                                    super.touchDown(event, x, y, pointer, button);
                                    buttonPress(6,true); 
                                    return true;
                                }
                                @Override
                                public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
                                    super.touchUp(event, x, y, pointer, button);
                                    buttonPress(6,false); 
                                }
                            });
                            hud.addActor(hud.xButton);
                        }else{
                            hud.xButton = new ImageButton(new Image(hud.blankButton[0][0]).getDrawable(),new Image(hud.blankButton[1][0]).getDrawable());
                            hud.xButton.setSize(83,92);
                            hud.xButton.setX( hud.getWidth() - (hud.xButton.getWidth()*hud.xButton.getScaleX()) -160 );
                            hud.xButton.setY( hud.getHeight() -(hud.xButton.getHeight()*hud.xButton.getScaleY()+ 100) );
                            hud.addActor(hud.xButton);
                        }
                        if(hero.getSkill2()!=null){
                            hud.bButton.clear();
                            hud.bButton = new ImageButton(new Image(hud.blankButton[0][0]).getDrawable(),new Image(hud.blankButton[1][0]).getDrawable());
                            hud.bButton.setSize(83,92);
                            hud.bButton.setX( hud.getWidth() - (hud.bButton.getWidth()*hud.bButton.getScaleX()) - 80 );
                            hud.bButton.setY( hud.getHeight() -(hud.bButton.getHeight()*hud.bButton.getScaleY() + 160) );
                            hud.bButton.addActor(hero.getSkill2());
                            hud.bButton.addListener(new PressedListener() {
                                @Override
                                public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                                    super.touchDown(event, x, y, pointer, button);
                                    buttonPress(7,true); 
                                    return true;
                                }
                                @Override
                                public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
                                    super.touchUp(event, x, y, pointer, button);
                                    buttonPress(7,false); 
                                }
                            });
                            hud.addActor(hud.bButton);
                        }else{
                            hud.bButton = new ImageButton(new Image(hud.blankButton[0][0]).getDrawable(),new Image(hud.blankButton[1][0]).getDrawable());
                            hud.bButton.setSize(83,92);
                            hud.bButton.setX( hud.getWidth() - (hud.bButton.getWidth()*hud.bButton.getScaleX()) - 80 );
                            hud.bButton.setY( hud.getHeight() -(hud.bButton.getHeight()*hud.bButton.getScaleY() + 160) );
                            hud.addActor(hud.bButton);
                        }
                        if(hero.getSkill3()!=null){
                            hud.aButton.clear();
                            hud.aButton = new ImageButton(new Image(hud.blankButton[0][0]).getDrawable(),new Image(hud.blankButton[1][0]).getDrawable());
                            hud.aButton.setSize(83,92);
                            hud.aButton.setX( hud.getWidth() - (hud.aButton.getWidth()*hud.aButton.getScaleX()) );
                            hud.aButton.setY( hud.getHeight() -(hud.aButton.getHeight()*hud.aButton.getScaleY() + 100) );
                            hud.aButton.addActor(hero.getSkill3());
                            hud.aButton.addListener(new PressedListener() {
                                @Override
                                public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                                    super.touchDown(event, x, y, pointer, button);
                                    buttonPress(8,true); 
                                    return true;
                                }
                                @Override
                                public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
                                    super.touchUp(event, x, y, pointer, button);
                                    buttonPress(8,false); 
                                }
                            });
                            hud.addActor(hud.aButton);
                        }else{
                            hud.aButton = new ImageButton(new Image(hud.blankButton[0][0]).getDrawable(),new Image(hud.blankButton[1][0]).getDrawable());
                            hud.aButton.setSize(83,92);
                            hud.aButton.setX( hud.getWidth() - (hud.aButton.getWidth()*hud.aButton.getScaleX())  );
                            hud.aButton.setY( hud.getHeight() -(hud.aButton.getHeight()*hud.aButton.getScaleY() + 100) );
                            hud.addActor(hud.aButton);
                        }
                    }else{

                    }
                    break;
                }
            }
        
    }
    float x;
    float y;
    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
        x= Gdx.graphics.getWidth();
        x = x/800f;
        y = Gdx.graphics.getHeight();
        y = y/480f;
        
        if(screenY>200*y&&screenY<280*y){
            if(screenX>110*x&&screenX<180*x){
                Gdx.app.log(Unsealed.LOG,"Down");
                buttonPress(1,true); 
                return true; 
            }
        }
        if(screenY>76*y&&screenY<156*y){
            if(screenX>110*x&&screenX<180*x){
                Gdx.app.log(Unsealed.LOG,"Up");
                buttonPress(0,true); 
                return true; 
            }
        }
        if(screenX>50*x&&screenX<125*x){
            if(screenY>150*y&&screenY<210*y){
                Gdx.app.log(Unsealed.LOG,"Left");
                buttonPress(2,true);
                return true;
            }   
        }
        if(screenX>180*x&&screenX<255*x){
            if(screenY>150*y&&screenY<210*y){
                Gdx.app.log(Unsealed.LOG,"Right");
                buttonPress(3,true); 
              return true;
            }
        }
        return super.touchDown(screenX, screenY, pointer, button);
    }
}
