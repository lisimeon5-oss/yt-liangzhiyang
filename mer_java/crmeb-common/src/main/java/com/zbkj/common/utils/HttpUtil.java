package com.zbkj.common.utils;


import com.alibaba.fastjson.JSONObject;
import com.zbkj.common.response.HttpResponse;
import org.apache.http.Consts;
import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.message.BasicNameValuePair;
import org.springframework.util.CollectionUtils;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @version 1.0.0
 * @ClassName HttpUtil
 * @Description httpclient 工具类
 */
public class HttpUtil {

    /**
     * @param url url+参数组合的请求地址
     * @return
     * @throws Exception
     * @Description Content-Type:"application/json"类型的get请求
     */
    public static HttpResponse toGetJson(String url) throws Exception {
        CloseableHttpClient httpclient = null;
        String result = null;
        try {
            httpclient = HttpClients.custom()
                    .setConnectionManager(createConnectionManager())
                    .build();
            HttpGet httpGet = new HttpGet(url);
            httpGet.setHeader("Content-Type", "application/json");
            httpGet.setHeader("charset", Consts.UTF_8.toString());
            httpGet.setConfig(getConfig());
            CloseableHttpResponse response = httpclient.execute(httpGet);

            HttpEntity entity = response.getEntity();
            BufferedReader reader = new BufferedReader(new InputStreamReader(entity.getContent(), Consts.UTF_8));
            StringBuffer sb = new StringBuffer();
            String content = null;
            while ((content = reader.readLine()) != null) {
                sb.append(content);
            }
            result = sb.toString();

            return new HttpResponse(response.getStatusLine().getStatusCode(), result);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        } finally {
            if (httpclient != null) {
                httpclient.close();
            }
        }
    }

    /**
     * @param url
     * @param data
     * @return
     * @throws Exception
     * @Description Content-Type:"application/json"类型的get请求
     */
    public static HttpResponse toGetJson(String url, Map<String, String> data) throws Exception {
        CloseableHttpClient httpclient = null;
        String result = null;
        try {
            httpclient = HttpClients.custom()
                    .setConnectionManager(createConnectionManager())
                    .build();
            StringBuffer buf = new StringBuffer();
            //拼装请求参数
            if (!CollectionUtils.isEmpty(data)) {
                data.keySet().stream().forEach(key -> {
                    buf.append("&").append(key).append("=").append(data.get(key));
                });
                ;
            }
            String params = buf.toString().replaceFirst("&", "");
            url = url.contains("?") ? url + params : url + "?" + params;
            //初始化get请求函数
            HttpGet httpGet = new HttpGet(url);
            httpGet.setHeader("Content-Type", "application/json");
            httpGet.setHeader("charset", Consts.UTF_8.toString());
            httpGet.setConfig(getConfig());
            CloseableHttpResponse response = httpclient.execute(httpGet);

            HttpEntity entity = response.getEntity();
            BufferedReader reader = new BufferedReader(new InputStreamReader(entity.getContent(), Consts.UTF_8));
            StringBuffer sb = new StringBuffer();
            String content = null;
            while ((content = reader.readLine()) != null) {
                sb.append(content);
            }
            result = sb.toString();

            return new HttpResponse(response.getStatusLine().getStatusCode(), result);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        } finally {
            if (httpclient != null) {
                httpclient.close();
            }
        }
    }


    /**
     * @param url
     * @return
     * @throws Exception
     * @Description Content-Type:"application/json"类型的get请求
     */
    public static HttpResponse toGetJsonContainsHeader(String url, Map<String, String> header) throws Exception {
        CloseableHttpClient httpclient = null;
        String result = null;
        try {
            httpclient = HttpClients.custom()
                    .setConnectionManager(createConnectionManager())
                    .build();
            StringBuffer buf = new StringBuffer();
            String params = buf.toString().replaceFirst("&", "");
            url = url.contains("?") ? url + params : url + "?" + params;
            //初始化get请求函数
            HttpGet httpGet = new HttpGet(url);

            //设置请求头
            if (!CollectionUtils.isEmpty(header)) {
                header.keySet().stream().forEach(key -> {
                    httpGet.setHeader(key, header.get(key));
                });
            }
            httpGet.setHeader("Content-Type", "application/json");
            httpGet.setHeader("charset", Consts.UTF_8.toString());
            httpGet.setConfig(getConfig());
            CloseableHttpResponse response = httpclient.execute(httpGet);

            HttpEntity entity = response.getEntity();
            BufferedReader reader = new BufferedReader(new InputStreamReader(entity.getContent(), Consts.UTF_8));
            StringBuffer sb = new StringBuffer();
            String content = null;
            while ((content = reader.readLine()) != null) {
                sb.append(content);
            }
            result = sb.toString();

            return new HttpResponse(response.getStatusLine().getStatusCode(), result);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        } finally {
            if (httpclient != null) {
                httpclient.close();
            }
        }
    }

