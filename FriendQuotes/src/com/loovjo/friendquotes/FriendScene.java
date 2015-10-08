package com.loovjo.friendquotes;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.event.KeyEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import com.loovjo.loo2D.scene.Scene;
import com.loovjo.loo2D.utils.FileLoader;
import com.loovjo.loo2D.utils.ImageLoader;
import com.loovjo.loo2D.utils.Line;
import com.loovjo.loo2D.utils.RandomUtils;
import com.loovjo.loo2D.utils.Vector;

public class FriendScene implements Scene {

	public ArrayList<String> quotes = new ArrayList<String>();

	public String currentQuote = "";

	public final int maxQuoteTime = 600;
	public final int alphaTime = 100;

	public int quoteTime = maxQuoteTime;
	
	public boolean illuminatify = false;

	public Document doc;

	public int illuminatiSize = 100;
	public int width, height;

	public ArrayList<Line> hearts = new ArrayList<Line>();

	public BufferedImage illuminati = createResizedCopy(ImageLoader.getImage("/Illumintati.png").toBufferedImage(),
			illuminatiSize, illuminatiSize, true);

	public float currentHue = 0;

	public static BufferedImage createResizedCopy(Image originalImage, int scaledWidth, int scaledHeight,
			boolean preserveAlpha) {
		System.out.println("resizing...");
		int imageType = preserveAlpha ? BufferedImage.TYPE_INT_RGB : BufferedImage.TYPE_INT_ARGB;
		BufferedImage scaledBI = new BufferedImage(scaledWidth, scaledHeight, imageType);
		Graphics2D g = scaledBI.createGraphics();
		if (preserveAlpha) {
			g.setComposite(AlphaComposite.Src);
		}
		g.drawImage(originalImage, 0, 0, scaledWidth, scaledHeight, null);
		g.dispose();
		return scaledBI;
	}

	public FriendScene() {
		String strQuotes = FileLoader.readFile("/Quotes.txt");
		for (String quote : strQuotes.split("\n")) {
			quotes.add(quote);
		}
		try {

			doc = Jsoup.connect("http://www.goodreads.com/quotes/tag?utf8=%E2%9C%93&id=friendship").get();
		} catch (IOException e) {
			e.printStackTrace();
		}
		loadNewQuote();
	}

	@Override
	public void update() {
		if (quoteTime++ > maxQuoteTime) {
			loadNewQuote();
		}
		currentHue += 0.001;
		for (int i = 0; i < hearts.size(); i++) {
			hearts.get(i).getEnd().distort(0.0001f);
			hearts.set(i, new Line(hearts.get(i).getStart().add(hearts.get(i).getEnd()), hearts.get(i).getEnd()));
			Vector start = hearts.get(i).getStart();
			Vector end = hearts.get(i).getEnd();
			if (start.getX() < 0) {
				start.setX(0);
				end = new Vector(-end.getX(), end.getY());
			}
			if (start.getX() > 1 - (float) illuminatiSize / width) {
				start.setX(1 - (float) illuminatiSize / width);
				end = new Vector(-end.getX(), end.getY());
			}
			if (start.getY() > 1 - (float) illuminatiSize / height) {
				start.setY(1 - (float) illuminatiSize / height);
				end = new Vector(end.getX(), -end.getY());
			}
			if (start.getY() < 0) {
				start.setY(0);
				end = new Vector(end.getX(), -end.getY());
			}
			hearts.set(i, new Line(start, end));
		}
	}

	private void loadNewQuote() {
		System.out.println("New quote!");

		if (false) {
			String quote = doc.getElementsByClass("quoteText")
					.get(RandomUtils.RAND.nextInt(doc.getElementsByClass("quoteText").size())).html();
			System.out.println(quote);
			Matcher m = Pattern.compile("“.*?”", Pattern.DOTALL).matcher(quote);
			m.find();
			currentQuote = m.group(0).replace("<br>", "");
		} else
			currentQuote = getRandomQuote();
		quoteTime = 0;

		hearts.clear();
		for (int i = 0; i < 10; i++)
			hearts.add(new Line(new Vector(Math.random(), Math.random()), new Vector(0, 0)));
	}

	private String getRandomQuote() {
		String oldQuote = currentQuote;
		while (currentQuote.equals(oldQuote)) {
			currentQuote = quotes.get(RandomUtils.RAND.nextInt(quotes.size()));
		}
		return currentQuote;
	}

	@Override
	public void render(Graphics g1, int width, int height) {
		this.width = width;
		this.height = height;
		Graphics2D g = (Graphics2D) g1;
		float alpha = 1;
		if (quoteTime < alphaTime) {
			alpha = (float) quoteTime / alphaTime;
		}
		if (quoteTime > maxQuoteTime - alphaTime) {
			alpha = 1 - ((float) (quoteTime - (maxQuoteTime - alphaTime)) / alphaTime);
		}
		RenderingHints rh = new RenderingHints(RenderingHints.KEY_TEXT_ANTIALIASING,
				RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
		g.setRenderingHints(rh);

		g.setColor(new Color(Color.HSBtoRGB(currentHue, 1, Math.min(1, alpha + 0.3f))));
		g.fillRect(0, 0, width, height);

		g.setColor(new Color(g.getColor().darker().getRed(), g.getColor().darker().getGreen(),
				g.getColor().darker().getBlue(), (int) Math.max(0, Math.min(255, alpha * 255))));
		drawHeart(g, width, height);

		g.setColor(new Color(1f, 1f, 1f, Math.max(0, Math.min(1, alpha))));
		g.setFont(new Font("Helvetica", Font.BOLD, 10));
		g.setFont(g.getFont().deriveFont(RandomUtils.getGoodFontSize(g, currentQuote, width - 50)));

		g.drawString(currentQuote, (width - g.getFontMetrics().stringWidth(currentQuote)) / 2,
				(height + g.getFont().getSize()) / 2);

	}

	private void drawHeart(Graphics2D g, int width, int height) {
		g.setFont(new Font("Helvetica", Font.BOLD, 100));
		for (Line v : hearts) {
			if (illuminatify)
				g.drawImage(illuminati, (int) (v.getStart().getX() * width), (int) (v.getStart().getY() * height),
						illuminatiSize, illuminatiSize, null);
			else
				g.drawString((char) 0x2764 + "", (int) (v.getStart().getX() * width),
						(int) (v.getStart().getY() * height) + g.getFontMetrics().getHeight());
		}
	}

	@Override
	public void mousePressed(Vector pos, int button) {
		// TODO Auto-generated method stub

	}

	@Override
	public void mouseReleased(Vector pos, int button) {
		// TODO Auto-generated method stub

	}

	@Override
	public void mouseMoved(Vector pos) {
		// TODO Auto-generated method stub

	}

	@Override
	public void keyPressed(int keyCode) {
		if (keyCode == KeyEvent.VK_SPACE) {
			if (quoteTime > alphaTime)
				quoteTime = maxQuoteTime - alphaTime;

		}
		if (keyCode == KeyEvent.VK_S)
			System.out.println(currentQuote);
	}

	@Override
	public void keyReleased(int keyCode) {
		// TODO Auto-generated method stub

	}

	@Override
	public void keyTyped(char key) {
		// TODO Auto-generated method stub

	}

	@Override
	public void mouseWheal(MouseWheelEvent e) {
		// TODO Auto-generated method stub

	}

}
