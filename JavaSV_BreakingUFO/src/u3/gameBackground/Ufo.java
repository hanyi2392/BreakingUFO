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

	Timer t1;							// 경과시간 표시를 위한 시계
	Timer t2;							// 공을 움직이게 하는 시계
	Timer left, right;

	int timeElapsed = 0;				// 경과시간 0으로 초기화
	int score = 0;						// 점수
	
	final int DELAY = 4;				// 타이머 액션 발생 빈도/시간
	DrawPanel drawPanel;				// 벽돌 깨는 패널		
	ScorePanel scorePanel;				// 시간, 체력 표시되는 패널
	//JLabel timeLabel;
	JLabel scoreLabel;
	JLabel successLabel = new JLabel(new ImageIcon("src/images/cong.png"));
	JLabel failLabel = new JLabel(new ImageIcon("src/images/fail.png"));
	int heart;	// 체력

	int rows = 10;
	int cols = 18;

	int rectWidth, rectHeight;

	BufferedImage personFinderIcon;		// 벽돌에 표시할 인간 탐색기 아이콘. 벽돌이 깨지면 함께 없어진다.
	int personFinder;					// 인간 탐색기의 남은 개수. 사람을 1명씩 데려간다.

	Brick[][] bricks = new Brick[rows][cols];
	ArrayList<Ball> ball = new ArrayList<>();
	Bar bar;

	private Image background = new ImageIcon("src/images/nightBackground.png").getImage();	// 배경이미지

	public Ufo() {

		rectWidth=(Main.SCREEN_WIDTH)/cols;
		rectHeight=(int)(rectWidth/1.618);							// 넓이:높이 = 황금비로 맞춤

		/*// 블럭을 랜덤으로 채움
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
		//timeLabel = new JLabel("0초 경과");
		scoreLabel = new JLabel("SCORE : 0");
		scoreLabel.setForeground(Color.white);
		heart=6;// 체력 초기화

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
				color=new Color(255/type/2,100+type*20,200);			// 블록의 type별로 색을 다르게 한다.
				stat=1;													// 공으로 stat번 맞추면 없어짐
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
				// 아이템 떨어짐
				score+=50;
				scoreLabel.setText("SCORE : "+score);
				
				if(isPersonFinder) {
					isPersonFinder=false;
					personFinder--;	// 인간 탐색기(아이템)벽돌을 없애고 개수를 내림
				}
				
				System.out.println(personFinder);
				for(int i=0;i<rows;i++) {
					for(int j=0;j<cols;j++) {
						if(bricks[i][j].stat>0) {
							return;
						}}	// 벽돌이 모두 없어졌는지 검사. 하나라도 있으면 함수를 빠져나온다.
				}
				drawPanel.add(successLabel,BorderLayout.CENTER);		// 문제 : JLabel이 t1을 가게 만들어야 표시된다. 왜?
				//JLabel finalScoreLabel = new JLabel("점수 : ");				// 점수 표시
				// scoreLabel을 프레임에 표시되게 하기
				// timerListener 작동 못하게 하기
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
		final int OVER_THE_BAR = 484;			// bar와 닿아있는 공의 높이

		public Ball (String img) {
			super("src/images/"+img);

			isStick=true;

			width = super.getIconWidth();

			img_x=bar.img_x+(int)(Math.random()*(bar.width-width));	// 100 ~ 190 랜덤 값
			img_y=OVER_THE_BAR;	
		}

		void setSteps(Bar bar) {	// bar에 맞춘 방향 쪽으로 공이 튀게 하는 메소드
			if(isIncludedInBar(bar)) {
				xSteps = steps*((img_x+width/2)-(bar.img_x+bar.width/2))/(bar.width/2+1);			// bar에 닿는 위치 : (center-bar.center) 를 속도(방향 결정)로 변환. 
				if(xSteps>steps-0.1) xSteps=steps-0.1;
				else if(xSteps<-steps+0.1) xSteps=steps*-1+0.1;
				ySteps = Math.sqrt((int)Math.pow(steps,2)-xSteps*xSteps)*-1;	// x의 보폭에 따라 일정한 속도가 되도록 y의 보폭을 결정
			}
		}

		void setRandomSteps() {
			xSteps = (Math.random()*2*(steps-0.1))-(steps-0.1);
			ySteps = Math.sqrt(Math.pow(steps,2)-xSteps*xSteps);	// x의 보폭에 따라 일정한 속도가 되도록 y의 보폭을 결정
		}

		void move() {
			img_x+=xSteps;
			img_y+=ySteps;
		}

		boolean isIncludedInBar(Bar bar) {	// bar와 닿아있는지 판별하는 메소드

			if(img_y >= OVER_THE_BAR && img_y <= OVER_THE_BAR+steps) {
				return img_x>=bar.img_x-width && img_x<=bar.img_x+bar.width;
			}
			return false;
		}

		void Break(Brick[][] b) {				// 이 메소드는 ball.isStick이 false일 때 실행되고 공을 움직이게 하는 TimerListener(t2)에 적는다.

			int ballHCenter = (int)img_x+width/2;
			int ballVCenter = (int)img_y+width/2;

			int i=ballVCenter/rectHeight;	// 공의 높이가 몇 행에 있는지 판별
			int j=ballHCenter/rectWidth;	// 공의 위치가 몇 열에 있는지 판별
			/*	
				(int)img_y[+width]%rectHeight<steps		// 공의 높이가 (행)경계선과 닿아 있는지 판별
				(int)img_x[+width]%rectWidth<steps		// 공의 위치가 (열)경계선과 닿아 있는지 판별
			 */
			if(ySteps<0) {			// 공이 위로 갈 때
				if((int)img_y%rectHeight<steps) {
					try{if(b[i-1][j].stat>0) {
						b[i-1][j].change();
						ySteps*=-1;
					}} catch(ArrayIndexOutOfBoundsException e) {
						// 공의 높이가 배열을 벗어났을 때 에러를 무시
					}
				}
			}
			else {					// 공이 아래로 갈 때
				if(((int)img_y+width)%rectHeight<steps) {
					try{if(b[i+1][j].stat>0) {
						b[i+1][j].change();
						ySteps*=-1;
					}} catch(ArrayIndexOutOfBoundsException e) {	// 공의 높이가 배열을 벗어났을 때 에러를 무시
					}
				}
			}

			if(xSteps<0) {			// 공이 왼쪽으로 갈 때
				if((int)img_x%rectWidth<steps) {
					try{if(b[i][j-1].stat>0) {
						b[i][j-1].change();
						xSteps*=-1;
					}} catch(ArrayIndexOutOfBoundsException e) {	// 공의 높이가 배열을 벗어났을 때 에러를 무시
					}
				}
			}
			else {					// 공이 오른쪽으로 갈 때
				if(((int)img_x+width)%rectWidth<steps) {
					try{if(b[i][j+1].stat>0) {
						b[i][j+1].change();
						xSteps*=-1;
					}} catch(ArrayIndexOutOfBoundsException e) {	// 공의 높이가 배열을 벗어났을 때 에러를 무시
					}
				}
			}

			// 벽돌의 꼭지점에 닿았을 때 처리
			if(ySteps<0 && xSteps<0 && 
					(Math.sqrt(Math.pow(ballVCenter-rectHeight*i,2)+Math.pow(ballHCenter-rectWidth*j,2))<width/2)) {
				try{if(b[i-1][j-1].stat>0) {
					b[i-1][j-1].change();
					double tmp = -xSteps;
					xSteps=-ySteps;
					ySteps=tmp;
				}} catch(ArrayIndexOutOfBoundsException e) {	// 공의 높이가 배열을 벗어났을 때 에러를 무시
				}
			}
			else if(ySteps<0 && xSteps>0 &&
					(Math.sqrt(Math.pow(ballVCenter-rectHeight*i,2)+Math.pow(ballHCenter-rectWidth*(j+1),2))<width/2)) {
				try{if(b[i-1][j+1].stat>0) {
					b[i-1][j+1].change();
					double tmp = xSteps;
					xSteps=ySteps;
					ySteps=tmp;
				}} catch(ArrayIndexOutOfBoundsException e) {	// 공의 높이가 배열을 벗어났을 때 에러를 무시
				}
			}
			else if(ySteps>0 && xSteps>0 &&
					(Math.sqrt(Math.pow(ballVCenter-rectHeight*(i+1),2)+Math.pow(ballHCenter-rectWidth*(j+1),2))<width/2)) {
				try{if(b[i+1][j+1].stat>0) {
					b[i+1][j+1].change();
					double tmp = -xSteps;
					xSteps=-ySteps;
					ySteps=tmp;
				}} catch(ArrayIndexOutOfBoundsException e) {	// 공의 높이가 배열을 벗어났을 때 에러를 무시
				}
			}
			else if(ySteps>0 && xSteps<0 &&
					(Math.sqrt(Math.pow(ballVCenter-rectHeight*(i+1),2)+Math.pow(ballHCenter-rectWidth*(j),2))<width/2)) {
				try{if(b[i+1][j-1].stat>0) {
					b[i+1][j-1].change();
					double tmp = xSteps;
					xSteps=ySteps;
					ySteps=tmp;
				}} catch(ArrayIndexOutOfBoundsException e) {	// 공의 높이가 배열을 벗어났을 때 에러를 무시
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
					case KeyEvent.VK_LEFT:	left.restart();		break;	// 키를 누르면 타이머가 시작되고 타이머에서 bar를 움직인다.
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
					case KeyEvent.VK_LEFT:	left.stop();		break;	// 키를 누르면 타이머가 시작되고 타이머에서 bar를 움직인다.
					case KeyEvent.VK_RIGHT:	right.stop();		break;
					}
				}	// 키를 떼면 bar가 멈춘다.
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
			//g.fillRect(0,0,this.getWidth(), this.getHeight());						// 배경색 칠하기
			g.drawImage(background,0,0,Main.SCREEN_WIDTH, Main.SCREEN_HEIGHT, this);	// 배경이미지 넣기
			setOpaque(false);	// 투명하게

			g.drawImage(bar.getImage(), bar.img_x, bar.img_y, null);

			// 배열에 있는 벽돌 그려주기
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

			// 공 그려주기
			for(Ball b:ball) {
				g.drawImage(b.getImage(), (int)b.img_x, (int)b.img_y, null);
			}
		}
		/*
		@Override
		public void paint(Graphics g) {
			g.drawImage(background,0,0,Main.SCREEN_WIDTH, Main.SCREEN_HEIGHT, this);
			setOpaque(false);	// 투명하게
			super.paint(g);
		}
		 */
		/*
			if(!setGener) {
				for(Brick r:array) {
					if((e.getX()>r.x) && (e.getY()>r.y) && 
							(e.getX()<r.x+r.rectWidth) && (e.getY()<r.y+r.rectHeight)) {

						array.remove(r.change());
						System.out.println("사각형 맞춤 "+array.size()+"개 남음");
						repaint();
						if(array.size()==0) {
							//timeLabel.setText("게임이" + timeElapsed+"초 만에 끝났습니다.");		//게임이 몇초만에 끝났습니다.
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
			g.fillRect(0,0,this.getWidth(), this.getHeight());	// 배경색 칠하기

			for(int i=0;i<heart;i++) {
				g.drawImage(img, img_x+i*30, img_y, null);
			}
		}
	}

	// 타이머...
	public void go() {

		//scorePanel.add(timeLabel);
		scorePanel.add(scoreLabel);
		t2 = new Timer(DELAY, new TimerListener2());
		t2.start();
	}

	// 공을 움직이게 하는 타이머
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
								drawPanel.add(failLabel);		// 문제 : JLabel이 t1을 가게 만들어야 표시된다. 왜?
								failLabel.setBounds((Main.SCREEN_WIDTH-547)/2,(Main.SCREEN_HEIGHT-276)/2,547,276);

								JLabel finalScoreLabel = new JLabel("점수 : "+score);						// 점수 표시
								drawPanel.add(finalScoreLabel);
								finalScoreLabel.setFont(new Font("PFStardust",Font.BOLD, 40));
								finalScoreLabel.setBounds((Main.SCREEN_WIDTH-547)/2,450,200,50);		// 아래로 추가하고 싶었는데 위로 추가됨
								// scoreLabel을 프레임에 표시되게 하기
								// timerListener 작동 못하게 하기

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

	// 시간 재는 타이머 리스너
	private class TimerListener implements ActionListener {
		public void actionPerformed (ActionEvent event) {

			timeElapsed++;
			if(timeElapsed%10==0) {
				// 사람이 올라감
				score-=personFinder;
				scoreLabel.setText("SCORE : "+score);
			}
			//timeLabel.setText(timeElapsed+"초 경과");
		}
	}
}
