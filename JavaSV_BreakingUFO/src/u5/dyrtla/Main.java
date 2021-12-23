package u5.dyrtla;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.Timer;

// 20185112 ±Ë«—¿Ã


public class Main {

	public static final int SCREEN_WIDTH = 960;
	public static final int SCREEN_HEIGHT = 600;

	public static void main(String[] args) {
		
		Ufo rt = new Ufo();
		rt.go();
		
	}

}