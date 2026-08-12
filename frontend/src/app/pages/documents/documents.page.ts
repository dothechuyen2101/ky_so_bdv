import { CommonModule } from '@angular/common';
import { Component, OnInit, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { errorMessage } from '../../core/http-error';
import { PreferencesService } from '../../core/preferences.service';
import { SessionService } from '../../core/session.service';
import { T24rApiService } from '../../t24r-api.service';
import {
  CompareResponse,
  DigitalDocument,
  DigitalDocumentDetail,
  DocumentStatus,
  DownloadFormat,
  ReconciliationStatus,
} from '../../types';

@Component({
  selector: 'app-documents-page',
  imports: [CommonModule, FormsModule],
  templateUrl: './documents.page.html',
})
export class DocumentsPage implements OnInit {
  private readonly api = inject(T24rApiService);
  private readonly preferences = inject(PreferencesService);
  readonly session = inject(SessionService);

  readonly documents = signal<DigitalDocument[]>([]);
  readonly selectedDetail = signal<DigitalDocumentDetail | null>(null);
  readonly compareResult = signal<CompareResponse | null>(null);
  readonly error = signal('');
  readonly loading = signal(false);

  selectedLeftId = '';
  selectedRightId = '';
  filters: {
    documentCode: string;
    transactionId: string;
    customerId: string;
    branchCode: string;
    documentType: string;
    status: DocumentStatus | '';
    reconciliationStatus: ReconciliationStatus | '';
  } = {
    documentCode: '',
    transactionId: '',
    customerId: '',
    branchCode: '',
    documentType: '',
    status: '',
    reconciliationStatus: '',
  };

  readonly statuses: Array<DocumentStatus | ''> = ['', 'SIGNED', 'PENDING', 'FAILED'];
  readonly reconciliationStatuses: Array<ReconciliationStatus | ''> = [
    '',
    'NOT_CHECKED',
    'MATCHED',
    'MISMATCHED',
    'MISSING_FILE',
    'HASH_MISMATCH',
    'DUPLICATE',
  ];
  readonly formats: DownloadFormat[] = ['JSON', 'XML', 'CSV', 'PDF'];

  ngOnInit(): void {
    this.search();
  }

  t(text: string): string {
    return this.preferences.t(text);
  }

  search(): void {
    this.loading.set(true);
    this.error.set('');
    this.api.search({ ...this.filters, page: 0, size: 20 }).subscribe({
      next: (response) => {
        this.documents.set(response.items);
        this.loading.set(false);
      },
      error: (error) => {
        this.loading.set(false);
        this.error.set(errorMessage(error));
      },
    });
  }

  selectDocument(item: DigitalDocument): void {
    this.api.detail(item.id).subscribe({
      next: (detail) => this.selectedDetail.set(detail),
      error: (error) => this.error.set(errorMessage(error)),
    });
  }

  setCompare(side: 'left' | 'right', item: DigitalDocument): void {
    if (side === 'left') {
      this.selectedLeftId = item.id;
    } else {
      this.selectedRightId = item.id;
    }
    if (
      this.selectedLeftId &&
      this.selectedRightId &&
      this.selectedLeftId !== this.selectedRightId
    ) {
      this.compare();
    }
  }

  compare(): void {
    if (
      !this.selectedLeftId ||
      !this.selectedRightId ||
      this.selectedLeftId === this.selectedRightId
    ) {
      return;
    }
    this.api.compare(this.selectedLeftId, this.selectedRightId).subscribe({
      next: (response) => this.compareResult.set(response),
      error: (error) => this.error.set(errorMessage(error)),
    });
  }

  reconcile(item: DigitalDocument): void {
    this.api.reconcile(item).subscribe({
      next: () => this.search(),
      error: (error) => this.error.set(errorMessage(error)),
    });
  }

  download(item: DigitalDocument, format: DownloadFormat): void {
    this.api.download(item, format).subscribe({
      next: (response) => {
        const blob = response.body;
        if (!blob) {
          return;
        }
        const url = URL.createObjectURL(blob);
        const link = document.createElement('a');
        link.href = url;
        link.download = `${item.documentCode}.${format.toLowerCase()}`;
        link.click();
        URL.revokeObjectURL(url);
      },
      error: (error) => this.error.set(errorMessage(error)),
    });
  }

  statusClass(status: ReconciliationStatus): string {
    if (status === 'MATCHED') {
      return 'matched';
    }
    return status === 'NOT_CHECKED' ? 'neutral' : 'warning';
  }
}
