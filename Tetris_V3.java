import javax.swing.*;
import java.awt.event.*;
import java.awt.*;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.util.Random;
import java.awt.Color;
import java.io.PrintWriter;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

//Steuerung:
//Pfeiltasten Links/Rechts = Bewegen Form nach links/rechts
//Pfeiltaste Unten         = Schneller nach unten
//Leertaste                = Form drehen

public class Tetris_V3 extends JFrame implements KeyListener
{
  int _x           = 15;
  int _y           = 20;
  int _score       = 0;
  int taktrate     = 250;
  int _richtung    = 0;
  int random;
  int _position    = 0;
  int _scoreStreak = 0;
  
  boolean _fallenErlaubt = true;
  
  JPanel _pScore = new JPanel();
  
  JLabel _lScore = new JLabel("<html><body><h1 style=\"color:red;\">Punkte: " + _score + "</h1></body></html>");
  
  GridBagConstraints _gbc;
  
  boolean[][] _feste;        //Feste Figuren am Boden
  boolean[][] _pos0;         //Figuren, welche in der Luft sind 
  boolean[][] _pos1;
  boolean[][] _pos2;
  boolean[][] _pos3;
  int[][]     _farbenArray;  //Farbe
  JPanel[][]  _zellenPanelArray;
  
  Random rand    = new Random();
  Thread _thread = new Thread();
  Tetris_V3(String title)
  {
    super(title);
    setLayout(new GridBagLayout());
    
    _gbc = new GridBagConstraints();
    
    _feste               = new boolean[_x][_y];
    _pos0                = new boolean[_x][_y];
    _pos1                = new boolean[_x][_y];
    _pos2                = new boolean[_x][_y];
    _pos3                = new boolean[_x][_y];
    _farbenArray         = new int[_x][_y]; 
    _zellenPanelArray    = new JPanel[_x][_y];
    
    addKeyListener(this);
    for (int x = 0; x < _x; x++) {
      for (int y = 0; y < _y; y++) {
        _zellenPanelArray[x][y] = new JPanel();
        _zellenPanelArray[x][y].setPreferredSize(new Dimension(30,30));
        _feste[x][y]            = false;
        _pos0[x][y]             = false;
        _farbenArray[x][y]      = 0;
        _zellenPanelArray[x][y].setBackground(new Color(194,194,194));
        _gbc.gridx = x;
        _gbc.gridy = y;
        _gbc.insets = new Insets(1,1,1,1);
        add(_zellenPanelArray[x][y], _gbc);
      }
    }
    _gbc.gridwidth = _x;
    _gbc.gridheight = _y;
    _gbc.fill = GridBagConstraints.HORIZONTAL;
    _gbc.gridx = 0;
    _gbc.gridy = _y+1;
    _gbc.weighty = 0.5;
    _pScore.setBackground(new Color(200,200,200));
    _pScore.add(_lScore);
    add(_pScore, _gbc);
    setzeFigur();
    getContentPane().setBackground(new Color(28,134,238));
    setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    startGame();
  }
  
  public void verloren()
  {
    scoreEintragen();
    dispose();
    Tetris_Niederlage niederlageFrame = new Tetris_Niederlage("Du hast Verloren");
    niederlageFrame.pack();
    niederlageFrame.setSize(500,400);
    niederlageFrame.setVisible(true);
    niederlageFrame.setResizable(false);
    niederlageFrame.setLocation(500,200);
  }
  
  public void startGame()
  {
    _thread = new Thread(() -> {
      while (true)
      {
        pruefenUnten();
        try
        {
          Thread.sleep(taktrate);
        }
        catch(InterruptedException ignored)
        {
        }
      }
    });
    _thread.start(); // thread starten
  }
  
  public void scoreEintragen() 
  {
    String fileName = "log.txt";
    PrintWriter print = null;
    
    try {
      print = new PrintWriter(new BufferedWriter (new FileWriter(fileName, true)));
    } catch(IOException iox) {
    } 
    print.println(_score);
    print.close();
  }
  
