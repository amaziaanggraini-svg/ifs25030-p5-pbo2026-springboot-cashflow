package org.delcom.ifs25030springbootcashflow.framework.view;

import com.vaadin.flow.server.ServiceInitEvent;
import com.vaadin.flow.server.VaadinServiceInitListener;
import org.springframework.stereotype.Component;

/**
 * Cadangan untuk metadata halaman (bahasa dan deskripsi) agar tetap ada
 * walaupun index.html bawaan dipakai. Nilai utama ada di src/main/frontend/index.html.
 */
@Component
public class PageMetaInitListener implements VaadinServiceInitListener {

    private static final String DESCRIPTION =
            "Cashflow: aplikasi pencatat pemasukan dan pengeluaran keuangan pribadi beserta saldo.";

    @Override
    public void serviceInit(ServiceInitEvent event) {
        event.getSource().addUIInitListener(uiEvent -> uiEvent.getUI().getPage().executeJs("""
                const root = document.documentElement;
                if (!root.getAttribute('lang')) { root.setAttribute('lang', 'id'); }
                if (!document.querySelector('meta[name="description"]')) {
                  const meta = document.createElement('meta');
                  meta.name = 'description';
                  meta.content = $0;
                  document.head.appendChild(meta);
                }
                """, DESCRIPTION));
    }
}
