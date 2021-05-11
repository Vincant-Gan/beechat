import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.io.*;

class signinFrame extends JFrame
{
    /**
     *
     */
    private static final long serialVersionUID = 1L;
    private JTextField uidBox;
    private JPasswordField passwordBox;
    Socket speaker;

    public signinFrame(Socket s)
    {
        speaker = s;
        setTitle("Beechat");
        //登录界面的大panel
        JPanel signinPanel = new JPanel();
        signinPanel.setLayout(new BorderLayout());
        //输入密码与uid的panel
        JPanel inputBox = new JPanel();
        inputBox.setLayout(new GridLayout(2, 2));
        //uid与密码输入框加入input panel
        uidBox = new JTextField();
        uidBox.setText("1234567890");
        passwordBox = new JPasswordField();
        JLabel uid = new JLabel("uid:", JLabel.CENTER);
        JLabel pswd = new JLabel("密码：", JLabel.CENTER);
        inputBox.add(uid);
        inputBox.add(uidBox);
        inputBox.add(pswd);
        inputBox.add(passwordBox);
        //登录与注册按钮及其panel
        JButton signInButton = new JButton("注册");
        JButton signUpButton = new JButton("登录");
        JPanel buttonBox = new JPanel();
        buttonBox.setLayout(new GridLayout(1, 2));
        buttonBox.add(signInButton);
        buttonBox.add(signUpButton);

        //全部加入大panel
        signinPanel.add(inputBox, BorderLayout.CENTER);
        signinPanel.add(buttonBox, BorderLayout.SOUTH);

        add(signinPanel);

        //给button增加actionListener功能
        signinListener accn = new signinListener();
        signInButton.addActionListener(accn);
        signUpButton.addActionListener(accn);

        //调size
        Toolkit kit = Toolkit.getDefaultToolkit();
        Dimension screenSize = kit.getScreenSize();
        setSize(screenSize.width / 4, screenSize.height / 5);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }

    private class signinListener implements ActionListener
    {
        public void actionPerformed(ActionEvent e)
        {
            String cmd = e.getActionCommand();
            String uid = uidBox.getText();
            char[] password = passwordBox.getPassword();
            if (uid.length() != 10)
            {
                JOptionPane.showMessageDialog(null, "uid应当为10位","错误", JOptionPane.ERROR_MESSAGE);
                return;
            }
            try {
                DataOutputStream speakerWriter = new DataOutputStream(speaker.getOutputStream());
                DataInputStream speakerReader = new DataInputStream(speaker.getInputStream());
                if (cmd.equals("登录"))
                {
                    String msg = "signin " + uid + " " + new String(password);
                    speakerWriter.write(msg.getBytes());
                    byte[] buff = new byte[1024];
                    speakerReader.read(buff);
                    String reply = new String(buff, StandardCharsets.UTF_8).trim();
                    if(reply.equals("signin successfully"))
                    {
                        dispose();
                        JFrame chacha = new chatFrame(speakerReader, speakerWriter, uid);
                        chacha.setVisible(true);
                    }
                    else if(reply.equals("uid not exists"))
                    {
                        JOptionPane.showMessageDialog(null, 
                        "uid不存在","错误", JOptionPane.ERROR_MESSAGE);
                    }
                    else if(reply.equals("wrong password"))
                    {
                        JOptionPane.showMessageDialog(null,
                        "密码错误","错误", JOptionPane.ERROR_MESSAGE);
                    }
                    else
                    {
                        JOptionPane.showMessageDialog(null,
                        "登陆错误","错误", JOptionPane.ERROR_MESSAGE);
                    }
 
                }
                else if(cmd.equals("注册"))
                {
                    String msg = "signup " + uid + " " + new String(password);
                    System.out.println(msg);
                    speakerWriter.write(msg.getBytes());
                    byte[] buff = new byte[1024];
                    speakerReader.read(buff);
                    String reply = new String(buff, StandardCharsets.UTF_8).trim();
                    System.out.println(reply);
                    if(reply.equals("signup successfully"))
                    {
                        dispose();
                        JFrame chacha = new chatFrame(speakerReader, speakerWriter, uid);
                        chacha.setVisible(true);
                    }
                    else if(reply.equals("uid already exists"))
                    {
                        JOptionPane.showMessageDialog(null,
                        "此uid已存在","错误", JOptionPane.ERROR_MESSAGE);
                    }
                    else
                    {
                        JOptionPane.showMessageDialog(null,
                        "登陆错误","错误", JOptionPane.ERROR_MESSAGE);
                    }
                }
            } catch(Exception ee){
                ee.printStackTrace();
            }
        }
    }
}