  public void pruefenUnten()
  {
    _fallenErlaubt = true;
    for (int x = _x-1; x > -1; x--) {
      for (int y = _y-1; y > -1; y--) {
        if (((_pos2[x][y] && _position == 2) || (_pos0[x][y] && _position == 0) || (_pos1[x][y] && _position == 1) || (_pos3[x][y] && _position == 3)) && y < _y-1) {
          if (_feste[x][y+1]) {
            _fallenErlaubt = false;
          } // end of if
        } // end of if
        if (((_pos2[x][y] && _position == 2) || (_pos0[x][y] && _position == 0) || (_pos1[x][y] && _position == 1) || (_pos3[x][y] && _position == 3)) && y == _y-1) {
          _fallenErlaubt = false;
        } // end of if
        if (_feste[x][6] == true) {
          verloren();
          x = -1;
          y = -1;
          _thread.interrupt();
        } // end of if
      }
    }
    if (_fallenErlaubt) {
      fallen();
    } else {
      ueberschreiben();  
    } // end of if-else
  }
  
  public void fallen()
  {
    for (int y = _y-1; y > -1; y--) {
      for (int x = 0; x < _x; x++) {
        if (_pos2[x][y]) {
          if (y < _y-1) {
            _pos2[x][y+1] = true;
            _pos2[x][y]   = false;   
          } else if (_position == 2){
              ueberschreiben();
            } // end of if-else
        } // end of if
      }
    }
    
    for (int x = _x-1; x > -1; x--) {
      for (int y = _y-1; y > -1; y--) {
        if (_pos0[x][y] ) {
          if (y < _y-1) {
            _pos0[x][y+1] = true;
            _pos0[x][y] = false;
          } else if (_position == 0){
              ueberschreiben();   
            } // end of if-else
        } 
      }
    }
    
    for (int x = _x-1; x > -1; x--) {
      for (int y = _y-1; y > -1; y--) {
        if (_pos1[x][y] ) {
          if (y < _y-1) {
            _pos1[x][y+1] = true;
            _pos1[x][y] = false;
          } else if (_position == 1){
              ueberschreiben();   
            } // end of if-else
        } 
      }
    }
    
    for (int x = _x-1; x > -1; x--) {
      for (int y = _y-1; y > -1; y--) {
        if (_pos3[x][y] ) {
          if (y < _y-1) {
            _pos3[x][y+1] = true;
            _pos3[x][y] = false;
          } else if (_position == 3){
              ueberschreiben();   
            } // end of if-else
        } 
      }
    }
    male();
  }
  
  public void ueberschreiben()
  {
    for (int x = 0; x < _x; x++) {
      for (int y = 0; y < _y; y++) {
        if (_pos0[x][y] && _position == 0) {
          _feste[x][y] = true;
        } // end of if
        if (_pos2[x][y] && _position == 2) {
          _feste[x][y] = true;
        } // end of if
        if (_pos3[x][y] && _position == 3) {
          _feste[x][y] = true;
        } // end of if
        if (_pos1[x][y] && _position == 1) {
          _feste[x][y] = true;
        } // end of if
      }
    }
    for (int x = 0; x < _x; x++) {
      for (int y = 0; y < _y; y++) {
        _pos0[x][y] = false;
        _pos2[x][y] = false;
        _pos1[x][y] = false;
        _pos3[x][y] = false;
      }
    }
    reihenPruefung();
    setzeFigur();
  }
  
  public void reihenPruefung()
  {
    for (int y = 0; y < _y; y++) {
      int count = 0;
      for (int x = 0; x < _x; x++) {
        if (_feste[x][y]) {
          count++;
        } // end of if
      }
      if (count == _x) {
        reiheEntfernen(y);
      } // end of if
    }
  }
  
  public void reiheEntfernen(int y)
  {
    for (int i = 0; i < _x; i++) {
      _feste[i][y] = false;
    }
    _score+=15;
    _scoreStreak++;
    if (_scoreStreak > 1) {
      _score = _score + (15*_scoreStreak);
    } // end of if
    _lScore.setText("<html><body><h1 style=\"color:red;\">Punkte: " + _score + "</h1></body></html>");
    reihenNachUnten(y);
  }
  
  public void reihenNachUnten(int z)
  {    
    for (int x = _x-1; x > -1; x--) {
      for (int y = z; y > -1; y--) {
        if (y+1 < _y && _feste[x][y]) {
          _feste[x][y] = false;
          _feste[x][y+1] = true;
        } // end of if
      }
    }    
    reihenPruefung();
    male();
  }
  
