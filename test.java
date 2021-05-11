import javax.swing.*;
import java.net.*;

public class test
{
    public static void main(String[] args)
    {
        String host = "124.71.233.58";
        int port = 7890;
        try
        {
            Socket speaker = new Socket(host, port);
            JFrame frame = new signinFrame(speaker);
            frame.setVisible(true);
            System.out.println("start for beechat");
            System.out.println(speaker.isConnected());
        }catch(Exception e){
            e.printStackTrace();
        }
    }
}
