import org.json.JSONArray;
import org.json.JSONObject;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.util.ArrayList;

public class TrackFrame extends JFrame {
    static final int APP_WIDTH = 500, APP_HEIGHT = 800;
    DefaultListModel<JCheckBox> model = new DefaultListModel<>();
    ArrayList<Integer> users = new ArrayList<>();
    CheckBoxList checkBoxList = new CheckBoxList(model) {
        @Override
        void onSelected(int index, boolean selected) {
            if(selected) {
                Main.mTray.trackUids.add(users.get(index));
            } else {
                Main.mTray.trackUids.remove(users.get(index));
            }
        }
    };

    TrackFrame() {
        super("Track Settings");
        setLayout(null);
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        setMinimumSize(new Dimension(APP_WIDTH, APP_HEIGHT));
        setBounds(new Rectangle(APP_WIDTH, APP_HEIGHT));
        setMaximumSize(new Dimension(LoginFrame.APP_WIDTH, LoginFrame.APP_HEIGHT));
        setResizable(false);
        final JScrollPane pane = new JScrollPane(checkBoxList);
        pane.setBounds(0, 0, APP_WIDTH-9, APP_HEIGHT-32);
        add(pane);
        addWindowListener(new WindowAdapter(){
            public void windowClosing(WindowEvent e){
                Main.mTray.frame = null;
            }
        });
        new Thread(()->{
            try {
                JSONArray arr = Main.api.request("friends.get").data("fields","domain").data("order", "hints").execute().getObject().getJSONObject("response").getJSONArray("items");
                for(int i = 0; i < arr.length(); i++) {
                    JSONObject user = arr.getJSONObject(i);
                    int id = user.getInt("id");
                    String name = user.getString("first_name")+" "+user.getString("last_name")+" ("+user.getString("domain")+")";
                    JCheckBox box = new JCheckBox();
                    box.setText(name);
                    box.setSelected(Main.mTray.trackUids.contains(id));
                    users.add(id);
                    model.addElement(box);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            checkBoxList.setModel(model);
            SwingUtilities.invokeLater(()->{
                checkBoxList.updateUI();
            });
            System.out.println("DONE");
        }).start();
        setVisible(true);
    }
}
