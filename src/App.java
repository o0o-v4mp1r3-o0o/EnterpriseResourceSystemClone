import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;

public class App{
    private JButton j = new JButton("+ (Add row to table)");
    private JTable table;
    private JLabel total;
    private static int userID;
    private int tableID;
    private float totalA = 0;
    private static Connection c;
    private dashboard dashboard;
    App(){}
    App(Connection c, dashboard dashboard){
        this.c=c;
        this.dashboard= dashboard;
    }
    App(Connection c, int tableID, dashboard dashboard){
        this.c=c;
        this.tableID=tableID;
        this.dashboard= dashboard;
    }
    public void pastBid(App app) throws SQLException{
        Statement stmnt = c.createStatement();
        ResultSet getID = stmnt.executeQuery("SELECT ID FROM users WHERE username = '"+LoginScreen.user+"'");
        getID.next();
        userID = getID.getInt(1);
        getID.close();
        ArrayList<String> AutoCompleteList = new ArrayList();
        HashMap<String, Float> AutoCompleteNumbers = new HashMap<>();
        Object[][] ss;
        if(tableID!=0) {
        ResultSet countrows = stmnt.executeQuery("SELECT count(*) FROM orders WHERE tableid = " + tableID);
        countrows.next();
        int numrows = countrows.getInt(1);
        countrows.close();
        ResultSet rs = stmnt.executeQuery("SELECT * FROM orders WHERE tableid = " + tableID);
        ss = new Object[numrows][4];

            int i = 0;
            while (rs.next()) {
                String item = rs.getString("item");
                float price = rs.getFloat("price");
                int quantity = rs.getInt("quantity");
                ss[i][0] = item;
                ss[i][1] = price;
                ss[i][2] = quantity;
                ss[i][3] = price * quantity;
                AutoCompleteList.add(item);
                AutoCompleteNumbers.put(item, price);
                System.out.println("ITEM = " + item);
                System.out.println("PRICE = " + price);
                System.out.println("QUANTITY = " + quantity);
                System.out.println(i);
                i++;
            }
            rs.close();
        }else{
            int numrows = 1;
            ResultSet rs = stmnt.executeQuery("SELECT * FROM products");
            ss = new Object[numrows][4];
            int i = 0;
            while (rs.next()) {
                String item = rs.getString("item");
                float price = rs.getFloat("price");
                AutoCompleteList.add(item);
                AutoCompleteNumbers.put(item, price);
                System.out.println("ITEM = " + item);
                System.out.println("PRICE = " + price);
                System.out.println(i);
                i++;
            }
            rs.close();
        }
        JFrame jframe = new JFrame();
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        Java2sAutoComboBox itemBox = new Java2sAutoComboBox(AutoCompleteList);
        JTextField quantity = new JTextField();
        JTextField price = new JTextField();
        quantity.getDocument().addDocumentListener(new docReduce() {
            @Override
            void update() {
                DefaultTableModel model = (DefaultTableModel) table.getModel();
                Object getvalue = model.getValueAt(table.getSelectedRow(), 1);
                float dollarvalue = getvalue instanceof String ? Float.valueOf((String) getvalue) : (float) getvalue;
                int quantityvalue = Integer.valueOf(quantity.getText());
                float totalprice = dollarvalue * quantityvalue;
                model.setValueAt(totalprice, table.getSelectedRow(), 3);
                app.totalAmount();
                total.setText("TOTAL: " + totalA);
            }
        });
        price.getDocument().addDocumentListener(new docReduce() {
            @Override
            void update() {
                float dollarvalue = Float.valueOf(price.getText());
                DefaultTableModel model = (DefaultTableModel) table.getModel();
                Object getvalue = model.getValueAt(table.getSelectedRow(), 2);
                int quantityvalue = getvalue instanceof String ? Integer.valueOf((String) getvalue) : (int) getvalue;
                float totalprice = dollarvalue * quantityvalue;
                model.setValueAt(totalprice, table.getSelectedRow(), 3);
                app.totalAmount();
                total.setText("TOTAL: " + totalA);
            }
        });

        itemBox.setDataList(AutoCompleteList);
        itemBox.setMaximumRowCount(3);
        itemBox.setStrict(false);
        ((JTextField) itemBox.getEditor().getEditorComponent()).getDocument().addDocumentListener(new docReduce() {
            @Override
            void update() {
                if (itemBox.getSelectedItem() != null) {
                    if (AutoCompleteNumbers.containsKey(itemBox.getSelectedItem().toString())) {
                        DefaultTableModel model = (DefaultTableModel) table.getModel();
                        model.setValueAt(AutoCompleteNumbers.get(itemBox.getSelectedItem().toString()),
                                table.getSelectedRow(), 1);
                        model.setValueAt(1, table.getSelectedRow(), 2);
                        model.setValueAt(AutoCompleteNumbers.get(itemBox.getSelectedItem().toString()), table.getSelectedRow(), 3);
                        app.totalAmount();
                    }
                }
            }
        });

        if (tableID == 0) {
            table = new JTable(new DefaultTableModel(ss, new Object[]{"Item", "Price", "Quantity", "Item Total"})) {
                public boolean isCellEditable(int row, int col) {
                    if (col == 3) return false;
                    return true;
                }
            };
        } else {
            table = new JTable(new DefaultTableModel(ss,
                    new Object[]{"Item", "Price", "Quantity", "Item Total", "Delete Row"})) {
                public boolean isCellEditable(int row, int col) {
                    if (col == 3) return false;
                    return true;
                }
            };
        }
        total = new JLabel("TOTAL: "+totalA);
        table.getColumnModel().getColumn(0).setCellEditor(new DefaultCellEditor(itemBox));
        table.getColumnModel().getColumn(1).setCellEditor(new DefaultCellEditor(price));
        table.getColumnModel().getColumn(2).setCellEditor(new DefaultCellEditor(quantity));
        JButton delete = new JButton("Delete");
        delete.addActionListener(e ->{
            table.getColumnModel().getColumn(4).getCellEditor().stopCellEditing();
            DefaultTableModel model = (DefaultTableModel) table.getModel();
            model.removeRow(table.convertRowIndexToModel(table.getSelectedRow()));
            app.totalAmount();
        });
        if(tableID!=0) {
            table.getColumnModel().getColumn(4).setCellRenderer(new dashboard.ButtonRenderer());
            table.getColumnModel().getColumn(4).setCellEditor(new dashboard.ButtonEditor(new JCheckBox(), delete));
        }
        app.totalAmount();
        panel.add(new JScrollPane(table));
        table.setPreferredScrollableViewportSize(new Dimension(420, 250));
        table.setFillsViewportHeight(true);
        j.setBounds(0,0,100,100);
        j.addActionListener(e -> {
            DefaultTableModel model = (DefaultTableModel) table.getModel();
            model.addRow(new Object[]{"", ""});
            table.getColumnModel().getColumn(0).setCellEditor(new DefaultCellEditor(itemBox));
            table.getColumnModel().getColumn(1).setCellEditor(new DefaultCellEditor(price));
            table.getColumnModel().getColumn(2).setCellEditor(new DefaultCellEditor(quantity));
        });
        JButton submitButton = new JButton("Submit");
        if(tableID==0) {
            submitButton.addActionListener(e -> {
                try {
                    app.submitData(c);
                    dashboard.update(dashboard,tableID,"ADD");
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            });
        }else{
            submitButton.addActionListener(e -> {
                try {
                    app.removeData(c);
                    app.editData(c,dashboard);
                    dashboard.update(dashboard,tableID,"ADD");
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            });
        }
        JButton deleteWholeOrder = new JButton("Delete Entire Order");
        deleteWholeOrder.addActionListener(e ->{
            try {
                stmnt.executeUpdate("DELETE FROM orders WHERE tableid="+tableID);
                stmnt.close();
                dashboard.update(dashboard,tableID,"DELETE");
                jframe.dispose();
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        });
        panel.add(j);
        panel.add(total);
        panel.add(submitButton);
        if(tableID>0) panel.add(deleteWholeOrder);
        jframe.setLayout(new GridLayout(2,1));
        jframe.add(panel);
        jframe.add(new JPanel());
        jframe.setSize(800,800);
        jframe.setVisible(true);
        jframe.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
    }

    public float totalAmount(){
        float ans = 0; if(table==null) return 0;
        for(int i = 0; i < table.getRowCount(); i++){
            try {
                ans += table.getValueAt(i, 3) instanceof String ? Float.valueOf((String) table.getValueAt(i, 3)) : (float) table.getValueAt(i, 3);
            }catch(NullPointerException e){

            }
        }
        totalA = ans;
        total.setText("TOTAL: "+totalA);
        return ans;
    }
    public void submitData(Connection c) throws SQLException {
        Statement stmnt = c.createStatement();
        StringBuilder ss = new StringBuilder();
        ResultSet rs = stmnt.executeQuery("SELECT max(tableid) FROM orders");
        rs.next();
        int max = rs.getInt(1)+1; rs.close();
        for(int i = 0; i < table.getRowCount(); i++){
            ss.append(userID+", ");
            for(int d = 0; d < table.getColumnCount()-1; d++){
                ss.append(table.getValueAt(i,d) instanceof String?"'"+table.getValueAt(i,d)+"'"+", ":table.getValueAt(i,
                        d) + ", ");
            }
            System.out.println("INSERT INTO orders(\"ID\", item, price, quantity, tableid) VALUES(" + ss + max+")");
            stmnt.executeUpdate("INSERT INTO orders(\"ID\", item, price, quantity, tableid) VALUES(" + ss + max+")");
            ss = new StringBuilder();
        }
        stmnt.close();
        tableID=max;
    }
    public void editData(Connection c, dashboard dashboard) throws SQLException {
        Statement stmnt = c.createStatement();
        StringBuilder ss = new StringBuilder();
        ResultSet rs = stmnt.executeQuery("SELECT max(tableid) FROM orders");
        rs.next();
        int max = rs.getInt(1)+1; rs.close();
        for(int i = 0; i < table.getRowCount(); i++){
            ss.append(userID+", ");
            for(int d = 0; d < table.getColumnCount()-2; d++){
                ss.append(table.getValueAt(i,d) instanceof String?"'"+table.getValueAt(i,d)+"'"+", ":table.getValueAt(i,
                        d) + ", ");
            }
            System.out.println("INSERT INTO orders(\"ID\", item, price, quantity, tableid) VALUES(" + ss + max+")");
            stmnt.executeUpdate("INSERT INTO orders(\"ID\", item, price, quantity, tableid) VALUES(" + ss + max+")");
            ss = new StringBuilder();
        }
        stmnt.close();
        dashboard.update(dashboard,tableID,"DELETE");
        tableID=max;
    }

    public void removeData(Connection c) throws SQLException{
        Statement stmnt = c.createStatement();
        stmnt.executeUpdate("DELETE FROM orders WHERE tableid="+tableID);
    }
}
