package com.zbkj.admin.controller.platform;

import com.zbkj.common.exception.CrmebException;
import com.zbkj.common.result.CommonResult;
import com.zbkj.common.utils.SecurityUtil;
import com.zbkj.service.service.BargainService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api/admin/platform/bargain")
public class BargainPlatformController {
    @Autowired private BargainService service;
    private void platform(){Integer merId=SecurityUtil.getLoginUserVo().getUser().getMerId();if(merId!=null&&merId>0)throw new CrmebException("bargain.notFound");}
    @GetMapping("/list") @PreAuthorize("hasAuthority('platform:bargain:list')")
    public CommonResult<?> list(@RequestParam(required=false) Integer merId,@RequestParam(defaultValue="1") int page,@RequestParam(defaultValue="20") int limit){platform();return CommonResult.success(service.list(merId,page,limit,false));}
    @GetMapping("/detail/{id}") @PreAuthorize("hasAuthority('platform:bargain:list')")
    public CommonResult<?> detail(@PathVariable Integer id){platform();return CommonResult.success(service.detail(id,null,false));}
    @PostMapping("/audit/{id}") @PreAuthorize("hasAuthority('platform:bargain:audit')")
    public CommonResult<?> audit(@PathVariable Integer id,@RequestParam boolean approved,@RequestParam(defaultValue="") String remark){platform();service.audit(id,approved,remark);return CommonResult.success();}
    @PostMapping("/enabled/{id}") @PreAuthorize("hasAuthority('platform:bargain:audit')")
    public CommonResult<?> enabled(@PathVariable Integer id,@RequestParam boolean enabled){platform();service.enabled(id,null,enabled);return CommonResult.success();}
    @GetMapping("/records") @PreAuthorize("hasAuthority('platform:bargain:list')")
    public CommonResult<?> records(@org.springframework.validation.annotation.Validated com.zbkj.common.request.BargainRecordSearchRequest query,@RequestParam(required=false) Integer merId,@RequestParam(defaultValue="1") int page,@RequestParam(defaultValue="20") int limit){platform();return CommonResult.success(service.adminRecords(merId,query,page,limit));}
    @GetMapping("/records/{id}/helps") @PreAuthorize("hasAuthority('platform:bargain:list')")
    public CommonResult<?> helps(@PathVariable Integer id,@RequestParam(defaultValue="1") int page,@RequestParam(defaultValue="20") int limit){platform();return CommonResult.success(service.adminHelps(id,null,page,limit));}
}
