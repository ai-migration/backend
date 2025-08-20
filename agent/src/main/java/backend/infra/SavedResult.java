package backend.infra;

import backend.dto.Report;
import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.nio.charset.StandardCharsets;
import java.util.*;

public class SavedResult {

    private SavedResult() {}

    public static class ProcessResult {
        private final List<Map<String, Object>> reportList;
        private final List<String> pathList;
        public ProcessResult(List<Map<String, Object>> reportList, List<String> pathList) {
            this.reportList = reportList;
            this.pathList = pathList;
        }
        public List<Map<String, Object>> getReportList() { return reportList; }
        public List<String> getPathList() { return pathList; }
    }

    public static ProcessResult uploadAndBuildReport(
            List<String> codes,
            Report report,                 // ★ Report 자체
            String folder,                 // "vo", "serviceimpl" 등
            String bucketName,
            S3Client s3Client,
            long userId,
            long jobId
    ) {
        List<Map<String, Object>> reportList = new ArrayList<>();
        List<String> pathList = new ArrayList<>();

        List<Map<String, Object>> conv = report != null ? report.getConversion() : Collections.emptyList();
        List<Map<String, Object>> gen  = report != null ? report.getGeneration() : Collections.emptyList();
        List<Map<String, Object>> eval  = report != null ? report.getEvaluation() : Collections.emptyList();

        int codeSize = codes != null ? codes.size() : 0;
        int convSize = conv != null ? conv.size()   : 0;
        int n = Math.min(codeSize, convSize);

        for (int i = 0; i < n; i++) {
            String code = codes.get(i);

            // 클래스명 추출
            CompilationUnit cu = StaticJavaParser.parse(code);
            String className = cu.findFirst(ClassOrInterfaceDeclaration.class)
                    .map(c -> c.getNameAsString())
                    .orElse(folder + UUID.randomUUID());

            // S3 업로드
            String savedPath = userId + "/" + jobId + "/conversion/" + folder + "/" + className + ".java";
            PutObjectRequest req = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(savedPath)
                    .contentType("text/x-java-source")
                    .build();
            s3Client.putObject(req,
                    software.amazon.awssdk.core.sync.RequestBody.fromBytes(code.getBytes(StandardCharsets.UTF_8)));

            // reportEntry 구성
            Map<String, Object> rep = new LinkedHashMap<>();
            rep.put("conversion", i < conv.size() ? conv.get(i) : Collections.emptyMap());
            rep.put("generation", i < gen.size() ? gen.get(i) : Collections.emptyMap());
            rep.put("evaluation", i < eval.size() ? eval.get(i) : Collections.emptyMap());

            Map<String, Object> res = new LinkedHashMap<>();
            res.put(className, rep);


//            Map<String, Object> entryValue;
//
//            Map<String, Object> both = new LinkedHashMap<>();
//            both.put("conversion", conv.get(i));
//            both.put("generation", gen.get(i));
//            both.put("evaluation", eval.get(i));
//            entryValue = both;
//
//            // {클래스명.java: entryValue}
//            Map<String, Object> res = new LinkedHashMap<>();
//            res.put(className, entryValue);

            reportList.add(res);
            pathList.add(savedPath);
        }

        return new ProcessResult(reportList, pathList);
    }


}
