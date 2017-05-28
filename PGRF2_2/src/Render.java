import com.jogamp.opengl.GL2;
import com.jogamp.opengl.GLAutoDrawable;
import com.jogamp.opengl.GLEventListener;
import com.jogamp.opengl.GLException;
import com.jogamp.opengl.glu.GLU;
import com.jogamp.opengl.glu.GLUquadric;
import com.jogamp.opengl.util.gl2.GLUT;
import com.jogamp.opengl.util.texture.Texture;
import com.jogamp.opengl.util.texture.TextureIO;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.io.IOException;
import java.util.Calendar;
import java.awt.event.MouseEvent;
import java.awt.event.KeyListener;
import java.awt.event.KeyEvent;

/**
 * trida pro zobrazeni sceny v OpenGL: transformace v prostoru, FPS,
 * perspektiva, viditelnost, pohled
 * 
 * @author PGRF FIM UHK Josef Janda
 * @version 2017
 */
public class Render implements GLEventListener, MouseListener, MouseMotionListener, MouseWheelListener, KeyListener {

	GLUT glut;
	GLU glu;
	int width, height, dx, dy;
	int ox, oy;
	long oldmils;
	long oldFPSmils;
	double fps;
	double zoom;
	private Texture texture;
	float uhel = 0, uhelX, uhelY;
	int mode = 6;
	int param;
	float m[] = new float[16];

	boolean per = true, depth = true;

	GLUquadric quadratic;
	GLUquadric quad;

	@Override
	public void init(GLAutoDrawable glDrawable) {

		GL2 gl = glDrawable.getGL().getGL2();
		glut = new GLUT();
		glu = new GLU();

		gl.glEnable(GL2.GL_DEPTH_TEST);
		gl.glFrontFace(GL2.GL_CCW);
		gl.glPolygonMode(GL2.GL_FRONT, GL2.GL_FILL);
		gl.glPolygonMode(GL2.GL_BACK, GL2.GL_FILL);
		gl.glDisable(GL2.GL_CULL_FACE);
		gl.glDisable(GL2.GL_LIGHTING);
		gl.glMatrixMode(GL2.GL_MODELVIEW);
		gl.glGetFloatv(GL2.GL_MODELVIEW_MATRIX, m, 0);

		quadratic = glu.gluNewQuadric(); // nova kvadrika
		quad = glu.gluNewQuadric();

		glu.gluQuadricNormals(quadratic, GLU.GLU_SMOOTH); // normaly pro
															// stinovani
		glu.gluQuadricNormals(quad, GLU.GLU_SMOOTH);
		glu.gluQuadricTexture(quad, true); // souradnice do textury

		java.io.InputStream is = getClass().getResourceAsStream("hour.png");
		// System.out.println("Loading texture...");
		if (is == null)
			System.out.println("File not found");
		else
			try {
				texture = TextureIO.newTexture(is, true, "png");
				// System.out.println("New texture is loading...");
			} catch (GLException | IOException e) {
				e.printStackTrace();
			}
	}

