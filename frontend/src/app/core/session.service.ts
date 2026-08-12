import { Injectable, computed, inject, signal } from '@angular/core';
import { Observable, tap } from 'rxjs';
import { T24rApiService } from '../t24r-api.service';

export interface CurrentUser {
  username: string;
  roles: string[];
}

@Injectable({ providedIn: 'root' })
export class SessionService {
  private readonly api = inject(T24rApiService);
  private readonly currentUser = signal<CurrentUser | null>(null);

  readonly user = this.currentUser.asReadonly();
  readonly isAuthenticated = computed(() => this.currentUser() !== null);
  readonly isAdmin = computed(() => this.currentUser()?.roles.includes('ADMIN') ?? false);
  readonly isMaker = computed(() => this.currentUser()?.roles.includes('MAKER') ?? false);
  readonly isChecker = computed(() => this.currentUser()?.roles.includes('CHECKER') ?? false);
  readonly canOperate = computed(() => this.isAdmin() || this.isMaker() || this.isChecker());

  landingPath(): string {
    return this.isAdmin() ? '/overview' : this.canOperate() ? '/operations' : '/documents';
  }

  roleLabel(): string {
    if (this.isAdmin()) return 'Administrator';
    if (this.isMaker()) return 'Maker';
    if (this.isChecker()) return 'Checker';
    return 'Document user';
  }

  login(username: string, password: string): Observable<CurrentUser> {
    return this.api
      .authenticate(username, password)
      .pipe(tap((user) => this.currentUser.set(user)));
  }

  logout(): void {
    this.api.clearCredentials();
    this.currentUser.set(null);
  }
}
