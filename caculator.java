import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

public class caculator
{
    public static void main(String[] args)
    {
        caculatorFrame frame = new caculatorFrame();
        frame.setVisible(true);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }
    
}

class caculatorFrame extends JFrame
{
    /**
     *
     */
    private static final long serialVersionUID = -7970159056180131885L;

    public caculatorFrame()
    {
        setTitle("caculator");
        add(new caculatorPanel());
        pack();
    }
}

class caculatorPanel extends JPanel
{
    /**
     *
     */
    private static final long serialVersionUID = -4523653776963111293L;
    private JButton display;
    private JPanel panel;
    private double result;
    private String lastCommand;
    private boolean start;

    public caculatorPanel()
    {
        setLayout(new BorderLayout());

        result = 0;
        lastCommand = "=";
        start = true;

        display = new JButton("0");
        display.setEnabled(false);
        add(display, BorderLayout.NORTH);

        ActionListener insert = new insertAction();
        ActionListener command = new commandAction();

        panel = new JPanel();
        panel.setLayout(new GridLayout(4,4));
        addButton("7", insert);
        addButton("8", insert);        
        addButton("9", insert);        
        addButton("/", command);

        addButton("4", insert);
        addButton("5", insert);        
        addButton("6", insert);        
        addButton("*", command);

        addButton("1", insert);
        addButton("2", insert);        
        addButton("3", insert);        
        addButton("-", command);
   
        addButton("0", insert);
        addButton(".", insert);        
        addButton("=", command);        
        addButton("+", command);
 
        add(panel, BorderLayout.CENTER);
    }

    private void addButton(String label, ActionListener listener)
    {
        JButton button = new JButton(label);
        button.addActionListener(listener);
        panel.add(button);
    }
    private class insertAction implements ActionListener
    {
        public void actionPerformed(ActionEvent event)
        {
            String input = event.getActionCommand();
            if(start)
            {
                display.setText("");
                start = false;
            }
            display.setText(display.getText() + input);
        }
    }

    private class commandAction implements ActionListener
    {
        public void actionPerformed(ActionEvent event)
        {
            String command = event.getActionCommand();
            if (start)
            {
                if(command.equals("-"))
                {
                    start = false;
                    display.setText("-");
                }
                else lastCommand = command;
            }
            else
            {
                caculate(Double.parseDouble(display.getText()));
                lastCommand = command;
                start = true;
            }
        }
    }
    public void caculate(double x)
    {
        if (lastCommand.equals("-")) result -= x;
        else if (lastCommand.equals("+")) result += x;
        else if (lastCommand.equals("*")) result *= x;
        else if (lastCommand.equals("/")) result /= x;
        else if (lastCommand.equals("=")) result = x;

        display.setText("" + result);
    }
}