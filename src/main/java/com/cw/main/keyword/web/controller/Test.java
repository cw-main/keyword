package com.cw.main.keyword.web.controller;

import com.cw.main.keyword.utils.KeyWordUtils;

public class Test {

    public static void main(String[] args) {

//        String s = KeyWordUtils.checkSensitiveWord("有意者请联系我!");
//        System.out.println("s存在关键字：" + s);
//
//        String s1 = KeyWordUtils.checkSensitiveWord(null, "有意者请联系我", null, null);
//        System.out.println("s1存在关键字：" + s1);
//
//        String s2 = KeyWordUtils.checkSensitiveWord(null, "有一意一者请联系我", 1, Boolean.TRUE);
//        System.out.println("s2存在关键字：" + s2);


//        String replace = KeyWordUtils.replaceKeyWordForSys("有意者请联系我!");
//        System.out.println("replace 替换后的文本：" + replace);

        String replace1 = KeyWordUtils.replaceKeyWordForCustom(null, "有意者请联系我!", 1, '&');

        System.out.println("replace1 替换后的文本：" + replace1);


    }

}
