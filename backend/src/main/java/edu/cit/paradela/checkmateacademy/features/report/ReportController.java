package edu.cit.paradela.checkmateacademy.features.report;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/reports")
@CrossOrigin(origins = "*")
public class ReportController {

    @Autowired
    private ReportRepository reportRepository;

    @PostMapping
    public ResponseEntity<Report> submitReport(@RequestBody Report report) {
        return ResponseEntity.ok(reportRepository.save(report));
    }

    @GetMapping
    public ResponseEntity<List<Report>> getAllReports() {
        return ResponseEntity.ok(reportRepository.findAllByOrderByCreatedAtDesc());
    }

    @PutMapping("/{reportId}/resolve")
    public ResponseEntity<Report> resolveReport(@PathVariable UUID reportId) {
        Report report = reportRepository.findById(reportId).orElseThrow();
        report.setStatus("RESOLVED");
        return ResponseEntity.ok(reportRepository.save(report));
    }
}
