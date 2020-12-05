package com.cw.main.keyword.service.impl;

import com.cw.main.keyword.web.req.Result;
import com.cw.main.keyword.service.KeyWordService;
import com.cw.main.keyword.utils.KeyWordUtils;
import org.springframework.stereotype.Service;

@Service
public class KeyWordServiceImpl implements KeyWordService {


    @Override
    public Result checkStr(String str) {
        String result = KeyWordUtils.checkSensitiveWord(str);
        return Result.success(result);
    }

    @Override
    public Result checkSensitiveWord(String str) {
        String s = KeyWordUtils.checkSensitiveWord(null, str, null, Boolean.TRUE);
        return Result.success(s);
    }
}
