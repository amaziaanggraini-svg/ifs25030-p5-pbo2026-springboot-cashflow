package org.delcom.ifs25030p5pbo2026springbootcashflow.framework.view;

import org.delcom.ifs25030p5pbo2026springbootcashflow.adapter.presenter.FinancePresenter;
import org.delcom.ifs25030p5pbo2026springbootcashflow.adapter.storage.FileStorage;
import org.delcom.ifs25030p5pbo2026springbootcashflow.domain.entity.SortOption;
import org.delcom.ifs25030p5pbo2026springbootcashflow.domain.entity.Transaction;
import org.delcom.ifs25030p5pbo2026springbootcashflow.domain.entity.TransactionType;
import org.delcom.ifs25030p5pbo2026springbootcashflow.domain.entity.User;
import org.delcom.ifs25030p5pbo2026springbootcashflow.domain.exception.EntityNotFoundException;
import org.delcom.ifs25030p5pbo2026springbootcashflow.domain.exception.ValidationException;
import org.delcom.ifs25030p5pbo2026springbootcashflow.domain.repository.IUserRepository;
import org.delcom.ifs25030p5pbo2026springbootcashflow.framework.auth.AuthSession;
import org.delcom.ifs25030p5pbo2026springbootcashflow.usecase.FinanceUseCase;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.progressbar.ProgressBar;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.upload.Upload;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.streams.UploadHandler;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Dashboard cashflow milik pengguna yang sedang login:
 * kartu saldo, pencarian, pengurutan, tambah pemasukan/pengeluaran, dan hapus.
 */
@Route(value = "", layout = MainLayout.class)
@PageTitle("Cashflow")
public class FinanceView extends VerticalLayout {

    private static final String EMPTY_TEXT = "- Data transaksi belum tersedia!";

    private final FinanceUseCase financeUseCase;
    private final FinancePresenter presenter;
    private final FileStorage fileStorage;
    private final IUserRepository userRepository;

    private final Grid<Transaction> grid = new Grid<>(Transaction.class, false);
    private final TextField searchField = new TextField();
    private final ComboBox<SortOption> sortBox = new ComboBox<>();
    private final Paragraph statusLabel = new Paragraph();
    private final Span emptyState = new Span(EMPTY_TEXT);
    private final SummaryCards summary;

    public FinanceView(
            FinanceUseCase financeUseCase,
            FinancePresenter presenter,
            FileStorage fileStorage,
            IUserRepository userRepository
    ) {
        this.financeUseCase = financeUseCase;
        this.presenter = presenter;
        this.fileStorage = fileStorage;
        this.userRepository = userRepository;
        this.summary = new SummaryCards(presenter);

        setSizeFull();
        setPadding(true);
        setSpacing(false);
        getStyle().set("background", "#F3F4F6");

        if (AuthSession.currentUser(userRepository).isEmpty()) {
            // MainLayout akan mengarahkan ke /login.
            return;
        }

        H1 title = new H1("Cashflow Saya");
        title.getStyle().set("margin", "0");

        configureGrid();
        configureToolbar();

        Div card = PageCard.wrap(title, summary.build(), buildToolbar(), statusLabel, grid);
        add(card);
        expand(card);
        refreshAll();
    }

    private User requireUser() {
        return AuthSession.currentUser(userRepository).orElseThrow();
    }

    // ------------------------------------------------------------------ grid

