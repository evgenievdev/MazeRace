package client;

import java.awt.Graphics;
import javax.swing.ImageIcon;
import javax.swing.JPanel;

@SuppressWarnings("serial")
public class StartUpPanel extends JPanel {

	public void paintComponent(Graphics g) {
		int x = 0, y = 0;
		ImageIcon icon = new ImageIcon("startImage.jpg");
		g.drawImage(icon.getImage(), x, y, getSize().width, getSize().height, this);
	}
}
