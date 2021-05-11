import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.ListSelectionEvent;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.io.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.Arrays;

class chatFrame extends JFrame
{
    ArrayList<friend> myFriends;
    String myUid;
    friend chattingBoy;
    displayBox _d;
    inputBox _i;
    friendListBox _f;
    DataInputStream speakerReader;
    DataOutputStream speakerWriter;

    public chatFrame(Socket s, String myUid)
    {
        //网络初始化
        try
        {
            speakerReader = new DataInputStream(s.getInputStream());
            speakerWriter = new DataOutputStream(s.getOutputStream());
            //获取好友列表
            getFriendList();
        }catch(Exception e){
            e.printStackTrace();
        }
        this.myUid = myUid;
        //图形界面初始化
        setTitle("Beechat");
        myFriends = new ArrayList<friend>();
        _d = new displayBox();
        _f = new friendListBox();
        _i = new inputBox();
        setLayout(new BorderLayout());
        add(_f, BorderLayout.EAST);
        add(_i, BorderLayout.SOUTH);
        add(_d, BorderLayout.CENTER);
        pack();
    }

    private class displayBox extends JPanel
    {
        private DefaultListModel<String> chatHistoryModel;
        private JList<String> chatHistoryList;

        public displayBox()
        {
            //获取历史记录按钮部署
            setLayout(new BorderLayout());
            JButton getChatHistoryButton = new JButton("获取历史聊天记录");
            getChatHistoryButton.addActionListener(new getChatHistoryListener());
            add(getChatHistoryButton, BorderLayout.NORTH);

            //聊天记录部署
            chatHistoryModel = new DefaultListModel<String>();
            chatHistoryList = new JList<String>(chatHistoryModel);
            chatHistoryList.addMouseListener(new installButton());
            add(new JScrollPane(chatHistoryList), BorderLayout.CENTER);
        }

        public void showChatHistory()
        {
            chatHistoryModel.clear();
            ArrayList<String> chatHistory = chattingBoy.getChatHistory();
            for (String s : chatHistory)
            {
                String[] msgs = s.split(" |\n");
                if (msgs[0].equals("text"))
                {
                    renderText(msgs);
                }
            }
        }

        private class getChatHistoryListener implements ActionListener
        {
            public void actionPerformed(ActionEvent e)
            {
                if (chattingBoy == null)
                    JOptionPane.showMessageDialog(null,
                    "你要获取谁的聊天记录？","错误", JOptionPane.ERROR_MESSAGE);
                else
                {
                    try
                    {
                        String message = "history " + myUid + chattingBoy.getUid();
                        speakerWriter.write(message.getBytes());
                        ArrayList<String> historyBuff = new ArrayList<String>();
                        byte[] buff = new byte[1024];
                        while (true)
                        {
                            speakerReader.read(buff);
                            String strbuff = new String(buff, StandardCharsets.UTF_8).trim();
                            if (strbuff.equals("flag")) break;
                            else historyBuff.add(strbuff);
                        }
                        showChatHistory();
                    }catch(Exception ee)
                    {
                        ee.printStackTrace();
                    }
                }
            }
        }

        private class installButton extends MouseAdapter
        {
            public void mouseClicked(MouseEvent e)
            {
                if(e.getButton() == MouseEvent.BUTTON3)
                {
                    String[] msgs = chatHistoryList.getSelectedValue().split("<!--|-->");
                    try {
                        String[] ajust_msg = msgs[1].split(" |\n", 6);
                        File file = new File("./recv/" + ajust_msg[4]);
                        if(file.exists())
                        {
                            JOptionPane.showMessageDialog(null,
                            "此文件已下载","错误", JOptionPane.ERROR_MESSAGE);
                            return;
                        }
                        file.mkdirs();
                        ajust_msg[0] = "offlinefileget";
                        String ajusted_msg = Arrays.toString(ajust_msg);
                        speakerWriter.write(ajusted_msg.getBytes());
                        byte[] buff = new byte[1024];
                        speakerReader.read(buff);
                        String reply = new String(buff, StandardCharsets.UTF_8).trim();
                        _d.chatHistoryModel.addElement("开始下载");
                        if (reply.equals("ready"))
                        {
                            FileOutputStream fileWriter = new FileOutputStream(file);
                            int packetLength = 0;
                            while(true)
                            {
                                packetLength = speakerReader.read(buff);
                                if(packetLength == -1) break;
                                fileWriter.write(buff, 0, packetLength);
                            }
                            fileWriter.close();
                            _d.chatHistoryModel.addElement("下载完成");
                        }
                    }catch(Exception ee)
                    {
                        ee.printStackTrace();
                    }

                }
            }
        }
    }