    /**
     * @param url
     * @param data
     * @param header 自定义请求头
     * @return
     * @throws Exception
     * @Description Content-Type:"application/json"类型的get请求
     */
    public static HttpResponse toGetJson(String url, Map<String, String> data, Map<String, String> header) throws Exception {
        CloseableHttpClient httpclient = null;
        String result = null;
        try {
            httpclient = HttpClients.custom()
                    .setConnectionManager(createConnectionManager())
                    .build();
            StringBuffer buf = new StringBuffer();
            //拼装请求参数
            if (!CollectionUtils.isEmpty(data)) {
                data.keySet().stream().forEach(key -> {
                    buf.append("&").append(key).append("=").append(data.get(key));
                });
            }
            String params = buf.toString().replaceFirst("&", "");
            url = url.contains("?") ? url + params : url + "?" + params;
            //初始化get请求函数
            HttpGet httpGet = new HttpGet(url);

            //设置请求头
            if (!CollectionUtils.isEmpty(header)) {
                header.keySet().stream().forEach(key -> {
                    httpGet.setHeader(key, header.get(key));
                });
            }
            httpGet.setHeader("Content-Type", "application/json");
            httpGet.setHeader("charset", Consts.UTF_8.toString());
            httpGet.setConfig(getConfig());
            CloseableHttpResponse response = httpclient.execute(httpGet);

            HttpEntity entity = response.getEntity();
            BufferedReader reader = new BufferedReader(new InputStreamReader(entity.getContent(), Consts.UTF_8));
            StringBuffer sb = new StringBuffer();
            String content = null;
            while ((content = reader.readLine()) != null) {
                sb.append(content);
            }
            result = sb.toString();

            return new HttpResponse(response.getStatusLine().getStatusCode(), result);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        } finally {
            if (httpclient != null) {
                httpclient.close();
            }
        }
    }


    /**
     * @param url url+参数组合的请求地址
     * @return
     * @throws Exception
     * @Description Content-Type:"application/x-www-form-urlencoded"类型的get请求
     */
    public static HttpResponse toGetForm(String url) throws Exception {
        CloseableHttpClient httpclient = null;
        String result = null;
        try {
            httpclient = HttpClients.custom()
                    .setConnectionManager(createConnectionManager())
                    .build();
            HttpGet httpGet = new HttpGet(url);
            httpGet.setHeader("Content-Type", "application/x-www-form-urlencoded");
            httpGet.setHeader("charset", Consts.UTF_8.toString());
            httpGet.setConfig(getConfig());
            CloseableHttpResponse response = httpclient.execute(httpGet);

            HttpEntity entity = response.getEntity();
            BufferedReader reader = new BufferedReader(new InputStreamReader(entity.getContent(), Consts.UTF_8));
            StringBuffer sb = new StringBuffer();
            String content = null;
            while ((content = reader.readLine()) != null) {
                sb.append(content);
            }
            result = sb.toString();

            return new HttpResponse(response.getStatusLine().getStatusCode(), result);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        } finally {
            if (httpclient != null) {
                httpclient.close();
            }
        }
    }

    /**
     * @param url
     * @param header
     * @return
     * @throws Exception
     * @Description "Content-Type":"application/x-www-form-urlencoded"类型的get请求
     */
    public static HttpResponse toGetFormContainsHeader(String url, Map<String, String> header) throws Exception {
        CloseableHttpClient httpclient = null;
        String result = null;
        try {
            httpclient = HttpClients.custom()
                    .setConnectionManager(createConnectionManager())
                    .build();
            HttpGet httpGet = new HttpGet(url);
            httpGet.setHeader("Content-Type", "application/x-www-form-urlencoded");
            httpGet.setHeader("charset", "utf-8");
            if (!CollectionUtils.isEmpty(header)) {
                header.keySet().forEach(e -> {
                    httpGet.setHeader(e, header.get(e));
                });
            }
            httpGet.setConfig(getConfig());
            CloseableHttpResponse response = httpclient.execute(httpGet);

            HttpEntity entity = response.getEntity();
            BufferedReader reader = new BufferedReader(new InputStreamReader(entity.getContent(), Consts.UTF_8));
            StringBuffer sb = new StringBuffer();
            String content = null;
            while ((content = reader.readLine()) != null) {
                sb.append(content);
            }
            result = sb.toString();

            return new HttpResponse(response.getStatusLine().getStatusCode(), result);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        } finally {
            if (httpclient != null) {
                httpclient.close();
            }
        }
    }

    /**
     * @param url
     * @param data
     * @param header 自定义请求头
     * @return
     * @throws Exception
     * @Description Content-Type:"application/json"类型的get请求
     */
    public static HttpResponse toGetForm(String url, Map<String, String> data, Map<String, String> header) throws Exception {
        CloseableHttpClient httpclient = null;
        String result = null;
        try {
            httpclient = HttpClients.custom()
                    .setConnectionManager(createConnectionManager())
                    .build();
            StringBuffer buf = new StringBuffer();
            //拼装请求参数
            if (!CollectionUtils.isEmpty(data)) {
                data.keySet().stream().forEach(key -> {
                    buf.append("&").append(key).append("=").append(data.get(key));
                });
            }
            String params = buf.toString().replaceFirst("&", "");
            url = url.contains("?") ? url + params : url + "?" + params;
            //初始化get请求函数
            HttpGet httpGet = new HttpGet(url);

            //设置请求头
            if (!CollectionUtils.isEmpty(header)) {
                header.keySet().stream().forEach(key -> {
                    httpGet.setHeader(key, header.get(key));
                });
            }
            httpGet.setHeader("Content-Type", "application/x-www-form-urlencoded");
            httpGet.setHeader("charset", Consts.UTF_8.toString());
            httpGet.setConfig(getConfig());
            CloseableHttpResponse response = httpclient.execute(httpGet);

            HttpEntity entity = response.getEntity();
            BufferedReader reader = new BufferedReader(new InputStreamReader(entity.getContent(), Consts.UTF_8));
            StringBuffer sb = new StringBuffer();
            String content = null;
            while ((content = reader.readLine()) != null) {
                sb.append(content);
            }
            result = sb.toString();

            return new HttpResponse(response.getStatusLine().getStatusCode(), result);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        } finally {
            if (httpclient != null) {
                httpclient.close();
            }
        }
    }

