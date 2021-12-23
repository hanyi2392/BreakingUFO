package u3.gameBackground;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Image;
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

public class Ufo extends JFrame {

	Timer t1;							// ����ð� ǥ�ø� ���� �ð�
	Timer t2;							// ���� �����̰� �ϴ� �ð�
	Timer left, right;

	int timeElapsed = 0;				// ����ð� 0���� �ʱ�ȭ
	int score = 0;						// ����
	
	final int DELAY = 4;				// Ÿ�̸� �׼� �߻� ��/�ð�
	DrawPanel drawPanel;				// ���� ���� �г�		
	ScorePanel scorePanel;				// �ð�, ü�� ǥ�õǴ� �г�
	//JLabel timeLabel;
	JLabel scoreLabel;
	JLabel successLabel = new JLabel(new ImageIcon("src/images/cong.png"));
	JLabel failLabel = new JLabel(new ImageIcon("src/images/fail.png"));
	int heart;	// ü��

	int rows = 10;
	int cols = 18;

	int rectWidth, rectHeight;

	BufferedImage personFinderIcon;		// ������ ǥ���� �ΰ� Ž���� ������. ������ ������ �Բ� ��������.
	int personFinder;					// �ΰ� Ž������ ���� ����. ����� 1�� ��������.

	Brick[][] bricks = new Brick[rows][cols];
	ArrayList<Ball> ball = new ArrayList<>();
	Bar bar;

	private Image background = new ImageIcon("src/images/nightBackground.png").getImage();	// ����̹���

	public Ufo() {

		rectWidth=(Main.SCREEN_WIDTH)/cols;
		rectHeight=(int)(rectWidth/1.618);							// ����:���� = Ȳ�ݺ�� ����

		/*// ���� �������� ä��
			for(int i=0;i<rows;i++) {
				for(int j=0;j<cols;j++) {
					bricks[i][j]=new Brick((int)(Math.random()*13)+1,i,j);
				}
			}
		 */


		for(int i=0;i<rows;i++) {
			for(int j=0;j<cols;j++) {
				bricks[i][j]=new Brick(i%5,i,j, i==8&&j%2==0 );
			}
		}

		bar = new Bar("bar.png");
		ball.add(new Ball("basicBall.png"));

		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setSize(Main.SCREEN_WIDTH,Main.SCREEN_HEIGHT);
		drawPanel = new DrawPanel();
		scorePanel = new ScorePanel();
		//timeLabel = new JLabel("0�� ���");
		scoreLabel = new JLabel("SCORE : 0");
		scoreLabel.setForeground(Color.white);
		heart=6;// ü�� �ʱ�ȭ

		getContentPane().add(BorderLayout.CENTER, drawPanel);
		getContentPane().add(BorderLayout.NORTH, scorePanel);

		setVisible(true);

		setResizable(false);

		t1 = new Timer(1000, new TimerListener());
	}

	class Brick {
		int x, y;
		Color color;
		int stat;
		int eilan;
		boolean isPersonFinder;
		public Brick (int type, int row, int col, boolean isPersonFinder) {
			this.x=rectWidth*col;
			this.y=rectHeight*row;
			this.isPersonFinder=isPersonFinder;
			if(isPersonFinder) {
				personFinder++;
			}
			if(type!=0 && type<8) {
				color=new Color(255/type/2,100+type*20,200);			// ����� type���� ���� �ٸ��� �Ѵ�.
				stat=1;													// ������ stat�� ���߸� ������
			}
			if((int)(Math.random()*10)==1) {
				eilan = (int)(Math.random()*8)+1;
			}

		}
		void change() {

			if(stat>0) {
				stat--;
			}
			if(stat==0) {
				// ������ ������
				score+=50;
				scoreLabel.setText("SCORE : "+score);
				
				if(isPersonFinder) {
					isPersonFinder=false;
					personFinder--;	// �ΰ� Ž����(������)������ ���ְ� ������ ����
				}
				
				System.out.println(personFinder);
				for(int i=0;i<rows;i++) {
					for(int j=0;j<cols;j++) {
						if(bricks[i][j].stat>0) {
							return;
						}}	// ������ ��� ���������� �˻�. �ϳ��� ������ �Լ��� �������´�.
				}
				drawPanel.add(successLabel,BorderLayout.CENTER);		// ���� : JLabel�� t1�� ���� ������ ǥ�õȴ�. ��?
				//JLabel finalScoreLabel = new JLabel("���� : ");				// ���� ǥ��
				// scoreLabel�� �����ӿ� ǥ�õǰ� �ϱ�
				// timerListener �۵� ���ϰ� �ϱ�
				t1.stop();
				t2.stop();
			}
		}
	}

