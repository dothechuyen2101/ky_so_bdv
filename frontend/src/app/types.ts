export type DocumentStatus = 'PENDING' | 'SIGNED' | 'FAILED';
export type ReconciliationStatus =
  'NOT_CHECKED' | 'MATCHED' | 'MISMATCHED' | 'MISSING_FILE' | 'HASH_MISMATCH' | 'DUPLICATE';
export type DownloadFormat = 'JSON' | 'XML' | 'CSV' | 'PDF';
export type OutboxStatus =
  'NEW' | 'PROCESSING' | 'PROCESSED' | 'FAILED' | 'RETRYING' | 'DEAD_LETTER';

export interface DigitalDocument {
  id: string;
  documentCode: string;
  sourceSystem: string;
  transactionId: string;
  customerId: string;
  customerName: string;
  accountNoMasked: string;
  documentType: string;
  businessType: string;
  branchCode: string;
  amount: number;
  currency: string;
  transactionDate: string;
  signedAt: string;
  status: DocumentStatus;
  reconciliationStatus: ReconciliationStatus;
  versionNumber: number;
  signedHash: string;
  certificateSerial: string;
  signerSubject: string;
  fileSize: number;
  mimeType: string;
}

export interface DigitalDocumentDetail {
  metadata: DigitalDocument;
  originalFilePath: string;
  signedFilePath: string;
  originalHash: string;
  signedHash: string;
  signatureAlgorithm: string;
  timestampTokenId: string;
  dataSnapshot: string;
  signatureValue: string;
}

export interface SearchResponse {
  items: DigitalDocument[];
  totalElements: number;
  totalPages: number;
  page: number;
  size: number;
}

export interface DashboardSummary {
  totalRecords: number;
  signedRecords: number;
  failedRecords: number;
  reconciliationIssues: number;
  recordsByBranch: Record<string, number>;
  recentRecords: DigitalDocument[];
}

export interface OutboxRunRequest {
  businessDate?: string | null;
  limit: number;
}

export interface OutboxRun {
  startedAt: string;
  finishedAt: string;
  enqueued: number;
  processed: number;
  succeeded: number;
  failed: number;
  deadLettered: number;
  documents: DigitalDocument[];
}

export interface ApprovalSubmission {
  startedAt: string;
  finishedAt: string;
  submitted: number;
}

export interface FieldDifference {
  path: string;
  leftValue: string | null;
  rightValue: string | null;
}

export interface CompareResponse {
  left: DigitalDocument;
  right: DigitalDocument;
  differences: FieldDifference[];
}

export interface OutboxEvent {
  id: string;
  eventId: string;
  sourceSystem: string;
  eventType: string;
  transactionId: string;
  documentType: string;
  payloadHash: string;
  status: OutboxStatus;
  retryCount: number;
  errorMessage: string | null;
  createdAt: string;
  processedAt: string | null;
  updatedAt: string;
}

export interface OutboxSummary {
  totalEvents: number;
  byStatus: Record<OutboxStatus, number>;
  recentEvents: OutboxEvent[];
}

export interface ReconciliationResponse {
  document: DigitalDocument;
  status: ReconciliationStatus;
  issues: string[];
  originalHashValid: boolean;
  signedHashValid: boolean;
  checkedAt: string;
}

export interface AuditLog {
  id: string;
  action: string;
  actor: string;
  targetType: string;
  targetId: string;
  message: string;
  metadata: string | null;
  createdAt: string;
}

export interface AuditResponse {
  items: AuditLog[];
  totalElements: number;
  totalPages: number;
  page: number;
  size: number;
}

export type UserRole = 'MAKER' | 'CHECKER' | 'ADMIN' | 'USER';

export interface UserAccount {
  id: string;
  username: string;
  displayName: string;
  role: UserRole;
  active: boolean;
  createdAt: string;
  updatedAt: string;
}

export interface CreateUserRequest {
  username: string;
  displayName: string;
  password: string;
  role: UserRole;
}

export interface UpdateUserRequest {
  displayName: string;
  role: UserRole;
  active: boolean;
  password: string | null;
}