    /**
     * @param url url+参数组合的请求地址
     * @return
     * @throws Exception
     * @Description Content-Type:"application/xml"类型的get请求
     */
    public static HttpResponse toGetXml(String url) throws Exception {
        CloseableHttpClient httpclient = null;
        String result = null;
        try {
            httpclient = HttpClients.custom()
                    .setConnectionManager(createConnectionManager())
                    .build();
            HttpGet httpGet = new HttpGet(url);
            httpGet.setHeader("Content-Type", "application/xml");
            httpGet.setHeader("charset", Consts.UTF_8.toString());
            httpGet.setConfig(getConfig());
            CloseableHttpResponse response = httpclient.execute(httpGet);

            HttpEntity entity = response.getEntity();
            BufferedReader reader = new BufferedReader(new InputStreamReader(entity.getContent(), Consts.UTF_8));
            StringBuffer sb = new StringBuffer();
            String content = null;
            while ((content = reader.readLine()) != null) {
                sb.append(content);
            }
            result = sb.toString();

            return new HttpResponse(response.getStatusLine().getStatusCode(), result);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        } finally {
            if (httpclient != null) {
                httpclient.close();
            }
        }
    }

    /**
     * @param url
     * @param data
     * @return
     * @throws Exception
     * @Description Content-Type:"application/xml"类型的get请求
     */
    public static HttpResponse toGetXml(String url, Map<String, String> data) throws Exception {
        CloseableHttpClient httpclient = null;
        String result = null;
        try {
            httpclient = HttpClients.custom()
                    .setConnectionManager(createConnectionManager())
                    .build();
            StringBuffer buf = new StringBuffer();
            //拼装请求参数
            if (!CollectionUtils.isEmpty(data)) {
                data.keySet().stream().forEach(key -> {
                    buf.append("&").append(key).append("=").append(data.get(key));
                });
                ;
            }
            String params = buf.toString().replaceFirst("&", "");
            url = url.contains("?") ? url + params : url + "?" + params;
            //初始化get请求函数
            HttpGet httpGet = new HttpGet(url);
            httpGet.setHeader("Content-Type", "application/xml");
            httpGet.setHeader("charset", Consts.UTF_8.toString());
            httpGet.setConfig(getConfig());
            CloseableHttpResponse response = httpclient.execute(httpGet);

            HttpEntity entity = response.getEntity();
            BufferedReader reader = new BufferedReader(new InputStreamReader(entity.getContent(), Consts.UTF_8));
            StringBuffer sb = new StringBuffer();
            String content = null;
            while ((content = reader.readLine()) != null) {
                sb.append(content);
            }
            result = sb.toString();

            return new HttpResponse(response.getStatusLine().getStatusCode(), result);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        } finally {
            if (httpclient != null) {
                httpclient.close();
            }
        }
    }

    /**
     * @param url
     * @param data
     * @param header 自定义请求头
     * @return
     * @throws Exception
     * @Description Content-Type:"application/xml"类型的get请求
     */
    public static HttpResponse toGetXml(String url, Map<String, String> data, Map<String, String> header) throws Exception {
        CloseableHttpClient httpclient = null;
        String result = null;
        try {
            httpclient = HttpClients.custom()
                    .setConnectionManager(createConnectionManager())
                    .build();
            StringBuffer buf = new StringBuffer();
            //拼装请求参数
            if (!CollectionUtils.isEmpty(data)) {
                data.keySet().stream().forEach(key -> {
                    buf.append("&").append(key).append("=").append(data.get(key));
                });
            }
            String params = buf.toString().replaceFirst("&", "");
            url = url.contains("?") ? url + params : url + "?" + params;
            //初始化get请求函数
            HttpGet httpGet = new HttpGet(url);

            //设置请求头
            if (!CollectionUtils.isEmpty(header)) {
                header.keySet().stream().forEach(key -> {
                    httpGet.setHeader(key, header.get(key));
                });
            }
            httpGet.setHeader("Content-Type", "application/xml");
            httpGet.setHeader("charset", Consts.UTF_8.toString());
            httpGet.setConfig(getConfig());
            CloseableHttpResponse response = httpclient.execute(httpGet);

            HttpEntity entity = response.getEntity();
            BufferedReader reader = new BufferedReader(new InputStreamReader(entity.getContent(), Consts.UTF_8));
            StringBuffer sb = new StringBuffer();
            String content = null;
            while ((content = reader.readLine()) != null) {
                sb.append(content);
            }
            result = sb.toString();

            return new HttpResponse(response.getStatusLine().getStatusCode(), result);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        } finally {
            if (httpclient != null) {
                httpclient.close();
            }
        }
    }

    /**
     * @param data
     * @param url
     * @return
     * @throws Exception
     * @Description POST请求-数据类型JSON
     */
    public static HttpResponse toPostJson(String url, String data) throws Exception {
        CloseableHttpClient httpclient = null;
        String result = null;
        try {
            httpclient = HttpClients.custom()
                    .setConnectionManager(createConnectionManager())
                    .build();
            HttpPost httppost = new HttpPost(url);
            // 设置包体参数。
            StringEntity se = new StringEntity(data);
            se.setContentEncoding("UTF-8");
            se.setContentType("application/json");
            httppost.setEntity(se);
            httppost.setConfig(getConfig());
            CloseableHttpResponse response = httpclient.execute(httppost);

            HttpEntity entity = response.getEntity();
            BufferedReader reader = new BufferedReader(new InputStreamReader(entity.getContent(), Consts.UTF_8));
            StringBuffer sb = new StringBuffer();
            String content = null;
            while ((content = reader.readLine()) != null) {
                sb.append(content);
            }
            result = sb.toString();

            return new HttpResponse(response.getStatusLine().getStatusCode(), result);
        } catch (Exception e) {
            e.printStackTrace();
            throw new Exception(e.getMessage());
        } finally {
            if (httpclient != null) {
                httpclient.close();
            }
        }
    }

