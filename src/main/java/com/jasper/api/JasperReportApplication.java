package com.jasper.api;

import java.io.ByteArrayOutputStream;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.databind.ObjectMapper;

import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import net.sf.jasperreports.engine.export.JRPdfExporter;
import net.sf.jasperreports.export.SimpleExporterInput;
import net.sf.jasperreports.export.SimpleOutputStreamExporterOutput;

@RestController
@SpringBootApplication
public class JasperReportApplication {

    @Value("${reports.relative.path}")
    private String reportsPath;
    @Value("${images.relative.path}")
    private String imagesPath;	
	@Value("${temp.files.path}")
	private String tempPath;          
	
	public static void main(String[] args) {
		SpringApplication.run(JasperReportApplication.class, args);
	}
    
    @SuppressWarnings("unchecked")
    @PostMapping("/report")
    public ResponseEntity<byte[]> generateReport(@RequestBody String json) {
        try {       	
			ObjectMapper objectMapper = new ObjectMapper();
			Map<String, Object> reportData = objectMapper.readValue(json, Map.class);        	
        	
	        String reportName = (String) reportData.get("report");
	        Map<String, Object> parameters = (Map<String, Object>) reportData.get("parameters");
	        List<Map<String, Object>> dataSource = (List<Map<String, Object>>) reportData.get("dataSource");			
			
            JasperPrint jasperPrint;
            JasperReport jasperReport = JasperCompileManager.compileReport(reportsPath + reportName + ".jrxml");          
            
            JRBeanCollectionDataSource beanDataSource = new JRBeanCollectionDataSource(dataSource, false);
            jasperPrint = JasperFillManager.fillReport(jasperReport, parameters, beanDataSource);

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            JRPdfExporter exporter = new JRPdfExporter();
            exporter.setExporterInput(new SimpleExporterInput(jasperPrint));
            exporter.setExporterOutput(new SimpleOutputStreamExporterOutput(outputStream));
            exporter.exportReport();

            byte[] relatorioBytes = outputStream.toByteArray();

            outputStream.close();

            return ResponseEntity.ok()
                    .header("Content-Type", "application/pdf")
                    .body(relatorioBytes);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }    
}
