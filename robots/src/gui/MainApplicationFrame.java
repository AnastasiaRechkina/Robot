package gui;

import java.awt.Dimension;
import java.awt.Point;
import java.awt.Toolkit;

import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.beans.PropertyVetoException;
import java.io.BufferedInputStream;
import java.io.FileInputStream;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.HashMap;

import javax.swing.JDesktopPane;
import javax.swing.JFrame;
import javax.swing.JInternalFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import log.Logger;

/**
 * Что требуется сделать:
 * 1. Метод создания меню перегружен функционалом и трудно читается. 
 * Следует разделить его на серию более простых методов (или вообще выделить отдельный класс).
 *
 */
public class MainApplicationFrame extends JFrame
{
    private final JDesktopPane desktopPane = new JDesktopPane();
    
    public HashMap<String, Object> getProperties(JInternalFrame frame)
    {
        HashMap<String, Object> result = new HashMap<String, Object>();
        result.put("Location", frame.getLocation());
        result.put("Size", frame.getSize());
        result.put("Selected", frame.isSelected());
        if(frame instanceof LogWindow)
            result.put("Type", "Log");
        else if (frame instanceof GameWindow)
            result.put("Type", "Game");
        return result;
    }
    
    public MainApplicationFrame() {
        //Make the big window be indented 50 pixels from each edge
        //of the screen.
        int inset = 50;        
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        setBounds(inset, inset,
            screenSize.width  - inset*2,
            screenSize.height - inset*2);

        setContentPane(desktopPane);         
        setJMenuBar(generateMenuBar());  
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        
     try (FileInputStream is = new FileInputStream("./temp.out")) {
            try {
                ObjectInputStream ois = new ObjectInputStream(new BufferedInputStream(is));
                try {
                    ArrayList<HashMap<String,Object>> restored = (ArrayList<HashMap<String, Object>>) ois.readObject();
                    for (HashMap<String,Object> frame : restored)
                    {
                        if (frame.get("Type").equals("Log"))
                        {
                            LogWindow logWindow = createLogWindow();
                            logWindow.setLocation((Point)frame.get("Location"));
                            logWindow.setSize((Dimension)frame.get("Size"));
                            logWindow.setSelected((boolean)frame.get("Selected"));
                            addWindow(logWindow);
                        }
                        if (frame.get("Type").equals("Game"))
                        {
                            GameWindow gameWindow = new GameWindow();
                            gameWindow.setLocation((Point)frame.get("Location"));
                            gameWindow.setSize((Dimension)frame.get("Size"));
                            gameWindow.setSelected((boolean)frame.get("Selected"));
                            addWindow(gameWindow);
                        }
                    }
                } catch (ClassNotFoundException ex) {
                    ex.printStackTrace();
                } catch (PropertyVetoException e) {
                    e.printStackTrace();
                } finally {
                    ois.close();
                }
            } finally {
                is.close();
            }
        }
        catch (IOException ex)
        {ex.printStackTrace();
        }
    }  
   
    protected LogWindow createLogWindow()
    {
        LogWindow logWindow = new LogWindow(Logger.getDefaultLogSource());
        logWindow.setLocation(10,10);
        logWindow.setSize(300, 800);
        setMinimumSize(logWindow.getSize());
        logWindow.pack();
        Logger.debug("Протокол работает");
        return logWindow;
    }
    