    /**
     * @param data
     * @param url
     * @return
     * @throws Exception
     * @Description POST请求-数据类型JSON
     */
    public static HttpResponse toPostJson(String url, String data, Map<String, String> header) throws Exception {
        CloseableHttpClient httpclient = null;
        String result = null;
        try {
            httpclient = HttpClients.custom()
                    .setConnectionManager(createConnectionManager())
                    .build();
            HttpPost httppost = new HttpPost(url);
            // 设置包体参数。
            StringEntity se = new StringEntity(data);
            se.setContentEncoding("UTF-8");
            se.setContentType("application/json");
            httppost.setEntity(se);
            //设置请求头
            if (!CollectionUtils.isEmpty(header)) {
                header.keySet().stream().forEach(key -> {
                    httppost.setHeader(key, header.get(key));
                });
            }
            httppost.setConfig(getConfig());
            CloseableHttpResponse response = httpclient.execute(httppost);
            if (response.getStatusLine().getStatusCode() == 200) {
                HttpEntity entity = response.getEntity();
                BufferedReader reader = new BufferedReader(new InputStreamReader(entity.getContent(), Consts.UTF_8));
                StringBuffer sb = new StringBuffer();
                String content = null;
                while ((content = reader.readLine()) != null) {
                    sb.append(content);
                }
                result = sb.toString();
            }
            return new HttpResponse(response.getStatusLine().getStatusCode(), result);
        } catch (Exception e) {
            e.printStackTrace();
            throw new Exception(e.getMessage());
        } finally {
            if (httpclient != null) {
                httpclient.close();
            }
        }
    }

    /**
     * @param url
     * @param data
     * @return
     * @throws Exception
     * @Description Content-Type:"application/json"类型的get请求
     */
    public static HttpResponse toPostJson(String url, Map<String, String> data) throws Exception {
        CloseableHttpClient httpclient = null;
        String result = null;
        try {
            httpclient = HttpClients.custom()
                    .setConnectionManager(createConnectionManager())
                    .build();
            HttpPost httppost = new HttpPost(url);
            httppost.setHeader("Content-Type", "application/json");
            httppost.setHeader("charset", Consts.UTF_8.toString());
            StringEntity se = new StringEntity(JSONObject.toJSONString(data), Consts.UTF_8.toString());
            se.setContentType("application/json");
            httppost.setEntity(se);
            httppost.setConfig(getConfig());
            CloseableHttpResponse response = httpclient.execute(httppost);

            HttpEntity entity = response.getEntity();
            BufferedReader reader = new BufferedReader(new InputStreamReader(entity.getContent(), Consts.UTF_8));
            StringBuffer sb = new StringBuffer();
            String content = null;
            while ((content = reader.readLine()) != null) {
                sb.append(content);
            }
            result = sb.toString();
            return new HttpResponse(response.getStatusLine().getStatusCode(), result);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        } finally {
            if (httpclient != null) {
                httpclient.close();
            }
        }
    }

    /**
     * @param url
     * @param data
     * @return
     * @throws Exception
     * @Description Content-Type:"application/json"类型的get请求
     */
    public static HttpResponse toPostJsonObj(String url, Map<String, Object> data) throws Exception {
        CloseableHttpClient httpclient = null;
        String result = null;
        try {
            httpclient = HttpClients.custom()
                    .setConnectionManager(createConnectionManager())
                    .build();
            HttpPost httppost = new HttpPost(url);
            httppost.setHeader("Content-Type", "application/json");
            httppost.setHeader("charset", Consts.UTF_8.toString());
            StringEntity se = new StringEntity(JSONObject.toJSONString(data), Consts.UTF_8.toString());
            se.setContentType("application/json");
            httppost.setEntity(se);
            httppost.setConfig(getConfig());
            CloseableHttpResponse response = httpclient.execute(httppost);

            HttpEntity entity = response.getEntity();
            BufferedReader reader = new BufferedReader(new InputStreamReader(entity.getContent(), Consts.UTF_8));
            StringBuffer sb = new StringBuffer();
            String content = null;
            while ((content = reader.readLine()) != null) {
                sb.append(content);
            }
            result = sb.toString();
            return new HttpResponse(response.getStatusLine().getStatusCode(), result);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        } finally {
            if (httpclient != null) {
                httpclient.close();
            }
        }
    }


