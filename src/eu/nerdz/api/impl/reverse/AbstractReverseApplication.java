/*
 This file is part of NerdzApi-java.

    NerdzApi-java is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    NerdzApi-java is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with NerdzApi-java.  If not, see <http://www.gnu.org/licenses/>.

    (C) 2013 Marco Cilloni <marco.cilloni@yahoo.com>
*/

package eu.nerdz.api.impl.reverse;


import android.util.Log;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.*;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.*;
import java.util.*;

import eu.nerdz.api.Application;
import eu.nerdz.api.ContentException;
import eu.nerdz.api.HttpException;
import eu.nerdz.api.LoginException;
import eu.nerdz.api.UserInfo;
import eu.nerdz.api.WrongUserInfoTypeException;

/**
 * A Reverse abstract implementation of an Application.
 */
public abstract class AbstractReverseApplication implements Application {

    /**
     * Represents the domain in which all post/get requests are made.
     */

    public final static String PROTOCOL = "https";
    public final static String SUBDOMAIN = "www.";
    public final static String SUBDOMAIN_FULL = AbstractReverseApplication.SUBDOMAIN + "nerdz.eu";
    public final static String NERDZ_DOMAIN_NAME = AbstractReverseApplication.PROTOCOL + "://" + AbstractReverseApplication.SUBDOMAIN_FULL;
    /**
     *
     */
    private static final long serialVersionUID = -5784101258239287408L;

    private static byte[] encodeForm(Map<String, String> form) {
        StringBuilder sb = new StringBuilder();
        boolean first = true;

        for (Map.Entry<String, String> pair : form.entrySet()) {
            if (!first) {
                sb.append('&');
            }

            first = false;

            sb.append(pair.getKey());
            sb.append('=');
            try {
                sb.append(URLEncoder.encode(pair.getValue(), "UTF-8"));
            } catch (UnsupportedEncodingException ignored) {}
        }

        return sb.toString().getBytes();
    }

    private String mUserName;
    private int mUserId = -1;

    private AbstractReverseApplication() {
        CookieManager cookieManager = new CookieManager();
        cookieManager.setCookiePolicy(CookiePolicy.ACCEPT_ALL);

        CookieHandler.setDefault(cookieManager);
    }

    /**
     * the constructor takes care of logging into NERDZ. The cookies gathered through the login process remains in the thread's cookie store,
     *
     * @param user     username, unescaped
     * @param password password
     */
    protected AbstractReverseApplication(String user, String password) throws IOException, HttpException, LoginException {
        this();

        this.mUserName = user;

        String token;

        //fetch token.
        {
            String body = this.get();

            // token is hidden in an input tag. It's needed just for login/logout
            int start = body.indexOf("<input type=\"hidden\" value=\"") + 28;
            token = body.substring(start, start + 32);
        }

        Map<String, String> form = new HashMap<String, String>(4);
        form.put("setcookie", "on");
        form.put("username", user);
        form.put("password", password);
        form.put("tok", token);

        // login
        String responseBody = this.post("/pages/profile/login.json.php", form, AbstractReverseApplication.NERDZ_DOMAIN_NAME);

        //check for a wrong login.
        if (responseBody.contains("error")) {
            throw new LoginException();
        }
    }

    /**
     * This constructor creates an HttpClient with the already existing cookies.
     * Validity of loginData is not checked.
     *
     * @param loginData login data, stored in a ReverseUserInfo class.
     * @throws WrongUserInfoTypeException if loginData is not an AbstractReverseApplication.ReverseUserInfo instance
     */
    protected AbstractReverseApplication(UserInfo loginData) throws WrongUserInfoTypeException {
        this();

        ReverseUserInfo userInfo;

        try {
            userInfo = (ReverseUserInfo) loginData;
        } catch (ClassCastException e) {
            throw new WrongUserInfoTypeException("login data passed is not Reverse. ");
        }

        this.mUserName = userInfo.getUsername();
        CookieManager cookieManager = (CookieManager) CookieHandler.getDefault();
        CookieStore cookieStore = cookieManager.getCookieStore();

        try {
            URI uri = new URI(AbstractReverseApplication.NERDZ_DOMAIN_NAME);

            cookieStore.add(uri, userInfo.getNerdzIdCookie());
            cookieStore.add(uri, userInfo.getNerdzUCookie());
        } catch (URISyntaxException ignored) {}
    }

