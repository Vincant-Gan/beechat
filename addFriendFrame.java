import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

public class addFriendFrame extends JFrame
{
    /**
     *
     */
    private static final long serialVersionUID = 1L;
    private JTextField uid;

    public addFriendFrame()
    {
        setTitle("加好友");
        setLayout(new BorderLayout());
        //设置输入框
        JPanel inputBox = new JPanel();
        inputBox.setLayout(new BorderLayout());
        uid = new JTextField();
        inputBox.add(new JLabel("对方的uid:", JLabel.CENTER), BorderLayout.WEST);
        inputBox.add(uid, BorderLayout.CENTER);
        //设置确认按钮
        JButton addFriendButton = new JButton("加为好友");
        addFriendButton.addActionListener(new addFriendListener());

        //将输入框与按键加入总布局中
        add(inputBox, BorderLayout.CENTER);
        add(addFriendButton, BorderLayout.SOUTH);
        //调size
        Toolkit kit = Toolkit.getDefaultToolkit();
        Dimension screenSize = kit.getScreenSize();
        setSize(screenSize.width / 4, screenSize.height / 5);
    }

    private class addFriendListener implements ActionListener
    {
        public void actionPerformed(ActionEvent e)
        {
            String uidString = uid.getText();
            System.out.println(uidString);
        }
    }
}
