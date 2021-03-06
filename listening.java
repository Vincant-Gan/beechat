import java.net.*;
import java.nio.charset.StandardCharsets;

import javax.swing.JOptionPane;

import java.io.*;

public class listening implements Runnable {
    private Thread t;
    private chatFrame oFrame;
    private Socket listener;
    private DataInputStream listenerReader;
    private DataOutputStream listenerWriter;

    public listening(chatFrame o)
    {
        oFrame = o;
        try{
            listener = new Socket("124.71.233.58", 7890);
            listenerReader = new DataInputStream(listener.getInputStream());
            listenerWriter = new DataOutputStream(listener.getOutputStream());
        }catch(Exception e)
        {
            e.printStackTrace();
        }
    }

    public void start(String uid)
    {
        if (t == null)
        {
            t = new Thread(this);
            t.start();
            String msg = "hello from " + uid;
            try
            {
                listenerWriter.write(msg.getBytes());
            }catch(Exception e)
            {
                e.printStackTrace();
            }
        }
    }

    public void run()
    {
        byte[] buff = new byte[1024];
        try
        {
            while(true)
            {
                int buffSize = listenerReader.read(buff);
                if(buffSize == -1) continue;
                String msg = new String(buff, 0, buffSize, StandardCharsets.UTF_8);
                String[] msgs = msg.split(" ", 3);
                if(msgs[0].equals("text"))
                {
                    oFrame.addText(msg);
                }
                else if(msgs[0].equals("addFriend") && msgs[2].equals(oFrame.getMyUid()))
                {
                    String info = msgs[1] + "请求加为好友";
                    String[] options = {"同意", "不同意"};
                    int ans = JOptionPane.showOptionDialog(null, info, "好友请求",
                        JOptionPane.YES_NO_OPTION, JOptionPane.INFORMATION_MESSAGE,
                        null, options, options[0]);
                    if (ans == JOptionPane.YES_OPTION)
                    {
                        listenerWriter.write("addFriend agree".getBytes());
                        oFrame.recvFriend(msgs[1]);
                    }
                    else
                    {
                        listenerWriter.write("addFriend disagree".getBytes());
                    }
                }
                else if(msgs[0].equals("offlinefile"))
                {
                    oFrame.addFile(msg);
                }
            }
        }catch(Exception e)
        {
            e.printStackTrace();
        }
    }
    
}
