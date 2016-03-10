package com.aviraldg.littlefinger.api.models;

import android.text.format.DateFormat;

import com.aviraldg.littlefinger.R;
import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;

@SuppressWarnings("unused")
public class Expense {
    private static final HashMap<String, Integer> expenseIcons = new HashMap<>();
    private static final HashMap<String, Integer> expenseColors = new HashMap<>();

    static {
        expenseIcons.put("Recharge", R.drawable.ic_phone);
        expenseIcons.put("Taxi", R.drawable.ic_car);

        expenseColors.put("verified", R.color.verified);
        expenseColors.put("fraud", R.color.fraud);
    }

    private String id;
    private String description;
    private float amount;
    private Calendar time;
    private String state;
    private String category;
    /** UI and App Specific Properties **/
    @JsonIgnore
    private boolean isExpanded;

    public static int getColorForState(String state) {
        int res = R.color.unverified;

        if (expenseColors.containsKey(state)) {
            res = expenseColors.get(state);
        }

        return res;
    }

    public boolean isExpanded() {
        return isExpanded;
    }

    public void setExpanded(boolean expanded) {
        isExpanded = expanded;
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

        if (expenseIcons.containsKey(getCategory())) {
            res = expenseIcons.get(getCategory());
        }

        return res;
    }

    public int getColor() {
        return getColorForState(getState());
    }
}
