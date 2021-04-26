import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.util.ArrayList;

public class test
{
    public static void main(String[] args)
    {
        JFrame frame = new signinFrame();
        frame.setVisible(true);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        System.out.println("start for beechat");
    }
}



class chatFrame extends JFrame
{
    ArrayList<friend> myFriends;
    displayBox _d;
    inputBox _i;
    friendListBox _f;

    public chatFrame()
    {
        myFriends = new ArrayList<friend>();
    }

    private class displayBox extends JPanel
    {
        public displayBox()
        {
            setLayout(new BorderLayout());
            JButton getChatHistoryButton = new JButton("获取历史聊天记录");
            getChatHistoryButton.addActionListener(new getChatHistoryListener());

        }
        private void addText(String message)
        {
        }

        private void addFile(String message)
        {
        }

        public void showChatHistory(String uid)
        {
        }

        private class getChatHistoryListener implements ActionListener
        {
            public void actionPerformed(ActionEvent e)
            {
                System.out.println("getting..");
            }
        }
    }

    private class friendListBox extends JScrollPane
    {
        private ArrayList<String> friendUid;

        public friendListBox()
        {
            int numofFriends = myFriends.size();
            setLayout(new GridLayout(numofFriends + 1, 1));
            JButton addFriendButton = new JButton("添加好友");
            addFriendButton.addActionListener(new addFriendButtonListener());
            add(addFriendButton);
            //获取好友们的uid
            friendUid = new ArrayList<String>();
            for (friend fd : myFriends)
            {
                String hisUid = fd.getUid();
                friendUid.add(hisUid);
                JButton aFriend = new JButton(hisUid);
                add(aFriend);
                aFriend.addActionListener(new showFriendChatFrame());
            }
        }

        private class addFriendButtonListener implements ActionListener
        {
            public void actionPerformed(ActionEvent e)
            {
                addFriendFrame frame = new addFriendFrame();
                frame.setVisible(true);
                frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            }
        }

        private class showFriendChatFrame implements ActionListener
        {
            public void actionPerformed(ActionEvent e)
            {
                String displayingUid = e.getActionCommand();
                _d.showChatHistory(displayingUid);
            }
        }
    }

    private class inputBox extends JPanel
    {
        JTextArea input;
        JButton sendButton;

        public inputBox()
        {
            setLayout(new BorderLayout());
            sendButton = new JButton("发送");
            sendButton.addActionListener(new sendButtonListener());
            input = new JTextArea();
            add(input, BorderLayout.CENTER);
            add(sendButton, BorderLayout.EAST);
        }

        private class sendButtonListener implements ActionListener
        {
            public void actionPerformed(ActionEvent e)
            {
                String toSend = input.getText();
                System.out.println(toSend);
            }
        }
    }

}
