package uk.ac.ebi.intact.application.editor.struts.security;

import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;

public class EditorJpaVendorAdapter extends HibernateJpaVendorAdapter {

    public EditorJpaVendorAdapter() {
        super();
    }

    public String getJpaDatabasePlatform() {
        return super.getDatabasePlatform();
    }
}