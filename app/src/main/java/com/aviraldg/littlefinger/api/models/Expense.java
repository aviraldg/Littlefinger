package com.aviraldg.littlefinger.api.models;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.text.format.DateFormat;

import com.aviraldg.littlefinger.R;
import com.facebook.imagepipeline.request.ImageRequestBuilder;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;

public class Expense {
    private String id;
    private String description;
    private float amount;
    private Calendar time;
    private String state;
    private String category;

    private static final HashMap<String, Integer> expenseIcons = new HashMap<>();
    private static final HashMap<String, Integer> expenseColors = new HashMap<>();

    static {
        expenseIcons.put("Recharge", R.drawable.ic_phone);
        expenseIcons.put("Taxi", R.drawable.ic_car);

        expenseColors.put("verified", R.color.verified);
        expenseColors.put("fraud", R.color.fraud);
    }

    public static int getColorForState(String state) {
        int res = R.color.unverified;

        if(expenseColors.containsKey(state)) {
            res = expenseColors.get(state);
        }

        return res;
    }

    public String getId() {
        return id;
    }

    public String getDescription() {
        return description;
    }

    public float getAmount() {
        return amount;
    }

    public String getFormattedAmount() {
        return String.format(Locale.getDefault(), "₹%.02f", amount);
    }

    public Calendar getTime() {
        return time;
    }

    public String getFormattedTime() {
        return DateFormat.format("h:mm a on d/L/yy", time).toString();
    }

    public String getState() {
        return state;
    }

    public String getCategory() {
        return category;
    }

    public String getMeta() {
        return String.format(Locale.getDefault(),
                "#%s #%s",
                getCategory().toLowerCase(),
                getState().toLowerCase());
    }

    public int getIcon() {
        int res = R.drawable.ic_wallet;

        if(expenseIcons.containsKey(getCategory())) {
            res = expenseIcons.get(getCategory());
        }

        return res;
    }

    public int getColor() {
        return getColorForState(getState());
    }
}
