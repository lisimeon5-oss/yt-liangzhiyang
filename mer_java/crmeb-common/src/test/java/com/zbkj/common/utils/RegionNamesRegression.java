package com.zbkj.common.utils;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

/** Standalone regression runner; no database or Spring context required. */
public class RegionNamesRegression {
    public static void main(String[] args) {
        JSONObject names = JSON.parseObject(RegionNamesUtil.merge("{\"en\":\"Old\",\"my\":\"Retained\"}", "{\"fr\":\" Paris \"}"));
        check("Retained".equals(names.getString("my")), "disabled languages must survive edits");
        check("Paris".equals(names.getString("fr")), "new language must be accepted");
        check("Old".equals(names.getString("en")), "omitted languages must survive edits");
        names = JSON.parseObject(RegionNamesUtil.merge(names.toJSONString(), "{\"en\":\"\",\"PT_BR\":\"Nome\"}"));
        check("".equals(names.getString("en")), "blank translation enables fallback");
        check("Nome".equals(names.getString("pt-br")), "language code normalization");
        check(JSON.parseObject(RegionNamesUtil.merge(names.toJSONString(), null)).equals(names), "old clients preserve names");
        for (String bad : new String[]{"[]", "null", "bad", "{\"en\":{}}", "{\"en\":42}", "{\"bad code\":\"x\"}"}) {
            boolean rejected = false;
            try { RegionNamesUtil.merge(null, bad); } catch (RuntimeException e) { rejected = true; }
            check(rejected, "invalid map must be rejected: " + bad);
        }
        System.out.println("Region name merge/validation regressions passed");
    }
    private static void check(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }
}
