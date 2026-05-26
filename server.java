import express from "express";
import path from "path";
import crypto from "crypto";
import { createServer as createViteServer } from "vite";
import { Document, FileRevision, AuditLog, SpringBootLog, SystemStats } from "./src/types";

// Setup Express
const app = express();
const PORT = 3000;

app.use(express.json());

// In-Memory Database Store
let documents: Document[] = [];
let revisions: FileRevision[] = [];
let auditLogs: AuditLog[] = [];
let springBootConsoleLogs: SpringBootLog[] = [];

// Track the master hash of the audit trail ledger to prove its mathematical dependency
let lastLedgerHash = "0000000000000000000000000000000000000000000000000000000000000000";

// Helper: Calculate SHA-256 hash
function calculateSHA256(text: string): string {
  return crypto.createHash("sha256").update(text).digest("hex");
}

// Simulated Spring Boot system logging
function logSpringBoot(level: 'INFO' | 'WARN' | 'ERROR' | 'DEBUG', logger: string, message: string) {
  springBootConsoleLogs.push({
    timestamp: new Date().toISOString(),
    level,
    logger,
    message
  });
  if (springBootConsoleLogs.length > 200) {
    springBootConsoleLogs.shift();
  }
}

// Immutable Ledger appending mechanism (Similar to PostgreSQL ledger implementation)
function appendToLedger(
  documentId: string, 
  revisionId: string, 
  action: AuditLog['action'], 
  details: string, 
  performedBy: string
): AuditLog {
  const id = `TX-${crypto.randomBytes(4).toString("hex").toUpperCase()}`;
  const timestamp = new Date().toISOString();
  const clientIp = "10.244.4.82";

  // Recalculate hash dependent on the record and the previous block's hash, forming an immutable chain
  const dataToHash = id + "|" + documentId + "|" + revisionId + "|" + action + "|" + details + "|" + performedBy + "|" + timestamp + "|" + clientIp + "|" + lastLedgerHash;
  const entryHash = calculateSHA256(dataToHash);
  
  const logEntry: AuditLog = {
    id,
    documentId,
    revisionId,
    action,
    details,
    performedBy,
    timestamp,
    clientIp,
    previousEntryHash: lastLedgerHash,
    entryHash,
    tampered: false,
  };

  auditLogs.push(logEntry);
  lastLedgerHash = entryHash;

  // Mirror as Spring Boot JPA Ledger Entity log
  logSpringBoot(
    "INFO", 
    "com.secure.trace.ledger.AuditLedgerRepository", 
    `INSERT INTO audit_ledger (tx_id, document_id, revision_id, action_type, prev_hash, curr_hash, executed_by) VALUES ('${id}', '${documentId}', '${revisionId}', '${action}', '${logEntry.previousEntryHash.substring(0, 8)}...', '${entryHash.substring(0, 8)}...', '${performedBy}')`
  );
  
  logSpringBoot(
    "INFO",
    "com.secure.trace.config.SecureLedgerEngine",
    `Ledger integrity validated. Block chain height: ${auditLogs.length}. Master hash checkpoint: ${lastLedgerHash.substring(0, 16)}`
  );

  return logEntry;
}

