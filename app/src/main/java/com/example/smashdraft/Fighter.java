package com.example.smashdraft;

import androidx.annotation.NonNull;

import java.io.Serializable;

public class Fighter  implements Serializable, Comparable<Fighter> {
    private final int imageId;
    private final String name;

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

    @Override
    public int compareTo(Fighter ch) {
        return this.getName().compareTo(ch.getName());
    }
}
