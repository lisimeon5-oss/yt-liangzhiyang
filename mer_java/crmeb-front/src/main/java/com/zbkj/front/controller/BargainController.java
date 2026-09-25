package com.zbkj.front.controller;

import com.zbkj.common.result.CommonResult;
import com.zbkj.service.service.BargainService;
import com.zbkj.service.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api/front/bargain")
public class BargainController {
    @Autowired private BargainService service;
    @Autowired private UserService users;
    @GetMapping("/list")
    public CommonResult<?> list(@RequestParam(required=false) Integer merId,@RequestParam(defaultValue="1") int page,@RequestParam(defaultValue="20") int limit){
        return CommonResult.success(service.list(merId,page,limit,true));
    }
    @GetMapping("/detail/{id}")
    public CommonResult<?> detail(@PathVariable Integer id){return CommonResult.success(service.detail(id,null,true));}
    @GetMapping("/progress/{id}")
    public CommonResult<?> progress(@PathVariable Integer id){return CommonResult.success(service.progress(id,users.getUserId()));}
    @PostMapping("/start/{id}")
    public CommonResult<?> start(@PathVariable Integer id){return CommonResult.success(service.start(id,users.getUserIdException()));}
    @PostMapping("/help/{id}")
    public CommonResult<?> help(@PathVariable Integer id){return CommonResult.success(service.help(id,users.getUserIdException()));}
    @GetMapping("/records")
    public CommonResult<?> records(@RequestParam(defaultValue="1") int page,@RequestParam(defaultValue="20") int limit){
        return CommonResult.success(service.records(users.getUserIdException(),null,page,limit));
    }
}