// Initialize seed data with pre-calculated, verified cryptographic hash chain
function initSeedData() {
  logSpringBoot("INFO", "org.springframework.boot.SpringApplication", "Starting TraceabilityApplication on Cloud-Run-Sandbox with JVM 21");
  logSpringBoot("INFO", "org.hibernate.Version", "HCANN000001: Hibernate Commons Annotations {6.0.6.Final}");
  logSpringBoot("INFO", "org.postgresql.Driver", "PostgreSQL JDBC Driver 42.6.0 connected successfully to schema: document_trace_db");
  logSpringBoot("INFO", "com.secure.trace.config.DatabaseLedgerLoader", "Pre-verifying PostgreSQL pg_ledger configuration and database security triggers...");

  // Reset collections
  documents = [];
  revisions = [];
  auditLogs = [];
  springBootConsoleLogs = springBootConsoleLogs.slice(0, 4); // keep bootstrap logs
  lastLedgerHash = "0000000000000000000000000000000000000000000000000000000000000000";

  // DOCUMENT 1 (Smart Contract Audit)
  const d1Id = "DOC-A8F2";
  const doc1: Document = {
    id: d1Id,
    title: "Secure Ether Vault Audit Protocol",
    department: "Engineering",
    classification: "Confidential",
    createdBy: "Sarah Jenkins (Auditor)",
    createdAt: new Date(Date.now() - 1000 * 60 * 60 * 48).toISOString(), // 48h ago
    currentVersion: "v1.2.0",
    status: "Active",
    description: "Multi-sig smart contract protocol vulnerability report and formal verification metrics."
  };
  documents.push(doc1);

  // Rev 1.0.0
  const r1_0Id = "REV-901";
  const r1_0Hash = calculateSHA256("EtherVault contract initial checkout code draft v1.0");
  const rev1_0: FileRevision = {
    id: r1_0Id,
    documentId: d1Id,
    versionNumber: "v1.0.0",
    fileSize: 421042,
    mimeType: "application/pdf",
    fileHash: r1_0Hash,
    previousHash: "0000000000000000000000000000000000000000000000000000000000000000",
    uploadedBy: "Sarah Jenkins",
    uploadedAt: new Date(Date.now() - 1000 * 60 * 60 * 48).toISOString(),
    changeSummary: "Initial report draft import.",
    signatureVerified: true,
    signatureName: "Sarah Jenkins"
  };
  revisions.push(rev1_0);
  appendToLedger(d1Id, r1_0Id, "DOCUMENT_CREATED", "Document record and v1.0.0 initialized with SHA-256", "Sarah Jenkins");

  // Rev 1.1.0
  const r1_1Id = "REV-902";
  const r1_1Hash = calculateSHA256("EtherVault contract code draft v1.1 - patched issue 49");
  const rev1_1: FileRevision = {
    id: r1_1Id,
    documentId: d1Id,
    versionNumber: "v1.1.0",
    fileSize: 428955,
    mimeType: "application/pdf",
    fileHash: r1_1Hash,
    previousHash: r1_0Hash,
    uploadedBy: "Sarah Jenkins",
    uploadedAt: new Date(Date.now() - 1000 * 60 * 60 * 24).toISOString(), // 24h ago
    changeSummary: "Patched critical reentrancy finding on Line 142. Re-audited bytecode.",
    signatureVerified: true,
    signatureName: "Sarah Jenkins"
  };
  revisions.push(rev1_1);
  appendToLedger(d1Id, r1_1Id, "REVISION_ADDED", "Created revision v1.1.0 reflecting reentrancy patches", "Sarah Jenkins");

  // Rev 1.2.0
  const r1_2Id = "REV-903";
  const r1_2Hash = calculateSHA256("EtherVault contract code compilation v1.2 - final certified audit");
  const rev1_2: FileRevision = {
    id: r1_2Id,
    documentId: d1Id,
    versionNumber: "v1.2.0",
    fileSize: 450125,
    mimeType: "application/pdf",
    fileHash: r1_2Hash,
    previousHash: r1_1Hash,
    uploadedBy: "Sarah Jenkins",
    uploadedAt: new Date(Date.now() - 1000 * 60 * 60 * 4).toISOString(), // 4h ago
    changeSummary: "Final certification issued with clean report approval.",
    signatureVerified: true,
    signatureName: "Sarah Jenkins"
  };
  revisions.push(rev1_2);
  appendToLedger(d1Id, r1_2Id, "REVISION_ADDED", "Created final revision v1.2.0 approved for mainnet deployment", "Sarah Jenkins");
  appendToLedger(d1Id, r1_2Id, "SIGNATURE_APPLIED", "Co-signed document revision v1.2.0 with cryptographic verification", "David Vance (CEO)");

  // DOCUMENT 2 (Vendor Framework Agreement - Tesla)
  const d2Id = "DOC-E412";
  const doc2: Document = {
    id: d2Id,
    title: "Tesla Supply Chain Master SLA",
    department: "Legal",
    classification: "Restricted",
    createdBy: "Michael Cho (Counsel)",
    createdAt: new Date(Date.now() - 1000 * 60 * 60 * 12).toISOString(), // 12h ago
    currentVersion: "v1.0.0",
    status: "Under Review",
    description: "Supply chain liability limits, dispute resolution protocols, and industrial delivery guidelines."
  };
  documents.push(doc2);

  // Rev 1.0.0
  const r2_0Id = "REV-101";
  const r2_0Hash = calculateSHA256("Tesla SLA Contract Content Draft Version 1.0 Original copy text.");
  const rev2_0: FileRevision = {
    id: r2_0Id,
    documentId: d2Id,
    versionNumber: "v1.0.0",
    fileSize: 189201,
    mimeType: "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
    fileHash: r2_0Hash,
    previousHash: "0000000000000000000000000000000000000000000000000000000000000000",
    uploadedBy: "Michael Cho",
    uploadedAt: new Date(Date.now() - 1000 * 60 * 60 * 12).toISOString(),
    changeSummary: "Initial boilerplate upload in drafting stage.",
    signatureVerified: false,
    signatureName: ""
  };
  revisions.push(rev2_0);
  appendToLedger(d2Id, r2_0Id, "DOCUMENT_CREATED", "Imported corporate vendor SLA model v1.0.0", "Michael Cho");

  // DOCUMENT 3 (Clinical Trial Protocol)
  const d3Id = "DOC-C902";
  const doc3: Document = {
    id: d3Id,
    title: "Covid Variant Booster Trial Schema v4",
    department: "R&D Medicine",
    classification: "Confidential",
    createdBy: "Dr. Aris Thorne",
    createdAt: new Date(Date.now() - 1000 * 60 * 60 * 72).toISOString(), // 72h ago
    currentVersion: "v4.0.0",
    status: "Active",
    description: "Clinical protocol detailing patient groups, double-blind triggers, and immunological telemetry."
  };
  documents.push(doc3);

  // Rev 4.0.0
  const r3_0Id = "REV-301";
  const r3_0Hash = calculateSHA256("Clinical Booster Trial Phase III Protocol Schema v4.0.0 final draft research documentation.");
  const rev3_0: FileRevision = {
    id: r3_0Id,
    documentId: d3Id,
    versionNumber: "v4.0.0",
    fileSize: 1240182,
    mimeType: "application/pdf",
    fileHash: r3_0Hash,
    previousHash: "92adf3a260bc516cef5d3122c6beef9c792adf3f60bc5d93bcad839401bee92a", // hypothetical old version hash
    uploadedBy: "Dr. Aris Thorne",
    uploadedAt: new Date(Date.now() - 1000 * 60 * 60 * 72).toISOString(),
    changeSummary: "Booster Trial Phase III medical trial definitions.",
    signatureVerified: true,
    signatureName: "Dr. Aris Thorne"
  };
  revisions.push(rev3_0);
  appendToLedger(d3Id, r3_0Id, "DOCUMENT_CREATED", "Clinical booster scheme registered & signed by R&D lead", "Dr. Aris Thorne");
  
  logSpringBoot("INFO", "com.secure.trace.config.DatabaseLedgerLoader", "Database ledger and document tree initialized with cryptographically verified integrity!");
}

