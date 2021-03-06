package com.example.p009_glide.glide.options;

/**
 * Created by geek on 2016/7/28.
 */

public class GlideOptions {
    private int radius;
    private int replaceImage;
    private int errImage;

    public GlideOptions() {
    }

    public GlideOptions(int replaceImage, int radius) {
        this(replaceImage, replaceImage, radius);
    }

    public GlideOptions(int replaceImage, int errImage, int radius) {
        this.radius = radius;
        this.replaceImage = replaceImage;
        this.errImage = errImage;
    }

    public int getRadius() {
        return radius;
    }

    public void setRadius(int radius) {
        this.radius = radius;
    }

    public int getReplaceImage() {
        return replaceImage;
    }

    public void setReplaceImage(int replaceImage) {
        this.replaceImage = replaceImage;
    }

    public int getErrImage() {
        return errImage;
    }

    public void setErrImage(int errImage) {
        this.errImage = errImage;
    }
}