	class Bar extends ImageIcon{
		int img_x, img_y;
		int steps = 4;
		int width;
		public Bar (String img) {
			super("src/images/"+img);

			img_x=100;
			img_y=500;

			width = super.getIconWidth();
		}
	}

	class Ball extends ImageIcon{
		double img_x, img_y;

		int steps=2;

		double xSteps, ySteps;
		boolean isStick;
		int width;
		final int OVER_THE_BAR = 484;			// bar�� ����ִ� ���� ����

		public Ball (String img) {
			super("src/images/"+img);

			isStick=true;

			width = super.getIconWidth();

			img_x=bar.img_x+(int)(Math.random()*(bar.width-width));	// 100 ~ 190 ���� ��
			img_y=OVER_THE_BAR;	
		}

		void setSteps(Bar bar) {	// bar�� ���� ���� ������ ���� Ƣ�� �ϴ� �޼ҵ�
			if(isIncludedInBar(bar)) {
				xSteps = steps*((img_x+width/2)-(bar.img_x+bar.width/2))/(bar.width/2+1);			// bar�� ��� ��ġ : (center-bar.center) �� �ӵ�(���� ����)�� ��ȯ. 
				if(xSteps>steps-0.1) xSteps=steps-0.1;
				else if(xSteps<-steps+0.1) xSteps=steps*-1+0.1;
				ySteps = Math.sqrt((int)Math.pow(steps,2)-xSteps*xSteps)*-1;	// x�� ������ ���� ������ �ӵ��� �ǵ��� y�� ������ ����
			}
		}

		void setRandomSteps() {
			xSteps = (Math.random()*2*(steps-0.1))-(steps-0.1);
			ySteps = Math.sqrt(Math.pow(steps,2)-xSteps*xSteps);	// x�� ������ ���� ������ �ӵ��� �ǵ��� y�� ������ ����
		}

		void move() {
			img_x+=xSteps;
			img_y+=ySteps;
		}

		boolean isIncludedInBar(Bar bar) {	// bar�� ����ִ��� �Ǻ��ϴ� �޼ҵ�

			if(img_y >= OVER_THE_BAR && img_y <= OVER_THE_BAR+steps) {
				return img_x>=bar.img_x-width && img_x<=bar.img_x+bar.width;
			}
			return false;
		}

