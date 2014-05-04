import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.io.*;
import java.util.ArrayList;

/**
 * Created with IntelliJ IDEA.
 * User: Eric Anthony Amrhein
 * Date: 4/30/14
 * Time: 5:28 PM
 * This is probably the most complicated thing I have ever written;
 * It includes,
 * validation
 * Write to File
 * Load From File
 * Event Handling
 * List procedures
 *
 * Pardon the ugly look and feel I am still not used to SWING's GUI Settings
 * The Spec said to include a close button, but swings GUI comes with a close button on the top left corner
 * so that felt redundant
 */

/**
 *  GUI Window Class
 */
public class GUI extends JFrame {
    private boolean DEBUG = false;
    private  ArrayList <String[]> itemList = new ArrayList<String[]>();

    //Invoke input verification "Magic"
    private MyVerifier verifier = new MyVerifier();

   //setPanels
    private JPanel ListArea, Everything, Dept,
                   itemName, itemPrice, discount,
                   output, calcPanel;
    //setFormats
    private NumberFormat moneyFormat, percentFormat;
    private DecimalFormat decimalFormat;

    // setTextfields
    private JTextField nameField,priceField, discountField, outPutField;

    //setButton
    private JButton calcButton;
    public double DISCOUNT;


    /**
     *  Window constructor
     */
    public GUI(){
        //initial window settings
        super("Discount Calculator");
        setUPFormats();
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new GridLayout(2,1));
        createTableArray();

        //create table
        final Model model = new Model();
        final JTable table = new JTable(model);
        table.setFillsViewportHeight(true);
        final JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setViewportView(table);

        //Create Labels
        JLabel label0 = new JLabel("Department: ",SwingConstants.RIGHT);
        JLabel label1 = new JLabel("Item: ",SwingConstants.RIGHT);
        JLabel label2 = new JLabel("Price: ",SwingConstants.RIGHT);
        JLabel label3 = new JLabel("Discount: ",SwingConstants.RIGHT);
        JLabel label4 = new JLabel("New Price:  ",SwingConstants.RIGHT);

        //Create Textfields
        nameField = new JTextField(10);
        priceField = new JTextField(10);
        priceField.setInputVerifier(verifier);
        discountField = new JTextField(10);
        discountField.setInputVerifier(verifier);
        outPutField = new JTextField(10);

        //add combobox
        String[] depts = {"Dept1", "Dept2", "Dept3", "Dept4", "Dept5"};
        final JComboBox departments = new JComboBox(depts);

        //Create Button
        calcButton = new JButton("Calculate");
        calcButton.setPreferredSize(new Dimension(100,25));

        /* computes discount on Button click
         * prints result to screen
         *  adds information to "tabledata.txt"
         */
        calcButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                outPutField.setText(String.valueOf(computePrice(
                        Double.parseDouble(priceField.getText()),
                        Double.parseDouble(discountField.getText()))));

