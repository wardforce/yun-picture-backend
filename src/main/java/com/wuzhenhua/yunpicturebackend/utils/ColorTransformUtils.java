package com.wuzhenhua.yunpicturebackend.utils;

public class ColorTransformUtils {
    public static String getStandarColor(String color) {
        if (color.length() == 7)
            color = color.substring(0, 4) + "0" + color.substring(4, 7);
        return color;
    }
}
