package com.siemens.tcloadtester.core;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.UnmarshalException;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;

import com.siemens.tcloadtester.TcLoadTester;
import com.siemens.tcloadtester.core.events.Logger;
import com.siemens.tcloadtester.core.events.WorkerEventListener;

/**
 * GUI class that initiates display and widgets.
 * 
 */
public class UserInterface implements WorkerEventListener {
	/**
	 * The display object
	 */
	private static Display display;
	/**
	 * The shell object.
	 */
	private static Shell shell;
	/**
	 * The table in which all worker information is shown.
	 */
	private static Table table;
	/**
	 * The start button.
	 */
	private static Button startButton;
	/**
	 * The stop button.
	 */
	private static Button stopButton;

	private static Button rereadButton;

	private static Button loadButton;

	private static Button outputButton;

	private static Button flushButton;

	private static Text markerIdText;

	private static Text outputFile;

	private static Label workerTotal;
	private static Label workerStarted;
	private static Label workerRunning;
	private static Label workerSleeping;
	private static Label workerFinished;
	private static Label workerError;

	private int wRunning;
	private int wSleeping;
	private int wFinished;
	private int wError;
	private int prevWStarted = 0;

	public final static Table getTable() {
		return table;
	}

	public final static void DisplayError(String msg) {
		MessageBox msgBox = new MessageBox(shell, SWT.OK | SWT.ICON_ERROR);
		msgBox.setText("An error has occurred");
		msgBox.setMessage(msg);
		msgBox.open();
	}

	public final static void DisplayError(String msg, Exception e) {
		MessageBox msgBox = new MessageBox(Display.getDefault().getActiveShell(), SWT.OK | SWT.ICON_ERROR);
		msgBox.setText("An error has occurred");

		if (e instanceof UnmarshalException) {
			UnmarshalException ume = (UnmarshalException) e;

			msg += "\n\nMessage:\n\n" + ume.getLinkedException().getMessage();
		}
		else if (e.getMessage() != null && !e.getMessage().equals("")){
			msg += "\n\nMessage:\n\n" + e.getMessage();
		}
		if (e.getStackTrace().length > 0) {
			msg += "\n\nStack trace:\n\n";
			for (StackTraceElement ste : e.getStackTrace()) {
				msg += ste.toString() + "\n";
			}
		}
		msgBox.setMessage(msg);
		msgBox.open();
	}

