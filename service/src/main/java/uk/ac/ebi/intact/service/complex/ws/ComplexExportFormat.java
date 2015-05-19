package uk.ac.ebi.intact.service.complex.ws;

/**
 * Created by maitesin on 24/12/2014.
 */
public enum ComplexExportFormat {
    JSON("json"), XML25("xml25"), XML30("xml30");
    String format;
    ComplexExportFormat(String f) {
        format = f;
    }
    String showFormat() {
        return format;
    }
    public static ComplexExportFormat formatOf(String format) {
        if (JSON.format.equals(format)) return JSON;
        else if (XML25.format.equals(format)) return XML25;
        else if (XML30.format.equals(format)) return XML30;
        else return JSON;
    }
}
