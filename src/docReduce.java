import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

public abstract class docReduce implements DocumentListener {

    @Override
    public void insertUpdate(DocumentEvent e) {
        update();
    }

    @Override
    public void removeUpdate(DocumentEvent e) {

    }

    @Override
    public void changedUpdate(DocumentEvent e) {
        update();
    }

    abstract void update();
}