    /**
     * @param url
     * @param data
     * @param header 自定义请求头
     * @return
     * @throws Exception
     * @Description Content-Type:"application/json"类型的get请求
     */
    public static HttpResponse toPostJson(String url, Map<String, String> data, Map<String, String> header) throws Exception {
        CloseableHttpClient httpclient = null;
        String result = null;
        try {
            httpclient = HttpClients.custom()
                    .setConnectionManager(createConnectionManager())
                    .build();
            HttpPost httppost = new HttpPost(url);
            //设置参数
            List<NameValuePair> nvps = new ArrayList<>();
            if (data != null && !data.isEmpty()) {//设置参数
                data.keySet().stream().forEach(e -> {
                    nvps.add(new BasicNameValuePair(e, data.get(e)));
                });
                httppost.setEntity(new UrlEncodedFormEntity(nvps, Consts.UTF_8.toString()));
            }

            //设置请求头
            if (!CollectionUtils.isEmpty(header)) {
                header.keySet().stream().forEach(key -> {
                    httppost.setHeader(key, header.get(key));
                });
            }
            StringEntity se = new StringEntity(JSONObject.toJSONString(data), Consts.UTF_8.toString());
            se.setContentType("application/json");
            httppost.setEntity(se);
            httppost.setConfig(getConfig());

            CloseableHttpResponse response = httpclient.execute(httppost);
            HttpEntity entity = response.getEntity();
            BufferedReader reader = new BufferedReader(new InputStreamReader(entity.getContent(), Consts.UTF_8));
            StringBuffer sb = new StringBuffer();
            String content = null;
            while ((content = reader.readLine()) != null) {
                sb.append(content);
            }
            result = sb.toString();

            return new HttpResponse(response.getStatusLine().getStatusCode(), result);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        } finally {
            if (httpclient != null) {
                httpclient.close();
            }
        }
    }

    /**
     * @param data
     * @param url
     * @param url
     * @return
     * @throws Exception
     * @Description POST请求-数据类型form表单
     */
    public static HttpResponse toPostForm(String url, String data) throws Exception {
        CloseableHttpClient httpclient = null;
        String result = null;
        try {
            httpclient = HttpClients.custom()
                    .setConnectionManager(createConnectionManager())
                    .build();
            HttpPost httppost = new HttpPost(url);
            if (data != null) {
                StringEntity entity = new StringEntity(data);
                entity.setContentEncoding("UTF-8");
                httppost.setEntity(entity);
                httppost.setHeader("Content-Type", "application/x-www-form-urlencoded");
            }
            httppost.setConfig(getConfig());
            CloseableHttpResponse response = httpclient.execute(httppost);

            HttpEntity entity = response.getEntity();
            BufferedReader reader = new BufferedReader(new InputStreamReader(entity.getContent(), Consts.UTF_8));
            StringBuffer sb = new StringBuffer();
            String content = null;
            while ((content = reader.readLine()) != null) {
                sb.append(content);
            }
            result = sb.toString();

            return new HttpResponse(response.getStatusLine().getStatusCode(), result);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        } finally {
            if (httpclient != null) {
                httpclient.close();
            }
        }
    }

    /**
     * @param url
     * @param data
     * @return
     * @throws Exception
     * @Description Content-Type:"application/json"类型的get请求
     */
    public static HttpResponse toPostForm(String url, Map<String, String> data) throws Exception {
        CloseableHttpClient httpclient = null;
        String result = null;
        try {
            httpclient = HttpClients.custom()
                    .setConnectionManager(createConnectionManager())
                    .build();
            HttpPost httppost = new HttpPost(url);
            httppost.setHeader("Content-Type", "application/x-www-form-urlencoded");
            httppost.setHeader("charset", Consts.UTF_8.toString());
            List<NameValuePair> nvps = new ArrayList<NameValuePair>();
            // 设置包体参数。
            if (!CollectionUtils.isEmpty(data)) {
                data.keySet().forEach(e -> {
                    NameValuePair nvp = new BasicNameValuePair(e, data.get(e));
                    nvps.add(nvp);
                });
            }
            StringEntity se = new UrlEncodedFormEntity(nvps, Consts.UTF_8);
            httppost.setEntity(se);
            httppost.setConfig(getConfig());
            CloseableHttpResponse response = httpclient.execute(httppost);

            HttpEntity entity = response.getEntity();
            BufferedReader reader = new BufferedReader(new InputStreamReader(entity.getContent(), Consts.UTF_8));
            StringBuffer sb = new StringBuffer();
            String content = null;
            while ((content = reader.readLine()) != null) {
                sb.append(content);
            }
            result = sb.toString();

            return new HttpResponse(response.getStatusLine().getStatusCode(), result);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        } finally {
            if (httpclient != null) {
                httpclient.close();
            }
        }
    }


    /**
     * @param url
     * @param data
     * @return
     * @throws Exception
     * @Description Content-Type:"application/json"类型的get请求
     */
    public static HttpResponse toPostFormObj(String url, Map<String, Object> data) throws Exception {
        CloseableHttpClient httpclient = null;
        String result = null;
        try {
            httpclient = HttpClients.custom()
                    .setConnectionManager(createConnectionManager())
                    .build();
            HttpPost httppost = new HttpPost(url);
            httppost.setHeader("Content-Type", "application/x-www-form-urlencoded");
            httppost.setHeader("charset", Consts.UTF_8.toString());
            List<NameValuePair> nvps = new ArrayList<NameValuePair>();
            // 设置包体参数。
            if (!CollectionUtils.isEmpty(data)) {
                data.keySet().forEach(e -> {
                    NameValuePair nvp = new BasicNameValuePair(e, data.get(e).toString());
                    nvps.add(nvp);
                });
            }
            StringEntity se = new UrlEncodedFormEntity(nvps, Consts.UTF_8);
            httppost.setEntity(se);
            httppost.setConfig(getConfig());
            CloseableHttpResponse response = httpclient.execute(httppost);

            HttpEntity entity = response.getEntity();
            BufferedReader reader = new BufferedReader(new InputStreamReader(entity.getContent(), Consts.UTF_8));
            StringBuffer sb = new StringBuffer();
            String content = null;
            while ((content = reader.readLine()) != null) {
                sb.append(content);
            }
            result = sb.toString();

            return new HttpResponse(response.getStatusLine().getStatusCode(), result);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        } finally {
            if (httpclient != null) {
                httpclient.close();
            }
        }
    }