// Prepare baseline data
initSeedData();

// Backup records to handle interactive rollback/restore simulator
let backupDocuments = JSON.parse(JSON.stringify(documents));
let backupRevisions = JSON.parse(JSON.stringify(revisions));
let backupAuditLogs = JSON.parse(JSON.stringify(auditLogs));
let backupLastLedgerHash = lastLedgerHash;

// API Routes
app.get("/api/documents", (req, res) => {
  logSpringBoot("INFO", "com.secure.trace.controller.DocumentController", "GET /api/documents - Fetching active document directory and validation indices");
  res.json(documents);
});

app.get("/api/documents/:id", (req, res) => {
  const doc = documents.find(d => d.id === req.params.id);
  if (!doc) {
    logSpringBoot("WARN", "com.secure.trace.controller.DocumentController", `Document with ID ${req.params.id} not found in repository`);
    return res.status(404).json({ error: "Document not found" });
  }
  const docRevs = revisions.filter(r => r.documentId === doc.id);
  logSpringBoot("INFO", "com.secure.trace.controller.DocumentController", `GET /api/documents/${req.params.id} - Loaded metadata and all ${docRevs.length} version hashes`);
  res.json({ document: doc, revisions: docRevs });
});

app.post("/api/documents", (req, res) => {
  const { title, department, classification, description, createdBy, fileContent, changeSummary, sizeBytes, mimeType } = req.body;
  
  if (!title || !department || !classification || !createdBy || !fileContent) {
    logSpringBoot("ERROR", "com.secure.trace.controller.DocumentController", "POST /api/documents failed due to missing required payloads");
    return res.status(400).json({ error: "Missing required document attributes" });
  }

  const docId = `DOC-${crypto.randomBytes(2).toString("hex").toUpperCase()}`;
  const revId = `REV-${crypto.randomBytes(2).toString("hex").toUpperCase()}`;
  const fileHash = calculateSHA256(fileContent);

  const newDoc: Document = {
    id: docId,
    title,
    department,
    classification,
    createdBy,
    createdAt: new Date().toISOString(),
    currentVersion: "v1.0.0",
    status: "Active",
    description: description || ""
  };

  const newRev: FileRevision = {
    id: revId,
    documentId: docId,
    versionNumber: "v1.0.0",
    fileSize: sizeBytes || Math.floor(Math.random() * 800000) + 10000,
    mimeType: mimeType || "application/pdf",
    fileHash,
    previousHash: "0000000000000000000000000000000000000000000000000000000000000000",
    uploadedBy: createdBy,
    uploadedAt: new Date().toISOString(),
    changeSummary: changeSummary || "Initial secure import.",
    signatureVerified: true,
    signatureName: createdBy
  };

  documents.push(newDoc);
  revisions.push(newRev);

  appendToLedger(docId, revId, "DOCUMENT_CREATED", `Registered document: '${title}' under department '${department}'`, createdBy);

  // Sync safety backup reference
  backupDocuments = JSON.parse(JSON.stringify(documents));
  backupRevisions = JSON.parse(JSON.stringify(revisions));
  backupAuditLogs = JSON.parse(JSON.stringify(auditLogs));
  backupLastLedgerHash = lastLedgerHash;

  logSpringBoot("INFO", "com.secure.trace.controller.DocumentController", `Created secure document trace ${docId}. Initial version: v1.0.0. Cryptographic SHA-256 target: ${fileHash.substring(0, 16)}...`);
  res.status(201).json({ document: newDoc, revision: newRev });
});