    private class friendListBox extends JPanel
    {
        private DefaultListModel<String> friendUid;
        private JList<String> friendList;

        public friendListBox()
        {
            setLayout(new BorderLayout());
            JButton addFriendButton = new JButton("添加好友");
            addFriendButton.addActionListener(new addFriendButtonListener());
            add(addFriendButton, BorderLayout.NORTH);
            //获取好友们的uid
            friendUid = new DefaultListModel<String>();
            for (friend fd : myFriends)
            {
                String hisUid = fd.getUid();
                friendUid.addElement(hisUid);
            }
            friendList = new JList<String>(friendUid);
            friendList.addListSelectionListener(new showFriendChatFrame());
            add(new JScrollPane(friendList), BorderLayout.CENTER);
        }

        private class addFriendButtonListener implements ActionListener
        {
            public void actionPerformed(ActionEvent e)
            {
                addFriendFrame frame = new addFriendFrame(chatFrame.this);
                frame.setVisible(true);
            }
        }

        private class showFriendChatFrame implements ListSelectionListener
        {
            public void valueChanged(ListSelectionEvent e)
            {
                String displayUid = friendList.getSelectedValue();
                chattingBoy = myFriends.get(myFriends.indexOf(new friend(displayUid)));
                _d.showChatHistory();
            }
        }
    }

    private class inputBox extends JPanel
    {
        JTextArea input;
        JButton sendButton;
        JButton sendFilebButton;

        public inputBox()
        {
            setLayout(new BorderLayout());
            sendButton = new JButton("发送");
            sendButton.addActionListener(new sendButtonListener());
            sendFilebButton = new JButton("发送文件");
            sendButton.addActionListener(new sendFilebButtonListener());
            input = new JTextArea();
            add(input, BorderLayout.CENTER);
            add(sendButton, BorderLayout.EAST);
            add(sendFilebButton, BorderLayout.WEST);
        }

        private class sendButtonListener implements ActionListener
        {
            public void actionPerformed(ActionEvent e)
            {
                if (chattingBoy == null)
                {
                    JOptionPane.showMessageDialog(null, 
                    "你要发给谁？","错误", JOptionPane.ERROR_MESSAGE);
                }
                else
                {
                    String toSend = input.getText();
                    toSend = "text " + myUid + " " + chattingBoy.getUid() +
                        new Date().toString() + "\n" + toSend;
                    chattingBoy.addChatHistory(toSend);
                    renderText(toSend.split(" |\n", 5));
                    try
                    {
                        speakerWriter.write(toSend.getBytes());
                    }catch(Exception ee){
                        ee.printStackTrace();
                    }
                    input.setText("");
                }
            }
        }

        private class sendFilebButtonListener implements ActionListener
        {
            public void actionPerformed(ActionEvent e)
            {
                JFileChooser jfc = new JFileChooser();
                jfc.setFileSelectionMode(JFileChooser.FILES_ONLY);
                jfc.showDialog(new JLabel(), "选一个文件");
                File file = jfc.getSelectedFile();
                String fileName = file.getName();
                String fileSize = Long.toString(file.length());
                String msg = "offlinefile " + myUid + " " + chattingBoy.getUid() + " "
                    + new Date().toString() + " " + fileName + "\n" + fileSize;
                String reply;
                byte[] data = new byte[1024];
                try {
                    speakerWriter.write(msg.getBytes());
                    speakerReader.read(data);
                    reply = new String(data, StandardCharsets.UTF_8).trim();
                    if (reply.equals("ready"))
                    {
                        String toSend = "<html><font size=\"2\">" +
                            "正在发送，请等待" + "</font></html>";
                        _d.chatHistoryModel.addElement(toSend);
                        InputStream fileReader = new FileInputStream(file);
                        while(true)
                        {
                            int ll = fileReader.read(data);
                            if(ll == -1) break;
                            speakerWriter.write(data);
                        }
                        fileReader.close();
                        renderFile(msg);
                    }
                }catch(Exception ee)
                {
                    ee.printStackTrace();
                }
            }
        }
    }
    