    /**
     * Checks if login data is valid.
     *
     * @return true if operations as logged user are possible. Exception if not
     * @throws IOException
     * @throws HttpException
     */
    @Override
    public boolean checkValidity() throws IOException, HttpException {

        System.out.println(((CookieManager) CookieHandler.getDefault()).getCookieStore().getCookies());

        if (this.get("/pages/pm/notify.json.php").contains("error")) {
            throw new LoginException("invalid token");
        }

        return true;

    }

    /**
     * Returns the username.
     *
     * @return a java.lang.String representing the username.
     */
    @Override
    public String getUsername() {
        return this.mUserName;
    }

    /**
     * Returns the NERDZ ID.
     *
     * @return an int representing the user ID
     */
    @Override
    public int getUserID() {

        if (this.mUserId > 0) {
            return this.mUserId;
        }

        CookieManager cookieManager = (CookieManager) CookieHandler.getDefault();
        for (HttpCookie cookie : cookieManager.getCookieStore().getCookies()) {
            if (cookie.getName().equals("nerdz_id")) {
                return (this.mUserId = Integer.parseInt(cookie.getValue()));
            }
        }

        return -1;
    }

    @Override
    public ReverseUserInfo getUserInfo() {
        return new ReverseUserInfo(this.mUserName);
    }

    /**
     * Executes a GET request on NERDZ.
     * This version returns the content of NERDZ_DOMAIN_NAME.
     *
     * @return a String containing the contents of NERDZ_DOMAIN_NAME.
     * @throws IOException
     * @throws HttpException
     */
    public String get() throws IOException, HttpException {
        return this.get("");
    }

    /**
     * Executes a GET request on NERDZ.
     * The given URL is automatically prepended with NERDZ_DOMAIN_NAME, so it should be something like /pages/pm/inbox.html.php.
     *
     * @param res     an address beginning with /
     * @return the content of NERDZ_DOMAIN_NAME + url
     * @throws IOException
     * @throws HttpException
     */
    public String get(String res) throws IOException, HttpException {

        URL url = new URL(AbstractReverseApplication.NERDZ_DOMAIN_NAME + res);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();

        String body = "";

        try {
            conn.setDoInput(true);
            InputStream in;

            try {
                 in = new BufferedInputStream(conn.getInputStream());
            } catch (IOException e) {
                String reason = new Scanner(conn.getErrorStream()).useDelimiter("\\A").next();

                throw new HttpException(conn.getResponseCode(), reason);
            }

            body = new Scanner(in).useDelimiter("\\A").next();
        } finally {
            conn.disconnect();
        }

        return body.trim();
    }

    /**
     * Issues a POST request to NERDZ.
     * The given URL is automatically prepended with NERDZ_DOMAIN_NAME, so it should be something like /pages/pm/inbox.html.php.
     * form is urlencoded by post, so it should not be encoded before.
     *
     * @param res  an address beginning with /
     * @param form a Map<String,String> that represents a form
     * @return a String containing the response body
     * @throws IOException
     * @throws HttpException
     */
    public String post(String res, Map<String, String> form) throws IOException, HttpException {
        return this.post(res, form, null);
    }

