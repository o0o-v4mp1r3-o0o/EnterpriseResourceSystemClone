import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.sql.*;
import java.util.ArrayList;

public class dashboard {
    private static JTable table;
    private static JPanel panel;
    public static Connection c;
    
    public void startProgram() throws SQLException {
        Statement stmnt = c.createStatement();
        ResultSet countrows = stmnt.executeQuery("select count(distinct tableid) from orders");
        countrows.next();
        int numrows = countrows.getInt(1);
        countrows.close();
        ResultSet rs = stmnt.executeQuery("select tableid as BidNumber,sum(price*quantity) as Total from orders group" +
                " by tableid order by tableid asc");
        Object[][] ss = new Object[numrows][2];
        int i = 0;
        while(rs.next()){
            int id = rs.getInt("bidnumber");
            float total = rs.getFloat("total");
            ss[i][0]=id;
            ss[i][1]=total;
            i++;
            System.out.println(id);
            System.out.println(total);
            System.out.println();
        }
        rs.close();
        JFrame frame = new JFrame();
        panel = new JPanel();
        panel.setLayout(new BoxLayout(panel,BoxLayout.Y_AXIS));
        frame.add(panel);
        JLabel dashtext = new JLabel("Dashboard");
        dashtext.setFont(new Font(dashtext.getName(),Font.PLAIN,100));
        panel.add(dashtext);
        table = new JTable(new DefaultTableModel(ss,new Object[]{"Bid ID","Total of Order", "Edit Order"})){
            public boolean isCellEditable(int row, int col) {
                if(col!=2){
                    return false;
                }
                return true;
            }
        };
        JButton editorder = new JButton("Edit");
        editorder.addActionListener(e ->{
            int x = (int) table.getValueAt(table.getSelectedRow(),0);
            App app = new App(c,x,this);
            try {
                app.pastBid(app);
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        });

        table.getColumnModel().getColumn(2).setCellRenderer(new ButtonRenderer());
        table.getColumnModel().getColumn(2).setCellEditor(new ButtonEditor(new JCheckBox(),editorder));
        JButton newOrder = new JButton("New Order");
        newOrder.addActionListener(e ->{
            App app = new App(c,this);
            try {
                app.pastBid(app);
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        });
        panel.add(newOrder);
        panel.add(Box.createVerticalStrut(150));
        panel.add(new JScrollPane(table));
        frame.setLayout(new GridLayout(2,1));
        frame.setSize(800,800);
        frame.setVisible(true);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }

    public void update(dashboard dashboard, int tableID, String addordelete) throws SQLException {
        Statement stmnt = c.createStatement();
        ResultSet countrows = stmnt.executeQuery("select count(distinct tableid) from orders");
        countrows.next();
        int numrows = countrows.getInt(1);
        countrows.close();
        ResultSet rs = stmnt.executeQuery("select tableid as BidNumber,sum(price*quantity) as Total from orders group" +
                " by tableid order by tableid asc");
        Object[][] ss = new Object[numrows][2];
        int i = 0;
        while(rs.next()){
            int id = rs.getInt("bidnumber");
            float total = rs.getFloat("Total");
            ss[i][0]=id;
            ss[i][1]=total;
            i++;
            System.out.println(id);
            System.out.println(total);
            System.out.println();
        }
        rs.close();
        DefaultTableModel model = (DefaultTableModel) table.getModel();
        ResultSet sss = stmnt.executeQuery("select sum(price) from orders where tableid="+tableID);
        sss.next();
        float cc = sss.getFloat(1);
        table.getColumnModel().getColumn(2).getCellEditor().stopCellEditing();
        if(addordelete.equals("ADD"))model.addRow(new Object[]{tableID, cc});
        if(addordelete.equals("DELETE"))model.removeRow(deleterow(tableID));
        sss.close();stmnt.close();
    }
    public int deleterow(int ID) {
        for (int i = 0; i < table.getRowCount(); i++) {
            try {
                int x = table.getValueAt(i, 0) instanceof String ? Integer.valueOf((String) table.getValueAt(i, 0)) :
                        (int) table.getValueAt(i, 0);
                if (x == ID) return i;
            } catch (NullPointerException e) {

            }
        }
        return -1;
    }
    static class ButtonRenderer extends JButton implements TableCellRenderer
    {
        public ButtonRenderer() {
            setOpaque(true);
        }
        public Component getTableCellRendererComponent(JTable table, Object value,
                                                       boolean isSelected, boolean hasFocus, int row, int column) {
            setText((value == null) ? "Edit" : value.toString());
            return this;
        }
    }
    static class ButtonEditor extends DefaultCellEditor
    {
        private String label;
        private JButton button;

        public ButtonEditor(JCheckBox checkBox, JButton button)
        {
            super(checkBox);
            this.button=button;
        }
        public Component getTableCellEditorComponent(JTable table, Object value,
                                                     boolean isSelected, int row, int column)
        {
            label = (value == null) ? "Edit" : value.toString();
            button.setText(label);
            return button;
        }
        public Object getCellEditorValue()
        {
            return label;
        }
    }
}
