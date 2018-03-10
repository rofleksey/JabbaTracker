import org.json.JSONArray;
import org.json.JSONObject;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;

public class VKApi {
    private static final String client_id = "5312384";
    private static final String scope = "messages, friends, docs";
    private static final String redirect_uri = "http://oauth.vk.com/blank.html";
    private static final String display = "mobile";
    private static final String response_type = "token";
    private static final Object listenersLock = new Object();
    public int selfID = -1;
    private String access_token, email, pass;

    synchronized String getAccessToken() throws IOException {
        Connection.Response authRes = Jsoup.connect("https://oauth.vk.com/authorize")
                .data("client_id", client_id)
                .data("scope", scope)
                .data("redirect_uri", redirect_uri)
                .data("display", display)
                .data("response_type", response_type)
                .userAgent("Mozilla").followRedirects(true).method(Connection.Method.POST).execute();
        Document auth = authRes.parse();
        String ip_h = auth.getElementsByAttributeValue("name", "ip_h").get(0).attr("value");
        String lg_h = auth.getElementsByAttributeValue("name", "lg_h").get(0).attr("value");
        String to = auth.getElementsByAttributeValue("name", "to").get(0).attr("value");
        String _origin = auth.getElementsByAttributeValue("name", "_origin").get(0).attr("value");
        Connection.Response login1 = Jsoup.connect("https://login.vk.com")
                .data("act", "login")
                .data("soft", "1")
                .data("utf8", "1")
                .data("_origin", _origin)
                .data("ip_h", ip_h)
                .data("lg_h", lg_h)
                .data("to", to)
                .data("email", email)
                .data("pass", pass)
                .userAgent("Mozilla").cookies(authRes.cookies()).method(Connection.Method.POST).execute();
        Document login1Doc = login1.parse();
        if (login1Doc.getElementsByTag("form").size() == 0) {
            return login1Doc.location().split("#")[1].split("&")[0].split("=")[1];
        } else {
            String req = login1Doc.getElementsByTag("form").attr("action");
            Connection.Response access = Jsoup.connect(req).userAgent("Mozilla").cookies(login1.cookies()).method(Connection.Method.POST).execute();
            return access.parse().location().split("#")[1].split("&")[0].split("=")[1];
        }
    }

    static byte[] downloadFile(String url) throws IOException {
        return Jsoup.connect(url).ignoreContentType(true).execute().bodyAsBytes();
    }

    static byte[] forceDownload(String url) {
        while (true) {
            try {
                return downloadFile(url);
            } catch (IOException e) {
                System.err.println("error downloading file " + url);
            }
        }
    }

    VKApi(String email, String pass) throws VKException {
        this.email = email;
        this.pass = pass;
        try {
            setAccessToken(getAccessToken());
            selfID = request("users.get").execute().getObject().getJSONArray("response").getJSONObject(0).getInt("id");
        } catch (IOException ex) {
            ex.printStackTrace();
            throw new VKException("Ошибка установления соединения");
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new VKException("Неправильный логин или пароль");
        }
    }

    static class VKException extends Exception {
        String caption;

        VKException(String str) {
            super(str);
            caption = str;
        }

        @Override
        public String toString() {
            return caption;
        }
    }

    void setAccessToken(String access_token) {
        this.access_token = access_token;
    }

    VKRequest request(String method) {
        return new VKRequest(method);
    }

    class VKRequest {
        Connection c;

        VKRequest(String method) {
            c = Jsoup.connect("https://api.vk.com/method/" + method);
            c.method(Connection.Method.POST);
            c.followRedirects(true);
            c.userAgent("Mozilla");
            c.data("v", "5.8");
        }

        VKRequest data(String what, String d) {
            c.data(what, d);
            return this;
        }

        VKRequest data(HashMap<String, String> map) {
            c.data(map);
            return this;
        }

        VKResponse execute() throws IOException {
            c.data("access_token", access_token).ignoreContentType(true);
            Connection.Response res = c.execute();
            return new VKResponse(new JSONObject(res.body()));
        }
    }

    class VKResponse {
        private JSONObject obj;
        private boolean error = false;

        VKResponse(JSONObject d) {
            obj = d;
            if (obj.has("error")) {
                this.error = true;
            }
        }

        public boolean isError() {
            return error;
        }

        JSONObject getObject() {
            return obj;
        }

        public String toString() {
            return obj.toString();
        }
    }

    synchronized void relogin() throws IOException {
        setAccessToken(getAccessToken());
    }

    abstract class VKLongPoll extends Thread {
        String key, server;
        long ts;
        BufferedInputStream in;

        @Override
        public void run() {
            initServer();
            while (true) {
                reconnect();
                JSONObject o = waitForData();
                if (o == null) {
                    System.out.println("Error converting input to object! Resuming...");
                    initServer();
                    continue;
                }
                if (o.has("failed")) {
                    System.out.println("Session failed! Resuming...");
                    initServer();
                    if (o.getInt("failed") == 1) {
                        ts = o.getLong("ts");
                    }
                    continue;
                }
                if (o.has("ts")) {
                    ts = o.getLong("ts");
                }
                onEvent(o.getJSONArray("updates"));
            }
        }

        JSONObject waitForData() {
            byte[] input = new byte[100 * 1024];
            byte[] buffer = new byte[1024];
            int ii;
            int pointer = 0;
            try {
                while ((ii = in.read(buffer)) != -1) {
                    for (int i = 0; i < ii; i++) {
                        input[pointer++] = buffer[i];
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                try {
                    JSONObject o = new JSONObject(new String(input));
                    System.out.println(o);
                    return o;
                } catch (Exception ee) {
                    return null;
                }
            }
        }

        void initServer() {
            while (true) {
                try {
                    tryInitServer();
                    System.out.println("Connection enstablished!");
                    return;
                } catch (Exception e) {
                    e.printStackTrace();
                    System.out.println("Failed to enstablish connection. Retrying...");
                    try {
                        Thread.sleep(2000);
                    } catch (InterruptedException e1) {
                        //
                    }
                }
            }
        }

        void reconnect() {
            while (true) {
                try {
                    tryReconnect();
                    System.out.println("Connection resumed!");
                    return;
                } catch (Exception e) {
                    e.printStackTrace();
                    System.out.println("Failed to resume connection. Retrying...");
                    try {
                        Thread.sleep(2000);
                    } catch (InterruptedException e1) {
                        //
                    }
                }
            }
        }

        void tryReconnect() throws IOException {
            in = new BufferedInputStream(new URL("https://" + server + "?act=a_check&key=" + key + "&ts=" + ts + "&wait=25&mode=2&version=1").openStream());
        }

        void tryInitServer() throws IOException {
            JSONObject o = request("messages.getLongPollServer").data("need_pts", "1").execute().getObject().getJSONObject("response");
            System.out.println(o);
            key = o.getString("key");
            server = o.getString("server");
            ts = o.getLong("ts");
        }

        abstract void onEvent(JSONArray updates);
    }

    abstract class VKListener extends Thread {
        HashMap<String, String> map = new HashMap<>();
        String req;
        long pause;

        VKListener(String req, long pause) {
            this.req = req;
            this.pause = pause;
        }

        VKListener data(String what, String d) {
            map.put(what, d);
            return this;
        }

        VKListener listen() {
            start();
            return this;
        }

        abstract void onVkEvent(VKResponse res) throws IOException;

        @Override
        public void run() {
            while (true) {
                try {
                    relogin();
                    synchronized (listenersLock) {
                        onVkEvent(request(req).data(map).execute());
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
                try {
                    Thread.sleep(pause);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
