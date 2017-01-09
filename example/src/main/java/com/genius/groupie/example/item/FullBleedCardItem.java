package com.genius.groupie.example.item;

import android.support.annotation.ColorRes;

import com.genius.groupie.example.MainActivity;

import java.util.HashMap;
import java.util.Map;

public class FullBleedCardItem extends CardItem {

    public FullBleedCardItem(@ColorRes int colorRes) {
        super(colorRes);
        getExtras().put(MainActivity.INSET_TYPE_KEY, MainActivity.INSET);
    }
}
