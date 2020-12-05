package com.cw.main.keyword.service;

import com.cw.main.keyword.web.req.Result;

public interface KeyWordService {

    Result checkStr(String str);

    Result checkSensitiveWord(String str);

}