    private void configureGrid() {
        grid.addComponentColumn(this::createProofPreview)
                .setHeader("Bukti")
                .setKey("proof")
                .setAutoWidth(true)
                .setFlexGrow(0)
                .setSortable(false);

        grid.addColumn(Transaction::getId)
                .setHeader("ID")
                .setKey("id")
                .setAutoWidth(true)
                .setFlexGrow(0)
                .setSortable(true)
                .setComparator(Transaction::getId);

        grid.addColumn(Transaction::getDescription)
                .setHeader("Keterangan")
                .setKey("description")
                .setFlexGrow(1)
                .setSortable(true)
                .setComparator(Comparator.comparing(Transaction::getDescription, String.CASE_INSENSITIVE_ORDER));

        grid.addComponentColumn(this::createAmountLabel)
                .setHeader("Jumlah")
                .setKey("amount")
                .setAutoWidth(true)
                .setSortable(true)
                .setComparator(Comparator.comparingLong(Transaction::getAmount));

        grid.addColumn(transaction -> transaction.getType().label())
                .setHeader("Tipe")
                .setKey("type")
                .setAutoWidth(true)
                .setSortable(true)
                .setComparator(Comparator.comparing(Transaction::getType));

        grid.addColumn(presenter::formatCreatedAt)
                .setHeader("Waktu")
                .setKey("createdAt")
                .setAutoWidth(true)
                .setSortable(true)
                .setComparator(Comparator.comparing(Transaction::getCreatedAt));

        grid.addComponentColumn(this::createActionButtons)
                .setHeader("Tindakan")
                .setKey("actions")
                .setAutoWidth(true)
                .setFlexGrow(0)
                .setSortable(false);

        grid.setEmptyStateComponent(emptyState);
        grid.addItemDoubleClickListener(event -> openEditDialog(event.getItem()));
        grid.setSizeFull();
        GridStyles.apply(grid);
    }

    private Component createProofPreview(Transaction transaction) {
        if (transaction.getProofPath() == null || transaction.getProofPath().isBlank()) {
            return new Span("-");
        }
        return ImagePreviewDialog.clickableThumbnail(
                "/uploads/" + transaction.getProofPath(),
                "Bukti transaksi",
                "48px"
        );
    }

    private Span createAmountLabel(Transaction transaction) {
        Span label = new Span(presenter.formatMoney(transaction.getAmount()));
        label.getStyle()
                .set("font-weight", "600")
                .set("color", transaction.isIncome() ? "#16A34A" : "#DC2626");
        return label;
    }

    private HorizontalLayout createActionButtons(Transaction transaction) {
        Button deleteButton = new Button("Hapus", VaadinIcon.TRASH.create(), event -> openDeleteDialog(transaction));
        deleteButton.addThemeVariants(ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_TERTIARY_INLINE);
        deleteButton.getStyle().set("cursor", "pointer");
        return new HorizontalLayout(deleteButton);
    }

    // --------------------------------------------------------------- toolbar

    private void configureToolbar() {
        searchField.setPlaceholder("Cari keterangan...");
        searchField.setPrefixComponent(VaadinIcon.SEARCH.create());
        searchField.setClearButtonVisible(true);
        searchField.addKeyUpListener(Key.ENTER, event -> reload());

        sortBox.setPlaceholder("Urutkan...");
        sortBox.setItems(SortOption.values());
        sortBox.setItemLabelGenerator(presenter::sortLabel);
        sortBox.setClearButtonVisible(true);
        sortBox.setWidth("200px");
        sortBox.addValueChangeListener(event -> reload());
    }

    private HorizontalLayout buildToolbar() {
        Button searchButton = new Button("Cari", VaadinIcon.SEARCH.create(), event -> reload());
        searchButton.getStyle().set("cursor", "pointer");

        Button refreshButton = new Button("Muat Ulang", VaadinIcon.REFRESH.create(), event -> refreshAll());
        refreshButton.getStyle().set("cursor", "pointer");

        Button incomeButton = new Button("Tambah Pemasukan", VaadinIcon.ARROW_CIRCLE_DOWN.create(),
                event -> openAddDialog(TransactionType.INCOME));
        incomeButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_SUCCESS);
        incomeButton.getStyle().set("cursor", "pointer");

