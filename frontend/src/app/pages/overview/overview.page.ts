import { CommonModule } from '@angular/common';
import { Component, OnInit, inject, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { errorMessage } from '../../core/http-error';
import { PreferencesService } from '../../core/preferences.service';
import { T24rApiService } from '../../t24r-api.service';
import { DashboardSummary, OutboxSummary } from '../../types';

@Component({
  selector: 'app-overview-page',
  imports: [CommonModule, RouterLink],
  templateUrl: './overview.page.html',
})
export class OverviewPage implements OnInit {
  private readonly api = inject(T24rApiService);
  private readonly preferences = inject(PreferencesService);

  readonly summary = signal<DashboardSummary | null>(null);
  readonly outboxSummary = signal<OutboxSummary | null>(null);
  readonly error = signal('');

  ngOnInit(): void {
    this.refresh();
  }

  t(text: string): string {
    return this.preferences.t(text);
  }

  refresh(): void {
    this.error.set('');
    this.api.summary().subscribe({
      next: (summary) => this.summary.set(summary),
      error: (error) => this.error.set(errorMessage(error)),
    });
    this.api.outboxSummary().subscribe({
      next: (summary) => this.outboxSummary.set(summary),
      error: (error) => this.error.set(errorMessage(error)),
    });
  }

  branchEntries(): Array<[string, number]> {
    return Object.entries(this.summary()?.recordsByBranch ?? {});
  }

  branchPercent(count: number): number {
    const totalRecords = this.summary()?.totalRecords ?? 0;
    return totalRecords ? (count / totalRecords) * 100 : 0;
  }
}
