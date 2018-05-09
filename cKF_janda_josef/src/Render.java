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
 * @version 2018
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
	@SuppressWarnings("unused")
	private Texture texture;
	float uhel = 0, uhelX, uhelY;
	int mode = 1;
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
		gl.glMatrixMode(GL2.GL_MODELVIEW);
		gl.glGetFloatv(GL2.GL_MODELVIEW_MATRIX, m, 0);
		quadratic = glu.gluNewQuadric();
		quad = glu.gluNewQuadric();
		glu.gluQuadricNormals(quadratic, GLU.GLU_SMOOTH); // normaly pro
															// stinovani
		glu.gluQuadricNormals(quad, GLU.GLU_SMOOTH);
		glu.gluQuadricTexture(quad, true); // souradnice do textury
		java.io.InputStream is = getClass().getResourceAsStream("hour.png");
		if (is == null)
			System.out.println("File not found");
		else
			try {
				texture = TextureIO.newTexture(is, true, "png");
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
		float speed = 60; // pocet stupnu rotace za vterinu
		float step = speed * (mils - oldmils) / 1000.0f; // prekresleni(frame)
		oldmils = mils;

		// zapnuti nebo vypnuti viditelnosti pomoci "D"
		if (depth)
			gl.glEnable(GL2.GL_DEPTH_TEST);
		else
			gl.glDisable(GL2.GL_DEPTH_TEST);

		// mazeme image buffer i z-buffer
		gl.glClearColor(0f, 0f, 0.2f, 1f);
		gl.glClear(GL2.GL_COLOR_BUFFER_BIT | GL2.GL_DEPTH_BUFFER_BIT);
		gl.glMatrixMode(GL2.GL_MODELVIEW);

		// nastaveni mod "M"

		if (mode == -1) {
			mode = 1;
		}

		switch (mode) {

		case 0:
			// rotace mazanim matice a vypocet uhlu na zaklade fps
			gl.glLoadIdentity();
			gl.glTranslated(zoom, 0, 0);
			uhel = (uhel + step) % 360;
			gl.glRotatef(uhel, 0, 1, 1);
			break;

		case 1:
			// rotace podle zmeny pozice mysi, osy rotace zustavaji svisle a
			// vodorovne
			gl.glRotated(30, 1, 0, 1);
			gl.glLoadIdentity();
			gl.glTranslated(0, 0, zoom * 2);
			gl.glRotatef(dx, 0, 0, 1);
			gl.glRotatef(dy, 0, 1, 0);
			gl.glMultMatrixf(m, 0);
			gl.glGetFloatv(GL2.GL_MODELVIEW_MATRIX, m, 0);
			zoom = 0;
			dx = 0;
			dy = 0;
			gl.glRotatef(90, 0, 1, 0);
			gl.glRotatef(-90, 0, 0, 1);
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
		gl.glTexParameteri(GL2.GL_TEXTURE_2D, GL2.GL_TEXTURE_MIN_FILTER, GL2.GL_LINEAR_MIPMAP_LINEAR);

		// light parameters
		float SHINE_ALL_DIRECTIONS = 1;
		float[] lightPos = { 25, dx - width / 2, height / 2 - dy, 2.0f, SHINE_ALL_DIRECTIONS };
		float[] lightColorAmbient = { 0.3f, 0.3f, 0.3f, 1f };
		float[] lightColorSpecular = { 0.7f, 0.7f, 0.7f, 1f };

		// light parameters
		gl.glLightfv(GL2.GL_LIGHT1, GL2.GL_POSITION, lightPos, 0);
		gl.glLightfv(GL2.GL_LIGHT1, GL2.GL_AMBIENT, lightColorAmbient, 0);
		gl.glLightfv(GL2.GL_LIGHT1, GL2.GL_SPECULAR, lightColorSpecular, 0);

		// Set material properties.
		float[] rgba = { 0.4f, 0.6f, 0.9f };
		gl.glMaterialfv(GL2.GL_FRONT, GL2.GL_AMBIENT, rgba, 0);
		gl.glMaterialfv(GL2.GL_FRONT, GL2.GL_SPECULAR, rgba, 0);
		gl.glMaterialf(GL2.GL_FRONT, GL2.GL_SHININESS, 0.75f);//////////////

		gl.glMatrixMode(GL2.GL_MODELVIEW);
		gl.glScaled(3, 3, 3);

		gl.glBegin(GL2.GL_DEPTH_TEXTURE_MODE);
		gl.glEnable(GL2.GL_LIGHT1);
		gl.glEnable(GL2.GL_LIGHTING);
		glu.gluCylinder(quadratic, 5.5f, 4.0f, 0.7f, 100, 100);// ram hodin
		gl.glRotated(180, 0, 0, 1);
		glu.gluCylinder(quadratic, 4.0f, 4.0f, 0.7f, 100, 100);// ram hodin
		gl.glDisable(GL2.GL_LIGHT1);
		gl.glDisable(GL2.GL_LIGHTING);
		gl.glEnd();

		// engin
		gl.glBegin(GL2.GL_DEPTH_TEXTURE_MODE);
		gl.glColor3f(0, 0, 0);
		gl.glMatrixMode(GL2.GL_MODELVIEW);
		gl.glPushMatrix();
		gl.glOrtho(-5, 5, -5, 5, 0.0, 85.0);
		gl.glTranslated(0, 0, -2.6);
		glut.glutSolidCube(20);
		gl.glPopMatrix();
		gl.glEnd();

		// cifernik
		gl.glBegin(GL2.GL_DEPTH_TEXTURE_MODE);
		gl.glColor3f(1f, 1f, 1f);
		glu.gluDisk(quad, 0, 5, 50, 50);
		gl.glEnd();

		// zadek a boky
		gl.glBegin(GL2.GL_DEPTH_TEXTURE_MODE);
		gl.glEnable(GL2.GL_LIGHT1);
		gl.glEnable(GL2.GL_LIGHTING);
		glu.gluCylinder(quadratic, 5.5f, 5.5f, -1.0f, 100, 100);
		glu.gluCylinder(quadratic, -5f, 5.5f, -0.4f, 100, 100);
		gl.glDisable(GL2.GL_LIGHT1);
		gl.glDisable(GL2.GL_LIGHTING);
		gl.glEnd();
		// rucicky
		gl.glBegin(GL2.GL_DEPTH_TEXTURE_MODE);
		gl.glMatrixMode(GL2.GL_MODELVIEW);
		gl.glPushMatrix();
		gl.glScaled(3.5, 3.5, 3.5);
		gl.glColor3f(0.0f, 0.0f, 1.0f);// blue
		glut.glutSolidSphere(0.05, 20, 20);
		gl.glEnd();
		// hodinova
		gl.glPushMatrix();
		gl.glRotated(-90, Math.sin(hourRotation(cal.get(Calendar.HOUR), cal.get(Calendar.MINUTE))),
				-Math.cos(hourRotation(cal.get(Calendar.HOUR), cal.get(Calendar.MINUTE))), 0);
		glut.glutSolidCone(0.04, 0.6, 18, 18);
		gl.glPopMatrix();
		// Minutova
		gl.glPushMatrix();
		gl.glRotated(-90, Math.sin(minuteRotation(cal.get(Calendar.MINUTE))),
				-Math.cos(minuteRotation(cal.get(Calendar.MINUTE))), 0);
		glut.glutSolidCone(0.04, 0.9, 18, 18);
		gl.glPopMatrix();
		// Sekundova
		gl.glPushMatrix();
		gl.glColor3f(1.0f, 0.0f, 0.0f);// red
		gl.glRotated(-90, Math.sin(secondRotation(cal.get(Calendar.SECOND))),
				-Math.cos(secondRotation(cal.get(Calendar.SECOND))), 0);
		glut.glutSolidCone(0.03, 1.0, 20, 20);
		gl.glPopMatrix();
		gl.glPopMatrix();
		gl.glEnd();
	}

	/**
	 * metody hodin....
	 * vypocet velikosti pohybu
	 * mezi minutami
	 * na cif
	 * Hodiny
	 */
	public double hourRotation(double hour, double minute) {

		return (-(hour * 30 + minute * 0.5) + 90) * Math.PI / 180;
	}

	/**
	 * minuty
	 */
	public double minuteRotation(double minute) {
		return (-(minute * 6) + 90) * Math.PI / 180;
	}

	/**
	 * sekundy
	 */
	public double secondRotation(double second) {
		return (-(second * 6) + 90) * Math.PI / 180;
	}

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
			System.out.println("mode =" + mode);
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
	}

}