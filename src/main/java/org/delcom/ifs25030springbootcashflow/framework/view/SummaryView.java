package org.delcom.ifs25030springbootcashflow.framework.view;

import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import org.delcom.ifs25030springbootcashflow.adapter.presenter.FinancePresenter;
import org.delcom.ifs25030springbootcashflow.domain.repository.IUserRepository;
import org.delcom.ifs25030springbootcashflow.framework.auth.AuthSession;
import org.delcom.ifs25030springbootcashflow.usecase.FinanceUseCase;

/**
 * Ringkasan keuangan milik pengguna yang sedang login (USER maupun ADMIN):
 * jumlah transaksi, total pemasukan, total pengeluaran, dan saldo.
 */
@Route(value = "summary", layout = MainLayout.class)
@PageTitle("Ringkasan")
public class SummaryView extends VerticalLayout {

    public SummaryView(
            FinanceUseCase financeUseCase,
            FinancePresenter presenter,
            IUserRepository userRepository
    ) {
        setSizeFull();
        setPadding(true);
        setSpacing(false);
        getStyle().set("background", "#F3F4F6");

        var user = AuthSession.currentUser(userRepository).orElse(null);
        if (user == null) {
            // MainLayout akan mengarahkan ke /login.
            return;
        }

        H1 title = new H1("Ringkasan Keuangan");
        title.getStyle().set("margin", "0");
        Paragraph subtitle = new Paragraph("Ikhtisar pemasukan, pengeluaran, dan saldo milik kamu.");
        subtitle.getStyle().set("margin", "0").set("color", "#4B5563");

        SummaryCards summary = new SummaryCards(presenter);
        summary.update(financeUseCase.getBalance(user.getId()));

        Div card = PageCard.wrap(false, title, subtitle, summary.build());
        add(card);
    }
}
