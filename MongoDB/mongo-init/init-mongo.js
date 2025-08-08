db = db.getSiblingDB("conversion_db");

db.createCollection("conversion_logs", {
  validator: {
    $jsonSchema: {
      bsonType: "object",
      required: ["jobId", "userId", "s3Path", "savedAt"],
      properties: {
        jobId: { bsonType: "long" },
        userId: { bsonType: "long" },
        inputLanguage: { bsonType: "string" },
        s3Path: { bsonType: "string" },
        savedAt: { bsonType: "date" }
      }
    }
  }
});