  public void setzeFigur()
  {
    _scoreStreak = 0;
    random = rand.nextInt(6);
    //4eck
    if (random == 0) {
      _pos0[_x/2][0]   = true;
      _pos0[_x/2+1][0] = true;
      _pos0[_x/2][1]   = true;
      _pos0[_x/2+1][1] = true;
      
      _pos1[_x/2][0]   = true;
      _pos1[_x/2+1][0] = true;
      _pos1[_x/2][1]   = true;
      _pos1[_x/2+1][1] = true;
      
      _pos2[_x/2][0]   = true;
      _pos2[_x/2+1][0] = true;
      _pos2[_x/2][1]   = true;
      _pos2[_x/2+1][1] = true;
      
      _pos3[_x/2][0]   = true;
      _pos3[_x/2+1][0] = true;
      _pos3[_x/2][1]   = true;
      _pos3[_x/2+1][1] = true;
    } // end of if
    //3erLine
    if (random == 1) {
      _pos0[_x/2][1]   = true;
      _pos0[_x/2+1][1] = true;
      _pos0[_x/2-1][1] = true;
      
      _pos1[_x/2][0]   = true;
      _pos1[_x/2][1]   = true;
      _pos1[_x/2][2]   = true;
      
      _pos2[_x/2][1]   = true;        
      _pos2[_x/2+1][1] = true;
      _pos2[_x/2-1][1] = true;
      
      _pos3[_x/2][0]   = true;
      _pos3[_x/2][1]   = true;
      _pos3[_x/2][2]   = true;
    } // end of if
    //2erLine
    if (random == 2) {
      _pos0[_x/2][0]   = true;
      _pos0[_x/2+1][0] = true;
      
      _pos1[_x/2][0]   = true;
      _pos1[_x/2][1]   = true;
      
      _pos2[_x/2][0]   = true;
      _pos2[_x/2+1][0] = true;
      
      _pos3[_x/2][0]   = true;
      _pos3[_x/2][1]   = true;
    } // end of if
    //kleinesL
    if (random == 3) {
      _pos0[_x/2][0]   = true;
      _pos0[_x/2+1][0] = true;
      _pos0[_x/2][1]   = true;
      
      _pos1[_x/2][0]   = true;
      _pos1[_x/2+1][0] = true;
      _pos1[_x/2+1][1] = true;
      
      _pos2[_x/2][0]   = true;
      _pos2[_x/2+1][1] = true;
      _pos2[_x/2][1]   = true;
      
      _pos3[_x/2][0]   = true;
      _pos3[_x/2+1][1] = true;
      _pos3[_x/2+1][0] = true;
    } // end of if
    //großesL
    if (random == 4) {
      _pos0[_x/2][0]   = true;
      _pos0[_x/2][1]   = true;
      _pos0[_x/2][2]   = true;
      _pos0[_x/2+1][2] = true;
      
      _pos1[_x/2-1][1] = true;
      _pos1[_x/2][1]   = true;
      _pos1[_x/2+1][1] = true;
      _pos1[_x/2+1][2] = true;
      
      _pos2[_x/2][0]   = true;
      _pos2[_x/2][1]   = true;
      _pos2[_x/2][2]   = true;
      _pos2[_x/2+1][0] = true;
      
      _pos3[_x/2-1][1] = true;
      _pos3[_x/2][1]   = true;
      _pos3[_x/2+1][1] = true;
      _pos3[_x/2+1][0] = true;
    } // end of if
    //Z
    if (random == 5) {
      _pos0[_x/2][1]   = true;
      _pos0[_x/2+1][1] = true;
      _pos0[_x/2+1][2] = true;
      _pos0[_x/2+2][2] = true;
      
      _pos1[_x/2][0]   = true;
      _pos1[_x/2][1]   = true;
      _pos1[_x/2+1][1] = true;
      _pos1[_x/2+1][2] = true;
      
      _pos2[_x/2][2]   = true;
      _pos2[_x/2+1][1] = true;
      _pos2[_x/2+1][2] = true;
      _pos2[_x/2+2][1] = true;
      
      _pos3[_x/2+1][0] = true;
      _pos3[_x/2+1][1] = true;
      _pos3[_x/2][1]   = true;
      _pos3[_x/2][2]   = true;
    } // end of if  
    male();
  }
  
