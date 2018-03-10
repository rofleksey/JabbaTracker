import javax.swing.*;

public class Main {
    static VKApi api;
    static TrayManager mTray = new TrayManager();
    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(
                    UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {

        }
        new LoginFrame();
    }
}
