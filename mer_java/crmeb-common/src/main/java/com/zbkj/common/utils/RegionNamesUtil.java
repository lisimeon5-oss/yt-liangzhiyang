package com.zbkj.common.utils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.zbkj.common.exception.CrmebException;
import com.zbkj.common.result.CommonResultCode;
import java.util.Locale;
import java.util.Map;

/** Language-keyed display names. Canonical address names/IDs are never modified here. */
public final class RegionNamesUtil {
    private RegionNamesUtil() {}

    public static String merge(String current, String patch) {
        JSONObject result = parse(current);
        for (Map.Entry<String, Object> entry : parse(patch).entrySet()) {
            String code = entry.getKey().trim().toLowerCase(Locale.ROOT).replace('_', '-');
            if (!code.matches("[a-z]{2,8}(-[a-z0-9]{1,8})*") || !(entry.getValue() instanceof String)) {
                throw invalid();
            }
            String text = ((String) entry.getValue()).trim();
            if (text.length() > 128) throw invalid();
            result.put(code, text);
        }
        if (result.size() > 200) throw invalid();
        return result.toJSONString();
    }

    private static JSONObject parse(String value) {
        if (value == null || value.trim().isEmpty()) return new JSONObject();
        if (value.length() > 100000) throw invalid();
        try {
            Object parsed = JSON.parse(value);
            if (!(parsed instanceof JSONObject)) throw invalid();
            return (JSONObject) parsed;
        } catch (Exception e) {
            throw invalid();
        }
    }

    private static CrmebException invalid() {
        return new CrmebException(CommonResultCode.VALIDATE_FAILED, "地区译名须为语言代码到文本的JSON对象，每项最多128字符");
    }
}