app.post("/api/documents/:id/revisions", (req, res) => {
  const doc = documents.find(d => d.id === req.params.id);
  if (!doc) {
    logSpringBoot("WARN", "com.secure.trace.controller.DocumentController", `Revision insertion failed: Document ${req.params.id} does not exist`);
    return res.status(404).json({ error: "Document not found" });
  }

  const { fileContent, changeSummary, uploadedBy, sizeBytes, mimeType } = req.body;
  if (!fileContent || !uploadedBy) {
    return res.status(400).json({ error: "Missing required revision attributes" });
  }

  const docRevs = revisions.filter(r => r.documentId === doc.id);
  // Sort revisions to grab current latest
  docRevs.sort((a, b) => new Date(a.uploadedAt).getTime() - new Date(b.uploadedAt).getTime());
  const lastRev = docRevs[docRevs.length - 1];

  // Parse last version number (e.g. "v1.2.0") and increment minor version
  let nextVersion = "v1.1.0";
  if (lastRev) {
    const cleanVer = lastRev.versionNumber.replace("v", "");
    const parts = cleanVer.split(".").map(Number);
    if (parts.length === 3 && !isNaN(parts[1])) {
      parts[1] += 1; // bump minor
      parts[2] = 0;   // reset patch
      nextVersion = `v${parts.join(".")}`;
    }
  }

  const revId = `REV-${crypto.randomBytes(2).toString("hex").toUpperCase()}`;
  const fileHash = calculateSHA256(fileContent);

  const newRev: FileRevision = {
    id: revId,
    documentId: doc.id,
    versionNumber: nextVersion,
    fileSize: sizeBytes || Math.floor(Math.random() * 500000) + 15000,
    mimeType: mimeType || "application/pdf",
    fileHash,
    previousHash: lastRev ? lastRev.fileHash : "0000000000000000000000000000000000000000000000000000000000000000",
    uploadedBy,
    uploadedAt: new Date().toISOString(),
    changeSummary: changeSummary || `Upgrade revision to ${nextVersion}`,
    signatureVerified: true,
    signatureName: uploadedBy
  };

  revisions.push(newRev);

  // Update document's current version index
  doc.currentVersion = nextVersion;

  appendToLedger(doc.id, revId, "REVISION_ADDED", `Appended revision ${nextVersion} summarizing patches: ${changeSummary}`, uploadedBy);

  // Sync safety backup reference
  backupDocuments = JSON.parse(JSON.stringify(documents));
  backupRevisions = JSON.parse(JSON.stringify(revisions));
  backupAuditLogs = JSON.parse(JSON.stringify(auditLogs));
  backupLastLedgerHash = lastLedgerHash;

  logSpringBoot("INFO", "com.secure.trace.controller.DocumentController", `Added revision ${nextVersion} for ${doc.id}. Previous hash: ${newRev.previousHash.substring(0, 10)}... Current hash: ${fileHash.substring(0, 10)}...`);
  res.status(201).json({ document: doc, revision: newRev });
});