    public String addFriend(String uid)
    {
        String toSend = "addFriend " + myUid + " " + uid;
        try
        {
            speakerWriter.write(toSend.getBytes());
            byte[] buff = new byte[1024];
            speakerReader.read(buff);
            String msg = new String(buff, StandardCharsets.UTF_8).trim();
            if (msg.equals("agree"))
            {
                myFriends.add(new friend(uid));
                _f.friendUid.addElement(uid);
            }
            return msg;
        }catch(Exception e)
        {
            e.printStackTrace();
            return "error";
        }
    }

    private void getFriendList()
    {
        String toSend = "friendlist request";
        try
        {
            speakerWriter.write(toSend.getBytes());
            byte[] buff = new byte[1024];
            speakerReader.read(buff);
            String msg = new String(buff, StandardCharsets.UTF_8).trim();
            for (String ll : msg.split("\n"))
            {
                String[] aa = ll.split(" ");
                if(aa[0].equals("friend"))
                {
                    if(aa[2].equals("T")) myFriends.add(new friend(aa[1], true));
                    else if(aa[2].equals("F")) myFriends.add(new friend(aa[1], false));
                    else System.out.println("error");
                }
                else
                {
                    System.out.println("friendlist error");
                }
            }
        }catch(Exception e)
        {
            e.printStackTrace();
        }
    }

    public void addText(String msg)
    {
        String[] msgs = msg.split(" |\n", 5);
        if(msgs[2] == myUid)
        {
            int indexOfTheBoy = myFriends.indexOf(new friend(msgs[1]));
            if(indexOfTheBoy != -1){
                friend theBoy = myFriends.get(indexOfTheBoy);
                theBoy.addChatHistory(msg);
                if(chattingBoy.equals(theBoy))
                {
                    renderText(msgs);
                }
            }
            else
            {
                System.out.println("uid not exist");
            }
        }
        else
        {
            System.out.println("not my message");
        }
    }

    private void renderText(String[] msgs)
    {
        String text = "<html>";
        if (msgs[1].equals(myUid))
        {
            text = text + "<font size=\"3\" color=\"green\">";
        }
        else
        {
            text = text + "<font size=\"3\" color=\"blue\">";
        }
        text = text + msgs[1] + msgs[3] + "</font><br>";
        text = text + "<font size=\"5\">" + msgs[4] + "</font></html>";
        _d.chatHistoryModel.addElement(text);
    }

    private void renderFile(String msg)
    {
        String[] msgs = msg.split(" |\n", 6);
        String text = "<html>";
        text = text + "<!--" + msg + "-->";
        if (msgs[1].equals(myUid))
        {
            text = text + "<font size=\"3\" color=\"green\">";
        }
        else
        {
            text = text + "<font size=\"3\" color=\"blue\">";
        }
        text = text + msgs[1] + msgs[3] + "</font><br>";
        text = text + "<font size=\"5\">" + msgs[4] + "</font><br>";
        text = text + "<font size=\"3\">文件大小：" + msgs[5] + "</font></html>";
        _d.chatHistoryModel.addElement(text);
    }

    public String getMyUid()
    {
        return myUid;
    }

    public void addFile(String msg)
    {
        String[] msgs = msg.split(" |\n", 6);
        if(msgs[2] == myUid)
        {
            int indexOfTheBoy = myFriends.indexOf(new friend(msgs[1]));
            if(indexOfTheBoy != -1){
                friend theBoy = myFriends.get(indexOfTheBoy);
                theBoy.addChatHistory(msg);
                if(chattingBoy.equals(theBoy))
                {
                    renderFile(msg);
                }
            }
            else
            {
                System.out.println("uid not exist");
            }
        }
        else
        {
            System.out.println("not my message");
        }
    }


}
