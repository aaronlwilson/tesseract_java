package model;

import clip.*;
import environment.Node;
import processing.core.PApplet;
import show.Scene;
import app.*;


public class Channel {

    public int channelNumber;


    private Scene _currentScene; //use getter setter

    public AbstractClip currentClip;


    //CONSTRUCTOR
    public Channel(int theChannelNumber) {
        channelNumber = theChannelNumber;

    }

    // commented this out just to make sure it wasn't affecting anything
//    public void constructNewClip(int clipClass) {
//
//        AbstractClip clip = new AbstractClip("Abstract Clip");
//        switch (clipClass) {
//            case TesseractMain.NODESCAN:
//                clip = new NodeScanClip(TesseractMain.clipNames[clipClass]);
//                break;
//            case TesseractMain.SOLID:
//                clip = new SolidColorClip(TesseractMain.clipNames[clipClass]);
//                break;
//            case TesseractMain.COLORWASH:
//                clip = new ColorWashClip(TesseractMain.clipNames[clipClass]);
//                break;
//
//            default:
//                throw new IllegalStateException("Unexpected value: " + clipClass);
//        }
//
//        if (clip != null) {
//            clip.init();
//            currentClip = clip;
//
//            //temp purple
//            currentClip.p4 = 1;
//            currentClip.p6 = 1;
//        }
//    }

    public void run() {// animation logic that runs per frame
        if(currentClip != null) {
            currentClip.run();

        }else if(_currentScene != null) {
            if(_currentScene.clip != null) {
                _currentScene.clip.run();
                //System.out.printf("scene run\n");
            }
        }
    }

    //this is just a generic call that reaches down into clips to perform drawing unique to each clip
    public int[] drawNode(Node node) {
        if(currentClip != null) {
            return currentClip.drawNode(node);

        }else if(_currentScene != null) {
            if(_currentScene.clip != null) {
                return _currentScene.clip.drawNode(node);
                //System.out.printf("scene run\n");
            }
        }

        return new int[3];
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