    /**
     * @param url
     * @param header
     * @return
     * @throws Exception
     * @Description "Content-Type":"application/x-www-form-urlencoded"类型的get请求
     */
    public static HttpResponse toPostFormContainsHeader(String url, Map<String, String> header) throws Exception {
        CloseableHttpClient httpclient = null;
        String result = null;
        try {
            httpclient = HttpClients.custom()
                    .setConnectionManager(createConnectionManager())
                    .build();
            HttpPost httppost = new HttpPost(url);
            //设置请求头
            if (!CollectionUtils.isEmpty(header)) {
                header.keySet().stream().forEach(key -> {
                    httppost.setHeader(key, header.get(key));
                });
            }
            httppost.setHeader("Content-Type", "application/x-www-form-urlencoded");
            httppost.setHeader("charset", Consts.UTF_8.toString());
            httppost.setConfig(getConfig());
            CloseableHttpResponse response = httpclient.execute(httppost);

            HttpEntity entity = response.getEntity();
            BufferedReader reader = new BufferedReader(new InputStreamReader(entity.getContent(), Consts.UTF_8));
            StringBuffer sb = new StringBuffer();
            String content = null;
            while ((content = reader.readLine()) != null) {
                sb.append(content);
            }
            result = sb.toString();

            return new HttpResponse(response.getStatusLine().getStatusCode(), result);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        } finally {
            if (httpclient != null) {
                httpclient.close();
            }
        }
    }

    /**
     * @param url
     * @param data
     * @param header 自定义请求头
     * @return
     * @throws Exception
     * @Description Content-Type:"application/json"类型的get请求
     */
    public static HttpResponse toPostForm(String url, Map<String, String> data, Map<String, String> header) throws Exception {
        CloseableHttpClient httpclient = null;
        String result = null;
        try {
            httpclient = HttpClients.custom()
                    .setConnectionManager(createConnectionManager())
                    .build();
            HttpPost httppost = new HttpPost(url);

            //设置请求头
            if (!CollectionUtils.isEmpty(header)) {
                header.keySet().stream().forEach(key -> {
                    httppost.setHeader(key, header.get(key));
                });
            }
            httppost.setHeader("Content-Type", "application/x-www-form-urlencoded");
            httppost.setHeader("charset", Consts.UTF_8.toString());
            List<NameValuePair> nvps = new ArrayList<NameValuePair>();
            // 设置包体参数。
            if (!CollectionUtils.isEmpty(data)) {
                data.keySet().forEach(e -> {
                    NameValuePair nvp = new BasicNameValuePair(e, data.get(e));
                    nvps.add(nvp);
                });
            }
            StringEntity se = new UrlEncodedFormEntity(nvps, Consts.UTF_8);
            httppost.setEntity(se);
            httppost.setConfig(getConfig());
            CloseableHttpResponse response = httpclient.execute(httppost);

            HttpEntity entity = response.getEntity();
            BufferedReader reader = new BufferedReader(new InputStreamReader(entity.getContent(), Consts.UTF_8));
            StringBuffer sb = new StringBuffer();
            String content = null;
            while ((content = reader.readLine()) != null) {
                sb.append(content);
            }
            result = sb.toString();

            return new HttpResponse(response.getStatusLine().getStatusCode(), result);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        } finally {
            if (httpclient != null) {
                httpclient.close();
            }
        }
    }


    /**
     * @param data
     * @param url
     * @return
     * @throws Exception
     * @Description POST请求-数据类型JSON
     */
    public static HttpResponse toPostText(String url, String data) throws Exception {
        CloseableHttpClient httpclient = null;
        String result = null;
        try {
            httpclient = HttpClients.custom()
                    .setConnectionManager(createConnectionManager())
                    .build();
            HttpPost httppost = new HttpPost(url);
            // 设置包体参数。
            StringEntity se = new StringEntity(data);
            se.setContentEncoding("UTF-8");
            se.setContentType("text/plain");
            httppost.setEntity(se);
            httppost.setConfig(getConfig());
            CloseableHttpResponse response = httpclient.execute(httppost);

            HttpEntity entity = response.getEntity();
            BufferedReader reader = new BufferedReader(new InputStreamReader(entity.getContent(), Consts.UTF_8));
            StringBuffer sb = new StringBuffer();
            String content = null;
            while ((content = reader.readLine()) != null) {
                sb.append(content);
            }
            result = sb.toString();

            return new HttpResponse(response.getStatusLine().getStatusCode(), result);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        } finally {
            if (httpclient != null) {
                httpclient.close();
            }
        }
    }

    /**
     * @param url
     * @param xmlStr
     * @return
     * @throws Exception
     * @Description Content-Type:"application/xml"类型的get请求
     */
    public static HttpResponse toPostXml(String url, String xmlStr) throws Exception {
        CloseableHttpClient httpclient = null;
        String result = null;
        try {
            httpclient = HttpClients.custom()
                    .setConnectionManager(createConnectionManager())
                    .build();
            HttpPost httppost = new HttpPost(url);
            httppost.setHeader("Content-Type", "application/xml");
            StringEntity se = new StringEntity(xmlStr, Consts.UTF_8);
            se.setContentEncoding("UTF-8");
            se.setContentType("application/xml");
            httppost.setEntity(se);
            httppost.setConfig(getConfig());
            CloseableHttpResponse response = httpclient.execute(httppost);

            HttpEntity entity = response.getEntity();
            BufferedReader reader = new BufferedReader(new InputStreamReader(entity.getContent(), Consts.UTF_8));
            StringBuffer sb = new StringBuffer();
            String content = null;
            while ((content = reader.readLine()) != null) {
                sb.append(content);
            }
            result = sb.toString();

            return new HttpResponse(response.getStatusLine().getStatusCode(), result);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        } finally {
            if (httpclient != null) {
                httpclient.close();
            }
        }
    }

