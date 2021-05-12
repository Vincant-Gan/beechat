import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.ListSelectionEvent;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.io.*;
import java.util.ArrayList;
import java.util.Date;

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
    SimpleDateFormat ft;

    public chatFrame(DataInputStream dis, DataOutputStream dos, String myUid)
    {
        //内存初始化
        this.myUid = myUid;
        myFriends = new ArrayList<friend>();
        ft = new SimpleDateFormat("yyyy-MM-dd_hh:mm:ss");
       //网络初始化
        try
        {
            speakerReader = dis;
            speakerWriter = dos;
            listening listener = new listening(this);
            listener.start(myUid);
            //获取好友列表
            getFriendList();
        }catch(Exception e){
            e.printStackTrace();
        }
       //图形界面初始化
        setTitle("Beechat");
        _d = new displayBox();
        _f = new friendListBox();
        _i = new inputBox();
        setLayout(new BorderLayout());
        add(_f, BorderLayout.EAST);
        add(_i, BorderLayout.SOUTH);
        add(_d, BorderLayout.CENTER);
        pack();
        addWindowListener(new windowClose());
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
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
                String[] msgs = s.split(" |\n", 5);
                if (msgs[0].equals("text"))
                {
                    renderText(msgs);
                }
                else if(msgs[0].equals("offlinefile"))
                {
                    renderFile(s);
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
                        String message = "history " + myUid + " " + chattingBoy.getUid();
                        speakerWriter.write(message.getBytes());
                        ArrayList<String> historyBuff = new ArrayList<String>();
                        byte[] buff = new byte[1024];
                        while (true)
                        {
                            int buffSize = speakerReader.read(buff);
                            String strbuff = new String(buff, 0, buffSize, StandardCharsets.UTF_8);
                            if (strbuff.equals("flag")) break;
                            else historyBuff.add(strbuff);
                            speakerWriter.write("ok".getBytes());
                        }
                        chattingBoy.setChatHistory(historyBuff);
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
                        file.createNewFile();
                        ajust_msg[0] = "offlinefileget";
                        String ajusted_msg = ajust_msg[0] + " " + ajust_msg[1]
                            + " " + ajust_msg[2] + " " + ajust_msg[3] + " " +
                            ajust_msg[4] + "\n" + ajust_msg[5];
                        ajusted_msg += "\n";
                        speakerWriter.write(ajusted_msg.getBytes());
                        byte[] buff = new byte[1024];
                        int buffSize = 0;
                        _d.chatHistoryModel.addElement("开始下载");
                        FileOutputStream fileWriter = new FileOutputStream(file);
                        int remainSieze = Integer.parseInt(ajust_msg[5].replace("\n",""));
                        while(remainSieze > 0)
                        {
                            buffSize = speakerReader.read(buff);
                            fileWriter.write(buff, 0, buffSize);
                            remainSieze -= buffSize;
                        }
                        fileWriter.close();
                        _d.chatHistoryModel.addElement("下载完成");
                        System.out.println("ok");
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
        JButton sendFileButton;

        public inputBox()
        {
            setLayout(new BorderLayout());
            sendButton = new JButton("发送");
            sendButton.addActionListener(new sendButtonListener());
            sendFileButton = new JButton("发送文件");
            sendFileButton.addActionListener(new sendFilebButtonListener());
            input = new JTextArea();
            add(input, BorderLayout.CENTER);
            add(sendButton, BorderLayout.EAST);
            add(sendFileButton, BorderLayout.WEST);
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
                    toSend = "text " + myUid + " " + chattingBoy.getUid() + " " +
                       ft.format(new Date()) + "\n" + toSend + "\n";
                    chattingBoy.addChatHistory(toSend);
                    renderText(toSend.split(" |\n", 5));
                    try
                    {
                        speakerWriter.write(toSend.getBytes("UTF-8"));
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
                if (chattingBoy == null)
                {
                    JOptionPane.showMessageDialog(null, 
                    "你要发给谁？","错误", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                JFileChooser jfc = new JFileChooser();
                jfc.setFileSelectionMode(JFileChooser.FILES_ONLY);
                jfc.showDialog(new JLabel(), "选一个文件");
                File file = jfc.getSelectedFile();
                String fileName = file.getName();
                String fileSize = Long.toString(file.length());
                String msg = "offlinefile " + myUid + " " + chattingBoy.getUid() + " "
                    + ft.format(new Date()) + " " + fileName + "\n" + fileSize + "\n";
                String reply;
                byte[] data = new byte[1024];
                try {
                    speakerWriter.write(msg.getBytes());
                    int buffSize = speakerReader.read(data);
                    reply = new String(data, 0, buffSize, StandardCharsets.UTF_8);
                    if (reply.equals("ready"))
                    {
                        String toSend = "<html><font size=\"2\">" +
                            "正在发送，请等待" + "</font></html>";
                        _d.chatHistoryModel.addElement(toSend);
                        InputStream fileReader = new FileInputStream(file);
                        while(true)
                        {
                            buffSize = fileReader.read(data);
                            if(buffSize == -1) break;
                            speakerWriter.write(data, 0, buffSize);
                        }
                        fileReader.close();
                        renderFile(msg);
                        toSend = "<html><font size=\"2\">" +
                            "发送完成!" + "</font></html>";
                        _d.chatHistoryModel.addElement(toSend);
                    }
                }catch(Exception ee)
                {
                    ee.printStackTrace();
                }
            }
        }
    }
    
    private class windowClose extends WindowAdapter
    {
        public void windowClosing(WindowEvent e)
        {
            super.windowClosing(e);
            try
            {
                speakerWriter.write("offline".getBytes());
                speakerWriter.close();
                speakerReader.close();
            }catch(Exception ee)
            {
                ee.printStackTrace();
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
            int buffSize = speakerReader.read(buff);
            String msg = new String(buff, 0, buffSize, StandardCharsets.UTF_8);
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
            int buffSize = speakerReader.read(buff);
            String msg = new String(buff, 0, buffSize, StandardCharsets.UTF_8);
            if (msg.equals("empty"))
            {
                return;
            }
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
        System.out.println(msg);
        if(msgs[2].equals(myUid))
        {
            int indexOfTheBoy = myFriends.indexOf(new friend(msgs[1]));
            if(indexOfTheBoy != -1){
                friend theBoy = myFriends.get(indexOfTheBoy);
                theBoy.addChatHistory(msg);
                if(chattingBoy != null && chattingBoy.equals(theBoy))
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
        text = text + msgs[1] + "  " + msgs[3] + "</font><br>";
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
        if(msgs[2].equals(myUid))
        {
            int indexOfTheBoy = myFriends.indexOf(new friend(msgs[1]));
            if(indexOfTheBoy != -1){
                friend theBoy = myFriends.get(indexOfTheBoy);
                theBoy.addChatHistory(msg);
                if(chattingBoy != null && chattingBoy.equals(theBoy))
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

    public void recvFriend(String uid)
    {
        myFriends.add(new friend(uid));
        _f.friendUid.addElement(uid);
    }

}
