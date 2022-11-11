package com.twisted.local.lobby;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.Event;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.twisted.util.Asset;
import com.twisted.local.lib.Ribbon;
import com.twisted.net.msg.remaining.MChat;

class SecTerminal extends Sector {

    //constants
    static final int TERMINAL_WIDTH = 480, TERMINAL_HEIGHT=300;
    static final String HELP_TEXT = "> Help text:"
            + "\n    Use the buttons above to create or connect to a server"
            + "\n    /name [text] - Rename yourself"
            + "\n    /start - As host, starts the game"
            + "\n    /kick [user] - As host, kick a player"
            ;

    //tree
    private Table terminalWidget;
    private ScrollPane pane;


    /**
     * Constructor
     */
    public SecTerminal(Lobby lobby){
        super(lobby);
    }


    /* Standard Graphics */

    @Override
    protected Group init(){
        Group parent = super.init();
        parent.setPosition(3, 3);

        //ribbon of the terminal
        Image band = new Image(Asset.retrieve(Asset.Pixel.DARKPURLE));
        band.setBounds(0, 36+3, TERMINAL_WIDTH+6, 3);
        parent.addActor(band);
        Ribbon ribbon = new Ribbon(Asset.retrieve(Asset.Pixel.DARKPURLE), 3);
        ribbon.setSize(TERMINAL_WIDTH+6,TERMINAL_HEIGHT+36+9);
        parent.addActor(ribbon);

        //create the widget
        terminalWidget = new Table();
        terminalWidget.left().bottom();

        //create the pane
        pane = new ScrollPane(terminalWidget, skin);
        pane.setBounds(3, 36+6, TERMINAL_WIDTH, TERMINAL_HEIGHT);
        pane.setColor(Color.BLACK);
        pane.setScrollingDisabled(true, false);
        pane.setFadeScrollBars(false);

        //text field input
        TextField textField = new TextField("", lobby.textFieldStyle);
        textField.setBlinkTime(0.4f);
        textField.setBounds(3, 3, TERMINAL_WIDTH, 36);
        textField.setColor(new Color(1.6f*54/255f, 1.6f*56/255f, 1.6f*68/255f, 1));

        textField.addCaptureListener((Event event) -> {
            if(event instanceof InputEvent && ((InputEvent) event).getType() == InputEvent.Type.keyUp){
                if((((InputEvent) event).getKeyCode()) == 66){
                    lobby.terminalInput(textField.getText());
                    textField.setText("");
                }
            }
            return false;
        });
        pane.addListener(event -> {
            if(event instanceof InputEvent && ((InputEvent) event).getType()== InputEvent.Type.enter){
                lobby.scrollFocus(pane);
            }
            else if(event instanceof InputEvent && ((InputEvent) event).getType()== InputEvent.Type.exit) {
                lobby.scrollFocus(null);
            }
            return true;
        });

        //add everything to the group
        parent.addActor(pane);
        parent.addActor(textField);

        return parent;
    }
    @Override
    void render(float delta) {

    }
    @Override
    void dispose() {

    }


    /* Event Methods */

    void addToTerminal(MChat.Type type, String string){
        //create the label
        Label label = new Label(string, Asset.labelStyle(Asset.Avenir.MEDIUM_14));
        label.setWrap(true);

        //set label color
        switch(type){
            case LOGISTICAL:
                label.setColor(Color.GRAY);
                break;
            case WARNING_ERROR:
                label.setColor(1, 0.5f, 0.5f, 1);
                break;
        }

        //add the label to the widget
        terminalWidget.row();
        terminalWidget.add(label).width(TERMINAL_WIDTH);

        //had to do thread for some reason
        new Thread(() -> {
            try {
                Thread.sleep(20);
            }
            catch (InterruptedException e) {
                e.printStackTrace();
            }
            pane.setScrollPercentY(1);

        }).start();
    }

    void addConnectionEnded(){
        addToTerminal(MChat.Type.LOGISTICAL, "---Connection Ended---");
    }

    void scrollToBottom(){
        pane.setScrollPercentY(1);
    }


}
