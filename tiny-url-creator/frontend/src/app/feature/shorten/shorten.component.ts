import { Component, DestroyRef, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import {
  FormBuilder,
  FormGroup,
  ReactiveFormsModule,
  Validators,
} from '@angular/forms';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';

import { LinkResponse } from '../../core/link.models';
import { LinkService } from '../../core/link.service';

@Component({
  selector: 'app-shorten',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './shorten.component.html',
  styleUrl: './shorten.component.css',
})
export class ShortenComponent {
  private readonly fb = inject(FormBuilder);
  private readonly linkService = inject(LinkService);
  private readonly destroyRef = inject(DestroyRef);

  /** Optional alias must match the backend contract: 3-30 chars of [A-Za-z0-9_-]. */
  readonly form: FormGroup = this.fb.group({
    url: ['', [Validators.required, Validators.pattern(/^https?:\/\/.+/i)]],
    alias: ['', [Validators.pattern(/^[A-Za-z0-9_-]{3,30}$/)]],
    expiresAt: [''],
  });

  readonly result = signal<LinkResponse | null>(null);
  readonly errorMessage = signal<string | null>(null);
  readonly submitting = signal(false);
  readonly copied = signal(false);

  submit(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
    this.submitting.set(true);
    this.errorMessage.set(null);
    this.result.set(null);
    this.copied.set(false);

    const { url, alias, expiresAt } = this.form.getRawValue();
    this.linkService
      .createLink({
        url: url ?? '',
        alias: alias || undefined,
        // datetime-local is local wall-clock; convert to an absolute UTC instant (ADR-016).
        expiresAt: this.toUtcInstant(expiresAt),
      })
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: (response) => {
          this.result.set(response);
          this.submitting.set(false);
        },
        error: (error: Error) => {
          this.errorMessage.set(error.message);
          this.submitting.set(false);
        },
      });
  }

  /** Converts a `datetime-local` value (local time, no zone) to a UTC ISO-8601 instant, or undefined. */
  private toUtcInstant(localDateTime: string | null | undefined): string | undefined {
    if (!localDateTime) {
      return undefined;
    }
    const parsed = new Date(localDateTime);
    return Number.isNaN(parsed.getTime()) ? undefined : parsed.toISOString();
  }

  async copy(): Promise<void> {
    const current = this.result();
    if (!current) {
      return;
    }
    try {
      await navigator.clipboard.writeText(current.shortUrl);
      this.copied.set(true);
    } catch {
      this.copied.set(false);
    }
  }
}
