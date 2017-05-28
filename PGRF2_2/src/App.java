import com.jogamp.opengl.GLCapabilities;
import com.jogamp.opengl.GLProfile;
import com.jogamp.opengl.awt.GLCanvas;
import com.jogamp.opengl.util.FPSAnimator;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import javax.swing.JButton;
import javax.swing.JMenuBar;
import javax.swing.JRadioButton;
import javax.swing.SwingUtilities;

public class App {
	private static final int FPS = 60; // animator's target frames per second
	public App() {
		
	}
	public void start() {
		try {
			Frame frame = new Frame("frame");
			frame.setSize(512, 384);
			
//			JMenuBar jMenuBar = new JMenuBar();
//			JButton butClock = new JButton("Clock");
//			jMenuBar.add(butClock);
//			JButton butSimpl = new JButton("Pyramid");
//			jMenuBar.add(butSimpl);
//			JButton butTeapot = new JButton("Teapot");
//			jMenuBar.add(butTeapot);
//			JButton butHelp = new JButton("Help");
//			jMenuBar.add(butHelp);
//			JRadioButton ortho = new JRadioButton("OrthoRH");
//			jMenuBar.add(ortho);
//			ortho.setSelected(false);
//			frame.setVisible(true);
//			frame.setFocusable(true);
//			frame.setLocationRelativeTo(null);
//			frame.add(jMenuBar, BorderLayout.PAGE_START);

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