package model;

import clip.*;
import environment.Node;
import processing.core.PApplet;
import show.Scene;
import app.*;


public class Channel {

    public int channelNumber;


    private Scene _currentScene; //use getter setter
    private Scene _nextScene; //use getter setter
    private float _mix = 0.0f;


    public AbstractClip currentClip; //used for direct control



    //CONSTRUCTOR
    public Channel(int theChannelNumber) {
        channelNumber = theChannelNumber;
    }

    //this logic is duplicated in Scene, it should be abstracted
    /*
     public void constructNewClip(int clipClass) {

        AbstractClip newClip = new AbstractClip();
        switch (clipClass) {
            case TesseractMain.NODESCAN:
                newClip = new NodeScanClip();
                break;
            case TesseractMain.SOLID:
                newClip = new SolidColorClip();
                break;
            case TesseractMain.COLORWASH:
                newClip = new ColorWashClip();
                break;
            case TesseractMain.VIDEO:
                newClip = new VideoClip();
                break;
            case TesseractMain.PARTICLE:
                newClip = new ParticleClip();
                break;


            default:
                throw new IllegalStateException("Unexpected value: " + clipClass);
        }

        if (newClip != null) {
            newClip.init();
            clip = newClip;
            this.setClipValues(this.getSceneValues(), this.filename);
        }
    }
    */

    public void run() {// animation logic that runs per frame
        if(currentClip != null) {
            currentClip.run();
        }

        if(_currentScene != null) {
            if(_currentScene.clip != null) {
                _currentScene.clip.run();
            }
        }

        if(_nextScene != null) {
            if(_nextScene.clip != null) {
                _nextScene.clip.run();
            }
        }

        if(_mix < 0){
            _mix = 0;

            _currentScene.clip.die();
            _currentScene = _nextScene;
            _nextScene = null;
            //System.out.printf("fade over\n");
        }

        if(_mix > 0) {
            _mix -= .02f;
            //System.out.printf("%.3f ", _mix);
        }


        //System.out.printf("channel -> scene run\n");
    }

    //this is just a generic call that reaches down into clips to perform drawing unique to each clip
    public int[] drawNode(Node node) {
        if(currentClip != null) {
            return currentClip.drawNode(node);

        }else if(_currentScene != null && _nextScene == null) {
            if(_currentScene.clip != null) {
                return _currentScene.clip.drawNode(node);
            }


        }else if(_currentScene != null && _nextScene != null) {// this the the transition between scenes

            int[] currentRgb = new int[3];
            int[] nextRgb = new int[3];
            int[] sceneMix = new int[3];

            if(_currentScene.clip != null) {
                currentRgb = _currentScene.clip.drawNode(node);
            }

            if(_nextScene.clip != null) {
                nextRgb = _nextScene.clip.drawNode(node);
            }


            for(int i=0; i<3; i++){
                //apply mix ratios to each scene and add them together for a generic output format
                sceneMix[i] = (int)(currentRgb[i] * _mix) + (int)(nextRgb[i] * Math.abs(_mix-1) );

                //sceneMix[i] = nextRgb[i] /2;
                //System.out.printf("%d ", sceneMix[i]);
                //sceneMix[i] = (int)(currentRgb[i] * _mix) ;
            }

            return sceneMix;
        }


        return new int[3];
    }


    public void drawUI(PApplet p, int x, int y){
        //draw the state of this channel to processing
    }

    // this is not safe and caused an NPE exception at one point
    // need to have a proper way to 'stop' the scene, but this works for now
    public void unsetScene() {
        this._currentScene = null;
        this._nextScene = null;
    }

    public boolean setScene(Scene theScene, boolean instant, int transition){

        if(instant){
            _currentScene = theScene;

        }else{
            //if there is no current scene, skip the transition, like when a scene is loaded into this channel for the first time
            if(_currentScene == null){
                _currentScene = theScene;
                return false;
            }
            _nextScene = theScene;
            _mix = 1.0f;
        }

        //will mix
        return true;
    }

    public Scene getScene(){
        return  _currentScene;
    }

    // I need a reference to the 'active' clip, meaning the one we are playing or will be playing once the transition is finished
    // Returns _nextScene.clip if _nextScene is set, otherwise _currentScene.clip.  if both are null, returns null
    public AbstractClip getActiveClip() {
        if (this._nextScene != null) {
            return this._nextScene.clip;
        } else if (this._currentScene != null) {
            return this._currentScene.clip;
        } else {
            return null;
        }
    }
}
