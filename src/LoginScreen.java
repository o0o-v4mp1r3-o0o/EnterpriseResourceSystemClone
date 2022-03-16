import javax.swing.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class LoginScreen {
    private final String url = "jdbc:postgresql://localhost:5432/Proj";
    public static String user;
    private String pass;
    public static void main(String[] args) {
        LoginScreen loginScreen = new LoginScreen();
        SwingUtilities.invokeLater(() ->{
            loginScreen.loginPls();
        });
    }
    public void loginPls() {
        JFrame frame = new JFrame();
        JPanel jPanel = new JPanel();
        jPanel.setLayout(null);
        JLabel username = new JLabel("Username:");
        JLabel password = new JLabel("Password:");
        JTextField usernameinput = new JTextField();
        JPasswordField passwordinput = new JPasswordField();
        JButton login = new JButton("Login");
        setLoginListener(login,usernameinput,passwordinput,frame);
        jPanel.add(username);
        jPanel.add(password);
        jPanel.add(usernameinput);
        jPanel.add(passwordinput);
        jPanel.add(login);
        username.setBounds(10,10,70,20);
        password.setBounds(10,80,70,20);
        usernameinput.setBounds(100,10,200,40);
        passwordinput.setBounds(100,80,200,40);
        login.setBounds(100,140,100,50);
        frame.add(jPanel);
        frame.setSize(800,800);
        frame.setVisible(true);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }
    public void setLoginListener(JButton logger,JTextField username, JPasswordField password, JFrame frame){
        logger.addActionListener(e ->{
            user= username.getText();
            pass = String.valueOf(password.getPassword());
            Connection connect = connect();
            if(connect!=null){
                frame.dispose();
                dashboard dashboard = new dashboard();
                dashboard.c = connect;
                try {
                    dashboard.startProgram();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
            username.setText(null);
            password.setText(null);
        });
    }
    public Connection connect() {
        Connection conn = null;
        try {
            conn = DriverManager.getConnection(url, user, pass);
            System.out.println("Connected to the PostgreSQL server successfully.");
        } catch (SQLException e) {
            System.out.println(e.getMessage());
            JOptionPane.showConfirmDialog(null,"Wrong username or password, try again.","WARNING!",2,
                    JOptionPane.ERROR_MESSAGE);
        }

        return conn;
    }
}