        Button expenseButton = new Button("Tambah Pengeluaran", VaadinIcon.ARROW_CIRCLE_UP.create(),
                event -> openAddDialog(TransactionType.EXPENSE));
        expenseButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_ERROR);
        expenseButton.getStyle().set("cursor", "pointer");

        HorizontalLayout toolbar = new HorizontalLayout(
                searchField, searchButton, sortBox, refreshButton, incomeButton, expenseButton);
        toolbar.setWidthFull();
        toolbar.setFlexGrow(1, searchField);
        toolbar.getStyle().set("flex-wrap", "wrap");
        return toolbar;
    }

    // ---------------------------------------------------------------- dialogs

    private void openAddDialog(TransactionType type) {
        TextField descriptionField = new TextField("Keterangan");
        TextField amountField = new TextField("Jumlah (Rp)");
        amountField.setPlaceholder("contoh: 1000000");
        amountField.setPrefixComponent(new Span("Rp"));
        descriptionField.setWidthFull();
        amountField.setWidthFull();

        AtomicReference<String> pendingName = new AtomicReference<>();
        AtomicReference<byte[]> pendingData = new AtomicReference<>();
        Upload upload = imageUpload(pendingName, pendingData);
        Span uploadHint = new Span("Bukti transaksi (opsional)");

        Dialog dialog = new Dialog();
        ProgressBar loadingBar = new ProgressBar();
        loadingBar.setVisible(false);

        Button saveButton = new Button("Simpan", VaadinIcon.CHECK.create());
        saveButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        saveButton.addClickListener(event -> {
            loadingBar.setVisible(true);
            saveButton.setEnabled(false);
            try {
                User user = requireUser();
                Transaction created = financeUseCase.addTransaction(
                        user.getId(), type, descriptionField.getValue(), amountField.getValue());
                attachProof(user.getId(), created, pendingName.get(), pendingData.get());
                showSuccess(presenter.showAddSuccess(created));
                dialog.close();
                refreshAll();
            } catch (ValidationException | EntityNotFoundException | IOException ex) {
                loadingBar.setVisible(false);
                saveButton.setEnabled(true);
                showError(presenter.showValidationError(ex.getMessage()));
            }
        });

        Button cancelButton = new Button("Batal", VaadinIcon.CLOSE.create(), event -> dialog.close());
        VerticalLayout body = new VerticalLayout(descriptionField, amountField, uploadHint, upload);
        body.setPadding(false);
        body.setSpacing(true);
        AppDialogs.build(dialog, "Tambah " + type.label(), AppDialogs.Tone.PRIMARY,
                loadingBar, body, cancelButton, saveButton);
        dialog.open();
    }

    /** Menyimpan bukti (jika ada). Bila penyimpanan file gagal, transaksi dibatalkan. */
    private void attachProof(int userId, Transaction created, String filename, byte[] data)
            throws IOException, ValidationException, EntityNotFoundException {
        if (filename == null || filename.isBlank() || data == null) {
            return;
        }
        try {
            String path = fileStorage.store("proofs", filename, new ByteArrayInputStream(data));
            financeUseCase.updateTransactionForUser(userId, created.getId(), null, null, null, path, false);
        } catch (IOException ex) {
            financeUseCase.removeTransactionForUser(userId, created.getId());
            throw ex;
        }
    }

    private void openEditDialog(Transaction transaction) {
        TextField descriptionField = new TextField("Keterangan");
        descriptionField.setValue(transaction.getDescription());
        TextField amountField = new TextField("Jumlah (Rp)");
        amountField.setValue(String.valueOf(transaction.getAmount()));
        amountField.setPrefixComponent(new Span("Rp"));
        descriptionField.setWidthFull();
        amountField.setWidthFull();

        AtomicReference<String> pendingName = new AtomicReference<>();
        AtomicReference<byte[]> pendingData = new AtomicReference<>();
        Upload upload = imageUpload(pendingName, pendingData);
        Span uploadHint = new Span("Ganti bukti (opsional)");

        Dialog dialog = new Dialog();
        ProgressBar loadingBar = new ProgressBar();
        loadingBar.setVisible(false);

        Button saveButton = new Button("Simpan", VaadinIcon.CHECK.create());
        saveButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        saveButton.addClickListener(event -> {
            loadingBar.setVisible(true);
            saveButton.setEnabled(false);
            try {
                String proofPath = null;
                if (pendingName.get() != null && !pendingName.get().isBlank() && pendingData.get() != null) {
                    proofPath = fileStorage.store(
                            "proofs", pendingName.get(), new ByteArrayInputStream(pendingData.get()));
                }
                financeUseCase.updateTransactionForUser(
                        requireUser().getId(),
                        transaction.getId(),
                        descriptionField.getValue(),
                        amountField.getValue(),
                        null,
                        proofPath,
                        false
                );
                showSuccess(presenter.showUpdateSuccess());
                dialog.close();
                refreshAll();
            } catch (EntityNotFoundException ex) {
                loadingBar.setVisible(false);
                saveButton.setEnabled(true);
                showError(presenter.showUpdateFailed(ex.getId()));
            } catch (ValidationException | IOException ex) {
                loadingBar.setVisible(false);
                saveButton.setEnabled(true);
                showError(presenter.showValidationError(ex.getMessage()));
            }
        });

        Button cancelButton = new Button("Batal", VaadinIcon.CLOSE.create(), event -> dialog.close());
        VerticalLayout body = new VerticalLayout(descriptionField, amountField, uploadHint, upload);
        body.setPadding(false);
        body.setSpacing(true);
        AppDialogs.build(dialog, "Mengubah " + transaction.getType().label() + " #" + transaction.getId(),
                AppDialogs.Tone.PRIMARY, loadingBar, body, cancelButton, saveButton);
        dialog.open();
    }

    private void openDeleteDialog(Transaction transaction) {
        Paragraph message = new Paragraph(
                "Yakin ingin menghapus transaksi \"" + transaction.getDescription() + "\" (ID "
                        + transaction.getId() + ") senilai " + presenter.formatMoney(transaction.getAmount()) + "?"
        );
        message.getStyle().set("margin", "0");

        Dialog dialog = new Dialog();
        ProgressBar loadingBar = new ProgressBar();
        loadingBar.setVisible(false);

        Button confirmButton = new Button("Hapus", VaadinIcon.TRASH.create());
        confirmButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_ERROR);
        confirmButton.addClickListener(event -> {
            loadingBar.setVisible(true);
            confirmButton.setEnabled(false);
            deleteTransaction(transaction);
            dialog.close();
        });

        Button cancelButton = new Button("Batal", VaadinIcon.CLOSE.create(), event -> dialog.close());
        AppDialogs.build(dialog, "Menghapus Transaksi", AppDialogs.Tone.DANGER,
                loadingBar, message, cancelButton, confirmButton);
        dialog.open();
    }

    // ---------------------------------------------------------------- actions

    private void deleteTransaction(Transaction transaction) {
        try {
            financeUseCase.removeTransactionForUser(requireUser().getId(), transaction.getId());
            showSuccess(presenter.showRemoveSuccess());
            refreshAll();
        } catch (EntityNotFoundException ex) {
            showError(presenter.showRemoveFailed(ex.getId()));
        } catch (ValidationException ex) {
            showError(presenter.showValidationError(ex.getMessage()));
        }
    }

    /** Memuat ulang semua data: kosongkan pencarian dan pengurutan. */
    private void refreshAll() {
        searchField.clear();
        sortBox.clear();
        reload();
    }

    /** Memuat daftar sesuai kata kunci dan opsi pengurutan saat ini. */
    private void reload() {
        int userId = requireUser().getId();
        String keyword = searchField.getValue();
        List<Transaction> items = financeUseCase.findTransactions(userId, keyword, sortBox.getValue());
        grid.setItems(items);
        summary.update(financeUseCase.getBalance(userId));

        boolean searching = keyword != null && !keyword.isBlank();
        emptyState.setText(searching ? "- Transaksi tidak ditemukan!" : EMPTY_TEXT);
        String title = searching ? "Hasil Pencarian: \"" + keyword.trim() + "\"" : "Daftar Transaksi";
        String balanceText = presenter.formatBalance(financeUseCase.getBalance(userId));
        if (items.isEmpty()) {
            statusLabel.setText(title + " — " + emptyState.getText().replaceFirst("^- ", "") + " | " + balanceText);
        } else {
            statusLabel.setText(title + " | " + balanceText);
        }
    }

    private Upload imageUpload(AtomicReference<String> pendingName, AtomicReference<byte[]> pendingData) {
        Upload upload = new Upload(UploadHandler.inMemory((metadata, data) -> {
            pendingName.set(metadata.fileName());
            pendingData.set(data);
        }));
        upload.setAcceptedMimeTypes("image/png", "image/jpeg", "image/webp");
        upload.setAcceptedFileExtensions(".png", ".jpg", ".jpeg", ".webp");
        upload.setMaxFiles(1);
        return upload;
    }

    private void showSuccess(String message) {
        Notification notification = Notification.show(message, 3000, Notification.Position.BOTTOM_START);
        notification.addThemeVariants(NotificationVariant.LUMO_SUCCESS);
    }

    private void showError(String message) {
        Notification notification = Notification.show(message, 4000, Notification.Position.BOTTOM_START);
        notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
    }
}