    protected void addWindow(JInternalFrame frame)
    {  	
        desktopPane.add(frame);
        frame.setVisible(true);
    }
    private JMenuBar generateMenuBar()
    {
        JMenuBar menuBar = new JMenuBar();

        menuBar.add(getCreat());
        menuBar.add(getTest());
        menuBar.add(getLook());
        menuBar.add(getClose());
        return menuBar;
    }
    private JMenuItem getCreat(){
    	  JMenu some = new JMenu("Создать");
          some.setMnemonic(KeyEvent.VK_O);
          some.getAccessibleContext().setAccessibleDescription(
                  "Позволяет управлять окнами");
          
          {
              JMenuItem systemLookAndFeel = new JMenuItem("Создать протокол", KeyEvent.VK_U);
              systemLookAndFeel.addActionListener((event) -> {
              	LogWindow logWindow = createLogWindow();
                  addWindow(logWindow);
              });
              some.add(systemLookAndFeel);
          }

          {
              JMenuItem crossplatformLookAndFeel = new JMenuItem("Создать поле", KeyEvent.VK_I);
              crossplatformLookAndFeel.addActionListener((event) -> {
              	  GameWindow gameWindow = new GameWindow();
                    gameWindow.setSize(400,  400);
                    addWindow(gameWindow);

              });
              some.add(crossplatformLookAndFeel);
          }

     return some;
    }
    private JMenuItem getTest(){
    	 JMenu testMenu = new JMenu("Тесты");
        testMenu.setMnemonic(KeyEvent.VK_T);
        testMenu.getAccessibleContext().setAccessibleDescription(
                "Тестовые команды");
        
        {
            JMenuItem addLogMessageItem = new JMenuItem("Сообщение в лог", KeyEvent.VK_S);
            addLogMessageItem.addActionListener((event) -> {
                Logger.debug("Новая строка");
            });
            testMenu.add(addLogMessageItem);
        }
        return testMenu;
    }
    private JMenuItem getLook()
    {
    	JMenu lookAndFeelMenu = new JMenu("Режим отображения");
    lookAndFeelMenu.setMnemonic(KeyEvent.VK_V);
    lookAndFeelMenu.getAccessibleContext().setAccessibleDescription(
            "Управление режимом отображения приложения");
    
    {
        JMenuItem systemLookAndFeel = new JMenuItem("Системная схема", KeyEvent.VK_S);
        systemLookAndFeel.addActionListener((event) -> {
            setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            this.invalidate();
        });
        lookAndFeelMenu.add(systemLookAndFeel);
    }

    {
        JMenuItem crossplatformLookAndFeel = new JMenuItem("Универсальная схема", KeyEvent.VK_S);
        crossplatformLookAndFeel.addActionListener((event) -> {
            setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
            this.invalidate();
        });
        lookAndFeelMenu.add(crossplatformLookAndFeel);
    }

    	return lookAndFeelMenu;
    }
    private JMenuItem getClose()
    {
    	 JMenu exitItem = new JMenu("Выход");       
         exitItem.setMnemonic(KeyEvent.VK_X);
         exitItem.getAccessibleContext().setAccessibleDescription(
                 "Выход");
         {
        	 
    	 JMenuItem exitall = new JMenuItem("Закрыть приложение", KeyEvent.VK_Q);
            addWindowListener(new WindowAdapter() {          	    
                public void windowClosing(WindowEvent we) {
                    String ObjButtons[] = {"Да", "Нет"};
                    int PromptResult = JOptionPane.showOptionDialog(null,
                            "Закрыть окно?", "Требуется поддтверждение",
                            JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE, null,
                            ObjButtons, ObjButtons[0]);
                    if (PromptResult == 0) {
                    	 ArrayList<HashMap<String, Object>> frames = new ArrayList<HashMap<String, Object>>();
                         for (JInternalFrame frame : desktopPane.getAllFrames())
                         {
                             frames.add(getProperties(frame));
                         }
                         FileOutputStream fos;
						try {
							fos = new FileOutputStream("./temp.out");
                            ObjectOutputStream oos;
							oos = new ObjectOutputStream(fos);
							oos.writeObject(frames);
							oos.flush();
							oos.close();
						} catch (IOException e) {						
							e.printStackTrace();
						}
                         we.getWindow().setVisible(false);
                         System.exit(0);                    
                    }
                    if (PromptResult == 1) {
                    	setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
                    }
                }
            });
           
            exitall.addActionListener((event) -> {
                Toolkit.getDefaultToolkit().getSystemEventQueue().postEvent(
                        new WindowEvent(this, WindowEvent.WINDOW_CLOSING));
            }); 
            exitItem.add(exitall);
            }          
    return exitItem;
    
    }
    
    private void setLookAndFeel(String className)
    {
        try
        {
            UIManager.setLookAndFeel(className);
            SwingUtilities.updateComponentTreeUI(this);
        }
        catch (ClassNotFoundException | InstantiationException
            | IllegalAccessException | UnsupportedLookAndFeelException e)
        {
            // just ignore
        }
    }
}
