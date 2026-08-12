import { CommonModule } from '@angular/common';
import { Component, OnInit, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { errorMessage } from '../../core/http-error';
import { PreferencesService } from '../../core/preferences.service';
import { T24rApiService } from '../../t24r-api.service';
import { UserAccount, UserRole } from '../../types';

@Component({
  selector: 'app-users-page',
  imports: [CommonModule, FormsModule],
  templateUrl: './users.page.html',
})
export class UsersPage implements OnInit {
  private readonly api = inject(T24rApiService);
  private readonly preferences = inject(PreferencesService);

  readonly users = signal<UserAccount[]>([]);
  readonly error = signal('');
  userForm = { username: '', displayName: '', password: '', role: 'USER' as UserRole };

  ngOnInit(): void {
    this.loadUsers();
  }

  t(text: string): string {
    return this.preferences.t(text);
  }

  loadUsers(): void {
    this.error.set('');
    this.api.users().subscribe({
      next: (users) => this.users.set(users),
      error: (error) => this.error.set(errorMessage(error)),
    });
  }

  createUser(): void {
    this.api.createUser(this.userForm).subscribe({
      next: () => {
        this.userForm = { username: '', displayName: '', password: '', role: 'USER' };
        this.loadUsers();
      },
      error: (error) => this.error.set(errorMessage(error)),
    });
  }

  toggleUser(user: UserAccount): void {
    this.api
      .updateUser(user.id, {
        displayName: user.displayName,
        role: user.role,
        active: !user.active,
        password: null,
      })
      .subscribe({
        next: () => this.loadUsers(),
        error: (error) => this.error.set(errorMessage(error)),
      });
  }
}
