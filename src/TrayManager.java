import org.json.JSONArray;
import org.json.JSONObject;

import java.awt.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;

public class TrayManager {
    TrayIcon trayIcon;
    SystemTray tray = SystemTray.getSystemTray();
    Image red, green;
    VKApi.VKLongPoll poll;
    TrackFrame frame;
    HashSet<Integer> trackUids = new HashSet<>(25);
    void start() {
        red = Toolkit.getDefaultToolkit().getImage(getClass().getResource("images/spy_red.png"));
        green = Toolkit.getDefaultToolkit().getImage(getClass().getResource("images/spy_green.png"));
        trayIcon = new TrayIcon(red, "Online Tracker");
        trayIcon.setImageAutoSize(true);
        trayIcon.setToolTip("Tracks online status of users");
        PopupMenu menu = new PopupMenu();
        MenuItem settingsItem = new MenuItem("Set targets");
        settingsItem.addActionListener((e) -> {
            if(frame == null) {
                frame = new TrackFrame();
            }
        });
        MenuItem exitItem = new MenuItem("Exit");
        exitItem.addActionListener((e) -> {
            Main.mTray.stop();
            System.exit(0);
        });
        menu.add(settingsItem);
        menu.add(exitItem);
        trayIcon.setPopupMenu(menu);
        try {
            tray.add(trayIcon);
        } catch (AWTException e) {
            e.printStackTrace();
        }
        poll = Main.api.new VKLongPoll() {
            @Override
            void onEvent(JSONArray updates) {
                for(int i = 0; i < updates.length(); i++) {
                    JSONArray a = updates.getJSONArray(i);
                    if(a.getInt(0) == 8 || a.getInt(0)==9) {
                        int userId = -a.getInt(1);
                        if(trackUids.contains(userId)) {
                            try {
                                JSONObject o = Main.api.request("users.get").data("user_ids", userId + "").execute().getObject().getJSONArray("response").getJSONObject(0);
                                System.out.println(o);
                                Main.mTray.setOnline(o.getString("first_name") + " " + o.getString("last_name"), a.getInt(0) == 8);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }
            }
        };
        poll.start();
        Runtime.getRuntime().addShutdownHook(new Thread(this::stop));
    }
    void setOnline(String name, boolean online) {
        if(online) {
            trayIcon.setImage(green);
        } else {
            trayIcon.setImage(red);
        }
        trayIcon.displayMessage(name+" is "+(online?"online":"offline"), online ? "Now is your chance ;)" : "Better luck next time ;(", TrayIcon.MessageType.NONE);
    }
    void stop() {
        tray.remove(trayIcon);
    }
}
