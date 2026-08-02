/** Request body for POST /api/links (mirrors backend CreateLinkRequest). */
export interface CreateLinkRequest {
  url: string;
  alias?: string;
  /** Optional absolute UTC ISO-8601 instant; omitted means the link never expires (REQ-011/020). */
  expiresAt?: string;
}

/** Success body for 201 Created (mirrors backend LinkResponse). */
export interface LinkResponse {
  code: string;
  shortUrl: string;
  originalUrl: string;
  /** Present only when the link has an expiry (omitted by the backend otherwise). */
  expiresAt?: string;
}

/** Error body returned by the backend GlobalExceptionHandler. */
export interface ApiErrorResponse {
  timestamp: string;
  status: number;
  error: string;
  message: string;
}
