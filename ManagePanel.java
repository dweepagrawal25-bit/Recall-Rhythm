import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.io.IOException;
import java.time.LocalDate;
import javax.swing.BorderFactory;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;

/** Screen to add new cards and to see / delete the cards in the current deck. */
public class ManagePanel extends JPanel {
    private final RecallApp app;
    private final JComboBox<String> typeBox = new JComboBox<>(ItemFactory.TYPES);
    private final JTextArea frontArea = new JTextArea(3, 22);
    private final JTextArea backArea = new JTextArea(4, 22);
    private final JTextField extraField = new JTextField(22);
    private final JLabel extraLabel = new JLabel(ItemFactory.getExtraLabel("Flashcard"));
    private final JLabel statusLabel = new JLabel(" ");
    private final DefaultTableModel model = new DefaultTableModel(
            new String[]{"ID", "Type", "Question", "Box", "Next review", "Accuracy"}, 0) {
        @Override public boolean isCellEditable(int row, int col) { return false; }
    };
    private final JTable table = new JTable(model);

    public ManagePanel(RecallApp app) {
        this.app = app;
        setLayout(new BorderLayout(16, 0));
        setBackground(Theme.BG);
        setBorder(BorderFactory.createEmptyBorder(16, 24, 16, 24));
        add(buildForm(), BorderLayout.WEST);
        add(buildTablePanel(), BorderLayout.CENTER);

        typeBox.addActionListener(e ->
                extraLabel.setText(ItemFactory.getExtraLabel((String) typeBox.getSelectedItem())));
        app.addListener(this::refresh);
        refresh();
    }

    private JLabel heading(String text) {
        JLabel l = new JLabel(text);
        l.setFont(Theme.font(Font.BOLD, 13));
        l.setForeground(Theme.TEXT);
        return l;
    }

    private JPanel buildForm() {
        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(Color.WHITE);
        form.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Theme.BORDER), BorderFactory.createEmptyBorder(16, 16, 16, 16)));
        form.setPreferredSize(new Dimension(360, 0));

        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 1;
        c.anchor = GridBagConstraints.NORTHWEST;
        c.insets = new Insets(4, 0, 4, 0);
        int r = 0;

        JLabel title = new JLabel("Add a new card");
        title.setFont(Theme.font(Font.BOLD, 18));
        title.setForeground(Theme.INDIGO);
        c.gridy = r++; form.add(title, c);

        c.gridy = r++; form.add(heading("Card type"), c);
        c.gridy = r++; form.add(typeBox, c);

        frontArea.setLineWrap(true);
        frontArea.setWrapStyleWord(true);
        backArea.setLineWrap(true);
        backArea.setWrapStyleWord(true);
        extraLabel.setFont(Theme.font(Font.BOLD, 13));

        c.gridy = r++; form.add(heading("Question / front"), c);
        c.gridy = r++; form.add(new JScrollPane(frontArea), c);
        c.gridy = r++; form.add(heading("Answer / back"), c);
        c.gridy = r++; form.add(new JScrollPane(backArea), c);
        c.gridy = r++; form.add(extraLabel, c);
        c.gridy = r++; form.add(extraField, c);

        RoundButton addBtn = new RoundButton("Add Card", Theme.INDIGO);
        addBtn.addActionListener(e -> addCard());
        c.gridy = r++; c.insets = new Insets(12, 0, 4, 0); form.add(addBtn, c);

        statusLabel.setFont(Theme.font(Font.PLAIN, 12));
        statusLabel.setForeground(Theme.MUTED);
        c.insets = new Insets(4, 0, 4, 0);
        c.gridy = r++; form.add(statusLabel, c);

        c.gridy = r; c.weighty = 1; form.add(new JPanel(), c);
        return form;
    }

    private JPanel buildTablePanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 8));
        panel.setOpaque(false);
        JLabel title = new JLabel("Cards in this deck");
        title.setFont(Theme.font(Font.BOLD, 16));
        title.setForeground(Theme.TEXT);

        table.setRowHeight(26);
        table.setFont(Theme.font(Font.PLAIN, 13));
        table.getTableHeader().setFont(Theme.font(Font.BOLD, 13));
        table.setSelectionBackground(new Color(0xE0E7FF));
        table.setSelectionForeground(Theme.TEXT);
        int[] widths = {40, 90, 380, 60, 100, 80};
        for (int i = 0; i < widths.length; i++) {
            table.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);
        }

        RoundButton deleteBtn = new RoundButton("Delete Selected Card", Theme.RED).compact();
        deleteBtn.addActionListener(e -> deleteSelected());
        JPanel south = new JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 0, 0));
        south.setOpaque(false);
        south.add(deleteBtn);

        panel.add(title, BorderLayout.NORTH);
        panel.add(new JScrollPane(table), BorderLayout.CENTER);
        panel.add(south, BorderLayout.SOUTH);
        return panel;
    }

    private void addCard() {
        try {
            app.addCard((String) typeBox.getSelectedItem(), frontArea.getText(), backArea.getText(), extraField.getText());
            frontArea.setText("");
            backArea.setText("");
            extraField.setText("");
            frontArea.requestFocusInWindow();
            statusLabel.setText("Card added. It is due for review today.");
        } catch (InvalidCardException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Invalid card", JOptionPane.WARNING_MESSAGE);
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this, "Could not save the deck:\n" + ex.getMessage(),
                    "Save failed", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void deleteSelected() {
        int row = table.getSelectedRow();
        if (row < 0) {
            statusLabel.setText("Select a card in the table first.");
            return;
        }
        int id = (Integer) model.getValueAt(row, 0);
        int answer = JOptionPane.showConfirmDialog(this, "Delete this card?\n\n" + model.getValueAt(row, 2),
                "Confirm delete", JOptionPane.YES_NO_OPTION);
        if (answer != JOptionPane.YES_OPTION) return;
        try {
            app.deleteCard(id);
            statusLabel.setText("Card deleted.");
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this, "Could not save the deck:\n" + ex.getMessage(),
                    "Save failed", JOptionPane.ERROR_MESSAGE);
        }
    }

    public void refresh() {
        model.setRowCount(0);
        LocalDate today = AppClock.today();
        for (StudyItem s : app.getCurrentDeck().getItems()) {
            String next = s.isDueOn(today) ? "Due now" : s.getNextReviewDate().toString();
            int acc = s.getAccuracyPercent();
            model.addRow(new Object[]{s.getId(), s.getType(), s.getFront(),
                    "Box " + s.getBoxLevel(), next, acc < 0 ? "-" : acc + "%"});
        }
    }
}