		void Break(Brick[][] b) {				// �� �޼ҵ�� ball.isStick�� false�� �� ����ǰ� ���� �����̰� �ϴ� TimerListener(t2)�� ���´�.

			int ballHCenter = (int)img_x+width/2;
			int ballVCenter = (int)img_y+width/2;

			int i=ballVCenter/rectHeight;	// ���� ���̰� �� �࿡ �ִ��� �Ǻ�
			int j=ballHCenter/rectWidth;	// ���� ��ġ�� �� ���� �ִ��� �Ǻ�
			/*	
				(int)img_y[+width]%rectHeight<steps		// ���� ���̰� (��)��輱�� ��� �ִ��� �Ǻ�
				(int)img_x[+width]%rectWidth<steps		// ���� ��ġ�� (��)��輱�� ��� �ִ��� �Ǻ�
			 */
			if(ySteps<0) {			// ���� ���� �� ��
				if((int)img_y%rectHeight<steps) {
					try{if(b[i-1][j].stat>0) {
						b[i-1][j].change();
						ySteps*=-1;
					}} catch(ArrayIndexOutOfBoundsException e) {
						// ���� ���̰� �迭�� ����� �� ������ ����
					}
				}
			}
			else {					// ���� �Ʒ��� �� ��
				if(((int)img_y+width)%rectHeight<steps) {
					try{if(b[i+1][j].stat>0) {
						b[i+1][j].change();
						ySteps*=-1;
					}} catch(ArrayIndexOutOfBoundsException e) {	// ���� ���̰� �迭�� ����� �� ������ ����
					}
				}
			}

			if(xSteps<0) {			// ���� �������� �� ��
				if((int)img_x%rectWidth<steps) {
					try{if(b[i][j-1].stat>0) {
						b[i][j-1].change();
						xSteps*=-1;
					}} catch(ArrayIndexOutOfBoundsException e) {	// ���� ���̰� �迭�� ����� �� ������ ����
					}
				}
			}
			else {					// ���� ���������� �� ��
				if(((int)img_x+width)%rectWidth<steps) {
					try{if(b[i][j+1].stat>0) {
						b[i][j+1].change();
						xSteps*=-1;
					}} catch(ArrayIndexOutOfBoundsException e) {	// ���� ���̰� �迭�� ����� �� ������ ����
					}
				}
			}

			// ������ �������� ����� �� ó��
			if(ySteps<0 && xSteps<0 && 
					(Math.sqrt(Math.pow(ballVCenter-rectHeight*i,2)+Math.pow(ballHCenter-rectWidth*j,2))<width/2)) {
				try{if(b[i-1][j-1].stat>0) {
					b[i-1][j-1].change();
					double tmp = -xSteps;
					xSteps=-ySteps;
					ySteps=tmp;
				}} catch(ArrayIndexOutOfBoundsException e) {	// ���� ���̰� �迭�� ����� �� ������ ����
				}
			}
			else if(ySteps<0 && xSteps>0 &&
					(Math.sqrt(Math.pow(ballVCenter-rectHeight*i,2)+Math.pow(ballHCenter-rectWidth*(j+1),2))<width/2)) {
				try{if(b[i-1][j+1].stat>0) {
					b[i-1][j+1].change();
					double tmp = xSteps;
					xSteps=ySteps;
					ySteps=tmp;
				}} catch(ArrayIndexOutOfBoundsException e) {	// ���� ���̰� �迭�� ����� �� ������ ����
				}
			}
			else if(ySteps>0 && xSteps>0 &&
					(Math.sqrt(Math.pow(ballVCenter-rectHeight*(i+1),2)+Math.pow(ballHCenter-rectWidth*(j+1),2))<width/2)) {
				try{if(b[i+1][j+1].stat>0) {
					b[i+1][j+1].change();
					double tmp = -xSteps;
					xSteps=-ySteps;
					ySteps=tmp;
				}} catch(ArrayIndexOutOfBoundsException e) {	// ���� ���̰� �迭�� ����� �� ������ ����
				}
			}
			else if(ySteps>0 && xSteps<0 &&
					(Math.sqrt(Math.pow(ballVCenter-rectHeight*(i+1),2)+Math.pow(ballHCenter-rectWidth*(j),2))<width/2)) {
				try{if(b[i+1][j-1].stat>0) {
					b[i+1][j-1].change();
					double tmp = xSteps;
					xSteps=ySteps;
					ySteps=tmp;
				}} catch(ArrayIndexOutOfBoundsException e) {	// ���� ���̰� �迭�� ����� �� ������ ����
				}
			}
		}
	}

	// drawPanel
	class DrawPanel extends JPanel {

