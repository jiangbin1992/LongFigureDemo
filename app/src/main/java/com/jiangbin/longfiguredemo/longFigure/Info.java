package com.jiangbin.longfiguredemo.longFigure;

import java.io.Serializable;
import java.util.List;

/**
 * Created by jiangbin on 2019/11/20 11:57
 */
public class Info implements Serializable {

    String content;
    List<String> imageList;

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public List<String> getImageList() {
        return imageList;
    }

    public void setImageList(List<String> imageList) {
        this.imageList = imageList;
    }
}