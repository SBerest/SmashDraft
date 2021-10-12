package com.example.smashdraft;
import androidx.annotation.NonNull;

public class Fighter  implements Comparable<Fighter> {
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
    @NonNull
    public String toString(){return this.name;}

    @Override
    public int compareTo(Fighter fighter) {
        return this.getName().compareTo(fighter.getName());
    }
}
