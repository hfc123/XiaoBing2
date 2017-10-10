package com.cheerchip.xiaobing2;

import android.util.Log;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.CookieStore;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Cookie;
import okhttp3.CookieJar;
import okhttp3.FormBody;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * Created by noname on 2017/10/10.
 */

public class OkHttpUtils {
    private static final String upload_picBase64_api = "http://kan.msxiaobing.com/APi/Image/UploadBase64";
    private static final String ice_api = "http://kan.msxiaobing.com/Api/ImageAnalyze/Process?service=yanzhi&tid=8b9a88049f9c4f26b4da60afc9d70ef4";
    private static final String ice_page = "http://kan.msxiaobing.com/ImageGame/Portal?task=yanzhi";

    public static String getUploadPicResult(String imgdataBase64) {
        StringBuffer sb=new StringBuffer();
        try {
            URL realUrl = new URL(upload_picBase64_api);
            HttpURLConnection conn = (HttpURLConnection) realUrl.openConnection();
            conn.setDoOutput(true);
            conn.setDoInput(true);
            conn.setUseCaches(false);
            conn.setRequestMethod("POST");

            conn.setRequestProperty("Charset", "UTF-8");
            conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            conn.setRequestProperty("Connection", "Keep-Alive");// 维持长连接

            DataOutputStream dataOutputStream = new DataOutputStream(conn.getOutputStream());
            dataOutputStream.writeBytes(imgdataBase64);
            dataOutputStream.flush();
            dataOutputStream.close();

            String readLine=new String();
            BufferedReader responseReader=new BufferedReader(new InputStreamReader(conn.getInputStream(),"UTF-8"));
            while((readLine=responseReader.readLine())!=null){
                sb.append(readLine).append("\n");
            }
            responseReader.close();
            Log.e("getUploadPicResult",sb.toString());

        } catch (Exception e) {
            e.printStackTrace();
        }
        finally {
            return sb.toString();
        }
    }
    public static  String analyzeImage(String jsonResultPic) {
        DefaultHttpClient httpClient=new DefaultHttpClient();
        DefaultHttpClient httpClient2=new DefaultHttpClient();
        HttpPost httpPost1=new HttpPost(ice_api);
        HttpPost httpPost2=new HttpPost(ice_page);

        JSONObject jasonObject = null;
        try {
            jasonObject = new JSONObject(jsonResultPic);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        Map<String,String> contentImgUrl = new HashMap<>();
        try {
        contentImgUrl.put("Host",jasonObject.getString("Host"));
        contentImgUrl.put("Url",jasonObject.getString("Url"));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        String imgUrl = contentImgUrl.get("Host") + contentImgUrl.get("Url");
        System.out.print(jsonResultPic);
        List<NameValuePair> form=new ArrayList<NameValuePair>();

        form.add(new BasicNameValuePair("MsgId",String.valueOf(System.currentTimeMillis())+"063"));
        form.add(new BasicNameValuePair("CreateTime",String.valueOf(Calendar.getInstance().getTimeInMillis())));
        form.add(new BasicNameValuePair("Content[imageUrl]",imgUrl));

        try {

            httpPost1.addHeader("Referer","http://kan.msxiaobing.com/V3/Portal?task=yanzhi&ftid=91ac082228fb48739f12c66ee3a3fee0");
            httpPost1.setEntity(new UrlEncodedFormEntity(form, "UTF-8"));

            HttpResponse pageResponse=httpClient.execute(httpPost2);
            CookieStore cookieStore=httpClient.getCookieStore();
            httpClient2.setCookieStore(cookieStore);
            HttpResponse response=httpClient2.execute( httpPost1);
            return EntityUtils.toString(response.getEntity());
        }catch (Exception e){
            e.printStackTrace();
        }
        return null;

    }
    public static int findScoreFromString(String jsonResultPic) {
        Pattern pattern = Pattern.compile("\\d+[.]\\d+");
        Matcher m = pattern.matcher(jsonResultPic);
        if (m.find()) {
            System.out.println("analyzeResult=" + m.group());
            double temp = Double.valueOf(m.group());
            int score = (int) (temp * 10);
            return score;
        } else {
            return 0;
        }
    }
   /* public String analyzeImage(String jsonResultPic){
        CookieJar cookieJar = new CookieJar() {
            //Cookie缓存区
            private final Map<String, List<Cookie>> cookiesMap = new HashMap<String, List<Cookie>>();
            @Override
            public void saveFromResponse(HttpUrl arg0, List<Cookie> arg1) {
                // TODO Auto-generated method stub
                //移除相同的url的Cookie
                String host = arg0.host();
                List<Cookie> cookiesList = cookiesMap.get(host);
                if (cookiesList != null){
                    cookiesMap.remove(host);
                }
                //再重新天添加
                cookiesMap.put(host,arg1);
            }

            @Override
            public List<Cookie> loadForRequest(HttpUrl arg0) {
                // TODO Auto-generated method stub
                List<Cookie> cookiesList = cookiesMap.get(arg0.host());
                //注：这里不能返回null，否则会报NULLException的错误。
                //原因：当Request 连接到网络的时候，OkHttp会调用loadForRequest()
                return cookiesList != null ? cookiesList : new ArrayList<Cookie>();
            }
        };

        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(5, TimeUnit.SECONDS)
                .cookieJar(cookieJar)
                .readTimeout(10, TimeUnit.SECONDS)
                .build();
        String imgUrl="";
        try {
            JSONObject jasonObject = new JSONObject(jsonResultPic);
            imgUrl =jasonObject.getString("Host") + jasonObject.getString("Url");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        FormBody formBody=new FormBody.Builder()
                .add("MsgId",String.valueOf(System.currentTimeMillis())+"063")
                .add("CreateTime",String.valueOf(Calendar.getInstance().getTimeInMillis()))
                .add("Content","["+imgUrl+"]")
                .build();
      //  RequestBody requestBody=RequestBody.create(formBody)
        client.cookieJar();
        Request request1=new Request.Builder()
                .post(null)
                .url(ice_page)
                .build();
        client.newCall(request1).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {

            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {

            }
        });
        Request request=new Request.Builder()
                .post(formBody)
                .url(ice_api)
                .build();
        client.newCall(request)
                .enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {

            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {

            }
        });


        return null;
    }*/
}
