package org.delcom.ifs25030springbootcashflow.framework.view;

import org.delcom.ifs25030springbootcashflow.adapter.presenter.FinancePresenter;
import org.delcom.ifs25030springbootcashflow.domain.entity.Balance;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;

/**
 * Empat kartu ringkasan keuangan: jumlah transaksi, pemasukan, pengeluaran, dan saldo.
 */
final class SummaryCards {

    private final Span totalCount = new Span("0");
    private final Span incomeTotal = new Span("Rp 0.00");
    private final Span expenseTotal = new Span("Rp 0.00");
    private final Span balanceTotal = new Span("Rp 0.00");
    private final FinancePresenter presenter;

    SummaryCards(FinancePresenter presenter) {
        this.presenter = presenter;
    }

    HorizontalLayout build() {
        HorizontalLayout row = new HorizontalLayout(
                card("Jumlah Transaksi", totalCount, "#DBEAFE", "#1D4ED8", "#1E40AF"),
                card("Total Pemasukan", incomeTotal, "#DCFCE7", "#16A34A", "#166534"),
                card("Total Pengeluaran", expenseTotal, "#FEE2E2", "#DC2626", "#991B1B"),
                card("Saldo", balanceTotal, "#FEF3C7", "#D97706", "#92400E")
        );
        row.setWidthFull();
        row.setSpacing(true);
        row.getStyle().set("align-items", "stretch").set("flex-wrap", "wrap");
        return row;
    }

    void update(Balance balance) {
        totalCount.setText(String.valueOf(balance.count()));
        incomeTotal.setText(presenter.formatMoney(balance.totalIncome()));
        expenseTotal.setText(presenter.formatMoney(balance.totalExpense()));
        balanceTotal.setText(presenter.formatMoney(balance.balance()));
    }

    private static Div card(String label, Span value, String background, String accent, String valueColor) {
        Span caption = new Span(label);
        caption.getStyle()
                .set("display", "block")
                .set("font-size", "0.875rem")
                .set("font-weight", "500")
                .set("color", valueColor);

        value.getStyle()
                .set("display", "block")
                .set("margin-top", "0.35rem")
                .set("font-size", "1.5rem")
                .set("font-weight", "700")
                .set("line-height", "1.1")
                .set("color", valueColor);

        Div card = new Div(caption, value);
        card.getStyle()
                .set("flex", "1")
                .set("min-width", "180px")
                .set("background", background)
                .set("border", "1px solid " + accent)
                .set("border-left", "6px solid " + accent)
                .set("border-radius", "12px")
                .set("padding", "1rem 1.1rem")
                .set("box-sizing", "border-box");
        return card;
    }
}
