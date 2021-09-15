package com.example.smashdraft;

import java.util.Comparator;

public class FighterPositionComparator implements Comparator<Fighter> {
    @Override
    public int compare(Fighter o1, Fighter o2) {
        return o1.getPosition() - o2.getPosition();
    }
}