  public void male()
  {
    for (int x = 0; x < _x; x++) {
      for (int y = 0; y < _y; y++) {
        if (!_pos0[x][y]) {
          _zellenPanelArray[x][y].setBackground(new Color(194,194,194));
        } // end of if
        if (!_feste[x][y]) {
          _zellenPanelArray[x][y].setBackground(new Color(194,194,194));
        } // end of if
        if (_pos0[x][y] && _position == 0) {
          _zellenPanelArray[x][y].setBackground(new Color(0,0,255));
        } else {
          _zellenPanelArray[x][y].setBackground(new Color(194,194,194));  
        } // end of if-else
        if (_pos2[x][y] && _position == 2) {
          _zellenPanelArray[x][y].setBackground(new Color(0,0,255));
        }
        if (_pos1[x][y] && _position == 1) {
          _zellenPanelArray[x][y].setBackground(new Color(0,0,255));
        }
        if (_pos3[x][y] && _position == 3) {
          _zellenPanelArray[x][y].setBackground(new Color(0,0,255));
        }
        if (_feste[x][y]) {
          _zellenPanelArray[x][y].setBackground(new Color(0,255,0));
        } // end of if
        if (y == 6) {
          _zellenPanelArray[x][y].setBackground(new Color(255,0,0));
        } // end of if
      }
    }
  }
  
  public void drehen()
  {
    boolean allowed = true;
    switch (_position) {
      case  0: 
        for (int x = 0; x < _x; x++) {
          for (int y = 0; y < _y; y++) {
            if (_pos1[x][y] && _feste[x][y]) {
              allowed = false;
            }
          }
        }
        break;
      case  1: 
        for (int x = 0; x < _x; x++) {
          for (int y = 0; y < _y; y++) {
            if (_pos2[x][y] && _feste[x][y]) {
              allowed = false;
            }
          }
        }
        break;
      case  2: 
        for (int x = 0; x < _x; x++) {
          for (int y = 0; y < _y; y++) {
            if (_pos3[x][y] && _feste[x][y]) {
              allowed = false;
            }
          }
        }
        break;  
      default: 
        for (int x = 0; x < _x; x++) {
          for (int y = 0; y < _y; y++) {
            if (_pos0[x][y] && _feste[x][y]) {
              allowed = false;
            }
          }
        }
    } // end of switch
    
    if (allowed == true) {
      _position++;
    } // end of if
    if (_position == 4) {
      _position = 0;
    } // end of if  
    male();
  }
  
  public void bewegenLinks()
  {
    boolean _pos0Allowed = true;
    boolean _pos2Allowed = true;
    boolean _pos1Allowed = true;
    boolean _pos3Allowed = true;
    for (int x = 0; x < _x; x++) {
      for (int y = 0; y < _y; y++) {
        if (_pos0[x][y]) {
          if (x-1 < 0 || (_feste[x-1][y] && _position == 0)) {
            _pos0Allowed = false;
          } // end of if
        } // end of if
      }
    }
    for (int x = 0; x < _x; x++) {
      for (int y = 0; y < _y; y++) {
        if (_pos2[x][y]) {
          if (x-1 < 0 || (_feste[x-1][y] && _position == 2)) {
            _pos2Allowed = false;
          } // end of if
        } // end of if
      }
    }
    for (int x = 0; x < _x; x++) {
      for (int y = 0; y < _y; y++) {
        if (_pos1[x][y]) {
          if (x-1 < 0 || (_feste[x-1][y] && _position == 1)) {
            _pos1Allowed = false;
          } // end of if
        } // end of if
      }
    }
    for (int x = 0; x < _x; x++) {
      for (int y = 0; y < _y; y++) {
        if (_pos3[x][y]) {
          if (x-1 < 0 || (_feste[x-1][y] && _position == 3)) {
            _pos3Allowed = false;
          } // end of if
        } // end of if
      }
    }
    if (_pos0Allowed) {
      for (int x = 0; x < _x; x++) {
        for (int y = 0; y < _y; y++) {
          if (_pos0[x][y]) {
            _pos0[x][y]   = false;
            _pos0[x-1][y] = true;
          } // end of if                                
        }
      }
    } // end of if
    if (_pos2Allowed) {
      for (int y = _y-1; y > -1; y--) {
        for (int x = 0; x < _x; x++) {
          if (_pos2[x][y]) {
            _pos2[x-1][y] = true;
            _pos2[x][y]   = false;            
          } 
        }
      }
    } // end of if
    if (_pos1Allowed) {
      for (int y = _y-1; y > -1; y--) {
        for (int x = 0; x < _x; x++) {
          if (_pos1[x][y]) {
            _pos1[x-1][y] = true;
            _pos1[x][y]   = false;            
          } 
        }
      }
    } // end of if
    if (_pos3Allowed) {
      for (int y = _y-1; y > -1; y--) {
        for (int x = 0; x < _x; x++) {
          if (_pos3[x][y]) {
            _pos3[x-1][y] = true;
            _pos3[x][y]   = false;            
          } 
        }
      }
    } // end of if
    male();
  }
  
