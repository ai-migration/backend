db = db.getSiblingDB("conversion_db");

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
        s3ConvPath: { bsonType: "object"},
        convReport: { bsonType: "object"}
      }
    }
  }
});