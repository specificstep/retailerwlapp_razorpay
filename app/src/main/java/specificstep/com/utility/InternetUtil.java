package specificstep.com.utility;

import android.util.Log;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.InputStream;
import java.net.URI;
import java.util.concurrent.TimeUnit;

import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class InternetUtil {

    public static String getServer_Data(String url) throws Exception {
        Log.i("InternetUtil", "URL : " + url);
        String server_data = "-1";
        DefaultHttpClient client = new DefaultHttpClient();
        URI uri = new URI(url);
        HttpGet method = new HttpGet(uri);
        HttpResponse res = client.execute(method);
        InputStream is = res.getEntity().getContent();
        server_data = generateString(is);
        Log.i("InternetUtil", "RESPONSE : " + server_data);
        return server_data;
    }

    // Post Response
    public static synchronized String getUrlData(String url, String parameter[], String parameterValues[])
            throws Exception {

        Log.i("InternetUtil", "URL : " + url);
        FormBody.Builder formBuilder = new FormBody.Builder();
        for (int i = 0; i < parameterValues.length; i++) {
            Log.i("InternetUtil", parameter[i] + " : " + parameterValues[i]);
            formBuilder.add(parameter[i],parameterValues[i]);
        }
        RequestBody formBody = formBuilder.build();

        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(120, TimeUnit.SECONDS)
                .writeTimeout(120, TimeUnit.SECONDS)
                .readTimeout(150, TimeUnit.SECONDS)
                .build();
        Request request = new Request.Builder()
                .url(url)
                .post(formBody)
                .build();

        Response response = client.newCall(request).execute();
        return response.body().string();

    }

    private static String generateString(InputStream stream) {
        try {
            StringBuffer sb = new StringBuffer();
            int cur;
            while ((cur = stream.read()) != -1) {
                sb.append((char) cur);
            }
            return String.valueOf(sb);
        }
        catch (Exception e) {
            Log.e("InternetUtil", "In generateString() " + e.getMessage());
            e.printStackTrace();
            return "0";
        }
    }
}