  public void bewegenRechts()
  {
    boolean _pos0Allowed = true;
    boolean _pos2Allowed = true;
    boolean _pos1Allowed = true;
    boolean _pos3Allowed = true;
    for (int x = _x-1; x > 0; x--) {
      for (int y = _y-1; y > 0; y--) {
        if (_pos0[x][y]) {
          if (x+1 > _x-1 || (_feste[x+1][y] && _position == 0)) {
            _pos0Allowed = false;
          } // end of if
        } // end of if
      }
    }
    for (int x = 0; x < _x; x++) {
      for (int y = 0; y < _y; y++) {
        if (_pos2[x][y]) {
          if (x+1 > _x-1 || (_feste[x+1][y] && _position == 2)) {
            _pos2Allowed = false;
          } // end of if
        } // end of if
      }
    }
    for (int x = 0; x < _x; x++) {
      for (int y = 0; y < _y; y++) {
        if (_pos1[x][y]) {
          if (x+1 > _x-1 || (_feste[x+1][y] && _position == 1)) {
            _pos1Allowed = false;
          } // end of if
        } // end of if
      }
    }
    for (int x = 0; x < _x; x++) {
      for (int y = 0; y < _y; y++) {
        if (_pos3[x][y]) {
          if (x+1 > _x-1 || (_feste[x+1][y] && _position == 3)) {
            _pos3Allowed = false;
          } // end of if
        } // end of if
      }
    }
    
    if (_pos0Allowed) {
      for (int x = _x-1; x > -1; x--) {
        for (int y = _y-1; y > -1; y--) {
          if (_pos0[x][y]) {
            _pos0[x][y]   = false;
            _pos0[x+1][y] = true;
          } // end of if
        }
      }
    }
    if (_pos2Allowed) {
      for (int y = _y-1; y > -1; y--) {
        for (int x = _x-1; x > -1; x--) {
          if (_pos2[x][y]) {
            _pos2[x+1][y] = true;
            _pos2[x][y]   = false;    
          } 
        }
      }
    } // end of if
    if (_pos1Allowed) {
      for (int y = _y-1; y > -1; y--) {
        for (int x = _x-1; x > -1; x--) {
          if (_pos1[x][y]) {
            _pos1[x+1][y] = true;
            _pos1[x][y]   = false;    
          } 
        }
      }
    } // end of if
    if (_pos3Allowed) {
      for (int y = _y-1; y > -1; y--) {
        for (int x = _x-1; x > -1; x--) {
          if (_pos3[x][y]) {
            _pos3[x+1][y] = true;
            _pos3[x][y]   = false;    
          } 
        }
      }
    } // end of if
    male();
  }  
  
  public void keyPressed(KeyEvent e)
  {
    int myKey = e.getKeyCode();
    if (myKey == KeyEvent.VK_RIGHT) {  
      bewegenRechts();
    }
    if (myKey == KeyEvent.VK_LEFT) {
      bewegenLinks();
    }
    if (myKey == KeyEvent.VK_DOWN) {
      pruefenUnten();
    }
    if (myKey == KeyEvent.VK_SPACE) {
      drehen();
    }
  }
  
  @Override
  public void keyReleased(KeyEvent e) {
  }
  
  public void keyTyped(KeyEvent e)
  {
  }
  
  public static void main(String[] args)
  {
    Tetris_V3 frame = new Tetris_V3("Tetris");
    frame.pack();
    frame.setSize(500,800);
    frame.setLocation(600,200);
    frame.setResizable(false);
    frame.setVisible(true);
  }
}
