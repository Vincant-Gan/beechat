import java.util.ArrayList;

public class friend
{
    private String uid;
    private ArrayList<String> chatHistory;
    private boolean online;

    public friend(String _uid)
    {
        uid = _uid;
        online = true;
        chatHistory = new ArrayList<String>();
    }

    public friend(String _uid, boolean oo)
    {
        uid = _uid;
        online = oo;
        chatHistory = new ArrayList<String>();
    }

    public ArrayList<String> getChatHistory()
    {
        return chatHistory;
    }

    public void addChatHistory(String message)
    {
        chatHistory.add(message);
    }

    public void setChatHistory(ArrayList<String> _c)
    {
        chatHistory = _c;
    }

    public String getUid()
    {
        return uid;
    }

    public void setOnline(boolean o)
    {
        online = o;
    }

    public boolean getOnline()
    {
        return online;
    }

    @Override
    public boolean equals(Object f)
    {
        if (!(f instanceof friend))
        {
            return false;
        }

        friend ff = (friend) f;
        if (this.uid.equals(ff.getUid()))
            return true;
        else return false;
    }
}
