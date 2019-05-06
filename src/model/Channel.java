package model;

import clip.*;
import processing.core.PApplet;
import show.Scene;
import app.*;


public class Channel {

    public int channelNumber;


    private Scene _currentScene; //use getter setter
    private AbstractClip _clip;

    //CONSTRUCTOR
    public Channel(int theChannelNumber) {
        channelNumber = theChannelNumber;

    }

    private void constructNewClip(int clipClass) {

        AbstractClip clip = new AbstractClip("Abstract Clip", channelNumber);
        switch (clipClass) {
            case TesseractMain.NODESCAN:
                clip = new NodeScanClip(TesseractMain.clipNames[clipClass], channelNumber);
                break;
            case TesseractMain.SOLID:
                clip = new SolidColorClip(TesseractMain.clipNames[clipClass], channelNumber);
                break;
            case TesseractMain.COLORWASH:
                clip = new ColorWashClip(TesseractMain.clipNames[clipClass], channelNumber);
                break;

        }

        if (clip != null) {
            clip.init();
            _clip = clip;
        }

    }


    public void drawUI(PApplet p, int x, int y){

    }

    public boolean setScene(Scene theScene){
        _currentScene = theScene;
        return true;
    }

    public Scene getScene(){
        return  _currentScene;
    }

}