app.post("/api/documents/:id/sign", (req, res) => {
  const doc = documents.find(d => d.id === req.params.id);
  if (!doc) return res.status(404).json({ error: "Document not found" });

  const { signatureName } = req.body;
  if (!signatureName) return res.status(400).json({ error: "signatureName is required" });

  const docRevs = revisions.filter(r => r.documentId === doc.id);
  docRevs.sort((a, b) => new Date(a.uploadedAt).getTime() - new Date(b.uploadedAt).getTime());
  const currentRev = docRevs[docRevs.length - 1];

  if (!currentRev) return res.status(400).json({ error: "No revision exists to sign" });

  currentRev.signatureVerified = true;
  currentRev.signatureName = signatureName;

  appendToLedger(doc.id, currentRev.id, "SIGNATURE_APPLIED", `SecOps crypto signature authorized for edition '${currentRev.versionNumber}'`, signatureName);

  // Sync safety backup reference
  backupDocuments = JSON.parse(JSON.stringify(documents));
  backupRevisions = JSON.parse(JSON.stringify(revisions));
  backupAuditLogs = JSON.parse(JSON.stringify(auditLogs));
  backupLastLedgerHash = lastLedgerHash;

  logSpringBoot("INFO", "com.secure.trace.service.SecuritySignatureService", `Crypto-signed revision ${currentRev.versionNumber} of ${doc.id} under signature holder: ${signatureName}`);
  res.json({ document: doc, revision: currentRev });
});

app.get("/api/ledger", (req, res) => {
  logSpringBoot("INFO", "com.secure.trace.controller.AuditLedgerController", "GET /api/ledger - Verified and loading complete cryptographic ledger block sequence");
  res.json(auditLogs);
});

app.get("/api/springboot-logs", (req, res) => {
  res.json(springBootConsoleLogs);
});

// Stats API
app.get("/api/stats", (req, res) => {
  const totalDocuments = documents.length;
  const totalRevisions = revisions.length;
  const totalLogEntries = auditLogs.length;
  const unverifiedRevisions = revisions.filter(r => !r.signatureVerified).length;

  // Pie chart variables
  const deptCount: { [key: string]: number } = {};
  const classCount: { [key: string]: number } = {};

  documents.forEach(d => {
    deptCount[d.department] = (deptCount[d.department] || 0) + 1;
    classCount[d.classification] = (classCount[d.classification] || 0) + 1;
  });

  const byDepartment = Object.entries(deptCount).map(([name, value]) => ({ name, value }));
  const byClassification = Object.entries(classCount).map(([name, value]) => ({ name, value }));

  // Mathematical Audit Chain Health Verification
  let ledgerChainHealthy = true;
  let currentHash = "0000000000000000000000000000000000000000000000000000000000000000";

  for (let i = 0; i < auditLogs.length; i++) {
    const log = auditLogs[i];
    // Check backlink integrity
    if (log.previousEntryHash !== currentHash) {
      ledgerChainHealthy = false;
      break;
    }

    // Verify block's specific hash
    const expectedData = log.id + "|" + log.documentId + "|" + log.revisionId + "|" + log.action + "|" + log.details + "|" + log.performedBy + "|" + log.timestamp + "|" + log.clientIp + "|" + log.previousEntryHash;
    const recomputedHash = calculateSHA256(expectedData);
    if (log.entryHash !== recomputedHash || log.tampered) {
      ledgerChainHealthy = false;
      break;
    }

    currentHash = log.entryHash;
  }

  const stats: SystemStats = {
    totalDocuments,
    totalRevisions,
    totalLogEntries,
    unverifiedRevisions,
    byDepartment,
    byClassification,
    ledgerChainHealthy
  };

  res.json(stats);
});

