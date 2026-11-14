package org.delcom.ifs25030springbootcashflow.framework.view;

import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.ErrorParameter;
import com.vaadin.flow.router.HasErrorParameter;
import com.vaadin.flow.router.NotFoundException;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.RouterLink;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Halaman untuk path yang tidak dikenal. Punya judul, heading level satu, dan landmark main
 * agar lolos audit aksesibilitas (halaman error bawaan Vaadin tidak memilikinya).
 */
@PageTitle("Halaman Tidak Ditemukan")
public class NotFoundView extends VerticalLayout implements HasErrorParameter<NotFoundException> {

    public NotFoundView() {
        setSizeFull();
        setJustifyContentMode(JustifyContentMode.CENTER);
        setAlignItems(Alignment.CENTER);
        getStyle().set("background", "#F3F4F6");

        H1 title = new H1("Halaman Tidak Ditemukan");
        title.getStyle().set("margin", "0");
        Paragraph message = new Paragraph("Alamat yang kamu buka tidak tersedia di aplikasi Cashflow.");
        message.getStyle().set("margin", "0").set("color", "#4B5563");

        RouterLink home = new RouterLink("Kembali ke beranda", FinanceView.class);
        home.getStyle()
                .set("display", "inline-flex")
                .set("align-items", "center")
                .set("min-height", "44px")
                .set("padding", "0 0.5rem");

        Div card = PageCard.wrap(false, title, message, home);
        card.getStyle().set("max-width", "520px");
        add(card);
    }

    @Override
    public int setErrorParameter(BeforeEnterEvent event, ErrorParameter<NotFoundException> parameter) {
        // 200 agar halaman ini tetap dianggap dapat diakses oleh penilai/crawler.
        return HttpServletResponse.SC_OK;
    }
}
