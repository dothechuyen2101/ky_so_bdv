import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { provideRouter, Router } from '@angular/router';
import { App } from './app';
import { appRoutes } from './app.routes';
import { SessionService } from './core/session.service';

describe('App', () => {
  beforeEach(async () => {
    localStorage.removeItem('t24r-language');
    localStorage.removeItem('t24r-theme');
    await TestBed.configureTestingModule({
      imports: [App],
      providers: [provideHttpClient(), provideHttpClientTesting(), provideRouter(appRoutes)],
    }).compileComponents();
  });

  async function renderApp(path = '/login') {
    const fixture = TestBed.createComponent(App);
    fixture.detectChanges();
    await TestBed.inject(Router).navigateByUrl(path);
    await fixture.whenStable();
    fixture.detectChanges();
    return fixture;
  }

  it('should create the app', () => {
    const fixture = TestBed.createComponent(App);
    const app = fixture.componentInstance;
    expect(app).toBeTruthy();
  });

  it('should render title', async () => {
    const fixture = await renderApp();
    const http = TestBed.inject(HttpTestingController);
    const compiled = fixture.nativeElement as HTMLElement;
    expect(compiled.querySelector('h1')?.textContent).toContain('Digital document operations');
    http.verify();
  });

  it('should restore and render the Vietnamese language', async () => {
    localStorage.setItem('t24r-language', 'vi');
    const fixture = await renderApp();

    expect((fixture.nativeElement as HTMLElement).textContent).toContain(
      'Vận hành chứng từ số, an toàn và kiểm soát.',
    );
  });

  it('should restore and toggle dark mode', async () => {
    localStorage.setItem('t24r-theme', 'dark');
    const fixture = await renderApp();

    expect(fixture.nativeElement.classList.contains('dark-mode')).toBe(true);
    (fixture.nativeElement as HTMLElement)
      .querySelector<HTMLButtonElement>('.theme-toggle')
      ?.click();
    await fixture.whenStable();
    fixture.detectChanges();
    expect(fixture.nativeElement.classList.contains('dark-mode')).toBe(false);
    expect(localStorage.getItem('t24r-theme')).toBe('light');
  });

  it('should route to documents and render backend results after login', async () => {
    const fixture = await renderApp();
    const http = TestBed.inject(HttpTestingController);
    const router = TestBed.inject(Router);
    const session = TestBed.inject(SessionService);
    let navigation: Promise<boolean> | undefined;
    session.login('viewer', 'password123').subscribe(() => {
      navigation = router.navigate(['/documents']);
    });

    http.expectOne('http://localhost:8080/api/v1/auth/me').flush({
      username: 'viewer',
      roles: ['USER'],
    });
    await navigation;
    fixture.detectChanges();
    http
      .expectOne((request) => request.url === 'http://localhost:8080/api/v1/documents')
      .flush({
        items: [
          {
            id: 'doc-1',
            documentCode: 'DOC-T24R-0001',
            transactionId: 'TX-0001',
            customerId: 'CIF001',
            accountNoMasked: '****1234',
            documentType: 'ACCOUNT_BALANCE',
            branchCode: 'HN01',
            businessType: 'RETAIL',
            status: 'SIGNED',
            reconciliationStatus: 'MATCHED',
          },
        ],
        totalElements: 1,
        totalPages: 1,
        page: 0,
        size: 20,
      });

    await fixture.whenStable();
    fixture.detectChanges();
    expect((fixture.nativeElement as HTMLElement).textContent).toContain('DOC-T24R-0001');
    http.verify();
  });
});
