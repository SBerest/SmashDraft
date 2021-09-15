package com.example.smashdraft;

import androidx.annotation.NonNull;

import java.io.Serializable;

public class Fighter  implements Serializable, Comparable<Fighter> {
    private final int imageId;
    private final String name;
    private int position;

    Fighter(int image, String name){
        this.imageId = image;
        this.name = name;
    }

    public int getImageId() {
        return imageId;
    }

    public String getName() {
        return name;
    }

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }

    @Override
    public int compareTo(Fighter ch) {
        return this.getName().compareTo(ch.getName());
    }

    @Override
    @NonNull
    public String toString(){
        return this.name;
    }

}
