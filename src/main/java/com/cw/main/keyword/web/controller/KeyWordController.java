package com.cw.main.keyword.web.controller;

import com.cw.main.keyword.service.KeyWordService;
import com.cw.main.keyword.web.req.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping("/keyWord")
public class KeyWordController {

    @Autowired
    private KeyWordService keyWordService;

    @GetMapping("/checkStr")
    @ResponseBody
    public Result checkStr(@RequestParam("str") String str) {
        return keyWordService.checkStr(str);
    }


    @GetMapping("/checkSensitiveWord")
    @ResponseBody
    public Result checkSensitiveWord(@RequestParam("str") String str) {
        return keyWordService.checkSensitiveWord(str);
    }


}
