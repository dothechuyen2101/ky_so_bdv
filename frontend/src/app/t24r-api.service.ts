import { HttpClient, HttpHeaders, HttpParams, HttpResponse } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import {
  AuditResponse,
  ApprovalSubmission,
  CompareResponse,
  DashboardSummary,
  DigitalDocument,
  DigitalDocumentDetail,
  DocumentStatus,
  DownloadFormat,
  OutboxRun,
  OutboxRunRequest,
  OutboxSummary,
  ReconciliationResponse,
  ReconciliationStatus,
  SearchResponse,
  CreateUserRequest,
  UpdateUserRequest,
  UserAccount,
} from './types';

interface SearchCriteria {
  documentCode?: string;
  transactionId?: string;
  customerId?: string;
  branchCode?: string;
  documentType?: string;
  status?: DocumentStatus | '';
  reconciliationStatus?: ReconciliationStatus | '';
  fromDate?: string;
  toDate?: string;
  page?: number;
  size?: number;
}

@Injectable({ providedIn: 'root' })
export class T24rApiService {
  private readonly apiBase = 'http://localhost:8080';
  private credentials = '';

  constructor(private readonly http: HttpClient) {}

  authenticate(
    username: string,
    password: string,
  ): Observable<{ username: string; roles: string[] }> {
    this.credentials = btoa(`${username}:${password}`);
    return this.http.get<{ username: string; roles: string[] }>(`${this.apiBase}/api/v1/auth/me`, {
      headers: this.authHeaders,
    });
  }

  clearCredentials(): void {
    this.credentials = '';
  }

  private get authHeaders(): HttpHeaders {
    return new HttpHeaders({ Authorization: `Basic ${this.credentials}` });
  }

  summary(): Observable<DashboardSummary> {
    return this.http.get<DashboardSummary>(`${this.apiBase}/api/v1/dashboard/summary`, {
      headers: this.authHeaders,
    });
  }

  search(criteria: SearchCriteria): Observable<SearchResponse> {
    let params = new HttpParams().set('page', criteria.page ?? 0).set('size', criteria.size ?? 20);
    for (const [key, value] of Object.entries(criteria)) {
      if (
        value !== undefined &&
        value !== null &&
        value !== '' &&
        key !== 'page' &&
        key !== 'size'
      ) {
        params = params.set(key, String(value));
      }
    }
    return this.http.get<SearchResponse>(`${this.apiBase}/api/v1/documents`, {
      headers: this.authHeaders,
      params,
    });
  }

  detail(id: string): Observable<DigitalDocumentDetail> {
    return this.http.get<DigitalDocumentDetail>(`${this.apiBase}/api/v1/documents/${id}`, {
      headers: this.authHeaders,
    });
  }

  submitForApproval(request: OutboxRunRequest): Observable<ApprovalSubmission> {
    return this.http.post<ApprovalSubmission>(`${this.apiBase}/api/v1/ingestions/run`, request, {
      headers: this.authHeaders,
    });
  }

  runOutbox(request: OutboxRunRequest): Observable<OutboxRun> {
    return this.http.post<OutboxRun>(`${this.apiBase}/api/v1/outbox-events/run`, request, {
      headers: this.authHeaders,
    });
  }

  outboxSummary(): Observable<OutboxSummary> {
    return this.http.get<OutboxSummary>(`${this.apiBase}/api/v1/outbox-events/summary`, {
      headers: this.authHeaders,
    });
  }

  compare(leftId: string, rightId: string): Observable<CompareResponse> {
    const params = new HttpParams().set('leftId', leftId).set('rightId', rightId);
    return this.http.get<CompareResponse>(`${this.apiBase}/api/v1/documents/compare`, {
      headers: this.authHeaders,
      params,
    });
  }

  auditLogs(): Observable<AuditResponse> {
    return this.http.get<AuditResponse>(`${this.apiBase}/api/v1/audit-logs`, {
      headers: this.authHeaders,
      params: new HttpParams().set('size', 12),
    });
  }

  reconcile(document: DigitalDocument): Observable<ReconciliationResponse> {
    return this.http.post<ReconciliationResponse>(
      `${this.apiBase}/api/v1/reconciliation/documents/${document.id}`,
      {},
      { headers: this.authHeaders },
    );
  }

  download(document: DigitalDocument, format: DownloadFormat): Observable<HttpResponse<Blob>> {
    return this.http.get(`${this.apiBase}/api/v1/documents/${document.id}/download`, {
      headers: this.authHeaders,
      params: new HttpParams().set('format', format),
      observe: 'response',
      responseType: 'blob',
    });
  }

  users(): Observable<UserAccount[]> {
    return this.http.get<UserAccount[]>(`${this.apiBase}/api/v1/users`, {
      headers: this.authHeaders,
    });
  }

  createUser(request: CreateUserRequest): Observable<UserAccount> {
    return this.http.post<UserAccount>(`${this.apiBase}/api/v1/users`, request, {
      headers: this.authHeaders,
    });
  }

  updateUser(id: string, request: UpdateUserRequest): Observable<UserAccount> {
    return this.http.put<UserAccount>(`${this.apiBase}/api/v1/users/${id}`, request, {
      headers: this.authHeaders,
    });
  }
}
