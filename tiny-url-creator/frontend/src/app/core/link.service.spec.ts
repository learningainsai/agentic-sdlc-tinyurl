import {
  HttpClientTestingModule,
  HttpTestingController,
} from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';

import { LinkService } from './link.service';
import { LinkResponse } from './link.models';

describe('LinkService', () => {
  let service: LinkService;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [LinkService],
    });
    service = TestBed.inject(LinkService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => httpMock.verify());

  it('POSTs url only when no alias is supplied', () => {
    const expected: LinkResponse = {
      code: 'abc1234',
      shortUrl: 'http://localhost:8080/abc1234',
      originalUrl: 'https://example.com',
    };

    service.createLink({ url: 'https://example.com' }).subscribe((response) => {
      expect(response).toEqual(expected);
    });

    const req = httpMock.expectOne('/api/links');
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual({ url: 'https://example.com' });
    req.flush(expected);
  });

  it('includes alias in the request body when supplied', () => {
    service
      .createLink({ url: 'https://example.com', alias: 'promo' })
      .subscribe();

    const req = httpMock.expectOne('/api/links');
    expect(req.request.body).toEqual({ url: 'https://example.com', alias: 'promo' });
    req.flush({ code: 'promo', shortUrl: 'x', originalUrl: 'y' });
  });

  it('surfaces the backend message on a 409 conflict', (done) => {
    service.createLink({ url: 'https://example.com', alias: 'taken' }).subscribe({
      error: (error: Error) => {
        expect(error.message).toBe('Alias already in use');
        done();
      },
    });

    httpMock.expectOne('/api/links').flush(
      { timestamp: 't', status: 409, error: 'Conflict', message: 'Alias already in use' },
      { status: 409, statusText: 'Conflict' },
    );
  });
});
