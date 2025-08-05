// init-mongo.js


db = db.getSiblingDB("bigproject");

db.createUser({
  user: "teammate",
  pwd: "tm1234",
  roles: [{ role: "readWrite", db: "bigproject" }]
});

// conversation_history
db.createCollection("conversation_history", {
  validator: {
    $jsonSchema: {
      bsonType: "object",
      required: ["history_id", "project_id", "status_code", "started_at"],
      properties: {
        history_id: { bsonType: "string" },
        project_id: { bsonType: "string" },
        user_id: { bsonType: "string" },
        llm_model_id: { bsonType: "string" },
        status_code: { bsonType: "string" },
        options: { bsonType: "object" },
        score: { bsonType: "double" },
        log_message: { bsonType: "string" },
        started_at: { bsonType: "date" },
        completed_at: { bsonType: "date" }
      }
    }
  }
});

// translated_file
db.createCollection("translated_file", {
  validator: {
    $jsonSchema: {
      bsonType: "object",
      required: ["translated_file_id", "file_content", "created_at"],
      properties: {
        translated_file_id: { bsonType: "string" },
        source_file_id: { bsonType: "string" },
        history_id: { bsonType: "string" },
        file_name: { bsonType: "string" },
        file_type_code: { bsonType: "string" },
        file_content: { bsonType: "string" },
        created_at: { bsonType: "date" }
      }
    }
  }
});

// package_artifact
db.createCollection("package_artifact", {
  validator: {
    $jsonSchema: {
      bsonType: "object",
      required: ["artifact_id", "storage_path", "file_size", "created_at"],
      properties: {
        artifact_id: { bsonType: "string" },
        history_id: { bsonType: "string" },
        file_name: { bsonType: "string" },
        storage_path: { bsonType: "string" },
        file_size: { bsonType: "long" },
        created_at: { bsonType: "date" }
      }
    }
  }
});

// analysis_report
db.createCollection("analysis_report", {
  validator: {
    $jsonSchema: {
      bsonType: "object",
      required: ["report_id", "report_type_code", "report_content", "created_at"],
      properties: {
        report_id: { bsonType: "string" },
        history_id: { bsonType: "string" },
        report_type_code: { bsonType: "string" },
        report_content: { bsonType: "string" },
        created_at: { bsonType: "date" }
      }
    }
  }
});
