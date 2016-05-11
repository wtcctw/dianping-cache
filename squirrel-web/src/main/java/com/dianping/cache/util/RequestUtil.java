package com.dianping.cache.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;

public class RequestUtil {

    private static final Logger logger = LoggerFactory.getLogger(RequestUtil.class);
	
	public static String getUsername(){
		RequestAttributes ra = RequestContextHolder.getRequestAttributes();
		if(ra != null){
			
			HttpServletRequest re = ((ServletRequestAttributes) ra).getRequest();
			if(re != null){
				
				String tmpusername = re.getRemoteUser();
				if (tmpusername == null) {
					return null;
				} else {
					String[] userinfo = tmpusername.split("\\|");
					return "".equals(userinfo[0])? "cache" : userinfo[0];
				}
			}
		}
		return null;
	}
	
    /**
     * 向指定URL发送GET方法的请求
     * 
     * @param url
     *            发送请求的URL
     * @param param
     *            请求参数，请求参数应该是 name1=value1&name2=value2 的形式。
     * @return URL 所代表远程资源的响应结果
     */
    public static String sendGet(String url, String param) {
        return sendGet(url,param,null).getContent();
    }

    public static HTTPResponse sendGet(String url, String param, Map<String,String> properties) {
        String result = "";
        BufferedReader in = null;
        HTTPResponse response = new HTTPResponse();
        try {
            String urlNameString = url;

            if(param != null)
                urlNameString = urlNameString + "?" + param;

            URL realUrl = new URL(urlNameString);
            // 打开和URL之间的连接
            HttpURLConnection connection = (HttpURLConnection)realUrl.openConnection();
            // 设置通用的请求属性
            connection.setConnectTimeout(1000);
            connection.setReadTimeout(5000);
            connection.setRequestProperty("accept", "*/*");
            connection.setRequestProperty("connection", "Keep-Alive");
            connection.setRequestProperty("user-agent",
                    "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1;SV1)");
            if(properties != null){
                for(Map.Entry<String,String> entry : properties.entrySet()){
                    connection.setRequestProperty(entry.getKey(),entry.getValue());
                }
            }
            // 建立实际的连接
            connection.connect();
            int responseCode = connection.getResponseCode();
            response.setCode(responseCode);
            if(responseCode != 200){
                in = new BufferedReader(
                        new InputStreamReader(connection.getErrorStream()));

            } else {
                in = new BufferedReader(
                        new InputStreamReader(connection.getInputStream()));
            }
            String line;
            while ((line = in.readLine()) != null) {
                result += line;
            }
            response.setContent(result);
        } catch (Exception e) {
            logger.error("sent get method error.",e);
        }
        // 使用finally块来关闭输入流
        finally {
            try {
                if (in != null) {
                    in.close();
                }
            } catch (Exception e2) {
                e2.printStackTrace();
            }
        }
        return response;
    }

    /**
     * 向指定 URL 发送POST方法的请求
     * 
     * @param url
     *            发送请求的 URL
     * @param param
     *            请求参数，请求参数应该是 name1=value1&name2=value2 的形式。
     * @return 所代表远程资源的响应结果
     */
    public static String sendPost(String url, String param) {
        return sendPost(url,param,null).getContent();
    }

    public static HTTPResponse sendPost(String url, String param, Map<String,String> properties) {
        PrintWriter out = null;
        BufferedReader in = null;
        HTTPResponse response = new HTTPResponse();
        String result = "";
        try {
            URL realUrl = new URL(url);
            HttpURLConnection conn = (HttpURLConnection) realUrl.openConnection();

            conn.setConnectTimeout(2000);
            conn.setReadTimeout(10000);

            if(properties != null){
                for(Map.Entry<String,String> entry : properties.entrySet()){
                    conn.setRequestProperty(entry.getKey(),entry.getValue());
                }
            } else {
                conn.setRequestProperty("Content-Type", "application/json");
                conn.setRequestProperty("connection", "Keep-Alive");
            }
            conn.setDoOutput(true);
            conn.setDoInput(true);
            out = new PrintWriter(conn.getOutputStream());

            out.print(param);
            out.flush();

            int responseCode = conn.getResponseCode();
            response.setCode(responseCode);
            if(responseCode != 200){
                in = new BufferedReader(
                        new InputStreamReader(conn.getErrorStream()));

            } else {
                in = new BufferedReader(
                        new InputStreamReader(conn.getInputStream()));
            }
            String line;
            while ((line = in.readLine()) != null) {
                result += line;
            }
            response.setContent(result);
        } catch (Exception e) {
            logger.error("send post error" , e);
        }
        finally{
            try{
                if(out!=null){
                    out.close();
                }
                if(in!=null){
                    in.close();
                }
            }
            catch(IOException ex){
                ex.printStackTrace();
            }
        }
        return response;
    }

    public static class HTTPResponse {
        private int code;
        private String content;

        public int getCode() {
            return code;
        }

        public void setCode(int code) {
            this.code = code;
        }

        public String getContent() {
            return content;
        }

        public void setContent(String content) {
            this.content = content;
        }

        @Override
        public String toString() {
            return "HTTPResponse{" +
                    "code=" + code +
                    ", content='" + content + '\'' +
                    '}';
        }
    }




}
