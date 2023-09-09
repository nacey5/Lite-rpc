package com.hzh.provider.facade;

import com.hzh.provider.annotation.RpcService;

/**
 * @ClassName WordFacadeImpl
 * @Description TODO
 * @Author DaHuangGo
 * @Date 2023/9/9 21:53
 * @Version 0.0.1
 **/
@RpcService(serviceInterface = WordFacade.class, serviceVersion = "1.0.0",group = "Second")

public class WordFacadeImpl implements WordFacade{
    @Override
    public String sysWord(String word) {
        return "spell : "+word;
    }
}