    /**
     * Issues a POST request to NERDZ.
     * The given URL is automatically prepended with NERDZ_DOMAIN_NAME, so it should be something like /pages/pm/inbox.html.php.
     * form is urlencoded by post, so it should not be encoded before.
     *
     * @param res     an address beginning with /
     * @param form    a Map<String,String> that represents a form
     * @param referer if not null, this string is used as the referer in the response.
     * @return a String containing the response body
     * @throws IOException
     * @throws HttpException
     */
    public String post(String res, Map<String, String> form, String referer) throws IOException, HttpException {

        URL url = new URL(AbstractReverseApplication.NERDZ_DOMAIN_NAME + res);

        byte[] encodedForm = AbstractReverseApplication.encodeForm(form);

        String body = "";
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();

        try {
            conn.setRequestMethod("POST");
            conn.setDoInput(true);
            conn.setDoOutput(true);
            conn.setFixedLengthStreamingMode(encodedForm.length);

            if (referer != null) {
                conn.setRequestProperty("Referer", referer);
            }

            OutputStream out;

            try {
                out = new BufferedOutputStream(conn.getOutputStream());
            } catch (IOException e) {
                String reason = new Scanner(conn.getErrorStream()).useDelimiter("\\A").next();

                throw new HttpException(conn.getResponseCode(), reason);
            }

            out.write(encodedForm);
            out.flush();

            InputStream in;

            try {
                 in = new BufferedInputStream(conn.getInputStream());
            } catch (IOException e) {
                InputStream errorStream = conn.getErrorStream();

                if (errorStream != null) {
                    throw new HttpException(conn.getResponseCode(), new Scanner(conn.getErrorStream()).useDelimiter("\\A").next());
                } else {
                    throw e;
                }
            }

            body = new Scanner(in).useDelimiter("\\A").next();
        } finally {
            conn.disconnect();
        }

        return body.trim();
    }

    @Override
    public void registerForPush(String service, String devId) throws IOException, HttpException, ContentException {
        Map<String,String> form = new HashMap<String, String>(2);
        form.put("service", service);
        form.put("deviceId", devId);
        String body = this.post("/push.php?action=subscribe",form);

        try {
            JSONObject jObj = new JSONObject(body);

            if(jObj.has("ERROR")) {
                throw new ContentException("Cannot subscribe: " + jObj.getString("ERROR"));
            }
        } catch (JSONException e) {
            throw new ContentException("Invalid json in response");
        }
    }

    @Override
    public void unregisterFromPush(String service, String devId) throws IOException, HttpException, ContentException {
        Map<String,String> form = new HashMap<String, String>(2);
        form.put("service", service);
        form.put("deviceId", devId);
        String body = this.post("/push.php?action=unsubscribe",form);

        try {
            JSONObject jObj = new JSONObject(body);

            if(jObj.has("ERROR")) {
                throw new ContentException("Cannot unsubscribe: " + jObj.getString("ERROR"));
            }
        } catch (JSONException e) {
            throw new ContentException("Invalid json in response");
        }
    }

    /**
     * Represents reverse login data.
     */
    public static class ReverseUserInfo implements UserInfo {

        /**
         *
         */
        private static final long serialVersionUID = -5768466751046728537L;

