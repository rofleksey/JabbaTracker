import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

/**
 * Created by Borisov on 23.02.2016.
 */
public class LoginFrame extends JFrame implements KeyListener {
    public static final int APP_WIDTH = 250;
    public static final int APP_HEIGHT = 180;
    public static final String DEFAULT_MAIL = "evilmorty@yandex.ru", DEFAULT_PW = "password";
    JTextField email, pass;
    JButton login;
    JLabel info;
    boolean mailChanged=false, pwChanged=false;
    LoginFrame() {
        super("Login");
        setLayout(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        //setBounds(0, 0, APP_WIDTH, APP_HEIGHT);
        setMinimumSize(new Dimension(APP_WIDTH, APP_HEIGHT));
        setMaximumSize(new Dimension(APP_WIDTH, APP_HEIGHT));
        setResizable(false);

        email = new JTextField(DEFAULT_MAIL);
        email.setBounds(50, 30, APP_WIDTH-100, 20);
        email.addMouseListener(new MouseListener() {
                                   @Override
                                   public void mouseClicked(MouseEvent mouseEvent) {
                                       if(!mailChanged) {
                                           mailChanged = true;
                                           email.setText("");
                                       }
                                   }

                                   @Override
                                   public void mousePressed(MouseEvent mouseEvent) {

                                   }

                                   @Override
                                   public void mouseReleased(MouseEvent mouseEvent) {

                                   }

                                   @Override
                                   public void mouseEntered(MouseEvent mouseEvent) {

                                   }

                                   @Override
                                   public void mouseExited(MouseEvent mouseEvent) {

                                   }
                               }
        );
        email.addKeyListener(this);
        add(email);

        pass = new JPasswordField(DEFAULT_PW);
        pass.setBounds(50, 60, APP_WIDTH-100, 20);
        pass.addMouseListener(new MouseListener() {
                                  @Override
                                  public void mouseClicked(MouseEvent mouseEvent) {
                                      if(!pwChanged) {
                                          pwChanged = true;
                                          pass.setText("");
                                      }
                                  }

                                  @Override
                                  public void mousePressed(MouseEvent mouseEvent) {

                                  }

                                  @Override
                                  public void mouseReleased(MouseEvent mouseEvent) {

                                  }

                                  @Override
                                  public void mouseEntered(MouseEvent mouseEvent) {

                                  }

                                  @Override
                                  public void mouseExited(MouseEvent mouseEvent) {

                                  }
                              }
        );
        pass.addKeyListener(this);
        add(pass);

        info = new JLabel("Введите данные для входа");
        info.setBounds(15, 90, APP_WIDTH-30, 20);
        info.setHorizontalAlignment(SwingConstants.CENTER);
        add(info);

        login = new JButton("Войти");
        login.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ev) {
                info.setText("Вход...");
                login.setEnabled(false);
                new Thread() {
                    @Override
                    public void run() {
                        try {
                            Main.api = new VKApi(email.getText(), pass.getText());
                            Main.mTray.start();
                            dispose();
                        } catch (VKApi.VKException e) {
                            info.setText(e.toString());
                            login.setEnabled(true);
                        }
                    }
                }.start();
            }
        });
        login.setBounds(50, 120, APP_WIDTH-100, 20);
        add(login);
        setVisible(true);
    }

    @Override
    public void keyTyped(KeyEvent keyEvent) {

    }

    @Override
    public void keyPressed(KeyEvent keyEvent) {
        if(keyEvent.getKeyCode() == KeyEvent.VK_ENTER) {
            System.out.println("!");
            if(login.isEnabled()) login.doClick();
        }
    }

    @Override
    public void keyReleased(KeyEvent keyEvent) {

    }
}
