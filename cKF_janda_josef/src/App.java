import com.jogamp.opengl.GLCapabilities;
import com.jogamp.opengl.GLProfile;
import com.jogamp.opengl.awt.GLCanvas;
import com.jogamp.opengl.util.FPSAnimator;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

public class App {
	private static final int FPS = 60; // animator's target frames per second

	public App() {

	}

	public void start() {
		try {
			Frame frame = new Frame("frame");
			frame.setSize(512, 384);
			ActionListener actionListener = new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent e) {
					frame.requestFocus();
					JOptionPane.showMessageDialog(frame,
							"W,S, pro ovládaní objektu \n" + "D => hloubka\n" + "M => spustí otáèení pohybu\n"
									+ "Mouse BTN1+dragged => otáèení objektem\n" + "Josef Janda, kai3, 9.5.2018\n",
							"Help", JOptionPane.PLAIN_MESSAGE);
				}
			};

			frame.setSize(512, 384);
			MenuBar menuBar = new MenuBar();
			Menu menu = new Menu("Menu");
			MenuItem m;
			m = new MenuItem("info");
			m.addActionListener(actionListener);
			menu.add(m);
			menuBar.add(menu);
			frame.setMenuBar(menuBar);

			// setup OpenGL Version 2
			GLProfile profile = GLProfile.get(GLProfile.GL2);
			GLCapabilities capabilities = new GLCapabilities(profile);
			capabilities.setRedBits(16);
			capabilities.setBlueBits(16);
			capabilities.setGreenBits(16);
			capabilities.setAlphaBits(16);
			capabilities.setDepthBits(24);

			// The canvas is the widget that's drawn in the JFrame
			GLCanvas canvas = new GLCanvas(capabilities);
			Render ren = new Render();
			canvas.addGLEventListener(ren);
			canvas.addMouseListener(ren);
			canvas.addMouseMotionListener(ren);
			canvas.addKeyListener(ren);
			canvas.setSize(512, 384);

			frame.add(canvas);

			final FPSAnimator animator = new FPSAnimator(canvas, FPS, true);

			frame.addWindowListener(new WindowAdapter() {
				@Override
				public void windowClosing(WindowEvent e) {
					new Thread() {
						@Override
						public void run() {
							if (animator.isStarted())
								animator.stop();
							System.exit(0);
						}
					}.start();
				}
			});
			frame.setTitle(ren.getClass().getName());
			frame.pack();
			frame.setVisible(true);
			animator.start(); // start the animation loop

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {
		SwingUtilities.invokeLater(() -> new App().start());

	}
}