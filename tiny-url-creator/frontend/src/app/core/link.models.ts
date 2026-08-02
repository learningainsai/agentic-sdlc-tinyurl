/** Request body for POST /api/links (mirrors backend CreateLinkRequest). */
export interface CreateLinkRequest {
  url: string;
  alias?: string;
}

/** Success body for 201 Created (mirrors backend LinkResponse). */
export interface LinkResponse {
  code: string;
  shortUrl: string;
  originalUrl: string;
}

/** Error body returned by the backend GlobalExceptionHandler. */
export interface ApiErrorResponse {
  timestamp: string;
  status: number;
  error: string;
  message: string;
}
