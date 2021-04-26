import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

class signinFrame extends JFrame
{
    /**
     *
     */
    private static final long serialVersionUID = 1L;
    private JTextField uidBox;
    private JPasswordField passwordBox;

    public signinFrame()
    {
        setTitle("Beechat welcome using Beechat");
        //登录界面的大panel
        JPanel signinPanel = new JPanel();
        signinPanel.setLayout(new BorderLayout());
        //输入密码与uid的panel
        JPanel inputBox = new JPanel();
        inputBox.setLayout(new GridLayout(2, 2));
        //uid与密码输入框加入input panel
        uidBox = new JTextField();
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
            if (cmd.equals("登录"))
            {
                System.out.println(uid);
            }
            else
            {
                System.out.println(password);
            }
        }
    }
}