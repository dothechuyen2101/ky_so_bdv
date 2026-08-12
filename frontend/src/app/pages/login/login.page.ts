import { CommonModule } from '@angular/common';
import { Component, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { PreferencesService, Language } from '../../core/preferences.service';
import { SessionService } from '../../core/session.service';

@Component({
  selector: 'app-login-page',
  imports: [CommonModule, FormsModule],
  templateUrl: './login.page.html',
})
export class LoginPage {
  private readonly router = inject(Router);
  private readonly session = inject(SessionService);
  readonly preferences = inject(PreferencesService);

  username = '';
  password = '';
  readonly errorMessage = signal('');
  readonly submitting = signal(false);

  t(text: string): string {
    return this.preferences.t(text);
  }

  setLanguage(language: Language): void {
    this.preferences.setLanguage(language);
  }

  login(): void {
    this.errorMessage.set('');
    this.submitting.set(true);
    this.session.login(this.username, this.password).subscribe({
      next: () => {
        this.password = '';
        this.submitting.set(false);
        void this.router.navigate([this.session.landingPath()]);
      },
      error: () => {
        this.session.logout();
        this.submitting.set(false);
        this.errorMessage.set(
          this.preferences.language() === 'vi'
            ? 'Tên đăng nhập hoặc mật khẩu không đúng'
            : 'Invalid username or password',
        );
      },
    });
  }
}
