package com.lilong;
interface IMyCallback{
    void newSong();
    void musicError(String msg);
    void playingState(int i,boolean b);
}