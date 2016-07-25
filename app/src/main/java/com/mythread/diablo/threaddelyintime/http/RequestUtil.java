package com.mythread.diablo.threaddelyintime.http;


import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.conn.ssl.X509HostnameVerifier;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;

/**
 * 作者： shenyonghe689 on 16/4/12.
 */
public abstract class RequestUtil
{
    private Context mContext;

    /**
     * 使用线程池，防止频繁调用导致内存不足
     */
    private ExecutorService MYPOOL = Executors.newFixedThreadPool(8);
    /**
     * 网络连接超时 10秒
     */
    private   final int READTIMEOUT = 20 * 1000;
    /**
     * 网络连接超时 10秒
     */
    private final int CONNTIMEOUT = 10 * 1000;

    private HttpURLConnection conn;
    private String resulutNull = "请求结果异常";
    private String netNotConnect = "网络未连接";
    private String netException = "网络请求异常=>";

    public RequestUtil(Context context)
    {
        this.mContext = context;
    }

    private String doHttpsURLGet(final String urlPath, List<BaseKeyValueDto> headers, String
            bksName, String bksPAW) throws IOException
    {
        boolean isHttps = urlPath.startsWith("https");
        if (isHttps)
        {
            InputStream inputStream = mContext.getAssets().open(bksName);
            conn = trustOrNot(urlPath, inputStream, bksPAW);
        }
        return doHttpURLGet(urlPath, headers);
    }

    private String doHttpURLGet(final String urlPath, List<BaseKeyValueDto> headers) throws
            IOException
    {
        URL url = new URL(urlPath);
        //利用HttpURLConnection对象从网络中获取网页数据
        conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        //不使用缓存
        conn.setUseCaches(false);
        //设置请求超时
        conn.setConnectTimeout(CONNTIMEOUT);
        conn.setReadTimeout(READTIMEOUT);

        if ((headers != null) && headers.size() > 0)
        {
            for (int i = 0; i < headers.size(); i++)
            {
                conn.setRequestProperty(headers.get(i).getKey(), headers.get(i).getValue());
            }
        }

        int resultCode = conn.getResponseCode();
        System.out.println("<======resultCode========>" + resultCode);
        if (resultCode == HttpURLConnection.HTTP_OK)
        {
            InputStream is = conn.getInputStream();
            InputStreamReader inputStreamReader = new InputStreamReader(is);
            BufferedReader br = new BufferedReader(inputStreamReader);
            String response = "";
            String readLine = null;
            while ((readLine = br.readLine()) != null)
            {
                response = response + readLine;
            }
            br.close();
            inputStreamReader.close();
            is.close();
            conn.disconnect();
            conn = null;
            System.out.println("httpsGet===>" + response);
            return response;
        } else
        {
            OnHttpsGetAndPostError(netException);
            return null;
        }
    }

    /**
     * httpurlconnecton get请求，自动判断是否是https请求，如果是 还可以通过参数
     *
     * @param urlPath 请求地址
     * @param headers 请求头列表
     * @param bksName bks文件名，将bks放到assets目录(如果是http请求 设置为null）
     * @param bksPAW  bks文件对应的密码(如果是http请求 设置为null）
     */
    public void doHttpsGet(final String urlPath, final List<BaseKeyValueDto> headers, final String
            bksName, final String bksPAW)
    {
        MYPOOL.execute(new Runnable()
        {

            @Override
            public void run()
            {
                try
                {
                    if (isNetworkAvailable(mContext))
                    {
                        String result = doHttpsURLGet(urlPath, headers, bksName, bksPAW);
                        if (result != null)
                        {
                            OnHttpsGetAndPostSucceed(result);
                        } else
                        {
                            OnHttpsGetAndPostError(resulutNull);
                        }
                    } else
                    {
                        OnHttpsGetAndPostError(netNotConnect);
                    }
                } catch (Exception e)
                {
                    e.printStackTrace();
                    OnHttpsGetAndPostError(netException+e.getMessage());
                }
            }
        });

    }