	/**
	 * Setup the application window and all its widgets.
	 * 
	 * @throws Exception
	 */
	public static final void init() throws Exception {
		display = new Display();

		shell = new Shell(display);
		shell.setText(TcLoadTester.appName + " v" + TcLoadTester.majorVersion + "." + TcLoadTester.minorVersion + " (Git: " + TcLoadTester.gitCommit + ")");
		final Image imgTeamcenter = new Image(display, TcLoadTester.class
				.getClassLoader().getResourceAsStream(
						"com/siemens/tcloadtester/images/teamcenter.png"));
		shell.setImage(imgTeamcenter);
		GridLayout gl = new GridLayout(1, false);
		gl.marginHeight = 0;
		gl.marginWidth = 0;
		gl.verticalSpacing = 0;
		shell.setLayout(gl);
		shell.setSize(990, 700);
		shell.setMinimumSize(990, 700);

		table = new Table(shell, SWT.MULTI | SWT.FULL_SELECTION | SWT.FLAT);
		table.setHeaderVisible(true);
		table.setLinesVisible(true);
		GridData gd = new GridData(SWT.FILL, SWT.FILL, true, true);
		table.setLayoutData(gd);

		TableColumn dummy = new TableColumn(table, SWT.NONE);
		dummy.setWidth(0);
		dummy.setMoveable(false);
		dummy.setResizable(false);
		TableColumn status = new TableColumn(table, SWT.NONE);
		status.setWidth(9);
		status.setMoveable(false);
		status.setResizable(false);
		TableColumn date = new TableColumn(table, SWT.NONE);
		date.setText("Date");
		date.setWidth(110);
		date.setMoveable(true);
		date.setResizable(false);
		TableColumn worker = new TableColumn(table, SWT.NONE);
		worker.setText("Worker");
		worker.setWidth(150);
		worker.setMoveable(true);
		worker.setResizable(true);
		TableColumn module = new TableColumn(table, SWT.NONE);
		module.setText("Module");
		module.setWidth(150);
		module.setMoveable(true);
		module.setResizable(true);
		TableColumn iteration = new TableColumn(table, SWT.RIGHT);
		iteration.setText("Iteration");
		iteration.setWidth(70);
		iteration.setMoveable(true);
		iteration.setResizable(false);
		TableColumn time = new TableColumn(table, SWT.RIGHT);
		time.setText("Elapsed");
		time.setWidth(70);
		time.setMoveable(true);
		time.setResizable(false);
		TableColumn progress = new TableColumn(table, SWT.RIGHT);
		progress.setText("Progress");
		progress.setWidth(58);
		progress.setMoveable(true);
		progress.setResizable(false);
		TableColumn misc = new TableColumn(table, SWT.NONE);
		misc.setText("Extra information");
		misc.setWidth(800);
		misc.setMoveable(false);
		misc.setResizable(true);

		Composite buttonBar = new Composite(shell, SWT.NONE);
		gl = new GridLayout(8, false);
		gl.marginHeight = 5;
		gl.marginWidth = 5;
		buttonBar.setLayout(gl);
		buttonBar.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));

		Composite c = new Composite(buttonBar, SWT.NONE);
		gl = new GridLayout(1, false);
		gl.marginHeight = 0;
		gl.marginWidth = 0;
		c.setLayout(gl);
		c.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));

		final Label outputLabel = new Label(c, SWT.NONE);
		outputLabel.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, true, false));
		outputLabel.setText("Output file:");
		outputFile = new Text(c, SWT.SINGLE | SWT.BORDER);
		outputFile.setLayoutData(new GridData(SWT.FILL, SWT.LEFT, true, false));
		if (TcLoadTester.configurationFile != null)
			outputFile.setText(TcLoadTester.configurationFile
					.getAbsolutePath());

		outputButton = new Button(buttonBar, SWT.FLAT);
		gd = new GridData(80, 42);
		gd.verticalAlignment = SWT.BOTTOM;
		outputButton.setLayoutData(gd);
		outputButton.setText("OUTPUT...");
		outputButton.setToolTipText("Set output file.");
		outputButton.setFont(new Font(display, display.getSystemFont()
				.getFontData()[0].getName(), 8, SWT.BOLD));

		flushButton = new Button(buttonBar, SWT.FLAT);
		gd.verticalAlignment = SWT.BOTTOM;
		flushButton.setLayoutData(gd);
		flushButton.setText("FLUSH");
		flushButton.setEnabled(false);
		flushButton.setToolTipText("Set output file.");
		flushButton.setFont(new Font(display, display.getSystemFont()
				.getFontData()[0].getName(), 8, SWT.BOLD));

		c = new Composite(buttonBar, SWT.NONE);
		gl = new GridLayout(1, false);
		gl.marginHeight = 0;
		gl.marginWidth = 0;
		c.setLayout(gl);
		c.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false));

		final Label markerLabel = new Label(c, SWT.NONE);
		markerLabel.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, true, false));
		markerLabel.setText("Marker:");
		markerIdText = new Text(c, SWT.SINGLE | SWT.BORDER);
		GridData gdMarker = new GridData(80, 15);
		gdMarker.verticalAlignment = SWT.BOTTOM;
		markerIdText.setLayoutData(gdMarker);
		markerIdText.setText(TcLoadTester.markerId);

		loadButton = new Button(buttonBar, SWT.FLAT);
		loadButton.setLayoutData(gd);
		loadButton.setText("OPEN...");
		loadButton.setToolTipText("Open configuration file.");
		loadButton.setFont(new Font(display, display.getSystemFont()
				.getFontData()[0].getName(), 8, SWT.BOLD));
		rereadButton = new Button(buttonBar, SWT.FLAT);
		rereadButton.setLayoutData(gd);
		rereadButton.setText("REREAD");
		rereadButton.setToolTipText("Reread configuration file.");
		rereadButton.setFont(new Font(display, display.getSystemFont()
				.getFontData()[0].getName(), 8, SWT.BOLD));
		rereadButton.setEnabled(false);

		startButton = new Button(buttonBar, SWT.FLAT);
		startButton.setLayoutData(gd);
		startButton.setImage(new Image(display, TcLoadTester.class
				.getClassLoader().getResourceAsStream(
						"com/siemens/tcloadtester/images/start.png")));
		startButton.setText("START");
		startButton.setFont(new Font(display, display.getSystemFont()
				.getFontData()[0].getName(), 8, SWT.BOLD));
		startButton.setEnabled(false);
		stopButton = new Button(buttonBar, SWT.FLAT);
		stopButton.setLayoutData(gd);
		stopButton.setImage(new Image(display, TcLoadTester.class
				.getClassLoader().getResourceAsStream(
						"com/siemens/tcloadtester/images/stop.png")));
		stopButton.setText("STOP");
		stopButton.setFont(new Font(display, display.getSystemFont()
				.getFontData()[0].getName(), 8, SWT.BOLD));
		stopButton.setEnabled(false);

		Label separator = new Label(shell, SWT.HORIZONTAL | SWT.SEPARATOR);
		separator.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		c = new Composite(shell, SWT.NONE);
		gl = new GridLayout(19, false);
		gl.marginHeight = 0;
		c.setLayout(gl);
		gd = new GridData(SWT.FILL, SWT.FILL, true, false);
		c.setLayoutData(gd);

		final Label fillerLabel = new Label(c, SWT.NONE);
		fillerLabel.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		separator = new Label(c, SWT.VERTICAL | SWT.SEPARATOR);
		separator.setLayoutData(new GridData(2, 12));

		final Label workerTotalLbl = new Label(c, SWT.NONE);
		workerTotalLbl.setLayoutData(new GridData(50, 16));
		workerTotalLbl.setText("Total:");

		workerTotal = new Label(c, SWT.RIGHT);
		workerTotal.setLayoutData(new GridData(25, 16));
		workerTotal.setText("0");

		separator = new Label(c, SWT.VERTICAL | SWT.SEPARATOR);
		separator.setLayoutData(new GridData(2, 12));

		final Label workerStartedLbl = new Label(c, SWT.NONE);
		workerStartedLbl.setLayoutData(new GridData(50, 16));
		workerStartedLbl.setText("Started:");

		workerStarted = new Label(c, SWT.RIGHT);
		workerStarted.setLayoutData(new GridData(25, 16));
		workerStarted.setText("0");

		separator = new Label(c, SWT.VERTICAL | SWT.SEPARATOR);
		separator.setLayoutData(new GridData(2, 12));

		final Label workerRunningLbl = new Label(c, SWT.NONE);
		workerRunningLbl.setLayoutData(new GridData(50, 16));
		workerRunningLbl.setText("Running:");

		workerRunning = new Label(c, SWT.RIGHT);
		workerRunning.setLayoutData(new GridData(25, 16));
		workerRunning.setText("0");

		separator = new Label(c, SWT.VERTICAL | SWT.SEPARATOR);
		separator.setLayoutData(new GridData(2, 12));

		final Label workerSleepingLbl = new Label(c, SWT.NONE);
		workerSleepingLbl.setLayoutData(new GridData(50, 16));
		workerSleepingLbl.setText("Sleeping:");

		workerSleeping = new Label(c, SWT.RIGHT);
		workerSleeping.setLayoutData(new GridData(25, 16));
		workerSleeping.setText("0");

		separator = new Label(c, SWT.VERTICAL | SWT.SEPARATOR);
		separator.setLayoutData(new GridData(2, 12));

		final Label workerFinishedLbl = new Label(c, SWT.NONE);
		workerFinishedLbl.setLayoutData(new GridData(50, 16));
		workerFinishedLbl.setText("Finished:");

		workerFinished = new Label(c, SWT.RIGHT);
		workerFinished.setLayoutData(new GridData(25, 16));
		workerFinished.setText("0");

		separator = new Label(c, SWT.VERTICAL | SWT.SEPARATOR);
		separator.setLayoutData(new GridData(2, 12));

		final Label workerErrorLbl = new Label(c, SWT.NONE);
		workerErrorLbl.setLayoutData(new GridData(50, 16));
		workerErrorLbl.setText("Error:");

		workerError = new Label(c, SWT.RIGHT);
		workerError.setLayoutData(new GridData(25, 16));
		workerError.setText("0");

		//
		// Listeners
		//
		loadButton.addMouseListener(new MouseListener() {
			@Override
			public void mouseUp(MouseEvent e) {
				table.deselectAll();
				table.notifyListeners(SWT.Selection, new Event());
				final FileDialog dlg = new FileDialog(shell, SWT.OPEN);
				dlg.setText("Select configuration file");
				dlg.setFilterExtensions(new String[] { "*.xml" });
				if (TcLoadTester.configurationFile != null
						&& TcLoadTester.configurationFile.exists())
					dlg.setFilterPath(TcLoadTester.configurationFile
							.getParent());
				else
					dlg.setFilterPath(TcLoadTester.appPath.getPath());
				String file = dlg.open();
				if (file != null) {
					TcLoadTester.configurationFile = new File(file);
					table.removeAll();
					TcLoadTester.start();
				}
				table.setFocus();
			}

			@Override
			public void mouseDown(MouseEvent e) {
			}

			@Override
			public void mouseDoubleClick(MouseEvent e) {
			}
		});

		outputFile.addFocusListener(new FocusListener() {
			@Override
			public void focusLost(FocusEvent e) {
				Text t = (Text) e.widget;
				if (t.getText().isEmpty())
					return;
				table.deselectAll();
				table.notifyListeners(SWT.Selection, new Event());
				File file = new File(t.getText());
				Logger.reset();
				try {
					file.createNewFile();
					TcLoadTester.outputFile = file;
					flushButton.setEnabled(true);
				} catch (IOException ex) {
					TcLoadTester.outputFile = null;
					DisplayError("File could not be created.");
				}
			}

			@Override
			public void focusGained(FocusEvent e) {
				flushButton.setEnabled(false);
			}
		});

		outputFile.addKeyListener(new KeyListener() {
			@Override
			public void keyReleased(KeyEvent e) {
				if (e.keyCode == 13) {
					table.setFocus();
				}
			}

			@Override
			public void keyPressed(KeyEvent e) {
			}
		});

		outputButton.addMouseListener(new MouseListener() {
			@Override
			public void mouseUp(MouseEvent e) {
				table.deselectAll();
				table.notifyListeners(SWT.Selection, new Event());
				final FileDialog dlg = new FileDialog(shell, SWT.SAVE);
				dlg.setText("Select output file");
				dlg.setFilterExtensions(new String[] { "*.csv" });
				String file = dlg.open();
				if (file != null) {
					TcLoadTester.outputFile = new File(file);
					outputFile.setText(TcLoadTester.outputFile
							.getAbsolutePath());
					outputFile.notifyListeners(SWT.FocusOut, new Event());
				}
				table.setFocus();
			}

			@Override
			public void mouseDown(MouseEvent e) {
			}

			@Override
			public void mouseDoubleClick(MouseEvent e) {
			}
		});

		flushButton.addMouseListener(new MouseListener() {
			@Override
			public void mouseUp(MouseEvent e) {
				Logger.flush();
			}

			@Override
			public void mouseDown(MouseEvent e) {
			}

			@Override
			public void mouseDoubleClick(MouseEvent e) {
			}
		});

		markerIdText.addKeyListener(new KeyListener() {

			@Override
			public void keyReleased(KeyEvent e) {
				if (e.keyCode == 13) {
					table.setFocus();
				}
			}

			@Override
			public void keyPressed(KeyEvent e) {
			}
		});

		markerIdText.addFocusListener(new FocusListener() {

			@Override
			public void focusLost(FocusEvent e) {
				TcLoadTester.markerId = markerIdText.getText();
			}

			@Override
			public void focusGained(FocusEvent e) {
			}
		});

		rereadButton.addMouseListener(new MouseListener() {
			@Override
			public void mouseUp(MouseEvent e) {
				table.deselectAll();
				table.notifyListeners(SWT.Selection, new Event());
				rereadButton.setEnabled(false);
				table.removeAll();
				table.setFocus();
				TcLoadTester.start();
			}

			@Override
			public void mouseDown(MouseEvent e) {
			}

			@Override
			public void mouseDoubleClick(MouseEvent e) {
			}
		});

		startButton.addMouseListener(new MouseListener() {
			@Override
			public void mouseUp(MouseEvent e) {
				final List<Worker> wl = new ArrayList<Worker>();
				for (TableItem item : table.getSelection()) {
					wl.add((Worker) item.getData("worker"));
				}
				startButton.setEnabled(false);
				table.deselectAll();
				Application.startWorkers(wl);
				table.setFocus();
			}

			@Override
			public void mouseDown(MouseEvent e) {
			}

			@Override
			public void mouseDoubleClick(MouseEvent e) {
			}
		});

		stopButton.addMouseListener(new MouseListener() {
			@Override
			public void mouseUp(MouseEvent e) {
				final List<Worker> wl = new ArrayList<Worker>();
				for (TableItem item : table.getSelection()) {
					wl.add((Worker) item.getData("worker"));
				}
				stopButton.setEnabled(false);
				table.deselectAll();
				Application.stopWorkers(wl);
				table.setFocus();
			}

			@Override
			public void mouseDown(MouseEvent e) {
			}

			@Override
			public void mouseDoubleClick(MouseEvent e) {
			}
		});

		table.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent e) {

				if (table.getSelectionCount() == 0) {
					if (startButton.isEnabled())
						startButton.setEnabled(false);
					if (stopButton.isEnabled())
						stopButton.setEnabled(false);
				} else {
					boolean started = false;
					boolean stopped = false;
					for (TableItem item : table.getSelection()) {
						Worker w = (Worker) item.getData("worker");

						if (w.mode == Mode.STARTED || w.status == Status.SCHEDULED)
							started = true;
						else
							stopped = true;
					}

					if (started && !stopButton.isEnabled())
						stopButton.setEnabled(true);
					else if (!started)
						stopButton.setEnabled(false);
					if (stopped && !startButton.isEnabled())
						startButton.setEnabled(true);
					else if (!stopped)
						startButton.setEnabled(false);
				}
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});

		table.addKeyListener(new KeyListener() {
			@Override
			public void keyReleased(KeyEvent e) {
				if (e.stateMask == SWT.CTRL && e.keyCode == 'a') {
					if (table.getSelectionCount() == table.getItems().length)
						table.deselectAll();
					else
						table.selectAll();
					table.notifyListeners(SWT.Selection, new Event());
				}
			}

			@Override
			public void keyPressed(KeyEvent e) {
			}
		});

		shell.open();
	}

	public static final void loop() {
		// Need to reset reread button if it has been pressed
		if (!rereadButton.isDisposed() && !rereadButton.isEnabled())
			rereadButton.setEnabled(true);
		while (!shell.isDisposed())
			if (!display.readAndDispatch())
				display.sleep();
		Logger.reset();
	}

	private final void updateWorkerStatus(
			final com.siemens.tcloadtester.core.events.Event e) {
		Worker w = (Worker) e.getSource();

		final TableItem t = w.getTableItem();
		final Status workerStatus = w.status;
		final Status prevWorkerStatus = w.prevStatus;
		final String id = w.getWorkerId();

		final String moduleId = w.getModuleId();
		final String moduleType = w.getModuleType();
		// Need to create new objects of variable below since they might change
		// from current thread while display thread is rendering
		final String iterations = new String(w.getIterations());
		final String timeDelta = new String(w.getModuleTimeDelta());
		final String percent = new String(w.getPercent());
		final String miscInfo = new String(w.getModuleMiscInfo());

		w.prevStatus = w.status;

		display.asyncExec(new Runnable() {
			@Override
			public void run() {

				switch (workerStatus) {
				case RUNNING:
					t.setBackground(1, new Color(display, 255, 255, 150));
					t.setText(new String[] { null, null, e.getDate(), id,
							moduleId, iterations, "---", percent, "" });

					wRunning++;
					workerRunning.setText(Integer.toString(wRunning));
					break;
				case SLEEPING:
					t.setBackground(1, new Color(display, 150, 150, 255));
					t.setText(new String[] { null, null, e.getDate(), id,
							moduleId, iterations, timeDelta, percent, miscInfo });

					wSleeping++;
					workerSleeping.setText(Integer.toString(wSleeping));
					break;
				case FINISHED:
					t.setBackground(1, new Color(display, 150, 255, 150));
					t.setText(new String[] { null, null, e.getDate(), id,
							moduleId, iterations, timeDelta, percent, miscInfo });

					wFinished++;
					workerFinished.setText(Integer.toString(wFinished));
					break;
				case RETRY:
				case ERROR:
					t.setBackground(1, new Color(display, 255, 150, 150));
					t.setText(new String[] { null, null, e.getDate(), id,
							moduleType, null, null, null,
							"Please see standard error console output for detailed error message" });
					wError++;
					workerError.setText(Integer.toString(wError));
					break;
				case SCHEDULED:
					t.setBackground(1, new Color(display, 150, 150, 255));
					break;
				default:
					break;
				}

				switch (prevWorkerStatus) {
				case RUNNING:
					wRunning--;
					workerRunning.setText(Integer.toString(wRunning));
					break;
				case SLEEPING:
					wSleeping--;
					workerSleeping.setText(Integer.toString(wSleeping));
					break;
				case FINISHED:
					wFinished--;
					workerFinished.setText(Integer.toString(wFinished));
					break;
				case RETRY:
				case ERROR:
					wError--;
					workerError.setText(Integer.toString(wError));
					break;
				default:
					break;
				}

				int wStarted = wRunning + wSleeping;
				if (wStarted != prevWStarted) {
					workerStarted.setText(Integer
							.toString(wRunning + wSleeping));

					if (wStarted > 0 && rereadButton.isEnabled()) {
						rereadButton.setEnabled(false);
						outputButton.setEnabled(false);
						loadButton.setEnabled(false);
						outputFile.setEnabled(false);
					} else if (wStarted == 0 && !rereadButton.isEnabled()) {
						rereadButton.setEnabled(true);
						outputButton.setEnabled(true);
						loadButton.setEnabled(true);
						outputFile.setEnabled(true);
					}
				}
				prevWStarted = wStarted;
			}
		});
	}

	@Override
	public void handleWorkerEvent(
			com.siemens.tcloadtester.core.events.Event e) {
		if (e.getSource() instanceof Worker) {
			updateWorkerStatus(e);
		}
	}

	public static void setTotalWorkers(final int nrOfWorkers) {
		display.asyncExec(new Runnable() {
			@Override
			public void run() {
				workerTotal.setText(Integer.toString(nrOfWorkers));
			}
		});
	}
}
