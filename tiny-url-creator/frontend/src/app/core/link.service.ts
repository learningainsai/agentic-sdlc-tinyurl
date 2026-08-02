import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable, throwError } from 'rxjs';
import { catchError } from 'rxjs/operators';

import { ApiErrorResponse, CreateLinkRequest, LinkResponse } from './link.models';

/**
 * Owns all HTTP access to the link API (ADR-002: components never call HttpClient directly).
 * Maps backend error bodies to a human-readable message the component can display inline.
 */
@Injectable({ providedIn: 'root' })
export class LinkService {
  private readonly endpoint = '/api/links';

  constructor(private readonly http: HttpClient) {}

  createLink(request: CreateLinkRequest): Observable<LinkResponse> {
    const body: CreateLinkRequest = { url: request.url };
    if (request.alias) {
      body.alias = request.alias;
    }
    return this.http
      .post<LinkResponse>(this.endpoint, body)
      .pipe(catchError((error: HttpErrorResponse) => throwError(() => this.toMessage(error))));
  }

  private toMessage(error: HttpErrorResponse): Error {
    const apiError = error.error as ApiErrorResponse | null;
    if (apiError && typeof apiError.message === 'string' && apiError.message.length > 0) {
      return new Error(apiError.message);
    }
    if (error.status === 0) {
      return new Error('Unable to reach the server. Please try again.');
    }
    return new Error('Something went wrong while shortening the URL.');
  }
}
