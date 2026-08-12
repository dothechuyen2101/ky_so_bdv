import { CommonModule } from '@angular/common';
import { Component, OnInit, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { errorMessage } from '../../core/http-error';
import { PreferencesService } from '../../core/preferences.service';
import { SessionService } from '../../core/session.service';
import { T24rApiService } from '../../t24r-api.service';
import { ApprovalSubmission, OutboxRun, OutboxSummary } from '../../types';

@Component({
  selector: 'app-operations-page',
  imports: [CommonModule, FormsModule],
  templateUrl: './operations.page.html',
})
export class OperationsPage implements OnInit {
  private readonly api = inject(T24rApiService);
  private readonly preferences = inject(PreferencesService);
  readonly session = inject(SessionService);

  readonly outboxSummary = signal<OutboxSummary | null>(null);
  readonly lastRun = signal<OutboxRun | null>(null);
  readonly lastSubmission = signal<ApprovalSubmission | null>(null);
  readonly error = signal('');
  readonly loading = signal(false);
  outboxLimit = 8;
  outboxBusinessDate = this.isoDateOffset(1);

  ngOnInit(): void {
    this.loadSummary();
  }

  t(text: string): string {
    return this.preferences.t(text);
  }

  loadSummary(): void {
    this.error.set('');
    this.api.outboxSummary().subscribe({
      next: (summary) => this.outboxSummary.set(summary),
      error: (error) => this.error.set(errorMessage(error)),
    });
  }

  runOutbox(): void {
    this.loading.set(true);
    this.error.set('');
    this.api
      .runOutbox({ limit: this.outboxLimit, businessDate: this.outboxBusinessDate || null })
      .subscribe({
        next: (response) => {
          this.lastRun.set(response);
          this.loading.set(false);
          this.loadSummary();
        },
        error: (error) => {
          this.loading.set(false);
          this.error.set(errorMessage(error));
        },
      });
  }

  submitForApproval(): void {
    this.loading.set(true);
    this.error.set('');
    this.api
      .submitForApproval({ limit: this.outboxLimit, businessDate: this.outboxBusinessDate || null })
      .subscribe({
        next: (response) => {
          this.lastSubmission.set(response);
          this.loading.set(false);
          this.loadSummary();
        },
        error: (error) => {
          this.loading.set(false);
          this.error.set(errorMessage(error));
        },
      });
  }

  private isoDateOffset(days: number): string {
    const date = new Date();
    date.setDate(date.getDate() + days);
    return date.toISOString().slice(0, 10);
  }
}
