import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.RowFilter;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;

/**
 * Screen to add new cards, search/filter existing cards, edit cards in-place,
 * delete cards, and import/export decks to CSV.
 */
public class ManagePanel extends JPanel {
    private final RecallApp app;
    private final JComboBox<String> typeBox = new JComboBox<>(ItemFactory.TYPES);
    private final JTextArea frontArea = new JTextArea(3, 22);
    private final JTextArea backArea = new JTextArea(4, 22);
    private final JTextField extraField = new JTextField(22);
    private final JTextField tagsField = new JTextField(22);
    private final JLabel extraLabel = new JLabel(ItemFactory.getExtraLabel("Flashcard"));
    private final JLabel statusLabel = new JLabel(" ");

    private final JTextField searchField = new JTextField(14);
    private final JComboBox<String> typeFilter = new JComboBox<>(new String[]{"All Types", "Flashcard", "Formula", "Concept"});
    private final JComboBox<String> boxFilter = new JComboBox<>(new String[]{"All Boxes", "Box 1", "Box 2", "Box 3", "Box 4", "Box 5"});

    private final DefaultTableModel model = new DefaultTableModel(
            new String[]{"ID", "Type", "Question", "Tags", "Box", "Next review", "Accuracy"}, 0) {
        @Override public boolean isCellEditable(int row, int col) { return false; }
    };
    private final JTable table = new JTable(model);
    private final TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>(model);
    private final JPanel formPanel = new JPanel(new GridBagLayout());

