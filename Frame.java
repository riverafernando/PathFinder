
import java.awt.GridLayout;
import java.awt.Toolkit;
import javax.swing.JFrame;

public class Frame
{
    private static final long serialVersionUID = 1L;

    public static void main(String [] args)
    {
        JFrame p = new JFrame();
        p.setTitle("PathFinder");
        p.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);        
        p.setLayout(new GridLayout(1,1,0,0));    
        p.setResizable(true);
        p.add(new Screen());
        p.pack();
        p.setLocationRelativeTo(null);
        p.setVisible(true);
       
    }
}