        // Workaround httpOnly (getter)
        private static boolean getHttpOnly(HttpCookie cookie) {
            try {
                Method getHttpOnly = HttpCookie.class.getMethod("getHttpOnly");

                return (Boolean) getHttpOnly.invoke(cookie);
            } catch (NoSuchMethodException ignored) {
                Field fieldHttpOnly = null;
                try {
                    fieldHttpOnly = cookie.getClass().getDeclaredField("httpOnly");
                    fieldHttpOnly.setAccessible(true);

                    return (Boolean) fieldHttpOnly.get(cookie);
                } catch (NoSuchFieldException e) {
                    e.printStackTrace();
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }

            return false;
        }

        // Workaround httpOnly (setter)
        private static void setHttpOnly(HttpCookie cookie, boolean httpOnly) {
            try {
                Method setHttpOnly = HttpCookie.class.getMethod("setHttpOnly", boolean.class);

                setHttpOnly.invoke(cookie, httpOnly);
            } catch (NoSuchMethodException ignored) {
                try {
                    Field fieldHttpOnly = cookie.getClass().getDeclaredField("httpOnly");
                    fieldHttpOnly.setAccessible(true);

                    fieldHttpOnly.set(cookie, httpOnly);
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                } catch (NoSuchFieldException e) {
                    e.printStackTrace();
                }
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }


        transient private String mUserName;
        transient private HttpCookie mNerdzU;
        transient private HttpCookie mNerdzId;

        /**
         * Creates an instance, an fills it with preprocessed loginData.
         * Assumes current CookieHandler is a CookieManager, and that login already happened.
         *
         * @param userName    a username
         * @throws ContentException
         */
        public ReverseUserInfo(String userName) throws ContentException {
            CookieManager cookieManager = (CookieManager) CookieHandler.getDefault();
            CookieStore cookieStore = cookieManager.getCookieStore();

            this.mUserName = userName;
            for (HttpCookie cookie : cookieStore.getCookies()) {
                if (cookie.getName().equals("nerdz_u")) {
                    this.mNerdzU = cookie;
                } else if (cookie.getName().equals("nerdz_id")) {
                    this.mNerdzId = cookie;
                }
            }

            if (this.mNerdzId == null || this.mNerdzU == null) {
                throw new ContentException("malformed cookie store");
            }
        }

        /**
         * Creates an instance using external data.
         *
         * @param userName a username
         * @param nerdzId  an id as a String
         * @param nerdzU   a nerdz_u token
         */
        public ReverseUserInfo(String userName, String nerdzId, String nerdzU) {
            this.init(userName, nerdzId, nerdzU);
        }

        /**
         * Here just for Externalizable.
         */

        @SuppressWarnings("unused")
        public ReverseUserInfo() {
        }

        @Override
        public int getNerdzID() {
            return Integer.parseInt(this.mNerdzId.getValue());
        }

        @Override
        public String getUsername() {
            return this.mUserName;
        }

        public String getNerdzU() {
            return this.mNerdzU.getValue();
        }

        @Override
        public void writeExternal(ObjectOutput outputStream) throws IOException {
            outputStream.writeObject(this.mUserName);
            outputStream.writeObject(this.mNerdzId.getValue());
            outputStream.writeObject(this.mNerdzU.getValue());

        }

        @Override
        public void readExternal(ObjectInput inputStream) throws IOException, ClassNotFoundException {
            this.init((String) inputStream.readObject(), (String) inputStream.readObject(), (String) inputStream.readObject());
        }

        public HttpCookie getNerdzUCookie() {
            System.out.printf("nerdz_u is http only: %b\n", ReverseUserInfo.getHttpOnly(this.mNerdzU));

            return this.mNerdzU;
        }

        public HttpCookie getNerdzIdCookie() {
            System.out.printf("nerdz_id is http only: %b\n", ReverseUserInfo.getHttpOnly(this.mNerdzId));

            return this.mNerdzId;
        }

        @Override
        public String toString() {
            return "Nerdz Username: " + this.mUserName + ", ID: " + this.getNerdzID() + ", NerdzU: " + this.mNerdzU.getValue();
        }

        private void init(String userName, String nerdzId, String nerdzU) {
            this.mUserName = userName;

            this.mNerdzU = new HttpCookie("nerdz_u", nerdzU);
            ReverseUserInfo.setHttpOnly(this.mNerdzU, true);
            this.mNerdzU.setMaxAge(1000L * 365L * 24L * 3600L * 1000L);
            this.mNerdzU.setPath("/");
            this.mNerdzU.setDomain('.' + AbstractReverseApplication.SUBDOMAIN_FULL);

            this.mNerdzId = new HttpCookie("nerdz_id", nerdzId);
            ReverseUserInfo.setHttpOnly(this.mNerdzId, true);
            this.mNerdzId.setMaxAge(1000L * 365L * 24L * 3600L * 1000L);
            this.mNerdzId.setPath("/");
            this.mNerdzId.setDomain('.' + AbstractReverseApplication.SUBDOMAIN_FULL);

        }

    }
}