    /**
     * httpurlconnecton get请求，自动判断是否是https请求，如果是 还可以通过参数
     *
     * @param urlPath 请求地址
     * @param headers 请求头列表
     */
    public void doHttpGet(final String urlPath, final List<BaseKeyValueDto> headers)
    {
        MYPOOL.execute(new Runnable()
        {

            @Override
            public void run()
            {
                try
                {
                    if (isNetworkAvailable(mContext))
                    {
                        String result = doHttpURLGet(urlPath, headers);
                        if (result != null)
                        {
                            OnHttpsGetAndPostSucceed(result);
                        } else
                        {
                            OnHttpsGetAndPostError(resulutNull);
                        }
                    } else
                    {
                        OnHttpsGetAndPostError(netNotConnect);
                    }
                } catch (Exception e)
                {
                    e.printStackTrace();
                    OnHttpsGetAndPostError(netException+e.getMessage());
                }
            }
        });

    }

    private String doHttpsURLPost(final String urlPath, String parameter,
                                  List<BaseKeyValueDto> headers, String bksName,
                                  String bksPAW) throws IOException
    {
        InputStream inputStream = mContext.getAssets().open(bksName);
        conn = trustOrNot(urlPath, inputStream, bksPAW);
        return doHttpURLPost(urlPath, parameter, headers);
    }

    private String doHttpURLPost(final String urlPath, String parameter,
                                 List<BaseKeyValueDto> headers) throws IOException
    {
        URL url = new URL(urlPath);
        //利用HttpURLConnection对象从网络中获取网页数据
        conn = (HttpURLConnection) url.openConnection();
        conn.setDoInput(true);
        conn.setDoOutput(true);
        conn.setRequestMethod("POST");
        conn.setUseCaches(false);
        conn.setInstanceFollowRedirects(false);
        //设置请求超时
        conn.setConnectTimeout(CONNTIMEOUT);
        conn.setReadTimeout(READTIMEOUT);

        if ((headers != null) && headers.size() > 0)
        {
            for (int i = 0; i < headers.size(); i++)
            {
                conn.setRequestProperty(headers.get(i).getKey(), headers.get(i).getValue());
            }
        }
        conn.connect();
        if (!StringUtil.isNullOrEmpty(parameter))
        {
            OutputStream outputStream = conn.getOutputStream();
            DataOutputStream out = new DataOutputStream(outputStream);
            out.writeBytes(parameter);
            out.flush();
            out.close();
            outputStream.close();
        }
        int resultCode = conn.getResponseCode();
        System.out.println("<======resultCode========>" + resultCode);
        if (resultCode == HttpURLConnection.HTTP_OK)
        {
            InputStream is = conn.getInputStream();
            InputStreamReader inputStreamReader = new InputStreamReader(is);
            BufferedReader br = new BufferedReader(inputStreamReader);

            String response = "";
            String readLine = null;
            while ((readLine = br.readLine()) != null)
            {
                response = response + readLine;
            }
            is.close();
            br.close();
            inputStreamReader.close();
            conn.disconnect();
            System.out.println("httpsPost===>" + response);
            return response;
        } else
        {
            OnHttpsGetAndPostError(netException);
            return null;
        }
    }

    /**
     * httpurlconnecton post请求 自动判断是否是https请求
     *
     * @param urlPath   请求地址
     * @param parameter 请求参数
     * @param headers   请求头列表
     * @param bksName   bks文件名，将bks放到assets目录(如果是http请求 设置为null）
     * @param bksPAW    bks文件对应的密码(如果是http请求 设置为null）
     */
    public void doHttpsPost(final String urlPath, final String parameter,
                            final List<BaseKeyValueDto> headers, final String bksName,
                            final String bksPAW)
    {
        MYPOOL.execute(new Runnable()
        {

            @Override
            public void run()
            {
                try
                {
                    if (isNetworkAvailable(mContext))
                    {
                        String result = doHttpsURLPost(urlPath, parameter, headers, bksName,
                                bksPAW);
                        if (result != null)
                        {
                            OnHttpsGetAndPostSucceed(result);
                        } else
                        {
                            OnHttpsGetAndPostError(resulutNull);
                        }
                    } else
                    {
                        OnHttpsGetAndPostError(netNotConnect);
                    }
                } catch (Exception e)
                {
                    e.printStackTrace();
                    OnHttpsGetAndPostError(netException+e.getMessage());
                }
            }
        });
    }

