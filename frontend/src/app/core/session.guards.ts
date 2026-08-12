import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { SessionService } from './session.service';

export const authGuard: CanActivateFn = () => {
  const session = inject(SessionService);
  return session.isAuthenticated() || inject(Router).createUrlTree(['/login']);
};

export const adminGuard: CanActivateFn = () => {
  const session = inject(SessionService);
  return session.isAdmin() || inject(Router).createUrlTree(['/documents']);
};

export const operationsGuard: CanActivateFn = () => {
  const session = inject(SessionService);
  return session.canOperate() || inject(Router).createUrlTree(['/documents']);
};

export const guestGuard: CanActivateFn = () => {
  const session = inject(SessionService);
  if (!session.isAuthenticated()) {
    return true;
  }
  return inject(Router).createUrlTree([session.landingPath()]);
};
