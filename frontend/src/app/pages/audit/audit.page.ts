import { CommonModule } from '@angular/common';
import { Component, OnInit, inject, signal } from '@angular/core';
import { errorMessage } from '../../core/http-error';
import { PreferencesService } from '../../core/preferences.service';
import { T24rApiService } from '../../t24r-api.service';
import { AuditLog } from '../../types';

@Component({
  selector: 'app-audit-page',
  imports: [CommonModule],
  templateUrl: './audit.page.html',
})
export class AuditPage implements OnInit {
  private readonly api = inject(T24rApiService);
  private readonly preferences = inject(PreferencesService);

  readonly auditLogs = signal<AuditLog[]>([]);
  readonly error = signal('');

  ngOnInit(): void {
    this.loadAudit();
  }

  t(text: string): string {
    return this.preferences.t(text);
  }

  loadAudit(): void {
    this.error.set('');
    this.api.auditLogs().subscribe({
      next: (response) => this.auditLogs.set(response.items),
      error: (error) => this.error.set(errorMessage(error)),
    });
  }
}