    public ManagePanel(RecallApp app) {
        this.app = app;
        setLayout(new BorderLayout(16, 0));
        setBackground(Theme.BG);
        setBorder(BorderFactory.createEmptyBorder(16, 24, 16, 24));

        table.setRowSorter(sorter);
        add(buildForm(), BorderLayout.WEST);
        add(buildTablePanel(), BorderLayout.CENTER);

        typeBox.addActionListener(e ->
                extraLabel.setText(ItemFactory.getExtraLabel((String) typeBox.getSelectedItem())));

        searchField.getDocument().addDocumentListener(new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e) { applyFilter(); }
            @Override public void removeUpdate(DocumentEvent e) { applyFilter(); }
            @Override public void changedUpdate(DocumentEvent e) { applyFilter(); }
        });
        typeFilter.addActionListener(e -> applyFilter());
        boxFilter.addActionListener(e -> applyFilter());

        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) editSelected();
            }
        });

        app.addListener(this::refresh);
        Theme.addListener(() -> {
            setBackground(Theme.BG);
            formPanel.setBackground(Theme.CARD_BG);
            formPanel.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(Theme.BORDER), BorderFactory.createEmptyBorder(16, 16, 16, 16)));
            table.setBackground(Theme.CARD_BG);
            table.setForeground(Theme.TEXT);
            table.setGridColor(Theme.BORDER);
            repaint();
        });
        refresh();
    }

    private JLabel heading(String text) {
        JLabel l = new JLabel(text);
        l.setFont(Theme.font(Font.BOLD, 13));
        l.setForeground(Theme.TEXT);
        return l;
    }

    private JPanel buildForm() {
        formPanel.setBackground(Theme.CARD_BG);
        formPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Theme.BORDER), BorderFactory.createEmptyBorder(16, 16, 16, 16)));
        formPanel.setPreferredSize(new Dimension(360, 0));

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
        c.gridy = r++; formPanel.add(title, c);

        c.gridy = r++; formPanel.add(heading("Card type"), c);
        c.gridy = r++; formPanel.add(typeBox, c);

        frontArea.setLineWrap(true);
        frontArea.setWrapStyleWord(true);
        backArea.setLineWrap(true);
        backArea.setWrapStyleWord(true);
        extraLabel.setFont(Theme.font(Font.BOLD, 13));

        c.gridy = r++; formPanel.add(heading("Question / front"), c);
        c.gridy = r++; formPanel.add(new JScrollPane(frontArea), c);
        c.gridy = r++; formPanel.add(heading("Answer / back"), c);
        c.gridy = r++; formPanel.add(new JScrollPane(backArea), c);
        c.gridy = r++; formPanel.add(extraLabel, c);
        c.gridy = r++; formPanel.add(extraField, c);
        c.gridy = r++; formPanel.add(heading("Tags (e.g. java, oop, exam)"), c);
        c.gridy = r++; formPanel.add(tagsField, c);

        RoundButton addBtn = new RoundButton("Add Card", Theme.INDIGO);
        addBtn.addActionListener(e -> addCard());
        c.gridy = r++; c.insets = new Insets(12, 0, 4, 0); formPanel.add(addBtn, c);

        statusLabel.setFont(Theme.font(Font.PLAIN, 12));
        statusLabel.setForeground(Theme.MUTED);
        c.insets = new Insets(4, 0, 4, 0);
        c.gridy = r++; formPanel.add(statusLabel, c);

        c.gridy = r; c.weighty = 1; formPanel.add(new JPanel() {{ setOpaque(false); }}, c);
        return formPanel;
    }

    private JPanel buildTablePanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 10));
        panel.setOpaque(false);

        // Top Header & Search/Filter Controls
        JPanel top = new JPanel(new BorderLayout());
        top.setOpaque(false);
        JLabel title = new JLabel("Cards in this deck");
        title.setFont(Theme.font(Font.BOLD, 16));
        title.setForeground(Theme.TEXT);

        JPanel filterBar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        filterBar.setOpaque(false);
        JLabel sLabel = new JLabel("Search:");
        sLabel.setFont(Theme.font(Font.BOLD, 12));
        sLabel.setForeground(Theme.MUTED);
        filterBar.add(sLabel);
        filterBar.add(searchField);
        filterBar.add(typeFilter);
        filterBar.add(boxFilter);

        top.add(title, BorderLayout.WEST);
        top.add(filterBar, BorderLayout.EAST);

        // Table setup
        table.setRowHeight(28);
        table.setFont(Theme.font(Font.PLAIN, 13));
        table.getTableHeader().setFont(Theme.font(Font.BOLD, 13));
        table.setSelectionBackground(new Color(0xE0E7FF));
        table.setSelectionForeground(Theme.TEXT);
        int[] widths = {36, 85, 300, 100, 60, 95, 75};
        for (int i = 0; i < widths.length && i < table.getColumnCount(); i++) {
            table.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);
        }

        // Bottom Actions Toolbar
        JPanel south = new JPanel(new BorderLayout());
        south.setOpaque(false);

        JPanel leftActions = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        leftActions.setOpaque(false);
        RoundButton editBtn = new RoundButton("Edit Card", Theme.INDIGO).compact();
        editBtn.addActionListener(e -> editSelected());
        RoundButton deleteBtn = new RoundButton("Delete Card", Theme.RED).compact();
        deleteBtn.addActionListener(e -> deleteSelected());
        leftActions.add(editBtn);
        leftActions.add(deleteBtn);

        JPanel rightActions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        rightActions.setOpaque(false);
        RoundButton exportBtn = new RoundButton("Export CSV", Theme.TEAL).compact();
        exportBtn.addActionListener(e -> exportCsv());
        RoundButton importBtn = new RoundButton("Import CSV", Theme.INDIGO_LIGHT).compact();
        importBtn.addActionListener(e -> importCsv());
        rightActions.add(exportBtn);
        rightActions.add(importBtn);

        south.add(leftActions, BorderLayout.WEST);
        south.add(rightActions, BorderLayout.EAST);

        panel.add(top, BorderLayout.NORTH);
        panel.add(new JScrollPane(table), BorderLayout.CENTER);
        panel.add(south, BorderLayout.SOUTH);
        return panel;
    }

    private void applyFilter() {
        String text = searchField.getText().trim();
        String type = (String) typeFilter.getSelectedItem();
        String box = (String) boxFilter.getSelectedItem();

        List<RowFilter<DefaultTableModel, Object>> filters = new ArrayList<>();
        if (!text.isEmpty()) {
            filters.add(RowFilter.regexFilter("(?i)" + java.util.regex.Pattern.quote(text)));
        }
        if (type != null && !type.equals("All Types")) {
            filters.add(RowFilter.regexFilter("^" + type + "$", 1));
        }
        if (box != null && !box.equals("All Boxes")) {
            filters.add(RowFilter.regexFilter("^" + box + "$", 4));
        }

        if (filters.isEmpty()) {
            sorter.setRowFilter(null);
        } else {
            sorter.setRowFilter(RowFilter.andFilter(filters));
        }
    }

    private void addCard() {
        try {
            app.addCard((String) typeBox.getSelectedItem(), frontArea.getText(), backArea.getText(),
                    extraField.getText(), tagsField.getText());
            frontArea.setText("");
            backArea.setText("");
            extraField.setText("");
            tagsField.setText("");
            frontArea.requestFocusInWindow();
            statusLabel.setText("Card added. It is due for review today.");
        } catch (InvalidCardException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Invalid card", JOptionPane.WARNING_MESSAGE);
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this, "Could not save the deck:\n" + ex.getMessage(),
                    "Save failed", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void editSelected() {
        int viewRow = table.getSelectedRow();
        if (viewRow < 0) {
            statusLabel.setText("Select a card in the table to edit.");
            return;
        }
        int modelRow = table.convertRowIndexToModel(viewRow);
        int id = (Integer) model.getValueAt(modelRow, 0);

        StudyItem target = null;
        for (StudyItem s : app.getCurrentDeck().getItems()) {
            if (s.getId() == id) { target = s; break; }
        }
        if (target == null) return;

        // Open Edit Dialog
        JDialog dialog = new JDialog((java.awt.Frame) SwingUtilities.getWindowAncestor(this), "Edit Card #" + id, true);
        dialog.setLayout(new BorderLayout(12, 12));
        dialog.setSize(480, 420);
        dialog.setLocationRelativeTo(this);

        JPanel p = new JPanel(new GridBagLayout());
        p.setBorder(BorderFactory.createEmptyBorder(16, 20, 16, 20));
        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 1;
        c.insets = new Insets(4, 0, 4, 0);
        int r = 0;

        JTextArea editFront = new JTextArea(target.getFront(), 3, 20);
        editFront.setLineWrap(true);
        editFront.setWrapStyleWord(true);
        JTextArea editBack = new JTextArea(target.getBack(), 4, 20);
        editBack.setLineWrap(true);
        editBack.setWrapStyleWord(true);
        JTextField editExtra = new JTextField(target.getExtra());
        JTextField editTags = new JTextField(target.getTags());

        c.gridy = r++; p.add(new JLabel("Question / Front:"), c);
        c.gridy = r++; p.add(new JScrollPane(editFront), c);
        c.gridy = r++; p.add(new JLabel("Answer / Back:"), c);
        c.gridy = r++; p.add(new JScrollPane(editBack), c);
        c.gridy = r++; p.add(new JLabel(target.getExtraLabel() + ":"), c);
        c.gridy = r++; p.add(editExtra, c);
        c.gridy = r++; p.add(new JLabel("Tags (comma-separated):"), c);
        c.gridy = r++; p.add(editTags, c);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        RoundButton saveBtn = new RoundButton("Save Changes", Theme.INDIGO).compact();
        RoundButton cancelBtn = new RoundButton("Cancel", Theme.MUTED).compact();
        btnPanel.add(cancelBtn);
        btnPanel.add(saveBtn);

        final StudyItem itemToEdit = target;
        saveBtn.addActionListener(e -> {
            try {
                app.updateCard(itemToEdit, editFront.getText(), editBack.getText(), editExtra.getText(), editTags.getText());
                statusLabel.setText("Card #" + itemToEdit.getId() + " updated successfully.");
                dialog.dispose();
            } catch (InvalidCardException ex) {
                JOptionPane.showMessageDialog(dialog, ex.getMessage(), "Validation Error", JOptionPane.WARNING_MESSAGE);
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(dialog, "Could not save deck:\n" + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });
        cancelBtn.addActionListener(e -> dialog.dispose());

        dialog.add(p, BorderLayout.CENTER);
        dialog.add(btnPanel, BorderLayout.SOUTH);
        dialog.setVisible(true);
    }

    private void deleteSelected() {
        int viewRow = table.getSelectedRow();
        if (viewRow < 0) {
            statusLabel.setText("Select a card in the table first.");
            return;
        }
        int modelRow = table.convertRowIndexToModel(viewRow);
        int id = (Integer) model.getValueAt(modelRow, 0);
        int answer = JOptionPane.showConfirmDialog(this, "Delete card #" + id + "?\n\n" + model.getValueAt(modelRow, 2),
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

    private void exportCsv() {
        JFileChooser chooser = new JFileChooser();
        chooser.setSelectedFile(new File(DeckStorage.fileNameFor(app.getCurrentDeck().getName()).replace(".json", ".csv")));
        chooser.setFileFilter(new FileNameExtensionFilter("CSV files (*.csv)", "csv"));
        if (chooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            File f = chooser.getSelectedFile();
            if (!f.getName().toLowerCase().endsWith(".csv")) f = new File(f.getAbsolutePath() + ".csv");
            try {
                app.exportCurrentDeckToCsv(f.toPath());
                JOptionPane.showMessageDialog(this, "Successfully exported " + app.getCurrentDeck().size()
                        + " cards to:\n" + f.getName(), "Export Successful", JOptionPane.INFORMATION_MESSAGE);
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(this, "Export failed:\n" + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void importCsv() {
        JFileChooser chooser = new JFileChooser();
        chooser.setFileFilter(new FileNameExtensionFilter("CSV files (*.csv)", "csv"));
        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            File f = chooser.getSelectedFile();
            try {
                int count = app.importCardsFromCsv(f.toPath());
                JOptionPane.showMessageDialog(this, "Successfully imported " + count + " cards into '"
                        + app.getCurrentDeck().getName() + "'.", "Import Complete", JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Import error:\n" + ex.getMessage(), "Import Failed", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    public void refresh() {
        model.setRowCount(0);
        LocalDate today = AppClock.today();
        for (StudyItem s : app.getCurrentDeck().getItems()) {
            String next = s.isDueOn(today) ? "Due now" : s.getNextReviewDate().toString();
            int acc = s.getAccuracyPercent();
            model.addRow(new Object[]{s.getId(), s.getType(), s.getFront(), s.getTags(),
                    "Box " + s.getBoxLevel(), next, acc < 0 ? "-" : acc + "%"});
        }
        applyFilter();
        revalidate();
        repaint();
    }
}