	@Override
	public void display(GLAutoDrawable glDrawable) {
		GL2 gl = glDrawable.getGL().getGL2();
		Calendar cal = Calendar.getInstance();
		// vypocet fps, nastaveni rychlosti otaceni kvuli rychlosti prekresleni
		long mils = System.currentTimeMillis();
		if ((mils - oldFPSmils) > 300) {
			fps = 1000 / (double) (mils - oldmils + 1);
			oldFPSmils = mils;
		}
		// System.out.println(fps);
		float speed = 60; // pocet stupnu rotace za vterinu
		float step = speed * (mils - oldmils) / 1000.0f; // krok za jedno
															// prekresleni(frame)
		oldmils = mils;

		// zapnuti nebo vypnuti viditelnosti pomoci "D"
		if (depth)
			gl.glEnable(GL2.GL_DEPTH_TEST);
		else
			gl.glDisable(GL2.GL_DEPTH_TEST);

		// mazeme image buffer i z-buffer
		gl.glClearColor(0f, 0f, 0f, 1f);
		gl.glClear(GL2.GL_COLOR_BUFFER_BIT | GL2.GL_DEPTH_BUFFER_BIT);
		gl.glMatrixMode(GL2.GL_MODELVIEW);

		mode = mode % 7;
		if (mode == 0) {
			mode = 6;
		}

		switch (mode) {
		case 0:
			// rotace postupnou upravou matice
			gl.glTranslated(zoom, 0, 0);
			gl.glRotatef(1, 0, 0, 1);

			break;
		case 1:

			// rotace mazanim matice a zvetsovanim uhlu
			gl.glLoadIdentity();
			gl.glTranslated(zoom, 0, 0);
			uhel++;
			gl.glRotatef(uhel, 0, 1, 1);

			break;
		case 2:
			// rotace podle zmeny pozice mysi
			gl.glTranslated(zoom, 0, 0);
			gl.glRotatef(dx, 1, 0, 0);
			gl.glRotatef(dy, 0, 1, 0);

			break;
		case 3:
			// rotace podle fps
			gl.glTranslated(zoom, 0, 0);
			gl.glRotatef(step, 1, 0, 0);
			gl.glRotatef(step, 0, 1, 0);

			break;
		case 4:
			// rotace mazanim matice a vypocet uhlu na zaklade fps
			gl.glLoadIdentity();
			gl.glTranslated(zoom, 0, 0);
			uhel = (uhel + step) % 360;
			gl.glRotatef(uhel, 0, 1, 1);

			break;
		case 5:
			// rotace podle zmeny pozice mysi, osy rotace rotuji s telesem s
			// telesem
			gl.glLoadIdentity();
			// gl.glTranslated(zoom, 0, 0);
			gl.glMultMatrixf(m, 0);

			if (Math.abs(dx) > Math.abs(dy)) {
				gl.glRotatef(dx, 0, 1, 0);
				dx = 0;
			} else {
				gl.glRotatef(dy, 1, 0, 0);
				dy = 0;
			}
			gl.glGetFloatv(GL2.GL_MODELVIEW_MATRIX, m, 0);

			break;

		case 6:
			// rotace podle zmeny pozice mysi, osy rotace zustavaji svisle a
			// vodorovne
			gl.glLoadIdentity();
			gl.glTranslated(zoom * 2, 0, 0);
			gl.glRotatef(dx, 0, 0, 1);
			gl.glRotatef(dy, 0, 1, 0);
			gl.glMultMatrixf(m, 0);
			gl.glGetFloatv(GL2.GL_MODELVIEW_MATRIX, m, 0);
			zoom = 0;
			dx = 0;
			dy = 0;
			break;
		}

		gl.glMatrixMode(GL2.GL_PROJECTION);
		gl.glLoadIdentity();
		// nastaveni transformace zobrazovaciho objemu
		if (per)
			glu.gluPerspective(45, width / (float) height, 0.1f, 100.0f);
		else
			gl.glOrtho(-20 * width / (float) height, 20 * width / (float) height, -20, 20, 0.1f, 100.0f);

		// pohledova transformace
		// divame se do sceny z kladne osy x, osa z je svisla
		glu.gluLookAt(50, -10, 0, 0, 0, 0, 0, 0, 1);
		gl.glRotated(10, 1, 0, 0);

		gl.glEnable(GL2.GL_TEXTURE_2D);
		gl.glDisable(GL2.GL_LIGHTING);
		gl.glTexParameteri(GL2.GL_TEXTURE_2D, GL2.GL_TEXTURE_MIN_FILTER, GL2.GL_LINEAR_MIPMAP_LINEAR);

		// light parameters
		float SHINE_ALL_DIRECTIONS = 2;
		float[] lightPos = { -40, -10, 0, SHINE_ALL_DIRECTIONS };
		float[] lightColorAmbient = { 0.3f, 0.3f, 0.3f, 1f };
		float[] lightColorSpecular = { 0.7f, 0.7f, 0.7f, 1f };

		// light parameters
		gl.glLightfv(GL2.GL_LIGHT1, GL2.GL_POSITION, lightPos, 0);
		gl.glLightfv(GL2.GL_LIGHT1, GL2.GL_AMBIENT, lightColorAmbient, 0);
		gl.glLightfv(GL2.GL_LIGHT1, GL2.GL_SPECULAR, lightColorSpecular, 0);

		
		gl.glEnable(GL2.GL_LIGHT1);
		gl.glEnable(GL2.GL_LIGHTING);

		// Set material properties.
		float[] rgba = { 0.4f, 0.6f, 0.9f };
		gl.glMaterialfv(GL2.GL_FRONT, GL2.GL_AMBIENT, rgba, 0);
		gl.glMaterialfv(GL2.GL_FRONT, GL2.GL_SPECULAR, rgba, 0);
		gl.glMaterialf(GL2.GL_FRONT, GL2.GL_SHININESS, 0.5f);

		gl.glBegin(GL2.GL_DEPTH_TEXTURE_MODE);
		gl.glRotatef(95, 20, 20, 20);
		gl.glScaled(3, 3, 3);
		glu.gluCylinder(quadratic, 4.0f, 4.0f, 1.0f, 100, 100);// ram hodin
		glu.gluCylinder(quadratic, 5.5f, 4.0f, 1.0f, 100, 100);// ram hodin
		gl.glDisable(GL2.GL_LIGHT1);
		gl.glDisable(GL2.GL_LIGHTING);
		// cifernik
		gl.glColor3f(1f, 1f, 1f);
		glu.gluDisk(quad, 0, 5, 50, 50);
		// zadek a boky
		gl.glEnable(GL2.GL_LIGHT1);
		gl.glEnable(GL2.GL_LIGHTING);
		glu.gluCylinder(quadratic, 5.5f, 5.5f, -1.0f, 100, 100);
		glu.gluCylinder(quadratic, -5f, 5.5f, -0.4f, 100, 100);
		gl.glEnd();

		
		// arms
		gl.glBegin(GL2.GL_DEPTH_TEXTURE_MODE);
		gl.glMatrixMode(GL2.GL_MODELVIEW);
		gl.glScaled(3.5, 3.5, 3.5);
//		gl.glDisable(GL2.GL_LIGHT1);
//		gl.glDisable(GL2.GL_LIGHTING);
//		for (double i = 30; i <= 360; i += 30) {// cyklus pro tvorbu cisel
//			double rad = i * Math.PI / 180;
//			gl.glTranslated(Math.cos(rad), Math.sin(rad), 0);// hranice
//																// ciferniku
//			if (i == 90) {// 12ka
//				gl.glColor3f(0.0f, 1.0f, 0.0f);// green
//				glut.glutSolidSphere(0.03, 20, 20);
//
//			}
//			gl.glColor3f(0.0f, 1.0f, 0.0f);// green
//			glut.glutSolidSphere(0.01, 20, 20);
//			gl.glTranslated(-Math.cos(rad), -Math.sin(rad), 0);// hranice
//																// ciferniku
//		}
		gl.glColor3f(0.0f, 0.0f, 1.0f);// blue
		glut.glutSolidSphere(0.05, 20, 20);
		// Hour Hand
		gl.glPushMatrix();
		gl.glRotated(-90, Math.sin(hourRotation(cal.get(Calendar.HOUR), cal.get(Calendar.MINUTE))),
				-Math.cos(hourRotation(cal.get(Calendar.HOUR), cal.get(Calendar.MINUTE))), 0);
		glut.glutSolidCone(0.04, 0.6, 18, 18);
		gl.glPopMatrix();
		// Minute hand
		gl.glPushMatrix();
		gl.glRotated(-90, Math.sin(minuteRotation(cal.get(Calendar.MINUTE))),
				-Math.cos(minuteRotation(cal.get(Calendar.MINUTE))), 0);
		glut.glutSolidCone(0.04, 0.9, 18, 18);
		gl.glPopMatrix();
		// Second Hand
		gl.glPushMatrix();
		gl.glColor3f(1.0f, 0.0f, 0.0f);// red
		gl.glRotated(-90, Math.sin(secondRotation(cal.get(Calendar.SECOND))),
				-Math.cos(secondRotation(cal.get(Calendar.SECOND))), 0);
		glut.glutSolidCone(0.03, 1.0, 20, 20);
		gl.glPopMatrix();
		gl.glEnd();
	}

