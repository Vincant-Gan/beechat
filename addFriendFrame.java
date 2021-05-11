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
    private chatFrame chacha;

    public addFriendFrame(chatFrame o)
    {
        chacha = o;
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

        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
    }

    private class addFriendListener implements ActionListener
    {
        public void actionPerformed(ActionEvent e)
        {
            String uidString = uid.getText();
            System.out.println(uidString);
            String msg = chacha.addFriend(uidString);
            if (msg.equals("agree"))
            {
                JOptionPane.showMessageDialog(null,
                "对方同意了ヽ(✿ﾟ▽ﾟ)ノ","好耶", JOptionPane.PLAIN_MESSAGE);
                dispose();
            }
            else if (msg.equals("disagree"))
            {
                 JOptionPane.showMessageDialog(null,
                "对方拒绝了Σ( ° △ °|||)︴","额。。", JOptionPane.PLAIN_MESSAGE);
            }
            else if (msg.equals("not online"))
            {
                 JOptionPane.showMessageDialog(null,
                "等TA在线时再加吧","TA不在线", JOptionPane.PLAIN_MESSAGE);
            }
            else
            {
                JOptionPane.showMessageDialog(null,
                "出错了。。","错误", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}
