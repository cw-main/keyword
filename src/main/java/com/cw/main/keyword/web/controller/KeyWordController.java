package com.cw.main.keyword.web.controller;

import com.cw.main.keyword.web.req.Result;
import com.cw.main.keyword.service.KeyWordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/keyWord")
public class KeyWordController {

    @Autowired
    private KeyWordService keyWordService;

    @PostMapping("/checkStr")
    @ResponseBody
    public Result checkStr(@RequestParam("str") String str) {
        return keyWordService.checkStr(str);
    }


    @PostMapping("/checkSensitiveWord")
    @ResponseBody
    public Result checkSensitiveWord(@RequestParam("str") String str) {
        return keyWordService.checkSensitiveWord(str);
    }


}