/**
 * SIMULATE DATABASE TAMPERING (INJECTION OVERRIDE)
 * In a real PostgreSQL database, if an administrator or rogue process modifies records directly 
 * in SQL bypasses the Spring Boot application layer (e.g. editing raw bytes in storage), the chain breaks.
 * This simulates that by forcing an unauthorized value change directly inside memory.
 */
app.post("/api/ledger/tamper", (req, res) => {
  const { docId, tamperField } = req.body;
  
  if (!docId) {
    return res.status(400).json({ error: "Missing document target ID" });
  }

  const doc = documents.find(d => d.id === docId);
  if (!doc) {
    return res.status(404).json({ error: "Document not found" });
  }

  logSpringBoot("WARN", "org.postgresql.jdbc.PgConnection", "ALERT: Direct DB table edit detected outside Spring transaction coordinator bounds!");

  if (tamperField === "fileHash") {
    // Modify file hash of the latest revision of this document directly
    const docRevs = revisions.filter(r => r.documentId === doc.id);
    docRevs.sort((a, b) => new Date(a.uploadedAt).getTime() - new Date(b.uploadedAt).getTime());
    if (docRevs.length > 0) {
      const target = docRevs[docRevs.length - 1];
      const oldHash = target.fileHash;
      target.fileHash = calculateSHA256("TAMPERED MANIPULATED ILLEGAL CONTENT");
      
      logSpringBoot("ERROR", "com.secure.trace.config.SecureLedgerEngine", `CRITICAL LEDGER MISMATCH: Hard disk contents corrupted/overridden for document revision ${target.id}. Recorded hash: ${oldHash.substring(0,10)}... Actual data hash: ${target.fileHash.substring(0,10)}...`);
    }
  } else {
    // Tamper with a metadata title as direct SQL injection update
    const oldTitle = doc.title;
    doc.title = "[TAMPERED] " + doc.title;
    logSpringBoot("ERROR", "org.hibernate.assertion.LockException", `SQL row integrity validation failure: Document ${doc.id} row checksum did not resolve correctly with db trigger. Raw record mutated in DB tables independently.`);
  }

  // Also introduce a direct entry alteration inside a historical audit log to trigger a ledger tamper state
  const logToTamper = auditLogs.find(l => l.documentId === docId);
  if (logToTamper) {
    logToTamper.details = "[TAMPERED CONTENT] " + logToTamper.details;
    logToTamper.tampered = true; // flag tampered in entry record
  }

  appendToLedger(doc.id, "REV-NUL", "TAMPER_DETECTED", "CRITICAL ERROR: Master audit ledger validation query detected metadata value inconsistency with hash chain", "System Guard Daemon");

  res.json({ success: true, message: "Database cell bypassed and tampered. Ledger hash verification will now fail!" });
});

// RESTORE DATABASE HEALTH FROM LOCAL CRYPTOGRAPHIC OFF-SITE BACKUP
app.post("/api/ledger/restore", (req, res) => {
  logSpringBoot("INFO", "com.secure.trace.service.DatabaseRecoveryCoordinator", "Executing Ledger recovery protocol from off-site secure read-only backup cluster...");
  
  documents = JSON.parse(JSON.stringify(backupDocuments));
  revisions = JSON.parse(JSON.stringify(backupRevisions));
  auditLogs = JSON.parse(JSON.stringify(backupAuditLogs));
  lastLedgerHash = backupLastLedgerHash;

  appendToLedger("SYS-RECOV", "REV-NUL", "LEDGER_RESTORED", "Database records restored successfully. Ledger chain verified fully clean and integrity certified.", "System Administrator");

  logSpringBoot("INFO", "com.secure.trace.config.SecureLedgerEngine", `Off-site synchronization complete. Current database hash matching primary master node: ${lastLedgerHash.substring(0, 16)}... Status: SECURE`);
  
  res.json({ success: true, message: "Ledger and file structures restored successfully to authentic state." });
});

async function startServer() {
  // Vite middleware for development
  if (process.env.NODE_ENV !== "production") {
    const vite = await createViteServer({
      server: { middlewareMode: true },
      appType: "spa",
    });
    app.use(vite.middlewares);
  } else {
    const distPath = path.join(process.cwd(), 'dist');
    app.use(express.static(distPath));
    app.get('*', (req, res) => {
      res.sendFile(path.join(distPath, 'index.html'));
    });
  }

  app.listen(PORT, "0.0.0.0", () => {
    console.log(`Server running on http://localhost:${PORT}`);
  });
}

startServer();
