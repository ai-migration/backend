// backend/infra/SecuritySavedResult.java
package backend.infra;

import com.fasterxml.jackson.databind.ObjectMapper;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * 역할: Security 파이프라인 산출물(JSON/MD)을 S3에 업로드하고
 *      Mongo SecurityLog에 저장할 요약(경로/개수)을 반환.
 * - 변환의 SavedResult.java와 대칭 구조 유지
 * - Jackson 네이밍 전략 설정 불필요 (기본 매퍼 사용)
 */
public class SecuritySavedResult {

    // 네이밍 전략 없이 기본 ObjectMapper 사용
    private static final ObjectMapper MAPPER = new ObjectMapper();

    private SecuritySavedResult() {}

    /** 변환 SavedResult와 패턴을 맞춘 결과 DTO */
    public static class ProcessResult {
        private final String s3AgentInputsPath;     // agent_inputs.json
        private final String s3ReportsDir;          // .../security_reports/ (prefix)
        private final String s3ReportJsonPath;      // report.json
        private final List<String> issueReportFiles;// 업로드된 *.md 키 목록
        private final Integer issueCount;           // 이슈 건수 요약(issues.total 또는 리스트 길이)

        public ProcessResult(String inputs, String dir, String report,
                             List<String> mds, Integer cnt) {
            this.s3AgentInputsPath = inputs;
            this.s3ReportsDir = dir;
            this.s3ReportJsonPath = report;
            this.issueReportFiles = mds;
            this.issueCount = cnt;
        }
        public String getS3AgentInputsPath() { return s3AgentInputsPath; }
        public String getS3ReportsDir() { return s3ReportsDir; }
        public String getS3ReportJsonPath() { return s3ReportJsonPath; }
        public List<String> getIssueReportFiles() { return issueReportFiles; }
        public Integer getIssueCount() { return issueCount; }
    }

    /**
     * 역할: 파이썬이 카프카로 보낸 result 맵(camelCase 키: agentInputs, metrics, qualityGate, issues, reportJson, markdowns)을
     *       S3에 업로드하고 업로드 경로/이슈 개수 요약을 반환.
     */
    @SuppressWarnings("unchecked")
    public static ProcessResult uploadAll(
        Map<String, Object> result,
        String bucketName,
        S3Client s3Client,
        long userId,
        long jobId
    ) throws Exception {
        String base = userId + "/" + jobId + "/security";
        String reportsPrefix = base + "/security_reports/";

        // 1) JSON 업로드 (존재하는 것만)
        String inputsKey = putJsonIfPresent(s3Client, bucketName,
                reportsPrefix + "agent_inputs.json", result.get("agentInputs"));
        putJsonIfPresent(s3Client, bucketName,
                reportsPrefix + "metrics.json", result.get("metrics"));
        putJsonIfPresent(s3Client, bucketName,
                reportsPrefix + "quality_gate.json", result.get("qualityGate"));
        putJsonIfPresent(s3Client, bucketName,
                reportsPrefix + "sonarqube_issues_combined.json", result.get("issues"));
        String reportKey = putJsonIfPresent(s3Client, bucketName,
                reportsPrefix + "report.json", result.get("reportJson"));

        // 2) Markdown 업로드
        List<String> mdKeys = new ArrayList<>();
        Object mdObj = result.get("markdowns");
        if (mdObj instanceof List<?>) {
            for (Object o : (List<?>) mdObj) {
                if (o instanceof Map) {
                    Map<?,?> md = (Map<?,?>) o;
                    Object name = md.get("name");
                    Object text = md.get("text");
                    if (name != null && text != null) {
                        String key = reportsPrefix + name.toString();
                        s3Client.putObject(
                            PutObjectRequest.builder()
                                .bucket(bucketName)
                                .key(key)
                                .contentType("text/markdown; charset=UTF-8")
                                .build(),
                            software.amazon.awssdk.core.sync.RequestBody.fromBytes(
                                text.toString().getBytes(StandardCharsets.UTF_8))
                        );
                        mdKeys.add(key);
                    }
                }
            }
        }

        // 3) 이슈 건수 요약(맵이면 total, 리스트면 size)
        Integer issueCount = null;
        Object issues = result.get("issues");
        if (issues instanceof Map) {
            Object total = ((Map<?,?>) issues).get("total");
            if (total != null) issueCount = Integer.valueOf(total.toString());
        } else if (issues instanceof List<?>) {
            issueCount = ((List<?>) issues).size();
        }

        return new ProcessResult(inputsKey, reportsPrefix, reportKey, mdKeys, issueCount);
    }

    /** 존재할 때만 JSON으로 직렬화하여 업로드 */
    private static String putJsonIfPresent(S3Client s3, String bucket, String key, Object obj) throws Exception {
        if (obj == null) return null;
        String json = MAPPER.writeValueAsString(obj);
        s3.putObject(
            PutObjectRequest.builder()
                .bucket(bucket)
                .key(key)
                .contentType("application/json; charset=UTF-8")
                .build(),
            software.amazon.awssdk.core.sync.RequestBody.fromBytes(json.getBytes(StandardCharsets.UTF_8))
        );
        return key;
    }
}
