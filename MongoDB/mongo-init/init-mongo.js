db = db.getSiblingDB("agent_db");

db.createCollection("conversion_logs", {
  validator: {
    $jsonSchema: {
      bsonType: "object",
      required: ["jobId", "userId", "s3OriginPath", "savedAt"],
      properties: {
        jobId: { bsonType: "long" },
        userId: { bsonType: "long" },
        inputLanguage: { bsonType: "string" },
        s3OriginPath: { bsonType: "string" },
        savedAt: { bsonType: "date" },

        s3ConvControllerPath:   { bsonType: 'array', items: { bsonType: 'string' } },
        s3ConvServicePath:      { bsonType: 'array', items: { bsonType: 'string' } },
        s3ConvServiceimplPath:  { bsonType: 'array', items: { bsonType: 'string' } },
        s3ConvVoPath:           { bsonType: 'array', items: { bsonType: 'string' } },

        convControllerReport:   { bsonType: 'array', items: { bsonType: 'object' } },
        convServiceReport:      { bsonType: 'array', items: { bsonType: 'object' } },
        convServiceimplReport:  { bsonType: 'array', items: { bsonType: 'object' } },
        convVoReport:           { bsonType: 'array', items: { bsonType: 'object' } }
      }
    }
  }
});

db.createCollection("security_logs", {
  validator: {
    $jsonSchema: {
      bsonType: "object",
      required: ["jobId", "userId", "s3OriginPath", "savedAt"],
      properties: {
        jobId: { bsonType: "long" },
        userId: { bsonType: "long" },
        s3OriginPath: { bsonType: "string" },
        savedAt: { bsonType: "date" },

        s3AgentInputsPath:      { bsonType: 'array', items: { bsonType: 'string' } },
        s3ReportsDir:           { bsonType: 'array', items: { bsonType: 'string' } },
        s3ReportJsonPath:       { bsonType: 'array', items: { bsonType: 'string' } },
        issueReportFiles:       { bsonType: 'array', items: { bsonType: 'string' } },

        SecurityReport:       { bsonType: 'array', items: { bsonType: 'object' } },
        issueCount:   { bsonType: 'array', items: { bsonType: 'int' } }
      }
    }
  }
});