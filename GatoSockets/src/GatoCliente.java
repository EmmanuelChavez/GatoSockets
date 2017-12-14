import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Toolkit;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.JPanel;

public class GatoCliente implements Runnable {
	private JFrame frame;
	private final int WIDTH = 506;
	private final int HEIGHT = 527;
	private Thread thread;

	private Painter painter;
	
	protected static DataOutputStream dos;
	protected static DataInputStream dis;

	private BufferedImage board;
	private BufferedImage redX;
	private BufferedImage blueX;
	private BufferedImage redCircle;
	private BufferedImage blueCircle;

	private String[] espacios = new String[9];

	protected static boolean tuTurno = false;
	protected static boolean oponente = true;

	private boolean incapazDeComunicarse = false;
	private boolean gano = false;
	private boolean enemigoGano = false;
	private boolean empate = false;

	private int tamanioDeEspacios = 160;
	private int errores = 0;
	private int primerLugar = -1;
	private int segundoLugar = -1;

	private Font fuente = new Font("Verdana", Font.BOLD, 32);
	private Font fuentePequenia = new Font("Verdana", Font.BOLD, 20);
	private Font fuenteGrande = new Font("Verdana", Font.BOLD, 50);

	private String esperandoCadena = "Esperando a otro jugador";
	private String cadenaIncapazDeComunicarse = "Incapaz de comunicarse con un oponente.";
	private String cadenaGano = "Ganaste!";
	private String cadenaEnemigoGano = "El oponente gano!";
	private String cadenaEmpate = "El juego termino en un empate.";

	private int[][]ganados = new int[][] { { 0, 1, 2 }, { 3, 4, 5 }, { 6, 7, 8 }, { 0, 3, 6 }, { 1, 4, 7 }, { 2, 5, 8 }, { 0, 4, 8 }, { 2, 4, 6 } };

	private Servidor servidor;

	/**
	 * <pre>
	 * 0, 1, 2 
	 * 3, 4, 5 
	 * 6, 7, 8
	 * </pre>
	 */

	public GatoCliente() {
		
		servidor=new Servidor();

		cargarImagenes();

		painter = new Painter();
		painter.setPreferredSize(new Dimension(WIDTH, HEIGHT));

		if (!servidor.connect()) servidor.initializeServer();

		frame = new JFrame();
		frame.setTitle("Juego Gato");
		frame.setContentPane(painter);
		frame.setSize(WIDTH, HEIGHT);
		frame.setLocationRelativeTo(null);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setResizable(false);
		frame.setVisible(true);

		thread = new Thread(this, "Juego Gato");
		thread.start();
	}

	public void run() {
		while (true) {
			tick();
			painter.repaint();

			if (!oponente && !servidor.accepted) {
				servidor.listenForServerRequest();
			}

		}
	}

	private void render(Graphics g) {
		g.drawImage(board, 0, 0, null);
		if (incapazDeComunicarse) {
			g.setColor(Color.RED);
			g.setFont(fuentePequenia);
			Graphics2D g2 = (Graphics2D) g;
			g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
			int stringWidth = g2.getFontMetrics().stringWidth(cadenaIncapazDeComunicarse);
			g.drawString(cadenaIncapazDeComunicarse, WIDTH / 2 - stringWidth / 2, HEIGHT / 2);
			return;
		}

		if (servidor.accepted) {
			for (int i = 0; i < espacios.length; i++) {
				if (espacios[i] != null) {
					if (espacios[i].equals("X")) {
						if (oponente) {
							g.drawImage(redX, (i % 3) * tamanioDeEspacios + 10 * (i % 3), (int) (i / 3) * tamanioDeEspacios + 10 * (int) (i / 3), null);
						} else {
							g.drawImage(blueX, (i % 3) * tamanioDeEspacios + 10 * (i % 3), (int) (i / 3) * tamanioDeEspacios + 10 * (int) (i / 3), null);
						}
					} else if (espacios[i].equals("O")) {
						if (oponente) {
							g.drawImage(blueCircle, (i % 3) * tamanioDeEspacios + 10 * (i % 3), (int) (i / 3) * tamanioDeEspacios + 10 * (int) (i / 3), null);
						} else {
							g.drawImage(redCircle, (i % 3) * tamanioDeEspacios + 10 * (i % 3), (int) (i / 3) * tamanioDeEspacios + 10 * (int) (i / 3), null);
						}
					}
				}
			}
			if (gano || enemigoGano) {
				Graphics2D g2 = (Graphics2D) g;
				g2.setStroke(new BasicStroke(10));
				g.setColor(Color.BLACK);
				g.drawLine(primerLugar % 3 * tamanioDeEspacios + 10 * primerLugar % 3 + tamanioDeEspacios / 2, (int) (primerLugar / 3) * tamanioDeEspacios + 10 * (int) (primerLugar / 3) + tamanioDeEspacios / 2, segundoLugar % 3 * tamanioDeEspacios + 10 * segundoLugar % 3 + tamanioDeEspacios / 2, (int) (segundoLugar / 3) * tamanioDeEspacios + 10 * (int) (segundoLugar / 3) + tamanioDeEspacios / 2);

				g.setColor(Color.RED);
				g.setFont(fuenteGrande);
				if (gano) {
					int stringWidth = g2.getFontMetrics().stringWidth(cadenaGano);
					g.drawString(cadenaGano, WIDTH / 2 - stringWidth / 2, HEIGHT / 2);
				} else if (enemigoGano) {
					int stringWidth = g2.getFontMetrics().stringWidth(cadenaEnemigoGano);
					g.drawString(cadenaEnemigoGano, WIDTH / 2 - stringWidth / 2, HEIGHT / 2);
				}
			}
			if (empate) {
				Graphics2D g2 = (Graphics2D) g;
				g.setColor(Color.BLACK);
				g.setFont(fuenteGrande);
				int stringWidth = g2.getFontMetrics().stringWidth(cadenaEmpate);
				g.drawString(cadenaEmpate, WIDTH / 2 - stringWidth / 2, HEIGHT / 2);
			}
		} else {
			g.setColor(Color.RED);
			g.setFont(fuente);
			Graphics2D g2 = (Graphics2D) g;
			g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
			int stringWidth = g2.getFontMetrics().stringWidth(esperandoCadena);
			g.drawString(esperandoCadena, WIDTH / 2 - stringWidth / 2, HEIGHT / 2);
		}

	}

