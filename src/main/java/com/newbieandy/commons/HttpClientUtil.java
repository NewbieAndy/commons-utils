package com.newbieandy.commons;

import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicHeader;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by machao on 2016/6/21.
 */
public class HttpClientUtil {

    /**
     * 模拟post请求
     *
     * @param url    请求路径
     * @param params 请求参数
     * @return
     */
    public static String post(String url, Map<String, String> params) {
        return post(url, params, null);
    }

    /**
     * 模拟post代理提交
     *
     * @param url       请求URL
     * @param params    请求参数
     * @param proxyIp   代理IP
     * @param proxyPost 代理端口
     * @return
     */
    public static String proxyPost(String url, Map<String, String> params, String proxyIp, Integer proxyPost) {
        return exeProxyPost(url, params, null, proxyIp, proxyPost);
    }

    /**
     * 模拟post请求
     *
     * @param url    请求路径
     * @param params 请求参数
     * @param heads  请求头
     * @return
     */
    public static String post(String url, Map<String, String> params, Map<String, String> heads) {
        return exePost(url, params, heads);

    }

    /**
     * 模拟代理Post请求
     *
     * @param url       请求Url
     * @param params    请求参数
     * @param heads     请求头
     * @param proxyIp   代理IP
     * @param proxyPost 代理端口
     * @return
     */
    public static String proxyPost(String url, Map<String, String> params, Map<String, String> heads, String proxyIp, Integer proxyPost) {
        return exeProxyPost(url, params, heads, proxyIp, proxyPost);
    }

    /**
     * 执行请求
     *
     * @param url    请求URL
     * @param params 请求参数
     * @param heads  请求头
     * @return
     */
    private static String exePost(String url, Map<String, String> params, Map<String, String> heads) {
        return exeProxyPost(url, params, heads, null, null);
    }

    /**
     * 执行请求
     *
     * @param url    请求URL
     * @param params 请求参数
     * @param heads  请求头
     * @return
     */
    private static String exeProxyPost(String url, Map<String, String> params, Map<String, String> heads, String proxyIp, Integer proxyPost) {
        //获取httpClient实例
        CloseableHttpClient httpClient = HttpClients.createDefault();
        //创建HttpPost
        HttpPost httpPost = new HttpPost(url);
        //设置代理
        if (null != proxyPost && null != proxyIp) {
            HttpHost proxy = new HttpHost(proxyIp, proxyPost, "http");
            RequestConfig config = RequestConfig.custom().setProxy(proxy).build();
            httpPost.setConfig(config);
        }

        //设置参数
        List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
        if (null != params && !params.isEmpty()) {
            for (Map.Entry<String, String> entry : params.entrySet()) {
                BasicNameValuePair basicNameValuePair = new BasicNameValuePair(entry.getKey(), entry.getValue());
                nameValuePairs.add(basicNameValuePair);
            }
        }
        //设置请求头
        if (null != heads && !heads.isEmpty()) {
            for (Map.Entry<String, String> entry : heads.entrySet()) {
                httpPost.setHeader(new BasicHeader(entry.getKey(), entry.getValue()));
            }
        }

        UrlEncodedFormEntity formEntity;
        try {
            //创建表单参数实体
            formEntity = new UrlEncodedFormEntity(nameValuePairs, "UTF-8");
            //设置参数
            httpPost.setEntity(formEntity);
            //发送请求
            System.out.println("POST请求:url->" + url + "开始...");
            CloseableHttpResponse httpResponse = httpClient.execute(httpPost);
            //获取响应实体
            try {
                return entryToString(httpResponse.getEntity(), "UTF-8");
            } finally {
                //关闭响应
                httpResponse.close();
            }
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            //释放连接
            try {
                httpClient.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }


    /**
     * 模拟GET请求
     *
     * @param url
     * @return
     */
    public static String get(String url) {
        return get(url, null, null);
    }

    /**
     * 模拟get请求
     *
     * @param url
     * @param params
     * @return
     */
    public static String get(String url, Map<String, String> params) {
        return get(url, params, null);
    }

    /**
     * 模拟get请求
     *
     * @param url    请求url
     * @param params 请求参数
     * @param heads  请求头
     * @return
     */
    public static String get(String url, Map<String, String> params, Map<String, String> heads) {
        if (null != params && !params.isEmpty()) {
            StringBuilder sb = new StringBuilder(url).append("?");
            for (Map.Entry<String, String> entry : params.entrySet()) {
                sb.append(entry.getKey()).append("=").append(entry.getValue()).append("&");
            }
            url = sb.toString();
            url = url.substring(0, url.length() - 1);
        }
        return exeGet(url, heads);
    }

    /**
     * 执行get请求
     *
     * @param url
     * @return
     */
    private static String exeGet(String url, Map<String, String> heads) {
        CloseableHttpClient httpClient = HttpClients.createDefault();
        try {
            //创建httpGet
            HttpGet httpGet = new HttpGet(url);

            //设置请求头
            if (null != heads && !heads.isEmpty()) {
                for (Map.Entry<String, String> entry : heads.entrySet()) {
                    httpGet.setHeader(new BasicHeader(entry.getKey(), entry.getValue()));
                }
            }
            CloseableHttpResponse httpResponse = httpClient.execute(httpGet);
            //获取相应实体
            try {
                return entryToString(httpResponse.getEntity(), "GBK");
            } finally {
                httpResponse.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            //关闭连接
            try {
                httpClient.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    /**
     * 下载图片
     *
     * @param url
     * @param dirBasePath
     * @param fileName
     */
    public static void downLoadImg(String url, String dirBasePath, String fileName) {
        CloseableHttpClient httpClient = HttpClients.createDefault();
        try {
            //创建httpGet
            HttpGet httpGet = new HttpGet(url);

            CloseableHttpResponse httpResponse = httpClient.execute(httpGet);
            //获取相应实体
            try {
                HttpEntity entity = httpResponse.getEntity();
                if (entity != null) {
                    InputStream input = entity.getContent();
                    //文件名
                    fileName = dirBasePath + "\\" + fileName;
                    OutputStream output = new FileOutputStream(fileName);
                    byte[] buffer = new byte[1024];
                    int len = -1;
                    while ((len = input.read(buffer)) != -1) {
                        output.write(buffer, 0, len);
                    }
                    output.flush();
                }
            } finally {
                httpResponse.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            //关闭连接
            try {
                httpClient.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 实体转字符串
     *
     * @param httpEntity
     * @return
     */
    private static String entryToString(HttpEntity httpEntity, String charset) {
        if (null != httpEntity) {
            try {
                if (null == charset) {
                    charset = "UTF-8";
                }
//                return EntityUtils.toString(httpEntity, "UTF-8");
                return EntityUtils.toString(httpEntity, charset);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }
}
