package com.lilong;
import com.lilong.IMyCallback;
interface IMyService{
    void pre();
    void play(int index);
    void pause();
    void stop();
    void next();
    
    void seekTo(int i);
    int getProgress();
    int getCurrentMusic();
    String getCurrent();
    boolean IsPlaying();
    
    void setPlayingMode(boolean singleLoop,boolean listLoop);
    
    void registerCallback(IMyCallback cb);
    void unregisterCallback(IMyCallback cb);
}