                String[] S1 = {
                        departments.getSelectedItem().toString(),
                        nameField.getText(),
                        priceField.getText(),
                        discountField.getText(),
                        outPutField.getText()
                };
                try{
                    PrintWriter pw = new PrintWriter(new BufferedWriter(new FileWriter("tabledata.txt",true)));
                    pw.println(S1[0]+" "+S1[1]+" "+S1[2]+" "+S1[3]+" "+S1[4]+" ");
                    pw.close();
                }
                catch (IOException ioe){
                    System.err.println("IOException: " + ioe.getMessage());
                }
                }

        });

        /*
        Create and Add Content to Panels
        Content is split between inputs and table
        */
        ListArea = new JPanel();
        ListArea.add(scrollPane);

        Dept = new JPanel();
        Dept.add(label0);
        Dept.add(departments);

        itemName = new JPanel();
        itemName.add(label1);
        itemName.add(nameField);

        itemPrice = new JPanel();
        itemPrice.add(label2);
        itemPrice.add(priceField);

        discount = new JPanel();
        discount.add(label3);
        discount.add(discountField);

        output = new JPanel();
        output.add(label4);
        output.add(outPutField);

        calcPanel = new JPanel();
        calcPanel.add(calcButton);

        Everything = new JPanel();
        Everything.setLayout(new GridLayout(6,0));
        Everything.add(Dept);
        Everything.add(itemName);
        Everything.add(itemPrice);
        Everything.add(discount);
        Everything.add(output);
        Everything.add(calcPanel);

        //add to contentPane
        add(ListArea);
        add(Everything);

        //pack and display
        pack();
        setVisible(true);
    }

    /**
     * Input Verification Class
     *
     */
    class MyVerifier extends InputVerifier implements ActionListener
    {
        //Set Minimum Price to 0
        //Set Min and Max Discount from 0% to 100%
        double MIN_AMOUNT = 0.0;
        double MIN_RATE = 0.0;
        double MAX_RATE = 100.0;

        //fixes input and tells if its good or not
        public boolean shouldYieldFocus(JComponent input){
            boolean inputOK  = verify(input);
            makeItprety(input);
             updateMoney();

            if (inputOK){
                return true;
            }
            else {
                Toolkit.getDefaultToolkit().beep();
                return false;
            }
        }
        protected void updateMoney() {
            double amount;
            double rate;

            try {
                amount = moneyFormat.parse(priceField.getText()).doubleValue();
            }
            catch (ParseException pe) {pe.printStackTrace();}

        }
       public boolean verify (JComponent input){
            return checkField(input, false);
        }
      protected void makeItprety(JComponent input){
          checkField(input, true);
      }
        protected boolean checkField(JComponent input, boolean changeIt){
            if (input== priceField){
                return checkPriceField(changeIt);
            }
            else if (input==discountField){
                return checkDiscountField(changeIt);
            }
            else {
                return true; //shouldn't happen
            }
        }
    // Check if the price field is valid. If it is valid returns true;
    // otherwise returns false.
    protected boolean checkPriceField(boolean change)
    {
        boolean wasValid = true;
        double amount=0.0;
        try{
            amount = moneyFormat.parse(priceField.getText()).doubleValue();
        }catch (ParseException pe){
            wasValid = false;
        }
        if((amount<MIN_AMOUNT)){
            wasValid = false;
            if(change){
                if(amount < MIN_AMOUNT){
                    amount = MIN_AMOUNT;
                } else {
                    amount = 0;
                }
            }
        }
        if (change){
            priceField.setText(moneyFormat.format(amount));
            priceField.selectAll();
        }
        return  wasValid;
    }
    //Checks if discount field is valid
    protected boolean checkDiscountField(boolean change){
        boolean wasValid = true;
        double rate = DISCOUNT;

        try{
            rate = percentFormat.parse(discountField.getText()).doubleValue();
        } catch (ParseException pe){
            wasValid = false;
            pe.printStackTrace();
        }
        if((rate<MIN_RATE)||(rate>MAX_RATE)){
            wasValid = false;
            if (change){
                if (rate<MAX_RATE) {
                    rate = MIN_RATE;
                }
                else if (rate>MAX_RATE){
                    rate = MAX_RATE;
                }
            }
        }
        if (change){
            discountField.setText(percentFormat.format(rate));
            discountField.selectAll();
        }
        return wasValid;

    }
        //Auto Corrects user action after field is yielded
        public void  actionPerformed (ActionEvent e){
            JTextField source = (JTextField)e.getSource();
            shouldYieldFocus(source);
            source.selectAll();
        }
    }
    /*
    Discount Calculation
     */
    double computePrice(double price, double discount)
    {
        return (price*((100 - discount)/100));
    }
    //Decimal Formats
    private void setUPFormats(){
        moneyFormat = (NumberFormat) NumberFormat.getNumberInstance();
        percentFormat = NumberFormat.getNumberInstance();
        percentFormat.setMinimumFractionDigits(3);

        decimalFormat = (DecimalFormat)NumberFormat.getNumberInstance();
        decimalFormat.setParseIntegerOnly(true);
    }
    class Model extends AbstractTableModel{
        private String[] columnNames = {"Dept","Name","Price","Discount","Saleprice"};
        public int getColumnCount(){
            return  columnNames.length;
        }
        public int getRowCount(){
            return itemList.size();
        }
        public  String getColumnName(int col){
            return columnNames[col];
        }
        public Object getValueAt(int row, int col){
            return itemList.get(row)[col];
        }
    }
    //reads tabledata.txt and adds to itemList arraylist
    private void createTableArray()
    {
        BufferedReader br;
        try
        {
            br = new BufferedReader(new FileReader("tabledata.txt"));
            String line = br.readLine();
            while (line != null)
            {
                String [] rowFields = line.split("[ ]+");
                itemList.add(rowFields);
                line = br.readLine();
            }
        }
        catch (FileNotFoundException e)
        {
            e.printStackTrace();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }
 }


