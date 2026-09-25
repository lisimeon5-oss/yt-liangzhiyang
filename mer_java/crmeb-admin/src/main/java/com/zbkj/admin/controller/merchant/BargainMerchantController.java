package com.zbkj.admin.controller.merchant;

import com.zbkj.common.exception.CrmebException;
import com.zbkj.common.request.BargainSaveRequest;
import com.zbkj.common.result.CommonResult;
import com.zbkj.common.utils.SecurityUtil;
import com.zbkj.service.service.BargainService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api/admin/merchant/bargain")
public class BargainMerchantController {
    @Autowired private BargainService service;
    private Integer merchant(){Integer id=SecurityUtil.getLoginUserVo().getUser().getMerId();if(id==null||id<=0)throw new CrmebException("bargain.notFound");return id;}
    @GetMapping("/list") @PreAuthorize("hasAuthority('merchant:bargain:list')")
    public CommonResult<?> list(@RequestParam(defaultValue="1") int page,@RequestParam(defaultValue="20") int limit){return CommonResult.success(service.list(merchant(),page,limit,false));}
    @GetMapping("/detail/{id}") @PreAuthorize("hasAuthority('merchant:bargain:list')")
    public CommonResult<?> detail(@PathVariable Integer id){return CommonResult.success(service.detail(id,merchant(),false));}
    @PostMapping("/save") @PreAuthorize("hasAuthority('merchant:bargain:save')")
    public CommonResult<?> save(@Validated @RequestBody BargainSaveRequest request){return CommonResult.success(service.save(merchant(),request));}
    @PostMapping("/enabled/{id}") @PreAuthorize("hasAuthority('merchant:bargain:save')")
    public CommonResult<?> enabled(@PathVariable Integer id,@RequestParam boolean enabled){service.enabled(id,merchant(),enabled);return CommonResult.success();}
    @GetMapping("/records") @PreAuthorize("hasAuthority('merchant:bargain:list')")
    public CommonResult<?> records(@Validated com.zbkj.common.request.BargainRecordSearchRequest query,@RequestParam(defaultValue="1") int page,@RequestParam(defaultValue="20") int limit){return CommonResult.success(service.adminRecords(merchant(),query,page,limit));}
    @GetMapping("/records/{id}/helps") @PreAuthorize("hasAuthority('merchant:bargain:list')")
    public CommonResult<?> helps(@PathVariable Integer id,@RequestParam(defaultValue="1") int page,@RequestParam(defaultValue="20") int limit){return CommonResult.success(service.adminHelps(id,merchant(),page,limit));}
}
