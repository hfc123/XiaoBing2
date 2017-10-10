package com.cheerchip.xiaobing2;

import android.util.Base64;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created by noname on 2017/10/10.
 */

public class PicToStringUtils {


    public static String getBase64(String imgFile) {
        //将图片文件转化为字节数组字符串，并对其进行Base64编码处理
        InputStream in = null;
        byte[] data = null;
        //读取图片字节数组
        try {
            in = new FileInputStream(imgFile);
            data = new byte[in.available()];
            in.read(data);
            in.close();
            return new String(Base64.encodeToString(data, Base64.DEFAULT));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

}