    /**
     * @param url
     * @param xmlStr
     * @param header 自定义请求头
     * @return
     * @throws Exception
     * @Description Content-Type:"application/json"类型的get请求
     */
    public static HttpResponse toPostXml(String url, String xmlStr, Map<String, String> header) throws Exception {
        CloseableHttpClient httpclient = null;
        String result = null;
        try {
            httpclient = HttpClients.custom()
                    .setConnectionManager(createConnectionManager())
                    .build();
            HttpPost httppost = new HttpPost(url);
            //设置请求头
            if (!CollectionUtils.isEmpty(header)) {
                header.keySet().stream().forEach(key -> {
                    httppost.setHeader(key, header.get(key));
                });
            }
            httppost.setHeader("Content-Type", "application/xml");
            StringEntity se = new StringEntity(xmlStr, Consts.UTF_8);
            httppost.setEntity(se);
            httppost.setConfig(getConfig());
            CloseableHttpResponse response = httpclient.execute(httppost);

            HttpEntity entity = response.getEntity();
            BufferedReader reader = new BufferedReader(new InputStreamReader(entity.getContent(), Consts.UTF_8));
            StringBuffer sb = new StringBuffer();
            String content = null;
            while ((content = reader.readLine()) != null) {
                sb.append(content);
            }
            result = sb.toString();

            return new HttpResponse(response.getStatusLine().getStatusCode(), result);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        } finally {
            if (httpclient != null) {
                httpclient.close();
            }
        }
    }

    /**
     * @param url,headerMap,paramsStr
     * @param url
     * @return
     * @throws Exception
     * @Description PUT请求-数据类型JSON
     */
    public static HttpResponse toPutJson(String url, Map<String, String> headerMap, String paramsStr) throws Exception {
        //创建HTTPCLIENT链接对象
        CloseableHttpClient httpclient = null;
        try {
            httpclient = HttpClients.custom()
                    .setConnectionManager(createConnectionManager())
                    .build();
            HttpPut httpPut = new HttpPut(url);
            // 设置头部参数。
            if (headerMap != null) {
                headerMap.forEach((key, val) -> {
                    httpPut.setHeader(key, val);
                });
            }
            if (paramsStr != null) {
                StringEntity se = new StringEntity(paramsStr);
                se.setContentEncoding("UTF-8");
                se.setContentType("application/json");
                httpPut.setEntity(se);
            }
            CloseableHttpResponse response = null;
            String result = null;
            response = httpclient.execute(httpPut);

            HttpEntity entity = response.getEntity();
            BufferedReader reader = new BufferedReader(new InputStreamReader(entity.getContent(), Consts.UTF_8));
            StringBuffer sb = new StringBuffer();
            String content = null;
            while ((content = reader.readLine()) != null) {
                sb.append(content);
            }
            result = sb.toString();

            return new HttpResponse(response.getStatusLine().getStatusCode(), result);
        } catch (Exception e) {
            e.printStackTrace();
            throw new Exception(e.getMessage());
        } finally {
            if (httpclient != null) {
                httpclient.close();
            }
        }
    }

    /*************************非公用工具类方法，仅供个别游戏单独使用*****************************/

    /**
     * @param xmlStr
     * @param url
     * @param userAgent
     * @return
     * @throws Exception
     * @Description 匹配AG和AGIN游戏使用
     */
    public static String toPostXml(String xmlStr, String url, String userAgent) throws Exception {
        CloseableHttpClient httpclient = null;
        try {
            httpclient = HttpClients.custom()
                    .setConnectionManager(createConnectionManager())
                    .build();

            HttpPost httppost = new HttpPost(url);
            httppost.addHeader("User-Agent", userAgent);
            // 设置包体参数。
            StringEntity se = new StringEntity(xmlStr);
            se.setContentEncoding("UTF-8");
            se.setContentType("text/xml");
            httppost.setEntity(se);
            httppost.setConfig(getConfig());
            CloseableHttpResponse response = httpclient.execute(httppost);
            HttpEntity entity = response.getEntity();
            BufferedReader reader = new BufferedReader(new InputStreamReader(entity.getContent(), Consts.UTF_8));
            StringBuffer sb = new StringBuffer();
            String content = null;
            while ((content = reader.readLine()) != null) {
                sb.append(content);
            }
            return sb.toString();
        } catch (Exception e) {
            e.printStackTrace();
            throw new Exception(e.getMessage());
        } finally {
            if (httpclient != null) {
                httpclient.close();
            }
        }
    }