		public DrawPanel() {
			left = new Timer(DELAY,new LeftMove());
			right = new Timer(DELAY, new RightMove());
			addKeyListener(new KeyListener() {
				public void keyPressed(KeyEvent e) {
					int keycode = e.getKeyCode();
					switch (keycode) {
					case KeyEvent.VK_LEFT:	left.restart();		break;	// Ű�� ������ Ÿ�̸Ӱ� ���۵ǰ� Ÿ�̸ӿ��� bar�� �����δ�.
					case KeyEvent.VK_RIGHT:	right.restart();	break;
					case KeyEvent.VK_SPACE:	
						for(Ball b:ball) {
							if(b.isStick==true) {
								b.setSteps(bar);
								b.isStick=false;
							}
						}
						t1.restart();
						break;
					}
					//repaint();
				}
				public void keyReleased(KeyEvent arg0) {	
					int keycode = arg0.getKeyCode();
					switch (keycode) {
					case KeyEvent.VK_LEFT:	left.stop();		break;	// Ű�� ������ Ÿ�̸Ӱ� ���۵ǰ� Ÿ�̸ӿ��� bar�� �����δ�.
					case KeyEvent.VK_RIGHT:	right.stop();		break;
					}
				}	// Ű�� ���� bar�� �����.
				public void keyTyped(KeyEvent arg0) {			}

			});
			this.requestFocus();
			setFocusable(true);

			try {
				personFinderIcon = ImageIO.read(new File("src/images/copyItem.png"));

			} catch (IOException e) {
				System.out.println("no image");
				System.exit(1);
			}
		}

		@Override
		public void paintComponent(Graphics g) {

			//g.setColor(Color.white);
			//g.fillRect(0,0,this.getWidth(), this.getHeight());						// ���� ĥ�ϱ�
			g.drawImage(background,0,0,Main.SCREEN_WIDTH, Main.SCREEN_HEIGHT, this);	// ����̹��� �ֱ�
			setOpaque(false);	// �����ϰ�

			g.drawImage(bar.getImage(), bar.img_x, bar.img_y, null);

			// �迭�� �ִ� ���� �׷��ֱ�
			for(int i=0;i<rows;i++) {
				for(int j=0;j<cols;j++) {
					if(bricks[i][j].stat>0) {
						g.setColor(bricks[i][j].color);
						g.fillRect(bricks[i][j].x, bricks[i][j].y, rectWidth, rectHeight);
						g.setColor(Color.white);
						g.drawRect(bricks[i][j].x, bricks[i][j].y, rectWidth, rectHeight);
						if(bricks[i][j].isPersonFinder) {
							g.drawImage(personFinderIcon, bricks[i][j].x+(rectWidth-personFinderIcon.getWidth())/2, bricks[i][j].y+(rectHeight-personFinderIcon.getWidth())/2,null);
						}
					}
				}
			}

			// �� �׷��ֱ�
			for(Ball b:ball) {
				g.drawImage(b.getImage(), (int)b.img_x, (int)b.img_y, null);
			}
		}
		/*
		@Override
		public void paint(Graphics g) {
			g.drawImage(background,0,0,Main.SCREEN_WIDTH, Main.SCREEN_HEIGHT, this);
			setOpaque(false);	// �����ϰ�
			super.paint(g);
		}
		 */
		/*
			if(!setGener) {
				for(Brick r:array) {
					if((e.getX()>r.x) && (e.getY()>r.y) && 
							(e.getX()<r.x+r.rectWidth) && (e.getY()<r.y+r.rectHeight)) {

						array.remove(r.change());
						System.out.println("�簢�� ���� "+array.size()+"�� ����");
						repaint();
						if(array.size()==0) {
							//timeLabel.setText("������" + timeElapsed+"�� ���� �������ϴ�.");		//������ ���ʸ��� �������ϴ�.
							frame.setVisible(false);
							t1.stop();
							t2.stop();
						}
						return;
					}
				}
			}
		}*/
		private class LeftMove implements ActionListener {
			public void actionPerformed (ActionEvent e) {
				if(bar.img_x>0){
					bar.img_x -= bar.steps;
					for(Ball b:ball) {
						if(b.isStick)
							b.img_x -= bar.steps;
					}
				}
				repaint();
			}
		}
		private class RightMove implements ActionListener {
			public void actionPerformed (ActionEvent e) {

				if(bar.img_x<Main.SCREEN_WIDTH-bar.width-6) {
					bar.img_x += bar.steps;
					for(Ball b:ball) {
						if(b.isStick)
							b.img_x += bar.steps;
					}
				}
				repaint();
			}
		}
	}
	class ScorePanel extends JPanel{