	private void tick() {
		if (errores >= 10) incapazDeComunicarse = true;

		if (!tuTurno && !incapazDeComunicarse) {
			try {
				int espacio = dis.readInt();
				if (oponente) espacios[espacio] = "X";
				else espacios[espacio] = "O";
				verificarSiEnemigoGano();
				verificarEmpate();
				tuTurno = true;
			} catch (IOException e) {
				e.printStackTrace();
				errores++;
			}
		}
	}

	private void verificarGanar() {
		for (int i = 0; i < ganados.length; i++) {
			if (oponente) {
				if (espacios[ganados[i][0]] == "O" && espacios[ganados[i][1]] == "O" && espacios[ganados[i][2]] == "O") {
					primerLugar = ganados[i][0];
					segundoLugar = ganados[i][2];
					gano = true;
				}
			} else {
				if (espacios[ganados[i][0]] == "X" && espacios[ganados[i][1]] == "X" && espacios[ganados[i][2]] == "X") {
					primerLugar = ganados[i][0];
					segundoLugar = ganados[i][2];
					gano = true;
				}
			}
		}
	}

	private void verificarSiEnemigoGano() {
		for (int i = 0; i < ganados.length; i++) {
			if (oponente) {
				if (espacios[ganados[i][0]] == "X" && espacios[ganados[i][1]] == "X" && espacios[ganados[i][2]] == "X") {
					primerLugar = ganados[i][0];
					segundoLugar = ganados[i][2];
					enemigoGano = true;
				}
			} else {
				if (espacios[ganados[i][0]] == "O" && espacios[ganados[i][1]] == "O" && espacios[ganados[i][2]] == "O") {
					primerLugar = ganados[i][0];
					segundoLugar = ganados[i][2];
					enemigoGano = true;
				}
			}
		}
	}

	private void verificarEmpate() {
		for (int i = 0; i < espacios.length; i++) {
			if (espacios[i] == null) {
				return;
			}
		}
		empate = true;
	}

	private void cargarImagenes() {
		try {
			board = ImageIO.read(getClass().getResourceAsStream("/board.png"));
			redX = ImageIO.read(getClass().getResourceAsStream("/redX.png"));
			redCircle = ImageIO.read(getClass().getResourceAsStream("/redCircle.png"));
			blueX = ImageIO.read(getClass().getResourceAsStream("/blueX.png"));
			blueCircle = ImageIO.read(getClass().getResourceAsStream("/blueCircle.png"));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@SuppressWarnings("unused")
	public static void main(String[] args) {
		
		GatoCliente ticTacToe = new GatoCliente();
		
	}

	private class Painter extends JPanel implements MouseListener {
		private static final long serialVersionUID = 1L;

		public Painter() {
			setFocusable(true);
			requestFocus();
			setBackground(Color.WHITE);
			addMouseListener(this);
		}

		@Override
		public void paintComponent(Graphics g) {
			super.paintComponent(g);
			render(g);
		}

		@Override
		public void mouseClicked(MouseEvent e) {
			if (servidor.accepted) {
				if (tuTurno && !incapazDeComunicarse && !gano && !enemigoGano) {
					int x = e.getX() / tamanioDeEspacios;
					int y = e.getY() / tamanioDeEspacios;
					y *= 3;
					int posicion = x + y;

					if (espacios[posicion] == null) {
						if (!oponente) espacios[posicion] = "X";
						else espacios[posicion] = "O";
						tuTurno = false;
						repaint();
						Toolkit.getDefaultToolkit().sync();

						try {
							dos.writeInt(posicion);
							dos.flush();
						} catch (IOException e1) {
							errores++;
							e1.printStackTrace();
						}

						System.out.println("SE HAN ENVIADO LOS DATOS");
						verificarGanar();
						verificarEmpate();

					}
				}
			}
		}

		@Override
		public void mousePressed(MouseEvent e) {

		}

		@Override
		public void mouseReleased(MouseEvent e) {

		}

		@Override
		public void mouseEntered(MouseEvent e) {

		}

		@Override
		public void mouseExited(MouseEvent e) {

		}

	}
	
}