    /**
     * @param url
     * @param data
     * @param header 自定义请求头
     * @return
     * @throws Exception
     * @Description 匹配SW游戏使用
     */
    public static HttpResponse toPostFormResponseCode(String url, Map<String, String> data, Map<String, String> header) throws Exception {
        CloseableHttpClient httpclient = null;
        String result = null;
        try {
            httpclient = HttpClients.custom()
                    .setConnectionManager(createConnectionManager())
                    .build();
            HttpPost httppost = new HttpPost(url);

            //设置请求头
            if (!CollectionUtils.isEmpty(header)) {
                header.keySet().stream().forEach(key -> {
                    httppost.setHeader(key, header.get(key));
                });
            }
            httppost.setHeader("Content-Type", "application/x-www-form-urlencoded");
            httppost.setHeader("charset", Consts.UTF_8.toString());
            List<NameValuePair> nvps = new ArrayList<NameValuePair>();
            // 设置包体参数。
            if (!CollectionUtils.isEmpty(data)) {
                data.keySet().forEach(e -> {
                    NameValuePair nvp = new BasicNameValuePair(e, data.get(e));
                    nvps.add(nvp);
                });
            }
            StringEntity se = new UrlEncodedFormEntity(nvps, Consts.UTF_8);
            httppost.setEntity(se);
            httppost.setConfig(getConfig());
            CloseableHttpResponse response = httpclient.execute(httppost);
            //sw成功状态返回201
            HttpEntity entity = response.getEntity();
            BufferedReader reader = new BufferedReader(new InputStreamReader(entity.getContent(), Consts.UTF_8));
            StringBuffer sb = new StringBuffer();
            String content = null;
            while ((content = reader.readLine()) != null) {
                sb.append(content);
            }
            result = sb.toString();

            return new HttpResponse(response.getStatusLine().getStatusCode(), result);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        } finally {
            if (httpclient != null) {
                httpclient.close();
            }
        }
    }


    /**
     * @param url
     * @param data
     * @return
     * @throws Exception
     * @Description 匹配VR彩票使用
     */
    public static HttpResponse toPostJsonObjectMap(String url, Map<String, Object> data) throws Exception {
        CloseableHttpClient httpclient = null;
        String result = null;
        try {
            httpclient = HttpClients.custom()
                    .setConnectionManager(createConnectionManager())
                    .build();
            HttpPost httppost = new HttpPost(url);
            httppost.setHeader("Content-Type", "application/json");
            httppost.setHeader("charset", Consts.UTF_8.toString());
            StringEntity se = new StringEntity(JSONObject.toJSONString(data), Consts.UTF_8.toString());
            se.setContentType("application/json");
            httppost.setEntity(se);
            httppost.setConfig(getConfig());
            CloseableHttpResponse response = httpclient.execute(httppost);

            HttpEntity entity = response.getEntity();
            BufferedReader reader = new BufferedReader(new InputStreamReader(entity.getContent(), Consts.UTF_8));
            StringBuffer sb = new StringBuffer();
            String content = null;
            while ((content = reader.readLine()) != null) {
                sb.append(content);
            }
            result = sb.toString();

            return new HttpResponse(response.getStatusLine().getStatusCode(), result);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        } finally {
            if (httpclient != null) {
                httpclient.close();
            }
        }
    }

    public static HttpResponse toPostJsonObjectMapAuthentication(String url, Map<String, Object> data, String authentication) throws Exception {
        CloseableHttpClient httpclient = null;
        String result = null;
        try {
            httpclient = HttpClients.custom()
                    .setConnectionManager(createConnectionManager())
                    .build();
            HttpPost httppost = new HttpPost(url);
            httppost.setHeader("Content-Type", "application/json");
            httppost.setHeader("charset", Consts.UTF_8.toString());
            httppost.setHeader("authentication", authentication);
            StringEntity se = new StringEntity(JSONObject.toJSONString(data), Consts.UTF_8.toString());
            se.setContentType("application/json");
            httppost.setEntity(se);
            httppost.setConfig(getConfig());
            CloseableHttpResponse response = httpclient.execute(httppost);

            HttpEntity entity = response.getEntity();
            BufferedReader reader = new BufferedReader(new InputStreamReader(entity.getContent(), Consts.UTF_8));
            StringBuffer sb = new StringBuffer();
            String content = null;
            while ((content = reader.readLine()) != null) {
                sb.append(content);
            }
            result = sb.toString();

            return new HttpResponse(response.getStatusLine().getStatusCode(), result);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        } finally {
            if (httpclient != null) {
                httpclient.close();
            }
        }
    }
    private static PoolingHttpClientConnectionManager createConnectionManager() throws Exception {
        TrustManager tm = new X509TrustManager() {
            @Override
            public X509Certificate[] getAcceptedIssuers() {
                return null;
            }

            @Override
            public void checkServerTrusted(X509Certificate[] arg0, String arg1) throws CertificateException {
            }

            @Override
            public void checkClientTrusted(X509Certificate[] arg0, String arg1) throws CertificateException {
            }
        };
        SSLContext context = SSLContext.getInstance("TLS");
        context.init(null, new TrustManager[]{tm}, null);

        SSLConnectionSocketFactory socketFactory = new SSLConnectionSocketFactory(context,
                NoopHostnameVerifier.INSTANCE);

        Registry<ConnectionSocketFactory> socketFactoryRegistry = RegistryBuilder.<ConnectionSocketFactory>create()
                .register("http", PlainConnectionSocketFactory.INSTANCE).register("https", socketFactory).build();

        PoolingHttpClientConnectionManager connectionManager = new PoolingHttpClientConnectionManager(
                socketFactoryRegistry);
        return connectionManager;
    }

    /**
     * @return
     * @MehtodName getConfig
     * @Description 设置请求响应超时时间
     */
    public static RequestConfig getConfig() {
        RequestConfig requestConfig = RequestConfig.custom()
                .setConnectTimeout(15000)
                .setConnectionRequestTimeout(15000)
                .setSocketTimeout(15000)
                .build();
        return requestConfig;
    }
}
