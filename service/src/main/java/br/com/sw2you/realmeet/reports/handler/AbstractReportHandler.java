package br.com.sw2you.realmeet.reports.handler;

import br.com.sw2you.realmeet.email.TemplateType;
import br.com.sw2you.realmeet.reports.enumeration.ReportFormat;
import br.com.sw2you.realmeet.reports.enumeration.ReportHandlerType;
import br.com.sw2you.realmeet.reports.model.AbstractReportData;
import br.com.sw2you.realmeet.reports.validator.AbstractReportValidator;
import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.util.HashMap;
import net.sf.jasperreports.engine.*;

public abstract class AbstractReportHandler<D extends AbstractReportData> {
    private final JasperReport jasperReport;

    public AbstractReportHandler(JasperReport jasperReport) {
        this.jasperReport = jasperReport;
    }

    public byte[] createReportBytes(D reportData, ReportFormat reportFormat) {
        var reportParams = new HashMap<String, Object>();
        var out = new ByteArrayOutputStream();

        fillReportParams(reportParams, reportData);

        try {
            var jasperPrint = JasperFillManager.fillReport(jasperReport, reportParams, getDataSource(reportData));
            exportReportToStream(jasperPrint, out, reportFormat);
            return out.toByteArray();
        } catch (JRException e) {
            throw new RuntimeException(e);
        }
    }

    public abstract TemplateType getTemplateType();

    public abstract AbstractReportValidator getValidator();

    public abstract ReportHandlerType getReportHandlerType();

    protected void fillReportParams(HashMap<String, Object> reportParams, D reportData) {}

    public JRDataSource getDataSource(D reportData) {
        //TODO
        return null;
    }

    private void exportReportToStream(JasperPrint jasperPrint, OutputStream out, ReportFormat reportFormat) {
        try {
            switch (reportFormat) {
                case PDF:
                    JasperExportManager.exportReportToPdfStream(jasperPrint, out);
                    break;
                case XML:
                    JasperExportManager.exportReportToXmlStream(jasperPrint, out);
                    break;
                default:
                    throw new IllegalArgumentException("Report type not supported: " + reportFormat.name());
            }
        } catch (JRException e) {
            throw new RuntimeException(e);
        }
    }
}
