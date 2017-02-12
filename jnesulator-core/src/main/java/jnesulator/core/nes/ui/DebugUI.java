package jnesulator.core.nes.ui;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.image.BufferedImage;

import javax.swing.JFrame;
import javax.swing.JOptionPane;

import jnesulator.core.nes.NES;

public class DebugUI extends JFrame {
	// StrokeInformer aStrokeInformer = new StrokeInformer();

	public class Repainter implements Runnable {

		@Override
		public void run() {
			fbuf.repaint();
		}
	}

	public class ShowFrame extends javax.swing.JPanel {

		public BufferedImage nextFrame;

		/**
		 *
		 */
		public ShowFrame() {
			this.setBounds(0, 0, xsize, ysize);
			this.setPreferredSize(new Dimension(xsize, ysize));
		}

		@Override
		public void paint(final Graphics g) {
			g.drawImage(nextFrame, 0, 0, xsize, ysize, null);
		}
	}

	private ShowFrame fbuf;

	private int xsize, ysize;

	private Repainter painter = new Repainter();

	public DebugUI(int height, int width) {
		this.xsize = height;
		this.ysize = width;
		fbuf = new ShowFrame();
		fbuf.setIgnoreRepaint(true);
	}

	public void messageBox(String s) {
		JOptionPane.showMessageDialog(fbuf, s);
	}

	public void run() {
		this.setTitle("jnesulator Debug " + NES.VERSION);
		this.setResizable(false);
		this.setDefaultCloseOperation(HIDE_ON_CLOSE);
		this.setContentPane(fbuf);
		this.pack();
		this.setVisible(true);
	}

	public void setFrame(BufferedImage b) {
		fbuf.nextFrame = b;

		java.awt.EventQueue.invokeLater(painter);
		// do the actual screen update on the event thread, basically all this
		// does is blit the new frame
	}
}