	/**
	 * metods for hours....
	 */
	public double hourRotation(double hour, double minute) {

		// System.out.println(minute);
		return (-(hour * 30 + minute * 0.5) + 90) * Math.PI / 180;
	}//

	/**
	 * minute
	 */
	public double minuteRotation(double minute) {// vypocet velikosti pohybu mezi minutami
													// na cif
		return (-(minute * 6) + 90) * Math.PI / 180;
	}//

	/**
	 * second
	 */
	public double secondRotation(double second) {
		// System.out.println(second);
		return (-(second * 6) + 90) * Math.PI / 180;
	}//

	@Override
	public void reshape(GLAutoDrawable glDrawable, int x, int y, int width, int height) {
		this.width = width;
		this.height = height;
		glDrawable.getGL().getGL2().glViewport(0, 0, width, height);
	}

	@Override
	public void mouseClicked(MouseEvent e) {
	}

	@Override
	public void mouseEntered(MouseEvent e) {
	}

	@Override
	public void mouseExited(MouseEvent e) {
	}

	@Override
	public void mousePressed(MouseEvent e) {
		if (e.getButton() == MouseEvent.BUTTON1) {
		}
		ox = e.getX();
		oy = e.getY();
	}

	@Override
	public void mouseReleased(MouseEvent e) {
		if (e.getButton() == MouseEvent.BUTTON1) {
		}
	}

	@Override
	public void mouseDragged(MouseEvent e) {
		dx = e.getX() - ox;
		dy = e.getY() - oy;
		ox = e.getX();
		oy = e.getY();
	}

	@Override
	public void mouseMoved(MouseEvent arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void keyPressed(KeyEvent e) {
	}

	@Override
	public void keyReleased(KeyEvent e) {
		switch (e.getKeyCode()) {
		case KeyEvent.VK_P:
			per = !per;
			break;
		case KeyEvent.VK_D:
			depth = !depth;
			break;
		case KeyEvent.VK_M:
			mode--;
			break;
		case KeyEvent.VK_W:
			zoom = zoom + 1;
			break;
		case KeyEvent.VK_S:
			zoom = zoom - 1;
			break;
		}
	}

	@Override
	public void keyTyped(KeyEvent e) {
	}

	@Override
	public void dispose(GLAutoDrawable glDrawable) {
	}

	@Override
	public void mouseWheelMoved(MouseWheelEvent e) {
		// TODO Auto-generated method stub

	}

}