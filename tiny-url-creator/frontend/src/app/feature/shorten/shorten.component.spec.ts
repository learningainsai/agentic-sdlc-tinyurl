import {
  HttpClientTestingModule,
  HttpTestingController,
} from '@angular/common/http/testing';
import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ShortenComponent } from './shorten.component';

describe('ShortenComponent', () => {
  let fixture: ComponentFixture<ShortenComponent>;
  let component: ShortenComponent;
  let httpMock: HttpTestingController;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ShortenComponent, HttpClientTestingModule],
    }).compileComponents();

    fixture = TestBed.createComponent(ShortenComponent);
    component = fixture.componentInstance;
    httpMock = TestBed.inject(HttpTestingController);
    fixture.detectChanges();
  });

  afterEach(() => httpMock.verify());

  // TEST-008: form submits URL and renders returned short link.
  it('submits the URL and renders the returned short link', () => {
    component.form.setValue({ url: 'https://example.com', alias: '', expiresAt: '' });

    component.submit();

    const req = httpMock.expectOne('/api/links');
    expect(req.request.method).toBe('POST');
    req.flush({
      code: 'abc1234',
      shortUrl: 'http://localhost:8080/abc1234',
      originalUrl: 'https://example.com',
    });
    fixture.detectChanges();

    const shortUrl = fixture.nativeElement.querySelector('.short-url') as HTMLAnchorElement;
    expect(shortUrl).toBeTruthy();
    expect(shortUrl.textContent).toContain('http://localhost:8080/abc1234');
    expect(component.errorMessage()).toBeNull();
  });

  // TEST-009: shows inline error on API error.
  it('shows an inline error when the API returns a conflict', () => {
    component.form.setValue({ url: 'https://example.com', alias: 'taken', expiresAt: '' });

    component.submit();

    httpMock.expectOne('/api/links').flush(
      { timestamp: 't', status: 409, error: 'Conflict', message: 'Alias already in use' },
      { status: 409, statusText: 'Conflict' },
    );
    fixture.detectChanges();

    const error = fixture.nativeElement.querySelector('.api-error') as HTMLElement;
    expect(error).toBeTruthy();
    expect(error.textContent).toContain('Alias already in use');
    expect(component.result()).toBeNull();
  });

  // TEST-017: datetime-local is converted to a UTC ISO instant before sending (ADR-016).
  it('converts an optional expiration date/time to a UTC instant in the request', () => {
    component.form.setValue({ url: 'https://example.com', alias: '', expiresAt: '2026-09-01T10:00' });

    component.submit();

    const req = httpMock.expectOne('/api/links');
    expect(req.request.body).toEqual({
      url: 'https://example.com',
      expiresAt: new Date('2026-09-01T10:00').toISOString(),
    });
    req.flush({ code: 'abc1234', shortUrl: 'http://localhost:8080/abc1234', originalUrl: 'https://example.com' });
  });

  it('does not call the API when the form is invalid', () => {
    component.form.setValue({ url: 'not-a-url', alias: '', expiresAt: '' });

    component.submit();

    httpMock.expectNone('/api/links');
    expect(component.form.controls['url'].touched).toBeTrue();
  });

  // TEST-018: the created link's expiry is displayed when present.
  it('displays the expiry of the created link', () => {
    component.form.setValue({ url: 'https://example.com', alias: '', expiresAt: '' });

    component.submit();

    httpMock.expectOne('/api/links').flush({
      code: 'abc1234',
      shortUrl: 'http://localhost:8080/abc1234',
      originalUrl: 'https://example.com',
      expiresAt: '2026-09-01T10:00:00Z',
    });
    fixture.detectChanges();

    const expiry = fixture.nativeElement.querySelector('.result-expiry') as HTMLElement;
    expect(expiry).toBeTruthy();
    expect(expiry.textContent).toContain('Expires');
  });
});
