package org.delcom.ifs25030p5pbo2026springbootcashflow.framework.view;

import org.delcom.ifs25030p5pbo2026springbootcashflow.adapter.presenter.FinancePresenter;
import org.delcom.ifs25030p5pbo2026springbootcashflow.domain.entity.Transaction;
import org.delcom.ifs25030p5pbo2026springbootcashflow.domain.entity.User;
import org.delcom.ifs25030p5pbo2026springbootcashflow.domain.exception.EntityNotFoundException;
import org.delcom.ifs25030p5pbo2026springbootcashflow.domain.repository.IUserRepository;
import org.delcom.ifs25030p5pbo2026springbootcashflow.framework.auth.AuthSession;
import org.delcom.ifs25030p5pbo2026springbootcashflow.usecase.FinanceUseCase;
import org.delcom.ifs25030p5pbo2026springbootcashflow.usecase.UserUseCase;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.BeforeEvent;
import com.vaadin.flow.router.HasUrlParameter;
import com.vaadin.flow.router.OptionalParameter;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

import java.util.Comparator;
import java.util.List;

/**
 * Halaman admin: melihat transaksi pengguna lain (read-only, tanpa tombol ubah/hapus).
 */
@Route(value = "user-transactions", layout = MainLayout.class)
@PageTitle("Transaksi Pengguna")
public class UserTransactionsView extends VerticalLayout implements HasUrlParameter<Integer>, BeforeEnterObserver {

    private final UserUseCase userUseCase;
    private final FinanceUseCase financeUseCase;
    private final FinancePresenter presenter;
    private final IUserRepository userRepository;
    private final Grid<Transaction> grid = new Grid<>(Transaction.class, false);
    private final H1 title = new H1("Transaksi Pengguna");
    private final Paragraph empty = new Paragraph("Belum ada transaksi.");
    private final SummaryCards summary;

    public UserTransactionsView(
            UserUseCase userUseCase,
            FinanceUseCase financeUseCase,
            FinancePresenter presenter,
            IUserRepository userRepository
    ) {
        this.userUseCase = userUseCase;
        this.financeUseCase = financeUseCase;
        this.presenter = presenter;
        this.userRepository = userRepository;
        this.summary = new SummaryCards(presenter);

        setSizeFull();
        setPadding(true);
        setSpacing(false);
        getStyle().set("background", "#F3F4F6");

        title.getStyle().set("margin", "0");
        empty.getStyle().set("margin", "0").set("color", "#4B5563");
        Paragraph readOnly = new Paragraph("Mode lihat saja: data pengguna lain tidak dapat diubah atau dihapus.");
        readOnly.getStyle().set("margin", "0").set("color", "#4B5563");

        grid.addComponentColumn(this::proofCell).setHeader("Bukti").setAutoWidth(true).setFlexGrow(0);
        grid.addColumn(Transaction::getId).setHeader("ID").setAutoWidth(true).setFlexGrow(0)
                .setSortable(true).setComparator(Transaction::getId);
        grid.addColumn(Transaction::getDescription).setHeader("Keterangan").setFlexGrow(1)
                .setSortable(true)
                .setComparator(Comparator.comparing(Transaction::getDescription, String.CASE_INSENSITIVE_ORDER));
        grid.addColumn(transaction -> presenter.formatMoney(transaction.getAmount()))
                .setHeader("Jumlah").setAutoWidth(true)
                .setSortable(true).setComparator(Comparator.comparingLong(Transaction::getAmount));
        grid.addColumn(transaction -> transaction.getType().label()).setHeader("Tipe").setAutoWidth(true)
                .setSortable(true).setComparator(Comparator.comparing(Transaction::getType));
        grid.addColumn(presenter::formatCreatedAt).setHeader("Waktu").setAutoWidth(true)
                .setSortable(true).setComparator(Comparator.comparing(Transaction::getCreatedAt));
        grid.setSizeFull();
        GridStyles.apply(grid);

        Div card = PageCard.wrap(title, readOnly, summary.build(), empty, grid);
        add(card);
        expand(card);
    }

    private Component proofCell(Transaction transaction) {
        if (transaction.getProofPath() == null || transaction.getProofPath().isBlank()) {
            return new Span("-");
        }
        return ImagePreviewDialog.clickableThumbnail(
                "/uploads/" + transaction.getProofPath(),
                "Bukti transaksi",
                "48px"
        );
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        User current = AuthSession.currentUser(userRepository).orElse(null);
        if (current == null || !current.isAdmin()) {
            event.forwardTo(FinanceView.class);
        }
    }

    @Override
    public void setParameter(BeforeEvent event, @OptionalParameter Integer userId) {
        User current = AuthSession.currentUser(userRepository).orElse(null);
        if (current == null || !current.isAdmin()) {
            event.forwardTo(FinanceView.class);
            return;
        }
        if (userId == null) {
            event.forwardTo(UsersView.class);
            return;
        }
        try {
            User owner = userUseCase.getUser(userId);
            title.setText("Transaksi milik " + owner.getFullName() + " (@" + owner.getUsername() + ")");
            List<Transaction> transactions = financeUseCase.getTransactionsByUser(userId);
            grid.setItems(transactions);
            summary.update(financeUseCase.getBalance(userId));
            empty.setVisible(transactions.isEmpty());
            grid.setVisible(!transactions.isEmpty());
        } catch (EntityNotFoundException ex) {
            event.forwardTo(UsersView.class);
        }
    }
}
