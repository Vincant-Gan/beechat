import java.util.ArrayList;

public class friend
{
    private String uid;
    private ArrayList<String> chatHistory;

    public friend(String _uid)
    {
        uid = _uid;
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
}