    /**
     * httpurlconnecton post请求 自动判断是否是https请求
     *
     * @param urlPath   请求地址
     * @param parameter 请求参数
     * @param headers   请求头列表
     */
    public void doHttpPost(final String urlPath, final String parameter,
                           final List<BaseKeyValueDto> headers)
    {
        MYPOOL.execute(new Runnable()
        {

            @Override
            public void run()
            {
                try
                {
                    if (isNetworkAvailable(mContext))
                    {
                        String result = doHttpURLPost(urlPath, parameter, headers);
                        if (result != null)
                        {
                            OnHttpsGetAndPostSucceed(result);
                        } else
                        {
                            OnHttpsGetAndPostError(resulutNull);
                        }
                    } else
                    {
                        OnHttpsGetAndPostError(netNotConnect);
                    }
                } catch (Exception e)
                {
                    e.printStackTrace();
                    OnHttpsGetAndPostError(netException+e.getMessage());
                }
            }
        });
    }

    /**
     * 动态配置是否验证https的证书
     *
     * @param urlPath
     * @param inputStream
     * @param bksPAW
     * @return
     */
    private HttpURLConnection trustOrNot(String urlPath, InputStream
            inputStream, String bksPAW)
    {
        HttpURLConnection conn = null;
        try
        {
            conn = verifyCertificste(urlPath, inputStream, bksPAW);
        } catch (Exception e)
        {
            e.printStackTrace();
            System.out.println("证书校验异常" + e.getMessage());
        }
        return conn;
    }

    /**
     * 检测当的网络（WLAN、3G/2G）状态
     *
     * @param context Context
     * @return true 表示网络可用
     */
    private boolean isNetworkAvailable(Context context)
    {
        ConnectivityManager connectivity = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivity != null)
        {
            NetworkInfo info = connectivity.getActiveNetworkInfo();
            if (info != null && info.isConnected())
            {
                // 当前网络是连接的
                if (info.getState() == NetworkInfo.State.CONNECTED)
                {
                    // 当前所连接的网络可用
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * https证书校验
     *
     * @param urlPath
     * @param inputStream
     * @param bksPAW
     * @return
     */
    private HttpsURLConnection verifyCertificste(String urlPath, InputStream inputStream,
                                                 String bksPAW) throws
            NoSuchAlgorithmException, KeyManagementException, IOException, CertificateException,
            KeyStoreException, UnrecoverableKeyException
    {
        URL url = new URL(urlPath);
        HttpsURLConnection urlConnection = (HttpsURLConnection) url.openConnection();

        //通过指定类型inputsream和提供者获取信任密钥库（KeyStore）实例
        String keyStoreType = KeyStore.getDefaultType();
        KeyStore keyStore = KeyStore.getInstance(keyStoreType);
        //信任密钥库实例使用约定的密码加载（Load）密钥库文件,加密码确保keyStore完整性
        keyStore.load(inputStream, bksPAW.toCharArray());

        //信任密钥管理器工厂实例使用约定的密码和密钥库进行初始化（Initialize）
        String tmfAlgorithm = TrustManagerFactory.getDefaultAlgorithm();
        TrustManagerFactory tmf = TrustManagerFactory.getInstance(tmfAlgorithm);
        tmf.init(keyStore);

        //初始化KeyManagerFactory用keyStore以及证书密码
        KeyManagerFactory kmf = KeyManagerFactory.getInstance("X509");
        kmf.init(keyStore, bksPAW.toCharArray());

        SSLContext sslContext = SSLContext.getInstance("TLS");
        sslContext.init(kmf.getKeyManagers(), tmf.getTrustManagers(), null);

        //不验证主机名，允许访问所有主机
        X509HostnameVerifier hostnameVerifier = SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER;
        HttpsURLConnection.setDefaultHostnameVerifier(hostnameVerifier);
        urlConnection.setSSLSocketFactory(sslContext.getSocketFactory());

        return urlConnection;
    }

    /**
     * 请求数据成功。
     *
     * @param result 返回请求到的数据
     */
    public abstract void OnHttpsGetAndPostSucceed(final String result);

    /**
     * 请求失败。
     */
    public abstract void OnHttpsGetAndPostError(final String errorInfo);
}