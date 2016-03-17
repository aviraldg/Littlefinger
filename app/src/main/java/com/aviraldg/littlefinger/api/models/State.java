package com.aviraldg.littlefinger.api.models;

import android.support.annotation.Nullable;

import com.aviraldg.littlefinger.R;

import java.util.ArrayList;
import java.util.HashMap;

public class State {
    private static ArrayList<String> states = new ArrayList<>();
    private static HashMap<String, Integer> colorMap = new HashMap<>();
    private static HashMap<String, Integer> drawableMap = new HashMap<>();
    private String name;

    public static final String FRAUD = "fraud";
    public static final String UNVERIFIED = "unverified";
    public static final String VERIFIED = "verified";

    static {
        states.add(FRAUD);
        states.add(UNVERIFIED);
        states.add(VERIFIED);

        colorMap.put(VERIFIED, R.color.verified);
        colorMap.put(UNVERIFIED, R.color.unverified);
        colorMap.put(FRAUD, R.color.fraud);

        drawableMap.put(VERIFIED, R.drawable.ic_verified);
        drawableMap.put(UNVERIFIED, R.drawable.ic_unreviewed);
        drawableMap.put(FRAUD, R.drawable.ic_fraud);
    }

    private State(String name) {
        this.name = name;
    }

    public static State forName(String name) {
        return states.indexOf(name) >= 0 ? new State(name) : new State(UNVERIFIED);
    }

    public static State forId(int id) {
        return id < states.size() ? forName(states.get(id)) : new State(UNVERIFIED);
    }

    public static int getStateCount() {
        return states.size();
    }

    public String getName() {
        return name;
    }

    public int getId() {
        return states.indexOf(name);
    }

    public int getColorResource() {
        return colorMap.containsKey(name) ? colorMap.get(name) : R.color.unverified;
    }

    public int getDrawableResource() {
        return drawableMap.containsKey(name) ? drawableMap.get(name) : R.drawable.ic_unreviewed;
    }
}
