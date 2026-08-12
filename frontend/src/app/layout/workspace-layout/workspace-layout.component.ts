import { Component, inject } from '@angular/core';
import { Router, RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';
import { PreferencesService, Language } from '../../core/preferences.service';
import { SessionService } from '../../core/session.service';

type WorkspaceView = 'overview' | 'documents' | 'operations' | 'audit' | 'users';

const VIEW_TITLES: Record<WorkspaceView, string> = {
  overview: 'Operations overview',
  documents: 'Document search',
  operations: 'Signing operations',
  audit: 'Audit activity',
  users: 'Users & permissions',
};

@Component({
  selector: 'app-workspace-layout',
  imports: [RouterLink, RouterLinkActive, RouterOutlet],
  templateUrl: './workspace-layout.component.html',
})
export class WorkspaceLayoutComponent {
  private readonly router = inject(Router);
  readonly session = inject(SessionService);
  readonly preferences = inject(PreferencesService);

  t(text: string): string {
    return this.preferences.t(text);
  }

  setLanguage(language: Language): void {
    this.preferences.setLanguage(language);
  }

  activeView(): WorkspaceView {
    const view = this.router.url.split('?')[0].split('/')[1] as WorkspaceView | undefined;
    return view && view in VIEW_TITLES ? view : 'documents';
  }

  viewTitle(): string {
    return this.t(VIEW_TITLES[this.activeView()]);
  }

  logout(): void {
    this.session.logout();
    void this.router.navigate(['/login']);
  }
}