		BufferedImage img = null;
		int img_x = 600, img_y = 5;
		public ScorePanel (){
			try {
				img = ImageIO.read(new File("src/images/heart.png"));
			} catch (IOException e) {
				System.out.println("no image");
				System.exit(1);	
			}
		}
		@Override
		public void paintComponent(Graphics g) {
			g.setColor(new Color(50,46,145));
			g.fillRect(0,0,this.getWidth(), this.getHeight());	// ���� ĥ�ϱ�

			for(int i=0;i<heart;i++) {
				g.drawImage(img, img_x+i*30, img_y, null);
			}
		}
	}

	// Ÿ�̸�...
	public void go() {

		//scorePanel.add(timeLabel);
		scorePanel.add(scoreLabel);
		t2 = new Timer(DELAY, new TimerListener2());
		t2.start();
	}

	// ���� �����̰� �ϴ� Ÿ�̸�
	private class TimerListener2 implements ActionListener {
		public void actionPerformed (ActionEvent event) {
			for(Ball b:ball) {
				if(b.isStick==false) {
					b.move();
					if((b.img_x<0&&b.xSteps<0) || (b.img_x>Main.SCREEN_WIDTH-b.width && b.xSteps>0)) {
						b.xSteps*=-1;
					}
					else if((b.img_y<0&&b.ySteps<0)) {
						b.ySteps*=-1;
					}
					else if(b.img_y>Main.SCREEN_HEIGHT) {
						ball.remove(b);
						if(ball.size()==0) {
							heart--;
							//t1.stop();
							if(heart>0) {
								b=new Ball("basicBall.png");
							}
							else {
								drawPanel.add(failLabel);		// ���� : JLabel�� t1�� ���� ������ ǥ�õȴ�. ��?
								failLabel.setBounds((Main.SCREEN_WIDTH-547)/2,(Main.SCREEN_HEIGHT-276)/2,547,276);

								JLabel finalScoreLabel = new JLabel("���� : "+score);						// ���� ǥ��
								drawPanel.add(finalScoreLabel);
								finalScoreLabel.setFont(new Font("PFStardust",Font.BOLD, 40));
								finalScoreLabel.setBounds((Main.SCREEN_WIDTH-547)/2,450,200,50);		// �Ʒ��� �߰��ϰ� �;��µ� ���� �߰���
								// scoreLabel�� �����ӿ� ǥ�õǰ� �ϱ�
								// timerListener �۵� ���ϰ� �ϱ�

								//t1.restart();
								break;
							}
							ball.add(new Ball("basicBall.png"));
							break;
						}
					}
					b.setSteps(bar);
					b.Break(bricks);
				}
			}


			repaint();
		}
	}

	// �ð� ��� Ÿ�̸� ������
	private class TimerListener implements ActionListener {
		public void actionPerformed (ActionEvent event) {

			timeElapsed++;
			if(timeElapsed%10==0) {
				// ����� �ö�
				score-=personFinder;
				scoreLabel.setText("SCORE : "+score);
			}
			//timeLabel.setText(timeElapsed+"�� ���");
		}
	}
}
